package com.nutriagricola.app.data.remote

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.util.UUID

class PhotoFileTransfer(context: Context) {
    private val root = File(context.applicationContext.filesDir, "foliar_photos")

    private fun directory(recordId: String): File {
        require(UUID.fromString(recordId).toString() == recordId)
        return File(root, recordId).canonicalFile
    }

    private fun validate(file: File): String {
        require(file.isFile && file.length() in 1..10L * 1024 * 1024) {
            "La foto debe existir y ocupar hasta 10 MB."
        }
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        require(options.outMimeType in listOf("image/jpeg", "image/png") &&
            options.outWidth > 0 && options.outHeight > 0 &&
            options.outWidth.toLong() * options.outHeight <= 25_000_000L) {
            "Usa fotos JPEG o PNG de hasta 25 megapíxeles."
        }
        return requireNotNull(options.outMimeType)
    }

    suspend fun uploadPart(recordId: String, path: String): MultipartBody.Part = withContext(Dispatchers.IO) {
        val uri = path.toUri()
        require(uri.scheme == "file") { "La foto no está guardada en el dispositivo." }
        val file = File(requireNotNull(uri.path)).canonicalFile
        require(file.parentFile == directory(recordId)) { "La foto no pertenece a este análisis." }
        val mime = validate(file)
        MultipartBody.Part.createFormData("file", if (mime == "image/png") "photo.png" else "photo.jpg",
            file.asRequestBody(mime.toMediaType()))
    }

    suspend fun isAvailable(recordId: String, path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = path.toUri()
            if (uri.scheme != "file") return@withContext false
            val file = File(requireNotNull(uri.path)).canonicalFile
            if (file.parentFile != directory(recordId)) return@withContext false
            validate(file)
            true
        } catch (_: IllegalArgumentException) { false }
    }

    suspend fun saveDownloaded(recordId: String, photoId: String, response: ResponseBody): String =
        withContext(Dispatchers.IO) {
            require(UUID.fromString(photoId).toString() == photoId)
            val folder = directory(recordId)
            check(folder.isDirectory || folder.mkdirs())
            val target = File(folder, "remote-$photoId.img")
            val temporary = File(folder, "remote-$photoId-${UUID.randomUUID()}.part")
            try {
                response.use { body ->
                    body.byteStream().use { source ->
                        temporary.outputStream().use { destination ->
                            val buffer = ByteArray(8192)
                            var total = 0L
                            while (true) {
                                val count = source.read(buffer)
                                if (count < 0) break
                                total += count
                                require(total <= 10L * 1024 * 1024) { "La foto supera 10 MB." }
                                destination.write(buffer, 0, count)
                            }
                        }
                    }
                }
                validate(temporary)
                java.nio.file.Files.move(temporary.toPath(), target.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                target.toUri().toString()
            } finally { temporary.delete() }
        }
}
