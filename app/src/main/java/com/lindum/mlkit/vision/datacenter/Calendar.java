package com.lindum.mlkit.vision.datacenter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author MarkGosling
 * @date 2022-02-24 16:46
 */
public class Calendar extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressActivity.show(this);
    }
}
