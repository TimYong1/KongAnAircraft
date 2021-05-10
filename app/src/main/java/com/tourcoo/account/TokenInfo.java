package com.tourcoo.account;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年03月22日10:33
 * @Email: 971613168@qq.com
 */

public class TokenInfo {


    /**
     * rongCloudToken :
     * systemToken :
     */

    private String rongCloudToken;
    private String systemToken;
    private String userId;

    public String getRongCloudToken() {
        return rongCloudToken;
    }

    public void setRongCloudToken(String rongCloudToken) {
        this.rongCloudToken = rongCloudToken;
    }

    public String getSystemToken() {
        return systemToken;
    }

    public void setSystemToken(String systemToken) {
        this.systemToken = systemToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
