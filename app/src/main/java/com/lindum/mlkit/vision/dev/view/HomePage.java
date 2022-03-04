package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/2/13 23:24
 */
public class HomePage extends AppCompatActivity implements HomeAdapter.ItemListener {
    public static UserEntity userEntity = new UserEntity();
    public static ArrayList<Bitmap> poseLibrarySwiperImageArrayList = new ArrayList<>();
    public static ArrayList<Bitmap> poseDetailFeelingImageArrayList = new ArrayList<>();
    private HomeAdapter adapter;
    private Handler addImageHandle;
    final int userInfoIntentCode = 1;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
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
        // 初始化
        init();
        // 从数据库获取动作实体类数据
        getPoseEntityFromDataBase();

    }

    /**
     * 初始化软件
     */
    public void init() {
        isFirstUseApp = isFirstUseApp();
        // 是否第一次使用软件
        if (isFirstUseApp) {
            Intent intent = new Intent(HomePage.this, IntroView.class);
            startActivity(intent);
            //适配安卓11及以下版本的存储权限请求
            new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .iconRes(R.mipmap.tips)
                    .title("提示")
                    .content("检测到您第一次使用本软件，请授予权限保证软件正常运行，软件运行过程中不会获取您的隐私，请放心使用!")
                    .positiveText("确定")
                    .onPositive((dialog, which) -> {
                        GetPermissionUtils.getRuntimePermissions(this);
                    })
                    .show();
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
        // 从数据库获取用户数据
        getUserInfoFromDateBase();
        // 日历按钮
        ImageView imageView = view1.findViewById(R.id.calendarButton);
        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, ProgressActivity.class);
            startActivity(intent);
        });

    }

    /**
     * 获取动作库图片
     */
    public void initSwiperImage() {
        for (PoseEntity poseEntity : poseEntityArrayList) {
            poseLibrarySwiperImageArrayList.add(returnBitMap(poseEntity.getPoseSwiperImageUrl()[0]));
            poseDetailFeelingImageArrayList.add(returnBitMap(poseEntity.getPoseFeelingImage()));
        }
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
        ControlTable controlTable = new ControlTable();
        RecyclerView recyclerView = view3.findViewById(R.id.recyclerView);
        // todo 线程池
        Thread getData = new Thread() {
            @Override
            public void run() {
                super.run();
                poseEntityArrayList = controlTable.readTable();
                // 获取动作库图片监听
                initSwiperImage();
                for (PoseEntity poseEntity : poseEntityArrayList) {
                    // 动作库 Items 加载
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

    public void initBanner() {
        // 轮播图
        SimpleImageBanner simpleImageBanner = view1.findViewById(R.id.banner);
        simpleImageBanner.setSource(getBannerList()).setOnItemClickListener((view, item, position1) -> Logger.e("headBanner position--->" + position1)).startScroll();
    }


    /**
     * 初始化底部 Tabbar，要在布局初始化之后调用
     */
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
                    TextView nickName = findViewById(R.id.personalPageNickNameText);
                    nickName.setText(userEntity.getNickName());
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


}
