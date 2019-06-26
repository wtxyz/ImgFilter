package com.cv.ViewAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cv.Entity.FilterModel;
import com.cv.imgfilter.R;
import java.util.List;

public class FilterListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private List<FilterModel> photoFiltersList;

    public FilterListAdapter(List<FilterModel> list){
        this.photoFiltersList = list;
    }

    //外部监听
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener = null;
    public static class ViewHolderFilterList extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public ViewHolderFilterList(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_filter_list_imageview);
            textView = itemView.findViewById(R.id.item_filter_list_textview);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_filters_list_layout,viewGroup,false);
        ViewHolderFilterList viewHolderFilterList = new ViewHolderFilterList(view);
        view.setOnClickListener(this);
        return viewHolderFilterList;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        FilterModel filterModel = photoFiltersList.get(i);

        ((ViewHolderFilterList)viewHolder).textView.setText(filterModel.getFilterName());
        ((ViewHolderFilterList)viewHolder).imageView.setImageBitmap(filterModel.getFilterSampleImage());
        viewHolder.itemView.setTag(i);
    }

    @Override
    public int getItemCount() {
        return photoFiltersList.size();
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
