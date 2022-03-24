package com.lindum.mlkit.vision.dev.posedetector.classification;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

import com.google.common.base.Preconditions;
import com.google.mlkit.vision.pose.Pose;
import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.entity.PoseEntity;
import com.lindum.mlkit.vision.dev.posedetector.PoseAngleDetector;
import com.lindum.mlkit.vision.dev.posedetector.PosePart;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */

public class PoseClassifierProcessor {
    private static final String TAG = "PoseClassifierProcessor";
    //    private static final String POSE_SAMPLES_FILE = "pose/squat_csvs_out_basic.csv";
//    private static final String POSE_SAMPLES_FILE = "pose/juanfumoxi.csv";
    // // TODO: 2022/3/8 可以把这个设为静态成员变量，不同动作切换不同的文件生成不同的分类器
    private static String POSE_SAMPLES_FILE = "pose/fitness_pose_samples.csv";
    //    private static final String POSE_SAMPLES_FILE = "pose/tunqiao.csv";
    volatile public static String maxConfidenceClass;
    // Specify classes for which we want rep counting.
    // These are the labels in the given {@code POSE_SAMPLES_FILE}. You can set your own class labels
    // for your pose samples.
    // 置信度
    volatile public static float confidence;
    private static final String PUSHUPS_CLASS = "pushups_down";
    private static final String SQUATS_CLASS = "squats_down";
    private static final String[] POSE_CLASSES = {
            PUSHUPS_CLASS, SQUATS_CLASS
    };
    private String[] poseClassifyFileName = {"fitness_pose_samples", "", "", "fitness_pose_samples", "tunqiao", "xixilijuanfu", "juanfumoxi", "guizifuwocheng", ""};
    private final boolean isStreamMode;

    private EMASmoothing emaSmoothing;
    private List<RepetitionCounter> repCounters;
    private PoseClassifier poseClassifier;
    private String lastRepResult;
    volatile private boolean addWarnPartFlag = true;
    Context context;
    static volatile public String poseTips;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @WorkerThread
    public PoseClassifierProcessor(Context context, boolean isStreamMode) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        this.isStreamMode = isStreamMode;
        if (isStreamMode) {
            emaSmoothing = new EMASmoothing();
            repCounters = new ArrayList<>();
            lastRepResult = "";
        }
        this.context = context;
        loadPoseSamples(context);
    }

    // // TODO: 2022/3/8
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadPoseSamples(Context context) {
        List<PoseSample> poseSamples = new ArrayList<>();
        try {
            if (!poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex].equals("")) {
                POSE_SAMPLES_FILE = "pose/" + poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex] + ".csv";
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(POSE_SAMPLES_FILE)));
            String csvLine = reader.readLine();
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                PoseSample poseSample = PoseSample.getPoseSample(csvLine, ",");
                if (poseSample != null) {
                    poseSamples.add(poseSample);
                }
                csvLine = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error when loading pose samples.\n" + e);
        }
        poseClassifier = new PoseClassifier(poseSamples);
        if (isStreamMode) {
            for (String className : POSE_CLASSES) {
                repCounters.add(new RepetitionCounter(className));
            }
        }
    }

    /**
     * Given a new {@link Pose} input, returns a list of formatted {@link String}s with Pose
     * classification results.
     *
     * <p>Currently it returns up to 2 strings as following:
     * 0: PoseClass : X reps
     * 1: PoseClass : [0.0-1.0] confidence
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @WorkerThread
    public List<String> getPoseResult(Pose pose) {
        if (!POSE_SAMPLES_FILE.equals("pose/" + poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex] + ".csv") && !poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex].equals("")) {
            loadPoseSamples(context);
        }


        poseTips = "";
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        List<String> result = new ArrayList<>();
        ClassificationResult classification = poseClassifier.classify(pose);

        // Update {@link RepetitionCounter}s if {@code isStreamMode}.
        if (isStreamMode) {
            // Feed pose to smoothing even if no pose found.
            classification = emaSmoothing.getSmoothedResult(classification);

            // Return early without updating repCounter if no pose found.
            if (pose.getAllPoseLandmarks().isEmpty()) {
                result.add(lastRepResult);
                return result;
            }

            for (RepetitionCounter repCounter : repCounters) {
                int repsBefore = repCounter.getNumRepeats();
                int repsAfter = repCounter.addClassificationResult(classification);
                if (repsAfter > repsBefore) {
                    // Play a fun beep when rep counter updates.
//                    ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
//                    tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                    lastRepResult = String.format(
                            Locale.US, "%s : %d reps", repCounter.getClassName(), repsAfter);
                    break;
                }
            }
//            result.add(lastRepResult);
        }
        // Add maxConfidence class of current frame to result if pose is found.
        assert pose != null;
        if (!pose.getAllPoseLandmarks().isEmpty()) {
            maxConfidenceClass = classification.getMaxConfidenceClass();
            String maxConfidenceClassResult = String.format(
                    Locale.US,
                    "%s : %.2f confidence",
                    maxConfidenceClass,
                    classification.getClassConfidence(maxConfidenceClass)
                            / poseClassifier.confidenceRange());
            confidence = classification.getClassConfidence(maxConfidenceClass) / poseClassifier.confidenceRange();
//            Logger.e("置信度" + confidence);
            CameraXLivePreviewActivity.confidence = confidence;

            if (!poseClassifyFileName[CameraXLivePreviewActivity.nowPoseIndex].equals("")) {
                result.add(maxConfidenceClassResult);
            }
            result.add("\n");
            result.add("\n");
            result.add("\n");
        }

        // 人体关键的监测

        PoseAngleDetector poseAngleDetector = new PoseAngleDetector(pose);
        // 二维和三维同时显示

        String angleText1 = "左膝3D" + poseAngleDetector.getLeftKnee3DAngle() + "°" + "2D" + poseAngleDetector.getLeftKneeAngle() + "°";
        String angleText2 = "右膝3D" + poseAngleDetector.getRightKnee3DAngle() + "°" + "2D" + poseAngleDetector.getRightKneeAngle() + "°";
        String angleText3 = "左胳3D" + poseAngleDetector.getLeftArm3DAngle() + "°" + "2D" + poseAngleDetector.getLeftArmAngle() + "°";
        String angleText4 = "右胳3D" + poseAngleDetector.getRightArm3DAngle() + "°" + "2D" + poseAngleDetector.getRightArmAngle() + "°";
        String angleText5 = "左躯3D" + poseAngleDetector.getLeftTrunk3DAngle() + "°" + "2D" + poseAngleDetector.getLeftTrunkAngle() + "°";
        String angleText6 = "右躯3D" + poseAngleDetector.getRightTrunk3DAngle() + "°" + "2D" + poseAngleDetector.getRightTrunkAngle() + "°";
        String angleText7 = "左肩3D" + poseAngleDetector.getLeftShoulder3DAngle() + "°" + "2D" + poseAngleDetector.getLeftShoulderAngle() + "°";
        String angleText8 = "右肩3D" + poseAngleDetector.getRightShoulder3DAngle() + "°" + "2D" + poseAngleDetector.getRightShoulderAngle() + "°";
        result.add(angleText1);
        result.add(angleText2);
        result.add(angleText3);
        result.add(angleText4);
        result.add(angleText5);
        result.add(angleText6);
        result.add(angleText7);
        result.add(angleText8);

            /*
              !!!!!!!! 这个类可以理解每次截取一张图片进行判断 ！！！！！！！
              !!!!!!!! 这个类可以理解每次截取一张图片进行判断 ！！！！！！！
              !!!!!!!! 这个类可以理解每次截取一张图片进行判断 ！！！！！！！
             */
//                Log.e("TEST", CameraXLivePreviewActivity.countdownSeconds + "S");
        // 倒计时结束，开始训练
        if (CameraXLivePreviewActivity.countdownSeconds == -1) {
            // 动作模式： 普通模式 1 严格模式 2
            // 普通模式 待定 todo
            // 如果严格模式，则要求每个部位都要精准才算一个有效动作
            int mode = 2;
            // 获取监测的动作实体
            PoseEntity poseEntity = CameraXLivePreviewActivity.getPoseEntityList().get(CameraXLivePreviewActivity.nowPoseIndex);
            // 获取监测部位列表
            List<PosePart> monitoringList = poseEntity.getMonitoringPosePartList();
            // 固定部位列表
            List<PosePart> fixedPartList = new ArrayList<>();
            // 动态部位列表
            List<PosePart> dynamicPart = new ArrayList<>();
            // 将固定部位和动态部位分别筛选出来
            for (PosePart part : monitoringList) {
                if (part.getPoseType() == 0)
                    fixedPartList.add(part);
                if (part.getPoseType() == 1)
                    dynamicPart.add(part);
            }
//                    Log.e("测试", fixedPartList.size() + "");
            // 判断是否全部满足固定部位标准
            for (PosePart part : fixedPartList) {
                int partIndex = part.getPoseIndex();
                // 当前帧的角度
                int nowFrameAngle;
                // 自定义角度的情况
                if (partIndex == -1) {
                    nowFrameAngle = poseAngleDetector.getAnyAngleBy3Point(part.getThreePointIndex());
                }
                // 常规角度的情况
                else {
                    nowFrameAngle = poseAngleDetector.getPoseAngleFromPartIndex(partIndex);
                }
                if (!(nowFrameAngle >= part.getFixedRange()[0] && nowFrameAngle <= part.getFixedRange()[1])) {
                    CameraXLivePreviewActivity.addWarnPart(partIndex);
                }
                if (partIndex != -1) {
                    if (nowFrameAngle < part.getFixedRange()[0]) {
                        poseTips += "，" + getPartName(partIndex) + "角度要大一点";
                    }
                    if (nowFrameAngle > part.getFixedRange()[1]) {
                        poseTips += "，" + getPartName(partIndex) + "角度小一点";
                    }
                }
            }
            if (confidence != 1) {
                CameraXLivePreviewActivity.confidenceNotOne = true;
                addWarnPartFlag = true;
            }
            // 判断是否全部满足动态部位第一个标准
            // 能够分清动作是up or down 并且置信度等于 1
            if (maxConfidenceClass != null && confidence == 1) {

                for (PosePart part : dynamicPart) {
                    int partIndex = part.getPoseIndex();
                    // 当前帧的角度
                    int nowFrameAngle;
                    // 自定义角度的情况
                    if (partIndex == -1) {
                        nowFrameAngle = poseAngleDetector.getAnyAngleBy3Point(part.getThreePointIndex());
                    }
                    // 常规部位角度情况
                    else {
                        nowFrameAngle = poseAngleDetector.getPoseAngleFromPartIndex(partIndex);
                    }
                    if (maxConfidenceClass.contains("down")) {
                        // 判断当前的角度是否满足第二个标准
                        if (!(nowFrameAngle >= part.getSecondStandardRange()[0] && nowFrameAngle <= part.getSecondStandardRange()[1]) && addWarnPartFlag) {
                            CameraXLivePreviewActivity.addWarnPart(partIndex);
                            Logger.e("当前置信度"+confidence);
                        }
                        if (nowFrameAngle < part.getSecondStandardRange()[0]) {
                            poseTips += "，" + getPartName(partIndex) + "角度要大一点";
                        }
                        if (nowFrameAngle > part.getSecondStandardRange()[1]) {
                            poseTips += "，" + getPartName(partIndex) + "角度小一点";
                        }
                        if (!addWarnPartFlag && nowFrameAngle < part.getSecondStandardRange()[0]) {
                            CameraXLivePreviewActivity.addWarnPart(partIndex);
                            Logger.e("当前置信度"+confidence);
                        }
                    }
                    if (maxConfidenceClass.contains("up")) {
                        // 判断当前的角度是否满足第一个标准
                        if (!(nowFrameAngle >= part.getFirstStandardRange()[0] && nowFrameAngle <= part.getFirstStandardRange()[1])&&addWarnPartFlag) {
                            CameraXLivePreviewActivity.addWarnPart(partIndex);
                        }
                        if (nowFrameAngle < part.getFirstStandardRange()[0]) {
                            poseTips += "，" + getPartName(partIndex) + "角度要大一点";
                        }
                        if (nowFrameAngle > part.getFirstStandardRange()[1]) {
                            poseTips += "，" + getPartName(partIndex) + "角度小一点";
                        }
                    }
                }
                addWarnPartFlag = false;
            }
        }

        return result;
    }

    /**
     * 获取部位名称
     *
     * @param index 身体部位索引
     * @return 身体部位名称
     */
    // // TODO: 2022/3/23 下标为-1的比如跪姿俯卧撑、卷腹摸膝会超过数组范围
    public String getPartName(int index) {
        String[] partNames = new String[]{"", "右手腕", "右胳膊肘", "右手臂与肩膀", "左手臂与肩膀", "左胳膊肘", "左手腕", "右腿与身体", "左腿与身体", "右膝盖", "左膝盖", "右脚腕", "左脚腕"};
        return partNames[index];
    }
}
