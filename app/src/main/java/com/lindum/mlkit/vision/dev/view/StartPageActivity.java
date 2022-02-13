package com.lindum.mlkit.vision.dev.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.result.ImageDetails;
import com.lindum.mlkit.vision.dev.result.TrainingResult;
import com.xuexiang.xui.XUI;

/**
 * @author MarkGosling
 * @date 2022/2/13 22:21
 */
public class StartPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);
    }
}
