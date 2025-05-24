package com.lindum.mlkit.vision

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import androidx.core.graphics.createBitmap

internal class QuickShotUtils {
    private fun generateLongBitmap(recyclerView: RecyclerView): Bitmap {
        val itemCount = recyclerView.adapter!!.itemCount
        val viewHolder = recyclerView.adapter!!.createViewHolder(recyclerView, 0)

        //Measure the sizes of list item views to find out how big itemView should be
        viewHolder.itemView.measure(
            View.MeasureSpec.makeMeasureSpec(
                recyclerView.width,
                View.MeasureSpec.EXACTLY
            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // Define measured widths/heights
        val measuredItemHeight = viewHolder.itemView.measuredHeight
        val measuredItemWidth = viewHolder.itemView.measuredWidth

        //Set width/height of list item views
        viewHolder.itemView.layout(0, 0, measuredItemWidth, measuredItemHeight)

        //Create the Bitmap and Canvas to draw on
        val recyclerViewBitmap =
            createBitmap(recyclerView.measuredWidth, measuredItemHeight * itemCount)
        val canvas = Canvas(recyclerViewBitmap)

        //Draw RecyclerView Background:
        if (recyclerView.background != null) {
            val drawable = recyclerView.background.mutate()
            drawable.setBounds(measuredItemWidth, measuredItemHeight * itemCount, 0, 0)
            drawable.draw(canvas)
        }

        //Draw all list item views
        var viewHolderTopPadding = 0
        for (i in 0..<itemCount) {
            recyclerView.adapter!!.onBindViewHolder(viewHolder, i)
            viewHolder.itemView.setDrawingCacheEnabled(true)
            viewHolder.itemView.buildDrawingCache()
            canvas.drawBitmap(
                viewHolder.itemView.drawingCache,
                0f,
                viewHolderTopPadding.toFloat(),
                null
            )
            viewHolderTopPadding += measuredItemHeight
            viewHolder.itemView.setDrawingCacheEnabled(false)
            viewHolder.itemView.destroyDrawingCache()
        }
        return recyclerViewBitmap
    }

    companion object {
        fun getMimeType(src: String): String {
            // jpg 格式图片
            var src = src
            val jpgFormat = "jpg"
            src = src.substring(1)
            if (jpgFormat == src) {
                src = "jpeg"
            }
            return "image" + File.separator + src
        }

        val isAboveAPI29: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}

