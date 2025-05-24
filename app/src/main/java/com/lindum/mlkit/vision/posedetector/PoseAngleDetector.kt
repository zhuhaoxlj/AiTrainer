package com.lindum.mlkit.vision.posedetector

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 角度计算工具
 */
class PoseAngleDetector(private val detectPose: Pose) {
    @Deprecated("3D angle isn't accurate")
    val leftArm3DAngle: Int
        // 左胳膊三维夹角
        get() = get3DAngle(11, 13, 15)

    @Deprecated("3D angle isn't accurate")
    val rightArm3DAngle: Int
        // 右胳膊三维夹角
        get() = get3DAngle(12, 14, 16)

    @Deprecated("3D angle isn't accurate")
    val leftKnee3DAngle: Int
        // 左膝盖三维夹角
        get() = get3DAngle(23, 25, 27)

    @Deprecated("3D angle isn't accurate")
    val rightKnee3DAngle: Int
        // 右腿三维夹角
        get() = get3DAngle(24, 26, 28)

    @Deprecated("3D angle isn't accurate")
    val leftShoulder3DAngle: Int
        // 左肩膀三维夹角
        get() = get3DAngle(13, 11, 23)

    @Deprecated("3D angle isn't accurate")
    val rightShoulder3DAngle: Int
        // 右肩膀三维夹角
        get() = get3DAngle(14, 12, 24)

    @Deprecated("3D angle isn't accurate")
    val leftTrunk3DAngle: Int
        // 左侧躯干三维夹角
        get() = get3DAngle(11, 23, 25)

    @Deprecated("3D angle isn't accurate")
    val rightTrunk3DAngle: Int
        // 右侧躯干三维夹角
        get() = get3DAngle(12, 24, 26)

    @Deprecated("3D angle isn't accurate")
    val leftAnkle3DAngle: Int
        // 左脚腕三维夹角
        get() = get3DAngle(25, 27, 31)

    @Deprecated("3D angle isn't accurate")
    val rightAnkle3DAngle: Int
        // 右脚腕三维夹角
        get() = get3DAngle(26, 28, 32)


    val leftArmAngle: Int
        // 左胳膊夹角
        get() = getAngle(11, 13, 15)

    val rightArmAngle: Int
        // 右胳膊夹角
        get() = getAngle(12, 14, 16)

    val leftKneeAngle: Int
        // 左膝盖夹角
        get() = getAngle(23, 25, 27)

    val rightKneeAngle: Int
        // 右腿夹角
        get() = getAngle(24, 26, 28)

    val leftShoulderAngle: Int
        // 左肩膀夹角
        get() = getAngle(13, 11, 23)

    val rightShoulderAngle: Int
        // 右肩膀夹角
        get() = getAngle(14, 12, 24)

    val leftTrunkAngle: Int
        // 左侧躯干夹角
        get() = getAngle(11, 23, 25)

    val rightTrunkAngle: Int
        // 右侧躯干夹角
        get() = getAngle(12, 24, 26)

    val leftAnkleAngle: Int
        // 左脚腕夹角
        get() = getAngle(25, 27, 31)

    val rightAnkleAngle: Int
        // 右脚腕夹角
        get() = getAngle(26, 28, 32)

    val leftWristAngle: Int
        // 左手腕夹角
        get() = getAngle(13, 15, 19)

    val rightWristAngle: Int
        // 右手腕夹角
        get() = getAngle(14, 16, 20)

    // 通过三个点获取任意夹角
    fun getAnyAngleBy3Point(points: IntArray): Int {
        return getAngle(points[0], points[1], points[2])
    }

    // 通过部位索引获取夹角
    fun getPoseAngleFromPartIndex(posePart: Int): Int {
        return when (posePart) {
            1 -> this.rightWristAngle
            2 -> this.rightArmAngle
            3 -> this.rightShoulderAngle
            4 -> this.leftShoulderAngle
            5 -> this.leftArmAngle
            6 -> this.leftWristAngle
            7 -> this.rightTrunkAngle
            8 -> this.leftTrunkAngle
            9 -> this.rightKneeAngle
            10 -> this.leftKneeAngle
            11 -> this.rightAnkleAngle
            12 -> this.leftAnkle3DAngle
            else -> 0
        }
    }

    // 通过部位索引获取3D夹角
    fun getPose3DAngleFromPartIndex(posePart: Int): Int {
        when (posePart) {
            1 -> return this.rightWristAngle
            2 -> return this.rightArmAngle
            3 -> return this.rightShoulderAngle
            4 -> return this.leftShoulderAngle
            5 -> return this.leftArmAngle
            6 -> return this.leftWristAngle
            7 -> return this.rightTrunkAngle
            8 -> return this.leftTrunkAngle
            9 -> return this.rightKneeAngle
            10 -> return this.leftKneeAngle
            11 -> return this.rightAnkleAngle
            12 -> return this.leftAnkle3DAngle
            else -> return 0
        }
    }

    fun getAngle(ft: Int, md: Int, lt: Int): Int {
        val firstPoint = detectPose.getPoseLandmark(ft)
        val midPoint = detectPose.getPoseLandmark(md)
        val lastPoint: PoseLandmark? = checkNotNull(detectPose.getPoseLandmark(lt))

        checkNotNull(midPoint)
        checkNotNull(firstPoint)
        var result = Math.toDegrees(
            atan2(
                (lastPoint!!.position.y - midPoint.position.y).toDouble(),
                (lastPoint.position.x - midPoint.position.x).toDouble()
            ) - atan2(
                (firstPoint.position.y - midPoint.position.y).toDouble(),
                (firstPoint.position.x - midPoint.position.x).toDouble()
            )
        ).toInt()
        result = abs(result.toDouble()).toInt()
        if (result > 180) {
            result = (360.0 - result).toInt()
        }
        return result
    }

    @Deprecated("3D angle isn't accurate")
    fun get3DAngle(ft: Int, md: Int, lt: Int): Int {
        val firstPoint = detectPose.getPoseLandmark(ft)
        val midPoint = detectPose.getPoseLandmark(md)
        val lastPoint = detectPose.getPoseLandmark(lt)
        val ft3 = firstPoint!!.position3D
        val md3 = midPoint!!.position3D
        val lt3 = lastPoint!!.position3D
        var angle = 0.0
        // 获取方向向量
        val vectorA = doubleArrayOf(
            (ft3.x - md3.x).toDouble(),
            (ft3.y - md3.y).toDouble(),
            (ft3.z - md3.z).toDouble()
        )
        val vectorB = doubleArrayOf(
            (lt3.x - md3.x).toDouble(),
            (lt3.y - md3.y).toDouble(),
            (lt3.z - md3.z).toDouble()
        )
        // 计算单位向量
        val moduloA = sqrt(vectorA[0].pow(2.0) + vectorA[1].pow(2.0) + vectorA[2].pow(2.0))
        val moduloB = sqrt(vectorB[0].pow(2.0) + vectorB[1].pow(2.0) + vectorB[2].pow(2.0))
        val unitVectorA =
            doubleArrayOf(vectorA[0] / moduloA, vectorA[1] / moduloA, vectorA[2] / moduloA)
        val unitVectorB =
            doubleArrayOf(vectorB[0] / moduloB, vectorB[1] / moduloB, vectorB[2] / moduloB)
        // 根据单位向量中心点到原点的距离获取二分之一夹角的余弦值
        val middleAB = doubleArrayOf(
            (unitVectorA[0] + unitVectorB[0]) / 2,
            (unitVectorA[1] + unitVectorB[1]) / 2,
            (unitVectorA[2] + unitVectorB[2]) / 2
        )
        val cosVal = sqrt(middleAB[0].pow(2.0) + middleAB[1].pow(2.0) + middleAB[2].pow(2.0))
        // 根据余弦值获取角度
        angle = acos(cosVal) * 180 / Math.PI * 2
        // 返回
        val b = BigDecimal(angle)
        angle = b.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble() // 取两位小数
        return angle.toInt()
    }
}
