package com.lindum.mlkit.vision

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.MlKitException
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.ActivityVisionCameraxLivePreviewBinding
import com.lindum.mlkit.vision.entity.PoseEntity
import com.lindum.mlkit.vision.posedetector.PoseDetectorProcessor
import com.lindum.mlkit.vision.preference.PreferenceUtils
import com.lindum.mlkit.vision.preference.SettingsActivity
import com.lindum.mlkit.vision.utils.ActivityStarter
import com.lindum.mlkit.vision.utils.BaduanjinPoseEvaluator
import com.lindum.mlkit.vision.utils.FileUtils
import com.lindum.mlkit.vision.utils.StatusBarUtils
import com.lindum.mlkit.vision.view.PoseDetailsActivity
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.MessageFormat
import java.time.LocalDate
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.cardview.widget.CardView
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * 姿态识别
 *
 * @author: MarkGosling
 * @date: 2022/1/25 16:13
 */
@KeepName
class CameraXLivePreviewActivity : AppCompatActivity(), OnRequestPermissionsResultCallback,
    CompoundButton.OnCheckedChangeListener,
    QuickShot.QuickShotListener {
    // TTS 语音助手 zh-todo 说话太卡
    private var textToSpeech: TextToSpeech? = null

    private var cameraProvider: ProcessCameraProvider? = null

    private var previewUseCase: Preview? = null

    private var analysisUseCase: ImageAnalysis? = null

    private var imageProcessor: VisionImageProcessor? = null

    private var needUpdateGraphicOverlayImageSourceInfo = false

    // 选择模型 姿态检测
    private var selectedModel: String = POSE_DETECTION

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var cameraSelector: CameraSelector? = null

    // 定时器
    var mHandler2: Handler? = null
    var r2: Runnable? = null

    // 状态更新定时器
    private var statusUpdateHandler: Handler? = null
    private var statusUpdateRunnable: Runnable? = null

    // 现在动作进行的秒数
    private var nowPoseProcessSeconds = 0

    // 训练总时间
    private var totalTrainingTime = 0

    // 完成动作组数
    private var countGroupsOfPose = 0

    // 是否提醒过呼吸方法
    private var isRemindedBreathingMethods = false

    private lateinit var binding: ActivityVisionCameraxLivePreviewBinding

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        // 设置布局
        binding = ActivityVisionCameraxLivePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 初始化
        // 从主页获取数据库中的动作数据
        poseEntityList = MainActivity.poseEntityArrayList
        init()
        // 重置姿势评估器
        BaduanjinPoseEvaluator.resetInstance()

        // 定时更新姿势状态
        updateVariable()
        updatePoseStatus()

        nowPoseName = poseEntityList!![nowPoseIndex].getPoseName()

        // 跳转至结果界面
        binding.statusCapsule.setOnClickListener { view: View? ->
            val poseEntity: PoseEntity = poseEntityList!![nowPoseIndex]
            countMinutes = nowPoseProcessSeconds / 60f
            if (poseEntity.getPoseType() == 1) {
                poseEntity.setCountTimes(countTimes)
            } else {
                poseEntity.setCountMinutes(countMinutes)
            }
            countMinutes = 0f
            countTimes = 0

            // 使用 ActivityStarter 启动训练结果页面，传递错误姿势列表
            val wrongPoseList = BaduanjinPoseEvaluator.getInstance().getAllWarningPoseList()
            ActivityStarter.startTrainingResultActivity(
                this@CameraXLivePreviewActivity,
                wrongPoseList,
                totalTrainingTime,
                countGroupsOfPose
            )

            // 停止训练
            isEnd = true
        }

        val options: MutableList<String?> = ArrayList<String?>()
        options.add(POSE_DETECTION)
        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter<String?>(this, R.layout.spinner_style, options)
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // attaching data adapter to spinner

        binding.facingSwitch.setOnCheckedChangeListener(this)

        ViewModelProvider(this, AndroidViewModelFactory.getInstance(application))
            .get<CameraXViewModel>(CameraXViewModel::class.java)
            .processCameraProvider!!
            .observe(
                this,
                Observer { provider: ProcessCameraProvider? ->
                    cameraProvider = provider
                    if (allPermissionsGranted()) {
                        bindAllCameraUseCases()
                    }
                })
        binding.settingsButton.setOnClickListener(
            View.OnClickListener { v: View? ->
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                intent.putExtra(
                    SettingsActivity.EXTRA_LAUNCH_SOURCE,
                    SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW
                )
                startActivity(intent)
            })

        binding.nextPoseImage.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex + 1 < (poseEntityList?.size ?: 0)) {
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
                nowPoseIndex++
                textToSpeech!!.speak(
                    "下一个动作" + poseEntityList!![nowPoseIndex].getPoseName(),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                isRemindedBreathingMethods = false
            } else {
                r2?.let {
                    mHandler2?.removeCallbacks(it)
                }
                binding.statusCapsule.callOnClick()
            }
        })
        binding.prePoseImage.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex - 1 >= 0) {
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
                nowPoseIndex--
            }
        })
        binding.nextButton.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex + 1 < poseEntityList!!.size) {
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
                nowPoseIndex++
                textToSpeech!!.speak(
                    "下一个动作" + poseEntityList!![nowPoseIndex].getPoseName(),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                isRemindedBreathingMethods = false
            }
        })
        binding.preButton.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex - 1 >= 0) {
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
                nowPoseIndex--
            }
        })
        if (!allPermissionsGranted()) {
            this.runtimePermissions
        }
    }

    fun init() {
        val layoutParams = binding.vFakeStatusBar.layoutParams
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this)
        binding.vFakeStatusBar.layoutParams = layoutParams

        // 初始化变量
        countdownSeconds = 5
        // zh-todo 记得改回来
        nowPoseIndex = 1

        val poseEntity = poseEntityList?.getOrNull(nowPoseIndex)
        if (poseEntity?.poseType == 0) {
            binding.poseProcessInfo.text = getText(R.string.one)
        } else if (poseEntity?.poseType == 1) {
            binding.poseProcessInfo.text = "0"
        }

        timeMarkPoint = System.currentTimeMillis() / 1000
        imageSavePath = MainActivity.appFilePath + "/files/analysisReport/" + LocalDate.now()
            .toString() + "/" + timeMarkPoint + "/"
        Log.i("test_path", imageSavePath.toString())
        FileUtils.createFile(imageSavePath, FileUtils.CREATE_FOLDER)

        // 初始化组件
        binding.explain.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@CameraXLivePreviewActivity, PoseDetailsActivity::class.java)
            intent.putExtra("feeling", poseEntityList!![nowPoseIndex].getPoseFeeling())
            intent.putExtra("steps", poseEntityList!![nowPoseIndex].getStepsOfPose())
            intent.putExtra(
                "commonMistakes",
                poseEntityList!![nowPoseIndex].getCommonMistakes()
            )
            intent.putExtra("details", poseEntityList!![nowPoseIndex].getDetailTips())
            intent.putExtra("breath", poseEntityList!![nowPoseIndex].getBreathTips())
            intent.putExtra("poseIndex", nowPoseIndex)
            startActivity(intent)
        })
        // TTS 初始化
        textToSpeech = TextToSpeech(this, OnInitListener { i: Int ->
            Logger.e("初始化中...")
            if (i == TextToSpeech.SUCCESS) {
                Logger.e("语音初始化成功")
                if (nowPoseIndex == 0) {
                    textToSpeech!!.setSpeechRate(0.1f)
                    textToSpeech!!.speak(
                        "第一个动作，" + poseEntityList!![nowPoseIndex].getPoseName(),
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                }
            } else {
                Logger.e("语音初始化失败")
            }
        })
    }

    fun clickNextPoseButton() {
        runOnUiThread {
            binding.nextPoseImage.callOnClick()
        }
    }

    fun updatePoseCount(poseCount: Int) {
        runOnUiThread {
            binding.poseProcessInfo.text = "$poseCount"
        }
    }

    /**
     * 定时清空警告点、倒数秒数刷新、计算总运动时长
     */
    fun updateVariable() {
        mHandler2 = Handler(Looper.getMainLooper())
        r2 = object : Runnable {
            override fun run() {
                updatePoseInfo()
                if (countdownSeconds >= 0) {
                    binding.countdownSecondsText.visibility = View.VISIBLE
                    textToSpeech!!.speak(
                        countdownSeconds.toString() + "",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                    countdownSeconds--
                }
                if (countdownSeconds == -1) {
                    binding.countdownSecondsText.visibility = View.INVISIBLE
                    // 语音助手提示
                    val voiceAssistantHandle = Handler()
                    val voiceAssistantRunnable: Runnable = object : Runnable {
                        override fun run() {
                            voiceAssistantHandle.postDelayed(this, 8000)
                            // 已经提醒过呼吸方法
                            if (!isRemindedBreathingMethods) {
                                textToSpeech!!.speak(
                                    if (poseEntityList!![nowPoseIndex]
                                            .getBreathTips() == null
                                    ) "注意均匀呼吸，不要憋气" else poseEntityList!![nowPoseIndex].getBreathTips(),
                                    TextToSpeech.QUEUE_ADD,
                                    null
                                )
                                isRemindedBreathingMethods = true
                            }
                        }
                    }
                    voiceAssistantHandle.post(voiceAssistantRunnable)
                }
                // 现在动作进行的秒数增加
                if (countdownSeconds == -1) {
                    nowPoseProcessSeconds++
                    val poseEntity = poseEntityList?.getOrNull(nowPoseIndex)
                    poseEntity?.let { pose ->
                        if (pose.poseType == 0 && nowPoseProcessSeconds > poseEntity.poseProcessValue) {
                            clickNextPoseButton()
                        }
                    }
                }
                if (!isEnd) {
                    totalTrainingTime++
                    mHandler2!!.postDelayed(this, 1000)
                }
            }
        }
        mHandler2!!.post(r2!!)
    }

    @SuppressLint("SetTextI18n")
    fun updatePoseInfo() {
        // 更新姿势名称
        nowPoseName = poseEntityList!![nowPoseIndex].getPoseName()
        binding.nowPoseIndexText.text = MessageFormat.format(
            "{0}/{1}",
            nowPoseIndex + 1,
            poseEntityList!!.size
        )
        binding.poseName.text = nowPoseName
        // 更新当前动作进度 个数
        if (poseEntityList!![nowPoseIndex].getPoseType() == 1) {
            // 更新当前动作进度 个数
        } else if (poseEntityList!![nowPoseIndex].getPoseType() == 0) {
            // 更新当前动作进度 秒数
            binding.poseProcessInfo.text = nowPoseProcessSeconds.toString() + "\""
        }
        // 更新倒数秒数
        binding.countdownSecondsText.text = countdownSeconds.toString() + ""
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (cameraProvider == null) {
            return
        }
        val newLensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK
            else
                CameraSelector.LENS_FACING_FRONT
        val newCameraSelector =
            CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                Logger.e("Set facing to $newLensFacing")
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            // Falls through
        }
        Toast.makeText(
            applicationContext,
            "This device does not have lens with facing: $newLensFacing",
            Toast.LENGTH_SHORT
        )
            .show()
    }

    public override fun onResume() {
        super.onResume()
        // 开始训练
        isEnd = false
        bindAllCameraUseCases()
        // 重新启动状态更新定时器
        updatePoseStatus()
    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.stop()
        statusUpdateRunnable?.let {
            statusUpdateHandler?.removeCallbacks(it)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        r2?.let {
            mHandler2?.removeCallbacks(it)
        }
        statusUpdateRunnable?.let {
            statusUpdateHandler?.removeCallbacks(it)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.stop()
        r2?.let {
            mHandler2?.removeCallbacks(it)
        }
        statusUpdateRunnable?.let {
            statusUpdateHandler?.removeCallbacks(it)
        }
    }

    /**
     * 保存 Bitmap 到本地
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @param bitmap   bitmap 图像文件
     */
    fun saveBitmap(filePath: String, fileName: String?, bitmap: Bitmap) {
        val file = File(filePath)
        //判断指定文件夹的路径是否存在
        if (!file.exists()) {
            Log.d("Save Bitmap", "TargetPath isn't exist")
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            val saveFile = File("$filePath/$fileName.jpg")
            try {
                val saveImgOut = FileOutputStream(saveFile)
                // compress - 压缩的意思
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, saveImgOut)
                //存储完成后需要清除相关的进程
                saveImgOut.flush()
                saveImgOut.close()
                Log.e("TEST", "图片保存成功")
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private fun bindAllCameraUseCases() {
        // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
        cameraProvider?.unbindAll()
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider?.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(binding.previewView.getSurfaceProvider())
        cameraProvider?.bindToLifecycle( /* lifecycleOwner= */this,
            cameraSelector!!,
            previewUseCase
        )
    }

    private fun bindAnalysisUseCase() {
        if (analysisUseCase != null) {
            cameraProvider?.unbind(analysisUseCase)
        }
        imageProcessor?.stop()

        try {
            if (POSE_DETECTION == selectedModel) {
                val poseDetectorOptions =
                    PreferenceUtils.getPoseDetectorOptionsForLivePreview(this)
                val shouldShowInFrameLikelihood =
                    PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this)
                val visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)
                val rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)
                val runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this)
                imageProcessor =
                    PoseDetectorProcessor(
                        this,
                        poseDetectorOptions,
                        shouldShowInFrameLikelihood,
                        visualizeZ,
                        rescaleZ,
                        runClassification,  /* isStreamMode = */
                        true
                    )
            } else {
                throw IllegalStateException("Invalid model name")
            }
        } catch (e: Exception) {
            Logger.e("Can not create image processor: $selectedModel", e)
            return
        }

        val builder = ImageAnalysis.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase!!.setAnalyzer( // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy? ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy!!.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        binding.graphicOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        binding.graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    // Capture the current frame bitmap for pose evaluation
                    currentFrameBitmap =
                        binding.previewView.bitmap?.copy(Bitmap.Config.ARGB_8888, true)

                    imageProcessor!!.processImageProxy(imageProxy, binding.graphicOverlay)
                } catch (e: MlKitException) {
                    Logger.e("Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(
                        applicationContext,
                        e.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        cameraProvider?.bindToLifecycle( /* lifecycleOwner= */this,
            cameraSelector!!,
            analysisUseCase
        )
    }

    private val requiredPermissions: Array<String>
        get() {
            try {
                val info =
                    this.packageManager
                        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                return if (ps != null && ps.size > 0) {
                    ps
                } else {
                    arrayOf<String>("0")
                }
            } catch (_: Exception) {
                return arrayOf<String>("0")
            }
        }

    private fun allPermissionsGranted(): Boolean {
        for (permission in this.requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> =
                ArrayList<String?>()
            for (permission in this.requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    if (!permission.contains("LOCATION") && !permission.contains("CONTACT") && !permission.contains(
                            "AUDIO"
                        ) && !permission.contains("CALL") && !permission.contains("SMS")
                    ) {
                        allNeededPermissions.add(permission)
                    }
                }
            }

            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray<String?>(),
                    PERMISSION_REQUESTS
                )
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        Logger.e("Permission granted!")
        if (allPermissionsGranted()) {
            bindAllCameraUseCases()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onQuickShotSuccess(path: String?) {
    }

    override fun onQuickShotFailed(path: String?, errorMsg: String?) {
    }

    /**
     * 定时更新姿势状态
     */
    private fun updatePoseStatus() {
        statusUpdateHandler = Handler(Looper.getMainLooper())
        statusUpdateRunnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                val warningPoseList = BaduanjinPoseEvaluator.getInstance().getAllWarningPoseList()

                runOnUiThread {
                    if (warningPoseList.isEmpty()) {
                        // 正常状态
                        binding.capsuleBackground.setBackgroundResource(R.drawable.capsule_background_normal)
                        binding.statusText.text = "正常"
                        binding.statusIcon.setImageResource(R.drawable.ic_status_normal)
                        // 停止闪烁动画
                        binding.statusCapsule.clearAnimation()
                    } else {
                        // 错误状态
                        binding.capsuleBackground.setBackgroundResource(R.drawable.capsule_background_error)
                        binding.statusText.text = "错误: ${warningPoseList.size}"
                        binding.statusIcon.setImageResource(R.drawable.ic_status_error)

                        // 添加闪烁动画
                        if (binding.statusCapsule.animation == null) {
                            val blinkAnimation = AlphaAnimation(1.0f, 0.6f).apply {
                                duration = 500
                                repeatMode = Animation.REVERSE
                                repeatCount = Animation.INFINITE
                            }
                            binding.statusCapsule.startAnimation(blinkAnimation)
                        }
                    }
                }

                // 每500毫秒更新一次状态
                statusUpdateHandler?.postDelayed(this, 500)
            }
        }
        statusUpdateHandler?.post(statusUpdateRunnable!!)
    }

    companion object {
        private const val PERMISSION_REQUESTS = 1

        // 姿态检测模型
        private const val POSE_DETECTION = "姿态检测"

        // 选择模型变量
        private const val STATE_SELECTED_MODEL = "selected_model"


        // 训练动作数据列表
        var poseEntityList: MutableList<PoseEntity>? = null

        // 当前动作名称
        var nowPoseName: String? = "AiTrainer"

        // 当前动作的下标
        @JvmField
        var nowPoseIndex: Int = 0

        // 首页倒计时
        @JvmField
        var countdownSeconds: Int = 5

        // 训练是否结束
        @JvmField
        var isEnd: Boolean = false

        var imageSavePath: String? = null

        // 当前时间戳
        var timeMarkPoint: Long = 0
        var countMinutes: Float = 0f

        @JvmField
        var countTimes: Int = 0

        // Store the current camera frame
        @Volatile
        var currentFrameBitmap: Bitmap? = null

        fun start(context: Context) {
            val intent = Intent(context, CameraXLivePreviewActivity::class.java)
            context.startActivity(intent)
        }

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Logger.e("Permission granted: $permission")
                return true
            }
            Logger.e("Permission NOT granted: $permission")
            return false
        }
    }
}
