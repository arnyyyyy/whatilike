package com.example.whatilike

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.example.whatilike.data.DownloadWorker
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test



class DownloadWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun downloadWorker_doWork_resultSuccess() {
//        val testUrl = "https://www.ingofincke.com/wp-content/uploads/2020/08/IntroducingTheDark.jpg"
        val testUrl = "https://via.placeholder.com/150"
        val inputData = workDataOf("url" to testUrl)

        val worker = TestListenableWorkerBuilder<DownloadWorker>(context)
            .setInputData(inputData)
            .build()

        runBlocking {
            val result = worker.doWork()

            assertTrue(result is ListenableWorker.Result.Success)
        }
    }


    @Test
    fun downloadWorker_doWork_resultFailure_invalidUrl() {
        val invalidUrl = "invalid_url"
        val inputData = workDataOf("url" to invalidUrl)

        val worker = TestListenableWorkerBuilder<DownloadWorker>(context)
            .setInputData(inputData)
            .build()

        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Failure)
        }
    }

    @Test
    fun downloadWorker_doWork_resultFailure_missingUrl() {
        val worker = TestListenableWorkerBuilder<DownloadWorker>(context).build()

        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Failure)
        }
    }
}