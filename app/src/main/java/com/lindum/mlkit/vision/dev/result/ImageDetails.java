package com.lindum.mlkit.vision.dev.result;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.utils.StatusBarUtils;
import com.xuexiang.xui.XUI;

/**
 * @author MarkGosling
 * @date 2022/2/12 22:52
 */
public class ImageDetails extends AppCompatActivity {
    public static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initTheme(this);
        super.onCreate(savedInstanceState);
        StatusBarUtils.fullScreen(this);
        setContentView(R.layout.image_details);
        init();
    }

    public void init() {
        ImageView imageView = findViewById(R.id.detailsImage);
        imageView.setImageBitmap(bitmap);
    }
}
