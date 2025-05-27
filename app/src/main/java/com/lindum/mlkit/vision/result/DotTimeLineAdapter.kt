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
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader

internal class DotTimeLineAdapter : RecyclerView.Adapter<DotTimeLineAdapter.ViewHolder?> {
    var mContext: Context
    var mList: MutableList<Event?>? = null

    fun setList(list: MutableList<Event?>) {
        mList = list
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
        holder.time.text = Util.LongtoStringFormat(1000 * mList!![position]!!.getTime())
        holder.textView.text = mList!![position]!!.getEvent()
        // 添加点击监听事件
        holder.itemView.setOnClickListener(View.OnClickListener { v: View? ->
            val poseDataPath =
                MainActivity.Companion.appFilePath + "/files/analysisReport/" + Util.LongtoStringFormat(
                    1000 * mList!![position]!!
                        .getTime()
                ).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].replace(
                    ".",
                    "-"
                ) + "/" + mList!!.get(position)!!
                    .getTime() + ""
            if (File(poseDataPath).exists() && File("$poseDataPath/analysisReport.json").exists()) {
                Logger.e("动作数据存在")
                val arrayList =
                    convertJSON2ArrayList(readFile2String("$poseDataPath/analysisReport.json"))
                val resultEntities = ArrayList<ResultEntity>()
                for (temp in arrayList) {
                    val resultEntity = ResultEntity()
                    resultEntity.poseName = temp.poseName
                    // 找到正确的图片
                    var rightPoseImage: Bitmap? = null
                    for (poseEntity in MainActivity.Companion.poseEntityArrayList!!) {
                        if (poseEntity.getPoseName() == temp.poseName) {
                            val indexOfPoseEntityList =
                                MainActivity.Companion.poseEntityArrayList!!.indexOf(poseEntity)
                            rightPoseImage = MainActivity.Companion.poseRightImageArrayList[indexOfPoseEntityList]
                        }
                    }
                    resultEntity.rightPoseImage = rightPoseImage
                    val wrongImageFile = File(temp.wrongPoseImagePath)
                    try {
                        val inputStream: InputStream = FileInputStream(wrongImageFile)
                        resultEntity.wrongPoseImage = BitmapFactory.decodeStream(inputStream)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    resultEntity.tips = temp.tips
                    resultEntities.add(resultEntity)
                }
                TrainingResultActivity.resultEntityList = resultEntities
                val intent = Intent(mContext, TrainingResultActivity::class.java)
                intent.putExtra("init", 1)
                intent.putExtra("poseDataPath", poseDataPath)
                mContext.startActivity(intent)
            }
        })
    }

    fun convertJSON2ArrayList(jsonContent: String?): ArrayList<JSONResultEntity> {
        val arrayList = JSONObject.parseArray<JSONResultEntity?>(
            jsonContent,
            JSONResultEntity::class.java
        ) as ArrayList<JSONResultEntity?>?
        return arrayList as ArrayList<JSONResultEntity>
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var time: TextView = view.findViewById<View?>(R.id.time) as TextView
        var textView: TextView = view.findViewById<View?>(R.id.text) as TextView
    }

    companion object {
        /**
         * 将文件读取到字符串
         *
         * @param filePath 文件路径
         * @return String 返回文件字符串内容
         */
        fun readFile2String(filePath: String?): String {
            var fileContent = ""
            //文件逐行读入
            try {
                val br = BufferedReader(
                    InputStreamReader(
                        FileInputStream(filePath)
                    )
                )
                var linestr: String //按行读取 将每次读取一行的结果赋值给linestr
                while ((br.readLine().also { linestr = it }) != null) {
                    fileContent = fileContent + linestr
                }
                br.close() //关闭IO
            } catch (e: Exception) {
                println("文件操作失败")
                e.printStackTrace()
            }
            return fileContent
        }
    }
}
