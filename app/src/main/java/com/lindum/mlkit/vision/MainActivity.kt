package com.lindum.mlkit.vision

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.ActivityMainBinding
import com.lindum.mlkit.vision.entity.PoseEntity
import com.lindum.mlkit.vision.entity.UserEntity
import com.lindum.mlkit.vision.fragments.DiaryFragment
import com.lindum.mlkit.vision.fragments.PoseLibraryFragment
import com.lindum.mlkit.vision.utils.FileUtils
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import java.time.LocalDate
import java.util.Objects
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var poseLibraryFragment: PoseLibraryFragment
    private lateinit var diaryFragment: DiaryFragment
    private var loadingBitmap: Bitmap? = null
    private var currentFragment: Fragment? = null
    private lateinit var sharedPreferences: SharedPreferences

    // 静态变量
    companion object {
        var userEntity = UserEntity()
        var poseEntityArrayList: MutableList<PoseEntity>? = null
        var poseLibrarySwiperImageArrayList = ArrayList<Bitmap>()
        var poseDetailFeelingImageArrayList = ArrayList<Bitmap>()
        var poseRightImageArrayList = ArrayList<Bitmap>()
        var appFilePath: String = ""
        var today: LocalDate = LocalDate.now()

        // SharedPreferences 常量
        private const val PREF_NAME = "ai_trainer_prefs"
        private const val IS_FIRST_LAUNCH = "is_first_launch"

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
            "file:///android_asset/feeling/shendun/male.png",
            "file:///android_asset/feeling/pingbanzhicheng/male.png",
            "file:///android_asset/feeling/fuwocheng/male.png",
            "file:///android_asset/feeling/tunqiao/male.png",
            "file:///android_asset/feeling/xixilijuanfu/male.png",
            "file:///android_asset/feeling/juanfumoxi/male.png",
            "file:///android_asset/feeling/guizifuwocheng/male.png",
            "file:///android_asset/feeling/shendun/male.png"
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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(binding.root)

        // 初始化Logger
        Logger.addLogAdapter(AndroidLogAdapter())

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // 检查是否首次启动
        if (isFirstLaunch()) {
            // 请求必要的存储权限
            requestStoragePermissions()
        }

        // 加载中图片设置
        loadingBitmap = BitmapFactory.decodeResource(resources, R.drawable.loading3)

        // 初始化文件夹
        initFiles()

        // 初始化Fragment
        initFragments()

        // 设置底部导航栏监听
        setupTabBarListeners()

        // 加载动作库数据
//        loadPoseData()
    }

    /**
     * 检查应用是否是首次启动
     * @return Boolean 如果是首次启动则返回true，否则返回false
     */
    private fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_LAUNCH, true)
    }

    /**
     * 处理首次启动的逻辑
     */
    private fun handleFirstLaunch() {
        // 在这里添加首次启动需要执行的逻辑
        Logger.d("应用首次启动")

        // 可以在这里添加引导页面的显示逻辑，例如：

        // 记录第一次使用AiTrainer
        FileUtils.createFile(
            "$appFilePath/files/analysisReport/recordFile.sys",
            FileUtils.CREATE_FILE
        )
        var time = System.currentTimeMillis() / 1000
        FileUtils.writeStringToFile(
            "$time 初习八段锦",
            "$appFilePath/files/analysisReport/",
            "recordFile.sys",
            FileUtils.APPEND_TEXT
        )

        // 首次启动后，更新标志位为false
        sharedPreferences.edit { putBoolean(IS_FIRST_LAUNCH, false) }
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

    fun switchFragment(fragment: Fragment) {
        if (currentFragment != fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            currentFragment = fragment
        }
    }

    /**
     * 请求存储权限
     */
    private fun requestStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 及以上版本使用分类媒体权限
            XXPermissions.with(this)
                .permission(Permission.READ_MEDIA_IMAGES)
                .permission(Permission.READ_MEDIA_VIDEO)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        if (allGranted) {
                            Logger.d("所有媒体权限已授予")
                            // 如果是首次启动，执行首次启动逻辑
                            handleFirstLaunch()
                        } else {
                            Logger.w("部分媒体权限未授予")
                            Toast.makeText(
                                this@MainActivity,
                                "部分权限未授予，应用可能无法正常工作",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Toast.makeText(
                                this@MainActivity,
                                "请在设置中手动开启媒体访问权限",
                                Toast.LENGTH_SHORT
                            ).show()
                            // 引导用户去设置中手动授权
                            XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "媒体访问权限被拒绝，应用功能受限",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        } else {
            // Android 12 及以下版本使用旧权限
            XXPermissions.with(this)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        if (allGranted) {
                            Logger.d("所有存储权限已授予")
                        } else {
                            Logger.w("部分存储权限未授予")
                            Toast.makeText(
                                this@MainActivity,
                                "部分权限未授予，应用可能无法正常工作",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Toast.makeText(
                                this@MainActivity,
                                "请在设置中手动开启存储权限",
                                Toast.LENGTH_SHORT
                            ).show()
                            // 引导用户去设置中手动授权
                            XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "存储权限被拒绝，应用功能受限",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }
    }
} 