/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluromatic.data

import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow

interface BluromaticRepository {
    val workManager: WorkManager
    val outputWorkInfo: Flow<WorkInfo?>

    fun applyBlur(blurLevel: Int): WorkContinuation {
        // Add WorkRequest to Cleanup temporary images
        val continuation = workManager.beginWith(
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )

        // Add WorkRequest to blur the image
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(createInputDataForWork(blurLevel))
        continuation = continuation.then(blurBuilder.build())

        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .build()
        continuation = continuation.then(save)
        continuation.enqueue()

        return continuation.then(blurBuilder.build())
    }

    fun cancelWork()

    // Helper function to create input data
    private fun createInputDataForWork(blurLevel: Int): Data {
        return workDataOf(KEY_BLUR_LEVEL to blurLevel)
    }
}