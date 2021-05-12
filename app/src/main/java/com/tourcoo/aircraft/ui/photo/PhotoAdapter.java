package com.tourcoo.aircraft.ui.photo;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.GlideManager;

import java.util.List;

import dji.sdk.media.MediaFile;


/**
 * @author :JenkinsZhou
 * @description : 相册适配器
 * @company :途酷科技
 * @date 2021年04月08日15:52
 * @Email: 971613168@qq.com
 */
public class PhotoAdapter extends BaseMultiItemQuickAdapter<MediaFileGroupOld, BaseViewHolder> {
    public static final String TAG = "PhotoAdapter";
    private boolean isScrolling = false;

    public PhotoAdapter(List<MediaFileGroupOld> data) {
        super(data);
        addItemType(0, R.layout.item_photo);
        addItemType(1, R.layout.item_photo_title);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MediaFileGroupOld item) {


        switch (item.getItemType()) {
            case 0:
                ImageView ivPhoto = helper.getView(R.id.ivPhoto);
                if (item.getMediaFile() == null || isScrolling) {
                    LogUtils.e("已被拦截");
                    return;
                }
                MediaFile mediaFile = item.getMediaFile();
//                !isScrolling
                if (mediaFile != null && mediaFile.getThumbnail() != null ) {
                    boolean isPhoto = mediaFile.getMediaType() == MediaFile.MediaType.JPEG || mediaFile.getMediaType() == MediaFile.MediaType.RAW_DNG;
                    if (!isPhoto) {
                        helper.setText(R.id.tvVideoDuration, mediaFile.getDurationInSeconds() + "");
                    }
                    helper.setGone(R.id.llVideo, !isPhoto);
                    GlideManager.loadImgAuto(item.getMediaFile().getThumbnail(), ivPhoto);
                    LogUtils.i("缩略图日期：" + item.getMediaFile().getTimeCreated());
                    LogUtils.i("缩略图尺寸" + ivPhoto.getWidth() + "---" + ivPhoto.getHeight());
//            LogUtils.d("缩略图尺寸"+SizeUtil.px2dp(343));
                } else {
                    LogUtils.w("缩略图为null");
                }

                break;
            case 1:
                //标题模块
                TextView textView = helper.getView(R.id.photoTitle);
                if (!TextUtils.isEmpty(item.getTitle())) {
                    textView.setText(item.getTitle());
                }

                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getData().get(position).getMediaFile() == null ? 1 : 0;
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }
}
