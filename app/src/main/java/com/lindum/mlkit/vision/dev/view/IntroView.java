package com.lindum.mlkit.vision.dev.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.ramotion.paperonboarding.PaperOnboardingEngine;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.xuexiang.xui.XUI;

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
        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener((oldElementIndex, newElementIndex) -> {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeTextColor(Color.parseColor("#ffffff"));
                }
            }.start();
        });

        engine.setOnRightOutListener(() -> {
            finish();
            usedSoftware();
        });
        changeTextColor(Color.parseColor("#ffffff"));
    }

    public void changeTextColor(int color) {
        FrameLayout frameLayout = findViewById(R.id.onboardingContentTextContainer);
        LinearLayout layout = (LinearLayout) frameLayout.getChildAt(0);
        for (int i = 0; i < 2; i++) {
            TextView textView = (TextView) layout.getChildAt(i);
            textView.setTextColor(color);
        }
    }

    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        PaperOnboardingPage scr1 = new PaperOnboardingPage("AiTrainer", "你的私人Ai健身教练",
                Color.parseColor("#628eb9"), R.drawable.ic_icon3, R.drawable.key);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("AiTrainer", "为你纠正错误的姿势",
                Color.parseColor("#61aab8"), R.drawable.ic_icon2, R.drawable.wallet);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("AiTrainer", "帮你遇见更好的自己~",
                Color.parseColor("#9e92c1"), R.drawable.ic_icon1, R.drawable.shopping_cart);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }

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
}
