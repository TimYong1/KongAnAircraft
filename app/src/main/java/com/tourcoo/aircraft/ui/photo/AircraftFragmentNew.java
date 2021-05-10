package com.tourcoo.aircraft.ui.photo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tourcoo.aircraftmanager.R;
import com.trello.rxlifecycle3.components.support.RxFragment;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月10日13:52
 * @Email: 971613168@qq.com
 */
public class AircraftFragmentNew extends RxFragment {
    private View contentView;
    private RecyclerView mCommonRecyclerView;
    private Handler mHandler;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_recyclerview, container, false);
        mCommonRecyclerView = contentView.findViewById(R.id.mCommonRecyclerView);
        return contentView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        Activity activity = getActivity();
        if (activity != null) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

    }
}
