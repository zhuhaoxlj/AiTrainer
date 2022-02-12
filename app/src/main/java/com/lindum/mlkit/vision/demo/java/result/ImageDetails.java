package com.lindum.mlkit.vision.demo.java.result;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.demo.R;
import com.xuexiang.xui.XUI;

/**
 * @author MarkGosling
 * @date 2022/2/12 22:52
 */
public class ImageDetails extends AppCompatActivity {
    private ImageView imageView;
    public static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_details);
        init();
    }

    public void init() {
        imageView = findViewById(R.id.detailsImage);
        imageView.setImageBitmap(bitmap);
    }
}
