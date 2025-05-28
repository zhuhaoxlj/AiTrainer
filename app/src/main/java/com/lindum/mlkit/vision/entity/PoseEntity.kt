package com.lindum.mlkit.vision.entity

import android.util.Log
import androidx.annotation.Keep
import com.lindum.mlkit.vision.posedetector.PosePart

/**
 * @author: MarkGosling
 * @date: 2022/1/16 0:40
 */
@Keep
class PoseEntity {
    /**
     * 动作库封面图
     */
    var poseCoverImage: String? = null

    /**
     * 动作详情三个面展示
     */
    var poseSwiperImageUrl: Array<String?>? = null

    /**
     * 动作名称
     */
    var poseName: String? = null

    /**
     * 动作类型 0 为保持性动作  1 为计数性动作
     */
    var poseType: Int = 0

    /**
     * 动作的过程量  维持秒数  完成次数
     */
    var poseProcessValue: Int = 0

    /**
     * 需要监测的部位列表
     */
    var monitorPosePartList: MutableList<PosePart>? = null

    // ↓↓↓ 注意事项 ↓↓↓
    /**
     * 动作步骤
     */
    var stepsOfPose: Array<String?>? = null

    /**
     * 呼吸要点
     */
    var breathTips: String? = null

    /**
     * 动作感觉
     */
    var poseFeeling: Array<String?>? = null

    /**
     * 动作感觉配图
     */
    var poseFeelingImage: String? = null

    /**
     * 常见错误
     */
    var commonMistakes: Array<String?>? = null

    /**
     * 细节图示
     */
    var detailImageUrl: Array<String?>? = null

    /**
     * 细节图示文字描述
     */
    var detailTips: Array<String?>? = null


    /**
     * 统计运动时长
     */
    var countMinutes: Float = 0f

    /**
     * 统计运动次数
     */
    var countTimes: Int = 0

    // 添加一个初始化块，用于在Release构建中验证类是否被正确保留
    init {
        Log.e("PoseLibraryDebug", "PoseEntity 实例化: ${this.javaClass.name}")
    }

    override fun toString(): String {
        Log.e("PoseLibraryDebug", "PoseEntity.toString() 被调用")
        return "PoseEntity{" +
                ", poseCoverImage='" + poseCoverImage + '\'' +
                ", poseSwiperImageUrl=" + poseSwiperImageUrl.contentToString() +
                ", poseName='" + poseName + '\'' +
                ", poseType=" + poseType +
                ", poseProcessValue=" + poseProcessValue +
                ", monitorPosePartList=" + this.monitorPosePartList +
                ", stepsOfPose=" + stepsOfPose.contentToString() +
                ", breathTips='" + breathTips + '\'' +
                ", poseFeeling=" + poseFeeling.contentToString() +
                ", poseFeelingImage='" + poseFeelingImage + '\'' +
                ", commonMistakes=" + commonMistakes.contentToString() +
                ", detailImageUrl=" + detailImageUrl.contentToString() +
                ", detailTips=" + detailTips.contentToString() +
                '}'
    }
}
