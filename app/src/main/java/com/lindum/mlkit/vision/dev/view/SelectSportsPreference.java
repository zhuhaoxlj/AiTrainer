package com.lindum.mlkit.vision.dev.view;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.button.SmoothCheckBox;
import com.xuexiang.xui.widget.flowlayout.FlowTagLayout;

import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/2/15 0:54
 */
public class SelectSportsPreference extends AppCompatActivity {
    FlowTagLayout mMultiFlowTagLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        setContentView(R.layout.select_sports_preference);
        init();
    }

    public void init() {
        Logger.addLogAdapter(new AndroidLogAdapter());
        mMultiFlowTagLayout = findViewById(R.id.flowlayout_multi_select);
        FlowTagAdapter tagAdapter = new FlowTagAdapter(this);
        mMultiFlowTagLayout.setAdapter(tagAdapter);
        mMultiFlowTagLayout.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        mMultiFlowTagLayout.setOnTagSelectListener((parent, position, selectedList) -> Logger.e(getSelectedText(parent, selectedList)));
        tagAdapter.addTags(ResUtils.getStringArray(R.array.tags_values));

        SmoothCheckBox sportsLevel1 = findViewById(R.id.sportsLevel1);
        SmoothCheckBox sportsLevel2 = findViewById(R.id.sportsLevel2);
        SmoothCheckBox sportsLevel3 = findViewById(R.id.sportsLevel3);

        LinearLayout sportsLevel1Button = findViewById(R.id.sportsLevel1Button);
        LinearLayout sportsLevel2Button = findViewById(R.id.sportsLevel2Button);
        LinearLayout sportsLevel3Button = findViewById(R.id.sportsLevel3Button);
        sportsLevel1Button.setOnClickListener(view -> sportsLevel1.callOnClick());
        sportsLevel2Button.setOnClickListener(view -> sportsLevel2.callOnClick());
        sportsLevel3Button.setOnClickListener(view -> sportsLevel3.callOnClick());

        sportsLevel1.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked) {
                sportsLevel2.setChecked(false);
                sportsLevel3.setChecked(false);
            }
            Logger.e("1" + isChecked);
        });
        sportsLevel2.setOnCheckedChangeListener((checkBox, isChecked) -> {
            Logger.e("2" + isChecked);
            if (isChecked) {
                sportsLevel1.setChecked(false);
                sportsLevel3.setChecked(false);
            }

        });
        sportsLevel3.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked) {
                sportsLevel2.setChecked(false);
                sportsLevel1.setChecked(false);
            }
            Logger.e("3" + isChecked);
        });
    }

    private String getSelectedText(FlowTagLayout parent, List<Integer> selectedList) {
        StringBuilder sb = new StringBuilder("选中的内容：\n");
        for (int index : selectedList) {
            sb.append(parent.getAdapter().getItem(index));
            sb.append(";");
        }
        return sb.toString();
    }
}
