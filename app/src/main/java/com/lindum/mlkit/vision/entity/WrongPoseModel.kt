package com.lindum.mlkit.vision.entity

import android.graphics.Bitmap
import androidx.annotation.Keep
import java.io.Serializable

/**
 * 错误的姿态类
 *
 * @author zhuhao
 * @date  19:17
 **/
@Keep
data class WrongPoseModel(
    var poseName: String? = null,
    var poseIndex: Int? = null, // 第几个动作，用于排序展示
    var wrongPoseImage: Bitmap? = null,
    var rightPoseBitmap: Bitmap? = null,
    val wrongTypeId: Int? = null, // 错误的 ID 用于筛选出同类的动作出错几次
    val poseDetail: String? = null,// 动作细节描述
    var poseTips: String? = null,// 错误姿态建议
) : Serializable