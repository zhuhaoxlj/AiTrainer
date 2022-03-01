package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jpeng.jptabbar.JPTabBar;
import com.jpeng.jptabbar.OnTabSelectListener;
import com.jpeng.jptabbar.anno.NorIcons;
import com.jpeng.jptabbar.anno.SeleIcons;
import com.jpeng.jptabbar.anno.Titles;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.posedetector.PoseEntity;
import com.lindum.mlkit.vision.dev.sql.ControlTable;
import com.lindum.mlkit.vision.dev.utils.FileUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/2/13 23:24
 */
public class HomePage extends AppCompatActivity implements HomeAdapter.ItemListener {
    private HomeAdapter adapter;
    final int userInfoIntentCode = 1;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private static final int PERMISSION_REQUESTS = 1;
    static List<PoseEntity> poseEntityArrayList;
    private ViewPager viewPager;
    private View view1, view2, view3, view4;
    private boolean isFirstUseApp = true;
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
            "无心法师:生死离别!月牙遭虐杀",
            "花千骨:尊上沦为花千骨",
    };
    @Titles
    private static final String[] TABBAR_TITLES = {"主页", "社区", "动作库", "个人"};
    @SeleIcons
    private static final int[] M_SELE_ICONS = {R.mipmap.home, R.mipmap.social, R.mipmap.library, R.mipmap.personal};
    @NorIcons
    private static final int[] M_NORMAL_ICONS = {R.mipmap.home_gray, R.mipmap.social_gray, R.mipmap.library_gray, R.mipmap.personal_gray};

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        setContentView(R.layout.home_page);
        // 注册 Intent 请求数据
        registerGetIntentData();
        // Logger 初始化
        Logger.addLogAdapter(new AndroidLogAdapter());
        init();
        // 从数据库获取动作实体类数据
        getPoseEntityFromDataBase();
    }

    /**
     * 初始化软件
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void init() {
        isFirstUseApp = isFirstUseApp();
        // 是否第一次使用软件
        if (isFirstUseApp) {
            Intent intent = new Intent(HomePage.this, IntroView.class);
            startActivity(intent);
            //适配安卓11及以下版本的存储权限请求
            if (Build.VERSION.SDK_INT > 29) {
                new MaterialDialog.Builder(this)
                        .cancelable(false)
                        .iconRes(R.mipmap.tips)
                        .title("提示")
                        .content("检测到您第一次使用本软件，请授予权限保证软件正常运行，软件运行过程中不会获取您的隐私，请放心使用!")
                        .positiveText("确定")
                        .onPositive((dialog, which) -> {
                            getRuntimePermissions();
                        })
                        .show();
            }
        }
        // 再次获取未授权权限
        if (!isFirstUseApp && !allPermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getRuntimePermissions();
            }
        }
        if(allPermissionsGranted()){
            TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            @SuppressLint("HardwareIds") String IMEI = tm.getDeviceId();
            Logger.e(IMEI);
        }
        viewPager = findViewById(R.id.viewPager);
        //获取布局对象管理
        LayoutInflater inflater = getLayoutInflater();
        //实例化view
        view1 = inflater.inflate(R.layout.page1, null);
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
        // 去除个人页标题栏左侧图标
        TitleBar personalPageTitleBar = view4.findViewById(R.id.personalPageTitleBar);
        personalPageTitleBar.setLeftVisible(false);
        // 去除动作库标题栏左侧图标
        TitleBar poseLibraryTitleBar = view3.findViewById(R.id.poseLibraryTitleBar);
        poseLibraryTitleBar.setLeftVisible(false);
        // 个人页面修改信息按钮
        ImageView changeProfileInfoButton = view4.findViewById(R.id.profileInfoButton);
        changeProfileInfoButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, UserInfoInitActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
        // 初始化轮播图
        initBanner();
        // 初始化食物列表
        initFoodList();
        // 初始化底部 Tabbar
        initTabbar();
    }

    public void getPoseEntityFromDataBase() {
        // 设置动作库
        ArrayList<Item> arrayList = new ArrayList<>();
        ControlTable controlTable = new ControlTable();
        RecyclerView recyclerView = view3.findViewById(R.id.recyclerView);
        // todo 线程池
        Thread getData = new Thread() {
            @Override
            public void run() {
                poseEntityArrayList = controlTable.readTable();
                for (PoseEntity poseEntity : poseEntityArrayList) {
                    arrayList.add(new Item(poseEntityArrayList.indexOf(poseEntity), poseEntity.getPoseName(), "副标题", R.mipmap.q1));
                }
                super.run();
            }
        };
        getData.start();
        adapter = new HomeAdapter(this, arrayList, this);
        recyclerView.setAdapter(adapter);
        GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
    }

    public void initBanner() {
        // 轮播图
        SimpleImageBanner simpleImageBanner = view1.findViewById(R.id.banner);
        simpleImageBanner.setSource(getBannerList()).setOnItemClickListener((view, item, position1) -> Logger.e("headBanner position--->" + position1)).startScroll();
    }

    /**
     * 初始化底部 Tabbar，要在布局初始化之后调用
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initTabbar() {
        final int personalPageIndex = 3;
        JPTabBar jpTabBar = findViewById(R.id.tabbar);
        jpTabBar.setContainer(viewPager);
        // 设置未读消息红点
        jpTabBar.showBadge(3, "1", false);
        // tabbar 添加监听
        jpTabBar.setTabListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int index) {
                Logger.d("选中了" + TABBAR_TITLES[index]);
                // 选择个人界面
                if (index == personalPageIndex) {
                    // 隐藏小红点
                    jpTabBar.hideBadge(3);
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
    public void onItemClick(Item item) {
        Toast.makeText(getApplicationContext(), item.title + "第" + item.index + "个" + " is clicked", Toast.LENGTH_SHORT).show();
        // 不要忘记在 AndroidManifest 中声明 Activity
        Intent intent = new Intent(HomePage.this, PoseDetailsActivity.class);
        intent.putExtra("feeling", poseEntityArrayList.get(item.index).getPoseFeeling());
        intent.putExtra("steps", poseEntityArrayList.get(item.index).getStepsOfPose());
        intent.putExtra("commonMistakes", poseEntityArrayList.get(item.index).getCommonMistakes());
        intent.putExtra("details", poseEntityArrayList.get(item.index).getDetailTips());
        intent.putExtra("breath", poseEntityArrayList.get(item.index).getBreathTips());
        startActivity(intent);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }



    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Logger.i("Permission granted: " + permission);
            return true;
        }
        Logger.i("Permission NOT granted: " + permission);
        return false;
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
        // 判断是否获取外置存储写入权限
        if (Environment.isExternalStorageManager()) {
            Logger.e("已获取外置存储写入权限");
        } else {
            requestExternalStorage(this);
        }
    }

    /**
     * 请求访问外置内存
     *
     * @param context 上下文场景
     */
    public void requestExternalStorage(Context context) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        }
        assert intent != null;
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }
}
