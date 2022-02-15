package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.gzuliyujiang.wheelpicker.AddressPicker;
import com.github.gzuliyujiang.wheelpicker.BirthdayPicker;
import com.github.gzuliyujiang.wheelpicker.SexPicker;
import com.github.gzuliyujiang.wheelpicker.annotation.AddressMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnAddressPickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnOptionPickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnTimePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.CityEntity;
import com.github.gzuliyujiang.wheelpicker.entity.CountyEntity;
import com.github.gzuliyujiang.wheelpicker.entity.ProvinceEntity;
import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.picker.RulerView;

import java.time.LocalDate;

/**
 * @author MarkGosling
 * @date 2022/2/14 10:55
 */
public class UserInfoInitActivity extends AppCompatActivity implements OnDatePickedListener, OnTimePickedListener, OnAddressPickedListener, OnOptionPickedListener {
    final int changeNickNameIntentCode = 1, changeProfileIntentCode = 2;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    TextView nickNameText;
    String profile;
    String sportsPreference;
    String sex;
    LocalDate birthday;
    float height;
    float weigth;
    String region;
    Bitmap profileImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        setContentView(R.layout.user_info_input);
        Logger.addLogAdapter(new AndroidLogAdapter());
        registerGetIntentData();
        init();
    }

    /**
     * 主函数注册请求数据的监听事件
     */
    public void registerGetIntentData() {
        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // 修改昵称
            if (result.getData() != null && result.getResultCode() == changeNickNameIntentCode) {
                String nickName = getStringFromIntentResult(result, "nickName");
                nickNameText.setText(nickName);
                String profile = getStringFromIntentResult(result, "profile");
                Logger.d(profile);
            } else {
                Toast.makeText(getApplicationContext(), "空", Toast.LENGTH_LONG).show();
            }
            // 修改简介
            if (result.getData() != null && result.getResultCode() == changeProfileIntentCode) {
                profile = getStringFromIntentResult(result, "profile");
                Logger.d(profile);
            } else {
                Toast.makeText(getApplicationContext(), "空", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 初始化函数
     */
    public void init() {
        nickNameText = findViewById(R.id.nickNameText);

        // TitleBar
        TitleBar titleBar = findViewById(R.id.UserInfoTitleBar);
        titleBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("nickName", nickNameText.getText() + "");
                setResult(1, intent);
                finish();
            }
        });
        RelativeLayout selectBirthDay = findViewById(R.id.selectBirthDayButton);
        selectBirthDay.setOnClickListener(view -> {
            BirthdayPicker picker = new BirthdayPicker(UserInfoInitActivity.this);
            picker.setDefaultValue(2001, 6, 15);
            picker.setOnDatePickedListener(UserInfoInitActivity.this);
            picker.getWheelLayout().setResetWhenLinkage(false);
            picker.show();
        });
        RelativeLayout selectSex = findViewById(R.id.selectSexButton);
        selectSex.setOnClickListener(this::onSex);

        // 身高选择器
        RulerView heightRuler = findViewById(R.id.heightRuler);
        heightRuler.setOnChooseResultListener(new RulerView.OnChooseResultListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEndResult(String result) {
                TextView heightText = findViewById(R.id.heigthText);
                heightText.setText(heightRuler.getCurrentValue() + "cm");
            }

            @Override
            public void onScrollResult(String result) {

            }
        });
        // 体重选择器
        RulerView weightRuler = findViewById(R.id.weightRuler);
        weightRuler.setOnChooseResultListener(new RulerView.OnChooseResultListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEndResult(String result) {
                TextView weightText = findViewById(R.id.weightText);
                weightText.setText(weightRuler.getCurrentValue() + "kg");
            }

            @Override
            public void onScrollResult(String result) {

            }
        });
        // 地区选择器
        RelativeLayout selectAddressButton = findViewById(R.id.selectAddressButton);
        selectAddressButton.setOnClickListener(this::onProvinceCityCounty);
        // 修改昵称
        RelativeLayout changeNickNameButton = findViewById(R.id.changeNickNameButton);
        changeNickNameButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserInfoInitActivity.this, ChangeNickName.class);
            intentActivityResultLauncher.launch(intent);
        });
        // 修改简介
        RelativeLayout changeProfileButton = findViewById(R.id.changeProfileButton);
        changeProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserInfoInitActivity.this, ChangeProfileActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
        // 运动偏好
        RelativeLayout selectSportsPreferenceButton = findViewById(R.id.selectSportsPreferenceButton);
        selectSportsPreferenceButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserInfoInitActivity.this, ChangeProfileActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
    }

    public void onProvinceCityCounty(View view) {
        AddressPicker picker = new AddressPicker(this);
        picker.setAddressMode(AddressMode.PROVINCE_CITY_COUNTY);
        picker.setDefaultValue("江苏省", "淮安市", "淮阴区");
        picker.setOnAddressPickedListener(this);
        picker.getWheelLayout().setOnLinkageSelectedListener((first, second, third) -> picker.getTitleView().setText(String.format("%s%s%s",
                picker.getFirstWheelView().formatItem(first),
                picker.getSecondWheelView().formatItem(second),
                picker.getThirdWheelView().formatItem(third))));
        picker.show();
    }

    public void onSex(View view) {
        SexPicker picker = new SexPicker(this);
        picker.setBodyWidth(140);
        picker.setIncludeSecrecy(false);
        picker.setDefaultValue("女");
        picker.setOnOptionPickedListener(this);
        picker.getWheelLayout().setOnOptionSelectedListener((position, item) -> picker.getTitleView().setText(picker.getWheelView().formatItem(position)));
        picker.show();
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onDatePicked(int year, int month, int day) {
        Logger.d("您的生日:" + year + "-" + month + "-" + day);
        TextView birthdayText = findViewById(R.id.birthdayText);
        birthdayText.setText(year + "-" + month + "-" + day);
    }

    @Override
    public void onTimePicked(int hour, int minute, int second) {

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("nickName", nickNameText.getText() + "");
        setResult(1, intent);
        super.onBackPressed();
    }

    @Override
    public void onOptionPicked(int position, Object item) {
        TextView sexText = findViewById(R.id.sexText);
        String[] sexType = {"男", "女"};
        sexText.setText(sexType[position]);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onAddressPicked(ProvinceEntity province, CityEntity city, CountyEntity county) {
        TextView addressText = findViewById(R.id.addressText);
        addressText.setText(province.getName() + "，" + city.getName() + "，" + county.getName());
    }
}
