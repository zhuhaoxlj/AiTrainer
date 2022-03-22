package com.lindum.mlkit.vision.dev.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.xuexiang.xui.utils.StatusBarUtils;

public class UI extends AppCompatActivity {
    private ImageView iv;
    private int dx=0;  //左移的距离
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui);
        // 状态栏沉浸
        StatusBarUtils.fullScreen(this);
    }
    public void turnLeft(View view){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ui);
        dx-=10;  //每次点击，向左移动
        Matrix matrix=new Matrix();
        matrix.setTranslate(dx,0);  //竖直方向不移动，只有左边移动

        //买个纸
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        //买个画板
        Canvas canvas=new Canvas(bitmap1);

        Paint paint=new Paint();
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap,matrix,paint);

        iv.setImageBitmap(bitmap1);


    }
}
