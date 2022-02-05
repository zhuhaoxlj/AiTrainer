package com.lindum.mlkit.vision.demo.java;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.demo.R;
import com.lindum.mlkit.vision.demo.java.posedetector.ProcessImage;
import com.sxu.shadowdrawable.ShadowDrawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @date 2022/1/27 8:25
 */
public class TrainingResult extends AppCompatActivity {
    static List<Bitmap> resultImage = new ArrayList<>();
    static List<String> resultTips = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_result);

        Intent intent = getIntent();


        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);



        LinearLayout resultContainer = findViewById(R.id.resultContainer);
//        // 监听图片处理线程的状态,如果处理图片线程结束，加载布局
//        Handler mHandler = new Handler();
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                Log.e("Train", processImage.isAlive() + "---");
//                if (!processImage.isAlive()) {
//                    for (String tip : resultTips) {
//                        int index = resultTips.indexOf(tip);
//                        View resultView = layoutInflater.inflate(R.layout.pose_analysis_card, null);
//                        ImageView imageView = resultView.findViewById(R.id.poseResultImage);
//                        imageView.setImageBitmap(resultImage.get(index));
//                        resultContainer.addView(resultView);
//                    }
//                }
//                if(processImage.isAlive()){
//                    mHandler.postDelayed(this, 50);
//                }
//            }
//        };
//        mHandler.post(r);


        // 进度条
        LinearLayout processBar1 = findViewById(R.id.processBar1);
        LinearLayout processBar2 = findViewById(R.id.processBar2);
        LinearLayout processBar3 = findViewById(R.id.processBar3);
        LinearLayout processBar4 = findViewById(R.id.processBar4);

        /* 这里要用到父容器的管理器 */
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) processBar1.getLayoutParams();
        layoutParams.height = 825 / 2;
        Log.e("TEST", processBar2.getMeasuredHeight() + "");
        ViewTreeObserver viewTreeObserver = processBar2.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(() -> {
            int height = processBar2.getMeasuredHeight();
            int width = processBar2.getMeasuredWidth();
            Log.e("TEST", "L:" + height);
            return true;
        });
        TextView textView1 = findViewById(R.id.processBar1Text);
        TextView textView3 = findViewById(R.id.processBar3Text);
        // 获取运动总时长
        int data = intent.getIntExtra("totalTrainingTime", 0);
        // 获取动作组数
        int groupsOfPose = intent.getIntExtra("groupsOfPose", 0);
        String minute = data / 60 + "'";
        String time = (data / 60 == 0 ? "" : minute) + data % 60 + "\"";
        textView1.setText(time);
        textView3.setText(groupsOfPose + "组");
        ViewGroup.LayoutParams layoutParams1 = processBar3.getLayoutParams();
        layoutParams1.height = 825 / 3;
        ShadowDrawable.setShadowDrawable(processBar1, Color.parseColor("#48b7ff"), 20, Color.parseColor("#48b7ff"), 20, 10, 10);
        ShadowDrawable.setShadowDrawable(processBar2, Color.parseColor("#ffb6d7"), 20, Color.parseColor("#ffb6d7"), 20, 10, 10);
        ShadowDrawable.setShadowDrawable(processBar3, Color.parseColor("#45ceb2"), 20, Color.parseColor("#45ceb2"), 20, 10, 10);
        ShadowDrawable.setShadowDrawable(processBar4, Color.parseColor("#fc5045"), 20, Color.parseColor("#fc5045"), 20, 10, 10);

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
}
