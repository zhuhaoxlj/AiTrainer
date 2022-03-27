package com.lindum.mlkit.vision.dev.result;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private ListView listView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progressView_circle_main)
    CircleProgressView progressViewCircleMain;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_main)
    TextView progressTextMain;
    // 运动时长
    @BindView(R.id.hpv_time)
    HorizontalProgressView hpvTime;
    @BindView(R.id.progress_text_time)
    TextView progressTextTime;
    // 卡路里
    @BindView(R.id.hpv_cal)
    HorizontalProgressView hpvCal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_text_cal)
    TextView progressTextCal;
    // 动作组数
    @BindView(R.id.hpv_group)
    HorizontalProgressView hpvGroup;
    @BindView(R.id.progress_text_group)
    TextView progressTextGroup;

    // 错误组数
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.hpv_wrong)
    HorizontalProgressView hpvWrong;
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
            }
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

        // 获取运动总时长
        trainingTotalTimes = intent.getIntExtra("totalTrainingTime", 0);
        // 获取动作组数
        groupsOfPose = intent.getIntExtra("groupsOfPose", 0);
        String minute = trainingTotalTimes / 60 + "'";
        String time = (trainingTotalTimes / 60 == 0 ? "" : minute) + trainingTotalTimes % 60 + "\"";

        progressViewCircleMain.setGraduatedEnabled(true);
        progressViewCircleMain.setProgressViewUpdateListener(this);
//        startProcessBar();
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
        // 将评分写入本地文件
        FileUtils.writeStringToFile("123 456 789 " + new Random().nextInt(), CameraXLivePreviewActivity.imageSavePath, "resultData.sys", FileUtils.OVERWRITE_TEXT);
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
            if (poseEntity.getPoseName() == poseName) {
                return index;
            }
        }
        return -1;
    }

    public static void addResultData(Bitmap bitmap, String tip) {
        resultImage.add(bitmap);
        resultTips.add(tip);
    }

    public Bitmap getBitmap() {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        File imagePath = new File(sdPath + "/AiTrainer");
        File[] files = imagePath.listFiles();// 读取
        assert files != null;
        for (File file : files) {
            if (file.getName().contains(".jpg")) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return BitmapFactory.decodeStream(fis);
            }
        }
        return getBitmap();
    }

    @Override
    public void onCircleProgressStart(View view) {

    }

    /**
     * 设置进度条的数值
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startProcessBar() {
        int poseGroups = CameraXLivePreviewActivity.poseEntityList.size();
        int scores = 60 + (int) (40 * (1 - ((float) resultImage.size() / (20 * poseGroups))));
        if (trainingTotalTimes <= 100) {
            // 设置训练时长进度条
            hpvTime.setEndProgress(trainingTotalTimes);
            hpvTime.startProgressAnimation();
        }
        float calSum = 0;
        // 计算卡路里
        int index = 0;
        for (PoseEntity poseEntity : CameraXLivePreviewActivity.poseEntityList) {
            if (index > CameraXLivePreviewActivity.nowPoseIndex) {
                break;
            }
            index++;
            if (poseEntity.getPoseType() == 1) {
                calSum += poseEntity.getPoseProcessValue() * poseEntity.getPoseBurnCal();
            } else {
                calSum += ((float) trainingTotalTimes / 60) * poseEntity.getGetPoseBurnCalPerMin();
            }
        }
        // 动作组数

        float groupsOfPoseProportion = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            groupsOfPoseProportion = 100 * ((float) groupsOfPose / (float) poseGroups);
        }
        // 设置动作组数进度条
        hpvGroup.setEndProgress(groupsOfPoseProportion);
        hpvGroup.startProgressAnimation();
        if (calSum <= 100) {
            // 设置卡路里进度条
            hpvCal.setEndProgress(calSum);
            hpvCal.startProgressAnimation();
        }
        float wrongPoseDecimal = 100 * ((resultImage.size()) / ((float) poseGroups * 20.0f));
        if (wrongPoseDecimal <= 100) {
            // 设置错误动作数进度条
            hpvWrong.setEndProgress(wrongPoseDecimal);
            hpvWrong.startProgressAnimation();
        }
        progressViewCircleMain.setEndProgress(scores);
        progressViewCircleMain.startProgressAnimation();
    }

    /**
     * 环形进度条数值更新
     *
     * @param view
     * @param progress
     */
    @Override
    public void onCircleProgressUpdate(View view, float progress) {
        int progressInt = (int) progress;
        if (view.getId() == R.id.progressView_circle_main) {
            progressTextMain.setText(progressInt + "");
        }
    }

    @Override
    public void onCircleProgressFinished(View view) {

    }

    @Override
    public void onHorizontalProgressStart(View view) {

    }

    // todo 卡路里进度条文本
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onHorizontalProgressUpdate(View view, float progress) {
        int progressInt = (int) progress;
        // 单位符号
        switch (view.getId()) {
            case R.id.hpv_time:
                progressTextTime.setText(progressInt + "'");
                break;
            case R.id.hpv_wrong:
                progressTextWrong.setText((int) (progress * 20 * CameraXLivePreviewActivity.poseEntityList.size()) / 100 + " 个");
                break;
            case R.id.hpv_cal:
                progressTextCal.setText(progressInt + " kcal");
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
}
