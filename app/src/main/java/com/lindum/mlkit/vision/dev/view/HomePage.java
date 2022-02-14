package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jpeng.jptabbar.JPTabBar;
import com.jpeng.jptabbar.OnTabSelectListener;
import com.jpeng.jptabbar.anno.NorIcons;
import com.jpeng.jptabbar.anno.SeleIcons;
import com.jpeng.jptabbar.anno.Titles;
import com.lindum.mlkit.vision.dev.CameraXLivePreviewActivity;
import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/2/13 23:24
 */
public class HomePage extends AppCompatActivity {
    private ViewPager viewPager;
    private View view1, view2, view3, view4;
    private List<View> mViews;  //存放视图的数组
    private PagerAdapter mPagerAdapter;//适配器
    public static String[] urls = new String[]{//640*360 360/640=0.5625
            "http://photocdn.sohu.com/tvmobilemvms/20150907/144160323071011277.jpg",//伪装者:胡歌演绎"痞子特工"
            "http://photocdn.sohu.com/tvmobilemvms/20150907/144158380433341332.jpg",//无心法师:生死离别!月牙遭虐杀
            "http://photocdn.sohu.com/tvmobilemvms/20150907/144160286644953923.jpg",//花千骨:尊上沦为花千骨
            "http://photocdn.sohu.com/tvmobilemvms/20150902/144115156939164801.jpg",//综艺饭:胖轩偷看夏天洗澡掀波澜
            "http://photocdn.sohu.com/tvmobilemvms/20150907/144159406950245847.jpg",//碟中谍4:阿汤哥高塔命悬一线,超越不可能
    };
    public static String[] titles = new String[]{
            "伪装者:胡歌演绎'痞子特工'",
            "无心法师:生死离别!月牙遭虐杀",
            "花千骨:尊上沦为花千骨",
            "综艺饭:胖轩偷看夏天洗澡掀波澜",
            "碟中谍4:阿汤哥高塔命悬一线,超越不可能",
    };
    @Titles
    private static final String[] mTitles = {"主页", "社区", "动作库", "个人"};
    @SeleIcons
    private static final int[] mSeleIcons = {R.mipmap.home, R.mipmap.social, R.mipmap.library, R.mipmap.personal};
    @NorIcons
    private static final int[] mNormalIcons = {R.mipmap.home_gray, R.mipmap.social_gray, R.mipmap.library_gray, R.mipmap.personal_gray};

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        // Logger 初始化
        Logger.addLogAdapter(new AndroidLogAdapter());
        viewPager = findViewById(R.id.viewPager);
        LayoutInflater inflater = getLayoutInflater();//获取布局对象管理
        view1 = inflater.inflate(R.layout.page1, null);//实例化view
        view2 = inflater.inflate(R.layout.page2, null);
        view3 = inflater.inflate(R.layout.pose_library, null);
        view4 = inflater.inflate(R.layout.page4, null);

        SimpleImageBanner simpleImageBanner = view1.findViewById(R.id.banner);
        simpleImageBanner.setSource(getBannerList()).setOnItemClickListener((view, item, position1) -> Logger.d("headBanner position--->" + position1)).startScroll();

        mViews = new ArrayList<View>();//将要显示的布局存放到list数组
        mViews.add(view1);
        mViews.add(view2);
        mViews.add(view3);
        mViews.add(view4);

        //实例化一个PagerAdapter的适配器
        mPagerAdapter = new PagerAdapter() {
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

        viewPager.setAdapter(mPagerAdapter);//设置适配器
        JPTabBar jpTabBar = findViewById(R.id.tabbar);
        jpTabBar.setContainer(viewPager);
        jpTabBar.showBadge(3, "12", false);
        // tabbar 添加监听
        jpTabBar.setTabListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int index) {
                Logger.d("选中了" + mTitles[index]);
                if (index == 3) {
                    jpTabBar.hideBadge(3);
                }
            }

            @Override
            public boolean onInterruptSelect(int index) {
                return false;
            }
        });
        View middleView = jpTabBar.getMiddleView();
        middleView.setOnClickListener(e -> {
            new MaterialDialog.Builder(this)
                    .iconRes(R.mipmap.tips)
                    .title("提示")
                    .content("1.请选择空旷的场地，或者纯色背景可以提高Ai健身教练的识别精准度。\n2.识别过程中，尽量保持身体在手机屏幕中，尽量选择遮挡人体部位最少的角度进行拍摄，再次感谢您的使用！")
                    .positiveText("确定")
                    .onPositive((dialog, which) -> {
                        // 不要忘记在 AndroidManifest 中声明 Activity
                        Intent intent = new Intent(HomePage.this, CameraXLivePreviewActivity.class);
                        startActivity(intent);
                    })
                    .show();
        });
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
}
