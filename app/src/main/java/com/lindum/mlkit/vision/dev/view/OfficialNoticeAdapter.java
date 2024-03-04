package com.lindum.mlkit.vision.dev.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.entity.OfficialNoticeEntity;

import java.util.List;

/**
 * @author YipJing
 * @date 2022/3/7 14:57
 */
public class OfficialNoticeAdapter extends ArrayAdapter<OfficialNoticeEntity>{
    private final int resourseId;

    public OfficialNoticeAdapter(Context context, int textViewResourseId, List<OfficialNoticeEntity> objects) {
        super(context, textViewResourseId, objects);
        resourseId =textViewResourseId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        OfficialNoticeEntity chat = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourseId, parent,false);
        XCRoundImageView chatItemPortrait = (XCRoundImageView) view.findViewById(R.id.chat_item_portrait);
        TextView nickName = (TextView) view.findViewById(R.id.chat_item_name);
        TextView time = (TextView) view.findViewById(R.id.chat_item_time);
        TextView chatContent = (TextView) view.findViewById(R.id.chat_item_content);
        chatItemPortrait.setImageResource(chat.getImageId());
        nickName.setText(chat.getNickName());
        time.setText(chat.getTime());
        chatContent.setText(chat.getChatContent());
        return view;
    }
}
