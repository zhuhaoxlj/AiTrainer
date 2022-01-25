/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lindum.mlkit.vision.demo.java.posedetector.classification;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

import com.google.common.base.Preconditions;
import com.lindum.mlkit.vision.demo.java.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseAngleDetector;
import com.lindum.mlkit.vision.demo.java.posedetector.PoseEntity;
import com.lindum.mlkit.vision.demo.java.posedetector.PosePart;
import com.google.mlkit.vision.pose.Pose;

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
    //  private static final String POSE_SAMPLES_FILE = "pose/fitness_pose_samples.csv";
    private static final String POSE_SAMPLES_FILE = "pose/squat_csvs_out_basic.csv";

    // Specify classes for which we want rep counting.
    // These are the labels in the given {@code POSE_SAMPLES_FILE}. You can set your own class labels
    // for your pose samples.
    private static final String PUSHUPS_CLASS = "pushups_down";
    private static final String SQUATS_CLASS = "down";
    private static final String[] POSE_CLASSES = {
            PUSHUPS_CLASS, SQUATS_CLASS
    };

    private final boolean isStreamMode;

    private EMASmoothing emaSmoothing;
    private List<RepetitionCounter> repCounters;
    private PoseClassifier poseClassifier;
    private String lastRepResult;

    @WorkerThread
    public PoseClassifierProcessor(Context context, boolean isStreamMode) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        this.isStreamMode = isStreamMode;
        if (isStreamMode) {
            emaSmoothing = new EMASmoothing();
            repCounters = new ArrayList<>();
            lastRepResult = "";
        }
        loadPoseSamples(context);
    }

    private void loadPoseSamples(Context context) {
        List<PoseSample> poseSamples = new ArrayList<>();
        try {
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

        if (pose != null) {

            PoseAngleDetector poseAngleDetector = new PoseAngleDetector(pose);
            // 三维
            {
//                String angleText1 = "左膝盖三维夹角" + poseAngleDetector.getLeftKnee3DAngle() + "°";
//                String angleText2 = "右膝盖三维夹角" + poseAngleDetector.getRightKnee3DAngle() + "°";
//                String angleText3 = "左胳膊三维夹角" + poseAngleDetector.getLeftArm3DAngle() + "°";
//                String angleText4 = "右胳膊三维夹角" + poseAngleDetector.getRightArm3DAngle() + "°";
//                String angleText5 = "左躯干三维夹角" + poseAngleDetector.getLeftTrunk3DAngle() + "°";
//                String angleText6 = "右躯干三维夹角" + poseAngleDetector.getRightTrunk3DAngle() + "°";
//                result.add(angleText1);
//                result.add(angleText2);
//                result.add(angleText3);
//                result.add(angleText4);
//                result.add(angleText5);
//                result.add(angleText6);
            }
//            // 二维
//            {
//                String angleText1 = "左膝盖二维夹角" + poseAngleDetector.getLeftKneeAngle() + "°";
//                String angleText2 = "右膝盖二维夹角" + poseAngleDetector.getRightKneeAngle() + "°";
//                String angleText3 = "左胳膊二维夹角" + poseAngleDetector.getLeftArmAngle() + "°";
//                String angleText4 = "右胳膊二维夹角" + poseAngleDetector.getRightArmAngle() + "°";
//                String angleText5 = "左躯干二维夹角" + poseAngleDetector.getLeftTrunkAngle() + "°";
//                String angleText6 = "右躯干二维夹角" + poseAngleDetector.getRightTrunkAngle() + "°";
//                result.add(angleText1);
//                result.add(angleText2);
//                result.add(angleText3);
//                result.add(angleText4);
//                result.add(angleText5);
                result.add(CameraXLivePreviewActivity.getCount()+"个");
//            }
            // 二维和三维同时显示
            {
//                String angleText1 = "左膝3D" + poseAngleDetector.getLeftKnee3DAngle() + "°" + "2D" + poseAngleDetector.getLeftKneeAngle() + "°";
//                String angleText2 = "右膝3D" + poseAngleDetector.getRightKnee3DAngle() + "°" + "2D" + poseAngleDetector.getRightKneeAngle() + "°";
//                String angleText3 = "左胳3D" + poseAngleDetector.getLeftArm3DAngle() + "°" + "2D" + poseAngleDetector.getLeftArmAngle() + "°";
//                String angleText4 = "右胳3D" + poseAngleDetector.getRightArm3DAngle() + "°" + "2D" + poseAngleDetector.getRightArmAngle() + "°";
//                String angleText5 = "左躯3D" + poseAngleDetector.getLeftTrunk3DAngle() + "°" + "2D" + poseAngleDetector.getLeftTrunkAngle() + "°";
//                String angleText6 = "右躯3D" + poseAngleDetector.getRightTrunk3DAngle() + "°" + "2D" + poseAngleDetector.getRightTrunkAngle() + "°";
//                int angle = poseAngleDetector.getLeftKneeAngle();
//                int angle2 = poseAngleDetector.getRightKneeAngle();
//                if (angle > 170 || angle2 > 170) {
//                    Log.e("Tips", "左" + angle + " " + "右" + angle2);
//                    CameraXLivePreviewActivity.addCount();
//                }

                /*
                  !!!!!!!! 这个类可以理解每次截取一张图片进行判断 ！！！！！！！
                  !!!!!!!! 这个类可以理解每次截取一张图片进行判断 ！！！！！！！
                  !!!!!!!! 这个类可以理解每次截取一张图片进行判断 ！！！！！！！
                 */


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
                Log.e("测试", fixedPartList.size() + "");
                if (mode == 2) {
                    boolean dynamicPartJudge1 = true;
                    boolean dynamicPartJudge2 = true;
                    boolean fixedPartJudge = true;
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
                        if (!(nowFrameAngle > part.getFixedRange()[0] && nowFrameAngle < part.getFixedRange()[1])) {
                            // 固定部位不满足标准的时候报红，那么动态部位什么情况下报红呢？ todo 我觉得是超过这个动态的总范围就报红
                            CameraXLivePreviewActivity.addWarnPart(partIndex);
                            fixedPartJudge = false;
                        }
                    }
                    // 判断是否全部满足动态部位第一个标准
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
                        // 判断当前的角度是否满足第一个标准
                        if (!(nowFrameAngle > part.getFirstStandardRange()[0] && nowFrameAngle < part.getFirstStandardRange()[1])) {
                            dynamicPartJudge1 = false;
                        }
                        if (dynamicPartJudge1 && !CameraXLivePreviewActivity.isPose1() && fixedPartJudge) {
                            CameraXLivePreviewActivity.addCount();
                            CameraXLivePreviewActivity.setPose1(true);
                        }
                        // 判断当前的角度是否满足第二个标准
                        if (!(nowFrameAngle > part.getSecondStandardRange()[0] && nowFrameAngle < part.getSecondStandardRange()[1])) {
                            dynamicPartJudge2 = false;
                        }
                        if (dynamicPartJudge2 && CameraXLivePreviewActivity.isPose1() && fixedPartJudge) {
                            CameraXLivePreviewActivity.addCount();
                            CameraXLivePreviewActivity.setPose1(false);
                        }
                    }
                }

            }


        }
        return result;
    }
}
