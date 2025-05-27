package com.lindum.mlkit.vision.posedetector;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * @author: MarkGosling
 * @date: 2022-01-16 14:24
 **/
public class PosePart {
    /**
     * 部位的索引
     */
    int[] poseIndex;
    /**
     * 部位名称
     */
    String partName;

    /**
     * 0 固定观测角度 1 动态角度
     */
    int poseType;
    /**
     * 监测的角度类型 2为二维角度 3为三维角度
     */
    int poseAngleType;
    /**
     * 保持型部位标准角度范围
     */
    int[] fixedRange;
    /**
     * 计数型 该部位第一个标准角度范围；上，左
     */
    int[] firstStandardRange;
    /**
     * 计数型 该部位第二个标准角度范围；下，右
     */
    int[] secondStandardRange;
    /**
     * 固定角度错误纠正建议
     */
    String tips;

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int[] getPoseIndex() {
        return poseIndex;
    }

    public void setPoseIndex(int[] poseIndex) {
        this.poseIndex = poseIndex;
    }

    public int getPoseType() {
        return poseType;
    }

    public void setPoseType(int poseType) {
        this.poseType = poseType;
    }

    public int getPoseAngleType() {
        return poseAngleType;
    }

    public void setPoseAngleType(int poseAngleType) {
        this.poseAngleType = poseAngleType;
    }

    public int[] getFixedRange() {
        return fixedRange;
    }

    public void setFixedRange(int[] fixedRange) {
        this.fixedRange = fixedRange;
    }

    public int[] getFirstStandardRange() {
        return firstStandardRange;
    }

    public void setFirstStandardRange(int[] firstStandardRange) {
        this.firstStandardRange = firstStandardRange;
    }

    public int[] getSecondStandardRange() {
        return secondStandardRange;
    }

    public void setSecondStandardRange(int[] secondStandardRange) {
        this.secondStandardRange = secondStandardRange;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    @NonNull
    @Override
    public String toString() {
        return "PosePart{" +
                "poseIndex=" + Arrays.toString(poseIndex) +
                ", partName='" + partName + '\'' +
                ", poseType=" + poseType +
                ", poseAngleType=" + poseAngleType +
                ", fixedRange=" + Arrays.toString(fixedRange) +
                ", firstStandardRange=" + Arrays.toString(firstStandardRange) +
                ", secondStandardRange=" + Arrays.toString(secondStandardRange) +
                ", tips='" + tips + '\'' +
                '}';
    }
}
