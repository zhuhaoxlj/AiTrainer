package com.lindum.mlkit.vision.dev.result;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * @author MarkGosling
 * @date 2022/2/12 10:17
 */
public class ResultEntity {
    private String poseName;
    private String tips;
    private Bitmap wrongPoseImage;
    private Bitmap rightPoseImage;
    private String wrongPoseImagePath;

    public String getPoseName() {
        return poseName;
    }

    public void setPoseName(String poseName) {
        this.poseName = poseName;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public Bitmap getWrongPoseImage() {
        return wrongPoseImage;
    }

    public void setWrongPoseImage(Bitmap wrongPoseImage) {
        this.wrongPoseImage = wrongPoseImage;
    }

    public Bitmap getRightPoseImage() {
        return rightPoseImage;
    }

    public void setRightPoseImage(Bitmap rightPoseImage) {
        this.rightPoseImage = rightPoseImage;
    }

    public String getWrongPoseImagePath() {
        return wrongPoseImagePath;
    }

    public void setWrongPoseImagePath(String wrongPoseImagePath) {
        this.wrongPoseImagePath = wrongPoseImagePath;
    }

    public ResultEntity() {

    }

    public ResultEntity(String poseName, String tips, Bitmap wrongPoseImage, Bitmap rightPoseImage) {
        this.poseName = poseName;
        this.tips = tips;
        this.wrongPoseImage = wrongPoseImage;
        this.rightPoseImage = rightPoseImage;
    }

    @NonNull
    @Override
    public String toString() {
        return "ResultEntity{" +
                "poseName='" + poseName + '\'' +
                ", tips='" + tips + '\'' +
                ", wrongPoseImage=" + wrongPoseImage +
                ", rightPoseImage=" + rightPoseImage +
                ", wrongPoseImagePath='" + wrongPoseImagePath + '\'' +
                '}';
    }
}
