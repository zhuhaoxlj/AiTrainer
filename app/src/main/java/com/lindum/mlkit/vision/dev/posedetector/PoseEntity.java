package com.lindum.mlkit.vision.dev.posedetector;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author: MarkGosling
 * @date: 2022/1/16 0:40
 **/
public class PoseEntity {
    /**
     * 动作名称
     */
    String poseName;
    /**
     * 动作类型 0 为保持性动作  1 为计数性动作
     */
    int poseType;
    /**
     * 动作的过程量  维持秒数  完成次数
     */
    int poseProcessValue;

    /**
     * 需要监测的部位列表
     */
    List<PosePart> monitorPosePartList;
    /**
     * 锻炼的部位 通过这个进行检索针对某个部位的锻炼
     */
    List<String> trainPartList;
    /**
     * 最佳拍摄监测角度
     */
    int bestMonitorAngle;
    // ↓↓↓ 注意事项 ↓↓↓

    /**
     * 视频链接
     */
    String videoUrl;
    /**
     * 动作步骤
     */
    String[] stepsOfPose;
    /**
     * 呼吸要点
     */
    String breathTips;
    /**
     * 动作感觉
     */
    String[] poseFeeling;
    /**
     * 动作感觉配图
     */
    String poseFeelingImage;
    /**
     * 常见错误
     */
    String[] commonMistakes;
    /**
     * 细节图示
     */
    String[] detailImageUrl;
    /**
     * 细节图示文字描述
     */
    String[] detailTips;

    public int getBestMonitorAngle() {
        return bestMonitorAngle;
    }

    public void setBestMonitorAngle(int bestMonitorAngle) {
        this.bestMonitorAngle = bestMonitorAngle;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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

    public String[] getCommonMistakes() {
        return commonMistakes;
    }

    public void setCommonMistakes(String[] commonMistakes) {
        this.commonMistakes = commonMistakes;
    }

    public String[] getDetailImageUrl() {
        return detailImageUrl;
    }

    public void setDetailImageUrl(String[] detailImageUrl) {
        this.detailImageUrl = detailImageUrl;
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

    @NonNull
    @Override
    public String toString() {
        return "PoseEntity{" +
                "poseName='" + poseName + '\'' +
                ", poseType=" + poseType +
                ", poseProcessValue=" + poseProcessValue +
                ", monitorPosePartList=" + monitorPosePartList +
                ", bestMonitorAngle=" + bestMonitorAngle +
                ", trainPartList=" + trainPartList +
                ", videoUrl='" + videoUrl + '\'' +
                ", stepsOfPose=" + Arrays.toString(stepsOfPose) +
                ", breathTips='" + breathTips + '\'' +
                ", poseFeeling=" + Arrays.toString(poseFeeling) +
                ", poseFeelingImage='" + poseFeelingImage + '\'' +
                ", commonMistakes='" + commonMistakes + '\'' +
                ", detailImageUrl=" + Arrays.toString(detailImageUrl) +
                ", detailTips=" + Arrays.toString(detailTips) +
                '}';
    }

    public void setTrainPartList(List<String> trainPartList) {
        this.trainPartList = trainPartList;
    }
}
