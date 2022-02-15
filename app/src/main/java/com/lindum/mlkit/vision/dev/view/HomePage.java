package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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

import com.jpeng.jptabbar.JPTabBar;
import com.jpeng.jptabbar.OnTabSelectListener;
import com.jpeng.jptabbar.anno.NorIcons;
import com.jpeng.jptabbar.anno.SeleIcons;
import com.jpeng.jptabbar.anno.Titles;
import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.posedetector.PoseEntity;
import com.lindum.mlkit.vision.dev.sql.ControlTable;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

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
    static List<PoseEntity> poseEntityArrayList;
    private ViewPager viewPager;
    private View view1, view2, view3, view4;
    /**
     * 存放视图的数组
     */
    private List<View> mViews;
    /**
     * 640*360 360/640=0.5625
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
            "伪装者:胡歌演绎'痞子特工'",
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
        registerGetIntentData();
        // Logger 初始化
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 使主线程可以进行简单的网络请求
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        init();
        // 从数据库获取动作实体类数据
        getPoseEntityFromDataBase();
    }

    public void getPoseEntityFromDataBase() {
        // 设置动作库
        ArrayList<Item> arrayList = new ArrayList<>();
        ControlTable controlTable = new ControlTable();
        RecyclerView recyclerView = view3.findViewById(R.id.recyclerView);
        Thread getData = new Thread() {
            @Override
            public void run() {
                poseEntityArrayList = controlTable.readTable();
                for (PoseEntity poseEntity : poseEntityArrayList) {
                    Logger.e(poseEntity.toString());
                    arrayList.add(new Item(poseEntity.getPoseName(), "副标题", R.mipmap.q1));
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void init() {

        // 底部栏切换页面初始化
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
        // 轮播图
        SimpleImageBanner simpleImageBanner = view1.findViewById(R.id.banner);
        simpleImageBanner.setSource(getBannerList()).setOnItemClickListener((view, item, position1) -> Logger.d("headBanner position--->" + position1)).startScroll();

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
        // 初始化食物列表
        initFoodList();
        // 初始化底部 Tabbar
        initTabbar();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initTabbar() {
        final int personalPageIndex = 3;
        JPTabBar jpTabBar = findViewById(R.id.tabbar);
        jpTabBar.setContainer(viewPager);
        // 设置未读消息红点
        jpTabBar.showBadge(3, "12", false);
        // tabbar 添加监听
        jpTabBar.setTabListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int index) {
                Logger.d("选中了" + TABBAR_TITLES[index]);
                if (index == personalPageIndex) {
                    // 隐藏小红点
                    jpTabBar.hideBadge(3);
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
                    Intent intent = new Intent(HomePage.this, CameraXLivePreviewActivity.class);
                    startActivity(intent);
                })
                .show());
    }

    /**
     * 初始化食物列表
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

    @Override
    public void onItemClick(Item item) {
        Toast.makeText(getApplicationContext(), item.title + " is clicked", Toast.LENGTH_SHORT).show();
        // 不要忘记在 AndroidManifest 中声明 Activity
        Intent intent = new Intent(HomePage.this, PoseDetailsActivity.class);
        startActivity(intent);
    }

}
