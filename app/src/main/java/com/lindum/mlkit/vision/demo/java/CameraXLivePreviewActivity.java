package com.lindum.mlkit.vision.demo.java;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
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

import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.lindum.mlkit.vision.demo.CameraXViewModel;
import com.lindum.mlkit.vision.demo.GraphicOverlay;
import com.lindum.mlkit.vision.demo.QuickShot;
import com.lindum.mlkit.vision.demo.R;
import com.lindum.mlkit.vision.demo.VisionImageProcessor;
import com.lindum.mlkit.vision.demo.java.posedetector.DrawLandmarkTools;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseAngleDetector;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseDataUtils;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseEntity;
import com.lindum.mlkit.vision.demo.java.posedetector.PosePart;
import com.lindum.mlkit.vision.demo.java.posedetector.ProcessImage;
import com.lindum.mlkit.vision.demo.preference.PreferenceUtils;
import com.lindum.mlkit.vision.demo.preference.SettingsActivity;

import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        CompoundButton.OnCheckedChangeListener, QuickShot.QuickShotListener {
    private static final String TAG = "CameraXLivePreview";
    private static final int PERMISSION_REQUESTS = 1;

    private static final String POSE_DETECTION = "姿态检测";
    private static final String OBJECT_DETECTION = "Object Detection";

    private static final String STATE_SELECTED_MODEL = "selected_model";
    private static float count = 0;
    private static List<int[]> warnPart;
    private static List<Integer> warnAngle;
    private static boolean pose1;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private String selectedModel = OBJECT_DETECTION;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;

    public static List<PoseEntity> poseEntityList;
    public static String nowPoseName = "AiTrainer";
    public static int nowPoseIndex = 0;
    public static int countdownSeconds = 10;
    // 训练是否结束
    public static boolean isEnd = false;
    // 给截图延时 调整检测截图灵敏度
    private int delayScreenCapture = 0;


    /**
     * 左下角信息
     * 动作名称
     */
    private TextView poseName;
    /**
     * 第几个动作
     */
    private TextView poseIndexText;
    // 当前动作进度
    private TextView poseProcessInfo;
    // 倒计时秒数
    private TextView countdownSecondsTextView;
    // 现在动作进行的秒数
    private int nowPoseProcessSeconds = 0;
    // 训练总时间
    private int totalTrainingTime = 0;
    // 几组动作
    private int countGroupsOfPose = 0;


    Bitmap resultBitmap;
    // 图片处理警告点
    List<int[]> imageProcessWarnPart;
    String tempTips = "";

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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            Toast.makeText(
                    getApplicationContext(),
                    "CameraX is only supported on SDK version >=21. Current SDK version is "
                            + VERSION.SDK_INT,
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        // 设置布局
        setContentView(R.layout.activity_vision_camerax_live_preview);
        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 初始化变量
        warnPart = new ArrayList<>();
        poseEntityList = new ArrayList<>();
        warnAngle = new ArrayList<>();
        pose1 = false;
        poseName = findViewById(R.id.poseName);
        poseIndexText = findViewById(R.id.nowPoseIndexText);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        poseProcessInfo = findViewById(R.id.poseProcessInfo);
        countdownSecondsTextView = findViewById(R.id.countdownSecondsText);
        //适配安卓11及以下版本的存储权限请求
//        if (Build.VERSION.SDK_INT > 29) {
//        requestExternalStorage(this);
//            // SD 卡根目录
        String sdPath = Environment.getExternalStorageDirectory().getPath();
//            // 姿态数据工具类
        PoseDataUtils.writeJSON2Loacl(poseEntityList, sdPath + "/AiTrainer/poseData.json");
//            isPermissionRequested = true;
//        }


        // 使主线程可以进行简单的网络请求
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Document document = null;
        try {
            document = Jsoup.connect("http://lindum.top/poseData.html").get();
            Log.e("html", document.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        Element element = document.body();
        String jsonText = element.ownText();

        ArrayList<PoseEntity> poseEntityArrayList;
        JSONArray jsonArray = (JSONArray) JSON.parse(jsonText);
        poseEntityArrayList = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<ArrayList<PoseEntity>>() {
        });
        Log.e("entity", poseEntityArrayList.toString());
        poseEntityList = poseEntityArrayList;
        // 设置动作持续变量类型
        for (PoseEntity poseEntity : poseEntityList) {
            // 计数类型
            poseEntity.setPoseType(0);
            poseEntity.setPoseProcessValue(100);
        }

        nowPoseName = poseEntityList.get(nowPoseIndex).getPoseName();

//        String sdPath1 = Environment.getExternalStorageDirectory().getPath();
//            // 姿态数据工具类
//        PoseDataUtils.writeJSON2Loacl(poseEntityList, sdPath1 + "/AiTrainer/poseData123.json");
//        Handler mHandler = new Handler();
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
////                if (count > 5) {
//////                    Log.e("Tips", "太差劲了");
////                    Toast.makeText(CameraXLivePreviewActivity.this, "非常棒", Toast.LENGTH_SHORT).show();
////                    count = 0;
////                }
//                Log.e("count", (int) count + "");
//                //do something
//                //每隔1s循环执行run方法
//                mHandler.postDelayed(this, 800);
//            }
//        };
//
//
//        mHandler.postDelayed(r, 100);//延时100毫秒


        Button button = findViewById(R.id.analysisResult);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CameraXLivePreviewActivity.this, TrainingResult.class);
                startActivity(intent);
            }
        });

        // 定时清空警告点、倒数秒数刷新、计算总运动时长
        Handler mHandler2 = new Handler();
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                warnPart.clear();
                warnAngle.clear();
                updatePoseInfo();
                if (countdownSeconds >= 0) {
                    countdownSecondsTextView.setVisibility(View.VISIBLE);
                    countdownSeconds--;
                }
                if (countdownSeconds == -1) {
                    countdownSecondsTextView.setVisibility(View.INVISIBLE);
                }
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



        // 初始化控件
        LinearLayout nextPoseButton = findViewById(R.id.nextButton);
        LinearLayout prePoseButton = findViewById(R.id.preButton);
        Button prePoseImageButton = findViewById(R.id.prePoseImage);
        Button nextPoseImageButton = findViewById(R.id.nextPoseImage);
        LinearLayout explainButton = findViewById(R.id.explain);
        explainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        /* 实时监测是否有警告点，如果有就截图 */
        Handler mHandler3 = new Handler();
        Runnable r3 = new Runnable() {
            @Override
            public void run() {
//                Log.e("TEST", delayScreenCapture + "");
                if (!warnPart.isEmpty() && delayScreenCapture == 0) {
                    Random random = new Random();
                    String fileName = nowPoseIndex + "-" + Math.abs(random.nextInt());
                    Bitmap bitmap2 = com.lindum.mlkit.vision.demo.QuickShot.of(graphicOverlay).getBitmap();
                    Log.e("TEST", "获取bitmap成功");
                    if (bitmap2 != null) {
                        ImageView imageView = findViewById(R.id.testImage);
                        Bitmap bitmap1 = previewView.getBitmap();
                        assert bitmap1 != null;
//                        imageView.setImageBitmap(combineBitmap(bitmap1, bitmap2));
                        new Thread("截图图片并保存") {
                            @Override
                            public void run() {
                                String filePath = Environment.getExternalStorageDirectory().getPath() + "/AiTrainer";
                                saveBitmap(filePath, fileName, bitmap1);
                                delayScreenCapture = 10;
                            }
                        }.start();
                    }
                    cleanWarnPart();
                }
                if (delayScreenCapture > 0) {
                    delayScreenCapture--;
                }
                // 当前动作类型
                int nowPoseType = poseEntityList.get(nowPoseIndex).getPoseType();
                // 当前动作过程量上限
                int nowPoseProcessValue = poseEntityList.get(nowPoseIndex).getPoseProcessValue();
                if (nowPoseType == 1 && count == nowPoseProcessValue + 1) {
                    // 动作组数加一
                    countGroupsOfPose++;
                    if (nowPoseIndex == poseEntityList.size() - 1 && !isEnd) {
                        isEnd = true;
                        // 跳转到结果界面
                        Intent intent = new Intent(CameraXLivePreviewActivity.this, TrainingResult.class);
                        intent.putExtra("totalTrainingTime", totalTrainingTime);
                        intent.putExtra("groupsOfPose", countGroupsOfPose);
                        startActivity(intent);
                        finish();
                    } else {

                        // 动作持续数值清空
                        nowPoseProcessSeconds = 0;
                        nextPoseButton.callOnClick();
                    }
                }
                if (nowPoseType == 0 && nowPoseProcessSeconds == nowPoseProcessValue + 1 && nowPoseProcessSeconds != 0) {
                    // 动作组数加一
                    countGroupsOfPose++;
                    // 最后一个动作
                    if (nowPoseIndex == poseEntityList.size() - 1 && !isEnd) {
                        isEnd = true;
                        // 跳转到结果界面
                        Intent intent = new Intent(CameraXLivePreviewActivity.this, TrainingResult.class);
                        intent.putExtra("totalTrainingTime", totalTrainingTime);
                        intent.putExtra("groupsOfPose", countGroupsOfPose);
                        startActivity(intent);
                        finish();
                    } else {
                        // 动作持续数值清空
                        nowPoseProcessSeconds = 0;
                        nextPoseButton.callOnClick();
                    }
                }
                mHandler3.postDelayed(this, 100);
            }
        };

        ProcessImage processImage = new ProcessImage(findViewById(R.id.testImage));
        processImage.processImage();
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        // 截图
        mHandler3.post(r3);

        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
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

    public static void cleanWarnPart() {
        warnAngle.clear();
        warnPart.clear();
    }

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
            poseProcessInfo.setText((int) nowPoseProcessSeconds + "\"");
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
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        bindAnalysisUseCase();
    }


    public static void addWarnPart(int partIndex, int angle) {
        // 添加报错的角度
        warnAngle.add(angle);
        // 添加报错的部位
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
//        Log.e("sys","添加了"+partIndex);

    }

    public void requestExternalStorage(Context context) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        }
        assert intent != null;
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
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
                Log.d(TAG, "Set facing to " + newLensFacing);
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
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }


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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, saveImgOut);
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
     * @param background
     * @param foreground
     * @return Bitmap
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
            switch (selectedModel) {
                case POSE_DETECTION:
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
                    break;
                default:
                    throw new IllegalStateException("Invalid model name");
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedModel, e);
//            Toast.makeText(
//                    getApplicationContext(),
//                    "Can not create image processor: " + e.getLocalizedMessage(),
//                    Toast.LENGTH_LONG)
//                    .show();
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
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
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
                allNeededPermissions.add(permission);
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
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    public void onQuickShotSuccess(String path) {

    }

    @Override
    public void onQuickShotFailed(String path, String errorMsg) {

    }
}
