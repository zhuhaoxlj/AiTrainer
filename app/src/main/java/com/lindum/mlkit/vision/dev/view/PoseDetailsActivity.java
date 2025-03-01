package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;

/**
 * @author MarkGosling
 * @date 2022/2/16 1:10
 */
public class PoseDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        setContentView(R.layout.pose_details);
        init();
    }

    public void init() {
        // 动作展示轮播图
        ImageView poseSwiperImage = findViewById(R.id.poseDetailImage);
        Intent intent = getIntent();
        int poseIndex = intent.getIntExtra("poseIndex", 0);
        poseSwiperImage.setImageBitmap(HomePage.poseLibrarySwiperImageArrayList.get(poseIndex));
        // 动作感觉图片
        ImageView poseDetailFeelingImage = findViewById(R.id.poseDetailFeelingImage);
        Bitmap bitmap = HomePage.poseDetailFeelingImageArrayList.get(poseIndex);
        if(bitmap==null){
            poseDetailFeelingImage.setVisibility(View.GONE);
        }
        poseDetailFeelingImage.setImageBitmap(bitmap);
        Logger.e("第" + poseIndex + " " + HomePage.poseEntityArrayList.get(poseIndex).getPoseSwiperImageUrl()[0]);
        TextView poseDetailFeeling = findViewById(R.id.poseDetailFeeling);
        TextView poseDetailSteps = findViewById(R.id.poseDetailSteps);
        TextView poseDetailCommonMistakes = findViewById(R.id.poseDetailCommonMistakes);
        TextView poseDetails = findViewById(R.id.poseDetails);
        TextView poseDetailName = findViewById(R.id.poseDetailName);
        TextView poseDetailBreath = findViewById(R.id.poseDetailBreath);
        // 动作名称
        String poseDetailNameString = HomePage.poseEntityArrayList.get(poseIndex).getPoseName();
        poseDetailName.setText(poseDetailNameString);
        String[] feeling = intent.getStringArrayExtra("feeling");
        String[] steps = intent.getStringArrayExtra("steps");
        String[] commonMistakes = intent.getStringArrayExtra("commonMistakes");
        String[] details = intent.getStringArrayExtra("details");
        String breath = intent.getStringExtra("breath");
        setTextView(poseDetailFeeling, feeling);
        setTextView(poseDetailSteps, steps);
        setTextView(poseDetailCommonMistakes, commonMistakes);
        setTextView(poseDetails, details);
        setTextView(poseDetailBreath, new String[]{breath});
    }

    @SuppressLint("SetTextI18n")
    public void setTextView(TextView textView, String[] words) {
        String originText = (String) textView.getText();
        if ("".equals(words[0])) {
            textView.setText(originText + "\n无");
        } else {
            originText += "\n";
            for (String word : words) {
                originText += "•";
                originText += word + "\n";
            }
            textView.setText(originText);
        }
    }
}
