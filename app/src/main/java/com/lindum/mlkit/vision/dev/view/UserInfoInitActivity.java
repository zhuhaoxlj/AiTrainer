package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.gzuliyujiang.wheelpicker.AddressPicker;
import com.github.gzuliyujiang.wheelpicker.BirthdayPicker;
import com.github.gzuliyujiang.wheelpicker.SexPicker;
import com.github.gzuliyujiang.wheelpicker.annotation.AddressMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnAddressPickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnLinkageSelectedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnOptionPickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnTimePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.CityEntity;
import com.github.gzuliyujiang.wheelpicker.entity.CountyEntity;
import com.github.gzuliyujiang.wheelpicker.entity.ProvinceEntity;
import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.widget.picker.RulerView;
import com.xuexiang.xui.widget.picker.widget.TimePickerView;

/**
 * @author MarkGosling
 * @date 2022/2/14 10:55
 */
public class UserInfoInitActivity extends AppCompatActivity implements OnDatePickedListener, OnTimePickedListener, OnAddressPickedListener, OnOptionPickedListener {
    private TimePickerView mDatePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_input);
        Logger.addLogAdapter(new AndroidLogAdapter());

        init();

    }

    public void init() {
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
        RulerView heigthRuler = findViewById(R.id.heightRuler);
        heigthRuler.setOnChooseResultListener(new RulerView.OnChooseResultListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEndResult(String result) {
                TextView heightText = findViewById(R.id.heigthText);
                heightText.setText(heigthRuler.getCurrentValue() + "cm");
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
        selectAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProvinceCityCounty(view);
            }
        });
    }

    public void onProvinceCityCounty(View view) {
        AddressPicker picker = new AddressPicker(this);
        picker.setAddressMode(AddressMode.PROVINCE_CITY_COUNTY);
        picker.setDefaultValue("江苏省", "淮安市", "淮阴区");
        picker.setOnAddressPickedListener(this);
        picker.getWheelLayout().setOnLinkageSelectedListener(new OnLinkageSelectedListener() {
            @Override
            public void onLinkageSelected(Object first, Object second, Object third) {
                picker.getTitleView().setText(String.format("%s%s%s",
                        picker.getFirstWheelView().formatItem(first),
                        picker.getSecondWheelView().formatItem(second),
                        picker.getThirdWheelView().formatItem(third)));
            }
        });
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
