package com.tourcoo.account;

import android.content.Intent;
import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.ui.account.LoginNewActivity;
import com.tourcoo.aircraft.ui.sample.AircraftApplication;
import com.tourcoo.util.SpUtil;
import com.tourcoo.util.StackUtil;


import io.rong.imlib.RongIMClient;

import static com.tourcoo.constant.AccountConstant.PREF_KEY_RY_TOKEN;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_SYS_TOKEN;
import static com.tourcoo.constant.AccountConstant.PREF_KEY_USER_ID;
import static com.tourcoo.retrofit.RequestConfig.SOCKET_URL_IP;

/**
 * @author :JenkinsZhou
 * @description : 账户管理类
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
     * 融云相关token
     */
    private static String ryToken = "";

    private String userId = "";
    private String socketUrl = "";
    private UserInfo userInfo;

    private static class Holder {
        private static final AccountHelper instance = new AccountHelper();
    }


    private AccountHelper() {

    }


    public static AccountHelper getInstance() {
        return Holder.instance;
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

    public String getSocketUrl() {
        if (!AccountHelper.getInstance().isLogin()) {
            socketUrl = "";
        }
        socketUrl = SOCKET_URL_IP + getUserId();
        return socketUrl;
    }

    private void setSysToken(String token) {
        if (null == token) {
            token = "";
        }
        sysToken = token;
        SpUtil.INSTANCE.put(PREF_KEY_SYS_TOKEN, token);
    }

    private void setRyToken(String token) {
        if (null == token) {
            token = "";
        }
        ryToken = token;
        SpUtil.INSTANCE.put(PREF_KEY_RY_TOKEN, token);
    }

    private void setUserId(String id) {
        if (null == id) {
            id = "";
        }
        userId = id;
        SpUtil.INSTANCE.put(PREF_KEY_USER_ID, id);
    }

    /**
     * 登录
     *
     * @param token
     */
    public void login(TokenInfo token) {
        if (token == null) {
            return;
        }
        setSysToken(token.getSystemToken());
        setRyToken(token.getRongCloudToken());
        setUserId(token.getUserId());
    }


    public void logout() {
        setSysToken(null);
        setRyToken(null);
        setUserId(null);
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
        StackUtil.getInstance().popAll();
        Intent intent = new Intent(AircraftApplication.getContext(), LoginNewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AircraftApplication.getContext().startActivity(intent);
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getSysToken()) && !TextUtils.isEmpty(getRyToken()) && !TextUtils.isEmpty(getUserId());
    }


    public void setUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo(){
        return userInfo;
    }

    public void logoutAndSkipLogin(){
        logout();
        skipLogin();
    }
}
