package com.lindum.mlkit.vision.posedetector

import android.util.Log
import androidx.annotation.Keep

/**
 * @author: MarkGosling
 * @date: 2022-01-16 14:24
 */
@Keep
class PosePart {
    /**
     * 部位的索引
     */
    var poseIndex: IntArray? = null

    /**
     * 部位名称
     */
    var partName: String? = null

    /**
     * 0 固定观测角度 1 动态角度
     */
    var poseType: Int = 0

    /**
     * 监测的角度类型 2为二维角度 3为三维角度
     */
    var poseAngleType: Int = 0

    /**
     * 保持型部位标准角度范围
     */
    var fixedRange: IntArray? = null

    /**
     * 计数型 该部位第一个标准角度范围；上，左
     */
    var firstStandardRange: IntArray? = null

    /**
     * 计数型 该部位第二个标准角度范围；下，右
     */
    var secondStandardRange: IntArray? = null

    /**
     * 固定角度错误纠正建议
     */
    var tips: String? = null

    // 添加一个初始化块，用于在Release构建中验证类是否被正确保留
    init {
        Log.e("PoseLibraryDebug", "PosePart 实例化: ${this.javaClass.name}")
    }

    override fun toString(): String {
        Log.e("PoseLibraryDebug", "PosePart.toString() 被调用")
        return "PosePart{" +
                "poseIndex=" + poseIndex.contentToString() +
                ", partName='" + partName + '\'' +
                ", poseType=" + poseType +
                ", poseAngleType=" + poseAngleType +
                ", fixedRange=" + fixedRange.contentToString() +
                ", firstStandardRange=" + firstStandardRange.contentToString() +
                ", secondStandardRange=" + secondStandardRange.contentToString() +
                ", tips='" + tips + '\'' +
                '}'
    }
}
