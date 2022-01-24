package com.lindum.mlkit.vision.demo.java;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.mlkit.common.MlKitException;
import com.lindum.mlkit.vision.demo.CameraXViewModel;
import com.lindum.mlkit.vision.demo.GraphicOverlay;
import com.lindum.mlkit.vision.demo.R;
import com.lindum.mlkit.vision.demo.VisionImageProcessor;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseEntity;
import com.lindum.mlkit.vision.demo.preference.PreferenceUtils;
import com.lindum.mlkit.vision.demo.preference.SettingsActivity;

import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Live preview demo app for ML Kit APIs using CameraX.
 */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXLivePreviewActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "CameraXLivePreview";
    private static final int PERMISSION_REQUESTS = 1;

    private static final String POSE_DETECTION = "姿态检测";
    private static final String OBJECT_DETECTION = "Object Detection";


    private static final String STATE_SELECTED_MODEL = "selected_model";
    private static float count = 0;
    private static List<int[]> warnPart;
    private static List<Integer> tempWarnPart;
    private static boolean pose1;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    boolean isPermissionRequested = false;
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
        // 初始化变量
        warnPart = new ArrayList<>();
        tempWarnPart = new ArrayList<>();
        poseEntityList = new ArrayList<>();
        pose1 = false;

        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        // 设置布局
        setContentView(R.layout.activity_vision_camerax_live_preview);

        //适配安卓11及以下版本的存储权限请求
//        if (Build.VERSION.SDK_INT > 29) {
//            requestExternalStorage(this);
//            // SD 卡根目录
//            String sdPath = Environment.getExternalStorageDirectory().getPath();
//            // 姿态数据工具类
//            PoseDataUtils.writeJSON2Loacl(poseEntityList, sdPath + "/AiTrainer/poseData.json");
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

        ArrayList<PoseEntity> poseEntityArrayList = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) JSON.parse(jsonText);
        poseEntityArrayList = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<ArrayList<PoseEntity>>() {
        });
        Log.e("entity", poseEntityArrayList.toString());
        poseEntityList = poseEntityArrayList;

        nowPoseName = poseEntityList.get(nowPoseIndex).getPoseName();


        Handler mHandler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
//                if (count > 5) {
////                    Log.e("Tips", "太差劲了");
//                    Toast.makeText(CameraXLivePreviewActivity.this, "非常棒", Toast.LENGTH_SHORT).show();
//                    count = 0;
//                }
                Log.e("count", (int) count + "");
                //do something
                //每隔1s循环执行run方法
                mHandler.postDelayed(this, 800);
            }
        };


        mHandler.postDelayed(r, 100);//延时100毫秒
        Handler mHandler2 = new Handler();
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                tempWarnPart.clear();
                warnPart.clear();

                //do something
                //每隔1s循环执行run方法
                mHandler2.postDelayed(this, 500);
            }
        };

        mHandler2.post(r2);
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Spinner spinner = findViewById(R.id.spinner);
        List<String> options = new ArrayList<>();
        options.add(POSE_DETECTION);
//        options.add(OBJECT_DETECTION);
//        options.add(OBJECT_DETECTION_CUSTOM);
//        options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
//        options.add(FACE_DETECTION);
//        options.add(BARCODE_SCANNING);
//        options.add(IMAGE_LABELING);
//        options.add(IMAGE_LABELING_CUSTOM);
//        options.add(CUSTOM_AUTOML_LABELING);
//        options.add(SELFIE_SEGMENTATION);
//        options.add(TEXT_RECOGNITION_LATIN);
//        options.add(TEXT_RECOGNITION_CHINESE);
//        options.add(TEXT_RECOGNITION_DEVANAGARI);
//        options.add(TEXT_RECOGNITION_JAPANESE);
//        options.add(TEXT_RECOGNITION_KOREAN);

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
                        provider -> {
                            cameraProvider = provider;
                            if (allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE,
                            SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW);
                    startActivity(intent);
                });

        // 初始化控件
        LinearLayout nextPoseButton = findViewById(R.id.nextButton);
        LinearLayout prePoseButton = findViewById(R.id.preButton);
        Button prePoseImageButton = findViewById(R.id.prePoseImage);
        Button nextPoseImageButton = findViewById(R.id.nextPoseImage);
        nextPoseImageButton.setOnClickListener(view -> {
            if (nowPoseIndex + 1 < poseEntityList.size()) {
                nowPoseIndex++;
                setNowPoseName(poseEntityList.get(nowPoseIndex).getPoseName());
                count = 0;
            }
        });
        prePoseImageButton.setOnClickListener(view -> {
            if (nowPoseIndex - 1 >= 0) {
                nowPoseIndex--;
                setNowPoseName(poseEntityList.get(nowPoseIndex).getPoseName());
                count = 0;
            }
        });
        nextPoseButton.setOnClickListener(view -> {
            if (nowPoseIndex + 1 < poseEntityList.size()) {
                nowPoseIndex++;
                setNowPoseName(poseEntityList.get(nowPoseIndex).getPoseName());
                count = 0;
            }
        });
        prePoseButton.setOnClickListener(view -> {
            if (nowPoseIndex - 1 >= 0) {
                nowPoseIndex--;
                setNowPoseName(poseEntityList.get(nowPoseIndex).getPoseName());
                count = 0;
            }
        });
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTED_MODEL, selectedModel);
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        bindAnalysisUseCase();
    }

    public static void addWarnPart(int partIndex) {
        StringBuilder awd = new StringBuilder();
        for (int[] tmp :
                warnPart) {
            awd.append(Arrays.toString(tmp)).append(",");
        }
//        Log.e("sys", String.valueOf(awd));
        Log.e("sys", "添加" + partIndex);
        Log.e("sys", tempWarnPart.toString());
        if (tempWarnPart.contains(partIndex)) {
            Log.e("sys", "添加过了");
        } else {
//            switch (partIndex){
//                case 1:
//                case 2:
//                    break;
//                case 3:
//
//                    break;
//
//            }
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

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }

            if (!permissionsList.isEmpty()) {
                String[] strings = new String[permissionsList.size()];
                requestPermissions(permissionsList.toArray(strings), 0);
            }
        }
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
}
