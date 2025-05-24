package com.lindum.mlkit.vision.utils

import android.util.Log
import com.lindum.mlkit.vision.CameraXLivePreviewActivity.Companion.addWarnPart
import com.lindum.mlkit.vision.posedetector.PoseAngleDetector
import com.lindum.mlkit.vision.posedetector.PosePart
import java.util.logging.Logger

/**
 * 八段锦动作检测与评分
 *
 * @author zhuhao
 * @date  20:07
 **/
object BaduanjinPoseEvaluator {

    /**
     * 两手托天理三焦 检测器
     */
    class Pose1 {
        companion object {
            var maxLeftArmAngle = 0
            var maxRightArmAngle = 0

            fun getScore(
                poseStage: String,
                monitoringList: List<PosePart>,
                detector: PoseAngleDetector
            ): String {
                Log.i("test_stage", poseStage)
                with(detector) {
                    val angle1 = getAngle(16, 24, 23) // 右手腕，右胯，左胯
                    val angle2 = getAngle(15, 23, 24) // 左手腕，左胯，右胯
                    val angle3 = getAngle(15, 11, 23) // 左手腕，左肩，左胯
                    val angle4 = getAngle(16, 12, 24) // 右手腕，右肩，右胯
                    // 确定当前帧的状态
                    val currentFrameState = when {
                        // 最底部状态条件
                        angle1 in 135..180 && angle2 in 135..180 &&
                                angle3 in 5..15 && angle4 in 5..15 -> 0

                        // 最顶部状态条件
                        leftArmAngle > 140 && rightArmAngle > 140 &&
                                leftShoulderAngle > 150 && rightShoulderAngle > 150 -> 2

                        // 中间状态
                        else -> 1
                    }

                    // 更新最大角度记录（不管状态如何）
                    if (currentFrameState == 2) {
                        if (maxLeftArmAngle < leftArmAngle) {
                            maxLeftArmAngle = leftArmAngle
                        }
                        if (maxRightArmAngle < rightArmAngle) {
                            maxRightArmAngle = rightArmAngle
                        }
                    } else {
                        if (maxLeftArmAngle < 165) {
                            addWarnPart(5)
                        }
                        if (maxRightArmAngle < 165) {
                            addWarnPart(4)
                        }
                        if (leftKneeAngle < 165) {
                            addWarnPart(10)
                        }
                        if (leftKneeAngle < 165) {
                            addWarnPart(9)
                        }
                    }

                    // 状态稳定性检查
                    if (currentFrameState == lastDetectedState) {
                        stableCount++
                    } else {
                        // 状态变化，重置计数器
                        stableCount = 1
                        lastDetectedState = currentFrameState
                    }

                    // 只有当状态稳定一段时间后才真正改变stage
                    if (stableCount >= STABLE_THRESHOLD) {
                        // 只在状态真正改变时赋值，避免不必要的赋值
                        if (stage != currentFrameState) {
                            stage = currentFrameState
                            // 重置计数器，让新状态也有稳定期
                            stableCount = 0
                        }
                    }
                }

                return when (stage) {
                    0 -> {
                        // todo 计算分数
                        "最底部分数：${calculateBottomScore()}"
                    }

                    2 -> {
                        // todo 计算分数
                        "最顶部分数：${calculateTopScore()}"
                    }

                    else -> "待机中..."
                }
            }

            // 添加分数计算方法
            private fun calculateBottomScore(): Int {
                // 根据具体需求实现分数计算
                return 100
            }

            private fun calculateTopScore(): Int {
                // 根据最大角度等参数计算顶部动作的分数
                val angleScore = (maxLeftArmAngle + maxRightArmAngle) / 3
                return minOf(100, angleScore)
            }
        }
    }
}