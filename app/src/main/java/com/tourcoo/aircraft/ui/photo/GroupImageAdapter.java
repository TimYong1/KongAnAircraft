package com.tourcoo.aircraft.ui.photo;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.entity.media.MediaEntity;
import com.tourcoo.entity.media.MediaFileGroup;
import com.tourcoo.util.BitmapUtil;
import com.tourcoo.util.DateUtil;
import com.tourcoo.util.GlideManager;
import com.tourcoo.util.StringUtil;

import java.util.List;

import dji.sdk.media.MediaFile;

import static com.tourcoo.constant.MediaConstant.MEDIA_TYPE_GROUP_CONTENT;
import static com.tourcoo.constant.MediaConstant.MEDIA_TYPE_GROUP_TITLE;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月10日17:15
 * @Email: 971613168@qq.com
 */
public class GroupImageAdapter extends BaseMultiItemQuickAdapter<MediaFileGroup, BaseViewHolder> {
    private boolean isScrolling = false;

    public GroupImageAdapter(List<MediaFileGroup> data) {
        super(data);
        addItemType(MEDIA_TYPE_GROUP_TITLE, R.layout.item_photo_title);
        addItemType(MEDIA_TYPE_GROUP_CONTENT, R.layout.item_photo);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder helper, MediaFileGroup item) {
      /*  if(isScrolling){
            return;
        }*/

        if (item == null) {
            return;
        }
        switch (item.getItemType()) {
            case MEDIA_TYPE_GROUP_CONTENT:
                //缩略图展示
                ImageView ivPhoto = helper.getView(R.id.ivPhoto);
                if (ivPhoto == null) {
                    LogUtils.e(TAG + "已被拦截");
                    return;
                }
                MediaEntity mediaEntity = item.getMediaEntity();
                MediaFile mediaFile;
                if (mediaEntity != null ) {
                    mediaFile = mediaEntity.getMedia();
                    if (mediaEntity.getThumbnailBytes() != null && mediaEntity.getThumbnailBytes().length > 0) {
                        LogUtils.i(TAG + "直接显示数据库图片");
                        ivPhoto.setImageBitmap(BitmapUtil.bytes2Bitmap(mediaEntity.getThumbnailBytes()));
//                        GlideManager.loadImgCenterCrop(BitmapUtil.bytes2Bitmap(mediaEntity.getThumbnailBytes()), ivPhoto, R.drawable.ic_aircraft_default);
                    } else {
                        if (mediaFile != null) {
                            LogUtils.w(TAG + "显示实时图片");
                            ivPhoto.setImageBitmap(mediaFile.getThumbnail());
//                            GlideManager.loadImgCenterCrop(mediaFile.getThumbnail(), ivPhoto, R.drawable.ic_aircraft_default);
                        } else {
                            LogUtils.e(TAG + "mediaFile==null");
                        }
                    }
                    if(mediaFile != null){
                        boolean isPhoto = mediaFile.getMediaType() == MediaFile.MediaType.JPEG || mediaFile.getMediaType() == MediaFile.MediaType.RAW_DNG;
                        if (!isPhoto) {
                            helper.setText(R.id.tvVideoDuration, DateUtil.stringForTime(mediaFile.getDurationInSeconds()) + "");
                        }
                        helper.setGone(R.id.llVideo, !isPhoto);
                    }
                } else {
                    LogUtils.i(TAG + "执行了缩略图复用");
                }
                break;
            case MEDIA_TYPE_GROUP_TITLE:
                //标题栏展示
                //标题模块
                TextView textView = helper.getView(R.id.photoTitle);
                if (textView != null) {
                    textView.setText(StringUtil.getNotNullValue(item.getTitle()));
                } else {
                    LogUtils.d(TAG + "执行了缩略图复用2");
                }
                break;
            default:
                break;
        }
    }


    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }
}
