package com.lindum.mlkit.vision.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.lindum.mlkit.vision.CameraXLivePreviewActivity
import com.lindum.mlkit.vision.MainActivity.Companion.poseRightImageArrayList
import com.lindum.mlkit.vision.entity.WrongPoseModel
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
class BaduanjinPoseEvaluator {
    // 防止多次重复触发保存
    private var countFrame = 0

    // 调整系数 - 根据图像测试调整以获得最佳匹配效果
    private val SCALE_FACTOR = 1.5f       // 缩放因子
    private val X_OFFSET_FACTOR = 3f    // X轴偏移因子
    private val DOT_RADIUS = 8f           // 关键点半径
    private val LINE_WIDTH = 10f          // 线条宽度

    // 最大错误姿势记录数
    private val MAX_WARNING_POSES = 20

    // 将 allWarningPoseList 改为可访问的，并添加获取方法
    private val allWarningPoseList = mutableListOf<WrongPoseModel>()

    // Pose1 内的变量转为实例变量
    var maxShoulderCombineAngle = 0
    var maxUpFrame: Bitmap? = null
    var maxUpWrongModel: WrongPoseModel? = null
    var minAngle = 999
    var minDownFrame: Bitmap? = null
    var minArmCombineAngle = Pair(999, 999)
    var minDownWrongModel: WrongPoseModel? = null

    // Pose2 内的变量转为实例变量
    var pose2MaxShoulderCombineAngle = 0
    var rightMaxFrame: Bitmap? = null
    var rightMaxWrongModel: WrongPoseModel? = null
    var rightMaxAngle = 0
    var leftMaxFrame: Bitmap? = null
    var leftMaxWrongModel: WrongPoseModel? = null
    var leftMaxAngle = 0

    companion object {
        // 单例实例
        private var instance: BaduanjinPoseEvaluator? = null

        // 这些常量可以保留为静态
        private const val FRAME_COOLDOWN = 5

        // 获取单例实例
        @JvmStatic
        fun getInstance(): BaduanjinPoseEvaluator {
            if (instance == null) {
                instance = BaduanjinPoseEvaluator()
            }
            return instance!!
        }

        // 重置单例实例
        @JvmStatic
        fun resetInstance() {
            instance = BaduanjinPoseEvaluator()
        }
    }

    // 获取所有错误姿势的列表
    fun getAllWarningPoseList(): List<WrongPoseModel> {
        return allWarningPoseList.toList()
    }

    // 清空错误姿势列表和所有状态变量
    fun clearAllWarningPoseList() {
        allWarningPoseList.clear()
        leftMaxFrame = null
        rightMaxFrame = null
        leftMaxAngle = 0
        rightMaxAngle = 0
        maxShoulderCombineAngle = 0
        maxUpFrame = null
        maxUpWrongModel = null
        minAngle = 999
        minDownFrame = null
        minDownWrongModel = null
        countFrame = 0
    }

    /**
     * 在Bitmap上绘制人体姿态关键点
     */
    fun drawPoseOnBitmap(
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

    // 添加错误姿势模型，并限制数量
    private fun addWrongPoseToList(wrongPoseModel: WrongPoseModel) {
        if (wrongPoseModel.wrongPart == "左腿") {
            Log.i("test_part", "left")
        }
        if (wrongPoseModel.wrongPart == "右腿") {
            Log.i("test_part", "right")
        }
        // 检查是否已存在相同的提示信息，避免重复添加
        val existingModel = allWarningPoseList.find { model ->
            model.wrongPart == wrongPoseModel.wrongPart &&
                    model.poseName == wrongPoseModel.poseName
        }

        if (existingModel != null || CameraXLivePreviewActivity.countdownSeconds >= 0 || CameraXLivePreviewActivity.nowPoseIndex != wrongPoseModel.poseIndex) {
            return
        }

        // 添加到列表开头
        allWarningPoseList.add(0, wrongPoseModel)

        // 如果超过最大数量，则移除最早添加的
        if (allWarningPoseList.size > MAX_WARNING_POSES) {
            allWarningPoseList.removeAt(allWarningPoseList.size - 1)
            Log.i("WrongPose", "Removed oldest warning due to max limit")
        }

        Log.i(
            "WrongPose",
            "Added pose error: ${wrongPoseModel.poseTips}, total: ${allWarningPoseList.size}"
        )
    }

    fun saveFrame(
        isUp: Boolean,
        bitmap: Bitmap?, filename: String
    ) {
        // 增加计数
        countFrame++

        // 如果计数小于冷却时间，直接返回不处理
        if (countFrame < FRAME_COOLDOWN) {
            return
        }

        // 重置计数
        countFrame = 0

        bitmap?.let {
            try {
                if (maxUpWrongModel != null) {
                    maxUpWrongModel!!.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                    maxUpWrongModel!!.rightPoseBitmap =
                        poseRightImageArrayList[maxUpWrongModel!!.poseIndex!!]
                    addWrongPoseToList(maxUpWrongModel!!)
                }
                if (minDownWrongModel != null) {
                    minDownWrongModel!!.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                    minDownWrongModel!!.rightPoseBitmap =
                        poseRightImageArrayList[minDownWrongModel!!.poseIndex!!]
                    addWrongPoseToList(minDownWrongModel!!)
                }
                if (leftMaxWrongModel != null) {
                    leftMaxWrongModel!!.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                    leftMaxWrongModel!!.rightPoseBitmap =
                        poseRightImageArrayList[leftMaxWrongModel!!.poseIndex!!]
                    addWrongPoseToList(leftMaxWrongModel!!)
                }
                if (rightMaxWrongModel != null) {
                    rightMaxWrongModel!!.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                    rightMaxWrongModel!!.rightPoseBitmap =
                        poseRightImageArrayList[rightMaxWrongModel!!.poseIndex!!]
                    addWrongPoseToList(rightMaxWrongModel!!)
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
                    leftMaxWrongModel = null
                    rightMaxWrongModel = null

                    leftMaxAngle = 0
                    rightMaxAngle = 0
                    leftMaxFrame = null
                    rightMaxFrame = null
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

    fun saveFixedFrame(
        wrongPoseModel: WrongPoseModel,
        bitmap: Bitmap?, filename: String
    ) {
        // 不再在这里增加计数和检查冷却时间
        // 而是直接处理错误姿势

        bitmap?.let {
            try {
                wrongPoseModel.wrongPoseImage = bitmap.copy(bitmap.config!!, true)
                wrongPoseModel.rightPoseBitmap =
                    poseRightImageArrayList[wrongPoseModel.poseIndex!!]
                addWrongPoseToList(wrongPoseModel)

                // 只有当需要保存图片到文件系统时才增加计数
                countFrame++
                if (countFrame >= FRAME_COOLDOWN) {
                    // 重置计数
                    countFrame = 0

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
                }
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
    fun addWarnPart(partIndex: Int): MutableList<IntArray> {
        val tempLeftWrongParts = mutableListOf<IntArray>()
        if (partIndex == 12) {
            tempLeftWrongParts.add(intArrayOf(25, 27, 31))
        } else if (partIndex == 11) {
            tempLeftWrongParts.add(intArrayOf(26, 28, 32))
        } else if (partIndex == 10) { // 左膝
            tempLeftWrongParts.add(intArrayOf(23, 25, 27))
        } else if (partIndex == 9) { // 右膝
            tempLeftWrongParts.add(intArrayOf(24, 26, 28))
        } else if (partIndex == 8) {
            tempLeftWrongParts.add(intArrayOf(11, 23, 25))
        } else if (partIndex == 7) {
            tempLeftWrongParts.add(intArrayOf(12, 24, 26))
        } else if (partIndex == 6) {
            tempLeftWrongParts.add(intArrayOf(13, 15, 19))
        } else if (partIndex == 5) { // 左手臂
            tempLeftWrongParts.add(intArrayOf(11, 13, 15))
        } else if (partIndex == 4) {
            tempLeftWrongParts.add(intArrayOf(13, 11, 23))
        } else if (partIndex == 3) {
            tempLeftWrongParts.add(intArrayOf(14, 12, 24))
        } else if (partIndex == 2) { // 右手臂
            tempLeftWrongParts.add(intArrayOf(12, 14, 16))
        } else if (partIndex == 1) {
            tempLeftWrongParts.add(intArrayOf(20, 16, 14))
        }
        return tempLeftWrongParts
    }

    /**
     * 检查固定部位角度
     */
    private fun checkFixedPartAngles(
        monitoringList: List<PosePart>,
        detector: PoseAngleDetector,
        currentFrame: Bitmap,
        pose: Pose,
        poseIndex: Int,
        poseName: String
    ) {
        // 用于收集当前帧中的所有错误
        val frameErrors = mutableListOf<Pair<WrongPoseModel, MutableList<IntArray>>>()

        // 筛选出固定部位角度
        monitoringList.filter { it.poseType == 0 }.forEach {
            val fixAngle = if (it.poseIndex.size == 3) {
                detector.getAnyAngleBy3Point(it.poseIndex)
            } else {
                detector.getAnyAngleBy4Point(it.poseIndex)
            }
            if (fixAngle !in it.firstStandardRange[0]..it.firstStandardRange[1]) {
                // 为每个错误创建新的WrongPoseModel实例
                val wrongModel = WrongPoseModel()
                wrongModel.poseIndex = poseIndex
                wrongModel.poseName = poseName
                wrongModel.poseTips = it.tips
                wrongModel.wrongPart = it.partName
                wrongModel.poseDetail = "${it.partName}角度：$fixAngle°"

                val tempWrongParts = mutableListOf<IntArray>()
                if (it.poseIndex.size == 3) {
                    tempWrongParts.add(0, it.poseIndex)
                }

                // 将错误和对应的错误部位添加到集合中
                frameErrors.add(Pair(wrongModel, tempWrongParts))
            }
        }

        // 处理所有收集到的错误
        if (frameErrors.isNotEmpty()) {
            // 复制当前帧，避免重复创建位图
            val frameCopy = currentFrame.copy(currentFrame.config!!, true)

            // 处理每个错误
            frameErrors.forEach { (wrongModel, wrongParts) ->
                val paintResult = drawPoseOnBitmap(wrongParts, frameCopy, pose)
                saveFixedFrame(wrongModel, paintResult, "fixedFrame_${wrongModel.wrongPart}")
            }
        }
    }

    /**
     * 两手托天理三焦 检测器
     * 将内部类重构为实例方法
     */
    fun getPose1Score(
        poseStage: String,
        result: MutableList<String?>? = null,
        monitoringList: List<PosePart>,
        detector: PoseAngleDetector,
        currentFrame: Bitmap,
        pose: Pose
    ) {
        countFrame += 1

        with(detector) {
            val angle3 = getAngle(13, 11, 23) // 左手腕，左肩，左胯
            val angle4 = getAngle(14, 12, 24) // 右手腕，右肩，右胯

            if (poseStage.contains("up")) {
                // 找到最顶部的一帧
                if (maxShoulderCombineAngle < angle3 + angle4) {
                    maxShoulderCombineAngle = angle3 + angle4
                    leftMaxAngle = leftArmAngle
                    rightMaxAngle = rightArmAngle

                    // 创建左手臂错误模型
                    val leftWrongModel = WrongPoseModel()
                    leftWrongModel.poseIndex = 0
                    leftWrongModel.poseName = "两手托天理三焦"
                    leftWrongModel.poseTips = "左手臂伸直，不要弯腰驼背"
                    leftWrongModel.wrongPart = "左手臂"
                    leftWrongModel.poseDetail = "左手臂角度：$leftArmAngle"

                    // 绘制人体关键点和错误区域，记录 up 最顶部关键帧
                    leftMaxFrame = drawPoseOnBitmap(addWarnPart(5), currentFrame, pose)
                    leftMaxWrongModel = leftWrongModel

                    // 创建右手臂错误模型
                    val rightWrongModel = WrongPoseModel()
                    rightWrongModel.poseIndex = 0
                    rightWrongModel.poseName = "两手托天理三焦"
                    rightWrongModel.poseTips = "右手臂伸直，不要弯腰驼背"
                    rightWrongModel.wrongPart = "右手臂"
                    rightWrongModel.poseDetail = "右手臂角度：$rightArmAngle°"

                    rightMaxFrame = drawPoseOnBitmap(addWarnPart(2), currentFrame, pose)
                    rightMaxWrongModel = rightWrongModel
                }
            } else if (poseStage.contains("down")) {
                if (leftMaxFrame != null && leftMaxAngle !in 160..180) {
                    saveFrame(true, leftMaxFrame, "leftMaxFrame_$leftMaxFrame")
                }
                if (rightMaxFrame != null && rightMaxAngle !in 160..180) {
                    saveFrame(true, rightMaxFrame, "rightMaxFrame_$rightMaxFrame")
                }
            }

            // 调用共用方法检查固定部位角度
            checkFixedPartAngles(monitoringList, detector, currentFrame, pose, 0, "两手托天理三焦")
        }
    }

    /**
     * 左右开弓似射雕 检测器
     * 将内部类重构为实例方法
     */
    fun getPose2Score(
        poseStage: String,
        result: MutableList<String?>? = null,
        monitoringList: List<PosePart>,
        detector: PoseAngleDetector,
        currentFrame: Bitmap,
        pose: Pose
    ) {
        countFrame += 1

        with(detector) {
            val leftShoulderAngle = getAngle(13, 11, 23)
            val rightShoulderAngle = getAngle(14, 12, 24)

            if (poseStage.contains("up")) {
                if (leftArmAngle < 90 && rightArmAngle > 90 && leftShoulderAngle > 50 && rightShoulderAngle > 50) {
                    result?.add("朝右")

                    // 找到朝右且右手臂角度最大（伸直）的一帧
                    if (rightMaxAngle < rightArmAngle) {
                        rightMaxAngle = rightArmAngle
                        result?.add("最大值:$rightMaxAngle")

                        // 创建右手臂错误模型
                        val rightWrongModel = WrongPoseModel()
                        rightWrongModel.poseIndex = 1
                        rightWrongModel.poseName = "左右开弓似射雕"
                        rightWrongModel.poseTips = "开弓时需手臂伸直"
                        rightWrongModel.wrongPart = "右手臂"
                        rightWrongModel.angle = Pair(leftArmAngle, rightArmAngle)

                        rightMaxFrame = drawPoseOnBitmap(addWarnPart(2), currentFrame, pose)
                        rightMaxWrongModel = rightWrongModel
                    }
                }
                if (rightArmAngle < 90 && leftArmAngle > 90 && leftShoulderAngle > 50 && rightShoulderAngle > 50) {
                    result?.add("朝左")

                    // 找到朝左且左手臂角度最大（伸直）的一帧
                    if (leftMaxAngle < leftArmAngle) {
                        leftMaxAngle = leftArmAngle

                        // 创建左手臂错误模型
                        val leftWrongModel = WrongPoseModel()
                        leftWrongModel.poseIndex = 1
                        leftWrongModel.poseName = "左右开弓似射雕"
                        leftWrongModel.poseTips = "开弓时需手臂伸直"
                        leftWrongModel.wrongPart = "左手臂"
                        leftWrongModel.angle = Pair(leftArmAngle, rightArmAngle)

                        leftMaxFrame = drawPoseOnBitmap(addWarnPart(5), currentFrame, pose)
                        leftMaxWrongModel = leftWrongModel
                    }
                }

            } else if (poseStage.contains("down")) {
                if (leftMaxFrame != null && leftMaxAngle !in 165..180) {
                    saveFrame(true, leftMaxFrame, "leftMaxFrame_$leftMaxFrame")
                }
                if (rightMaxFrame != null && rightMaxAngle !in 165..180) {
                    saveFrame(true, rightMaxFrame, "rightMaxFrame_$rightMaxFrame")
                }
            }

            // 调用共用方法检查固定部位角度
            checkFixedPartAngles(monitoringList, detector, currentFrame, pose, 1, "左右开弓似射雕")
        }
    }
}