package com.lindum.mlkit.vision.dev

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.annotation.RequiresApi
import com.lindum.mlkit.vision.dev.PixelCopyHelper.PixelCopyListener
import com.lindum.mlkit.vision.dev.PixelCopyHelper.getSurfaceBitmap
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class QuickShot {
    private var printStackTrace = false
    private var jpgQuality: Int = JPG_MAX_QUALITY
    private var fileExtension: String = EXTENSION_JPG
    private var filename: String? = System.currentTimeMillis().toString()
    private var path: String? = null
    private var bitmap: Bitmap? = null
    private var view: View? = null
    private val context: Context
    private var listener: QuickShotListener? = null


    private constructor(view: View) {
        this.view = view
        this.context = view.getContext()
    }

    private constructor(bitmap: Bitmap, context: Context) {
        this.bitmap = bitmap
        this.context = context
    }

    /**
     * @param filename if not set, filename defaults to a timestamp from [System.currentTimeMillis]
     */
    fun setFilename(filename: String?): QuickShot {
        this.filename = filename
        return this
    }

    /**
     * *NOTE: For devices running Android 10 (+API 29) and above image files will now be saved relative to /Internal storage/Pictures/ due to 'Scoped storage'*<br></br><br></br>
     *
     * Directories which don't already exist will be automatically created.
     *
     * @param path if not set, path defaults to /Pictures/ regardless of any API level
     */
    fun setPath(path: String?): QuickShot {
        this.path = path
        return this
    }

    private fun setFileExtension(fileExtension: String) {
        this.fileExtension = fileExtension
    }

    /**
     * Save as .jpg format in highest quality
     * default is .jpg
     */
    fun toJPG(): QuickShot {
        jpgQuality = JPG_MAX_QUALITY
        setFileExtension(EXTENSION_JPG)
        return this
    }

    /**
     * Save as .jpg format in a custom quality between 0-100
     * default is 100
     */
    fun toJPG(jpgQuality: Int): QuickShot {
        this.jpgQuality = jpgQuality
        setFileExtension(EXTENSION_JPG)
        return this
    }

    /**
     * Save as .png format for lossless compression
     * default is .jpg
     */
    fun toPNG(): QuickShot {
        setFileExtension(EXTENSION_PNG)
        return this
    }

    /**
     * Save as .nomedia for making the picture invisible for photo viewer apps and galleries.
     */
    fun toNomedia(): QuickShot {
        setFileExtension(EXTENSION_NOMEDIA)
        return this
    }

    /**
     * Enable QuickShot to log and print exception stacks
     */
    fun enableLogging(): QuickShot {
        printStackTrace = true
        return this
    }

    /**
     * Listen for successive or failure results when calling save()
     */
    fun setResultListener(listener: QuickShotListener): QuickShot {
        this.listener = listener
        if (listener == null) {
            throw NullPointerException("QuickShot.setResultListener() was provided with a null object reference")
        }
        return this
    }

    private fun getContext(): Context {
        if (context == null) {
            throw NullPointerException("Attempt to save the picture failed: View or Context was null")
        }
        return context
    }

    fun getBitmap(): Bitmap? {
        if (bitmap != null) {
            return bitmap
        } else if (view is TextureView) {
            bitmap = (view as TextureView).getBitmap()
            val canvas = Canvas(bitmap!!)
            view?.draw(canvas)
            canvas.setBitmap(null)
            return bitmap
        } else {
            bitmap =
                Bitmap.createBitmap(view!!.getWidth(), view!!.getHeight(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            view!!.draw(canvas)
            canvas.setBitmap(null)
            return bitmap
        }
    }


    /**
     * save() runs in a asynchronous thread
     *
     * @throws NullPointerException if View is null.
     */
    @Throws(NullPointerException::class)
    fun save() {
        if (view is SurfaceView) {
            getSurfaceBitmap(view as SurfaceView, object : PixelCopyListener {
                override fun onSurfaceBitmapReady(surfaceBitmap: Bitmap?) {
                    BitmapSaver(
                        getContext(),
                        surfaceBitmap,
                        printStackTrace,
                        path,
                        filename,
                        fileExtension,
                        jpgQuality,
                        listener!!
                    ).execute()
                }

                override fun onSurfaceBitmapError(errorMsg: String?) {
                    listener!!.onQuickShotFailed(path, errorMsg)
                }
            })
        } else {
            BitmapSaver(
                getContext(),
                getBitmap(),
                printStackTrace,
                path,
                filename,
                fileExtension,
                jpgQuality,
                listener!!
            ).execute()
        }
    }

    interface QuickShotListener {
        fun onQuickShotSuccess(path: String?)

        fun onQuickShotFailed(path: String?, errorMsg: String?)
    }

    internal class BitmapSaver(
        context: Context?,
        private var bitmap: Bitmap?,
        private val printStacktrace: Boolean,
        private var path: String?,
        private val filename: String?,
        private val fileExtension: String,
        private val jpgQuality: Int,
        private val listener: QuickShotListener
    ) : AsyncTask<Void?, Void?, Void?>() {
        private val weakContext: WeakReference<Context?>
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        private var errorMsg: String? = null
        private var file: File? = null

        init {
            this.weakContext = WeakReference<Context?>(context)
        }

        @Deprecated("")
        private fun saveLegacy() {
            if (path == null) {
                path = Environment.getExternalStorageDirectory()
                    .toString() + File.separator + Environment.DIRECTORY_PICTURES
            }
            val directory = File(path)
            directory.mkdirs()
            file = File(directory, filename + fileExtension)
            try {
                BufferedOutputStream(FileOutputStream(file)).use { out ->
                    when (fileExtension) {
                        EXTENSION_JPG -> bitmap!!.compress(
                            Bitmap.CompressFormat.JPEG,
                            jpgQuality,
                            out
                        )

                        EXTENSION_PNG -> bitmap!!.compress(Bitmap.CompressFormat.PNG, 0, out)
                    }
                }
            } catch (e: Exception) {
                if (printStacktrace) {
                    e.printStackTrace()
                }
                errorMsg = e.toString()
                cancel(true)
            } finally {
                bitmap = null
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        private fun saveScopedStorage() {
            path =
                if (path != null) (Environment.DIRECTORY_PICTURES + File.separator + path) else Environment.DIRECTORY_PICTURES
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path)
            contentValues.put(
                MediaStore.MediaColumns.MIME_TYPE,
                QuickShotUtils.getMimeType(fileExtension)
            )
            val resolver = weakContext.get()!!.getContentResolver()
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri == null) {
                errorMsg = String.format(
                    "Couldn't insert ContentValues with data: [%s] into the ContentResolver",
                    contentValues.toString()
                )
                cancel(true)
                return
            }
            try {
                resolver.openOutputStream(imageUri).use { out ->
                    when (fileExtension) {
                        EXTENSION_JPG -> bitmap!!.compress(
                            Bitmap.CompressFormat.JPEG,
                            jpgQuality,
                            out!!
                        )

                        EXTENSION_PNG -> bitmap!!.compress(Bitmap.CompressFormat.PNG, 0, out!!)
                    }
                    file = File(path, filename + fileExtension)
                }
            } catch (e: Exception) {
                if (printStacktrace) {
                    e.printStackTrace()
                }
                errorMsg = e.toString()
                resolver.delete(imageUri, null, null)
                cancel(true)
            } finally {
                bitmap = null
            }
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            if (QuickShotUtils.isAboveAPI29) {
                saveScopedStorage()
            } else {
                saveLegacy()
            }
            return null
        }

        override fun onPostExecute(v: Void?) {
            listener.onQuickShotSuccess(file!!.getAbsolutePath())
            if (!QuickShotUtils.isAboveAPI29) {
                MediaScannerConnection.scanFile(
                    weakContext.get(),
                    arrayOf<String>(file!!.getAbsolutePath()),
                    null,
                    null
                )
            }
        }

        override fun onCancelled() {
            mainThreadHandler.post(object : Runnable {
                override fun run() {
                    listener.onQuickShotFailed(file!!.getAbsolutePath(), errorMsg)
                }
            })
        }
    }

    companion object {
        private const val EXTENSION_JPG = ".jpg"
        private const val EXTENSION_PNG = ".png"
        private const val EXTENSION_NOMEDIA = ".nomedia"
        private const val JPG_MAX_QUALITY = 100

        fun of(view: View): QuickShot {
            return QuickShot(view)
        }

        fun of(bitmap: Bitmap, context: Context): QuickShot {
            return QuickShot(bitmap, context)
        }
    }
}

