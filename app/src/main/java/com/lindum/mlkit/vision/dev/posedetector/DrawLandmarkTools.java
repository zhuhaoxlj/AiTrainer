package com.lindum.mlkit.vision.dev.posedetector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class DrawLandmarkTools {
    private Bitmap bitmap;

    public DrawLandmarkTools(Bitmap bmp) {
        this.bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
    }

    public Bitmap drawPoint(PointF pointF) {
        Canvas canvas = new Canvas(bitmap);
        Paint paintPoint = new Paint();
        canvas.drawCircle(pointF.x, pointF.y, 15, paintPoint);
        return bitmap;
    }

    public Bitmap drawPoint(PointF pointF, int color, int radius) {
        Canvas canvas = new Canvas(bitmap);
        Paint paintPoint = new Paint();
        paintPoint.setColor(color);
        canvas.drawCircle(pointF.x, pointF.y, radius, paintPoint);
        return bitmap;
    }

    public Bitmap drawLine(PointF p1, PointF p2) {
        Canvas canvas = new Canvas(bitmap);
        Paint paintLine = new Paint();
        paintLine.setStrokeWidth(10);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paintLine);
        return bitmap;
    }

    public Bitmap drawLine(PointF p1, PointF p2, int lineColor, int lineSize) {
        Canvas canvas = new Canvas(bitmap);
        Paint paintLine = new Paint();
        paintLine.setStrokeWidth(lineSize);
        paintLine.setColor(lineColor);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paintLine);
        return bitmap;
    }

    public Bitmap drawAllLandmark(List<PoseLandmark> landmarkList) {
        if (landmarkList.isEmpty()) {
            //矩形背景
            Paint bgRect = new Paint();
            bgRect.setStyle(Paint.Style.FILL);
            bgRect.setColor(Color.WHITE);
            bgRect.setAlpha(100);
            RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(rectF, bgRect);
            Paint textPaint = new Paint();
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setStrokeWidth(8);
            textPaint.setTextSize(bitmap.getHeight() >> 3);
            textPaint.setColor(Color.RED);
            textPaint.setTextAlign(Paint.Align.CENTER);
            String text = "检测失败，仅允许一个人站在画面中央";
            //计算baseline
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
            float baseline = rectF.centerY() + distance;
            canvas.drawText(text, rectF.centerX(), baseline, textPaint);
            return bitmap;
        } else {
            for (PoseLandmark poseLandmark : landmarkList) {
                bitmap = drawPoint(poseLandmark.getPosition());
            }
            // 嘴
            bitmap = drawLine(landmarkList.get(9).getPosition(), landmarkList.get(10).getPosition());
            // 眼睛
            for (int i = 0; i < 3; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 1).getPosition());
            }
            for (int i = 4; i < 6; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 1).getPosition());
            }
            bitmap = drawLine(landmarkList.get(0).getPosition(), landmarkList.get(4).getPosition());
            bitmap = drawLine(landmarkList.get(6).getPosition(), landmarkList.get(8).getPosition());
            bitmap = drawLine(landmarkList.get(3).getPosition(), landmarkList.get(7).getPosition());
            // 手臂
            bitmap = drawLine(landmarkList.get(11).getPosition(), landmarkList.get(12).getPosition());
            bitmap = drawLine(landmarkList.get(16).getPosition(), landmarkList.get(20).getPosition());
            bitmap = drawLine(landmarkList.get(16).getPosition(), landmarkList.get(22).getPosition());
            bitmap = drawLine(landmarkList.get(15).getPosition(), landmarkList.get(19).getPosition());
            bitmap = drawLine(landmarkList.get(15).getPosition(), landmarkList.get(21).getPosition());
            for (int i = 11; i < 19; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 2).getPosition());
            }
            // 腿
            bitmap = drawLine(landmarkList.get(11).getPosition(), landmarkList.get(23).getPosition());
            bitmap = drawLine(landmarkList.get(12).getPosition(), landmarkList.get(24).getPosition());
            bitmap = drawLine(landmarkList.get(32).getPosition(), landmarkList.get(28).getPosition());
            bitmap = drawLine(landmarkList.get(31).getPosition(), landmarkList.get(27).getPosition());
            for (int i = 23; i < 31; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 2).getPosition());
            }
        }
        return bitmap;
    }

    public Bitmap drawAllLandmark(List<PoseLandmark> landmarkList, int pointColor, int pointSize, int lineColor, int lineSize) {
        if (landmarkList.isEmpty()) {
            //矩形背景
            Paint bgRect = new Paint();
            bgRect.setStyle(Paint.Style.FILL);
            bgRect.setColor(Color.WHITE);
            bgRect.setAlpha(100);
            RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(rectF, bgRect);
            Paint textPaint = new Paint();
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setStrokeWidth(8);
            textPaint.setTextSize(bitmap.getHeight() >> 3);
            textPaint.setColor(Color.RED);
            textPaint.setTextAlign(Paint.Align.CENTER);
            String text = "检测失败，仅允许一个人站在画面中央";
            //计算baseline
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
            float baseline = rectF.centerY() + distance;
            canvas.drawText(text, rectF.centerX(), baseline, textPaint);
            return bitmap;
        } else {
            for (PoseLandmark poseLandmark : landmarkList) {
                bitmap = drawPoint(poseLandmark.getPosition(), pointColor, pointSize);
            }
            // 嘴
            bitmap = drawLine(landmarkList.get(9).getPosition(), landmarkList.get(10).getPosition(), lineColor, lineSize);
            // 眼睛
            for (int i = 0; i < 3; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 1).getPosition(), lineColor, lineSize);
            }
            for (int i = 4; i < 6; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 1).getPosition(), lineColor, lineSize);
            }
            bitmap = drawLine(landmarkList.get(0).getPosition(), landmarkList.get(4).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(6).getPosition(), landmarkList.get(8).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(3).getPosition(), landmarkList.get(7).getPosition(), lineColor, lineSize);
            // 手臂
            bitmap = drawLine(landmarkList.get(11).getPosition(), landmarkList.get(12).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(16).getPosition(), landmarkList.get(20).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(16).getPosition(), landmarkList.get(22).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(15).getPosition(), landmarkList.get(19).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(15).getPosition(), landmarkList.get(21).getPosition(), lineColor, lineSize);
            for (int i = 11; i < 19; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 2).getPosition(), lineColor, lineSize);
            }
            // 腿
            bitmap = drawLine(landmarkList.get(11).getPosition(), landmarkList.get(23).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(12).getPosition(), landmarkList.get(24).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(32).getPosition(), landmarkList.get(28).getPosition(), lineColor, lineSize);
            bitmap = drawLine(landmarkList.get(31).getPosition(), landmarkList.get(27).getPosition(), lineColor, lineSize);
            for (int i = 23; i < 31; i++) {
                drawLine(landmarkList.get(i).getPosition(), landmarkList.get(i + 2).getPosition(), lineColor, lineSize);
            }
        }
        return bitmap;
    }

    public Bitmap cutImage(List<PoseLandmark> landmarkList, Bitmap bitmap) {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float maxY = 0F;
        float maxX = 0F;
        float minX = 99999F;
        float minY = 99999F;
        for (PoseLandmark poseLandmark : landmarkList) {
            maxX = Math.max(maxX, poseLandmark.getPosition().x);
            maxY = Math.max(maxY, poseLandmark.getPosition().y);
            minX = Math.min(minX, poseLandmark.getPosition().x);
            minY = Math.min(minY, poseLandmark.getPosition().y);
        }
        minX = minX - 80 < 0 ? 0 : minX - 80;
        minY = minY - 80 < 0 ? 0 : minY - 80;
        maxX = maxX + 80 > imageHeight ? imageHeight : maxX + 80;
        maxY = maxY + 80 > imageWidth ? imageWidth : maxY + 80;
        Log.e("Cut Max", maxX + "," + maxY);
        Log.e("Cut right", landmarkList.get(31).getPosition().x + "," + landmarkList.get(31).getPosition().y);
        Point point = new Point();
        point.x = (int) minX;
        point.y = (int) minY;
        Log.e("Cut Image", imageWidth + "," + imageHeight);
        Log.e("Cut min", maxY + "-" + minY);
        int height = (int) (maxY - minY);
        int width = (int) (maxX - minX);
        Log.e("Cut width", width + "");
        Log.e("Cut height", height + "");
        if (width < 0 || height < 0) {
            return null;
        }
        return cropBitmap(bitmap, point, width + 80 > imageWidth ? width : width + 80, height + 80 > imageHeight ? height : height + 80);
    }

    /**
     * 裁剪
     *
     * @param bitmap 原图
     * @return 裁剪后的图像
     */
    private Bitmap cropBitmap(Bitmap bitmap, Point point, int width, int height) {
        width = width + point.x > bitmap.getWidth() ? bitmap.getWidth() - point.x : width;
        height = height + point.y > bitmap.getHeight() ? bitmap.getHeight() - point.y : height;
        return Bitmap.createBitmap(bitmap, point.x, point.y, width, height, null, false);
    }
}
