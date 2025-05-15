package com.lindum.mlkit.vision.dev.result

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.TrainingResultBinding
import com.lindum.mlkit.vision.dev.utils.FileUtils
import com.lindum.mlkit.vision.dev.view.HomePage
import com.orhanobut.logger.Logger
import com.xuexiang.xui.widget.progress.CircleProgressView
import com.xuexiang.xui.widget.progress.CircleProgressView.CircleProgressUpdateListener
import com.xuexiang.xui.widget.progress.HorizontalProgressView
import com.xuexiang.xui.widget.progress.HorizontalProgressView.HorizontalProgressUpdateListener
import java.util.Collections
import java.util.Map
import java.util.function.Function

/**
 * @author MarkGosling
 * @date 2022/1/27 8:25
 */
class TrainingResult : AppCompatActivity(), HorizontalProgressUpdateListener,
    CircleProgressUpdateListener {
    // 要存储到本地的分析结果
    private var jsonResultEntitiesArrayList: ArrayList<JSONResultEntity?>? = null
    private val poseResultMap: MutableMap<String?, Int?> = HashMap<String?, Int?>()

    // 运动总时长
    var trainingTotalTimes: Int = 0

    // 动作组数进度条占比
    var groupsOfPose: Int = 0

    // 消耗的时间转分秒
    var costTime: String? = null

    // 消耗的卡路里
    var calSum: Double = 0.0

    // 错误动作数
    var countWrongImage: Int = 0
    private var listView: ListView? = null
    private lateinit var binding: TrainingResultBinding

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TrainingResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = getIntent()
        if (intent.getIntExtra("init", 0) == 0) {
            // 获取运动总时长
            trainingTotalTimes = intent.getIntExtra("totalTrainingTime", 0)
            // 获取动作组数
            groupsOfPose = intent.getIntExtra("groupsOfPose", 0)
            // 记录训练日记
            FileUtils.writeStringToFile(
                CameraXLivePreviewActivity.timeMarkPoint.toString() + " 完成训练",
                HomePage.appFilePath + "/files/analysisReport/",
                "recordFile.sys",
                FileUtils.APPEND_TEXT
            )
            Logger.e("分析界面初始化数据")
            resultEntityList = ArrayList<ResultEntity>()
            for (tips in resultTips) {
                var tips = tips
                val index: Int = resultTips.indexOf(tips)
                tips = index.toString() + tips
                if (poseResultMap.get(tips) == null) {
                    poseResultMap.put(tips, 1)
                } else {
                    poseResultMap.replace(tips, poseResultMap[tips]!! + 1)
                }
            }
            val list: MutableList<MutableMap.MutableEntry<String?, Int?>> =
                ArrayList<MutableMap.MutableEntry<String?, Int?>>(poseResultMap.entries)
            list.sortWith(compareBy { it.value })
            list.reverse()
            val map2: MutableMap<String?, Int?> = LinkedHashMap<String?, Int?>()
            for (entry in list) {
                map2.put(entry.key, entry.value)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                map2.forEach { (o1: String?, o2: Int?) ->
                    if (o1!!.length > 3) {
                        val index = o1.split("，".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0].toInt()
                        var commonMistakes: String? = ""
                        var solveMethod = ""
                        if (HomePage.poseEntityArrayList.get(getPoseIndexByName(resultName.get(index)))
                                .getCommonMistakes().size > 1
                        ) {
                            commonMistakes = HomePage.poseEntityArrayList.get(
                                getPoseIndexByName(
                                    resultName.get(index)
                                )
                            ).getCommonMistakes()[0]
                            solveMethod = HomePage.poseEntityArrayList.get(
                                getPoseIndexByName(
                                    resultName.get(index)
                                )
                            ).getCommonMistakes()[1].replace("解决：", "")
                        }
                        var rightPoseImage: Bitmap? = null
                        for (poseEntity in HomePage.poseEntityArrayList) {
                            if (poseEntity.getPoseName() === resultName.get(index)) {
                                val indexOfPoseEntityList =
                                    HomePage.poseEntityArrayList.indexOf(poseEntity)
                                rightPoseImage =
                                    HomePage.poseRightImageArrayList.get(indexOfPoseEntityList)
                            }
                        }
                        val temp = ResultEntity()
                        //                    ResultEntity temp = new ResultEntity(resultName.get(index), "出错" + o2 + "次\n" + commonMistakes + "。" + "\nAi建议：" + "\n" + solveMethod + "。" + resultTips.get(index).substring(1) + "\n", resultImage.get(index), rightPoseImage);
                        temp.setPoseName(resultName.get(index))
                        temp.setTips(
                            "该错误出错" + o2 + "次\n" + commonMistakes + "。" + "\nAi建议：" + "\n" + solveMethod + "。" + resultTips.get(
                                index
                            )!!
                                .substring(1) + "\n"
                        )
                        temp.setWrongPoseImage(resultImage.get(index))
                        temp.setRightPoseImage(rightPoseImage)
                        temp.setWrongPoseImagePath(wrongImagePath.get(index))
                        Logger.e("出错图片路径" + wrongImagePath.get(index))
                        resultEntityList!!.add(temp)
                    }
                }
                countWrongImage = resultImage.size
            }
        } else {
            Logger.e("===" + intent.getStringExtra("poseDataPath"))
            val data = FileUtils.readFileToString(
                intent.getStringExtra("poseDataPath") + "/",
                "resultData.sys"
            ).get(0)
            trainingTotalTimes =
                data.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
            groupsOfPose =
                data.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
            countWrongImage =
                data.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2].toInt()
        }
        // ResultEntity 转 JSONResultEntity
        jsonResultEntitiesArrayList = ArrayList<JSONResultEntity?>()
        for (resultEntity in resultEntityList!!) {
            val jsonResultEntity = JSONResultEntity()
            jsonResultEntity.setPoseName(resultEntity.getPoseName())
            jsonResultEntity.setTips(resultEntity.getTips())
            jsonResultEntity.setWrongPoseImagePath(resultEntity.getWrongPoseImagePath())
            jsonResultEntitiesArrayList!!.add(jsonResultEntity)
        }
        // 初始化控件
        listView = findViewById<ListView>(R.id.resultList)
        // 动作分析列表初始化
        val resultListAdapter = ResultListAdapter(this, R.layout.result_list_item, resultEntityList)
        listView!!.setAdapter(resultListAdapter)
        listView!!.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
            val wrongImageView = view!!.findViewById<ImageView>(R.id.wrongPoseImage)
            wrongImageView.setOnClickListener(View.OnClickListener { v: View? ->
                wrongImageView.setDrawingCacheEnabled(true)
                val bitmap = Bitmap.createBitmap(wrongImageView.getDrawingCache())
                Logger.e("这是第" + l + "张图片")
                val intent1 = Intent(this@TrainingResult, ImageDetails::class.java)
                ImageDetails.bitmap = bitmap
                wrongImageView.setDrawingCacheEnabled(false)
                startActivity(intent1)
            })
            val rightImageView = view.findViewById<ImageView>(R.id.rightPoseImage)
            rightImageView.setOnClickListener(View.OnClickListener { v: View? ->
                rightImageView.setDrawingCacheEnabled(true)
                val bitmap = Bitmap.createBitmap(rightImageView.getDrawingCache())
                Logger.e("这是第" + l + "张图片")
                val intent1 = Intent(this@TrainingResult, ImageDetails::class.java)
                ImageDetails.bitmap = bitmap
                rightImageView.setDrawingCacheEnabled(false)
                startActivity(intent1)
            })
        })

        //  初始化控件
        binding.hpvTime.setProgressViewUpdateListener(this)
        binding.hpvGroup.setProgressViewUpdateListener(this)
        binding.hpvCal.setProgressViewUpdateListener(this)
        binding.hpvWrong.setProgressViewUpdateListener(this)
        binding.progressViewCircleMain.setGraduatedEnabled(true)
        binding.progressViewCircleMain.setProgressViewUpdateListener(this)


        val minute = (trainingTotalTimes / 60).toString() + "'"
        costTime =
            (if (trainingTotalTimes / 60 == 0) "" else minute) + trainingTotalTimes % 60 + "\""


        binding.progressViewCircleMain.setGraduatedEnabled(true)
        binding.progressViewCircleMain.setProgressViewUpdateListener(this)
        // 初始化进度条
        startProcessBar()
        if (intent.getIntExtra("init", 0) == 0) {
            // 将训练结果保存到本地
            saveTrainingResultToLocal()
        }
    }

    /**
     * 将训练结果保存到本地
     */
    fun saveTrainingResultToLocal() {
        FileUtils.createFile(
            CameraXLivePreviewActivity.imageSavePath + "/resultData.sys",
            FileUtils.CREATE_FILE
        )
        // 将评分写入本地文件 时长 动作组数
        FileUtils.writeStringToFile(
            trainingTotalTimes.toString() + " " + groupsOfPose + " " + countWrongImage,
            CameraXLivePreviewActivity.imageSavePath,
            "resultData.sys",
            FileUtils.OVERWRITE_TEXT
        )
        // 将动作分析结果保存到本地文件
        FileUtils.writeStringToFile(
            JSON.toJSONString(jsonResultEntitiesArrayList),
            CameraXLivePreviewActivity.imageSavePath,
            "analysisReport.json",
            FileUtils.OVERWRITE_TEXT
        )
    }

    /**
     * 通过动作名称，查找该动作在动作库的下标
     *
     * @param poseName 动作名称
     * @return 动作下标
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getPoseIndexByName(poseName: String?): Int {
        for (poseEntity in HomePage.poseEntityArrayList) {
            val index = HomePage.poseEntityArrayList.indexOf(poseEntity)
            if (poseEntity.getPoseName() == poseName) {
                return index
            }
        }
        return -1
    }


    /**
     * 设置进度条的数值
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startProcessBar() {
        // 动作组数
        val poseGroups = HomePage.poseEntityArrayList.size
        val groupsRate = (100 * (groupsOfPose.toFloat() / poseGroups)).toInt()
        // 设置动作组数进度条
        binding.hpvGroup.setEndProgress(groupsRate.toFloat())
        binding.hpvGroup.startProgressAnimation()

        // 时长
        var timeRate =
            (100 * (trainingTotalTimes.toFloat() / (poseGroups * (HomePage.poseEntityArrayList.get(0)
                .getPoseProcessValue() + 4)))).toInt()
        if (timeRate > 100) {
            timeRate = 100
        }
        binding.hpvTime.setEndProgress(timeRate.toFloat())
        binding.hpvTime.startProgressAnimation()

        // 计算卡路里
        Logger.e("动作个数" + HomePage.poseEntityArrayList.size)
        for (i in HomePage.poseEntityArrayList.indices) {
            val poseEntity = HomePage.poseEntityArrayList.get(i)
            if (poseEntity.getCountTimes() != 0) {
                calSum += poseEntity.getPoseBurnCal() * poseEntity.getCountTimes()
            } else {
                calSum += poseEntity.getGetPoseBurnCalPerMin() * poseEntity.getCountMinutes()
            }
        }
        Logger.e("卡路里" + calSum)
        val calRate = (100 * (calSum / 50)).toInt()
        binding.hpvCal.setEndProgress(calRate.toFloat())
        binding.hpvCal.startProgressAnimation()

        // 错误动作数
        var wrongRate: Int
        if (groupsOfPose == 0) {
            wrongRate = ((countWrongImage.toFloat() / ((groupsOfPose + 1) * 20)) * 100).toInt()
        } else {
            wrongRate = ((countWrongImage.toFloat() / ((groupsOfPose) * 20)) * 100).toInt()
        }
        Log.i("test_wrong", wrongRate.toString() + "")
        if (wrongRate > 100) {
            wrongRate = 100
        }
        binding.hpvWrong.setEndProgress(wrongRate.toFloat())
        binding.hpvWrong.startProgressAnimation()
        // 评分
        val scores = 60 + (40 * (1 - (countWrongImage.toFloat() / (20 * poseGroups)))).toInt()
        binding.progressViewCircleMain.setEndProgress(scores.toFloat())
        binding.progressViewCircleMain.startProgressAnimation()
    }

    /**
     * 评分环形进度条数值更新
     *
     * @param view     环形进度条 View
     * @param progress 数值
     */
    @SuppressLint("SetTextI18n")
    override fun onCircleProgressUpdate(view: View, progress: Float) {
        val progressInt = progress.toInt()
        if (view.getId() == R.id.progressView_circle_main) {
            binding.progressTextMain.setText(progressInt.toString() + "")
        }
    }


    /**
     * 卡路里进度条文本
     *
     * @param view     水平进度条布局
     * @param progress 实时更新的进度
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "NonConstantResourceId")
    override fun onHorizontalProgressUpdate(view: View, progress: Float) {
        when (view.id) {
            R.id.hpv_time -> binding.progressTextTime.text = costTime
            R.id.hpv_wrong -> binding.progressTextWrong.text = "$countWrongImage 个"
            R.id.hpv_cal -> binding.progressTextCal.text = calSum.toInt().toString() + " kcal"
            R.id.hpv_group -> binding.progressTextGroup.text = "$groupsOfPose 组"
            else -> {}
        }
    }

    override fun onHorizontalProgressFinished(view: View?) {
    }

    override fun onCircleProgressFinished(view: View?) {
    }

    override fun onHorizontalProgressStart(view: View?) {
    }

    override fun onCircleProgressStart(view: View?) {
    }

    companion object {
        // 展示结果数组
        @JvmField
        var resultEntityList: ArrayList<ResultEntity>? = null

        @JvmField
        var resultName: MutableList<String?> = ArrayList<String?>()

        @JvmField
        var resultImage: MutableList<Bitmap?> = ArrayList<Bitmap?>()

        @JvmField
        var resultTips: MutableList<String?> = ArrayList<String?>()

        @JvmField
        var wrongImagePath: MutableList<String?> = ArrayList<String?>()
    }
}
