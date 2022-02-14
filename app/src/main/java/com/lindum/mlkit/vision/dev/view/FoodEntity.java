package com.lindum.mlkit.vision.dev.view;

import android.graphics.drawable.Drawable;

/**
 * @author MarkGosling
 * @date 2022/2/15 3:40
 */
public class FoodEntity {
    private String foodName;
    private String fibre;
    private String cal;
    private Drawable foodImage;

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFibre() {
        return fibre;
    }

    public void setFibre(String fibre) {
        this.fibre = fibre;
    }

    public String getCal() {
        return cal;
    }

    public void setCal(String cal) {
        this.cal = cal;
    }

    public Drawable getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(Drawable foodImage) {
        this.foodImage = foodImage;
    }

    public FoodEntity(String foodName, String fibre, String cal, Drawable foodImage) {
        this.foodImage = foodImage;
        this.fibre = fibre;
        this.cal = cal;
        this.foodName = foodName;
    }
}
