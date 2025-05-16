package com.lindum.mlkit.vision.dev

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.lindum.mlkit.vision.dev.databinding.ActivityMainBinding
import com.lindum.mlkit.vision.dev.entity.PoseEntity
import com.lindum.mlkit.vision.dev.entity.UserEntity
import com.lindum.mlkit.vision.dev.fragments.DiaryFragment
import com.lindum.mlkit.vision.dev.fragments.PoseLibraryFragment
import com.lindum.mlkit.vision.dev.sql.ControlTable
import com.lindum.mlkit.vision.dev.utils.FileUtils
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import java.time.LocalDate
import java.util.Objects
import java.util.concurrent.ExecutionException

@RequiresApi(api = Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var poseLibraryFragment: PoseLibraryFragment
    private lateinit var diaryFragment: DiaryFragment
    private var loadingBitmap: Bitmap? = null
    private var currentFragment: Fragment? = null

    // 静态变量
    companion object {
        var userEntity = UserEntity()
        var poseEntityArrayList: MutableList<PoseEntity>? = null
        var poseLibrarySwiperImageArrayList = ArrayList<Bitmap>()
        var poseDetailFeelingImageArrayList = ArrayList<Bitmap>()
        var poseRightImageArrayList = ArrayList<Bitmap>()
        var appFilePath: String = ""
        var today: LocalDate = LocalDate.now()

        // 动作库封面图
        val poseLibraryCoverImageUrl = arrayOf(
            "file:///android_asset/pose_cover/1.png",
            "file:///android_asset/pose_cover/2.png",
            "file:///android_asset/pose_cover/3.png",
            "file:///android_asset/pose_cover/4.png",
            "file:///android_asset/pose_cover/5.png",
            "file:///android_asset/pose_cover/6.png",
            "file:///android_asset/pose_cover/7.png",
            "file:///android_asset/pose_cover/8.png",
        )

        // 动作库感觉图
        val poseFeelingImageUrl = arrayOf(
            "file:///android_asset/feeling/shendun/male.png",
            null,
            "file:///android_asset/feeling/pingbanzhicheng/male.png",
            "file:///android_asset/feeling/fuwocheng/male.png",
            "file:///android_asset/feeling/tunqiao/male.png",
            "file:///android_asset/feeling/xixilijuanfu/male.png",
            "file:///android_asset/feeling/juanfumoxi/male.png",
            "file:///android_asset/feeling/guizifuwocheng/male.png",
            null
        )

        // 正确动作图
        val rightPoseImageUrl = arrayOf(
            "file:///android_asset/feeling/shendun/2.png",
            "file:///android_asset/feeling/kaoqiangzhanli/2.png",
            "file:///android_asset/feeling/pingbanzhicheng/2.png",
            "file:///android_asset/feeling/fuwocheng/2.png",
            "file:///android_asset/feeling/tunqiao/2.png",
            "file:///android_asset/feeling/xixilijuanfu/2.png",
            "file:///android_asset/feeling/juanfumoxi/2.png",
            "file:///android_asset/feeling/guizifuwocheng/2.png",
            "file:///android_asset/feeling/fubulashen/2.png"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化Logger
        Logger.addLogAdapter(AndroidLogAdapter())

        // 加载中图片设置
        loadingBitmap = BitmapFactory.decodeResource(resources, R.drawable.loading3)

        // 初始化文件夹
        initFiles()

        // 初始化Fragment
        initFragments()

        // 设置底部导航栏监听
        setupTabBarListeners()

        // 加载动作库数据
        loadPoseData()
    }

    private fun initFiles() {
        // 初始化应用文件夹路径
        appFilePath = Objects.requireNonNull(applicationContext.filesDir.parentFile).absolutePath
        // 创建分析文件夹
        FileUtils.createFile("$appFilePath/files/analysisReport/", FileUtils.CREATE_FOLDER)
        // 创建今日文件夹 例如 2022-03-27
        today = LocalDate.now()
        FileUtils.createFile("$appFilePath/files/analysisReport/$today", FileUtils.CREATE_FOLDER)
        // 创建个人信息数据文件夹
        FileUtils.createFile("$appFilePath/files/personalData/", FileUtils.CREATE_FOLDER)
        // 创建动作库文件夹 存储动作库的图片缓存
        FileUtils.createFile("$appFilePath/files/poseLibrary/", FileUtils.CREATE_FOLDER)
        // 创建动作文件 存储动作检测数据
        FileUtils.createFile("$appFilePath/files/poseData/poseData.json", FileUtils.CREATE_FILE)
    }

    private fun initFragments() {
        poseLibraryFragment = PoseLibraryFragment()
        diaryFragment = DiaryFragment()

        // 默认显示动作库Fragment
        switchFragment(poseLibraryFragment)
    }

    private fun setupTabBarListeners() {
        binding.customTabBar.setOnTabSelectedListener(object :
            CustomTabBarView.OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                when (position) {
                    0 -> {
                        // 动作库
                        switchFragment(poseLibraryFragment)
                    }

                    1 -> {
                        // 中间按钮 - 启动相机进行姿势检测
                        CameraXLivePreviewActivity.start(this@MainActivity)
                    }

                    2 -> {
                        // 日记
                        switchFragment(diaryFragment)
                    }
                }
            }
        })
    }

    private fun loadPoseData() {
        // 主线程更新动作库handler
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == 0x1233) {
                    // 通知Fragment刷新UI
                    poseLibraryFragment.refreshData()
                }
            }
        }

        Thread {
            // 从数据库加载动作库数据
            val controlTable = ControlTable()
            poseEntityArrayList = controlTable.readTable()

            // 设置动作持续变量类型
            poseEntityArrayList?.let { poseList ->
                if (poseList.isNotEmpty()) {
                    for (poseEntity in poseList) {
                        // 倒计时类型0 计数类型1
                        poseEntity.setPoseType(0)
                        poseEntity.setPoseProcessValue(30)
                    }

                    // 加载动作库图片
                    initSwiperImages()

                    // 每隔500ms更新一次动作库，直到封面图片获取完毕
                    var needRefresh = true
                    while (needRefresh) {
                        try {
                            Thread.sleep(500)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        handler.sendEmptyMessage(0x1233)
                        needRefresh = false

                        for (bitmap in poseLibrarySwiperImageArrayList) {
                            if (bitmap == loadingBitmap) {
                                needRefresh = true
                                break
                            }
                        }
                    }
                }
            }
        }.start()
    }

    private fun initSwiperImages() {
        poseLibrarySwiperImageArrayList.clear()
        poseDetailFeelingImageArrayList.clear()
        poseRightImageArrayList.clear()

        for (i in poseLibraryCoverImageUrl.indices) {
            try {
                val futureTarget1: FutureTarget<Bitmap> = Glide.with(this)
                    .asBitmap()
                    .load(poseLibraryCoverImageUrl[i])
                    .submit()

                val futureTarget3: FutureTarget<Bitmap> = Glide.with(this)
                    .asBitmap()
                    .load(rightPoseImageUrl[i])
                    .submit()

                poseLibrarySwiperImageArrayList.add(futureTarget1.get())
                poseRightImageArrayList.add(futureTarget3.get())

                try {
                    val futureTarget2: FutureTarget<Bitmap> = Glide.with(this)
                        .asBitmap()
                        .load(poseFeelingImageUrl[i])
                        .submit()
                    poseDetailFeelingImageArrayList.add(futureTarget2.get())
                } catch (e: Exception) {
                    poseDetailFeelingImageArrayList.add(loadingBitmap!!)
                }

            } catch (e: ExecutionException) {
                Log.e("MainActivity", "图片加载失败: ${e.message}")
                poseLibrarySwiperImageArrayList.add(loadingBitmap!!)
                poseRightImageArrayList.add(loadingBitmap!!)
                poseDetailFeelingImageArrayList.add(loadingBitmap!!)
            } catch (e: InterruptedException) {
                Log.e("MainActivity", "图片加载中断: ${e.message}")
                poseLibrarySwiperImageArrayList.add(loadingBitmap!!)
                poseRightImageArrayList.add(loadingBitmap!!)
                poseDetailFeelingImageArrayList.add(loadingBitmap!!)
            }
        }
    }

    fun switchFragment(fragment: Fragment) {
        if (currentFragment != fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            currentFragment = fragment
        }
    }
} 