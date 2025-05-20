package com.lindum.mlkit.vision

import android.content.Context
import android.widget.Toast

class SpeechUtils {
    fun sayHoldOn(context: Context?) {
        Toast.makeText(context, "保持住，不要乱动", Toast.LENGTH_LONG).show()
    }
}
