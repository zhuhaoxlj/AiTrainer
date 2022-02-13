package com.lindum.mlkit.vision.dev.posedetector;

public class PosePart {
    // 部位的索引 当索引为 -1时为任意三点夹角监测
    int poseIndex;
    // 三点索引
    int[] threePointIndex;
    // 0 保持型 1 计数型
    int poseType;
    // 监测的角度类型 2为二维角度 3为三维角度
    int poseAngleType;
    // 保持型部位标准角度范围
    int[] fixedRange;
    // 计数型 该部位第一个标准角度范围
    int[] firstStandardRange;
    // 计数型 该部位第二个标准角度范围
    int[] secondStandardRange;

    public int getPoseIndex() {
        return poseIndex;
    }

    public void setPoseIndex(int poseIndex) {
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

    public int[] getThreePointIndex() {
        return threePointIndex;
    }

    public void setThreePointIndex(int[] threePointIndex) {
        this.threePointIndex = threePointIndex;
    }
}
