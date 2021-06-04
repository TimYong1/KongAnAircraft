package com.tourcoo.account;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraft.ui.account.LoginNewActivity;
import com.tourcoo.aircraft.ui.sample.AircraftApplication;
import com.tourcoo.config.AppConfig;
import com.tourcoo.entity.account.SasTokenBean;
import com.tourcoo.util.SpUtil;
import com.tourcoo.util.StackUtil;
import com.tourcoo.util.StringUtil;


import io.rong.imlib.RongIMClient;

import static com.tourcoo.constant.AccountConstant.PREF_KEY_REFRESH_TOKEN;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_RY_TOKEN;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_SAS_TENANT;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_SYS_TOKEN;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_USER_CODE;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_USER_ID;
import static com.tourcoo.constant.CommonConstant.APP_TYPE_KONG_AN;
import static com.tourcoo.constant.CommonConstant.APP_TYPE_PRO;
import static com.tourcoo.constant.CommonConstant.APP_TYPE_SAS;
import static com.tourcoo.retrofit.RequestConfig.SOCKET_KONG_AN_URL_IP;
import static com.tourcoo.retrofit.RequestConfig.SOCKET_PRO_URL_IP;
import static com.tourcoo.retrofit.RequestConfig.SOCKET_SAS_URL_IP;

/**
 * @author :JenkinsZhou
 * @description : 账户管理类(SAS)
 * @company :途酷科技
 * @date 2021年03月22日10:05
 * @Email: 971613168@qq.com
 */
public class AccountHelper {
    public static final String TAG = "AccountHelper";
    /**
     * 系统token
     */
    private static String sysToken = "";

    /**
     * 刷新token时需要的token
     */
    private static String refreshToken = "";
    /**
     * 融云相关token
     */
    private static String ryToken = "";

    private String userId = "";
    private String userCode = "";
    private String socketUrl = "";
    /**
     * 租户信息（仅SAS系统需要）
     */
    private String sasTenant;


    private SasUserInfo userInfo;

    private static class Holder {
        private static final AccountHelper instance = new AccountHelper();
    }


    private AccountHelper() {

    }


    public static AccountHelper getInstance() {
        return Holder.instance;
    }

    /**
     * 获取租户信息
     *
     * @return
     */
    public String getSasTenant() {
        if (TextUtils.isEmpty(sasTenant)) {
            sasTenant = SpUtil.INSTANCE.getString(PREF_KEY_SAS_TENANT);
            if (null == sasTenant) {
                sasTenant = "";
            }
            return sasTenant;
        }
        return sasTenant;
    }

    public void setSasTenant(String tenant) {
        if (null == tenant) {
            tenant = "";
        }
        sasTenant = tenant;
        SpUtil.INSTANCE.put(PREF_KEY_SAS_TENANT, tenant);
    }

    public String getSysToken() {
        if (TextUtils.isEmpty(sysToken)) {
            sysToken = SpUtil.INSTANCE.getString(PREF_KEY_SYS_TOKEN);
            if (null == sysToken) {
                sysToken = "";
            }
            return sysToken;
        }
        return sysToken;
    }

    public String getRefreshToken() {
        if (TextUtils.isEmpty(refreshToken)) {
            refreshToken = SpUtil.INSTANCE.getString(PREF_KEY_REFRESH_TOKEN);
            if (null == refreshToken) {
                refreshToken = "";
            }
            return refreshToken;
        }
        return refreshToken;
    }

    public void setRefreshToken(String token) {
        if (null == token) {
            token = "";
        }
        refreshToken = token;
        SpUtil.INSTANCE.put(PREF_KEY_REFRESH_TOKEN, token);
    }


    public static String getRyToken() {
        if (TextUtils.isEmpty(ryToken)) {
            ryToken = SpUtil.INSTANCE.getString(PREF_KEY_RY_TOKEN);
            if (null == ryToken) {
                ryToken = "";
            }
            return ryToken;
        }
        return ryToken;
    }

    public String getUserId() {
        if (TextUtils.isEmpty(userId)) {
            userId = SpUtil.INSTANCE.getString(PREF_KEY_USER_ID);
            if (null == userId) {
                userId = "";
            }
            return userId;
        }
        return userId;
    }

    public String getUserCode() {
        if (TextUtils.isEmpty(userCode)) {
            userCode = SpUtil.INSTANCE.getString(PREF_KEY_USER_CODE);
            if (null == userCode) {
                userCode = "";
            }
            return userCode;
        }
        return userCode;
    }

    public String getSocketUrl() {
        if (!AccountHelper.getInstance().isLogin()) {
            socketUrl = "";
        }
        switch (AppConfig.APP_TYPE) {

            case APP_TYPE_KONG_AN:
                //新空安 要求传userId
                socketUrl = SOCKET_KONG_AN_URL_IP + getUserId();
                break;
            case APP_TYPE_SAS:
                //sas系统传 租户id和设备id
                socketUrl = SOCKET_SAS_URL_IP + getSasTenant() + "/" + ProductManager.getInstance().getDroneId();
                break;
            case APP_TYPE_PRO:
                //检察院要求传userCode
                socketUrl = SOCKET_PRO_URL_IP + getUserCode();
            default:
                break;
        }
        return socketUrl;
    }

    public void setSysToken(String token) {
        if (null == token) {
            token = "";
        }
        sysToken = token;
        SpUtil.INSTANCE.put(PREF_KEY_SYS_TOKEN, token);
    }

    public void setRyToken(String token) {
        if (null == token) {
            token = "";
        }
        ryToken = token;
        SpUtil.INSTANCE.put(PREF_KEY_RY_TOKEN, token);
    }

    public void setUserId(String id) {
        if (null == id) {
            id = "";
        }
        userId = id;
        SpUtil.INSTANCE.put(PREF_KEY_USER_ID, id);
    }

    private void setUserCode(String code) {
        if (null == code) {
            code = "";
        }
        userCode = code;
        SpUtil.INSTANCE.put(PREF_KEY_USER_CODE, code);
    }

    /**
     * 登录
     *
     * @param tokenBean
     */
    public void login(SasTokenBean tokenBean) {
        if (tokenBean == null) {
            return;
        }
        setSysToken(tokenBean.getToken());
        setUserId(tokenBean.getUserId());
        setRefreshToken(tokenBean.getRefreshToken());


    }


    public void logout() {
        setSysToken(null);
        setRyToken(null);
        setUserId(null);
        setUserCode(null);
        try {
            if (RongIMClient.getInstance() != null) {
                RongIMClient.getInstance().disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG + e.toString());
        }
    }

    public void skipLogin() {
        Intent intent = new Intent(AircraftApplication.getContext(), LoginNewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AircraftApplication.getContext().startActivity(intent);
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getSysToken())  && !TextUtils.isEmpty(getUserId());
    }


    public void setUserInfo(SasUserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        setUserId(StringUtil.getNotNullValue(userInfo.getId()));
        this.userInfo = userInfo;
    }



    public SasUserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * 退出并且跳转到登录
     */
    public void logoutAndSkipLogin() {
        logout();
        StackUtil.getInstance().popAll();
        skipLogin();
    }
}
