package com.lindum.mlkit.vision.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author MarkGosling
 * @date 2022/3/3 20:06
 */

public class PictureUtils {
    public static Bitmap bitmap = null;

    /**
     * 保存 Bitmap 到本地
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @param bitmap   bitmap 图像文件
     */
    public static void saveBitmap(String filePath, String fileName, Bitmap bitmap) {
        File file = new File(filePath);
        //判断指定文件夹的路径是否存在
        if (!file.exists()) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(filePath + "/" + fileName + ".png");
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.e("TEST", "图片保存成功");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 通过图片连接获得 Bitmap
     *
     * @param url 图片链接
     * @return Bitmap 图片
     */
    public static Bitmap returnBitMap(final String url) {
        new Thread(() -> {
            URL imageurl = null;
            try {
                imageurl = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                assert imageurl != null;
                HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return bitmap;
    }

    /**
     * 从本地获取图片
     * @param fileName 图片绝对路径
     * @return Bitmap 图片
     */
    public static Bitmap getBitmapFromLocalFile(String fileName) {
        File imageFile = new File(fileName);
        try {
            InputStream inputStream = new FileInputStream(imageFile);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
