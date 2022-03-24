package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jpeng.jptabbar.JPTabBar;
import com.jpeng.jptabbar.OnTabSelectListener;
import com.jpeng.jptabbar.anno.NorIcons;
import com.jpeng.jptabbar.anno.SeleIcons;
import com.jpeng.jptabbar.anno.Titles;
import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.datacenter.PersonalPlan;
import com.lindum.mlkit.vision.dev.datacenter.ProgressActivity;
import com.lindum.mlkit.vision.dev.entity.PoseEntity;
import com.lindum.mlkit.vision.dev.entity.UserEntity;
import com.lindum.mlkit.vision.dev.sql.ControlTable;
import com.lindum.mlkit.vision.dev.sql.UpdateUserInfo;
import com.lindum.mlkit.vision.dev.utils.DeviceIdUtils;
import com.lindum.mlkit.vision.dev.utils.FileUtils;
import com.lindum.mlkit.vision.dev.utils.GetPermissionUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.StatusBarUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.guidview.GuideCaseQueue;
import com.xuexiang.xui.widget.guidview.GuideCaseView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author MarkGosling
 * @date 2022/2/13 23:24
 */
public class HomePage extends AppCompatActivity implements HomeAdapter.ItemListener {
    private Bitmap loadingBitmap;
    public static UserEntity userEntity = new UserEntity();
    public static ArrayList<Bitmap> poseLibrarySwiperImageArrayList = new ArrayList<>();
    public static ArrayList<Bitmap> poseDetailFeelingImageArrayList = new ArrayList<>();
    private HomeAdapter adapter;
    private Handler addImageHandle;
    final int userInfoIntentCode = 1;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    public static List<PoseEntity> poseEntityArrayList;
    private ViewPager viewPager;
    private View view1, view2, view3, view4;
    private boolean isFirstUseApp = true;
    //  JPTabBar
    JPTabBar jpTabBar;
    private TextView todaySteps;
    // 传感器管理类
    private SensorManager sensorManager;
    // 今日日期
    private LocalDate today;
    // 应用文件夹路径
    public static String appFilePath;
    // 步数统计传感器监听
    private MySensorEventListener mySensorEventListener;
    /**
     * 存放视图的数组
     */
    private List<View> mViews;
    /**
     * 轮播图图片
     * 图片 1：
     * 图片 2：
     * 图片 3：
     */
    public static String[] urls = new String[]{
            "http://lindum.top/AiTrainer/cover/cover1.png",
            "http://lindum.top/AiTrainer/cover/cover2.png",
            "http://lindum.top/AiTrainer/cover/cover3.png",
    };
    /**
     * 轮播图标题
     */
    public static String[] titles = new String[]{
            "生命在于运动'",
            "运动使人健康",
            "运动是一切生命的源泉",
    };
    @Titles
    private static final String[] TABBAR_TITLES = {"主页", "社区", "动作库", "个人"};
    @SeleIcons
    private static final int[] M_SELE_ICONS = {R.mipmap.home, R.mipmap.social, R.mipmap.library, R.mipmap.personal};
    @NorIcons
    private static final int[] M_NORMAL_ICONS = {R.mipmap.home_gray, R.mipmap.social_gray, R.mipmap.library_gray, R.mipmap.personal_gray};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        // 状态栏沉浸
        StatusBarUtils.initStatusBarStyle(this, false);
        setContentView(R.layout.home_page);
        // 注册 Intent 请求数据
        registerGetIntentData();
        // Logger 初始化
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 初始化
        init();
    }

    /**
     * 初始化软件
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    public void init() {
        // 初始化应用文件夹路径
        appFilePath = Objects.requireNonNull(this.getApplicationContext().getFilesDir().getParentFile()).getAbsolutePath();
        // 创建分析文件夹
        FileUtils.createFile( appFilePath+ "/files/analysisReport/", FileUtils.CREATE_FOLDER);
        // 创建今日文件夹
        today = LocalDate.now();
        FileUtils.createFile(appFilePath + "/files/analysisReport/"+today.toString(), FileUtils.CREATE_FOLDER);
        // 创建个人信息数据文件夹
        FileUtils.createFile(appFilePath+"/files/personalData/",FileUtils.CREATE_FOLDER);
        // 加载中图片设置
        loadingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading3);
        isFirstUseApp = isFirstUseApp();
        // 是否第一次使用软件
        if (isFirstUseApp) {
            Intent intent = new Intent(HomePage.this, IntroView.class);
            startActivity(intent);
            // 聚焦视图
            guideView();
        }
        // 再次获取未授权权限
        if (!isFirstUseApp && !GetPermissionUtils.allPermissionsGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                GetPermissionUtils.getRuntimePermissions(this);
            }
        }
        viewPager = findViewById(R.id.viewPager);
        //获取布局对象管理
        LayoutInflater inflater = getLayoutInflater();
        //实例化view
        view1 = inflater.inflate(R.layout.homepage, null);
        view2 = inflater.inflate(R.layout.page2, null);
        view3 = inflater.inflate(R.layout.pose_library, null);
        view4 = inflater.inflate(R.layout.personal_page, null);
        //将要显示的布局存放到list数组
        mViews = new ArrayList<>();
        mViews.add(view1);
        mViews.add(view2);
        mViews.add(view3);
        mViews.add(view4);
        //实例化一个PagerAdapter的适配器
        //返回要滑动的VIew的个数
        //来判断pager的一个view是否和instantiateItem方法返回的object有关联
        //从当前container中删除指定位置（position）的View;
        //第一：将当前视图添加到container中，第二：返回当前View
        // 适配器
        PagerAdapter mPagerAdapter = new PagerAdapter() {
            @Override   //返回要滑动的VIew的个数
            public int getCount() {
                return mViews.size();
            }

            @Override  //来判断pager的一个view是否和instantiateItem方法返回的object有关联
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override  //从当前container中删除指定位置（position）的View;
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(mViews.get(position));
            }

            @NonNull
            @Override  //第一：将当前视图添加到container中，第二：返回当前View
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(mViews.get(position));
                return mViews.get(position);
            }
        };
        //设置适配器
        viewPager.setAdapter(mPagerAdapter);
        // 初始化底部 TabBar
        initTabBar();
        initSensor();
        // 初始化界面 start
        initHomePage();
        // 主线程更新动作库 handler
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x1233) {
                    // 主线程更新动作库图片
                    initPoseLibrary();
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                ControlTable controlTable = new ControlTable();
                poseEntityArrayList = controlTable.readTable();
                // 把动作库的图片连接转换为图片
                initSwiperImage();
                // 每隔500ms更新一次动作库，知道封面图片获取完毕
                boolean reflash = true;
                while (reflash) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(0x1233);
                    reflash = false;

                    for (Bitmap bitmap : poseLibrarySwiperImageArrayList) {
                        if (bitmap == loadingBitmap) {
                            reflash = true;
                            break;
                        }
                    }
                }
            }
        }.start();

        initPersonalPage();
        // 初始化界面 end
    }

    /**
     * 初始化主页
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initHomePage() {
        // 日历按钮
        ImageView imageView = view1.findViewById(R.id.calendarButton);
        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, ProgressActivity.class);
            startActivity(intent);
        });
        // 初始化轮播图
        SimpleImageBanner simpleImageBanner = view1.findViewById(R.id.banner);
        simpleImageBanner.setFocusedByDefault(true);
        simpleImageBanner.setSource(getBannerList()).setOnItemClickListener((view, item, position1) -> Logger.e("headBanner position--->" + position1)).startScroll();
        // 初始化食物列表
        initFoodList();
        // 状态栏背景条
        LinearLayout statusBar = view1.findViewById(R.id.homePageStatusBar);
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = StatusBarUtils.getStatusBarHeight(this);

        // 消息通知按钮
        ImageView chatListButton = view1.findViewById(R.id.chatListButton);
        chatListButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, OfficialNoticeActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 初始化动作库界面
     */
    public void initPoseLibrary() {
        // 去除动作库标题栏左侧图标
        TitleBar poseLibraryTitleBar = view3.findViewById(R.id.poseLibraryTitleBar);
        poseLibraryTitleBar.setLeftVisible(false);
        // 从数据库获取动作实体类数据
        getPoseEntityFromDataBase();
        // 状态栏背景条
        LinearLayout statusBar = view3.findViewById(R.id.poseLibraryStatusBar);
        statusBar.setBackgroundColor(Color.parseColor("#299de3"));
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = StatusBarUtils.getStatusBarHeight(this);
    }

    /**
     * 初始化个人界面
     */
    @SuppressLint("SetTextI18n")
    public void initPersonalPage() {
        // 训练天数
        TextView practiceDays = view4.findViewById(R.id.practiceDays);
        practiceDays.setText(FileUtils.countFoldersUnderPath(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/analysisReport/")+"\n练习(天)");
        // 状态栏背景条
        LinearLayout statusBar = view4.findViewById(R.id.statusBar);
        // 状态栏背景颜色
        statusBar.setBackgroundColor(Color.parseColor("#88b2ff"));
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = StatusBarUtils.getStatusBarHeight(this);
        //今日运动步数
        todaySteps = view4.findViewById(R.id.todaySteps);
        // 设置个人计划按钮
        LinearLayout personalPlanButton = view4.findViewById(R.id.personalPlanButton);
        personalPlanButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, PersonalPlan.class);
            intent.putExtra("steps", mySensorEventListener.getSteps());
            startActivity(intent);
        });
        // 去除标题栏左侧返回图标
        TitleBar personalPageTitleBar = view4.findViewById(R.id.personalPageTitleBar);
        personalPageTitleBar.setBackgroundColor(Color.parseColor("#88b2ff"));
        personalPageTitleBar.setLeftVisible(false);
        // 修改个人信息按钮
        ImageView changeProfileInfoButton = view4.findViewById(R.id.profileInfoButton);
        changeProfileInfoButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, UserInfoInitActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
        // 从数据库获取用户数据
        getUserInfoFromDateBase();
    }

    public void initSensor() {
        // 初始化步数监听
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mySensorEventListener = new MySensorEventListener();
        sensorManager.registerListener(mySensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mySensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 聚焦视图
     */
    public void guideView() {
        // 聚焦视图
        // 训练中心按钮
        LinearLayout trainButton = findViewById(R.id.trainButton);
        GuideCaseView guideCaseView = new GuideCaseView.Builder(HomePage.this)
                .focusCircleRadiusFactor(1.5)
                .title("训练中心\n智能Ai教练为您矫正姿势")
                .focusBorderColor(Color.GREEN)
                .titleStyle(0, Gravity.CENTER)
                .fitWindowsAuto()
                .focusOn(trainButton)
                .build();

        // 主页
        LinearLayout guideView1 = findViewById(R.id.guideView1);
        GuideCaseView guideCaseView1 = new GuideCaseView.Builder(HomePage.this)
                .focusCircleRadiusFactor(0.8)
                .title("主页")
                .focusBorderColor(Color.GREEN)
                .titleStyle(0, Gravity.CENTER)
                .fitWindowsAuto()
                .focusOn(guideView1)
                .build();
        // 社区
        LinearLayout guideView2 = findViewById(R.id.guideView2);
        GuideCaseView guideCaseView2 = new GuideCaseView.Builder(HomePage.this)
                .focusCircleRadiusFactor(0.8)
                .title("社区")
                .focusBorderColor(Color.GREEN)
                .titleStyle(0, Gravity.CENTER)
                .fitWindowsAuto()
                .focusOn(guideView2)
                .build();
        // 动作库
        LinearLayout guideView3 = findViewById(R.id.guideView3);
        GuideCaseView guideCaseView3 = new GuideCaseView.Builder(HomePage.this)
                .focusCircleRadiusFactor(0.8)
                .title("动作库\n动作详细信息")
                .focusBorderColor(Color.GREEN)
                .titleStyle(0, Gravity.CENTER)
                .fitWindowsAuto()
                .focusOn(guideView3)
                .build();
        // 个人中心
        LinearLayout guideView4 = findViewById(R.id.guideView4);
        GuideCaseView guideCaseView4 = new GuideCaseView.Builder(HomePage.this)
                .focusCircleRadiusFactor(0.8)
                .title("个人中心\n查看")
                .focusBorderColor(Color.GREEN)
                .titleStyle(0, Gravity.CENTER)
                .fitWindowsAuto()
                .focusOn(guideView4)
                .build();

        GuideCaseQueue guideCaseQueue = new GuideCaseQueue();
        guideCaseQueue.add(guideCaseView);
        guideCaseQueue.add(guideCaseView1);
        guideCaseQueue.add(guideCaseView2);
        guideCaseQueue.add(guideCaseView3);
        guideCaseQueue.add(guideCaseView4);
        guideCaseQueue.show();
    }

    /**
     * 获取动作库图片
     */
    public void initSwiperImage() {
        for (int i = 0; i < poseEntityArrayList.size(); i++) {
            poseLibrarySwiperImageArrayList.add(loadingBitmap);
            poseDetailFeelingImageArrayList.add(null);
        }
        for (PoseEntity poseEntity : poseEntityArrayList) {
            int indexOfPoseEntityList = poseEntityArrayList.indexOf(poseEntity);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    poseLibrarySwiperImageArrayList.set(indexOfPoseEntityList, returnBitMap(poseEntity.getPoseSwiperImageUrl()[0]));
                }
            }.start();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    poseDetailFeelingImageArrayList.set(indexOfPoseEntityList, returnBitMap(poseEntity.getPoseFeelingImage()));
                }
            }.start();
        }
//        for (PoseEntity poseEntity : poseEntityArrayList) {
//            poseLibrarySwiperImageArrayList.add(returnBitMap(poseEntity.getPoseSwiperImageUrl()[0]));
//            poseDetailFeelingImageArrayList.add(returnBitMap(poseEntity.getPoseFeelingImage()));
//        }
    }

    public Bitmap returnBitMap(final String url) {
        Bitmap bitmap = null;
        if (url.equals("")) {
            return null;
        }
        URL imageurl = null;
        try {
            imageurl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assert imageurl != null;
            HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 从数据库获取用户信息
     */
    public void getUserInfoFromDateBase() {
        String IMEI;
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            IMEI = DeviceIdUtils.getDeviceId(this);
        } else {
            IMEI = DeviceIdUtils.getIMEIString(this);
        }
        UpdateUserInfo updateUserInfo = new UpdateUserInfo();
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                super.run();
                userEntity = updateUserInfo.search(IMEI);
            }
        }.start();
    }

    /**
     * 从数据库获取动作库数据
     */
    public void getPoseEntityFromDataBase() {
        // 设置动作库
        ArrayList<PoseLibraryItem> arrayList = new ArrayList<>();
        RecyclerView recyclerView = view3.findViewById(R.id.recyclerView);
        // todo 线程池
        Thread getData = new Thread() {
            @Override
            public void run() {
                super.run();
                // 动作库 Items 加载
                for (PoseEntity poseEntity : poseEntityArrayList) {
                    int index = poseEntityArrayList.indexOf(poseEntity);
                    arrayList.add(new PoseLibraryItem(index, poseEntity.getPoseName(), "", poseLibrarySwiperImageArrayList.get(index), "#000000"));
                }
            }
        };
        getData.start();
        adapter = new HomeAdapter(this, arrayList, this);
        recyclerView.setAdapter(adapter);
        GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
    }

    /**
     * 初始化底部 TabBar，要在布局初始化之后调用
     */
    public void initTabBar() {
        final int personalPageIndex = 3;
        jpTabBar = findViewById(R.id.tabbar);
        jpTabBar.setContainer(viewPager);
        if (!FileUtils.isFileExist(HomePage.this.getApplicationContext().getFilesDir().getAbsolutePath(), "userInfo.sys")) {
            // 设置未读消息红点
            jpTabBar.showBadge(3, "1", false);
        }
        // tabBar 添加监听
        jpTabBar.setTabListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int index) {
                Logger.d("选中了" + TABBAR_TITLES[index]);
                // 隐藏小红点
                jpTabBar.hideBadge(3);
                // 选择个人界面
                if (index == personalPageIndex) {

                    // 更新用户信息
                    getUserInfoFromDateBase();
                    TextView nickName = findViewById(R.id.personalPageNickNameText);
                    nickName.setText(userEntity.getNickName());
                    // 提示输入用户信息
                    if (!FileUtils.isFileExist(HomePage.this.getApplicationContext().getFilesDir().getAbsolutePath(), "userInfo.sys")) {
                        new MaterialDialog.Builder(HomePage.this)
                                .iconRes(R.mipmap.tips)
                                .title("提示")
                                .content("为了推荐更适合的课程并为您量身制定训练计划，请完善个人信息。\n\n本软件的数据仅供健身计划分析使用。")
                                .positiveText("确定")
                                .onPositive((dialog, which) -> {
                                    Intent intent = new Intent(HomePage.this, UserInfoInitActivity.class);
                                    intentActivityResultLauncher.launch(intent);
                                })
                                .show();
                    } else {
                        Logger.e("用户已经填写过数据");
                    }
                }
            }

            @Override
            public boolean onInterruptSelect(int index) {
                return false;
            }
        });
        View middleView = jpTabBar.getMiddleView();
        middleView.setOnClickListener(e -> new MaterialDialog.Builder(this)
                .iconRes(R.mipmap.tips)
                .title("提示")
                .content("1.请选择空旷的场地，或者纯色背景可以提高Ai健身教练的识别精准度。\n2.识别过程中，尽量保持身体在手机屏幕中，尽量选择遮挡人体部位最少的角度进行拍摄，再次感谢您的使用！")
                .positiveText("确定")
                .onPositive((dialog, which) -> {
                    // 不要忘记在 AndroidManifest 中声明 Activity
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(HomePage.this, CameraXLivePreviewActivity.class);
                    }
                    startActivity(intent);
                })
                .show());
    }

    /**
     * 是否第一次使用软件
     *
     * @return 返回是否第一次使用
     */
    public boolean isFirstUseApp() {
        boolean isFileExist;
        String dataPath = this.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/firstUse.sys";
        File file = new File(dataPath);
        isFileExist = file.exists();
        return !isFileExist;
    }

    public boolean isUserInfoInit(String userInfo) {
        boolean isFileExist;
        String dataPath = this.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/userInfo.sys";
        File file = new File(dataPath);
        isFileExist = file.exists();
        return !isFileExist;
    }

    /**
     * 初始化食物列表，要在布局初始化之后调用
     */
    public void initFoodList() {
        // 食物列表
        ListView listView = view1.findViewById(R.id.foodList);
        List<FoodEntity> foodEntityList = new ArrayList<>();
        @SuppressLint("UseCompatLoadingForDrawables") FoodEntity foodEntity1 = new FoodEntity("芋头", "高纤维：0.8g", "低GI：51", getResources().getDrawable(R.drawable.taro));
        @SuppressLint("UseCompatLoadingForDrawables") FoodEntity foodEntity2 = new FoodEntity("山药", "高纤维：0.8g", "低GI：51", getResources().getDrawable(R.drawable.yam));
        @SuppressLint("UseCompatLoadingForDrawables") FoodEntity foodEntity3 = new FoodEntity("杨桃", "高纤维：0.8g", "低GI：51", getResources().getDrawable(R.drawable.carambola));
        @SuppressLint("UseCompatLoadingForDrawables") FoodEntity foodEntity4 = new FoodEntity("甜瓜", "高纤维：0.8g", "低GI：51", getResources().getDrawable(R.drawable.muskmelon));
        foodEntityList.add(foodEntity1);
        foodEntityList.add(foodEntity2);
        foodEntityList.add(foodEntity3);
        foodEntityList.add(foodEntity4);
        // 食物列表
        FoodListAdapter resultListAdapter = new FoodListAdapter(this, R.layout.food_item, foodEntityList);
        listView.setAdapter(resultListAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> Logger.e("第" + i + "个"));
    }

    /**
     * 主函数注册请求数据的监听事件
     */
    public void registerGetIntentData() {
        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // 修改昵称
            if (result.getData() != null && result.getResultCode() == userInfoIntentCode) {
                String nickName = getStringFromIntentResult(result, "nickName");
                TextView personalPageNickNameText = findViewById(R.id.personalPageNickNameText);
                personalPageNickNameText.setText(nickName);
            } else {
                Toast.makeText(getApplicationContext(), "空", Toast.LENGTH_LONG).show();
            }
        });
    }

    public UserEntity convertJSON2UserEntity(String jsonContent) {
        UserEntity codeSnippetsArrayList;
        JSONArray jsonArray = (JSONArray) JSON.parse(jsonContent);
        codeSnippetsArrayList = (UserEntity) jsonArray.toJavaList(UserEntity.class);
        return codeSnippetsArrayList;
    }

    /**
     * 从 ActivityResult 通过 key 读取字符串
     *
     * @param result ActivityResult
     * @param key    获取字符串的 key
     * @return key 对应的字符串
     */
    public String getStringFromIntentResult(ActivityResult result, String key) {
        assert result.getData() != null;
        return result.getData().getCharSequenceExtra(key) == null ? "" : result.getData().getCharSequenceExtra(key).toString();
    }

    /**
     * 获取 BannerItemList
     *
     * @return 返回 List<BannerItem>
     */
    public static List<BannerItem> getBannerList() {
        List<BannerItem> list = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            BannerItem item = new BannerItem();
            item.imgUrl = urls[i];
            item.title = titles[i];
            list.add(item);
        }
        return list;
    }

    /**
     * 动作库 Item 的点击监听事件
     *
     * @param item 动作库 item
     */
    @Override
    public void onItemClick(PoseLibraryItem item) {
        Toast.makeText(getApplicationContext(), item.title + "第" + item.index + "个" + " is clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomePage.this, PoseDetailsActivity.class);
        intent.putExtra("feeling", poseEntityArrayList.get(item.index).getPoseFeeling());
        intent.putExtra("steps", poseEntityArrayList.get(item.index).getStepsOfPose());
        intent.putExtra("commonMistakes", poseEntityArrayList.get(item.index).getCommonMistakes());
        intent.putExtra("details", poseEntityArrayList.get(item.index).getDetailTips());
        intent.putExtra("breath", poseEntityArrayList.get(item.index).getBreathTips());
        intent.putExtra("poseIndex", item.index);
        Logger.e(HomePage.poseEntityArrayList.get(item.index).getPoseSwiperImageUrl()[0]);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(mySensorEventListener);
    }

    class MySensorEventListener implements SensorEventListener {
        private int mStepDetector = 0;  // 自应用运行以来STEP_DETECTOR检测到的步数
        public int mStepCounter = 0;   // 自系统开机以来STEP_COUNTER检测到的步数

        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                if (event.values[0] == 1.0f) {
                    mStepDetector++;
                }
            } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                mStepCounter = (int) event.values[0];
            }
            // 设置今日步数
            todaySteps.setText(mStepCounter + " 步");
            // 创建今日文件夹，存放今天产生的数据

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public int getSteps() {
            return mStepCounter;
        }
    }
}
