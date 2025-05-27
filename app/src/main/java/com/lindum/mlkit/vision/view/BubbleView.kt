package com.lindum.mlkit.vision.view

/**
 *
 * @author zhuhao
 * @date  15:08
 **/
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.lindum.mlkit.vision.dev.R
import androidx.core.content.withStyledAttributes

class BubbleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    companion object {
        const val ARROW_RIGHT = 0
        const val ARROW_LEFT = 1
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#F9E9CD".toColorInt() // 气泡背景色
        style = Paint.Style.FILL
    }
    private val cornerRadius = 8f // 圆角半径
    private val triangleWidth = 20f // 三角宽
    private val triangleHeight = 20f // 三角高

    // 三角形距离顶部的距离，默认居中
    private var triangleTopMargin = -1f
    // 箭头方向，默认右侧
    private var arrowDirection = ARROW_RIGHT
    
    // 缓存绘制路径，避免重复创建
    private var rectPath: Path? = null
    private var trianglePath: Path? = null
    
    // 缓存矩形区域
    private val rectF = RectF()
    
    // 标记是否需要重新计算路径
    private var pathsDirty = true

    init {
        // 从XML中获取属性
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.BubbleView, defStyle, 0) {
                triangleTopMargin = getDimension(R.styleable.BubbleView_triangleTopMargin, -1f)
                arrowDirection = getInt(R.styleable.BubbleView_arrowDirection, ARROW_RIGHT)
            }
        }
        
        // 设置硬件加速层类型以提高性能
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * 设置箭头方向
     * @param direction 箭头方向: [ARROW_RIGHT] 或 [ARROW_LEFT]
     */
    fun setArrow(direction: Int) {
        if (direction != arrowDirection) {
            arrowDirection = direction
            pathsDirty = true
            invalidate()  // 重绘视图
        }
    }

    /**
     * 设置箭头距离顶部的距离
     * @param margin 距离顶部的距离（单位：像素），-1表示居中
     */
    fun setTriangleTopMargin(margin: Float) {
        if (margin != triangleTopMargin) {
            triangleTopMargin = margin
            pathsDirty = true
            invalidate()  // 重绘视图
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 尺寸变化时标记路径需要重新计算
        pathsDirty = true
    }

    // 计算所有绘制路径
    private fun calculatePaths() {
        if (!pathsDirty) return
        
        // 计算三角形的Y坐标
        val triangleY: Float = if (triangleTopMargin >= 0) {
            // 如果设置了距离顶部的值，使用该值
            triangleTopMargin + (triangleHeight / 2)
        } else {
            // 否则居中显示
            height / 2f
        }
        
        // 根据箭头方向创建不同的路径
        if (arrowDirection == ARROW_RIGHT) {
            // 箭头在右侧，左边留出三角形高度的距离
            rectF.set(triangleHeight, 0f, width - triangleWidth, height.toFloat())
            
            // 创建三角形路径
            if (trianglePath == null) trianglePath = Path()
            trianglePath?.reset()
            trianglePath?.moveTo(width - triangleWidth, triangleY - triangleHeight / 2)
            trianglePath?.lineTo(width.toFloat(), triangleY)
            trianglePath?.lineTo(width - triangleWidth, triangleY + triangleHeight / 2)
            trianglePath?.close()
        } else {
            // 箭头在左侧，右边留出三角形高度的距离
            rectF.set(triangleWidth, 0f, width - triangleHeight, height.toFloat())
            
            // 创建三角形路径
            if (trianglePath == null) trianglePath = Path()
            trianglePath?.reset()
            trianglePath?.moveTo(triangleWidth, triangleY - triangleHeight / 2)
            trianglePath?.lineTo(0f, triangleY)
            trianglePath?.lineTo(triangleWidth, triangleY + triangleHeight / 2)
            trianglePath?.close()
        }
        
        pathsDirty = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 确保路径已计算
        calculatePaths()
        
        // 绘制圆角矩形
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        
        // 绘制三角形
        trianglePath?.let { canvas.drawPath(it, paint) }
    }
}