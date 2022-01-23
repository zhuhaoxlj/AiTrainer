package com.lindum.mlkit.vision.demo.java.posedetector;

import java.util.List;

public class PoseEntity {
    // 动作名称
    String poseName;
    // 需要监测的部位列表
    List<PosePart> monitorPosePartList;
    // 锻炼的部位
    List<String> trainPartList;
    // 注意事项
    String tips;

    public String getPoseName() {
        return poseName;
    }

    public void setPoseName(String poseName) {
        this.poseName = poseName;
    }

    public List<PosePart> getMonitorPosePartList() {
        return monitorPosePartList;
    }

    public void setMonitorPosePartList(List<PosePart> monitorPosePartList) {
        this.monitorPosePartList = monitorPosePartList;
    }

    public List<String> getTrainPartList() {
        return trainPartList;
    }

    public void setTrainPartList(List<String> trainPartList) {
        this.trainPartList = trainPartList;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
