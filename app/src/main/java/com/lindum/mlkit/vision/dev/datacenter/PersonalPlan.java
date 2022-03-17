package com.lindum.mlkit.vision.dev.datacenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.datacenter.circleprogress.WaveProgress;
import com.lindum.mlkit.vision.dev.sql.UpdateUserInfo;
import com.lindum.mlkit.vision.dev.utils.DeviceIdUtils;
import com.lindum.mlkit.vision.dev.view.HomePage;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.warkiz.tickseekbar.OnSeekChangeListener;
import com.warkiz.tickseekbar.SeekParams;
import com.warkiz.tickseekbar.TickSeekBar;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.StatusBarUtils;
import com.xuexiang.xui.widget.button.roundbutton.RoundButton;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

/**
 * @author MarkGosling
 * @date 2022/3/11 15:23
 */

public class PersonalPlan extends AppCompatActivity {
    private Intent intent;
    private static int stepsGoalValue;
    private static int calGoalValue;
    private int nowStepsValue;
    private int nowCalValue;
    // 拖动进度条
    TickSeekBar tickSeekBar;
    // 水球进度条
    private WaveProgress waveProgress;
    // 当前数值与目标数值文字
    private TextView trainPlanDataText;
    // 当前界面 flag true为卡路里 false为步数
    private boolean viewFlag = true;
    // 水球进度条下方的目标信息
    TextView trainGoalText;
    // 设备识别码
    String IMEI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        StatusBarUtils.translucent(this);
        setContentView(R.layout.personal_plan);
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 获取 intent 数据
        intent = getIntent();
//        nowStepsValue = intent.getIntExtra("steps", 0);
//        nowCalValue = intent.getIntExtra("cal", 0);
        nowCalValue = 6000;
        nowStepsValue = 4000;
        // 从数据库读取用户信息
        // 初始化组件
        initComponent();
        // 获取IMEI
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            IMEI = DeviceIdUtils.getDeviceId(this);
        } else {
            IMEI = DeviceIdUtils.getIMEIString(this);
        }
        // 初始化数值
        if (calGoalValue == 0) {
            calGoalValue = HomePage.userEntity.getCalGoalValue();
        }
        if (stepsGoalValue == 0) {
            stepsGoalValue = HomePage.userEntity.getStepsGoalValue();
        }
        if (HomePage.userEntity.getCalGoalValue() == 0) {
            calGoalValue = 5000;
        }
        if (HomePage.userEntity.getStepsGoalValue() == 0) {
            stepsGoalValue = 5000;
        }
        updateTextView(viewFlag);
    }

    @Override
    public void finish() {
        super.finish();
        // 更新数据库信息
        new Thread() {
            @Override
            public void run() {
                super.run();
                UpdateUserInfo updateUserInfo = new UpdateUserInfo();
                updateUserInfo.upDate(IMEI, "calGoalValue", calGoalValue);
                updateUserInfo.upDate(IMEI, "stepsGoalValue", stepsGoalValue);
            }
        }.start();
    }

    public void initComponent() {
        // 初始化水球进度条
        waveProgress = findViewById(R.id.wave_progress_bar);
        // 初始化当前数值与目标数值文字
        trainPlanDataText = findViewById(R.id.trainPlanDataText);
        // 目标文字显示
        trainGoalText = findViewById(R.id.trainGoalText);
        // 卡路里目标选择器
        tickSeekBar = findViewById(R.id.calGoalSelector);
        tickSeekBar.setProgress(calGoalValue / 1000);
        tickSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onSeeking(SeekParams seekParams) {
                Logger.e(seekParams.progress + "" + "000cal");
                if (viewFlag) {
                    calGoalValue = seekParams.progress * 1000;
                    trainGoalText.setText("今日消耗卡路里目标(千卡 kcal):" + seekParams.progress + "000cal");
                    int waveProgressValue = (int) (100.0 * (3500.0 / (seekParams.progress * 1000.0)));
                    waveProgress.setValue(waveProgressValue);
                    if (waveProgressValue >= 100) {
                        Logger.e("恭喜你完成目标");
                    }
                } else {
                    stepsGoalValue = seekParams.progress * 1000;
                    trainGoalText.setText("今日步数目标(步 step):" + seekParams.progress + "000step");
                    int waveProgressValue = (int) (100.0 * (3500.0 / (seekParams.progress * 1000.0)));
                    waveProgress.setValue(waveProgressValue);
                    if (waveProgressValue >= 100) {
                        Logger.e("恭喜你完成目标");
                    }
                }
                updateTextView(viewFlag);
            }

            @Override
            public void onStartTrackingTouch(TickSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(TickSeekBar seekBar) {

            }
        });
        // 卡路里按钮和步数按钮初始化
        RelativeLayout calDataButton = findViewById(R.id.calDataButton);
        RelativeLayout stepsDataButton = findViewById(R.id.stepsDataButton);
        // 卡路里数据按钮
        calDataButton.setOnClickListener(view -> {
            stepsDataButton.setBackgroundColor(Color.parseColor("#f8f6f9"));
            calDataButton.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView stepsText = (TextView) stepsDataButton.getChildAt(0);
            stepsText.setTextColor(Color.parseColor("#000000"));
            TextView calText = (TextView) calDataButton.getChildAt(0);
            calText.setTextColor(Color.parseColor("#88b2ff"));
            viewFlag = true;
            updateTextView(viewFlag);
        });
        // 步数数据按钮
        stepsDataButton.setOnClickListener(view -> {
            calDataButton.setBackgroundColor(Color.parseColor("#f8f6f9"));
            stepsDataButton.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView stepsText = (TextView) stepsDataButton.getChildAt(0);
            stepsText.setTextColor(Color.parseColor("#88b2ff"));
            TextView calText = (TextView) calDataButton.getChildAt(0);
            calText.setTextColor(Color.parseColor("#000000"));
            viewFlag = false;
            updateTextView(viewFlag);
        });

        // 返回按钮
        ImageView personalGoalBackButton = findViewById(R.id.personalGoalBackButton);
        personalGoalBackButton.setOnClickListener(view -> finish());
        // 训练按钮
        RoundButton goToTrainBu = findViewById(R.id.goToTrainBu);
        goToTrainBu.setOnClickListener(v -> {
            new MaterialDialog.Builder(this)
                    .iconRes(R.mipmap.tips)
                    .title("提示")
                    .content("1.请选择空旷的场地，或者纯色背景可以提高Ai健身教练的识别精准度。\n2.识别过程中，尽量保持身体在手机屏幕中，尽量选择遮挡人体部位最少的角度进行拍摄，再次感谢您的使用！")
                    .positiveText("确定")
                    .onPositive((dialog, which) -> {
                        Intent intent = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            intent = new Intent(PersonalPlan.this, CameraXLivePreviewActivity.class);
                        }
                        startActivity(intent);
                    })
                    .show();
        });
    }

    /**
     * 更新文本内容
     *
     * @param type true 更新卡路里 false 更新步数
     */
    @SuppressLint("SetTextI18n")
    public void updateTextView(boolean type) {
        if (type) {
            trainPlanDataText.setText(nowCalValue + "/" + calGoalValue + "Cal");
            int value = (int) (100.0 * ((float) nowCalValue / (float) calGoalValue));
            Logger.e("当前值" + value);
            waveProgress.setValue(value);
            tickSeekBar.setProgress(calGoalValue / 1000);
            trainGoalText.setText("今日消耗卡路里目标(千卡 kcal):" + calGoalValue + "cal");
        } else {
            trainPlanDataText.setText(nowStepsValue + "/" + stepsGoalValue + "Step");
            int value = (int) (100.0 * ((float) nowStepsValue / (float) stepsGoalValue));
            Logger.e("当前值" + value);
            trainGoalText.setText("今日消步数目标(步 Step):" + stepsGoalValue + "Step");
            waveProgress.setValue(value);
            tickSeekBar.setProgress(stepsGoalValue / 1000);
        }
    }
}
