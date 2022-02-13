package com.lindum.mlkit.vision.dev.posedetector;

import java.util.List;

public class PoseEntity {
    // 动作名称
    String poseName;
    // 动作类型 0 为保持性动作  1 为计数性动作
    int poseType;
    // 动作的过程量  维持秒数  完成次数
    int poseProcessValue;

    // 需要监测的部位列表
    List<PosePart> monitorPosePartList;
    // 锻炼的部位 通过这个进行检索针对某个部位的锻炼
    // 最佳拍摄监测角度
    int bestMonitorAngle;
    List<String> trainPartList;
    // ↓↓↓ 注意事项 ↓↓↓

    // 视频链接
    String videoURL;
    // 动作步骤
    String[] stepsOfPose;
    // 呼吸要点
    String breathTips;
    // 动作感觉
    String[] poseFeeling;
    // 动作感觉配图
    String poseFeelingImage;
    // 常见错误
    String commonMistakes;
    // 细节图示
    String[] detailImageURL;
    // 细节图示文字描述
    String[] detailTips;

    public int getBestMonitorAngle() {
        return bestMonitorAngle;
    }

    public void setBestMonitorAngle(int bestMonitorAngle) {
        this.bestMonitorAngle = bestMonitorAngle;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String[] getStepsOfPose() {
        return stepsOfPose;
    }

    public void setStepsOfPose(String[] stepsOfPose) {
        this.stepsOfPose = stepsOfPose;
    }

    public String getBreathTips() {
        return breathTips;
    }

    public void setBreathTips(String breathTips) {
        this.breathTips = breathTips;
    }

    public String[] getPoseFeeling() {
        return poseFeeling;
    }

    public void setPoseFeeling(String[] poseFeeling) {
        this.poseFeeling = poseFeeling;
    }

    public String getPoseFeelingImage() {
        return poseFeelingImage;
    }

    public void setPoseFeelingImage(String poseFeelingImage) {
        this.poseFeelingImage = poseFeelingImage;
    }

    public String getCommonMistakes() {
        return commonMistakes;
    }

    public void setCommonMistakes(String commonMistakes) {
        this.commonMistakes = commonMistakes;
    }

    public String[] getDetailImageURL() {
        return detailImageURL;
    }

    public void setDetailImageURL(String[] detailImageURL) {
        this.detailImageURL = detailImageURL;
    }

    public String[] getDetailTips() {
        return detailTips;
    }

    public void setDetailTips(String[] detailTips) {
        this.detailTips = detailTips;
    }

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
}
