package com.lindum.mlkit.vision

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast

/**
 * Created by Bert on 2017/4/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ShotUtils(private val mContext: Context) {
    private var mImageReader: ImageReader? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null


    init {
        createImageReader()
    }

    fun setData(mResultData: Intent?) {
        Companion.mResultData = mResultData
    }

    fun init(mActivity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            Toast.makeText(mContext, "系统版本不支持", Toast.LENGTH_SHORT).show()
            return
        }
        val mediaProjectionManager =
            mActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mActivity.startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun createImageReader() {
        mImageReader = ImageReader.newInstance(
            this.screenWidth,
            this.screenHeight, PixelFormat.RGBA_8888, 1
        )
    }

    fun startScreenShot(mShotListener: ShotListener) {
        val handler1 = Handler()
        handler1.postDelayed(object : Runnable {
            override fun run() {
                startVirtual()
            }
        }, 5)

        handler1.postDelayed(object : Runnable {
            override fun run() {
                startCapture(mShotListener)
            }
        }, 5)
    }

    fun startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay()
        } else {
            setUpMediaProjection()
            virtualDisplay()
        }
    }


    private fun virtualDisplay() {
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(
            "screen-mirror",
            this.screenWidth,
            this.screenHeight,
            this.screenDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader!!.getSurface(),
            null,
            null
        )
    }

    fun setUpMediaProjection() {
        if (mResultData == null) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            mContext.startActivity(intent)
        } else {
            if (mMediaProjection == null) {
                mMediaProjection = this.mediaProjectionManager!!.getMediaProjection(
                    Activity.RESULT_OK,
                    mResultData!!
                )
            }
        }
    }

    private val mediaProjectionManager: MediaProjectionManager?
        get() = mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?

    fun startCapture(mShotListener: ShotListener) {
        val image = mImageReader!!.acquireLatestImage()
        if (image == null) {
            startScreenShot(mShotListener)
        } else {
            val bitmap = covetBitmap(image)
            if (bitmap != null) {
                mShotListener.OnSuccess(bitmap)
                stopVirtual()
            } else {
            }
        }
    }

    private fun covetBitmap(image: Image): Bitmap {
        val width = image.getWidth()
        val height = image.getHeight()
        val planes = image.getPlanes()
        val buffer = planes[0]!!.getBuffer()
        //每个像素的间距
        val pixelStride = planes[0]!!.getPixelStride()
        //总的间距
        val rowStride = planes[0]!!.getRowStride()
        val rowPadding = rowStride - pixelStride * width
        var bitmap =
            Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        image.close()
        return bitmap
    }

    fun stopVirtual() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null

        tearDownMediaProjection()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    private val screenWidth: Int
        /**
         * 获得屏幕宽高
         */
        get() {
            val wm = mContext
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val width = wm.getDefaultDisplay().getWidth()
            return width
        }

    private val screenHeight: Int
        /**
         * 获得屏幕宽高
         */
        get() {
            val wm = mContext
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val height = wm.getDefaultDisplay().getHeight()
            return height
        }

    private val screenDpi: Int
        /**
         * 获得DPI
         */
        get() {
            val wm = mContext
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val metrics = DisplayMetrics()
            wm.getDefaultDisplay().getMetrics(metrics)
            return metrics.densityDpi
        }


    interface ShotListener {
        fun OnSuccess(bitmap: Bitmap?)
    }

    companion object {
        const val REQUEST_MEDIA_PROJECTION: Int = 1
        private var mResultData: Intent? = null
    }
}


