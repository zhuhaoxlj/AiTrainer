package com.lindum.mlkit.vision.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.lindum.mlkit.vision.dev.R
import androidx.core.content.withStyledAttributes

/**
 *
 * @author zhuhao
 * @date  14:55
 **/
class FontTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        // 缓存已加载的字体，避免重复加载
        private val typefaceCache = HashMap<String, Typeface>()
    }

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.FontTextView) {
                val fontAsset = getString(R.styleable.FontTextView_fontAsset)
                if (!fontAsset.isNullOrEmpty()) {
                    setTypefaceFromAsset(context, fontAsset)
                }
            }
        }
    }
    
    private fun setTypefaceFromAsset(context: Context, fontAsset: String) {
        try {
            // 先从缓存中查找字体
            var tf = typefaceCache[fontAsset]
            if (tf == null) {
                // 如果缓存中没有，则从资源加载
                tf = Typeface.createFromAsset(context.assets, fontAsset)
                // 存入缓存
                typefaceCache[fontAsset] = tf
            }
            typeface = tf
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}