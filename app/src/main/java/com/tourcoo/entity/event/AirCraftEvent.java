package com.tourcoo.entity.event;

import dji.sdk.base.BaseProduct;

/**
 * @author :JenkinsZhou
 * @description : 无人机相关事件
 * @company :途酷科技
 * @date 2021年04月22日11:13
 * @Email: 971613168@qq.com
 */
public class AirCraftEvent {
    private BaseProduct baseProduct;
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public BaseProduct getBaseProduct() {
        return baseProduct;
    }

    public void setBaseProduct(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    public AirCraftEvent(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    public AirCraftEvent() {
    }

    public AirCraftEvent(int code) {
        this.code = code;
    }
}
