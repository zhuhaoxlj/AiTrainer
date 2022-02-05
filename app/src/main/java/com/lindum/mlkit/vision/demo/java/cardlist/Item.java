package com.lindum.mlkit.vision.demo.java.cardlist;


/**
 * @author MarkGosling
 * @date 2022/1/27 5:25
 */
public class Item {

    public String title;
    public String subTitle;
    public int bgDrawable;
    public int drawable;
    public String color;

    public Item(String text, String subTitle, int drawable, String color) {
        this.title = text;
        this.subTitle = subTitle;
        this.drawable = drawable;
        this.color = color;
    }

    public Item(String text, String subTitle, int bgDrawable) {
        this.title = text;
        this.subTitle = subTitle;
        this.bgDrawable = bgDrawable;
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
