package com.lindum.mlkit.vision.result

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.entity.WrongPoseModel

/**
 * 错误姿势列表适配器
 *
 * 用于在训练结果界面显示错误姿势列表
 */
class WrongPoseListAdapter(
    context: Context,
    private val resourceId: Int,
    private val wrongPoseList: List<WrongPoseModel>
) : ArrayAdapter<WrongPoseModel>(context, resourceId, wrongPoseList) {

    /**
     * 获取列表项视图
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        // 复用 convertView 提高效率
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, parent, false)
            viewHolder = ViewHolder()

            // 找到各个控件
            viewHolder.poseName = view.findViewById(R.id.poseResultName)
            viewHolder.tips = view.findViewById(R.id.poseResultTips)
            viewHolder.wrongPoseImage = view.findViewById(R.id.wrongPoseImage)
            viewHolder.rightPoseImage = view.findViewById(R.id.rightPoseImage)

            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // 获取当前位置的错误姿势模型
        val wrongPose = wrongPoseList[position]

        // 设置姿势名称
        viewHolder.poseName?.text = wrongPose.poseName ?: "--"

        // 设置提示信息
        viewHolder.tips?.text = wrongPose.poseTips ?: "--"

        // 设置错误姿势图片
        wrongPose.wrongPoseImage?.let {
            viewHolder.wrongPoseImage?.setImageBitmap(it)
        }

        // 设置正确姿势图片
        wrongPose.rightPoseBitmap?.let {
            viewHolder.rightPoseImage?.setImageBitmap(it)
        }

        return view
    }

    /**
     * ViewHolder 模式提高列表性能
     */
    private class ViewHolder {
        var poseName: TextView? = null
        var tips: TextView? = null
        var wrongPoseImage: ImageView? = null
        var rightPoseImage: ImageView? = null
    }
} 