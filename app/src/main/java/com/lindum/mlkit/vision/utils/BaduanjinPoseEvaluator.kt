package com.lindum.mlkit.vision.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.lindum.mlkit.vision.GraphicOverlay
import com.lindum.mlkit.vision.posedetector.PoseAngleDetector
import com.lindum.mlkit.vision.posedetector.PoseGraphic
import com.lindum.mlkit.vision.posedetector.PosePart
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 八段锦动作检测与评分
 *
 * @author zhuhao
 * @date  20:07
 **/
object BaduanjinPoseEvaluator {

    /**
     * 两手托天理三焦 检测器
     */
    class Pose1 {
        companion object {
            var maxCombineAngle = 0

            private var maxUpFrame: Bitmap? = null
            private var maxAngle4Frame: Bitmap? = null
            private var currentPose: Pose? = null

            fun getScore(
                poseStage: String,
                monitoringList: List<PosePart>,
                detector: PoseAngleDetector,
                currentFrame: Bitmap? = null,
                pose: Pose? = null
            ): String {
                Log.i("test_stage", poseStage)
                with(detector) {
                    val angle1 = getAngle(16, 24, 23) // 右手腕，右胯，左胯
                    val angle2 = getAngle(15, 23, 24) // 左手腕，左胯，右胯
                    val angle3 = getAngle(15, 11, 23) // 左手腕，左肩，左胯
                    val angle4 = getAngle(16, 12, 24) // 右手腕，右肩，右胯
                    if (poseStage.contains("up")) {
                        if (maxCombineAngle < angle3 + angle4) {
                            maxCombineAngle = angle3 + angle4
                            if (currentFrame != null) {
                                maxUpFrame = currentFrame.copy(currentFrame.config!!, true)
                                currentPose = pose
                            }
                        }
                    } else if (poseStage.contains("down")) {
                        saveMaxAngleFrame(maxUpFrame, "maxUpFrame_$maxUpFrame")
                    }
                }
                return ""
            }

            fun reset() {
                maxCombineAngle = 0
                maxUpFrame?.recycle()
                maxAngle4Frame?.recycle()
                maxUpFrame = null
                maxAngle4Frame = null
                currentPose = null
            }

            private fun saveMaxAngleFrame(bitmap: Bitmap?, filename: String) {
                bitmap?.let {
                    try {
                        // Draw pose keypoints on the bitmap if pose is available
                        val frameWithPose = if (currentPose != null) {
                            drawPoseOnBitmap(it, currentPose!!)
                        } else {
                            it
                        }

                        val directory = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "AiTrainer"
                        )
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }

                        val timeStamp =
                            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val file = File(directory, "${filename}_$timeStamp.jpg")

                        val outputStream = FileOutputStream(file)
                        frameWithPose.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        reset()
                        Log.d("PoseCapture", "Saved max angle frame to: ${file.absolutePath}")
                    } catch (e: Exception) {
                        Log.e("PoseCapture", "Error saving max angle frame", e)
                    }
                }
            }

            /**
             * Draw pose keypoints on the bitmap
             */
            private fun drawPoseOnBitmap(bitmap: Bitmap, pose: Pose): Bitmap {
                // Create a new bitmap to draw on
                val resultBitmap = bitmap.copy(bitmap.config!!, true)
                val canvas = Canvas(resultBitmap)
                
                // Create transformers similar to GraphicOverlay
                val imageWidth = bitmap.width
                val imageHeight = bitmap.height
                
                // Determine scale factor
                val scaleFactor = imageWidth.toFloat() / imageHeight.toFloat()
                
                // Create coordinate transformation functions like in GraphicOverlay
                fun scale(imagePixel: Float): Float {
                    return imagePixel * scaleFactor
                }
                
                fun translateX(x: Float): Float {
                    // This is the key transformation that correctly positions X coordinates
                    return x
                }
                
                fun translateY(y: Float): Float {
                    // This is the key transformation that correctly positions Y coordinates
                    return y
                }
                
                // Paint for drawing landmarks
                val pointPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    style = android.graphics.Paint.Style.FILL
                    strokeWidth = 4f
                }
                
                // Paint for drawing connections
                val linePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GREEN
                    strokeWidth = 4f
                    style = android.graphics.Paint.Style.STROKE
                }
                
                // Draw all landmarks
                for (landmark in pose.allPoseLandmarks) {
                    canvas.drawCircle(
                        landmark.position.x,
                        landmark.position.y,
                        8f,
                        pointPaint
                    )
                }
                
                // Define connections (pairs of landmarks to connect with lines)
                val connections = listOf(
                    // Face connections
                    Pair(PoseLandmark.NOSE, PoseLandmark.LEFT_EYE_INNER),
                    Pair(PoseLandmark.LEFT_EYE_INNER, PoseLandmark.LEFT_EYE),
                    Pair(PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER),
                    Pair(PoseLandmark.LEFT_EYE_OUTER, PoseLandmark.LEFT_EAR),
                    Pair(PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE_INNER),
                    Pair(PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.RIGHT_EYE),
                    Pair(PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EYE_OUTER),
                    Pair(PoseLandmark.RIGHT_EYE_OUTER, PoseLandmark.RIGHT_EAR),
                    
                    // Body connections
                    Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
                    Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
                    Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
                    Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
                    Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
                    Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
                    Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
                    Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
                    Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
                    Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
                    Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
                    Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
                )
                
                // Draw each connection
                for (connection in connections) {
                    val startLandmark = pose.getPoseLandmark(connection.first)
                    val endLandmark = pose.getPoseLandmark(connection.second)
                    
                    if (startLandmark != null && endLandmark != null) {
                        canvas.drawLine(
                            startLandmark.position.x,
                            startLandmark.position.y,
                            endLandmark.position.x,
                            endLandmark.position.y,
                            linePaint
                        )
                    }
                }
                
                return resultBitmap
            }
        }
    }
}