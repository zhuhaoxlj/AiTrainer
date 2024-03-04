package com.lindum.mlkit.vision.dev.view;

/**
 * @author MarkGosling
 * @date 2022/2/15 1:08
 */

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.lindum.mlkit.vision.dev.R;
import com.xuexiang.xui.widget.flowlayout.BaseTagAdapter;

/**
 * @author xuexiang
 * @date 2017/11/21 上午10:44
 */
public class FlowTagAdapter extends BaseTagAdapter<String, TextView> {

    public FlowTagAdapter(Context context) {
        super(context);
    }

    @Override
    protected TextView newViewHolder(View convertView) {
        return (TextView) convertView.findViewById(R.id.tv_tag);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_item_tag;
    }

    @Override
    protected void convert(TextView textView, String item, int position) {
        textView.setText(item);
    }
}
