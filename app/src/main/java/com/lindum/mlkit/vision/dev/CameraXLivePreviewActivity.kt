package com.lindum.mlkit.vision.dev

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.MlKitException
import com.lindum.mlkit.vision.dev.QuickShot.QuickShotListener
import com.lindum.mlkit.vision.dev.entity.PoseEntity
import com.lindum.mlkit.vision.dev.posedetector.PoseDataUtils
import com.lindum.mlkit.vision.dev.posedetector.PoseDetectorProcessor
import com.lindum.mlkit.vision.dev.posedetector.classification.PoseClassifierProcessor
import com.lindum.mlkit.vision.dev.preference.PreferenceUtils
import com.lindum.mlkit.vision.dev.preference.SettingsActivity
import com.lindum.mlkit.vision.dev.result.TrainingResult
import com.lindum.mlkit.vision.dev.utils.FileUtils
import com.lindum.mlkit.vision.dev.view.HomePage
import com.lindum.mlkit.vision.dev.view.PoseDetailsActivity
import com.orhanobut.logger.Logger
import com.ramotion.circlemenu.CircleMenuView
import com.xuexiang.xui.XUI
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.SingleButtonCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.MessageFormat
import java.time.LocalDate
import java.util.Random
import kotlin.math.abs

/**
 * Live preview demo app for ML Kit APIs using CameraX.
 *
 * @author: MarkGosling
 * @date: 2022/1/25 16:13
 */
@KeepName
class CameraXLivePreviewActivity : AppCompatActivity(), OnRequestPermissionsResultCallback,
    AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, QuickShotListener,
    OnTouchListener {
    // TTS 语音助手
    private var textToSpeech: TextToSpeech? = null

    // 相机预览视图组件
    private var previewView: PreviewView? = null

    // 上一个动作按钮
    var prePoseImageButton: Button? = null

    // 下一个动作按钮
    var nextPoseImageButton: Button? = null

    // ml kit 绘图层
    private var graphicOverlay: GraphicOverlay? = null

    // todo
    private var cameraProvider: ProcessCameraProvider? = null

    // todo
    private var previewUseCase: Preview? = null

    // todo
    private var analysisUseCase: ImageAnalysis? = null

    // todo
    private var imageProcessor: VisionImageProcessor? = null

    // todo
    private var needUpdateGraphicOverlayImageSourceInfo = false

    // 选择模型 姿态检测
    private var selectedModel: String = POSE_DETECTION

    // todo
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    // todo
    private var cameraSelector: CameraSelector? = null

    // 定时器
    var mHandler2: Handler? = null
    var r2: Runnable? = null
    var mHandler3: Handler? = null
    var r3: Runnable? = null

    /**
     * 左下角信息
     */
    // 动作名称
    private var poseName: TextView? = null

    // 第几个动作
    private var poseIndexText: TextView? = null

    // 当前动作进度
    private var poseProcessInfo: TextView? = null

    // 倒计时秒数
    private var countdownSecondsTextView: TextView? = null

    // 现在动作进行的秒数
    private var nowPoseProcessSeconds = 0

    // 训练总时间
    private var totalTrainingTime = 0

    // 完成动作组数
    private var countGroupsOfPose = 0

    // 下一个动作按钮
    var nextPoseButton: LinearLayout? = null

    // 上一个动作按钮
    var prePoseButton: LinearLayout? = null

    // 是否提醒过呼吸方法
    private var isRemindedBreathingMethods = false

    // 环形按钮
    var circleMenuView: CircleMenuView? = null
    var mLongPressRunnable: Runnable? = null
    var mLastMotionX: Int = 0
    var mLastMotionY: Int = 0
    var isLongPress: Boolean = false
    var isMoved: Boolean = false

    @RequiresApi(api = VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 本软件兼容 Android5 以上的版本
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            MaterialDialog.Builder(this@CameraXLivePreviewActivity)
                .iconRes(R.mipmap.tips)
                .title("警告")
                .content(
                    "CameraX仅支持SDK版本大于21的设备，当前设备SDK版本为\n" +
                            "                            + VERSION.SDK_INT"
                )
                .positiveText("确认并退出")
                .onPositive(SingleButtonCallback { dialog: MaterialDialog?, which: DialogAction? -> finish() })
                .show()
        }
        // UI 框架初始化
        XUI.initTheme(this)
        // todo
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        // 设置布局
        setContentView(R.layout.activity_vision_camerax_live_preview)
        // 初始化
        init()
        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mLongPressRunnable = Runnable { handler.sendMessage(handler.obtainMessage(1)) }


        // 设置点击监听事件获取点击位置
        val rootView = findViewById<ConstraintLayout>(R.id.rootView)
        rootView.setOnTouchListener(this)

        // SD 卡根目录
        val sdPath = Environment.getExternalStorageDirectory().getPath()
        //            // 姿态数据工具类
        PoseDataUtils.writeJSON2Loacl(poseEntityList, sdPath + "/AiTrainer/poseData.json")

        //            isPermissionRequested = true;
//        }

        // 定时更新变量定时器
        updateVariable()
        // 从主页获取数据库中的动作数据
        poseEntityList = HomePage.poseEntityArrayList


        nowPoseName = poseEntityList!!.get(nowPoseIndex).getPoseName()

        // 跳转至结果界面
        val button = findViewById<Button>(R.id.analysisResult)
        button.setOnClickListener(View.OnClickListener { view: View? ->
            val poseEntity: PoseEntity = poseEntityList!!.get(nowPoseIndex)
            countMinutes = nowPoseProcessSeconds / 60f
            if (poseEntity.getPoseType() == 1) {
                poseEntity.setCountTimes(countTimes)
            } else {
                poseEntity.setCountMinutes(countMinutes)
            }
            countMinutes = 0f
            countTimes = 0
            val intent = Intent(this@CameraXLivePreviewActivity, TrainingResult::class.java)
            intent.putExtra("totalTrainingTime", totalTrainingTime)
            intent.putExtra("groupsOfPose", countGroupsOfPose)
            // 停止训练
            isEnd = true
            startActivity(intent)
            finish()
        })

        // 启动截图定时器
        this.screenCut


        previewView = findViewById<PreviewView?>(R.id.preview_view)
        if (previewView == null) {
            Logger.e("previewView is null")
        }

        // 截图
        if (graphicOverlay == null) {
            Logger.e("graphicOverlay is null")
        }
        val spinner = findViewById<Spinner>(R.id.spinner)
        val options: MutableList<String?> = ArrayList<String?>()
        options.add(POSE_DETECTION)
        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter<String?>(this, R.layout.spinner_style, options)
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter)
        spinner.setOnItemSelectedListener(this)

        val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)

        ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
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
        val settingsButton = findViewById<ImageView>(R.id.settings_button)
        settingsButton.setOnClickListener(
            View.OnClickListener { v: View? ->
                val intent = Intent(getApplicationContext(), SettingsActivity::class.java)
                intent.putExtra(
                    SettingsActivity.EXTRA_LAUNCH_SOURCE,
                    SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW
                )
                startActivity(intent)
            })

        nextPoseImageButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex + 1 < poseEntityList!!.size) {
                nowPoseIndex++
                count = 0f
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
                textToSpeech!!.speak(
                    "下一个动作" + poseEntityList!!.get(nowPoseIndex).getPoseName(),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                isRemindedBreathingMethods = false
            }
        })
        prePoseImageButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex - 1 >= 0) {
                nowPoseIndex--
                count = 0f
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
            }
        })
        nextPoseButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex + 1 < poseEntityList!!.size) {
                nowPoseIndex++
                count = 0f
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
                textToSpeech!!.speak(
                    "下一个动作" + poseEntityList!!.get(nowPoseIndex).getPoseName(),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                isRemindedBreathingMethods = false
            }
        })
        prePoseButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (nowPoseIndex - 1 >= 0) {
                nowPoseIndex--
                count = 0f
                countdownSeconds = 3
                nowPoseProcessSeconds = 0
                updatePoseInfo()
            }
        })
        if (!allPermissionsGranted()) {
            this.runtimePermissions
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val x = event.getX(0).toInt()
        val y = event.getY(0).toInt()
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                circleMenuView!!.close(false)
                isLongPress = false
                isMoved = false
                mLastMotionX = x
                mLastMotionY = y
                Logger.e("结束位置：(" + event.getX() + "," + event.getY())
                circleMenuView!!.setX(event.getX() - 400)
                circleMenuView!!.setY(event.getY() - 400)
                handler.postDelayed(mLongPressRunnable!!, 300)
                circleMenuView!!.setVisibility(View.INVISIBLE)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isMoved) {
                    return false
                }
                if (abs((mLastMotionX - x).toDouble()) > 50
                    || abs((mLastMotionY - y).toDouble()) > 50
                ) {
                    isMoved = true
                    handler.removeCallbacks(mLongPressRunnable!!)
                }
            }

            MotionEvent.ACTION_UP -> handler.removeCallbacks(mLongPressRunnable!!)
            else -> {}
        }
        /**
         * 注意返回值
         * true：view继续响应Touch操作；
         * false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
         */
        return true
    }

    @RequiresApi(api = VERSION_CODES.O)
    fun init() {
        // 初始化变量
        countdownSeconds = 5
        nowPoseIndex = 0
        warnPart = ArrayList<IntArray?>()
        poseEntityList = ArrayList<PoseEntity>()
        pose1 = false
        // 创建截图文件夹
        timeMarkPoint = System.currentTimeMillis() / 1000
        imageSavePath = HomePage.appFilePath + "/files/analysisReport/" + LocalDate.now()
            .toString() + "/" + timeMarkPoint + "/"
        FileUtils.createFile(imageSavePath, FileUtils.CREATE_FOLDER)
        // 初始化组件
        poseName = findViewById<TextView>(R.id.poseName)
        poseIndexText = findViewById<TextView>(R.id.nowPoseIndexText)
        graphicOverlay = findViewById<GraphicOverlay?>(R.id.graphic_overlay)
        poseProcessInfo = findViewById<TextView>(R.id.poseProcessInfo)
        countdownSecondsTextView = findViewById<TextView>(R.id.countdownSecondsText)
        nextPoseButton = findViewById<LinearLayout>(R.id.nextButton)
        prePoseButton = findViewById<LinearLayout>(R.id.preButton)
        prePoseImageButton = findViewById<Button>(R.id.prePoseImage)
        nextPoseImageButton = findViewById<Button>(R.id.nextPoseImage)
        circleMenuView = findViewById<CircleMenuView>(R.id.circle_menu)
        circleMenuView!!.setVisibility(View.INVISIBLE)
        circleMenuView!!.setEventListener(object : CircleMenuView.EventListener() {
            override fun onMenuOpenAnimationStart(view: CircleMenuView) {
                Log.d("D", "onMenuOpenAnimationStart")
            }

            override fun onMenuOpenAnimationEnd(view: CircleMenuView) {
                Log.d("D", "onMenuOpenAnimationEnd")
            }

            override fun onMenuCloseAnimationStart(view: CircleMenuView) {
                Log.d("D", "onMenuCloseAnimationStart")
            }

            override fun onMenuCloseAnimationEnd(view: CircleMenuView) {
                Log.d("D", "onMenuCloseAnimationEnd")
            }

            override fun onButtonClickAnimationStart(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonClickAnimationStart| index: " + index)
            }

            override fun onButtonClickAnimationEnd(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonClickAnimationEnd| index: " + index)
                circleMenuView!!.setVisibility(View.INVISIBLE)
            }

            override fun onButtonLongClick(view: CircleMenuView, index: Int): Boolean {
                Log.d("D", "onButtonLongClick| index: " + index)
                return true
            }

            override fun onButtonLongClickAnimationStart(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonLongClickAnimationStart| index: " + index)
            }

            override fun onButtonLongClickAnimationEnd(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonLongClickAnimationEnd| index: " + index)
                circleMenuView!!.setVisibility(View.INVISIBLE)
            }
        })
        val explainButton = findViewById<LinearLayout>(R.id.explain)
        explainButton.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@CameraXLivePreviewActivity, PoseDetailsActivity::class.java)
            intent.putExtra("feeling", poseEntityList!!.get(nowPoseIndex).getPoseFeeling())
            intent.putExtra("steps", poseEntityList!!.get(nowPoseIndex).getStepsOfPose())
            intent.putExtra(
                "commonMistakes",
                poseEntityList!!.get(nowPoseIndex).getCommonMistakes()
            )
            intent.putExtra("details", poseEntityList!!.get(nowPoseIndex).getDetailTips())
            intent.putExtra("breath", poseEntityList!!.get(nowPoseIndex).getBreathTips())
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
                        "第一个动作，" + poseEntityList!!.get(nowPoseIndex).getPoseName(),
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                }
            } else {
                Logger.e("语音初始化失败")
            }
        })
    }

    /**
     * 定时清空警告点、倒数秒数刷新、计算总运动时长
     */
    fun updateVariable() {
        mHandler2 = Handler()
        r2 = object : Runnable {
            override fun run() {
                warnPart!!.clear()
                updatePoseInfo()
                if (countdownSeconds >= 0) {
                    countdownSecondsTextView!!.setVisibility(View.VISIBLE)
                    textToSpeech!!.speak(
                        countdownSeconds.toString() + "",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                    countdownSeconds--
                }
                if (countdownSeconds == -1) {
                    countdownSecondsTextView!!.setVisibility(View.INVISIBLE)
                    // 语音助手提示
                    val voiceAssistantHandle = Handler()
                    val voiceAssistantRunnable: Runnable = object : Runnable {
                        override fun run() {
                            voiceAssistantHandle.postDelayed(this, 8000)
                            // 已经提醒过呼吸方法
                            if (!isRemindedBreathingMethods) {
                                textToSpeech!!.speak(
                                    if (poseEntityList!!.get(nowPoseIndex)
                                            .getBreathTips() == null
                                    ) "注意均匀呼吸，不要憋气" else poseEntityList!!.get(
                                        nowPoseIndex
                                    ).getBreathTips(), TextToSpeech.QUEUE_ADD, null
                                )
                                isRemindedBreathingMethods = true
                            }
                        }
                    }
                    voiceAssistantHandle.post(voiceAssistantRunnable)
                }

                // TODO: 2022/3/8
//                Logger.e("12312312" + poseEntityList.get(nowPoseIndex).getMonitorPosePartList().toString());

                // 现在动作进行的秒数增加
                if (countdownSeconds == -1) {
                    nowPoseProcessSeconds++
                }
                if (!isEnd) {
                    totalTrainingTime++
                }
                //do something
                //每隔1s循环执行run方法
                mHandler2!!.postDelayed(this, 1000)
            }
        }
        mHandler2!!.post(r2!!)
    }

    @SuppressLint("SetTextI18n")
    fun updatePoseInfo() {
        // 更新姿势名称
        nowPoseName = poseEntityList!![nowPoseIndex].getPoseName()
        poseIndexText!!.setText(
            MessageFormat.format(
                "{0}/{1}",
                nowPoseIndex + 1,
                poseEntityList!!.size
            )
        )
        poseName!!.setText(nowPoseName)
        Log.e("TEST", poseEntityList!!.get(nowPoseIndex).getPoseType().toString() + "")
        if (poseEntityList!!.get(nowPoseIndex).getPoseType() == 1) {
            // 更新当前动作进度 个数
            poseProcessInfo!!.setText(count.toInt().toString() + "")
        } else if (poseEntityList!!.get(nowPoseIndex).getPoseType() == 0) {
            // 更新当前动作进度 秒数
            poseProcessInfo!!.setText(nowPoseProcessSeconds.toString() + "\"")
        }
        // 更新倒数秒数
        countdownSecondsTextView!!.setText(countdownSeconds.toString() + "")
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
    }

    @Synchronized
    override fun onItemSelected(
        parent: AdapterView<*>, view: View?, pos: Int,
        id: Long
    ) {
        selectedModel = parent.getItemAtPosition(pos).toString()
        Logger.e("Selected model: " + selectedModel)
        bindAnalysisUseCase()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing.
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                circleMenuView!!.setVisibility(View.VISIBLE)
                circleMenuView!!.open(true)
            } else {
            }
        }
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
                Logger.e("Set facing to " + newLensFacing)
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            // Falls through
        }
        Toast.makeText(
            getApplicationContext(),
            "This device does not have lens with facing: " + newLensFacing,
            Toast.LENGTH_SHORT
        )
            .show()
    }

    public override fun onResume() {
        super.onResume()
        // 开始训练
        isEnd = false
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mHandler3!!.removeCallbacks(r3!!)
        mHandler2!!.removeCallbacks(r2!!)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
        mHandler3!!.removeCallbacks(r3!!)
        mHandler2!!.removeCallbacks(r2!!)
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
            val saveFile = File(filePath + "/" + fileName + ".jpg")
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
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView!!.getSurfaceProvider())
        cameraProvider!!.bindToLifecycle( /* lifecycleOwner= */this,
            cameraSelector!!,
            previewUseCase
        )
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }

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
            Logger.e("Can not create image processor: " + selectedModel, e)
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
                    val rotationDegrees = imageProxy!!.getImageInfo().getRotationDegrees()
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay!!.setImageSourceInfo(
                            imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped
                        )
                    } else {
                        graphicOverlay!!.setImageSourceInfo(
                            imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor!!.processImageProxy(imageProxy, graphicOverlay)
                } catch (e: MlKitException) {
                    Logger.e("Failed to process image. Error: " + e.getLocalizedMessage())
                    Toast.makeText(
                        getApplicationContext(),
                        e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        cameraProvider!!.bindToLifecycle( /* lifecycleOwner= */this,
            cameraSelector!!,
            analysisUseCase
        )
    }

    val screenCut: Unit
        // // TODO: 2022/3/23 会连续多拍
        get() {
            /* 实时监测是否有警告点，如果有就截图 */
            mHandler3 = Handler()
            r3 = object : Runnable {
                override fun run() {
//                if (!warnPart.isEmpty() && confidence == 1 && !confidenceNotOne) {
//                if (!warnPart.isEmpty() && PoseClassifierProcessor.confidence == 1&& confidenceNotOne) {
                    // 如果这里置信度等于1才截图，其他情况的固定部位就不会截图
                    if (!warnPart!!.isEmpty() && confidence == 1f && confidenceNotOne) {
                        confidenceNotOne = false
                        val random = Random()
                        val fileName =
                            nowPoseIndex.toString() + "-" + abs(
                                random.nextInt().toDouble()
                            ) + "-" + PoseClassifierProcessor.maxConfidenceClass
                        val bitmap2 =
                            QuickShot.of(graphicOverlay!!).getBitmap()
                        if (bitmap2 != null) {
                            imageView =
                                findViewById<ImageView?>(R.id.testImage)
                            val bitmap1 = previewView!!.getBitmap()
                            // 原图和关键点图片混合
//                        imageView.setImageBitmap(combineBitmap(bitmap1, bitmap2));
                            // 将当前错误的分析结果传递至 TrainingResult 界面
                            if (PoseClassifierProcessor.poseTips != null && PoseClassifierProcessor.poseTips != "") {
                                TrainingResult.resultImage.add(
                                    combineBitmap(
                                        bitmap1,
                                        bitmap2
                                    )
                                )
                                TrainingResult.resultName.add(nowPoseName)
                                TrainingResult.resultTips.add(PoseClassifierProcessor.poseTips)
                                TrainingResult.wrongImagePath.add(imageSavePath + fileName + ".jpg")
                            }
                            Logger.e("生成的建议" + PoseClassifierProcessor.poseTips)
                            if (bitmap1 != null) {
                                object : Thread("截取图片并保存") {
                                    @RequiresApi(api = VERSION_CODES.O)
                                    override fun run() {
                                        saveBitmap(
                                            imageSavePath!!,
                                            fileName,
                                            bitmap1
                                        )
                                    }
                                }.start()
                            }
                        }
                        cleanWarnPart()
                    }

                    // 当前动作类型
                    val nowPoseType: Int = poseEntityList!!.get(
                        nowPoseIndex
                    ).getPoseType()
                    // 当前动作过程量上限
                    val nowPoseProcessValue: Int =
                        poseEntityList!!.get(
                            nowPoseIndex
                        ).getPoseProcessValue()
                    if (nowPoseType == 1 && count == (nowPoseProcessValue + 1).toFloat()) {
                        // 动作组数加一
                        countGroupsOfPose++
                        if (nowPoseIndex == poseEntityList!!.size - 1 && !isEnd) {
                            Log.e("total", totalTrainingTime.toString() + "")
                            // 跳转到结果界面
                            val intent =
                                Intent(this@CameraXLivePreviewActivity, TrainingResult::class.java)
                            intent.putExtra("totalTrainingTime", totalTrainingTime)
                            intent.putExtra("groupsOfPose", countGroupsOfPose)
                            // 停止训练
                            isEnd = true
                            startActivity(intent)
                            finish()
                        } else {
                            val poseEntity: PoseEntity =
                                poseEntityList!!.get(
                                    nowPoseIndex
                                )
                            if (poseEntity.getPoseType() == 1) {
                                poseEntity.setCountTimes(countTimes)
                            } else {
                                poseEntity.setCountMinutes(countMinutes)
                            }
                            // 动作持续数值清空
                            nowPoseProcessSeconds = 0
                            countMinutes = 0f
                            countTimes = 0
                            nextPoseButton!!.callOnClick()
                        }
                    }
                    if (nowPoseType == 0 && nowPoseProcessSeconds == nowPoseProcessValue + 1 && nowPoseProcessSeconds != 0) {
                        // 动作组数加一
                        countGroupsOfPose++
                        // 最后一个动作
                        if (nowPoseIndex == poseEntityList!!.size - 1 && !isEnd) {
                            // 跳转到结果界面
                            val intent =
                                Intent(this@CameraXLivePreviewActivity, TrainingResult::class.java)
                            intent.putExtra("totalTrainingTime", totalTrainingTime)
                            intent.putExtra("groupsOfPose", countGroupsOfPose)
                            // 停止训练
                            isEnd = true
                            startActivity(intent)
                            finish()
                        } else {
                            val poseEntity: PoseEntity =
                                poseEntityList!!.get(
                                    nowPoseIndex
                                )
                            countMinutes =
                                nowPoseProcessSeconds / 60f
                            if (poseEntity.getPoseType() == 1) {
                                poseEntity.setCountTimes(countTimes)
                            } else {
                                poseEntity.setCountMinutes(countMinutes)
                            }
                            // 动作持续数值清空
                            nowPoseProcessSeconds = 0
                            countMinutes = 0f
                            countTimes = 0
                            nextPoseButton!!.callOnClick()
                        }
                    }
                    mHandler3!!.postDelayed(this, 800)
                }
            }
            mHandler3!!.post(r3!!)
        }

    private val requiredPermissions: Array<String>
        get() {
            try {
                val info =
                    this.getPackageManager()
                        .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.size > 0) {
                    return ps
                } else {
                    return arrayOf<String>("0")
                }
            } catch (e: Exception) {
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

    companion object {
        // 权限请求变量 todo
        private const val PERMISSION_REQUESTS = 1

        // 动作置信度不为 1 flag 变量
        @JvmField
        @Volatile
        var confidenceNotOne: Boolean = true

        // 动作置信度变量
        @JvmField
        @Volatile
        var confidence: Float = 0f

        // 姿态检测模型
        private const val POSE_DETECTION = "姿态检测"

        // 选择模型变量
        private const val STATE_SELECTED_MODEL = "selected_model"

        // 动作计数
        private var count = 0f

        // 警告部位列表
        private var warnPart: MutableList<IntArray?>? = null

        // todo 判断的 flag 变量
        private var pose1 = false

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
        var imageView: ImageView? = null

        // 截图保存路径
        var imageSavePath: String? = null

        // 当前时间戳
        var timeMarkPoint: Long = 0
        var countMinutes: Float = 0f

        @JvmField
        var countTimes: Int = 0

        fun cleanWarnPart() {
            warnPart!!.clear()
        }

        fun isPose1(): Boolean {
            return pose1
        }

        fun setPose1(pose1: Boolean) {
            Companion.pose1 = pose1
        }

        fun start(context: Context) {
            val intent = Intent(context, CameraXLivePreviewActivity::class.java)
            context.startActivity(intent)
        }

        /**
         * 添加报错部位
         *
         * @param partIndex 报错部位的索引
         */
        @JvmStatic
        fun addWarnPart(partIndex: Int) {
            if (partIndex == 12) {
                warnPart!!.add(intArrayOf(25, 27, 31))
            } else if (partIndex == 11) {
                warnPart!!.add(intArrayOf(26, 28, 32))
            } else if (partIndex == 10) {
                warnPart!!.add(intArrayOf(23, 25, 27))
            } else if (partIndex == 9) {
                warnPart!!.add(intArrayOf(24, 26, 28))
            } else if (partIndex == 8) {
                warnPart!!.add(intArrayOf(11, 23, 25))
            } else if (partIndex == 7) {
                warnPart!!.add(intArrayOf(12, 24, 26))
            } else if (partIndex == 6) {
                warnPart!!.add(intArrayOf(13, 15, 19))
            } else if (partIndex == 5) {
                warnPart!!.add(intArrayOf(11, 13, 15))
            } else if (partIndex == 4) {
                warnPart!!.add(intArrayOf(13, 11, 23))
            } else if (partIndex == 3) {
                warnPart!!.add(intArrayOf(14, 12, 24))
            } else if (partIndex == 2) {
                warnPart!!.add(intArrayOf(12, 14, 16))
            } else if (partIndex == 1) {
                warnPart!!.add(intArrayOf(20, 16, 14))
            }
        }

        @JvmStatic
        fun getWarnPart(): MutableList<IntArray?> {
            return warnPart!!
        }

        fun addCount() {
            count += 0.5f
        }

        fun getCount(): Int {
            return count.toInt()
        }

        /**
         * 合并两张bitmap为一张
         *
         * @param background 底层的图片
         * @param foreground 上层的图片
         * @return Bitmap 合成后的图片
         */
        fun combineBitmap(background: Bitmap?, foreground: Bitmap): Bitmap? {
            if (background == null) {
                return null
            }
            val bgWidth = background.getWidth()
            val bgHeight = background.getHeight()
            val fgWidth = foreground.getWidth()
            val fgHeight = foreground.getHeight()
            val newmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newmap)
            canvas.drawBitmap(background, 0f, 0f, null)
            canvas.drawBitmap(foreground, 0f, 0f, null)
            canvas.save()
            canvas.restore()
            return newmap
        }

        /**
         * 缩放bitmap
         *
         * @param bm
         * @param newWidth
         * @param newHeight
         * @return
         */
        fun zoomImg(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            // 获得图片的宽高
            val width = bm.getWidth()
            val height = bm.getHeight()
            // 计算缩放比例
            val scaleWidth = (newWidth.toFloat()) / width
            val scaleHeight = (newHeight.toFloat()) / height
            // 取得想要缩放的matrix参数
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)
            // 得到新的图片
            val newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
            return newbm
        }

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Logger.e("Permission granted: " + permission)
                return true
            }
            Logger.e("Permission NOT granted: " + permission)
            return false
        }
    }
}
