package com.lindum.mlkit.vision.dev.view;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lindum.mlkit.vision.dev.R;

import java.util.ArrayList;

/**
 * @author MarkGosling
 * @date 2022/1/27 5:26
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private ArrayList<PoseLibraryItem> mValues;
    private Context mContext;
    protected ItemListener mListener;

    public HomeAdapter(Context context, ArrayList<PoseLibraryItem> values, ItemListener itemListener) {
        mValues = values;
        mContext = context;
        mListener = itemListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Item 标题
        private TextView title;
        // Item 副标题
        private TextView subTitle;
        private ImageView imageView;
        private RelativeLayout relativeLayout;
        private PoseLibraryItem item;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            title = v.findViewById(R.id.title);
            subTitle = v.findViewById(R.id.subTitle);
            imageView = v.findViewById(R.id.imageView);
            relativeLayout = v.findViewById(R.id.relativeLayout);
        }

        public void setData(PoseLibraryItem item) {
            this.item = item;
            title.setText(item.title);
            subTitle.setText(item.subTitle);
//            imageView.setImageDrawable(item.getBgDrawable());
            relativeLayout.setBackground(new BitmapDrawable(mContext.getResources(), item.getBitmap()));
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.setData(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface ItemListener {
        void onItemClick(PoseLibraryItem item);
    }
}
