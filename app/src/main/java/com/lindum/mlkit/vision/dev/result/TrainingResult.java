package com.lindum.mlkit.vision.dev.result;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.entity.PoseEntity;
import com.lindum.mlkit.vision.dev.utils.FileUtils;
import com.lindum.mlkit.vision.dev.view.HomePage;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.widget.progress.CircleProgressView;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author MarkGosling
 * @date 2022/1/27 8:25
 */
public class TrainingResult extends AppCompatActivity implements HorizontalProgressView.HorizontalProgressUpdateListener, CircleProgressView.CircleProgressUpdateListener {
    // 展示结果数组
    public static ArrayList<ResultEntity> resultEntityList;
    // 要存储到本地的分析结果
    private ArrayList<JSONResultEntity> jsonResultEntitiesArrayList;
    private final Map<String, Integer> poseResultMap = new HashMap<>();
    public static List<String> resultName = new ArrayList<>();
    public static List<Bitmap> resultImage = new ArrayList<>();
    public static List<String> resultTips = new ArrayList<>();
    public static List<String> wrongImagePath = new ArrayList<>();
    // 运动总时长
    int trainingTotalTimes;
    // 动作组数进度条占比
    int groupsOfPose;
    // 消耗的时间转分秒
    String costTime;
    // 消耗的卡路里
    float calSum = 0;
    // 错误动作数
    int countWrongImage = 0;
    private ListView listView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progressView_circle_main)
    CircleProgressView progressViewCircleMain;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_main)
    TextView progressTextMain;
    // 运动时长
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.hpv_time)
    HorizontalProgressView hpvTime;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_time)
    TextView progressTextTime;
    // 卡路里
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.hpv_cal)
    HorizontalProgressView hpvCal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_cal)
    TextView progressTextCal;
    // 动作组数
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.hpv_group)
    HorizontalProgressView hpvGroup;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_group)
    TextView progressTextGroup;

    // 错误组数
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.hpv_wrong)
    HorizontalProgressView hpvWrong;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_wrong)
    TextView progressTextWrong;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_result);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent.getIntExtra("init", 0) == 0) {
            // 获取运动总时长
            trainingTotalTimes = intent.getIntExtra("totalTrainingTime", 0);
            // 获取动作组数
            groupsOfPose = intent.getIntExtra("groupsOfPose", 0);
            // 记录训练日记
            FileUtils.writeStringToFile(CameraXLivePreviewActivity.timeMarkPoint + " 完成训练", HomePage.appFilePath + "/files/analysisReport/", "recordFile.sys", FileUtils.APPEND_TEXT);
            Logger.e("分析界面初始化数据");
            resultEntityList = new ArrayList<>();
            for (String tips : resultTips) {
                int index = resultTips.indexOf(tips);
                tips = index + tips;
                if (poseResultMap.get(tips) == null) {
                    poseResultMap.put(tips, 1);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        poseResultMap.replace(tips, poseResultMap.get(tips) + 1);
                    }
                }
            }
            List<Map.Entry<String, Integer>> list = new ArrayList<>(poseResultMap.entrySet());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.sort(Comparator.comparing(Map.Entry::getValue));
            }
            Collections.reverse(list);
            Map<String, Integer> map2 = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : list) {
                map2.put(entry.getKey(), entry.getValue());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                map2.forEach((o1, o2) -> {
                    if (o1.length() > 3) {
                        int index = Integer.parseInt(o1.split("，")[0]);
                        String commonMistakes = "";
                        String solveMethod = "";
                        if (HomePage.poseEntityArrayList.get(getPoseIndexByName(resultName.get(index))).getCommonMistakes().length > 1) {
                            commonMistakes = HomePage.poseEntityArrayList.get(getPoseIndexByName(resultName.get(index))).getCommonMistakes()[0];
                            solveMethod = HomePage.poseEntityArrayList.get(getPoseIndexByName(resultName.get(index))).getCommonMistakes()[1].replace("解决：", "");
                        }
                        Bitmap rightPoseImage = null;
                        for (PoseEntity poseEntity : HomePage.poseEntityArrayList) {
                            if (poseEntity.getPoseName() == resultName.get(index)) {
                                int indexOfPoseEntityList = HomePage.poseEntityArrayList.indexOf(poseEntity);
                                rightPoseImage = HomePage.poseRightImageArrayList.get(indexOfPoseEntityList);
                            }
                        }
                        ResultEntity temp = new ResultEntity();
//                    ResultEntity temp = new ResultEntity(resultName.get(index), "出错" + o2 + "次\n" + commonMistakes + "。" + "\nAi建议：" + "\n" + solveMethod + "。" + resultTips.get(index).substring(1) + "\n", resultImage.get(index), rightPoseImage);
                        temp.setPoseName(resultName.get(index));
                        temp.setTips("该错误出错" + o2 + "次\n" + commonMistakes + "。" + "\nAi建议：" + "\n" + solveMethod + "。" + resultTips.get(index).substring(1) + "\n");
                        temp.setWrongPoseImage(resultImage.get(index));
                        temp.setRightPoseImage(rightPoseImage);
                        temp.setWrongPoseImagePath(wrongImagePath.get(index));
                        Logger.e("出错图片路径" + wrongImagePath.get(index));
                        resultEntityList.add(temp);
                    }
                });
                countWrongImage = resultImage.size();
            }
        } else {
            Logger.e("===" + intent.getStringExtra("poseDataPath"));
            String data = FileUtils.readFileToString(intent.getStringExtra("poseDataPath") + "/", "resultData.sys").get(0);
            trainingTotalTimes = Integer.parseInt(data.split(" ")[0]);
            groupsOfPose = Integer.parseInt(data.split(" ")[1]);
            countWrongImage = Integer.parseInt(data.split(" ")[2]);
        }
        // ResultEntity 转 JSONResultEntity
        jsonResultEntitiesArrayList = new ArrayList<>();
        for (ResultEntity resultEntity : resultEntityList) {
            JSONResultEntity jsonResultEntity = new JSONResultEntity();
            jsonResultEntity.setPoseName(resultEntity.getPoseName());
            jsonResultEntity.setTips(resultEntity.getTips());
            jsonResultEntity.setWrongPoseImagePath(resultEntity.getWrongPoseImagePath());
            jsonResultEntitiesArrayList.add(jsonResultEntity);
        }
        // 初始化控件
        listView = findViewById(R.id.resultList);
        // 动作分析列表初始化
        ResultListAdapter resultListAdapter = new ResultListAdapter(this, R.layout.result_list_item, resultEntityList);
        listView.setAdapter(resultListAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            ImageView wrongImageView = view.findViewById(R.id.wrongPoseImage);
            wrongImageView.setOnClickListener(v -> {
                wrongImageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(wrongImageView.getDrawingCache());
                Logger.e("这是第" + l + "张图片");
                Intent intent1 = new Intent(TrainingResult.this, ImageDetails.class);
                ImageDetails.bitmap = bitmap;
                wrongImageView.setDrawingCacheEnabled(false);
                startActivity(intent1);
            });
            ImageView rightImageView = view.findViewById(R.id.rightPoseImage);
            rightImageView.setOnClickListener(v -> {
                rightImageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(rightImageView.getDrawingCache());
                Logger.e("这是第" + l + "张图片");
                Intent intent1 = new Intent(TrainingResult.this, ImageDetails.class);
                ImageDetails.bitmap = bitmap;
                rightImageView.setDrawingCacheEnabled(false);
                startActivity(intent1);
            });
        });

        //  初始化控件
        hpvTime.setProgressViewUpdateListener(this);
        hpvGroup.setProgressViewUpdateListener(this);
        hpvCal.setProgressViewUpdateListener(this);
        hpvWrong.setProgressViewUpdateListener(this);
        progressViewCircleMain.setGraduatedEnabled(true);
        progressViewCircleMain.setProgressViewUpdateListener(this);


        String minute = trainingTotalTimes / 60 + "'";
        costTime = (trainingTotalTimes / 60 == 0 ? "" : minute) + trainingTotalTimes % 60 + "\"";


        progressViewCircleMain.setGraduatedEnabled(true);
        progressViewCircleMain.setProgressViewUpdateListener(this);
        // 初始化进度条
        startProcessBar();
        if (intent.getIntExtra("init", 0) == 0) {
            // 将训练结果保存到本地
            saveTrainingResultToLocal();
        }
    }

    /**
     * 将训练结果保存到本地
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void saveTrainingResultToLocal() {
        FileUtils.createFile(CameraXLivePreviewActivity.imageSavePath + "/resultData.sys", FileUtils.CREATE_FILE);
        // 将评分写入本地文件 时长 动作组数
        FileUtils.writeStringToFile(trainingTotalTimes + " " + groupsOfPose + " " + countWrongImage, CameraXLivePreviewActivity.imageSavePath, "resultData.sys", FileUtils.OVERWRITE_TEXT);
        // 将动作分析结果保存到本地文件
        FileUtils.writeStringToFile(JSON.toJSONString(jsonResultEntitiesArrayList), CameraXLivePreviewActivity.imageSavePath, "analysisReport.json", FileUtils.OVERWRITE_TEXT);
    }

    /**
     * 通过动作名称，查找该动作在动作库的下标
     *
     * @param poseName 动作名称
     * @return 动作下标
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int getPoseIndexByName(String poseName) {
        for (PoseEntity poseEntity : HomePage.poseEntityArrayList) {
            int index = HomePage.poseEntityArrayList.indexOf(poseEntity);
            if (poseEntity.getPoseName().equals(poseName)) {
                return index;
            }
        }
        return -1;
    }


    /**
     * 设置进度条的数值
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startProcessBar() {
        // 动作组数
        int poseGroups = HomePage.poseEntityArrayList.size();
        int groupsRate = (int) (100 * ((float) groupsOfPose / poseGroups));
        // 设置动作组数进度条
        hpvGroup.setEndProgress(groupsRate);
        hpvGroup.startProgressAnimation();

        // 时长
        int timeRate = (int) (100 * ((float) trainingTotalTimes / (poseGroups * (HomePage.poseEntityArrayList.get(0).getPoseProcessValue() + 4))));
        if (timeRate > 100) {
            timeRate = 100;
        }
        hpvTime.setEndProgress(timeRate);
        hpvTime.startProgressAnimation();

        // 计算卡路里

        float totalCalSum = 0;
        int index = 0;
        Logger.e("动作个数" + HomePage.poseEntityArrayList.size());
        for (PoseEntity poseEntity : HomePage.poseEntityArrayList) {
            if (index <= groupsOfPose) {
                calSum += ((float) poseEntity.getPoseProcessValue() / 60) * poseEntity.getGetPoseBurnCalPerMin();
            }
            totalCalSum += ((float) poseEntity.getPoseProcessValue() / 60) * poseEntity.getGetPoseBurnCalPerMin();
            index++;
        }
        Logger.e("卡路里" + calSum);
        int calRate = (int) (100 * (calSum / totalCalSum));
        hpvCal.setEndProgress(calRate);
        hpvCal.startProgressAnimation();

        // 错误动作数
        int wrongRate;
        if (groupsOfPose == 0) {
            wrongRate = (int) (((float) countWrongImage / ((groupsOfPose + 1) * 20)) * 100);
        }else {
            wrongRate = (int) (((float) countWrongImage / ((groupsOfPose) * 20)) * 100);
        }
        Log.i("test_wrong", wrongRate + "");
        if (wrongRate > 100) {
            wrongRate = 100;
        }
        hpvWrong.setEndProgress(wrongRate);
        hpvWrong.startProgressAnimation();
        // 评分
        int scores = 60 + (int) (40 * (1 - ((float) countWrongImage / (20 * poseGroups))));
        progressViewCircleMain.setEndProgress(scores);
        progressViewCircleMain.startProgressAnimation();
    }

    /**
     * 评分环形进度条数值更新
     *
     * @param view     环形进度条 View
     * @param progress 数值
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onCircleProgressUpdate(View view, float progress) {
        int progressInt = (int) progress;
        if (view.getId() == R.id.progressView_circle_main) {
            progressTextMain.setText(progressInt + "");
        }
    }


    /**
     * 卡路里进度条文本
     *
     * @param view     水平进度条布局
     * @param progress 实时更新的进度
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    public void onHorizontalProgressUpdate(View view, float progress) {
        switch (view.getId()) {
            case R.id.hpv_time:
                progressTextTime.setText(costTime);
                break;
            case R.id.hpv_wrong:
                progressTextWrong.setText((int) countWrongImage + " 个");
                break;
            case R.id.hpv_cal:
                progressTextCal.setText((int) calSum + " kcal");
                break;
            case R.id.hpv_group:
                progressTextGroup.setText(groupsOfPose + " 组");
                break;
            default:
                break;
        }
    }

    @Override
    public void onHorizontalProgressFinished(View view) {

    }

    @Override
    public void onCircleProgressFinished(View view) {

    }

    @Override
    public void onHorizontalProgressStart(View view) {

    }

    @Override
    public void onCircleProgressStart(View view) {

    }

}
