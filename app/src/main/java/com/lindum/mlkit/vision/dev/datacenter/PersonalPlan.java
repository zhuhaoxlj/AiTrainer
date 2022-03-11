package com.lindum.mlkit.vision.dev.datacenter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.warkiz.tickseekbar.OnSeekChangeListener;
import com.warkiz.tickseekbar.SeekParams;
import com.warkiz.tickseekbar.TickSeekBar;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.StatusBarUtils;

/**
 * @author MarkGosling
 * @date 2022/3/11 15:23
 */

public class PersonalPlan extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        StatusBarUtils.translucent(this);
        setContentView(R.layout.personal_plan);
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 初始化组件
        initComponent();
    }

    public void initComponent() {
        // 目标文字显示
        TextView textView = findViewById(R.id.calGoalText);
        // 卡路里目标选择器
        TickSeekBar tickSeekBar = findViewById(R.id.calGoalSelector);
        tickSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSeeking(SeekParams seekParams) {
                Logger.e(seekParams.progress + "" + "000cal");
                textView.setText("今日消耗卡路里目标(千卡 kcal):" + seekParams.progress + "000cal");
            }

            @Override
            public void onStartTrackingTouch(TickSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(TickSeekBar seekBar) {

            }
        });
        // 卡路里数据按钮
        RelativeLayout calDataButton = findViewById(R.id.calDataButton);
        // 步数数据按钮
        RelativeLayout stepsDataButton = findViewById(R.id.stepsDataButton);
        calDataButton.setOnClickListener(view -> {
            stepsDataButton.setBackgroundColor(Color.parseColor("#f8f6f9"));
            calDataButton.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView stepsText = (TextView) stepsDataButton.getChildAt(0);
            stepsText.setTextColor(Color.parseColor("#000000"));
            TextView calText = (TextView) calDataButton.getChildAt(0);
            calText.setTextColor(Color.parseColor("#88b2ff"));
        });
        stepsDataButton.setOnClickListener(view -> {
            calDataButton.setBackgroundColor(Color.parseColor("#f8f6f9"));
            stepsDataButton.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView stepsText = (TextView) stepsDataButton.getChildAt(0);
            stepsText.setTextColor(Color.parseColor("#88b2ff"));
            TextView calText = (TextView) calDataButton.getChildAt(0);
            calText.setTextColor(Color.parseColor("#000000"));
        });

        // 返回按钮
        ImageView personalGoalBackButton = findViewById(R.id.personalGoalBackButton);
        personalGoalBackButton.setOnClickListener(view -> finish());
    }
}
