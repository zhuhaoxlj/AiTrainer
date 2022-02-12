package com.lindum.mlkit.vision.demo.java.result;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lindum.mlkit.vision.demo.R;
import com.lindum.mlkit.vision.demo.java.CameraXLivePreviewActivity;

import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/2/12 10:31
 */
public class ResultAdapter extends ArrayAdapter<ResultEntity> {
    private final int resourceId;

    public ResultAdapter(Context context, int textViewResourceId, List<ResultEntity> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ResultEntity resultEntity = getItem(position);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        ImageView rightPoseImage = view.findViewById(R.id.rightPoseImage);
        ImageView wrongPoseImage = view.findViewById(R.id.wrongPoseImage);
        view.setPadding(5, 5, 5, 5);
        TextView poseName = view.findViewById(R.id.poseResultName);
        TextView poseTips = view.findViewById(R.id.poseResultTips);
        rightPoseImage.setImageBitmap(resultEntity.getRightPoseImage());
        wrongPoseImage.setImageBitmap(resultEntity.getWrongPoseImage());
        poseName.setText(resultEntity.getPoseName());
        poseTips.setText(resultEntity.getTips());
        return view;
    }
}
