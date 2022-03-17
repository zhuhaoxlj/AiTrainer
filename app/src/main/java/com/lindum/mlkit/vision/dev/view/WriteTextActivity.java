package com.lindum.mlkit.vision.dev.view;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.sarnava.textwriter.TextWriter;

import java.util.Locale;

public class WriteTextActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_text);
        TextWriter textWriter = findViewById(R.id.textWriter);
        textWriter
                .setWidth(12)
                .setDelay(30)
                .setColor(Color.BLUE)
                .setConfig(TextWriter.Configuration.INTERMEDIATE)
                .setSizeFactor(30f)
                .setLetterSpacing(25f)
                .setText("AiTrainer".toUpperCase(Locale.ROOT))
                .setListener(() -> {
                    //do stuff after animation is finished
                })
                .startAnimation();

    }
}
