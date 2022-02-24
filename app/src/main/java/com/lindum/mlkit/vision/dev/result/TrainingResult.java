package com.lindum.mlkit.vision.dev.result;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.widget.progress.CircleProgressView;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Administrator
 * @date 2022/1/27 8:25
 */
public class TrainingResult extends AppCompatActivity implements HorizontalProgressView.HorizontalProgressUpdateListener, CircleProgressView.CircleProgressUpdateListener {
    public static List<String> resultName = new ArrayList<>();
    public static List<Bitmap> resultImage = new ArrayList<>();
    public static List<String> resultTips = new ArrayList<>();
    // 动作组数进度条占比
    int groupsOfPose;
    private ListView listView;
    @BindView(R.id.progressView_circle_main)
    CircleProgressView progressViewCircleMain;
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
    @BindView(R.id.progress_text_cal)
    TextView progressTextCal;
    // 动作组数
    @BindView(R.id.hpv_group)
    HorizontalProgressView hpvGroup;
    @BindView(R.id.progress_text_group)
    TextView progressTextGroup;

    // 错误组数
    @BindView(R.id.hpv_wrong)
    HorizontalProgressView hpvWrong;
    @BindView(R.id.progress_text_wrong)
    TextView progressTextWrong;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_result);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        List<ResultEntity> resultEntityList = new ArrayList<>();
        for (String tips : resultTips) {
            int index = resultTips.indexOf(tips);
            if (tips.length() < 3) {
                break;
            }
            ResultEntity temp = new ResultEntity(resultName.get(index), tips.substring(1, tips.length()), resultImage.get(index), resultImage.get(index));
            resultEntityList.add(temp);
        }
        // 初始化控件
        listView = findViewById(R.id.resultList);
        ResultListAdapter resultListAdapter = new ResultListAdapter(this, R.layout.result_list_item, resultEntityList);
        listView.setAdapter(resultListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageView imageView = view.findViewById(R.id.wrongPoseImage);
                imageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
                Intent intent1 = new Intent(TrainingResult.this, ImageDetails.class);
                ImageDetails.bitmap = bitmap;
                imageView.setDrawingCacheEnabled(false);
                startActivity(intent1);
            }
        });

        //  初始化控件
        hpvTime.setProgressViewUpdateListener(this);
        hpvGroup.setProgressViewUpdateListener(this);
        hpvCal.setProgressViewUpdateListener(this);
        hpvWrong.setProgressViewUpdateListener(this);
        progressViewCircleMain.setGraduatedEnabled(true);
        progressViewCircleMain.setProgressViewUpdateListener(this);

        // 获取运动总时长
        int data = intent.getIntExtra("totalTrainingTime", 0);
        // 获取动作组数
        groupsOfPose = intent.getIntExtra("groupsOfPose", 0);
        String minute = data / 60 + "'";
        String time = (data / 60 == 0 ? "" : minute) + data % 60 + "\"";

        progressViewCircleMain.setGraduatedEnabled(true);
        progressViewCircleMain.setProgressViewUpdateListener(this);
        startProcessBar();
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

    public void startProcessBar() {
        hpvTime.startProgressAnimation();
        float groupsOfPoseProportion = 100 * ((float) groupsOfPose / (float) CameraXLivePreviewActivity.poseEntityList.size());
        hpvGroup.setEndProgress(groupsOfPoseProportion);
        hpvGroup.startProgressAnimation();
        hpvCal.startProgressAnimation();
        hpvWrong.startProgressAnimation();
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

    @Override
    public void onHorizontalProgressUpdate(View view, float progress) {
        int progressInt = (int) progress;
        // 单位符号
        switch (view.getId()) {
            case R.id.hpv_time:
                progressTextTime.setText(progressInt + "'");
                break;
            case R.id.hpv_wrong:
                progressTextWrong.setText(progressInt + " 个");
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
