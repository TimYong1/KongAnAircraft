package com.tourcoo.aircraft.ui.banner;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tourcoo.aircraftmanager.R;

public class ImageHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public TextView tvDesc;
    public ImageHolder(@NonNull View view) {
        super(view);
        this.imageView = view.findViewById(R.id.ivBanner);
        this.tvDesc = view.findViewById(R.id.tvDesc);
    }
}