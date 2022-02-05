package com.lindum.mlkit.vision.demo.java.posedetector;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.math.BigDecimal;

public class PoseAngleDetector {
    private final Pose detectPose;

    public PoseAngleDetector(Pose pose) {
        detectPose = pose;
    }

    // 左胳膊三维夹角
    public int getLeftArm3DAngle() {
        return get3DAngle(11, 13, 15);
    }

    // 右胳膊三维夹角
    public int getRightArm3DAngle() {
        return get3DAngle(12, 14, 16);
    }

    // 左膝盖三维夹角
    public int getLeftKnee3DAngle() {
        return get3DAngle(23, 25, 27);
    }

    // 右腿三维夹角
    public int getRightKnee3DAngle() {
        return get3DAngle(24, 26, 28);
    }

    // 左肩膀三维夹角
    public int getLeftShoulder3DAngle() {
        return get3DAngle(13, 11, 23);
    }

    // 右肩膀三维夹角
    public int getRightShoulder3DAngle() {
        return get3DAngle(14, 12, 24);
    }

    // 左侧躯干三维夹角
    public int getLeftTrunk3DAngle() {
        return get3DAngle(11, 23, 25);
    }

    // 右侧躯干三维夹角
    public int getRightTrunk3DAngle() {
        return get3DAngle(12, 24, 26);
    }

    // 左脚腕三维夹角
    public int getLeftAnkle3DAngle() {
        return get3DAngle(25, 27, 31);
    }

    // 右脚腕三维夹角
    public int getRightAnkle3DAngle() {
        return get3DAngle(26, 28, 32);
    }


    // 左胳膊夹角
    public int getLeftArmAngle() {
        return getAngle(11, 13, 15);
    }

    // 右胳膊夹角
    public int getRightArmAngle() {
        return getAngle(12, 14, 16);
    }

    // 左膝盖夹角
    public int getLeftKneeAngle() {
        return getAngle(23, 25, 27);
    }

    // 右腿夹角
    public int getRightKneeAngle() {
        return getAngle(24, 26, 28);
    }

    // 左肩膀夹角
    public int getLeftShoulderAngle() {
        return getAngle(13, 11, 23);
    }

    // 右肩膀夹角
    public int getRightShoulderAngle() {
        return getAngle(14, 12, 24);
    }

    // 左侧躯干夹角
    public int getLeftTrunkAngle() {
        return getAngle(11, 23, 25);
    }

    // 右侧躯干夹角
    public int getRightTrunkAngle() {
        return getAngle(12, 24, 26);
    }

    // 左脚腕夹角
    public int getLeftAnkleAngle() {
        return getAngle(25, 27, 31);
    }

    // 右脚腕夹角
    public int getRightAnkleAngle() {
        return getAngle(26, 28, 32);
    }

    // 左手腕夹角
    public int getLeftWristAngle() {
        return getAngle(13, 15, 19);
    }

    // 右手腕夹角
    public int getRightWristAngle() {
        return getAngle(14, 16, 20);
    }

    // 通过三个点获取任意夹角
    public int getAnyAngleBy3Point(int[] points) {
        return getAngle(points[0], points[1], points[2]);
    }

    // 通过部位索引获取夹角
    public int getPoseAngleFromPartIndex(int posePart) {
        switch (posePart) {
            case 1:
                return getRightWristAngle();
            case 2:
                return getRightArmAngle();
            case 3:
                return getRightShoulderAngle();
            case 4:
                return getLeftShoulderAngle();
            case 5:
                return getLeftArmAngle();
            case 6:
                return getLeftWristAngle();
            case 7:
                return getRightTrunkAngle();
            case 8:
                return getLeftTrunkAngle();
            case 9:
                return getRightKneeAngle();
            case 10:
                return getLeftKneeAngle();
            case 11:
                return getRightAnkleAngle();
            case 12:
                return getLeftAnkle3DAngle();
            default:
                return 0;
        }
    }

    // 通过部位索引获取3D夹角
    public int getPose3DAngleFromPartIndex(int posePart) {
        switch (posePart) {
            case 1:
                return getRightWristAngle();
            case 2:
                return getRightArmAngle();
            case 3:
                return getRightShoulderAngle();
            case 4:
                return getLeftShoulderAngle();
            case 5:
                return getLeftArmAngle();
            case 6:
                return getLeftWristAngle();
            case 7:
                return getRightTrunkAngle();
            case 8:
                return getLeftTrunkAngle();
            case 9:
                return getRightKneeAngle();
            case 10:
                return getLeftKneeAngle();
            case 11:
                return getRightAnkleAngle();
            case 12:
                return getLeftAnkle3DAngle();
            default:
                return 0;
        }
    }

    public int getAngle(int ft, int md, int lt) {
        PoseLandmark firstPoint = detectPose.getPoseLandmark(ft);
        PoseLandmark midPoint = detectPose.getPoseLandmark(md);
        PoseLandmark lastPoint = detectPose.getPoseLandmark(lt);

        assert lastPoint != null;
        assert midPoint != null;
        assert firstPoint != null;
        int result =
                (int) Math.toDegrees(
                        Math.atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                                lastPoint.getPosition().x - midPoint.getPosition().x)
                                - Math.atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                                firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result);
        if (result > 180) {
            result = (int) (360.0 - result);
        }
        return result;
    }


    private int get3DAngle(int ft, int md, int lt) {
        PoseLandmark firstPoint = detectPose.getPoseLandmark(ft);
        PoseLandmark midPoint = detectPose.getPoseLandmark(md);
        PoseLandmark lastPoint = detectPose.getPoseLandmark(lt);
        PointF3D ft3 = firstPoint.getPosition3D();
        PointF3D md3 = midPoint.getPosition3D();
        PointF3D lt3 = lastPoint.getPosition3D();
        double angle = 0;
        // 获取方向向量
        double[] vectorA = new double[]{ft3.getX() - md3.getX(), ft3.getY() - md3.getY(), ft3.getZ() - md3.getZ()};
        double[] vectorB = new double[]{lt3.getX() - md3.getX(), lt3.getY() - md3.getY(), lt3.getZ() - md3.getZ()};
        // 计算单位向量
        double moduloA = Math.sqrt(Math.pow(vectorA[0], 2) + Math.pow(vectorA[1], 2) + Math.pow(vectorA[2], 2));
        double moduloB = Math.sqrt(Math.pow(vectorB[0], 2) + Math.pow(vectorB[1], 2) + Math.pow(vectorB[2], 2));
        double[] unitVectorA = new double[]{vectorA[0] / moduloA, vectorA[1] / moduloA, vectorA[2] / moduloA};
        double[] unitVectorB = new double[]{vectorB[0] / moduloB, vectorB[1] / moduloB, vectorB[2] / moduloB};
        // 根据单位向量中心点到原点的距离获取二分之一夹角的余弦值
        double[] middleAB = new double[]{(unitVectorA[0] + unitVectorB[0]) / 2, (unitVectorA[1] + unitVectorB[1]) / 2, (unitVectorA[2] + unitVectorB[2]) / 2};
        double cosVal = Math.sqrt(Math.pow(middleAB[0], 2) + Math.pow(middleAB[1], 2) + Math.pow(middleAB[2], 2));
        // 根据余弦值获取角度
        angle = Math.acos(cosVal) * 180 / Math.PI * 2;
        // 返回
        BigDecimal b = new BigDecimal(angle);
        angle = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); // 取两位小数
        return (int) angle;
    }

}
