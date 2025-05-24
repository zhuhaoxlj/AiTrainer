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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 用于在叠加视图中渲染推理信息（延迟、FPS、分辨率）的图形实例
 */
class InferenceInfoGraphic(
    private val overlay: GraphicOverlay,
    private val frameLatency: Long,
    private val detectorLatency: Long,
// Only valid when a stream of input images is being processed. Null for single image mode.
    private val framesPerSecond: Int?
) : GraphicOverlay.Graphic(overlay) {
    private val textPaint: Paint = Paint()
    private var poseName: String? = null

    private var showLatencyInfo = true

    init {
        textPaint.setColor(TEXT_COLOR)
        textPaint.textSize = TEXT_SIZE
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
        postInvalidate()
    }

    /**
     * Creates an [InferenceInfoGraphic] to only display image size.
     */
    constructor(overlay: GraphicOverlay) : this(overlay, 0, 0, null) {
        showLatencyInfo = false
    }

    @Synchronized
    override fun draw(canvas: Canvas?) {
        val x: Float = TEXT_SIZE * 0.5f
        val y: Float = TEXT_SIZE * 1.5f

        canvas?.drawText(
            "画面尺寸 " + overlay.imageHeight + "x" + overlay.imageWidth,
            x,
            y,
            textPaint
        )

        if (!showLatencyInfo) {
            return
        }
        // Draw FPS (if valid) and inference latency
        if (framesPerSecond != null) {
            canvas?.drawText(
                "FPS帧率  $framesPerSecond  单帧处理时长  $frameLatency ms",
                x,
                y + TEXT_SIZE,
                textPaint
            )
        } else {
            canvas?.drawText("单帧处理时长  $frameLatency ms", x, y + TEXT_SIZE, textPaint)
        }
        canvas?.drawText(
            "检测器处理时长: $detectorLatency ms",
            x,
            y + TEXT_SIZE * 2,
            textPaint
        )
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 60.0f
    }
}
