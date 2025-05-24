package com.lindum.mlkit.vision.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.PoseDetailsBinding
import com.lindum.mlkit.vision.utils.StatusBarUtils
import com.orhanobut.logger.Logger
import com.xuexiang.xui.XUI

/**
 * @author MarkGosling
 * @date 2022/2/16 1:10
 */
class PoseDetailsActivity : AppCompatActivity() {
    private lateinit var binding: PoseDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding = PoseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    fun init() {
        val layoutParams = binding.vFakeStatusBar.layoutParams
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this)
        binding.vFakeStatusBar.layoutParams = layoutParams
        val intent = getIntent()
        val poseIndex = intent.getIntExtra("poseIndex", 0)
        binding.poseDetailImage.setImageBitmap(
            MainActivity.Companion.poseLibrarySwiperImageArrayList[poseIndex]
        )
        // 动作感觉图片
        val poseDetailFeelingImage = findViewById<ImageView>(R.id.poseDetailFeelingImage)
        var bitmap: Bitmap? = null
        bitmap = MainActivity.Companion.poseDetailFeelingImageArrayList[poseIndex]
        if (bitmap == null) {
            poseDetailFeelingImage.setVisibility(View.GONE)
        }
        poseDetailFeelingImage.setImageBitmap(bitmap)
        Logger.e(
            "第$poseIndex " + MainActivity.Companion.poseEntityArrayList!!.get(
                poseIndex
            ).getPoseSwiperImageUrl()[0]
        )
        val poseDetailFeeling = findViewById<TextView>(R.id.poseDetailFeeling)
        val poseDetailSteps = findViewById<TextView>(R.id.poseDetailSteps)
        val poseDetailCommonMistakes = findViewById<TextView>(R.id.poseDetailCommonMistakes)
        val poseDetails = findViewById<TextView>(R.id.poseDetails)
        val poseDetailName = findViewById<TextView>(R.id.poseDetailName)
        val poseDetailBreath = findViewById<TextView>(R.id.poseDetailBreath)
        // 动作名称
        var poseDetailNameString: String? = null
        poseDetailNameString =
            MainActivity.Companion.poseEntityArrayList!!.get(poseIndex).getPoseName()
        poseDetailName.text = poseDetailNameString
        val feeling = intent.getStringArrayExtra("feeling")
        val steps = intent.getStringArrayExtra("steps")
        val commonMistakes = intent.getStringArrayExtra("commonMistakes")
        val details = intent.getStringArrayExtra("details")
        val breath = intent.getStringExtra("breath")
        setTextView(poseDetailFeeling, feeling!!)
        setTextView(poseDetailSteps, steps!!)
        setTextView(poseDetailCommonMistakes, commonMistakes!!)
        setTextView(poseDetails, details!!)
        setTextView(poseDetailBreath, arrayOf<String?>(breath))
    }

    @SuppressLint("SetTextI18n")
    fun setTextView(textView: TextView, words: Array<String?>) {
        var originText = textView.getText() as String?
        if ("" == words[0]) {
            textView.text = "$originText\n无"
        } else {
            originText += "\n"
            for (word in words) {
                originText += "•"
                originText += word + "\n"
            }
            textView.text = originText
        }
    }
}
