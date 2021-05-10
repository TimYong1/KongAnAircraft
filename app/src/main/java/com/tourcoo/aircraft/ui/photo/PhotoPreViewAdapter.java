package com.tourcoo.aircraft.ui.photo;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.PhotoView;
import com.tourcoo.aircraftmanager.R;

import java.util.List;

import dji.sdk.media.MediaFile;


/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年04月22日16:27
 * @Email: 971613168@qq.com
 */
public class PhotoPreViewAdapter extends PagerAdapter {
    private List<View> mViewList;
    private List<MediaFile> mediaFileGroupList;

    public PhotoPreViewAdapter(List<View> mViewList, List<MediaFile> mediaFileGroupList) {
        this.mViewList = mViewList;
        this.mediaFileGroupList = mediaFileGroupList;
    }

    @Override
    public int getCount() {
        //必须实现
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        //必须实现
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //必须实现，实例化
        View currentView = mViewList.get(position);
        if (position < mediaFileGroupList.size()) {
            MediaFile mediaFileGroup = mediaFileGroupList.get(position);
            PhotoView imageView = currentView.findViewById(R.id.photoPreview);
            if (mediaFileGroup != null && mediaFileGroup.getPreview() != null) {
//                GlideManager.loadImgAuto(mediaFileGroup.getMediaFile().getPreview(), currentView);
                imageView.setImageResource(R.mipmap.ic_fly_photo);
            }
        }
        container.addView(currentView);
        return mViewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //必须实现，销毁
        container.removeView(mViewList.get(position));
    }
}
