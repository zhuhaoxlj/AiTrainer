package com.lindum.mlkit.vision.dev.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    //    /**
//     * 保存Bitmap到本地
//     *
//     * @param inputStream inputStream
//     */
//    public static void saveBitmap(InputStream inputStream) {
//        Bitmap mBitmap = BitmapFactory.decodeStream(inputStream);
//        //3.保存Bitmap
//        try {
//            String savePath = getSDCardPath() + "/AAABBB/";
//            File temp = new File(savePath);
//            //文件
//            String filepath = savePath + "/Screen_1.png";
//            File file = new File(filepath);
//            if (!temp.exists()) {
//                temp.mkdirs();
//            }
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            FileOutputStream fos = null;
//            fos = new FileOutputStream(file);
//            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            fos.flush();
//            fos.close();
//            Toast.makeText(this, "截屏文件已保存至SDCard/AAABBB/下", Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
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
}
