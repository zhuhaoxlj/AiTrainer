package com.lindum.mlkit.vision.utils

import android.content.Context
import android.content.Intent
import com.lindum.mlkit.vision.entity.WrongPoseModel
import com.lindum.mlkit.vision.result.TrainingResultActivity
import java.io.Serializable

/**
 * Activity 启动工具类
 * 
 * 用于简化 Activity 间的跳转和数据传递
 */
object ActivityStarter {

    /**
     * 启动训练结果页面，传递错误姿势列表
     * 
     * @param context 上下文
     * @param wrongPoseList 错误姿势列表
     * @param totalTrainingTime 训练总时长（秒）
     * @param groupsOfPose 动作组数
     */
    fun startTrainingResultActivity(
        context: Context, 
        wrongPoseList: List<WrongPoseModel>,
        totalTrainingTime: Int,
        groupsOfPose: Int
    ) {
        // 存储数据到临时持有者，而不是通过 Intent 传递
        TemporaryDataHolder.setWrongPoseModels(wrongPoseList)
        
        val intent = Intent(context, TrainingResultActivity::class.java).apply {
            // 只传递简单数据类型
            putExtra("hasWrongPoseList", true) // 标记有错误姿势列表
            putExtra("totalTrainingTime", totalTrainingTime)
            putExtra("groupsOfPose", groupsOfPose) 
            putExtra("init", 0) // 新创建的训练结果
        }
        context.startActivity(intent)
    }
} 