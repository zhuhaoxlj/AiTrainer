package com.lindum.mlkit.vision

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.io.File

internal class QuickShotUtils {
    private fun generateLongBitmap(recyclerView: RecyclerView): Bitmap {
        val itemCount = recyclerView.getAdapter()!!.getItemCount()
        val viewHolder = recyclerView.getAdapter()!!.createViewHolder(recyclerView, 0)

        //Measure the sizes of list item views to find out how big itemView should be
        viewHolder.itemView.measure(
            View.MeasureSpec.makeMeasureSpec(
                recyclerView.getWidth(),
                View.MeasureSpec.EXACTLY
            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // Define measured widths/heights
        val measuredItemHeight = viewHolder.itemView.getMeasuredHeight()
        val measuredItemWidth = viewHolder.itemView.getMeasuredWidth()

        //Set width/height of list item views
        viewHolder.itemView.layout(0, 0, measuredItemWidth, measuredItemHeight)

        //Create the Bitmap and Canvas to draw on
        val recyclerViewBitmap = Bitmap.createBitmap(
            recyclerView.getMeasuredWidth(),
            measuredItemHeight * itemCount,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(recyclerViewBitmap)

        //Draw RecyclerView Background:
        if (recyclerView.getBackground() != null) {
            val drawable = recyclerView.getBackground().mutate()
            drawable.setBounds(measuredItemWidth, measuredItemHeight * itemCount, 0, 0)
            drawable.draw(canvas)
        }

        //Draw all list item views
        var viewHolderTopPadding = 0
        for (i in 0..<itemCount) {
            recyclerView.getAdapter()!!.onBindViewHolder(viewHolder, i)
            viewHolder.itemView.setDrawingCacheEnabled(true)
            viewHolder.itemView.buildDrawingCache()
            canvas.drawBitmap(
                viewHolder.itemView.getDrawingCache(),
                0f,
                viewHolderTopPadding.toFloat(),
                null
            )
            viewHolderTopPadding += measuredItemHeight
            viewHolder.itemView.setDrawingCacheEnabled(false)
            viewHolder.itemView.destroyDrawingCache()

            //            //TODO This should work but doesn't
//            recyclerView.getAdapter().onBindViewHolder(viewHolder, i);
//            viewHolder.itemView.draw(canvas);
//            canvas.drawBitmap(recyclerViewBitmap, 0f, viewHolderTopPadding, null);
//            viewHolderTopPadding += measuredItemHeight;
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

