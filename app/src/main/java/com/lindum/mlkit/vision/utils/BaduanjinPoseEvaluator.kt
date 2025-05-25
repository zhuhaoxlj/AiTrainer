package com.lindum.mlkit.vision.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.lindum.mlkit.vision.MainActivity.Companion.poseRightImageArrayList
import com.lindum.mlkit.vision.entity.WrongPoseModel
import com.lindum.mlkit.vision.posedetector.PoseAngleDetector
import com.lindum.mlkit.vision.posedetector.PosePart
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.maxShoulderCombineAngle
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.maxUpFrame
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.maxUpWrongModel
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.minAngle
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.minDownFrame
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator.Pose1.Companion.minDownWrongModel
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
    // 防止多次重复触发保存
    private const val FRAME_COOLDOWN = 5
    private var countFrame = 0
    private val allWarningPoseList = mutableListOf<WrongPoseModel>()

    private fun saveFrame(
        isUp: Boolean,
        bitmap: Bitmap?, filename: String
    ) {
        if (countFrame < FRAME_COOLDOWN) {
        }

        bitmap?.let {
            try {
                if (maxUpWrongModel != null) {
                    maxUpWrongModel!!.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                    maxUpWrongModel!!.rightPoseBitmap =
                        poseRightImageArrayList[maxUpWrongModel!!.poseIndex!!]
                    allWarningPoseList.add(0, maxUpWrongModel!!)
                }
                if (minDownWrongModel != null) {
                    minDownWrongModel!!.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                    minDownWrongModel!!.rightPoseBitmap =
                        poseRightImageArrayList[minDownWrongModel!!.poseIndex!!]
                    allWarningPoseList.add(0, minDownWrongModel!!)
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
                it.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                outputStream.flush()
                outputStream.close()
                if (isUp) {
                    maxShoulderCombineAngle = 0
                    maxUpFrame?.recycle()
                    maxUpFrame = null
                    maxUpWrongModel = null
                } else {
                    minAngle = 999
                    minDownFrame?.recycle()
                    minDownFrame = null
                    minDownWrongModel = null
                }
                Log.d("PoseCapture", "Saved max angle frame to: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("PoseCapture", "Error saving max angle frame", e)
            }
        }
    }

    private fun saveFixedFrame(
        wrongPoseModel: WrongPoseModel,
        bitmap: Bitmap?, filename: String
    ) {
        if (countFrame < FRAME_COOLDOWN) {
        }

        bitmap?.let {
            try {
                wrongPoseModel.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                wrongPoseModel.rightPoseBitmap =
                    poseRightImageArrayList[maxUpWrongModel!!.poseIndex!!]
                allWarningPoseList.add(0, wrongPoseModel)
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
    fun addWarnPart(warningParts: MutableList<IntArray>, partIndex: Int): MutableList<IntArray> {
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
        return warningParts
    }

    /**
     * 两手托天理三焦 检测器
     */
    class Pose1 {
        companion object {
            // 用于判断是否进入 up 状态，当这个值最大时就说明运动到最顶部了
            var maxShoulderCombineAngle = 0

            // 最顶部的两个手臂的角度 Pair(left,right)
            var maxArmCombineAngle = Pair(0, 0)

            // 最顶部的一帧图片
            internal var maxUpFrame: Bitmap? = null

            // 最顶部的一帧错误 model
            internal var maxUpWrongModel: WrongPoseModel? = null

            var minAngle = 999
            internal var minDownFrame: Bitmap? = null

            // 最底部的两个手臂的角度 Pair(left,right)
            var minArmCombineAngle = Pair(999, 999)

            // 最底部部的一帧错误 model
            internal var minDownWrongModel: WrongPoseModel? = null


            // 调整系数 - 根据图像测试调整以获得最佳匹配效果
            private const val SCALE_FACTOR = 1.5f       // 缩放因子
            private const val X_OFFSET_FACTOR = 3f    // X轴偏移因子
            private const val DOT_RADIUS = 8f           // 关键点半径
            private const val LINE_WIDTH = 10f          // 线条宽度


            fun getScore(
                poseStage: String,
                monitoringList: List<PosePart>,
                detector: PoseAngleDetector,
                currentFrame: Bitmap,
                pose: Pose
            ): String {
                countFrame += 1

                with(detector) {
                    val angle3 = getAngle(15, 11, 23) // 左手腕，左肩，左胯
                    val angle4 = getAngle(16, 12, 24) // 右手腕，右肩，右胯
                    val tempWrongModel = WrongPoseModel()
                    tempWrongModel.poseIndex = 0
                    tempWrongModel.poseName = "两手托天理三焦"
                    // 从detector中获取左右手臂角度
                    val leftArm = leftArmAngle
                    val rightArm = rightArmAngle

                    if (poseStage.contains("up")) {
                        // 有最大值就更新，同时记录这一帧图片、错误地方、错误建议
                        if (maxShoulderCombineAngle < angle3 + angle4) {
                            maxShoulderCombineAngle = angle3 + angle4
                            maxArmCombineAngle = Pair(leftArm, rightArm)

                            val tempWrongParts = mutableListOf<IntArray>()
                            // 手臂角度不对
                            if (maxArmCombineAngle.first !in 165..180) {
                                addWarnPart(tempWrongParts, 5)
                                tempWrongModel.poseTips = "手臂伸直，不要弯腰驼背"
                            }
                            if (maxArmCombineAngle.second !in 165..180) {
                                addWarnPart(tempWrongParts, 2)
                                tempWrongModel.poseTips = "手臂伸直，不要弯腰驼背"
                            }

                            // 绘制人体关键点和错误区域，记录 up 最顶部关键帧
                            maxUpFrame = drawPoseOnBitmap(tempWrongParts, currentFrame, pose)
                            maxUpWrongModel = tempWrongModel
                        }
                        if (minDownFrame != null) {
                            saveFrame(false, minDownFrame, "minDownFrame_$minDownFrame")
                        }
                    } else if (poseStage.contains("down")) {
                        // 找到最下面的一帧
                        if (angle3 + angle4 < minAngle) {
                            minAngle = angle3 + angle4
                            minArmCombineAngle = Pair(leftArm, rightArm)

                            val tempWrongParts = mutableListOf<IntArray>()
                            // 手臂角度不对
                            if (minArmCombineAngle.first !in 130..145) {
                                addWarnPart(tempWrongParts, 5)
                                tempWrongModel.poseTips = "双手掌心向上交叉于下腹部"
                            }
                            if (minArmCombineAngle.second !in 130..145) {
                                addWarnPart(tempWrongParts, 2)
                                tempWrongModel.poseTips = "双手掌心向上交叉于下腹部"
                            }

                            // 绘制人体关键点和错误区域，记录 up 最顶部关键帧
                            minDownFrame = drawPoseOnBitmap(tempWrongParts, currentFrame, pose)
                            minDownWrongModel = tempWrongModel
                        }
                        if (maxUpFrame != null) {
                            saveFrame(true, maxUpFrame, "maxUpFrame_$maxUpFrame")
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
                            val tempWrongParts = mutableListOf<IntArray>()
                            if (it.poseIndex.size == 3) {
                                tempWrongParts.add(0, it.poseIndex)
                            }
                            tempWrongModel.poseTips = it.tips
                            val paintResult = drawPoseOnBitmap(tempWrongParts, currentFrame, pose)
                            saveFixedFrame(tempWrongModel, paintResult, "fixedFrame_$paintResult")

                            // 使用带防抖的日志方法
//                            logWarning(fixAngle, it.tips)
                        }
                    }
                }
                return ""
            }

            /**
             * 在Bitmap上绘制人体姿态关键点
             */
            private fun drawPoseOnBitmap(
                warningParts: MutableList<IntArray>,
                bitmap: Bitmap,
                pose: Pose
            ): Bitmap {
                // 创建一个副本，避免修改原始bitmap
                val bitmapWithWrongParts = bitmap.copy(bitmap.config!!, true)
                val canvas = Canvas(bitmapWithWrongParts)

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
                if (landmarks.isEmpty()) return bitmapWithWrongParts

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
                return bitmapWithWrongParts
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


            // Add a new method for explicitly resetting angles when needed (e.g., between exercises)
            fun resetAngles() {
                maxShoulderCombineAngle = 0
                maxArmCombineAngle = Pair(0, 0)
                minAngle = 999
            }
        }
    }
}