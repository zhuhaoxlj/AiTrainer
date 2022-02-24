package com.lindum.mlkit.vision.dev.view;


/**
 * @author MarkGosling
 * @date 2022/1/27 5:25
 */
public class Item {
    public int index;
    public String title;
    public String subTitle;
    public int bgDrawable;
    public int drawable;
    public String color;

    public Item(int index, String text, String subTitle, int drawable, String color) {
        this.index = index;
        this.title = text;
        this.subTitle = subTitle;
        this.drawable = drawable;
        this.color = color;
    }

    public Item(int index, String text, String subTitle, int bgDrawable) {
        this.index = index;
        this.title = text;
        this.subTitle = subTitle;
        this.bgDrawable = bgDrawable;
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
}
