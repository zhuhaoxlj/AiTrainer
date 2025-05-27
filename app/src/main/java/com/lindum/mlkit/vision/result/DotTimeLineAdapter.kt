package com.lindum.mlkit.vision.result

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSONObject
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.orhanobut.logger.Logger
import com.vivian.timelineitemdecoration.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

internal class DotTimeLineAdapter : RecyclerView.Adapter<DotTimeLineAdapter.ViewHolder?> {
    var mContext: Context
    var mList: MutableList<Event?>? = null
    private val TAG = "DotTimeLineAdapter"

    // 缓存已加载的结果数据，避免重复加载
    private val resultCache = ConcurrentHashMap<String, ArrayList<JSONResultEntity>>()

    // 缓存格式化的时间字符串，避免重复计算
    private val timeFormatCache = ConcurrentHashMap<Long, String>()

    // 缓存已加载的图片，使用弱引用避免内存泄漏
    private val imageCache = ConcurrentHashMap<String, WeakReference<Bitmap>>()

    // 布局加载器缓存
    private var layoutInflater: LayoutInflater? = null

    fun setList(list: MutableList<Event?>) {
        mList = list
        notifyDataSetChanged()
    }

    constructor(context: Context) {
        mContext = context
        layoutInflater = LayoutInflater.from(context)
    }

    constructor(context: Context, list: MutableList<Event?>) {
        mContext = context
        mList = list
        layoutInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "onCreateViewHolder: start")

        // 使用缓存的LayoutInflater以提高性能
        val inflater = layoutInflater ?: LayoutInflater.from(mContext).also { layoutInflater = it }

        // 使用布局加载标志来优化性能
        val view = inflater.inflate(R.layout.pop_item, parent, false)
        val holder = ViewHolder(view)

        Log.d(TAG, "onCreateViewHolder: end, took ${System.currentTimeMillis() - startTime}ms")
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "onBindViewHolder: start for position $position")

        val event = mList?.get(position) ?: return
        val timestamp = event.getTime()

        // 使用缓存的时间格式
        var formattedTime = timeFormatCache[timestamp]
        if (formattedTime == null) {
            val timeFormatStartTime = System.currentTimeMillis()
            formattedTime = Util.LongtoStringFormat(1000 * timestamp)
            timeFormatCache[timestamp] = formattedTime
            Log.d(
                TAG,
                "onBindViewHolder: time formatting took ${System.currentTimeMillis() - timeFormatStartTime}ms"
            )
        }

        val textSetStartTime = System.currentTimeMillis()
        holder.time.text = formattedTime
        holder.textView.text = event.getEvent()
        val isLastItem = position == ((mList?.size ?: 0) - 1)
        holder.llInfoCard?.visibility = if (isLastItem) View.GONE else View.VISIBLE
        Log.d(TAG, "Position $position: isLastItem=$isLastItem, card visibility=${if (isLastItem) "GONE" else "VISIBLE"}")
        // 设置训练数据
        val scoreValue = event.getScore()
        val duration = event.getDuration()
        val errorCount = event.getErrorCount()

        // 分数
        if (scoreValue > 0) {
            holder.scoreValue.text = scoreValue.toString()
            // 根据分数设置不同颜色
            when {
                scoreValue >= 90 -> {
                    holder.scoreValue.setTextColor("#4CAF50".toColorInt()) // 绿色
                }

                scoreValue >= 75 -> {
                    holder.scoreValue.setTextColor("#2196F3".toColorInt()) // 蓝色
                }

                scoreValue >= 60 -> {
                    holder.scoreValue.setTextColor("#FF9800".toColorInt()) // 橙色
                }

                else -> {
                    holder.scoreValue.setTextColor("#F44336".toColorInt()) // 红色
                }
            }
        } else {
            holder.scoreValue.text = "--"
            Log.d(TAG, "分数无效，显示--")
        }

        // 训练时长
        if (duration > 0) {
            val minutes = duration / 60
            val seconds = duration % 60
            val formattedDuration = if (minutes > 0) {
                "$minutes'$seconds\""
            } else {
                "$seconds\""
            }
            holder.durationValue.text = formattedDuration
            Log.d(TAG, "时长格式化: $duration 秒 -> $formattedDuration")
        } else {
            holder.durationValue.text = "--"
            Log.d(TAG, "时长无效，显示--")
        }

        // 错误次数
        if (errorCount >= 0) {
            holder.errorCountValue.text = errorCount.toString()
            Log.d(TAG, "显示错误次数: $errorCount")
        } else {
            holder.errorCountValue.text = "--"
            Log.d(TAG, "错误次数无效，显示--")
        }

        Log.d(
            TAG,
            "onBindViewHolder: text setting took ${System.currentTimeMillis() - textSetStartTime}ms"
        )

        // 添加点击监听事件 - 使用懒加载方式，只有在实际点击时才执行耗时操作
        val clickListenerStartTime = System.currentTimeMillis()
        holder.itemView.setOnClickListener { v: View? ->
            val poseDataPath = event.getPoseDataPath()

            if (poseDataPath != null && File(poseDataPath).exists() && File("$poseDataPath/analysisReport.json").exists()) {
                Logger.e("动作数据存在: $poseDataPath")

                // 使用协程异步加载数据
                CoroutineScope(Dispatchers.Main).launch {
                    // 显示加载指示器
                    holder.showLoading(true)

                    // 在后台线程加载数据
                    val resultEntities = withContext(Dispatchers.IO) {
                        loadResultEntities(poseDataPath)
                    }

                    // 隐藏加载指示器
                    holder.showLoading(false)

                    // 启动结果界面
                    TrainingResultActivity.resultEntityList = resultEntities
                    val intent = Intent(mContext, TrainingResultActivity::class.java)
                    intent.putExtra("init", 1)
                    intent.putExtra("poseDataPath", poseDataPath)
                    mContext.startActivity(intent)
                }
            } else {
                Log.e(TAG, "动作数据不存在: $poseDataPath")
            }
        }
        Log.d(
            TAG,
            "onBindViewHolder: click listener setup took ${System.currentTimeMillis() - clickListenerStartTime}ms"
        )

        Log.d(
            TAG,
            "onBindViewHolder: end for position $position, total time ${System.currentTimeMillis() - startTime}ms"
        )
    }

    // 异步加载结果实体
    private suspend fun loadResultEntities(poseDataPath: String): ArrayList<ResultEntity> =
        withContext(Dispatchers.IO) {
            val resultEntities = ArrayList<ResultEntity>()

            // 检查缓存中是否已有数据
            val cacheKey = "$poseDataPath/analysisReport.json"
            var arrayList = resultCache[cacheKey]

            // 如果缓存中没有，则从文件加载
            if (arrayList == null) {
                val jsonContent = readFile2String("$poseDataPath/analysisReport.json")
                arrayList = convertJSON2ArrayList(jsonContent)
                // 存入缓存
                resultCache[cacheKey] = arrayList
            }

            // 处理数据
            for (temp in arrayList) {
                val resultEntity = ResultEntity()
                resultEntity.poseName = temp.poseName

                // 找到正确的图片
                var rightPoseImage: Bitmap? = null
                val poseName = temp.poseName

                // 使用缓存的正确姿势图片
                val rightImageCacheKey = "right_$poseName"
                val cachedRightImage = imageCache[rightImageCacheKey]?.get()

                if (cachedRightImage != null) {
                    rightPoseImage = cachedRightImage
                } else {
                    // 如果缓存中没有，则从主列表查找
                    for (poseEntity in MainActivity.Companion.poseEntityArrayList!!) {
                        if (poseEntity.getPoseName() == poseName) {
                            val indexOfPoseEntityList =
                                MainActivity.Companion.poseEntityArrayList!!.indexOf(poseEntity)

                            Log.d(
                                TAG,
                                "正确姿势图片索引: $indexOfPoseEntityList, 姿势名称: $poseName"
                            )
                            Log.d(
                                TAG,
                                "poseRightImageArrayList大小: ${MainActivity.Companion.poseRightImageArrayList.size}"
                            )

                            if (indexOfPoseEntityList < MainActivity.Companion.poseRightImageArrayList.size) {
                                rightPoseImage =
                                    MainActivity.Companion.poseRightImageArrayList[indexOfPoseEntityList]
                                // 缓存图片
                                imageCache[rightImageCacheKey] = WeakReference(rightPoseImage)

                                Log.d(TAG, "成功加载正确姿势图片")
                            } else {
                                Log.e(
                                    TAG,
                                    "正确姿势图片索引越界: $indexOfPoseEntityList >= ${MainActivity.Companion.poseRightImageArrayList.size}"
                                )
                            }
                            break
                        }
                    }
                }
                resultEntity.rightPoseImage = rightPoseImage

                // 加载错误姿势图片
                val wrongImagePath = temp.wrongPoseImagePath
                val wrongImageCacheKey = "wrong_$wrongImagePath"
                val cachedWrongImage = imageCache[wrongImageCacheKey]?.get()

                if (cachedWrongImage != null) {
                    resultEntity.wrongPoseImage = cachedWrongImage
                    Log.d(TAG, "使用缓存的错误姿势图片: $wrongImagePath")
                } else {
                    Log.d(TAG, "尝试加载错误姿势图片: $wrongImagePath")
                    if (wrongImagePath.isNullOrEmpty()) {
                        Log.e(TAG, "错误姿势图片路径为空")
                    } else {
                        val wrongImageFile = File(wrongImagePath)
                        if (!wrongImageFile.exists()) {
                            Log.e(TAG, "错误姿势图片文件不存在: $wrongImagePath")
                        } else {
                            try {
                                val inputStream: InputStream = FileInputStream(wrongImageFile)
                                val wrongImage = BitmapFactory.decodeStream(inputStream)
                                if (wrongImage == null) {
                                    Log.e(TAG, "解码错误姿势图片失败: $wrongImagePath")
                                } else {
                                    resultEntity.wrongPoseImage = wrongImage
                                    // 缓存图片
                                    imageCache[wrongImageCacheKey] = WeakReference(wrongImage)
                                    Log.d(TAG, "成功加载错误姿势图片: $wrongImagePath")
                                }
                                inputStream.close()
                            } catch (e: FileNotFoundException) {
                                Log.e(TAG, "找不到错误姿势图片文件: $wrongImagePath", e)
                                e.printStackTrace()
                            } catch (e: Exception) {
                                Log.e(TAG, "加载错误姿势图片出错: ${e.message}", e)
                                e.printStackTrace()
                            }
                        }
                    }
                }

                resultEntity.tips = temp.tips
                resultEntities.add(resultEntity)
            }

            return@withContext resultEntities
        }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    // 使用高效的ViewHolder模式
    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 直接在构造函数中初始化视图引用，避免重复findViewById
        val time: TextView = view.findViewById(R.id.time)
        val textView: TextView = view.findViewById(R.id.text)
        val scoreValue: TextView = view.findViewById(R.id.score_value)
        val durationValue: TextView = view.findViewById(R.id.duration_value)
        val errorCountValue: TextView = view.findViewById(R.id.error_count_value)
        private val loadingView: View? = view.findViewById(R.id.loading_view)
        val llInfoCard: CardView? = view.findViewById(R.id.training_data_card)

        fun showLoading(show: Boolean) {
            loadingView?.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    companion object {
        /**
         * 将文件读取到字符串
         *
         * @param filePath 文件路径
         * @return String 返回文件字符串内容
         */
        fun readFile2String(filePath: String?): String {
            if (filePath == null) return ""

            var fileContent = ""
            //文件逐行读入
            try {
                val br = BufferedReader(
                    InputStreamReader(
                        FileInputStream(filePath)
                    )
                )
                val stringBuilder = StringBuilder()
                var linestr: String? //按行读取 将每次读取一行的结果赋值给linestr
                while (br.readLine().also { linestr = it } != null) {
                    stringBuilder.append(linestr)
                }
                fileContent = stringBuilder.toString()
                br.close() //关闭IO
            } catch (e: Exception) {
                println("文件操作失败")
                e.printStackTrace()
            }
            return fileContent
        }

        /**
         * 将JSON字符串转换为JSONResultEntity列表
         *
         * @param jsonContent JSON字符串
         * @return ArrayList<JSONResultEntity> 返回JSONResultEntity列表
         */
        fun convertJSON2ArrayList(jsonContent: String?): ArrayList<JSONResultEntity> {
            if (jsonContent.isNullOrEmpty()) {
                return ArrayList()
            }

            try {
                val arrayList = JSONObject.parseArray<JSONResultEntity>(
                    jsonContent,
                    JSONResultEntity::class.java
                )

                // 显式转换为ArrayList<JSONResultEntity>
                val result = ArrayList<JSONResultEntity>()
                if (arrayList != null) {
                    for (item in arrayList) {
                        if (item != null) {
                            result.add(item)
                        }
                    }
                }
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                return ArrayList()
            }
        }

        /**
         * 清除所有缓存
         */
        fun clearAllCaches() {
            // 可以在应用内存不足时调用此方法
        }
    }
}
