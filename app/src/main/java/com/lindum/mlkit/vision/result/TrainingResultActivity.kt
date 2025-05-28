package com.lindum.mlkit.vision.result

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.lindum.mlkit.vision.CameraXLivePreviewActivity
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.TrainingResultBinding
import com.lindum.mlkit.vision.entity.WrongPoseModel
import com.lindum.mlkit.vision.utils.FileUtils
import com.lindum.mlkit.vision.utils.StatusBarUtils
import com.lindum.mlkit.vision.utils.TemporaryDataHolder
import com.orhanobut.logger.Logger
import com.xuexiang.xui.widget.progress.CircleProgressView.CircleProgressUpdateListener
import com.xuexiang.xui.widget.progress.HorizontalProgressView.HorizontalProgressUpdateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * @author MarkGosling
 * @date 2022/1/27 8:25
 */
class TrainingResultActivity : AppCompatActivity(), HorizontalProgressUpdateListener,
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

    // 错误动作数
    var countWrongImage: Int = 0
    private var listView: ListView? = null
    private lateinit var binding: TrainingResultBinding

    // 添加 WrongPoseModel 列表
    private var wrongPoseModelList = mutableListOf<WrongPoseModel>()
    private val party by lazy {
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3) // 从屏幕水平居中 30% 高度位置发射
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TrainingResultBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(binding.root)


        val layoutParams = binding.vFakeStatusBar.layoutParams
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this)
        binding.vFakeStatusBar.layoutParams = layoutParams

        val intent = getIntent()

        if (intent.getBooleanExtra("hasWrongPoseList", false)) {
            // 从临时存储中获取错误姿势列表而不是从 intent 中获取
            wrongPoseModelList = TemporaryDataHolder.getWrongPoseModels()!!.toMutableList()
            // 获取运动总时长
            trainingTotalTimes = intent.getIntExtra("totalTrainingTime", 0)
            // 获取动作组数
            groupsOfPose = intent.getIntExtra("groupsOfPose", 0)
            // 记录训练日记
            FileUtils.writeStringToFile(
                CameraXLivePreviewActivity.timeMarkPoint.toString() + " 完成训练",
                MainActivity.Companion.appFilePath + "/files/analysisReport/",
                "recordFile.sys",
                FileUtils.APPEND_TEXT
            )

            // 设置错误动作数量
            countWrongImage = wrongPoseModelList.size

            // 初始化 jsonResultEntitiesArrayList
            jsonResultEntitiesArrayList = ArrayList()

            // 在后台线程中处理所有图片保存
            CoroutineScope(Dispatchers.Main).launch {
                // 创建图片保存目录
                withContext(Dispatchers.IO) {
                    val imageDirPath = CameraXLivePreviewActivity.imageSavePath + "/images"
                    val imageDir = File(imageDirPath)
                    if (!imageDir.exists()) {
                        imageDir.mkdirs()
                    }
                }

                // 处理每个错误姿势
                for (wrongPose in wrongPoseModelList) {
                    val jsonResultEntity = JSONResultEntity()
                    jsonResultEntity.poseName = wrongPose.poseName
                    jsonResultEntity.tips = wrongPose.poseTips

                    // 保存错误姿势图片到文件，并设置路径
                    if (wrongPose.wrongPoseImage != null) {
                        try {
                            // 在IO线程中执行图片保存
                            val imagePath = withContext(Dispatchers.IO) {
                                // 生成唯一文件名
                                val fileName =
                                    "wrong_pose_${System.currentTimeMillis()}_${wrongPose.poseIndex}.jpg"
                                val imageDirPath =
                                    CameraXLivePreviewActivity.imageSavePath + "/images"
                                val filePath = "$imageDirPath/$fileName"
                                val imageFile = File(filePath)

                                // 将图片保存到文件 - 在IO线程中执行
                                val outputStream = FileOutputStream(imageFile)
                                wrongPose.wrongPoseImage!!.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    90,
                                    outputStream
                                )
                                outputStream.flush()
                                outputStream.close()

                                Logger.e("保存错误姿势图片: $filePath")
                                return@withContext filePath
                            }

                            // 设置图片路径
                            jsonResultEntity.wrongPoseImagePath = imagePath
                        } catch (e: Exception) {
                            Logger.e("保存错误姿势图片失败: ${e.message}")
                            e.printStackTrace()
                            jsonResultEntity.wrongPoseImagePath = ""
                        }
                    } else {
                        jsonResultEntity.wrongPoseImagePath = ""
                    }

                    jsonResultEntitiesArrayList!!.add(jsonResultEntity)
                }

                // 保存训练结果到本地
                withContext(Dispatchers.IO) {
                    saveTrainingResultToLocal()
                }
            }

            // 初始化列表视图
            listView = findViewById(R.id.resultList)

            // 创建适配器，这里需要编写一个新的适配器来显示 WrongPoseModel 数据
            val resultListAdapter =
                WrongPoseListAdapter(this, R.layout.result_list_item, wrongPoseModelList)
            listView!!.adapter = resultListAdapter

            // 设置列表项点击事件
            listView?.onItemClickListener =
                AdapterView.OnItemClickListener { _, view, position, _ ->
                    // 错误姿势图片点击查看大图
                    val wrongImageView = view.findViewById<ImageView>(R.id.wrongPoseImage)
                    wrongImageView.setOnClickListener {
                        wrongImageView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(wrongImageView.drawingCache)
                        val intent1 = Intent(this@TrainingResultActivity, ImageDetails::class.java)
                        ImageDetails.bitmap = bitmap
                        wrongImageView.isDrawingCacheEnabled = false
                        startActivity(intent1)
                    }

                    // 正确姿势图片点击查看大图
                    val rightImageView = view.findViewById<ImageView>(R.id.rightPoseImage)
                    rightImageView.setOnClickListener {
                        rightImageView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(rightImageView.drawingCache)
                        val intent1 = Intent(this@TrainingResultActivity, ImageDetails::class.java)
                        ImageDetails.bitmap = bitmap
                        rightImageView.isDrawingCacheEnabled = false
                        startActivity(intent1)
                    }
                }

        } else if (intent.getIntExtra("init", 0) == 0) {
            // 旧的初始化代码保持不变
            // 获取运动总时长
            trainingTotalTimes = intent.getIntExtra("totalTrainingTime", 0)
            // 获取动作组数
            groupsOfPose = intent.getIntExtra("groupsOfPose", 0)
            // 记录训练日记
            FileUtils.writeStringToFile(
                CameraXLivePreviewActivity.timeMarkPoint.toString() + " 完成训练",
                MainActivity.Companion.appFilePath + "/files/analysisReport/",
                "recordFile.sys",
                FileUtils.APPEND_TEXT
            )
            Logger.e("分析界面初始化数据")
            resultEntityList = ArrayList()
            for (tips in resultTips) {
                var tips = tips
                val index: Int = resultTips.indexOf(tips)
                tips = index.toString() + tips
                if (poseResultMap[tips] == null) {
                    poseResultMap.put(tips, 1)
                } else {
                    poseResultMap.replace(tips, poseResultMap[tips]!! + 1)
                }
            }
            val list: MutableList<MutableMap.MutableEntry<String?, Int?>> =
                ArrayList(poseResultMap.entries)
            list.sortWith(compareBy { it.value })
            list.reverse()
            val map2: MutableMap<String?, Int?> = LinkedHashMap()
            for (entry in list) {
                map2.put(entry.key, entry.value)
            }
            map2.forEach { (o1: String?, o2: Int?) ->
                if (o1!!.length > 3) {
                    val index = o1.split("，".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0].toInt()
                    var commonMistakes: String? = ""
                    var solveMethod = ""
                    val poseIndex = getPoseIndexByName(resultName[index])
                    if (poseIndex != -1 && 
                        (MainActivity.Companion.poseEntityArrayList!![poseIndex].commonMistakes?.size ?: 0) > 1
                    ) {
                        commonMistakes =
                            MainActivity.Companion.poseEntityArrayList!![poseIndex].commonMistakes?.get(0)
                        solveMethod =
                            MainActivity.Companion.poseEntityArrayList!![poseIndex].commonMistakes?.get(1)!!.replace("解决：", "")
                    } else {
                        Logger.e("找不到姿势或姿势没有足够的错误提示: ${resultName[index]}, index: $poseIndex")
                    }
                    var rightPoseImage: Bitmap? = null
                    for (poseEntity in MainActivity.Companion.poseEntityArrayList!!) {
                        if (poseEntity.poseName === resultName[index]) {
                            val indexOfPoseEntityList =
                                MainActivity.Companion.poseEntityArrayList!!.indexOf(poseEntity)
                            rightPoseImage =
                                MainActivity.Companion.poseRightImageArrayList[indexOfPoseEntityList]
                        }
                    }
                    val temp = ResultEntity()
                    //                    ResultEntity temp = new ResultEntity(resultName.get(index), "出错" + o2 + "次\n" + commonMistakes + "。" + "\nAi建议：" + "\n" + solveMethod + "。" + resultTips.get(index).substring(1) + "\n", resultImage.get(index), rightPoseImage);
                    temp.poseName = resultName[index]
                    temp.tips =
                        "该错误出错" + o2 + "次\n" + commonMistakes + "。" + "\nAi建议：" + "\n" + solveMethod + "。" + resultTips[index]!!
                            .substring(1) + "\n"
                    temp.wrongPoseImage = resultImage[index]
                    temp.rightPoseImage = rightPoseImage
                    temp.wrongPoseImagePath = wrongImagePath[index]
                    Logger.e("出错图片路径" + wrongImagePath[index])
                    resultEntityList!!.add(temp)
                }
            }
            countWrongImage = resultImage.size

            // 原始代码中的列表适配器初始化
            listView = findViewById(R.id.resultList)
            val resultListAdapter =
                ResultListAdapter(this, R.layout.result_list_item, resultEntityList)
            listView!!.setAdapter(resultListAdapter)
            listView?.onItemClickListener =
                AdapterView.OnItemClickListener { _, view, _, l ->
                    val wrongImageView = view.findViewById<ImageView>(R.id.wrongPoseImage)
                    wrongImageView.setOnClickListener {
                        wrongImageView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(wrongImageView.drawingCache)
                        Logger.e("这是第" + l + "张图片")
                        val intent1 = Intent(this@TrainingResultActivity, ImageDetails::class.java)
                        ImageDetails.bitmap = bitmap
                        wrongImageView.isDrawingCacheEnabled = false
                        startActivity(intent1)
                    }
                    val rightImageView = view.findViewById<ImageView>(R.id.rightPoseImage)
                    rightImageView.setOnClickListener {
                        rightImageView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(rightImageView.drawingCache)
                        Logger.e("这是第" + l + "张图片")
                        val intent1 = Intent(this@TrainingResultActivity, ImageDetails::class.java)
                        ImageDetails.bitmap = bitmap
                        rightImageView.isDrawingCacheEnabled = false
                        startActivity(intent1)
                    }
                }

        } else {
            // 从已保存的数据文件加载
            Logger.e("===开始从历史记录加载数据，路径: " + intent.getStringExtra("poseDataPath"))
            try {
                val dataFile = intent.getStringExtra("poseDataPath") + "/resultData.sys"
                Logger.e("尝试读取数据文件: $dataFile")
                val dataContent = FileUtils.readFileToString(
                    intent.getStringExtra("poseDataPath") + "/",
                    "resultData.sys"
                )
                
                if (dataContent.isEmpty()) {
                    Logger.e("数据文件为空或不存在")
                    return
                }
                
                val data = dataContent[0]
                Logger.e("成功读取数据: $data")
                
                val dataParts = data.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                Logger.e("数据分段数量: ${dataParts.size}")
                
                if (dataParts.size >= 3) {
                    trainingTotalTimes = dataParts[0].toInt()
                    groupsOfPose = dataParts[1].toInt()
                    countWrongImage = dataParts[2].toInt()
                    Logger.e("解析数据成功: 时长=$trainingTotalTimes, 组数=$groupsOfPose, 错误数=$countWrongImage")
                } else {
                    Logger.e("数据格式不正确")
                }
            } catch (e: Exception) {
                Logger.e("读取数据文件出错: ${e.message}")
                e.printStackTrace()
            }

            // 确保resultEntityList已经被正确初始化
            if (resultEntityList == null || resultEntityList!!.isEmpty()) {
                Logger.e("从历史记录加载，但结果列表为空，尝试从文件加载")

                // 尝试从文件加载分析报告
                try {
                    val jsonFilePath = intent.getStringExtra("poseDataPath") + "/analysisReport.json"
                    Logger.e("尝试读取JSON分析报告: $jsonFilePath")
                    
                    val jsonContent = FileUtils.readFileToString(
                        intent.getStringExtra("poseDataPath") + "/",
                        "analysisReport.json"
                    )

                    if (jsonContent.isEmpty()) {
                        Logger.e("JSON分析报告为空或不存在")
                    } else {
                        // 解析JSON数据
                        try {
                            Logger.e("JSON内容: ${jsonContent[0].substring(0, Math.min(100, jsonContent[0].length))}...")
                            val jsonResultList = DotTimeLineAdapter.convertJSON2ArrayList(jsonContent[0])
                            Logger.e("成功解析JSON数据，结果数量: ${jsonResultList.size}")

                            // 初始化结果实体列表
                            resultEntityList = ArrayList()

                            // 处理每个JSON结果实体
                            for (jsonResult in jsonResultList) {
                                val resultEntity = ResultEntity()
                                resultEntity.poseName = jsonResult.poseName
                                resultEntity.tips = jsonResult.tips
                                resultEntity.wrongPoseImagePath = jsonResult.wrongPoseImagePath
                                
                                Logger.e("处理姿势: ${jsonResult.poseName}, 图片路径: ${jsonResult.wrongPoseImagePath}")

                                // 加载错误姿势图片
                                if (jsonResult.wrongPoseImagePath != null && jsonResult.wrongPoseImagePath.isNotEmpty()) {
                                    try {
                                        val wrongImageFile = File(jsonResult.wrongPoseImagePath)
                                        Logger.e("尝试加载错误姿势图片: ${jsonResult.wrongPoseImagePath}")
                                        if (wrongImageFile.exists()) {
                                            val inputStream = FileInputStream(wrongImageFile)
                                            val wrongImage = BitmapFactory.decodeStream(inputStream)
                                            if (wrongImage != null) {
                                                resultEntity.wrongPoseImage = wrongImage
                                                Logger.e("成功加载错误姿势图片")
                                            } else {
                                                Logger.e("解码错误姿势图片失败")
                                            }
                                            inputStream.close()
                                        } else {
                                            Logger.e("错误姿势图片文件不存在: ${jsonResult.wrongPoseImagePath}")
                                        }
                                    } catch (e: Exception) {
                                        Logger.e("加载错误姿势图片失败: ${e.message}")
                                        e.printStackTrace()
                                    }
                                } else {
                                    Logger.e("错误姿势图片路径为空或无效")
                                }

                                // 加载正确姿势图片
                                for (poseEntity in MainActivity.Companion.poseEntityArrayList!!) {
                                    if (poseEntity.poseName == jsonResult.poseName) {
                                        val indexOfPoseEntityList =
                                            MainActivity.Companion.poseEntityArrayList!!.indexOf(poseEntity)

                                        Logger.e("正确姿势图片索引: $indexOfPoseEntityList, 姿势名称: ${poseEntity.poseName}")
                                        Logger.e("poseRightImageArrayList大小: ${MainActivity.Companion.poseRightImageArrayList.size}")

                                        if (indexOfPoseEntityList < MainActivity.Companion.poseRightImageArrayList.size) {
                                            resultEntity.rightPoseImage =
                                                MainActivity.Companion.poseRightImageArrayList[indexOfPoseEntityList]

                                            if (resultEntity.rightPoseImage != null) {
                                                Logger.e("成功加载正确姿势图片")
                                            } else {
                                                Logger.e("正确姿势图片为空")
                                            }
                                        } else {
                                            Logger.e("正确姿势图片索引越界: $indexOfPoseEntityList >= ${MainActivity.Companion.poseRightImageArrayList.size}")
                                        }
                                        break
                                    }
                                }

                                resultEntityList!!.add(resultEntity)
                            }
                        } catch (e: Exception) {
                            Logger.e("解析JSON数据出错: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("读取JSON分析报告出错: ${e.message}")
                    e.printStackTrace()
                }
            }

            // 初始化原始代码中的列表适配器
            listView = findViewById<ListView>(R.id.resultList)
            resultEntityList?.forEach {
                val poseIndex = getPoseIndexByName(it.poseName)
                if (poseIndex != -1 && poseIndex < MainActivity.Companion.poseRightImageArrayList.size) {
                    it.rightPoseImage = MainActivity.Companion.poseRightImageArrayList[poseIndex]
                } else {
                    Logger.e("找不到姿势或索引越界: ${it.poseName}, index: $poseIndex")
                }
            }
            val resultListAdapter =
                ResultListAdapter(this, R.layout.result_list_item, resultEntityList)
            listView!!.setAdapter(resultListAdapter)
            listView?.onItemClickListener =
                AdapterView.OnItemClickListener { _, view, _, l ->
                    val wrongImageView = view.findViewById<ImageView>(R.id.wrongPoseImage)
                    wrongImageView.setOnClickListener {
                        wrongImageView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(wrongImageView.drawingCache)
                        Logger.e("这是第" + l + "张图片")
                        val intent1 = Intent(this@TrainingResultActivity, ImageDetails::class.java)
                        ImageDetails.bitmap = bitmap
                        wrongImageView.isDrawingCacheEnabled = false
                        startActivity(intent1)
                    }
                    val rightImageView = view.findViewById<ImageView>(R.id.rightPoseImage)
                    rightImageView.setOnClickListener {
                        rightImageView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(rightImageView.drawingCache)
                        Logger.e("这是第" + l + "张图片")
                        val intent1 = Intent(this@TrainingResultActivity, ImageDetails::class.java)
                        ImageDetails.bitmap = bitmap
                        rightImageView.isDrawingCacheEnabled = false
                        startActivity(intent1)
                    }
                }
        }

        // ResultEntity 转 JSONResultEntity - 保留原有逻辑，如果使用新的 WrongPoseModel 则在上面已生成
        if (jsonResultEntitiesArrayList == null) {
            jsonResultEntitiesArrayList = ArrayList<JSONResultEntity?>()
            for (resultEntity in resultEntityList!!) {
                val jsonResultEntity = JSONResultEntity()
                jsonResultEntity.poseName = resultEntity.poseName
                jsonResultEntity.tips = resultEntity.tips
                jsonResultEntity.wrongPoseImagePath = resultEntity.wrongPoseImagePath
                jsonResultEntitiesArrayList!!.add(jsonResultEntity)
            }
        }

        //  初始化进度控件
        binding.hpvTime.setProgressViewUpdateListener(this)
        binding.hpvGroup.setProgressViewUpdateListener(this)
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

        // 如果是新创建的训练结果，保存到本地
        if (intent.getIntExtra("init", 0) == 0 || intent.getBooleanExtra(
                "hasWrongPoseList",
                false
            )
        ) {
            // 将训练结果保存到本地
            saveTrainingResultToLocal()

            // 清除文件缓存，确保日记页面能立即看到新记录
            FileUtils.clearFileCache()
        }
    }

    /**
     * 将训练结果保存到本地
     */
    fun saveTrainingResultToLocal() {
        try {
            val resultDataPath = CameraXLivePreviewActivity.imageSavePath + "/resultData.sys"
            Logger.e("开始保存训练结果到: $resultDataPath")
            
            FileUtils.createFile(
                resultDataPath,
                FileUtils.CREATE_FILE
            )
            // 将评分写入本地文件 时长 动作组数
            val resultData = "$trainingTotalTimes $groupsOfPose $countWrongImage"
            Logger.e("保存结果数据: $resultData")
            
            FileUtils.writeStringToFile(
                resultData,
                CameraXLivePreviewActivity.imageSavePath + "/",
                "resultData.sys",
                FileUtils.OVERWRITE_TEXT
            )
            
            // 将动作分析结果保存到本地文件
            val jsonPath = CameraXLivePreviewActivity.imageSavePath + "/analysisReport.json"
            val jsonData = JSON.toJSONString(jsonResultEntitiesArrayList)
            Logger.e("保存JSON分析报告到: $jsonPath, 数据长度: ${jsonData.length}")
            
            FileUtils.writeStringToFile(
                jsonData,
                CameraXLivePreviewActivity.imageSavePath + "/",
                "analysisReport.json",
                FileUtils.OVERWRITE_TEXT
            )
            
            Logger.e("训练结果保存完成")
        } catch (e: Exception) {
            Logger.e("保存训练结果出错: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 通过动作名称，查找该动作在动作库的下标
     *
     * @param poseName 动作名称
     * @return 动作下标
     */
    fun getPoseIndexByName(poseName: String?): Int {
        for (poseEntity in MainActivity.Companion.poseEntityArrayList!!) {
            val index = MainActivity.Companion.poseEntityArrayList!!.indexOf(poseEntity)
            if (poseEntity.poseName == poseName) {
                return index
            }
        }
        return -1
    }


    /**
     * 设置进度条的数值
     */
    fun startProcessBar() {
        // 动作组数
        val poseGroups = MainActivity.Companion.poseEntityArrayList!!.size
        val groupsRate = (100 * (groupsOfPose.toFloat() / poseGroups)).toInt()
        // 设置动作组数进度条
        binding.hpvGroup.setEndProgress(groupsRate.toFloat())
        binding.hpvGroup.startProgressAnimation()

        // 时长
        var timeRate =
            (100 * (trainingTotalTimes.toFloat() / (poseGroups * (MainActivity.Companion.poseEntityArrayList!![0]
                .poseProcessValue + 4)))).toInt()
        if (timeRate > 100) {
            timeRate = 100
        }
        binding.hpvTime.setEndProgress(timeRate.toFloat())
        binding.hpvTime.startProgressAnimation()

        // 错误动作数
        var wrongRate: Int
        wrongRate = if (groupsOfPose == 0) {
            ((countWrongImage.toFloat() / ((groupsOfPose + 1) * 20)) * 100).toInt()
        } else {
            ((countWrongImage.toFloat() / ((groupsOfPose) * 20)) * 100).toInt()
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
        if (view.id == R.id.progressView_circle_main) {
            binding.progressTextMain.text = progressInt.toString() + ""
        }
    }


    /**
     * 卡路里进度条文本
     *
     * @param view     水平进度条布局
     * @param progress 实时更新的进度
     */
    @SuppressLint("SetTextI18n", "NonConstantResourceId")
    override fun onHorizontalProgressUpdate(view: View, progress: Float) {
        when (view.id) {
            R.id.hpv_time -> binding.progressTextTime.text = costTime
            R.id.hpv_wrong -> binding.progressTextWrong.text = "$countWrongImage 个"
            R.id.hpv_group -> binding.progressTextGroup.text = "$groupsOfPose 组"
            else -> {}
        }
    }

    override fun onHorizontalProgressFinished(view: View?) {
    }

    override fun onCircleProgressFinished(view: View?) {
        // 当环形进度条动画完成后，如果分数是100分，启动烟花特效
        if (view?.id == R.id.progressView_circle_main) {
            val poseGroups = MainActivity.Companion.poseEntityArrayList!!.size
            val scores = 60 + (40 * (1 - (countWrongImage.toFloat() / (20 * poseGroups)))).toInt()
            if (scores == 100) {
                binding.konfettiView.post {
                    binding.konfettiView.start(party)
                }
            }
        }
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
