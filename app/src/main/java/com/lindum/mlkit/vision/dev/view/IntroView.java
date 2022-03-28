package com.lindum.mlkit.vision.dev.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.utils.GetPermissionUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.ramotion.paperonboarding.PaperOnboardingEngine;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class IntroView extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initTheme(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // todo 适配水滴屏幕 当隐藏状态栏的时候上面是黑边
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_main_layout);
        // 适配安卓11及以下版本的存储权限请求
        new MaterialDialog.Builder(this)
                .cancelable(false)
                .iconRes(R.mipmap.tips)
                .title("提示")
                .content("检测到您第一次使用本软件，请授予权限保证软件正常运行，软件运行过程中不会获取您的隐私，请放心使用!")
                .positiveText("确定")
                .onPositive((dialog, which) -> GetPermissionUtils.getRuntimePermissions(this))
                .show();
        // Intro 绑定数据
        bindDataToAssembly();
    }

    // Intro 绑定数据
    public void bindDataToAssembly() {
        com.ramotion.paperonboarding.PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboard(), getApplicationContext());
        // 第一屏 Intro 界面修改字体颜色
        changeTextColor(Color.parseColor("#ffffff"));
        // 界面切换监听
        engine.setOnChangeListener((oldElementIndex, newElementIndex) -> new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 修改 Intro 界面的字体颜色
                changeTextColor(Color.parseColor("#ffffff"));
            }
        }.start());
        // 当最后一个界面滑动结束
        engine.setOnRightOutListener(() -> {
            usedSoftware();
            finish();
        });
    }

    /**
     * 修改字体颜色
     * @param color 字体的颜色
     */
    public void changeTextColor(int color) {
        FrameLayout frameLayout = findViewById(R.id.onboardingContentTextContainer);
        LinearLayout layout = (LinearLayout) frameLayout.getChildAt(0);
        for (int i = 0; i < 2; i++) {
            TextView textView = (TextView) layout.getChildAt(i);
            textView.setTextColor(color);
        }
    }

    /**
     * 获取 Intro 界面数据
     *
     * @return 返回 PaperOnboardingPage 数组
     */
    private ArrayList<PaperOnboardingPage> getDataForOnboard() {
        PaperOnboardingPage scr1 = new PaperOnboardingPage("AiTrainer", "你的私人Ai健身教练",
                Color.parseColor("#8fc7ff"), R.drawable.ic_icon3, R.drawable.key);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("AiTrainer", "为你纠正错误的姿势",
                Color.parseColor("#89eeff"), R.drawable.ic_icon2, R.drawable.wallet);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("AiTrainer", "帮你遇见更好的自己~",
                Color.parseColor("#d1c1ff"), R.drawable.ic_icon1, R.drawable.shopping_cart);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }

    /**
     * 标记使用过软件
     */
    public void usedSoftware() {
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        try {
            String string = "第一次使用软件";
            fos = openFileOutput("firstUse.sys", MODE_PRIVATE);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(string);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Logger.e("不可以退出");
    }
}
