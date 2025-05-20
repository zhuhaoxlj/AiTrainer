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
package com.lindum.mlkit.vision

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.lindum.mlkit.vision.preference.PreferenceUtils
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

/**
 * Preview the camera image in the screen.
 */
class CameraSourcePreview(private val context: Context, attrs: AttributeSet?) : ViewGroup(
    context, attrs
) {
    private val surfaceView: SurfaceView
    private var startRequested = false
    private var surfaceAvailable = false
    private var cameraSource: CameraSource? = null

    private var overlay: GraphicOverlay? = null

    init {
        surfaceView = SurfaceView(context)
        surfaceView.getHolder().addCallback(SurfaceCallback())
        addView(surfaceView)
    }

    @Throws(IOException::class)
    private fun start(cameraSource: CameraSource?) {
        this.cameraSource = cameraSource

        if (this.cameraSource != null) {
            startRequested = true
            startIfReady()
        }
    }

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource?, overlay: GraphicOverlay?) {
        this.overlay = overlay
        start(cameraSource)
    }

    fun stop() {
        if (cameraSource != null) {
            cameraSource!!.stop()
        }
    }

    fun release() {
        if (cameraSource != null) {
            cameraSource!!.release()
            cameraSource = null
        }
        surfaceView.getHolder().getSurface().release()
    }

    @Throws(IOException::class, SecurityException::class)
    private fun startIfReady() {
        if (startRequested && surfaceAvailable) {
            if (PreferenceUtils.isCameraLiveViewportEnabled(context)) {
                cameraSource!!.start(surfaceView.getHolder())
            } else {
                cameraSource!!.start()
            }
            requestLayout()

            if (overlay != null) {
                val size = cameraSource!!.getPreviewSize()
                val min = min(size.getWidth().toDouble(), size.getHeight().toDouble()).toInt()
                val max = max(size.getWidth().toDouble(), size.getHeight().toDouble()).toInt()
                val isImageFlipped =
                    cameraSource!!.getCameraFacing() == CameraSource.CAMERA_FACING_FRONT
                if (this.isPortraitMode) {
                    // Swap width and height sizes when in portrait, since it will be rotated by 90 degrees.
                    // The camera preview and the image being processed have the same size.
                    overlay!!.setImageSourceInfo(min, max, isImageFlipped)
                } else {
                    overlay!!.setImageSourceInfo(max, min, isImageFlipped)
                }
                overlay!!.clear()
            }
            startRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            surfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            surfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var width = 320
        var height = 240
        if (cameraSource != null) {
            val size = cameraSource!!.getPreviewSize()
            if (size != null) {
                width = size.getWidth()
                height = size.getHeight()
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (this.isPortraitMode) {
            val tmp = width
            width = height
            height = tmp
        }

        val previewAspectRatio = width.toFloat() / height
        val layoutWidth = right - left
        val layoutHeight = bottom - top
        val layoutAspectRatio = layoutWidth.toFloat() / layoutHeight
        if (previewAspectRatio > layoutAspectRatio) {
            // The preview input is wider than the layout area. Fit the layout height and crop
            // the preview input horizontally while keep the center.
            val horizontalOffset = (previewAspectRatio * layoutHeight - layoutWidth).toInt() / 2
            surfaceView.layout(-horizontalOffset, 0, layoutWidth + horizontalOffset, layoutHeight)
        } else {
            // The preview input is taller than the layout area. Fit the layout width and crop the preview
            // input vertically while keep the center.
            val verticalOffset = (layoutWidth / previewAspectRatio - layoutHeight).toInt() / 2
            surfaceView.layout(0, -verticalOffset, layoutWidth, layoutHeight + verticalOffset)
        }
    }

    private val isPortraitMode: Boolean
        get() {
            val orientation = context.getResources().getConfiguration().orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }

            Log.d(
                TAG,
                "isPortraitMode returning false by default"
            )
            return false
        }

    companion object {
        private const val TAG = "MIDemoApp:Preview"
    }
}
