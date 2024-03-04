package com.lindum.mlkit.vision.dev.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;

/**
 * @author MarkGosling
 * @date 2022/2/14 11:38
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
}
