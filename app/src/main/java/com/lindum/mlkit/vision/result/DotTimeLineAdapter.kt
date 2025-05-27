package com.lindum.mlkit.vision.result

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import java.util.concurrent.ConcurrentHashMap
import java.lang.ref.WeakReference

internal class DotTimeLineAdapter : RecyclerView.Adapter<DotTimeLineAdapter.ViewHolder?> {
    var mContext: Context
    var mList: MutableList<Event?>? = null
    
    // 缓存已加载的结果数据，避免重复加载
    private val resultCache = ConcurrentHashMap<String, ArrayList<JSONResultEntity>>()
    
    // 缓存格式化的时间字符串，避免重复计算
    private val timeFormatCache = ConcurrentHashMap<Long, String>()
    
    // 缓存已加载的图片，使用弱引用避免内存泄漏
    private val imageCache = ConcurrentHashMap<String, WeakReference<Bitmap>>()

    fun setList(list: MutableList<Event?>) {
        mList = list
        notifyDataSetChanged()
    }

    constructor(context: Context) {
        mContext = context
    }

    constructor(context: Context, list: MutableList<Event?>) {
        mContext = context
        mList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.pop_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = mList?.get(position) ?: return
        val timestamp = event.getTime()
        
        // 使用缓存的时间格式
        var formattedTime = timeFormatCache[timestamp]
        if (formattedTime == null) {
            formattedTime = Util.LongtoStringFormat(1000 * timestamp)
            timeFormatCache[timestamp] = formattedTime
        }
        
        holder.time.text = formattedTime
        holder.textView.text = event.getEvent()
        
        // 添加点击监听事件
        holder.itemView.setOnClickListener { v: View? ->
            val dateFormatted = formattedTime
                .split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0].replace(".", "-")
            
            val poseDataPath = "${MainActivity.Companion.appFilePath}/files/analysisReport/$dateFormatted/$timestamp"
            
            if (File(poseDataPath).exists() && File("$poseDataPath/analysisReport.json").exists()) {
                Logger.e("动作数据存在")
                
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
            }
        }
    }
    
    // 异步加载结果实体
    private suspend fun loadResultEntities(poseDataPath: String): ArrayList<ResultEntity> = withContext(Dispatchers.IO) {
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
                        if (indexOfPoseEntityList < MainActivity.Companion.poseRightImageArrayList.size) {
                            rightPoseImage = MainActivity.Companion.poseRightImageArrayList[indexOfPoseEntityList]
                            // 缓存图片
                            imageCache[rightImageCacheKey] = WeakReference(rightPoseImage)
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
            } else {
                val wrongImageFile = File(wrongImagePath)
                try {
                    val inputStream: InputStream = FileInputStream(wrongImageFile)
                    val wrongImage = BitmapFactory.decodeStream(inputStream)
                    resultEntity.wrongPoseImage = wrongImage
                    // 缓存图片
                    imageCache[wrongImageCacheKey] = WeakReference(wrongImage)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
            
            resultEntity.tips = temp.tips
            resultEntities.add(resultEntity)
        }
        
        return@withContext resultEntities
    }

    fun convertJSON2ArrayList(jsonContent: String?): ArrayList<JSONResultEntity> {
        if (jsonContent.isNullOrEmpty()) {
            return ArrayList()
        }
        
        try {
            val arrayList = JSONObject.parseArray<JSONResultEntity?>(
                jsonContent,
                JSONResultEntity::class.java
            ) as? ArrayList<JSONResultEntity?>?
            
            return arrayList as? ArrayList<JSONResultEntity> ?: ArrayList()
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var time: TextView = view.findViewById<View?>(R.id.time) as TextView
        var textView: TextView = view.findViewById<View?>(R.id.text) as TextView
        private var loadingView: View? = view.findViewById(R.id.loading_view)
        
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
         * 清除所有缓存
         */
        fun clearAllCaches() {
            // 可以在应用内存不足时调用此方法
        }
    }
}
