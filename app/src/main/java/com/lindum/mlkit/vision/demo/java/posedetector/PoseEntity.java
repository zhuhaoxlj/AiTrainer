package com.lindum.mlkit.vision.demo.java.posedetector;

import java.util.List;

public class PoseEntity {
    // 动作名称
    String poseName;
    // 动作类型 0 为保持性动作  1 为计数性动作
    int poseType;
    // 动作的过程量  维持秒数  完成次数
    int poseProcessValue;
    // 需要监测的部位列表
    // 锻炼的部位
    List<PosePart> monitorPosePartList;
    List<String> trainPartList;
    // 注意事项
    String tips;

    public int getPoseType() {
        return poseType;
    }

    public void setPoseType(int poseType) {
        this.poseType = poseType;
    }

    public int getPoseProcessValue() {
        return poseProcessValue;
    }

    public void setPoseProcessValue(int poseProcessValue) {
        this.poseProcessValue = poseProcessValue;
    }

    public List<PosePart> getMonitorPosePartList() {
        return monitorPosePartList;
    }

    public String getPoseName() {
        return poseName;
    }

    public void setPoseName(String poseName) {
        this.poseName = poseName;
    }

    public List<PosePart> getMonitoringPosePartList() {
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
