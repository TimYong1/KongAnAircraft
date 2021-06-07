package com.tourcoo.aircraft.product;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;

import dji.common.product.Model;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.accessory.beacon.Beacon;
import dji.sdk.accessory.speaker.Speaker;
import dji.sdk.accessory.spotlight.Spotlight;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月07日14:06
 * @Email: 971613168@qq.com
 */
public class AircraftUtil {

    public static boolean isAircraft() {
        return ProductManager.getProductInstance() instanceof Aircraft;
    }

    public static boolean isGimbalModuleAvailable() {
        return isProductModuleAvailable() && (null != ProductManager.getProductInstance().getGimbal());
    }
    public static boolean isHandHeld() {
        return ProductManager.getProductInstance() instanceof HandHeld;
    }


    public static boolean isPlaybackAvailable() {
        return isCameraModuleAvailable() && (null != ProductManager.getProductInstance()
                .getCamera()
                .getPlaybackManager());
    }

    public static boolean isRemoteControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null !=getAircraftInstance().getRemoteController());
    }

    public static boolean isFlightControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != getAircraftInstance().getFlightController());
    }

    public static boolean isCompassAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != getAircraftInstance()
                .getFlightController()
                .getCompass());
    }

    public static boolean isFlightLimitationAvailable() {
        return isFlightControllerAvailable() && isAircraft();
    }









    public static boolean isMavicAir2() {
        BaseProduct baseProduct = ProductManager.getProductInstance();
        if (baseProduct != null) {
            LogUtils.d("设备型号=" + baseProduct.getModel());
            return baseProduct.getModel() == Model.MAVIC_AIR_2;
        }
        return false;
    }
    public static boolean isMavicAir() {
        BaseProduct baseProduct = ProductManager.getProductInstance();
        if (baseProduct != null) {
            LogUtils.d("设备型号=" + baseProduct.getModel());
            return baseProduct.getModel() == Model.MAVIC_AIR;
        }
        return false;
    }



    public static boolean isMatchModel(Model model) {
        BaseProduct baseProduct = ProductManager.getProductInstance();
        if (baseProduct != null) {
            LogUtils.d("设备型号=" + baseProduct.getModel());
            return baseProduct.getModel() == model;
        }
        return false;
    }

    /**
     * 相机模块是否可用
     *
     * @return
     */
    public static boolean isCameraModuleAvailable() {
        return isProductModuleAvailable() && (null != ProductManager.getProductInstance().getCamera());
    }

    /**
     * 媒体管理是否可用
     *
     * @return
     */
    public static boolean isMediaManagerAvailable() {
        return isCameraModuleAvailable() && (null != ProductManager.getProductInstance().getCamera().getMediaManager());
    }

    public static boolean isProductModuleAvailable() {
        return (null != ProductManager.getProductInstance());
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) ProductManager.getProductInstance();
    }

    public static boolean isAircraftConnected() {
        return ProductManager.getProductInstance() != null && ProductManager.getProductInstance() instanceof Aircraft;
    }

}
