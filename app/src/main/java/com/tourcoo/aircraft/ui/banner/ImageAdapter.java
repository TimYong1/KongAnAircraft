package com.tourcoo.aircraft.ui.banner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.StringUtil;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

/**
 * 自定义布局，图片
 */
public class ImageAdapter extends BannerAdapter<BannerBean, ImageHolder> {

    public ImageAdapter(List<BannerBean> mDatas) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
    }

    //更新数据
    public void updateData(List<BannerBean> data) {
        //这里的代码自己发挥，比如如下的写法等等
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }


    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
//        ImageView imageView = new ImageView(parent.getContext());
     View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_view, parent, false);
        ImageView imageView = view.findViewById(R.id.ivBanner);
      /*  //注意，必须设置为match_parent，这个是viewpager2强制要求的
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);*/
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return new ImageHolder(view);
    }

    @Override
    public void onBindView(ImageHolder holder, BannerBean data, int position, int size) {
        holder.imageView.setImageResource(data.getImageRes());
        holder.tvDesc.setText(StringUtil.getNotNullValue(data.getDesc()));
    }

}
