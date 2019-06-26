package com.cv.ViewAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cv.imgfilter.R;
import com.cv.utils.AlbumModel;
import com.cv.utils.GetWindowSizeUtil;

import java.util.List;

public class AlbumRecyclerViewHolder extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private List<AlbumModel> mphotoItemDataList;
    private View view;
    private int xOffset;
    private static final int TYPE_FIRST_BTN= 1;
    private static final int TYPE_IMAGE = 2;
    private final int TAKE_REQUEST_CODE = 3;

    public static AlbumRecyclerViewHolder newInstance(List<AlbumModel> albumModelList){

        return new AlbumRecyclerViewHolder(albumModelList);

    }
    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener = null;

    private AlbumRecyclerViewHolder(List<AlbumModel> albumModelList){
        mphotoItemDataList = albumModelList;
    }
    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return TYPE_FIRST_BTN;
        }else{
            return TYPE_IMAGE;
        }

    }
    static class ViewHolderFirstBtn extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        ImageView imageView2;
        ViewHolderFirstBtn(View view){
            super(view);
            imageView =  view.findViewById(R.id.item_first_btn);
            textView = view.findViewById(R.id.item_first_btn_text);
            imageView2 = view.findViewById(R.id.item_first_btn_for);
        }
    }

    static class ViewHolderImage extends RecyclerView.ViewHolder{
        ImageView imageView;
        ViewHolderImage(View view){
            super(view);
            imageView = view.findViewById(R.id.photo_item);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {

        if (i == TYPE_IMAGE) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_all_photo, viewGroup, false);
            //此处监听
            final ViewHolderImage viewHolder = new ViewHolderImage(view);
            xOffset = GetWindowSizeUtil.getWindowSize(viewGroup).x;
            view.setOnClickListener(this);
            return viewHolder;
        } else if (i == TYPE_FIRST_BTN) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_all_first, viewGroup, false);
            //此处监听
            final ViewHolderFirstBtn viewHolder = new ViewHolderFirstBtn(view);
            xOffset = GetWindowSizeUtil.getWindowSize(viewGroup).x;
            view.setOnClickListener(this);
            return viewHolder;
        } else {
            throw new RuntimeException("The type has to be ONE or TWO");
        }
    }//onCreate


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {

        AlbumModel albumModel = mphotoItemDataList.get(i);
        int TYPE = viewHolder.getItemViewType();
        switch (TYPE){
            case TYPE_FIRST_BTN:
                CoordinatorLayout.LayoutParams layoutParamsFirstBtn =(CoordinatorLayout.LayoutParams) ((ViewHolderFirstBtn)viewHolder).imageView.getLayoutParams();
                layoutParamsFirstBtn.height = xOffset/3-1;
                layoutParamsFirstBtn.width = xOffset/3-1;
                break;
            case TYPE_IMAGE:
                RequestOptions requestOptions = new RequestOptions();
                CoordinatorLayout.LayoutParams layoutParams =(CoordinatorLayout.LayoutParams) ((ViewHolderImage)viewHolder).imageView.getLayoutParams();
                layoutParams.height = xOffset/3-1;
                layoutParams.width = xOffset/3-1;
                Glide.with(view).load(albumModel.getmImgUrlId()).into( ((ViewHolderImage)viewHolder).imageView);
                break;
            default:
                break;
        }
        viewHolder.itemView.setTag(i);//将position放在Tag中

    }

    //item click
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int) v.getTag());//position
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {

        return mphotoItemDataList.size();
    }


}
