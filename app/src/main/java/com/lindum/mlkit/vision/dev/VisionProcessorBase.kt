/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lindum.mlkit.vision.dev

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build.VERSION_CODES
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.gms.tasks.Tasks
import com.google.android.odml.image.BitmapMlImageBuilder
import com.google.android.odml.image.ByteBufferMlImageBuilder
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.lindum.mlkit.vision.dev.preference.PreferenceUtils
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max
import kotlin.math.min

/**
 * Abstract base class for vision frame processors. Subclasses need to implement [ ][.onSuccess] to define what they want to with the detection results and
 * [.detectInImage] to specify the detector object.
 *
 * @param <T> The type of the detected feature.
</T> */
abstract class VisionProcessorBase<T> protected constructor(context: Context) :
    VisionImageProcessor {
    private val activityManager: ActivityManager
    private val fpsTimer = Timer()
    private val executor: ScopedExecutor
    private val temperatureMonitor: TemperatureMonitor

    // Whether this processor is already shut down
    private var isShutdown = false

    // Used to calculate latency, running in the same thread, no sync needed.
    private var numRuns = 0
    private var totalFrameMs: Long = 0
    private var maxFrameMs: Long = 0
    private var minFrameMs = Long.Companion.MAX_VALUE
    private var totalDetectorMs: Long = 0
    private var maxDetectorMs: Long = 0
    private var minDetectorMs = Long.Companion.MAX_VALUE

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    init {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)
        fpsTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },  /* delay= */
            0,  /* period= */
            1000
        )
        temperatureMonitor = TemperatureMonitor(context)
    }

    // -----------------Code for processing single still image----------------------------------------
    override fun processBitmap(bitmap: Bitmap?, graphicOverlay: GraphicOverlay?) {
        val frameStartMs = SystemClock.elapsedRealtime()

        if (isMlImageEnabled(graphicOverlay?.getContext())) {
            val mlImage = BitmapMlImageBuilder(bitmap!!).build()
            requestDetectInImage(
                mlImage,
                graphicOverlay!!,  /* originalCameraImage= */
                null,  /* shouldShowFps= */
                false,
                frameStartMs
            )
            mlImage.close()

            return
        }

        requestDetectInImage(
            InputImage.fromBitmap(bitmap!!, 0),
            graphicOverlay!!,  /* originalCameraImage= */
            null,  /* shouldShowFps= */
            false,
            frameStartMs
        )
    }

    // -----------------Code for processing live preview frame from Camera1 API-----------------------
    @Synchronized
    override fun processByteBuffer(
        data: ByteBuffer?, frameMetadata: FrameMetadata?, graphicOverlay: GraphicOverlay?
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay!!)
        }
    }

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay) {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null && !isShutdown) {
            processImage(processingImage!!, processingMetaData!!, graphicOverlay)
        }
    }

    private fun processImage(
        data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay
    ) {
        val frameStartMs = SystemClock.elapsedRealtime()

        // If live viewport is on (that is the underneath surface view takes care of the camera preview
        // drawing), skip the unnecessary bitmap creation that used for the manual preview drawing.
        val bitmap =
            if (PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.getContext()))
                null
            else
                BitmapUtils.getBitmap(data, frameMetadata)

        if (isMlImageEnabled(graphicOverlay.getContext())) {
            val mlImage =
                ByteBufferMlImageBuilder(
                    data,
                    frameMetadata.width,
                    frameMetadata.height,
                    MlImage.IMAGE_FORMAT_NV21
                )
                    .setRotation(frameMetadata.rotation)
                    .build()

            requestDetectInImage(
                mlImage,
                graphicOverlay,
                bitmap,  /* shouldShowFps= */
                true,
                frameStartMs
            )
                .addOnSuccessListener(
                    executor,
                    OnSuccessListener { results: T? -> processLatestImage(graphicOverlay) })

            // This is optional. Java Garbage collection can also close it eventually.
            mlImage.close()
            return
        }

        requestDetectInImage(
            InputImage.fromByteBuffer(
                data,
                frameMetadata.width,
                frameMetadata.height,
                frameMetadata.rotation,
                InputImage.IMAGE_FORMAT_NV21
            ),
            graphicOverlay,
            bitmap,  /* shouldShowFps= */
            true,
            frameStartMs
        )
            .addOnSuccessListener(
                executor,
                OnSuccessListener { results: T? -> processLatestImage(graphicOverlay) })
    }

    // -----------------Code for processing live preview frame from CameraX API-----------------------
    @RequiresApi(VERSION_CODES.KITKAT)
    @ExperimentalGetImage
    override fun processImageProxy(image: ImageProxy?, graphicOverlay: GraphicOverlay?) {
        val frameStartMs = SystemClock.elapsedRealtime()
        if (isShutdown) {
            image!!.close()
            return
        }

        var bitmap: Bitmap? = null
        if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay!!.getContext())) {
            bitmap = BitmapUtils.getBitmap(image!!)
        }

        if (isMlImageEnabled(graphicOverlay.getContext())) {
            val mlImage =
                MediaMlImageBuilder(image!!.getImage()!!)
                    .setRotation(image!!.getImageInfo().getRotationDegrees())
                    .build()

            requestDetectInImage(
                mlImage,
                graphicOverlay,  /* originalCameraImage= */
                bitmap,  /* shouldShowFps= */
                true,
                frameStartMs
            ) // When the image is from CameraX analysis use case, must call image.close() on received
                // images when finished using them. Otherwise, new images may not be received or the
                // camera may stall.
                // Currently MlImage doesn't support ImageProxy directly, so we still need to call
                // ImageProxy.close() here.
                .addOnCompleteListener(OnCompleteListener { results: Task<T?>? -> image.close() })
            return
        }

        requestDetectInImage(
            InputImage.fromMediaImage(
                image!!.getImage()!!,
                image!!.getImageInfo().getRotationDegrees()
            ),
            graphicOverlay,  /* originalCameraImage= */
            bitmap,  /* shouldShowFps= */
            true,
            frameStartMs
        ) // When the image is from CameraX analysis use case, must call image.close() on received
            // images when finished using them. Otherwise, new images may not be received or the camera
            // may stall.
            .addOnCompleteListener(OnCompleteListener { results: Task<T?>? -> image.close() })
    }

    // -----------------Common processing logic-------------------------------------------------------
    private fun requestDetectInImage(
        image: InputImage?,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<T?> {
        return setUpListener(
            detectInImage(image)!!, graphicOverlay, originalCameraImage, shouldShowFps, frameStartMs
        )
    }

    private fun requestDetectInImage(
        image: MlImage?,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<T?> {
        return setUpListener(
            detectInImage(image)!!, graphicOverlay, originalCameraImage, shouldShowFps, frameStartMs
        )
    }

    private fun setUpListener(
        task: Task<T?>,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<T?> {
        val detectorStartMs = SystemClock.elapsedRealtime()
        return task.addOnSuccessListener(
            executor,
            OnSuccessListener { results: T? ->
                val endMs = SystemClock.elapsedRealtime()
                val currentFrameLatencyMs = endMs - frameStartMs
                val currentDetectorLatencyMs = endMs - detectorStartMs
                if (numRuns >= 500) {
                    resetLatencyStats()
                }
                numRuns++
                frameProcessedInOneSecondInterval++
                totalFrameMs += currentFrameLatencyMs
                maxFrameMs = max(currentFrameLatencyMs.toDouble(), maxFrameMs.toDouble()).toLong()
                minFrameMs = min(currentFrameLatencyMs.toDouble(), minFrameMs.toDouble()).toLong()
                totalDetectorMs += currentDetectorLatencyMs
                maxDetectorMs =
                    max(currentDetectorLatencyMs.toDouble(), maxDetectorMs.toDouble()).toLong()
                minDetectorMs =
                    min(currentDetectorLatencyMs.toDouble(), minDetectorMs.toDouble()).toLong()

                // Only log inference info once per second. When frameProcessedInOneSecondInterval is
                // equal to 1, it means this is the first frame processed during the current second.
                if (frameProcessedInOneSecondInterval == 1) {
                    Log.d(TAG, "Num of Runs: " + numRuns)
                    Log.d(
                        TAG,
                        ("Frame latency: max="
                                + maxFrameMs
                                + ", min="
                                + minFrameMs
                                + ", avg="
                                + totalFrameMs / numRuns)
                    )
                    Log.d(
                        TAG,
                        ("Detector latency: max="
                                + maxDetectorMs
                                + ", min="
                                + minDetectorMs
                                + ", avg="
                                + totalDetectorMs / numRuns)
                    )
                    val mi = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(mi)
                    val availableMegs = mi.availMem / 0x100000L
                    Log.d(TAG, "Memory available in system: " + availableMegs + " MB")
                    temperatureMonitor.logTemperature()
                }

                graphicOverlay.clear()
                if (originalCameraImage != null) {
                    graphicOverlay.add(CameraImageGraphic(graphicOverlay, originalCameraImage))
                }
                this@VisionProcessorBase.onSuccess(results!!, graphicOverlay)
                if (!PreferenceUtils.shouldHideDetectionInfo(graphicOverlay.context)) {
                    graphicOverlay.add(
                        InferenceInfoGraphic(
                            graphicOverlay,
                            currentFrameLatencyMs,
                            currentDetectorLatencyMs,
                            if (shouldShowFps) framesPerSecond else null
                        )
                    )
                }
                graphicOverlay.postInvalidate()
            })
            .addOnFailureListener(
                executor,
                OnFailureListener { e: Exception? ->
                    graphicOverlay.clear()
                    graphicOverlay.postInvalidate()
                    val error = "Failed to process. Error: " + e!!.localizedMessage
                    Toast.makeText(
                        graphicOverlay.context,
                        error + "\nCause: " + e.cause,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.d(TAG, error)
                    e.printStackTrace()
                    this@VisionProcessorBase.onFailure(e)
                })
    }

    override fun stop() {
        executor.shutdown()
        isShutdown = true
        resetLatencyStats()
        fpsTimer.cancel()
        temperatureMonitor.stop()
    }

    private fun resetLatencyStats() {
        numRuns = 0
        totalFrameMs = 0
        maxFrameMs = 0
        minFrameMs = Long.Companion.MAX_VALUE
        totalDetectorMs = 0
        maxDetectorMs = 0
        minDetectorMs = Long.Companion.MAX_VALUE
    }

    protected abstract fun detectInImage(image: InputImage?): Task<T?>?

    protected open fun detectInImage(image: MlImage?): Task<T?>? {
        return Tasks.forException<T?>(
            MlKitException(
                "MlImage is currently not demonstrated for this feature",
                MlKitException.INVALID_ARGUMENT
            )
        )
    }

    protected abstract fun onSuccess(results: T, graphicOverlay: GraphicOverlay)

    protected abstract fun onFailure(e: Exception)

    protected open fun isMlImageEnabled(context: Context?): Boolean {
        return false
    }

    companion object {
        protected const val MANUAL_TESTING_LOG: String = "LogTagForTest"
        private const val TAG = "VisionProcessorBase"
    }
}
