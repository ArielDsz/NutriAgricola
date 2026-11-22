package com.nutriagricola.app.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class RecordUploadScheduler(context: Context) {
    private val manager = WorkManager.getInstance(context.applicationContext)

    fun enqueue(userId: String, replaceWaiting: Boolean = false) {
        require(userId.isNotBlank())
        val policy = if (replaceWaiting) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.APPEND_OR_REPLACE
        manager.enqueueUniqueWork(workName(userId), policy, request(userId))
    }

    fun observe(userId: String) = manager.getWorkInfosForUniqueWorkFlow(workName(userId))

    companion object {
        private fun workName(userId: String) = "record-upload-" + userId

        fun request(userId: String): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<RecordUploadWorker>()
                .setInputData(workDataOf("userId" to userId))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
    }
}
