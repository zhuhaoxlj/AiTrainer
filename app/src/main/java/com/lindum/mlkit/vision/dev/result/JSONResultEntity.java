package com.lindum.mlkit.vision.dev.result;

import androidx.annotation.NonNull;

public class JSONResultEntity {
    private String poseName;
    private String tips;
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

    public String getWrongPoseImagePath() {
        return wrongPoseImagePath;
    }

    public void setWrongPoseImagePath(String wrongPoseImagePath) {
        this.wrongPoseImagePath = wrongPoseImagePath;
    }

    public JSONResultEntity(String poseName, String tips, String wrongPoseImagePath) {
        this.poseName = poseName;
        this.tips = tips;
        this.wrongPoseImagePath = wrongPoseImagePath;
    }

    public JSONResultEntity() {
    }

    @NonNull
    @Override
    public String toString() {
        return "JSONResultEntity{" +
                "poseName='" + poseName + '\'' +
                ", tips='" + tips + '\'' +
                ", wrongPoseImagePath='" + wrongPoseImagePath + '\'' +
                '}';
    }
}
