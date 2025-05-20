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

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.FontTextView) {
                val fontAsset = getString(R.styleable.FontTextView_fontAsset)
                if (!fontAsset.isNullOrEmpty()) {
                    try {
                        val tf = Typeface.createFromAsset(context.assets, fontAsset)
                        typeface = tf
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}