package com.cv.ViewAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cv.imgfilter.R;

import java.util.List;

public class ColorListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private List<Integer> colorList;//color value = int

    public ColorListViewAdapter(List<Integer> colorList){

        this.colorList = colorList;
    }


    //外部监听
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener = null;


    public static class ViewHolderColorList extends RecyclerView.ViewHolder{
        ImageView imageView;


        public ViewHolderColorList(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.color_show_item);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_color_layout, viewGroup, false);
            ViewHolderColorList holderColorList = new ViewHolderColorList(view);
            view.setOnClickListener(this);
            return holderColorList;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            int colorValue = colorList.get(i);
            ((ViewHolderColorList)viewHolder).imageView.setBackgroundColor(colorValue);
            viewHolder.itemView.setTag(i);
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {

            mOnItemClickListener.onItemClick(v, (int) v.getTag());//position
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
