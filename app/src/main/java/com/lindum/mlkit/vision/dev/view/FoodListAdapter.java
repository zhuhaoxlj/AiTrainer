package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lindum.mlkit.vision.dev.R;

import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/2/15 3:39
 */
public class FoodListAdapter extends ArrayAdapter<FoodEntity> {
    private final int resourceId;

    public FoodListAdapter(Context context, int textViewResourceId, List<FoodEntity> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FoodEntity footEntity = getItem(position);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        ImageView foodImage = view.findViewById(R.id.foodImage);
        TextView fibre = view.findViewById(R.id.fibre);
        TextView cal = view.findViewById(R.id.cal);
        TextView foodName = view.findViewById(R.id.foodName);
        foodImage.setImageDrawable(footEntity.getFoodImage());
        fibre.setText(footEntity.getFibre());
        cal.setText(footEntity.getCal());
        foodName.setText(footEntity.getFoodName());
        return view;
    }
}
