package com.tourcoo.aircraft.ui.sample.showcase.defaultlayout;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.tourcoo.aircraftmanager.R;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年06月08日17:12
 * @Email: 971613168@qq.com
 */
public class FlyControlActivityTest extends RxAppCompatActivity implements View.OnClickListener {
    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_control_1);
    }
}
