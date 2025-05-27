package com.lindum.mlkit.vision.posedetector.classification

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.common.base.Preconditions
import com.google.mlkit.vision.pose.Pose
import com.lindum.mlkit.vision.CameraXLivePreviewActivity
import com.lindum.mlkit.vision.CameraXLivePreviewActivity.Companion.currentFrameBitmap
import com.lindum.mlkit.vision.CameraXLivePreviewActivity.Companion.poseEntityList
import com.lindum.mlkit.vision.posedetector.PoseAngleDetector
import com.lindum.mlkit.vision.posedetector.PosePart
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

/**
 * 接受姿态流进行计数和分类
 */
class PoseClassifierProcessor @WorkerThread constructor(
    context: Context,
    isStreamMode: Boolean
) {
    // 姿态二分类文件，用于计数
    private val poseClassifyFileName = arrayOf<String>(
        "pose/lsttlsj.csv", // 两手托天理三焦
        "pose/zykgssd.csv", // 左右开弓似射雕
        "pose/tlpwxdj.csv", // 调理脾胃需单举
        "pose/wlqswhq.csv", // 五劳七伤往后瞧
        "pose/ytbwqxh.csv", // 摇头摆尾去心火
        "pose/lspzgsy.csv", // ‌两手攀足固肾腰
        "", // ‌攒拳怒目增气力
        "", // ‌背后七颠百病消
    )

    /**
     * 区分连续分析与单帧分析：
     * true：表示连续的视频流分析模式，需要跟踪姿态变化、计数动作、平滑结果等
     * false：表示单帧分析模式，只需要对当前帧进行姿态分类，不需要维护状态或计数
     */
    private val isStreamMode: Boolean
    private var emaSmoothing: EMASmoothing? = null

    // 计数器
    private var repCounters: MutableList<RepetitionCounter>? = null

    // 基于 K 最近邻(KNN) 算法的姿态分类
    private var poseClassifier: PoseClassifier? = null
    private var lastRepResult: String? = null

    @Volatile
    private var addWarnPartFlag = true
    var context: Context

    init {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper())
        this.isStreamMode = isStreamMode
        if (isStreamMode) {
            emaSmoothing = EMASmoothing()
            repCounters = ArrayList<RepetitionCounter>()
            lastRepResult = ""
        }
        this.context = context
        loadPoseSamples(context)
    }

    private fun loadPoseSamples(context: Context) {
        val poseSamples: MutableList<PoseSample?> = ArrayList<PoseSample?>()
        try {
            POSE_SAMPLES_FILE =
                poseClassifyFileName.getOrNull(CameraXLivePreviewActivity.nowPoseIndex)
                    ?: "pose/lsttlsj.csv"
            val reader = BufferedReader(
                InputStreamReader(context.assets.open(POSE_SAMPLES_FILE))
            )
            var csvLine = reader.readLine()
            while (csvLine != null) {
                // 如果行不是有效的{@link PoseSample}，获得空值并跳过添加到列表中
                val poseSample = PoseSample.getPoseSample(csvLine, ",")
                if (poseSample != null) {
                    poseSamples.add(poseSample)
                }
                csvLine = reader.readLine()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error when loading pose samples.\n$e")
        }
        poseClassifier = PoseClassifier(poseSamples)
        if (isStreamMode) {
            for (className in POSE_CLASSES) {
                repCounters!!.add(RepetitionCounter(className))
            }
        }
    }

    /**
     * 给定一个新的[Pose]输入，返回带有姿势的格式化[String]列表
     * 分类结果。
     *
     *
     * 目前它最多返回 2 个字符串，如下所示：
     * 0：姿势类：X代表
     * 1：PoseClass：[0.0-1.0] 置信度
     */
    @WorkerThread
    fun getPoseResult(pose: Pose): MutableList<String?> {
        // 如果是下一个新的二分类文件，则加载新的
        if (!TextUtils.equals(
                POSE_SAMPLES_FILE,
                poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex]
            ) && poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex] != ""
        ) {
            loadPoseSamples(context)
        }

        // 清空纠错建议
        poseTips = ""

        // 确保当前代码不在Android的主线程（UI线程）上执行，否则抛出异常
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper())
        // 屏幕左下角显示的检测结果信息
        val result: MutableList<String?> = ArrayList<String?>()
        var classification = poseClassifier!!.classify(pose)

        // Update {@link RepetitionCounter}s if {@code isStreamMode}.
        if (isStreamMode) {
            // 即使没有检测到人体关键点，也要平滑姿势。
            classification = emaSmoothing?.getSmoothedResult(classification)

            // 如果没有检测到姿态，则提前返回结果，不更新重复计数器
            if (pose.allPoseLandmarks.isEmpty()) {
                result.add(lastRepResult)
                return result
            }
            repCounters?.let {
                for (repCounter in repCounters) {
                    val repsBefore = repCounter.numRepeats
                    val repsAfter = repCounter.addClassificationResult(classification)

                    if (repsAfter > repsBefore) {
                        // 计数器更新时播放蜂鸣声
                        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                        val currentPose =
                            poseEntityList?.getOrNull(CameraXLivePreviewActivity.nowPoseIndex)
                        var activity: CameraXLivePreviewActivity? = null
                        if (context is CameraXLivePreviewActivity) {
                            activity = context as CameraXLivePreviewActivity
                        }
                        currentPose?.let { pose ->
                            // 计数动作
                            if (pose.poseType == 1) {
                                activity?.updatePoseCount(repsAfter)
                                if (repsAfter == pose.poseProcessValue) {
                                    activity?.clickNextPoseButton()
                                }
                            } else {
                                activity?.clickNextPoseButton()
                            }
                        }

                        break
                    }
                }
            }
            result.add(lastRepResult)
        }

        // 确保 pose 对象不为null，如果是 null 则抛出异常
        checkNotNull(pose)

        // 如果检测到人体关键点，将当前帧的 maxConfidence 类添加到结果中
        if (!pose.allPoseLandmarks.isEmpty()) {
            maxConfidenceClass = classification.maxConfidenceClass
            val maxConfidenceClassResult = String.format(
                Locale.US,
                "%s : %.2f confidence",
                maxConfidenceClass,
                classification.getClassConfidence(maxConfidenceClass)
                        / poseClassifier!!.confidenceRange()
            )
            classifierConfidence =
                classification.getClassConfidence(maxConfidenceClass) / poseClassifier!!.confidenceRange()

            if (poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex] != "") {
                result.add(maxConfidenceClassResult)
            }
        }

        // 人体关键的监测
        val poseAngleDetector = PoseAngleDetector(pose)

        // 倒计时结束，开始检测
        if (CameraXLivePreviewActivity.countdownSeconds == -1) {
            // 获取监测的动作实体
            val poseEntity = poseEntityList!!.getOrNull(CameraXLivePreviewActivity.nowPoseIndex)
            // 获取监测部位列表
            val monitoringList = poseEntity!!.monitoringPosePartList

            newLogic(maxConfidenceClass ?: "", result, monitoringList, poseAngleDetector)
        }
        // 把结果顶上去
        result.add(" ")
        result.add(" ")
        result.add(" ")
        result.add(" ")
        result.add(" ")
        result.add(" ")
        return result
    }

    /**
     * 新增逻辑
     */
    private fun newLogic(
        maxConfidenceClass: String,
        result: MutableList<String?>,
        parts: MutableList<PosePart>,
        detector: PoseAngleDetector
    ) {
        if (currentFrameBitmap == null) return
        
        // 获取BaduanjinPoseEvaluator实例
        val evaluator = BaduanjinPoseEvaluator.getInstance()
        
        when (CameraXLivePreviewActivity.nowPoseIndex) {
            0 -> evaluator.getPose1Score(
                maxConfidenceClass,
                result,
                parts,
                detector,
                currentFrameBitmap!!,
                detector.detectPose
            )

            1 -> evaluator.getPose2Score(
                maxConfidenceClass,
                result,
                parts,
                detector,
                currentFrameBitmap!!,
                detector.detectPose
            )
        }
    }

    /**
     * 获取部位名称
     *
     * @param index 身体部位索引
     * @return 身体部位名称
     */
    fun getPartName(index: Int): String {
        val partNames = arrayOf<String>(
            "",
            "右手腕",
            "右胳膊肘",
            "右手臂与肩膀",
            "左手臂与肩膀",
            "左胳膊肘",
            "左手腕",
            "右腿与身体",
            "左腿与身体",
            "右膝盖",
            "左膝盖",
            "右脚腕",
            "左脚腕"
        )
        return partNames[index]
    }

    companion object {
        private const val TAG = "PoseClassifierProcessor"

        private var POSE_SAMPLES_FILE = ""

        @Volatile
        var maxConfidenceClass: String? = null

        // 置信度
        @Volatile
        var classifierConfidence: Float = 0f

        // zh-todo 剩余动作适配
        private val POSE_CLASSES = arrayOf<String?>(
            "lsttlsj_down",
            "zykgssd_up",
            "tlpwxdj_up",
            "wlqswhq_up",
            "ytbwqxh_up",
        )

        @Volatile
        var poseTips: String? = null
    }
}
