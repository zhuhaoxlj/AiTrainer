package com.lindum.mlkit.vision

import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.lindum.mlkit.vision.dev.databinding.LayoutCustomTabBarBinding

class CustomTabBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: LayoutCustomTabBarBinding =
        LayoutCustomTabBarBinding.inflate(LayoutInflater.from(context), this, true)
    private var onTabSelectedListener: OnTabSelectedListener? = null

    interface OnTabSelectedListener {
        fun onTabSelected(position: Int)
    }

    init {
        initView()
    }

    private fun initView() {
        setupListeners()
        updateSelectedTab(binding.llPoseLibrary, 0)
    }

    private fun setupListeners() {
        binding.llPoseLibrary.setOnClickListener {
            updateSelectedTab(binding.llPoseLibrary, 0)
            onTabSelectedListener?.onTabSelected(0)
        }

        binding.tabCenter.setOnClickListener {
            onTabSelectedListener?.onTabSelected(1)
        }

        binding.llDiary.setOnClickListener {
            updateSelectedTab(binding.llDiary, 2)
            onTabSelectedListener?.onTabSelected(2)
        }
    }

    fun setOnTabSelectedListener(listener: OnTabSelectedListener) {
        this.onTabSelectedListener = listener
    }

    fun setCurrentTab(position: Int) {
        when (position) {
            0 -> updateSelectedTab(binding.llPoseLibrary, position)
            1 -> updateSelectedTab(binding.tabCenter, position)
            2 -> updateSelectedTab(binding.llDiary, position)
        }
    }

    private fun updateSelectedTab(selectedTab: LinearLayout, position: Int) {
        // 重置所有标签为未选中状态
        binding.llPoseLibrary.isSelected = false
        binding.tabCenter.isSelected = false
        binding.llDiary.isSelected = false

        // 设置当前选中的标签
        selectedTab.isSelected = true

        // 更新图标和文字颜色
        updateTabAppearance(binding.llPoseLibrary, binding.ivPoseLibrary, binding.tvPoseLibrary)
        // 中间图标不需要文字颜色更新
        updateCenterTabAppearance()
        updateTabAppearance(binding.llDiary, binding.ivDiary, binding.tvDiary)
    }

    private fun updateTabAppearance(
        tab: LinearLayout,
        iconView: ImageView,
        textView: TextView
    ) {
        if (tab.isSelected) {
            // 选中状态下颜色变为黑色
            textView.setTextColor("#F3BF4C".toColorInt())
            iconView.setColorFilter("#F3BF4C".toColorInt())
        } else {
            // 未选中状态下颜色变为灰色
            textView.setTextColor(ContextCompat.getColor(context, com.lindum.mlkit.vision.dev.R.color.tab_unselected))
            iconView.setColorFilter(ContextCompat.getColor(context, com.lindum.mlkit.vision.dev.R.color.tab_unselected))
        }
    }

    private fun updateCenterTabAppearance() {
        // 中间按钮不需要变色，它始终是黑色背景白色图标
    }
} 