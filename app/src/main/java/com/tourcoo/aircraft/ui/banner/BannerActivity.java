package com.tourcoo.aircraft.ui.banner;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.SizeUtil;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.transformer.ScaleInTransformer;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.flightcontroller.RTK;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月20日14:53
 * @Email: 971613168@qq.com
 */
public class BannerActivity extends RxAppCompatActivity {
    private ImageAdapter bannerImageAdapter;
    private Banner banner;
    private List<BannerBean> imageIdArrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_layout);
        banner = findViewById(R.id.banner);
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initImages();
        initBanner();
    }

    private void initImages() {
        imageIdArrayList.clear();
        BannerBean bannerBean1 = new BannerBean();
        bannerBean1.setImageRes(R.drawable.mavic_mini);
        bannerBean1.setDesc("mavic_mini");

        BannerBean bannerBean2 = new BannerBean();
        bannerBean2.setImageRes(R.drawable.matrice_rtk_three);
        bannerBean2.setDesc("经纬 M300 RTK");


        BannerBean bannerBean3 = new BannerBean();
        bannerBean3.setImageRes(R.drawable.mavic_air_two);
        bannerBean3.setDesc("mavic_air_2");
        BannerBean bannerBea4 = new BannerBean();
        bannerBea4.setImageRes(R.drawable.mavic_two_enterprise_dual);
        bannerBea4.setDesc("mavic_air_2_行业进阶版");

        BannerBean bannerBean5 = new BannerBean();
        bannerBean5.setImageRes(R.drawable.phantom_four_pro);
        bannerBean5.setDesc("精灵4PRO V2.0");
        imageIdArrayList.add(bannerBean1);
        imageIdArrayList.add(bannerBean2);
        imageIdArrayList.add(bannerBean3);
        imageIdArrayList.add(bannerBea4);
        imageIdArrayList.add(bannerBean5);
    }

    private void initBanner() {
        //添加生命周期观察者
        banner.setAdapter(new ImageAdapter(imageIdArrayList),false);
        banner.setIndicator(new CircleIndicator(this));
        //添加画廊效果
        banner.setBannerGalleryEffect(80, 10,0.82f);

        LogUtils.i("屏幕宽度："+getScreenWidth());
    }


    public int getScreenWidth() {
        Point point = new Point();
        WindowManager manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(point);
        return point.x;
    }



}
