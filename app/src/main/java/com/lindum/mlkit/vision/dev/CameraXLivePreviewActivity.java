package com.lindum.mlkit.vision.dev;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.lindum.mlkit.vision.dev.entity.PoseEntity;
import com.lindum.mlkit.vision.dev.posedetector.PoseDataUtils;
import com.lindum.mlkit.vision.dev.posedetector.PoseDetectorProcessor;
import com.lindum.mlkit.vision.dev.posedetector.classification.PoseClassifierProcessor;
import com.lindum.mlkit.vision.dev.preference.PreferenceUtils;
import com.lindum.mlkit.vision.dev.preference.SettingsActivity;
import com.lindum.mlkit.vision.dev.result.TrainingResult;
import com.lindum.mlkit.vision.dev.utils.FileUtils;
import com.lindum.mlkit.vision.dev.view.HomePage;
import com.lindum.mlkit.vision.dev.view.PoseDetailsActivity;
import com.orhanobut.logger.Logger;
import com.ramotion.circlemenu.CircleMenuView;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Live preview demo app for ML Kit APIs using CameraX.
 *
 * @author: MarkGosling
 * @date: 2022/1/25 16:13
 **/


@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXLivePreviewActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, QuickShot.QuickShotListener, View.OnTouchListener {
    // TTS 语音助手
    private TextToSpeech textToSpeech;
    // 权限请求变量 todo
    private static final int PERMISSION_REQUESTS = 1;
    // 动作置信度不为 1 flag 变量
    public volatile static boolean confidenceNotOne = true;
    // 动作置信度变量
    public volatile static float confidence = 0;
    // 姿态检测模型
    private static final String POSE_DETECTION = "姿态检测";
    // 选择模型变量
    private static final String STATE_SELECTED_MODEL = "selected_model";
    // 动作计数
    private static float count = 0;
    // 警告部位列表
    private static List<int[]> warnPart;
    // todo 判断的 flag 变量
    private static boolean pose1;
    // 相机预览视图组件
    private PreviewView previewView;
    // 上一个动作按钮
    Button prePoseImageButton;
    // 下一个动作按钮
    Button nextPoseImageButton;
    // ml kit 绘图层
    private GraphicOverlay graphicOverlay;

    @Nullable
    // todo
    private ProcessCameraProvider cameraProvider;
    @Nullable
    // todo
    private Preview previewUseCase;
    @Nullable
    // todo
    private ImageAnalysis analysisUseCase;
    @Nullable
    // todo
    private VisionImageProcessor imageProcessor;
    // todo
    private boolean needUpdateGraphicOverlayImageSourceInfo;
    // 选择模型 姿态检测
    private String selectedModel = POSE_DETECTION;
    // todo
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    // todo
    private CameraSelector cameraSelector;
    // 训练动作数据列表
    public static List<PoseEntity> poseEntityList;
    // 当前动作名称
    public static String nowPoseName = "AiTrainer";
    // 当前动作的下标
    public static int nowPoseIndex = 0;
    // 首页倒计时
    public static int countdownSeconds = 5;
    // 训练是否结束
    public static boolean isEnd = false;
    // 定时器
    Handler mHandler2;
    Runnable r2;
    Handler mHandler3;
    Runnable r3;
    /**
     * 左下角信息
     */
    // 动作名称
    private TextView poseName;
    // 第几个动作
    private TextView poseIndexText;
    // 当前动作进度
    private TextView poseProcessInfo;
    // 倒计时秒数
    private TextView countdownSecondsTextView;
    // 现在动作进行的秒数
    private int nowPoseProcessSeconds = 0;
    // 训练总时间
    private int totalTrainingTime = 0;
    // 完成动作组数
    private int countGroupsOfPose = 0;
    // 下一个动作按钮
    LinearLayout nextPoseButton;
    // 上一个动作按钮
    LinearLayout prePoseButton;
    // 是否提醒过呼吸方法
    private boolean isRemindedBreathingMethods = false;
    public static ImageView imageView;
    // 截图保存路径
    public static String imageSavePath;
    // 当前时间戳
    public static long timeMarkPoint;
    // 环形按钮
    CircleMenuView circleMenuView;
    Runnable mLongPressRunnable;
    int mLastMotionX, mLastMotionY;
    boolean isLongPress;
    boolean isMoved;
    public static float countMinutes = 0;
    public static int countTimes = 0;

    @RequiresApi(api = VERSION_CODES.O)
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 本软件兼容 Android5 以上的版本
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            new MaterialDialog.Builder(CameraXLivePreviewActivity.this)
                    .iconRes(R.mipmap.tips)
                    .title("警告")
                    .content("CameraX仅支持SDK版本大于21的设备，当前设备SDK版本为\n" +
                            "                            + VERSION.SDK_INT")
                    .positiveText("确认并退出")
                    .onPositive((dialog, which) -> finish())
                    .show();
        }
        // UI 框架初始化
        XUI.initTheme(this);
        // todo
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        // 设置布局
        setContentView(R.layout.activity_vision_camerax_live_preview);
        // 初始化
        init();
        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mLongPressRunnable = () -> handler.sendMessage(handler.obtainMessage(1));


        // 设置点击监听事件获取点击位置
        ConstraintLayout rootView = findViewById(R.id.rootView);
        rootView.setOnTouchListener(this);

        // SD 卡根目录
        String sdPath = Environment.getExternalStorageDirectory().getPath();
//            // 姿态数据工具类
        PoseDataUtils.writeJSON2Loacl(poseEntityList, sdPath + "/AiTrainer/poseData.json");
//            isPermissionRequested = true;
//        }

        // 定时更新变量定时器
        updateVariable();
        // 从主页获取数据库中的动作数据
        poseEntityList = HomePage.poseEntityArrayList;


        nowPoseName = poseEntityList.get(nowPoseIndex).getPoseName();

        // 跳转至结果界面
        Button button = findViewById(R.id.analysisResult);
        button.setOnClickListener(view -> {
            PoseEntity poseEntity = poseEntityList.get(nowPoseIndex);
            countMinutes = nowPoseProcessSeconds / 60f;
            if (poseEntity.getPoseType() == 1) {
                poseEntity.setCountTimes(countTimes);
            } else {
                poseEntity.setCountMinutes(countMinutes);
            }
            countMinutes = 0;
            countTimes = 0;
            Intent intent = new Intent(CameraXLivePreviewActivity.this, TrainingResult.class);
            intent.putExtra("totalTrainingTime", totalTrainingTime);
            intent.putExtra("groupsOfPose", countGroupsOfPose);
            // 停止训练
            isEnd = true;
            startActivity(intent);
            finish();
        });

        // 启动截图定时器
        getScreenCut();


        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Logger.e("previewView is null");
        }
        // 截图

        if (graphicOverlay == null) {
            Logger.e("graphicOverlay is null");
        }
        Spinner spinner = findViewById(R.id.spinner);
        List<String> options = new ArrayList<>();
        options.add(POSE_DETECTION);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);

        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider ->

                        {
                            cameraProvider = provider;
                            if (allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });
        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v ->
                {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE,
                            SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW);
                    startActivity(intent);
                });

        nextPoseImageButton.setOnClickListener(view ->
        {
            if (nowPoseIndex + 1 < poseEntityList.size()) {
                nowPoseIndex++;
                count = 0;
                countdownSeconds = 3;
                nowPoseProcessSeconds = 0;
                updatePoseInfo();
                textToSpeech.speak("下一个动作" + poseEntityList.get(nowPoseIndex).getPoseName(), TextToSpeech.QUEUE_FLUSH, null);
                isRemindedBreathingMethods = false;
            }
        });
        prePoseImageButton.setOnClickListener(view ->
        {
            if (nowPoseIndex - 1 >= 0) {
                nowPoseIndex--;
                count = 0;
                countdownSeconds = 3;
                nowPoseProcessSeconds = 0;
                updatePoseInfo();
            }
        });
        nextPoseButton.setOnClickListener(view ->
        {
            if (nowPoseIndex + 1 < poseEntityList.size()) {
                nowPoseIndex++;
                count = 0;
                countdownSeconds = 3;
                nowPoseProcessSeconds = 0;
                updatePoseInfo();
                textToSpeech.speak("下一个动作" + poseEntityList.get(nowPoseIndex).getPoseName(), TextToSpeech.QUEUE_FLUSH, null);
                isRemindedBreathingMethods = false;
            }

        });
        prePoseButton.setOnClickListener(view ->
        {
            if (nowPoseIndex - 1 >= 0) {
                nowPoseIndex--;
                count = 0;
                countdownSeconds = 3;
                nowPoseProcessSeconds = 0;
                updatePoseInfo();
            }
        });
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX(0);
        int y = (int) event.getY(0);
        switch (event.getAction()) {
            /**
             * 点击的开始位置
             */
            case MotionEvent.ACTION_DOWN:
                circleMenuView.close(false);
                isLongPress = false;
                isMoved = false;
                mLastMotionX = x;
                mLastMotionY = y;
                Logger.e("结束位置：(" + event.getX() + "," + event.getY());
                circleMenuView.setX(event.getX() - 400);
                circleMenuView.setY(event.getY() - 400);
                handler.postDelayed(mLongPressRunnable, 300);
                circleMenuView.setVisibility(View.INVISIBLE);
                break;
            /**
             * 触屏实时位置
             */
            case MotionEvent.ACTION_MOVE:
                if (isMoved)
                    break;
                if (Math.abs(mLastMotionX - x) > 50
                        || Math.abs(mLastMotionY - y) > 50) {
                    isMoved = true;
                    handler.removeCallbacks(mLongPressRunnable);
                }
                break;
            /**
             * 离开屏幕的位置
             */
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(mLongPressRunnable);
                break;
            default:
                break;
        }
        /**
         *  注意返回值
         *  true：view继续响应Touch操作；
         *  false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
         */
        return true;
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void init() {
        // 初始化变量
        countdownSeconds = 5;
        nowPoseIndex = 0;
        warnPart = new ArrayList<>();
        poseEntityList = new ArrayList<>();
        pose1 = false;
        // 创建截图文件夹
        timeMarkPoint = System.currentTimeMillis() / 1000;
        imageSavePath = HomePage.appFilePath + "/files/analysisReport/" + LocalDate.now().toString() + "/" + timeMarkPoint + "/";
        FileUtils.createFile(imageSavePath, FileUtils.CREATE_FOLDER);
        // 初始化组件
        poseName = findViewById(R.id.poseName);
        poseIndexText = findViewById(R.id.nowPoseIndexText);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        poseProcessInfo = findViewById(R.id.poseProcessInfo);
        countdownSecondsTextView = findViewById(R.id.countdownSecondsText);
        nextPoseButton = findViewById(R.id.nextButton);
        prePoseButton = findViewById(R.id.preButton);
        prePoseImageButton = findViewById(R.id.prePoseImage);
        nextPoseImageButton = findViewById(R.id.nextPoseImage);
        circleMenuView = findViewById(R.id.circle_menu);
        circleMenuView.setVisibility(View.INVISIBLE);
        circleMenuView.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationStart");
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationEnd");
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationStart");
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationEnd");
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonClickAnimationStart| index: " + index);
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonClickAnimationEnd| index: " + index);
                circleMenuView.setVisibility(View.INVISIBLE);
            }

            @Override
            public boolean onButtonLongClick(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonLongClick| index: " + index);
                return true;
            }

            @Override
            public void onButtonLongClickAnimationStart(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonLongClickAnimationStart| index: " + index);
            }

            @Override
            public void onButtonLongClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonLongClickAnimationEnd| index: " + index);
                circleMenuView.setVisibility(View.INVISIBLE);
            }
        });
        LinearLayout explainButton = findViewById(R.id.explain);
        explainButton.setOnClickListener(view -> {
            Intent intent = new Intent(CameraXLivePreviewActivity.this, PoseDetailsActivity.class);
            intent.putExtra("feeling", poseEntityList.get(nowPoseIndex).getPoseFeeling());
            intent.putExtra("steps", poseEntityList.get(nowPoseIndex).getStepsOfPose());
            intent.putExtra("commonMistakes", poseEntityList.get(nowPoseIndex).getCommonMistakes());
            intent.putExtra("details", poseEntityList.get(nowPoseIndex).getDetailTips());
            intent.putExtra("breath", poseEntityList.get(nowPoseIndex).getBreathTips());
            intent.putExtra("poseIndex", nowPoseIndex);
            startActivity(intent);
        });
        // TTS 初始化
        textToSpeech = new TextToSpeech(this, i -> {
            Logger.e("初始化中...");
            if (i == TextToSpeech.SUCCESS) {
                Logger.e("语音初始化成功");
                if (nowPoseIndex == 0) {
                    textToSpeech.setSpeechRate(0.1f);
                    textToSpeech.speak("第一个动作，" + poseEntityList.get(nowPoseIndex).getPoseName(), TextToSpeech.QUEUE_ADD, null);

                }

            } else {
                Logger.e("语音初始化失败");
            }
        });
    }

    public static void cleanWarnPart() {
        warnPart.clear();
    }

    public static boolean isPose1() {
        return pose1;
    }

    public static void setPose1(boolean pose1) {
        CameraXLivePreviewActivity.pose1 = pose1;
    }

    public static List<PoseEntity> getPoseEntityList() {
        return poseEntityList;
    }

    public static void setPoseEntityList(List<PoseEntity> poseEntityList) {
        CameraXLivePreviewActivity.poseEntityList = poseEntityList;
    }

    public static void setNowPoseName(String nowPoseName) {
        CameraXLivePreviewActivity.nowPoseName = nowPoseName;
    }

    /**
     * 定时清空警告点、倒数秒数刷新、计算总运动时长
     */
    public void updateVariable() {
        mHandler2 = new Handler();
        r2 = new Runnable() {
            @Override
            public void run() {
                warnPart.clear();
                updatePoseInfo();
                if (countdownSeconds >= 0) {
                    countdownSecondsTextView.setVisibility(View.VISIBLE);
                    textToSpeech.speak(countdownSeconds + "", TextToSpeech.QUEUE_ADD, null);
                    countdownSeconds--;
                }
                if (countdownSeconds == -1) {
                    countdownSecondsTextView.setVisibility(View.INVISIBLE);
                    // 语音助手提示
                    Handler voiceAssistantHandle = new Handler();
                    Runnable voiceAssistantRunnable = new Runnable() {
                        @Override
                        public void run() {
                            voiceAssistantHandle.postDelayed(this, 8000);
                            // 已经提醒过呼吸方法
                            if (!isRemindedBreathingMethods) {
                                textToSpeech.speak(poseEntityList.get(nowPoseIndex).getBreathTips() == null ? "注意均匀呼吸，不要憋气" : poseEntityList.get(nowPoseIndex).getBreathTips(), TextToSpeech.QUEUE_ADD, null);
                                isRemindedBreathingMethods = true;
                            }
                        }
                    };
                    voiceAssistantHandle.post(voiceAssistantRunnable);
                }
                // TODO: 2022/3/8
//                Logger.e("12312312" + poseEntityList.get(nowPoseIndex).getMonitorPosePartList().toString());

                // 现在动作进行的秒数增加
                if (countdownSeconds == -1) {
                    nowPoseProcessSeconds++;
                }
                if (!isEnd) {
                    totalTrainingTime++;
                }
                //do something
                //每隔1s循环执行run方法
                mHandler2.postDelayed(this, 1000);
            }
        };
        mHandler2.post(r2);
    }

    @SuppressLint("SetTextI18n")
    public void updatePoseInfo() {
        // 更新姿势名称
        setNowPoseName(poseEntityList.get(nowPoseIndex).getPoseName());
        poseIndexText.setText(MessageFormat.format("{0}/{1}", nowPoseIndex + 1, poseEntityList.size()));
        poseName.setText(nowPoseName);
        Log.e("TEST", poseEntityList.get(nowPoseIndex).getPoseType() + "");
        if (poseEntityList.get(nowPoseIndex).getPoseType() == 1) {
            // 更新当前动作进度 个数
            poseProcessInfo.setText((int) count + "");
        } else if (poseEntityList.get(nowPoseIndex).getPoseType() == 0) {
            // 更新当前动作进度 秒数
            poseProcessInfo.setText(nowPoseProcessSeconds + "\"");
        }
        // 更新倒数秒数
        countdownSecondsTextView.setText(countdownSeconds + "");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTED_MODEL, selectedModel);
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos,
                                            long id) {
        selectedModel = parent.getItemAtPosition(pos).toString();
        Logger.e("Selected model: " + selectedModel);
        bindAnalysisUseCase();
    }

    /**
     * 添加报错部位
     *
     * @param partIndex 报错部位的索引
     */
    public static void addWarnPart(int partIndex) {
        if (partIndex == 12) {
            warnPart.add(new int[]{25, 27, 31});
        } else if (partIndex == 11) {
            warnPart.add(new int[]{26, 28, 32});
        } else if (partIndex == 10) {
            warnPart.add(new int[]{23, 25, 27});
        } else if (partIndex == 9) {
            warnPart.add(new int[]{24, 26, 28});
        } else if (partIndex == 8) {
            warnPart.add(new int[]{11, 23, 25});
        } else if (partIndex == 7) {
            warnPart.add(new int[]{12, 24, 26});
        } else if (partIndex == 6) {
            warnPart.add(new int[]{13, 15, 19});
        } else if (partIndex == 5) {
            warnPart.add(new int[]{11, 13, 15});
        } else if (partIndex == 4) {
            warnPart.add(new int[]{13, 11, 23});
        } else if (partIndex == 3) {
            warnPart.add(new int[]{14, 12, 24});
        } else if (partIndex == 2) {
            warnPart.add(new int[]{12, 14, 16});
        } else if (partIndex == 1) {
            warnPart.add(new int[]{20, 16, 14});
        }
    }

    public static List<int[]> getWarnPart() {
        return warnPart;
    }

    public static void addCount() {
        count += 0.5F;
    }

    public static int getCount() {
        return (int) count;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                circleMenuView.setVisibility(View.VISIBLE);
                circleMenuView.open(true);
            } else {

            }
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (cameraProvider == null) {
            return;
        }
        int newLensFacing =
                lensFacing == CameraSelector.LENS_FACING_FRONT
                        ? CameraSelector.LENS_FACING_BACK
                        : CameraSelector.LENS_FACING_FRONT;
        CameraSelector newCameraSelector =
                new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                Logger.e("Set facing to " + newLensFacing);
                lensFacing = newLensFacing;
                cameraSelector = newCameraSelector;
                bindAllCameraUseCases();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }
        Toast.makeText(
                        getApplicationContext(),
                        "This device does not have lens with facing: " + newLensFacing,
                        Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 开始训练
        isEnd = false;
        bindAllCameraUseCases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler3.removeCallbacks(r3);
        mHandler2.removeCallbacks(r2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        mHandler3.removeCallbacks(r3);
        mHandler2.removeCallbacks(r2);
    }

    /**
     * 保存 Bitmap 到本地
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @param bitmap   bitmap 图像文件
     */
    public void saveBitmap(String filePath, String fileName, Bitmap bitmap) {
        File file = new File(filePath);
        //判断指定文件夹的路径是否存在
        if (!file.exists()) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(filePath + "/" + fileName + ".jpg");
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.e("TEST", "图片保存成功");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 合并两张bitmap为一张
     *
     * @param background 底层的图片
     * @param foreground 上层的图片
     * @return Bitmap 合成后的图片
     */
    public static Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
        if (background == null) {
            return null;
        }
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = foreground.getWidth();
        int fgHeight = foreground.getHeight();
        Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(foreground, 0, 0, null);
        canvas.save();
        canvas.restore();
        return newmap;
    }

    /**
     * 缩放bitmap
     *
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return;
        }
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            if (POSE_DETECTION.equals(selectedModel)) {
                PoseDetectorOptionsBase poseDetectorOptions =
                        PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
                boolean shouldShowInFrameLikelihood =
                        PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
                boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
                boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
                boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
                imageProcessor =
                        new PoseDetectorProcessor(
                                this,
                                poseDetectorOptions,
                                shouldShowInFrameLikelihood,
                                visualizeZ,
                                rescaleZ,
                                runClassification,
                                /* isStreamMode = */ true);
            } else {
                throw new IllegalStateException("Invalid model name");
            }
        } catch (Exception e) {
            Logger.e("Can not create image processor: " + selectedModel, e);
            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Logger.e("Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    // // TODO: 2022/3/23 会连续多拍
    public void getScreenCut() {
        /* 实时监测是否有警告点，如果有就截图 */
        mHandler3 = new Handler();
        r3 = new Runnable() {
            @Override
            public void run() {
//                if (!warnPart.isEmpty() && confidence == 1 && !confidenceNotOne) {
//                if (!warnPart.isEmpty() && PoseClassifierProcessor.confidence == 1&& confidenceNotOne) {
                // 如果这里置信度等于1才截图，其他情况的固定部位就不会截图
                if (!warnPart.isEmpty() && confidence == 1 && confidenceNotOne) {
                    confidenceNotOne = false;
                    Random random = new Random();
                    String fileName = nowPoseIndex + "-" + Math.abs(random.nextInt()) + "-" + PoseClassifierProcessor.maxConfidenceClass;
                    Bitmap bitmap2 = com.lindum.mlkit.vision.dev.QuickShot.of(graphicOverlay).getBitmap();
                    if (bitmap2 != null) {
                        imageView = findViewById(R.id.testImage);
                        Bitmap bitmap1 = previewView.getBitmap();
                        // 原图和关键点图片混合
//                        imageView.setImageBitmap(combineBitmap(bitmap1, bitmap2));
                        // 将当前错误的分析结果传递至 TrainingResult 界面
                        if (PoseClassifierProcessor.poseTips != null && !PoseClassifierProcessor.poseTips.equals("")) {
                            TrainingResult.resultImage.add(combineBitmap(bitmap1, bitmap2));
                            TrainingResult.resultName.add(nowPoseName);
                            TrainingResult.resultTips.add(PoseClassifierProcessor.poseTips);
                            TrainingResult.wrongImagePath.add(imageSavePath + fileName + ".jpg");
                        }
                        Logger.e("生成的建议" + PoseClassifierProcessor.poseTips);
                        if (bitmap1 != null) {
                            new Thread("截取图片并保存") {
                                @RequiresApi(api = VERSION_CODES.O)
                                @Override
                                public void run() {
                                    saveBitmap(imageSavePath, fileName, bitmap1);
                                }
                            }.start();
                        }
                    }
                    cleanWarnPart();
                }

                // 当前动作类型
                int nowPoseType = poseEntityList.get(nowPoseIndex).getPoseType();
                // 当前动作过程量上限
                int nowPoseProcessValue = poseEntityList.get(nowPoseIndex).getPoseProcessValue();
                if (nowPoseType == 1 && count == nowPoseProcessValue + 1) {
                    // 动作组数加一
                    countGroupsOfPose++;
                    if (nowPoseIndex == poseEntityList.size() - 1 && !isEnd) {
                        Log.e("total", totalTrainingTime + "");
                        // 跳转到结果界面
                        Intent intent = new Intent(CameraXLivePreviewActivity.this, TrainingResult.class);
                        intent.putExtra("totalTrainingTime", totalTrainingTime);
                        intent.putExtra("groupsOfPose", countGroupsOfPose);
                        // 停止训练
                        isEnd = true;
                        startActivity(intent);
                        finish();
                    } else {
                        PoseEntity poseEntity = poseEntityList.get(nowPoseIndex);
                        if (poseEntity.getPoseType() == 1) {
                            poseEntity.setCountTimes(countTimes);
                        } else {
                            poseEntity.setCountMinutes(countMinutes);
                        }
                        // 动作持续数值清空
                        nowPoseProcessSeconds = 0;
                        countMinutes = 0;
                        countTimes = 0;
                        nextPoseButton.callOnClick();
                    }
                }
                if (nowPoseType == 0 && nowPoseProcessSeconds == nowPoseProcessValue + 1 && nowPoseProcessSeconds != 0) {
                    // 动作组数加一
                    countGroupsOfPose++;
                    // 最后一个动作
                    if (nowPoseIndex == poseEntityList.size() - 1 && !isEnd) {
                        // 跳转到结果界面
                        Intent intent = new Intent(CameraXLivePreviewActivity.this, TrainingResult.class);
                        intent.putExtra("totalTrainingTime", totalTrainingTime);
                        intent.putExtra("groupsOfPose", countGroupsOfPose);
                        // 停止训练
                        isEnd = true;
                        startActivity(intent);
                        finish();
                    } else {
                        PoseEntity poseEntity = poseEntityList.get(nowPoseIndex);
                        countMinutes = nowPoseProcessSeconds / 60f;
                        if (poseEntity.getPoseType() == 1) {
                            poseEntity.setCountTimes(countTimes);
                        } else {
                            poseEntity.setCountMinutes(countMinutes);
                        }
                        // 动作持续数值清空
                        nowPoseProcessSeconds = 0;
                        countMinutes = 0;
                        countTimes = 0;
                        nextPoseButton.callOnClick();
                    }
                }
                mHandler3.postDelayed(this, 800);
            }
        };
        mHandler3.post(r3);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                if (!permission.contains("LOCATION") && !permission.contains("CONTACT") && !permission.contains("AUDIO") && !permission.contains("CALL") && !permission.contains("SMS")) {
                    allNeededPermissions.add(permission);
                }
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Logger.e("Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Logger.e("Permission granted: " + permission);
            return true;
        }
        Logger.e("Permission NOT granted: " + permission);
        return false;
    }

    @Override
    public void onQuickShotSuccess(String path) {

    }

    @Override
    public void onQuickShotFailed(String path, String errorMsg) {

    }

}
