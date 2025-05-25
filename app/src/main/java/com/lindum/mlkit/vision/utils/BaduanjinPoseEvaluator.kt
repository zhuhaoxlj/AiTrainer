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
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.minAngle
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
    // 存储需要标红的身体部位索引
    private val warningParts = mutableSetOf<IntArray>()
    private var isUpWarn = true

    // 存储最近发出的警告，用于防止重复警告
    private val recentWarnings = mutableMapOf<String, Long>()

    // 警告冷却时间 (毫秒)
    private const val WARNING_COOLDOWN = 2000L

    private fun saveFrame(isUp: Boolean, bitmap: Bitmap?, filename: String) {
        if (isUp) {
            minAngle = 999
            // Don't reset maxCombineAngle here to preserve the value between phases
        } else {
            // Only reset maxCombineAngle when explicitly needed, not during normal saving
            // maxCombineAngle = 0
        }
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
                // Don't call reset() here as it will clear important angle values
                // reset()
                Log.d("PoseCapture", "Saved max angle frame to: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("PoseCapture", "Error saving max angle frame", e)
            }
        }
    }

    /**
     * 添加报错部位
     *
     * @param partIndex 报错部位的索引
     */
    fun addWarnPart(partIndex: Int) {
        if (partIndex == 12) {
            warningParts.add(intArrayOf(25, 27, 31))
        } else if (partIndex == 11) {
            warningParts.add(intArrayOf(26, 28, 32))
        } else if (partIndex == 10) { // 左膝
            warningParts.add(intArrayOf(23, 25, 27))
        } else if (partIndex == 9) { // 右膝
            warningParts.add(intArrayOf(24, 26, 28))
        } else if (partIndex == 8) {
            warningParts.add(intArrayOf(11, 23, 25))
        } else if (partIndex == 7) {
            warningParts.add(intArrayOf(12, 24, 26))
        } else if (partIndex == 6) {
            warningParts.add(intArrayOf(13, 15, 19))
        } else if (partIndex == 5) { // 左手臂
            warningParts.add(intArrayOf(11, 13, 15))
        } else if (partIndex == 4) {
            warningParts.add(intArrayOf(13, 11, 23))
        } else if (partIndex == 3) {
            warningParts.add(intArrayOf(14, 12, 24))
        } else if (partIndex == 2) { // 右手臂
            warningParts.add(intArrayOf(12, 14, 16))
        } else if (partIndex == 1) {
            warningParts.add(intArrayOf(20, 16, 14))
        }
    }

    /**
     * 防止重复警告的日志记录方法
     */
    private fun logWarning(angle: Int, message: String) {
        val currentTime = System.currentTimeMillis()
        val lastWarningTime = recentWarnings[message] ?: 0L

        // 只有当距离上次警告超过冷却时间，才发出新警告
        if (currentTime - lastWarningTime > WARNING_COOLDOWN) {
            Log.i("test_tips", "$angle---$message")
            recentWarnings[message] = currentTime
        }
    }

    /**
     * 两手托天理三焦 检测器
     */
    class Pose1 {
        companion object {
            // 用于判断是否进入 up 状态
            var maxShoulderCombineAngle = 0
            var maxArmCombineAngle = 0
            var minAngle = 999
            private var maxUpFrame: Bitmap? = null
            private var minDownFrame: Bitmap? = null

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
                // 每次调用前清空警告部位
                warningParts.clear()

                with(detector) {
                    val angle1 = getAngle(15, 0, 16) // 左手，鼻子，右手
                    val angle3 = getAngle(15, 11, 23) // 左手腕，左肩，左胯
                    val angle4 = getAngle(16, 12, 24) // 右手腕，右肩，右胯

                    // 从detector中获取左右手臂角度
                    val leftArm = leftArmAngle
                    val rightArm = rightArmAngle

                    if (poseStage.contains("up")) {
                        // 如果这是 up 阶段的第一帧，重置 maxShoulderCombineAngle
                        if (maxShoulderCombineAngle == 0) {
                            maxShoulderCombineAngle = angle3 + angle4
                        } else if (maxShoulderCombineAngle < angle3 + angle4) {
                            maxShoulderCombineAngle = angle3 + angle4
                            maxArmCombineAngle = leftArm + rightArm
                            if (currentFrame != null) {
                                if (maxShoulderCombineAngle > 0 && maxArmCombineAngle !in 165 * 2..180 * 2) {
                                    addWarnPart(2)
                                    addWarnPart(5)
                                    isUpWarn = true
                                    // 使用带防抖的日志方法
                                    logWarning(maxShoulderCombineAngle, "手臂伸直，不要弯腰驼背")
                                }
                                maxUpFrame = if (pose != null) {
                                    drawPoseOnBitmap(false, currentFrame, pose)
                                } else {
                                    currentFrame.copy(currentFrame.config!!, true)
                                }
                            }
                        }
                        // Don't save frame every time, only when needed
                        if (minDownFrame != null) {
                            saveFrame(false, minDownFrame, "minDownFrame_$minDownFrame")
                            minDownFrame = null
                        }
                    } else if (poseStage.contains("down")) {
                        if (angle1 < minAngle) {
                            minAngle = angle1
                            if (currentFrame != null) {
                                minDownFrame = if (pose != null) {
                                    drawPoseOnBitmap(true, currentFrame, pose)
                                } else {
                                    currentFrame.copy(currentFrame.config!!, true)
                                }
                            }
                        }
                        // Don't save frame every time, only when needed
                        if (maxUpFrame != null) {
                            saveFrame(true, maxUpFrame, "maxUpFrame_$maxUpFrame")
                            maxUpFrame = null
                        }
                    }
                    // 筛选出固定部位角度
                    monitoringList.filter { it.poseType == 0 }.forEach {
                        val fixAngle = if (it.poseIndex.size == 3) {
                            getAnyAngleBy3Point(it.poseIndex)
                        } else {
                            getAnyAngleBy4Point(it.poseIndex)
                        }
                        if (fixAngle !in it.firstStandardRange[0]..it.firstStandardRange[1]) {
                            // 如果检测到问题角度，也可以添加到警告集合
                            warningParts.add(it.poseIndex)
                            // 使用带防抖的日志方法
                            logWarning(fixAngle, it.tips)
                        }
                    }
                }
                return ""
            }

            /**
             * 在Bitmap上绘制人体姿态关键点
             */
            private fun drawPoseOnBitmap(isUp: Boolean, bitmap: Bitmap, pose: Pose): Bitmap {
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
                // 添加红色画笔用于标记警告部位
                val warningPaint = Paint().apply {
                    color = Color.RED
                    strokeWidth = LINE_WIDTH * 1.2f  // 使警告线条稍粗一些
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
                    val (x, y) = transformCoordinate(
                        landmark.position.x,
                        landmark.position.y,
                        bitmap.width
                    )
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

                if (isUp && isUpWarn) {
                    warningParts.forEach {
                        drawLine(
                            canvas,
                            pose.getPoseLandmark(it[0]),
                            pose.getPoseLandmark(it[1]),
                            warningPaint,
                            bitmap.width
                        )
                        drawLine(
                            canvas,
                            pose.getPoseLandmark(it[1]),
                            pose.getPoseLandmark(it[2]),
                            warningPaint,
                            bitmap.width
                        )
                    }
                }
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
            private fun transformCoordinate(
                x: Float,
                y: Float,
                imageWidth: Int
            ): Pair<Float, Float> {
                return Pair(
                    x * SCALE_FACTOR - imageWidth / X_OFFSET_FACTOR,
                    y * SCALE_FACTOR
                )
            }

            fun reset() {
                // Only release bitmap resources, don't reset angle values here
                maxUpFrame?.recycle()
                minDownFrame?.recycle()
                maxUpFrame = null
                minDownFrame = null
                warningParts.clear()
                recentWarnings.clear() // Clear the warning history
                // Don't reset maxCombineAngle and minAngle here
            }

            // Add a new method for explicitly resetting angles when needed (e.g., between exercises)
            fun resetAngles() {
                maxShoulderCombineAngle = 0
                maxArmCombineAngle = 0
                minAngle = 999
            }
        }
    }
}