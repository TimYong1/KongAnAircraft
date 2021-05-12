package com.tourcoo.aircraft.ui.photo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.entity.media.MediaEntity;
import com.tourcoo.entity.media.MediaGroup;
import com.tourcoo.util.StringUtil;

import java.util.List;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月10日16:51
 * @Email: 971613168@qq.com
 */
public class PhotoGroupAdapter extends BaseQuickAdapter<MediaGroup, BaseViewHolder> {

    public PhotoGroupAdapter(List<MediaGroup> mediaGroupList) {
        super(R.layout.item_photo_group,mediaGroupList);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MediaGroup item) {
        if (item == null || item.getMediaEntityList() == null) {
            return;
        }
        helper.setText(R.id.photoTitle, StringUtil.getNotNullValueLine(item.getDateTitle()));
        RecyclerView photoRecyclerView = helper.getView(R.id.photoRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 5);
       /* ImageAdapter adapter = new ImageAdapter(item.getMediaEntityList());
        photoRecyclerView.setLayoutManager(gridLayoutManager);
        adapter.bindToRecyclerView(photoRecyclerView);*/
    }
}
