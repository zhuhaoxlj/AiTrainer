package com.lindum.mlkit.vision.utils

import com.lindum.mlkit.vision.CameraXLivePreviewActivity
import com.lindum.mlkit.vision.entity.WrongPoseModel

/**
 * 临时数据持有者，用于存储无法序列化且无法直接通过Intent传递的对象
 */
object TemporaryDataHolder {

    // Store the current wrong pose models list
    private var wrongPoseModels: List<WrongPoseModel>? = null

    /**
     * 设置错误姿势模型列表
     */
    fun setWrongPoseModels(models: List<WrongPoseModel>) {
        models.forEach {
            if (!it.isFormatedTips) {
                val commonWrong = CameraXLivePreviewActivity.poseEntityList?.get(
                    it.poseIndex ?: 0
                )?.commonMistakes[0]
                it.poseTips =
                    (it.poseDetail ?: "") + " " + it.poseTips + "\n" + commonWrong
                it.isFormatedTips = true
            }
        }
        wrongPoseModels = models
    }

    /**
     * 获取错误姿势模型的列表
     */
    fun getWrongPoseModels(): List<WrongPoseModel>? {
        return wrongPoseModels
    }
} 