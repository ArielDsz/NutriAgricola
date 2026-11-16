package com.nutriagricola.app.data.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PendingCameraPhoto(val path: String, val captureUri: Uri)

class FoliarPhotoStorage(
    context: Context,
    private val isRecordSaved: suspend (String) -> Boolean
) {
    private val appContext = context.applicationContext
    private val root = File(appContext.filesDir, "foliar_photos")

    private fun directory(recordId: String): File {
        require(UUID.fromString(recordId).toString() == recordId)
        return File(root, recordId)
    }

    private fun newFile(recordId: String, extension: String): File {
        val directory = directory(recordId)
        check(directory.isDirectory || directory.mkdirs()) { "Could not create photo directory" }
        return File(directory, "${UUID.randomUUID()}.$extension")
    }

    private fun ownedFile(recordId: String, path: String): File {
        val uri = path.toUri()
        require(uri.scheme == "file")
        val file = File(requireNotNull(uri.path)).canonicalFile
        require(file.parentFile == directory(recordId).canonicalFile)
        return file
    }

    private fun isImage(file: File): Boolean {
        if (!file.isFile || file.length() == 0L) return false
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options.outWidth > 0 && options.outHeight > 0
    }

    suspend fun createCameraPhoto(recordId: String): PendingCameraPhoto = withContext(Dispatchers.IO) {
        val file = newFile(recordId, "jpg")
        try {
            check(file.createNewFile())
            PendingCameraPhoto(
                path = file.toUri().toString(),
                captureUri = FileProvider.getUriForFile(
                    appContext, "${appContext.packageName}.foliarphotos", file
                )
            )
        } catch (exception: Exception) {
            file.delete()
            throw exception
        }
    }

    suspend fun importPhoto(recordId: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val mimeType = appContext.contentResolver.getType(uri)
        require(mimeType == null || mimeType.startsWith("image/")) { "Not an image" }
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?.takeIf { it.matches(Regex("[A-Za-z0-9]+")) } ?: "img"
        val file = newFile(recordId, extension)
        try {
            val input = checkNotNull(appContext.contentResolver.openInputStream(uri))
            input.use { source -> file.outputStream().use { target -> source.copyTo(target) } }
            check(isImage(file)) { "Unreadable image" }
            file.toUri().toString()
        } catch (exception: Exception) {
            file.delete()
            throw exception
        }
    }

    suspend fun isValidPhoto(recordId: String, path: String): Boolean = withContext(Dispatchers.IO) {
        isImage(ownedFile(recordId, path))
    }

    suspend fun discardPhoto(recordId: String, path: String) = withContext(Dispatchers.IO) {
        // A failed Room check also prevents deletion. Only this draft's files are eligible.
        if (!isRecordSaved(recordId)) ownedFile(recordId, path).delete()
        Unit
    }

    suspend fun discardDraft(recordId: String) = withContext(Dispatchers.IO) {
        if (!isRecordSaved(recordId)) {
            val directory = directory(recordId)
            directory.listFiles()?.filter { it.isFile }?.forEach { it.delete() }
            directory.delete()
        }
    }
}
