package com.lindum.mlkit.vision.dev.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
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
    }
}
