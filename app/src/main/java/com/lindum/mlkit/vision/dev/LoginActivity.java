package com.lindum.mlkit.vision.dev;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.sxu.shadowdrawable.ShadowDrawable;

/**
 * Login Activity
 *
 * @author: MarkGosling
 * @date: 2022/1/25 16:14
 **/

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pose_result);
        LinearLayout processBar1 = findViewById(R.id.processBar1);
        LinearLayout processBar2 = findViewById(R.id.processBar2);
        LinearLayout processBar3 = findViewById(R.id.processBar3);
        LinearLayout processBar4 = findViewById(R.id.processBar4);
        Log.e("TEST", processBar1.toString());
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
        ViewGroup.LayoutParams layoutParams1 = processBar3.getLayoutParams();
        layoutParams1.height = 825 / 3;
        ShadowDrawable.setShadowDrawable(processBar1, Color.parseColor("#48b7ff"), 20, Color.parseColor("#48b7ff"), 20, 10, 10);
        ShadowDrawable.setShadowDrawable(processBar2, Color.parseColor("#ffb6d7"), 20, Color.parseColor("#ffb6d7"), 20, 10, 10);
        ShadowDrawable.setShadowDrawable(processBar3, Color.parseColor("#45ceb2"), 20, Color.parseColor("#45ceb2"), 20, 10, 10);
        ShadowDrawable.setShadowDrawable(processBar4, Color.parseColor("#fc5045"), 20, Color.parseColor("#fc5045"), 20, 10, 10);
    }
}

