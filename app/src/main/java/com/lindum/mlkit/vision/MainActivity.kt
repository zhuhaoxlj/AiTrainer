package com.lindum.mlkit.vision

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
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
import com.xuexiang.xui.XUI
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.SingleButtonCallback
import java.time.LocalDate
import java.util.Objects

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
            "file:///android_asset/pose_detail/1.png",
            "file:///android_asset/pose_detail/2.png",
            "file:///android_asset/pose_detail/3.png",
            "file:///android_asset/pose_detail/4.png",
            "file:///android_asset/pose_detail/5.png",
            "file:///android_asset/pose_detail/6.png",
            "file:///android_asset/pose_detail/7.png",
            "file:///android_asset/pose_detail/8.png"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        XUI.initTheme(this)
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
                        MaterialDialog.Builder(this@MainActivity)
                            .iconRes(R.mipmap.tips)
                            .title("提示")
                            .content("1.请选择空旷的场地，或者纯色背景可以提高Ai健身教练的识别精准度。\n2.识别过程中，尽量保持身体在手机屏幕中，尽量选择遮挡人体部位最少的角度进行拍摄，再次感谢您的使用！")
                            .positiveText("确定")
                            .onPositive(SingleButtonCallback { dialog: MaterialDialog?, which: DialogAction? ->
                                // 不要忘记在 AndroidManifest 中声明 Activity
                                var intent: Intent? = null
                                intent = Intent(
                                    this@MainActivity,
                                    CameraXLivePreviewActivity::class.java
                                )
                                startActivity(intent)
                            })
                            .show();
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
     * 请求应用所需的所有权限
     */
    private fun requestStoragePermissions() {
        // 创建一个包含所有需要请求的常规权限的列表
        val permissions = mutableListOf(
            Permission.CAMERA,
            Permission.READ_PHONE_STATE,
            Permission.READ_CALENDAR,
        )

        // 根据系统版本添加合适的存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.addAll(
                listOf(
                    Permission.READ_MEDIA_IMAGES,
                    Permission.READ_MEDIA_VIDEO,
                    Permission.READ_MEDIA_AUDIO
                )
            )
        } else {
            // 旧版本Android使用存储权限
            permissions.addAll(
                listOf(
                    Permission.READ_EXTERNAL_STORAGE,
                    Permission.WRITE_EXTERNAL_STORAGE
                )
            )

            // Android 11+ 需要管理所有文件的权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                permissions.add(Permission.MANAGE_EXTERNAL_STORAGE)
            }
        }

        // 请求所有权限
        XXPermissions.with(this)
            .permission(permissions)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        Logger.d("所有权限已授予")
                        // 如果是首次启动，执行首次启动逻辑
                        handleFirstLaunch()
                    } else {
                        Logger.w("部分权限未授予")
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
                            "请在设置中手动开启必要权限",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 引导用户去设置中手动授权
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "权限被拒绝，应用功能受限",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

        // 对于需要特殊处理的系统权限（受保护权限），可以提示用户手动前往设置授权
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // WRITE_SETTINGS权限需要特殊处理
            if (!android.provider.Settings.System.canWrite(this)) {
                MaterialDialog.Builder(this)
                    .title("需要额外权限")
                    .content("应用需要修改系统设置的权限，请在接下来的界面中授予此权限")
                    .positiveText("去设置")
                    .onPositive { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        intent.data = android.net.Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .negativeText("取消")
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 正确姿势图
        rightPoseImageUrl.forEachIndexed { index, url ->
            val rightBitmap = BitmapFactory.decodeStream(
                assets.open(url.replace("file:///android_asset/", ""))
            )
            poseRightImageArrayList.add(rightBitmap)
        }
    }
} 