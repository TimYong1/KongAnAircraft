package com.tourcoo.aircraft.ui.photo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tourcoo.aircraftmanager.R;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

/**
 * @author :JenkinsZhou
 * @description : 飞行相册
 * @company :途酷科技
 * @date 2021年04月08日15:17
 * @Email: 971613168@qq.com
 */
public class FlyPhotoActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_layout);
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 得到Fragment 管理器对象
        // Fragment 管理器
        FragmentManager fragmentManager = getSupportFragmentManager();
        // 开始得到Fragment 的事务处理
        // Fragment 事务处理
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AircraftPhotoFragment aircraftPhotoFragment = new AircraftPhotoFragment();
        // fragment_id 是布局中给fragment 占位置的控
        fragmentTransaction.add(R.id.flContainer, aircraftPhotoFragment);
        // 提交事务
        fragmentTransaction.commit();
    }
}
