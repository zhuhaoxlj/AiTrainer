package com.lindum.mlkit.vision.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.lindum.mlkit.vision.posedetector.PoseAngleDetector
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

            // 调整系数 - 根据图像测试调整以获得最佳匹配效果
            private const val SCALE_FACTOR = 1.5f       // 缩放因子
            private const val X_OFFSET_FACTOR = 3f    // X轴偏移因子
            private const val DOT_RADIUS = 8f           // 关键点半径
            private const val LINE_WIDTH = 10f          // 线条宽度

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
                                // 如果有pose信息，绘制关键点后再保存
                                maxUpFrame = if (pose != null) {
                                    drawPoseOnBitmap(currentFrame, pose)
                                } else {
                                    currentFrame.copy(currentFrame.config!!, true)
                                }
                            }
                        }
                    } else if (poseStage.contains("down")) {
                        saveMaxAngleFrame(maxUpFrame, "maxUpFrame_$maxUpFrame")
                    }
                }
                return ""
            }

            /**
             * 在Bitmap上绘制人体姿态关键点
             */
            private fun drawPoseOnBitmap(bitmap: Bitmap, pose: Pose): Bitmap {
                // 创建一个副本，避免修改原始bitmap
                val mutableBitmap = bitmap.copy(bitmap.config!!, true)
                val canvas = Canvas(mutableBitmap)
                
                // 设置画笔
                val whitePaint = Paint().apply {
                    color = Color.WHITE
                    strokeWidth = LINE_WIDTH
                }
                val leftPaint = Paint().apply {
                    color = Color.GREEN
                    strokeWidth = LINE_WIDTH
                }
                val rightPaint = Paint().apply {
                    color = Color.YELLOW
                    strokeWidth = LINE_WIDTH
                }
                val dotPaint = Paint().apply {
                    color = Color.WHITE
                    strokeWidth = DOT_RADIUS
                }
                
                // 获取所有关键点
                val landmarks = pose.allPoseLandmarks
                if (landmarks.isEmpty()) return mutableBitmap
                
                // 提取常用关键点
                val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
                val leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
                val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
                val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
                val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
                val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
                val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
                val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
                val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
                val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
                val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)
                
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
                val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
                val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
                val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
                val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
                val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
                
                val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
                val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
                val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
                val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
                val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
                val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
                val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
                val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
                val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
                val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)
                
                // 绘制所有关键点
                for (landmark in landmarks) {
                    val (x, y) = transformCoordinate(landmark.position.x, landmark.position.y, bitmap.width)
                    canvas.drawCircle(x, y, DOT_RADIUS, dotPaint)
                }
                
                // 绘制连接线
                // 面部
                drawLine(canvas, nose, leftEyeInner, whitePaint, bitmap.width)
                drawLine(canvas, leftEyeInner, leftEye, whitePaint, bitmap.width)
                drawLine(canvas, leftEye, leftEyeOuter, whitePaint, bitmap.width)
                drawLine(canvas, leftEyeOuter, leftEar, whitePaint, bitmap.width)
                drawLine(canvas, nose, rightEyeInner, whitePaint, bitmap.width)
                drawLine(canvas, rightEyeInner, rightEye, whitePaint, bitmap.width)
                drawLine(canvas, rightEye, rightEyeOuter, whitePaint, bitmap.width)
                drawLine(canvas, rightEyeOuter, rightEar, whitePaint, bitmap.width)
                drawLine(canvas, leftMouth, rightMouth, whitePaint, bitmap.width)
                
                // 身体
                drawLine(canvas, leftShoulder, rightShoulder, whitePaint, bitmap.width)
                drawLine(canvas, leftHip, rightHip, whitePaint, bitmap.width)
                
                // 左侧身体
                drawLine(canvas, leftShoulder, leftElbow, leftPaint, bitmap.width)
                drawLine(canvas, leftElbow, leftWrist, leftPaint, bitmap.width)
                drawLine(canvas, leftShoulder, leftHip, leftPaint, bitmap.width)
                drawLine(canvas, leftHip, leftKnee, leftPaint, bitmap.width)
                drawLine(canvas, leftKnee, leftAnkle, leftPaint, bitmap.width)
                drawLine(canvas, leftWrist, leftThumb, leftPaint, bitmap.width)
                drawLine(canvas, leftWrist, leftPinky, leftPaint, bitmap.width)
                drawLine(canvas, leftWrist, leftIndex, leftPaint, bitmap.width)
                drawLine(canvas, leftIndex, leftPinky, leftPaint, bitmap.width)
                drawLine(canvas, leftAnkle, leftHeel, leftPaint, bitmap.width)
                drawLine(canvas, leftHeel, leftFootIndex, leftPaint, bitmap.width)
                
                // 右侧身体
                drawLine(canvas, rightShoulder, rightElbow, rightPaint, bitmap.width)
                drawLine(canvas, rightElbow, rightWrist, rightPaint, bitmap.width)
                drawLine(canvas, rightShoulder, rightHip, rightPaint, bitmap.width)
                drawLine(canvas, rightHip, rightKnee, rightPaint, bitmap.width)
                drawLine(canvas, rightKnee, rightAnkle, rightPaint, bitmap.width)
                drawLine(canvas, rightWrist, rightThumb, rightPaint, bitmap.width)
                drawLine(canvas, rightWrist, rightPinky, rightPaint, bitmap.width)
                drawLine(canvas, rightWrist, rightIndex, rightPaint, bitmap.width)
                drawLine(canvas, rightIndex, rightPinky, rightPaint, bitmap.width)
                drawLine(canvas, rightAnkle, rightHeel, rightPaint, bitmap.width)
                drawLine(canvas, rightHeel, rightFootIndex, rightPaint, bitmap.width)
                
                return mutableBitmap
            }
            
            /**
             * 在Canvas上绘制两点之间的连线
             */
            private fun drawLine(
                canvas: Canvas,
                startLandmark: PoseLandmark?,
                endLandmark: PoseLandmark?,
                paint: Paint,
                imageWidth: Int
            ) {
                if (startLandmark == null || endLandmark == null) return
                
                val (startX, startY) = transformCoordinate(
                    startLandmark.position.x,
                    startLandmark.position.y,
                    imageWidth
                )
                
                val (endX, endY) = transformCoordinate(
                    endLandmark.position.x,
                    endLandmark.position.y,
                    imageWidth
                )
                
                canvas.drawLine(startX, startY, endX, endY, paint)
            }
            
            /**
             * 转换坐标 - 使用从图像测试中确定的简单缩放和偏移
             */
            private fun transformCoordinate(x: Float, y: Float, imageWidth: Int): Pair<Float, Float> {
                return Pair(
                    x * SCALE_FACTOR - imageWidth / X_OFFSET_FACTOR,
                    y * SCALE_FACTOR
                )
            }

            fun reset() {
                maxCombineAngle = 0
                maxUpFrame?.recycle()
                maxAngle4Frame?.recycle()
                maxUpFrame = null
                maxAngle4Frame = null
            }

            private fun saveMaxAngleFrame(bitmap: Bitmap?, filename: String) {
                bitmap?.let {
                    try {
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
                        it.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        reset()
                        Log.d("PoseCapture", "Saved max angle frame to: ${file.absolutePath}")
                    } catch (e: Exception) {
                        Log.e("PoseCapture", "Error saving max angle frame", e)
                    }
                }
            }
        }
    }
}