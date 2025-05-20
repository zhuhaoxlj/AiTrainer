package com.lindum.mlkit.vision.view;


import android.graphics.Bitmap;

/**
 * @author MarkGosling
 * @date 2022/1/27 5:25
 */
public class PoseLibraryItem {
    public int index;
    public String title;
    public String subTitle;
    public int bgDrawable;
    public int drawable;
    public Bitmap bitmap;
    public String color;

    public PoseLibraryItem(int index, String text, String subTitle, int drawable, String color) {
        this.index = index;
        this.title = text;
        this.subTitle = subTitle;
        this.drawable = drawable;
        this.color = color;
    }

    public PoseLibraryItem(int index, String text, String subTitle, int bgDrawable) {
        this.index = index;
        this.title = text;
        this.subTitle = subTitle;
        this.bgDrawable = bgDrawable;
    }

    public PoseLibraryItem(int index, String text, String subTitle, Bitmap bitmap, String color) {
        this.bitmap = bitmap;
        this.index = index;
        this.title = text;
        this.subTitle = subTitle;
        this.color = color;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return title;
    }

    public void setText(String text) {
        this.title = text;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getBgDrawable() {
        return bgDrawable;
    }

    public void setBgDrawable(int bgDrawable) {
        this.bgDrawable = bgDrawable;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
