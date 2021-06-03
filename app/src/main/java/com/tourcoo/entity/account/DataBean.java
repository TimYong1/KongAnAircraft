package com.tourcoo.entity.account;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年06月03日14:43
 * @Email: 971613168@qq.com
 */
public class DataBean {

    /**
     * token : eyJ0eXAiOiJKc29uV2ViVG9rZW4iLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoi6LaF57qn566h55CG5ZGYIiwidG9rZW5fdHlwZSI6InRva2VuIiwidXNlcmlkIjoiMiIsImFjY291bnQiOiJsYW1wIiwiaWF0IjoxNjIyNjkxMjIwLCJuYmYiOjE2MjI2OTEyMjAsImV4cCI6MTYyMjcyMDAyMH0.D_UHXSdiW2kZ_Nfzo8-VWIkHTaY6OQFiQW8Kt4Xawvc
     * tokenType : token
     * refreshToken : eyJ0eXAiOiJKc29uV2ViVG9rZW4iLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaF90b2tlbiIsInVzZXJpZCI6IjIiLCJpYXQiOjE2MjI2OTEyMjAsIm5iZiI6MTYyMjY5MTIyMCwiZXhwIjoxNjIyNzc3NjIwfQ.5sTOu_1JNtgBPL1p8JoIltjAlH_h3XQOxBakW8KU0pw
     * name : 超级管理员
     * account : lamp
     * avatar :
     * workDescribe : 不想上班!
     * userId : 2
     * expire : 28800
     * expiration : 2021-06-03 19:33:40
     * expireMillis : 28800
     */

    private String token;
    private String tokenType;
    private String refreshToken;
    private String name;
    private String account;
    private String avatar;
    private String workDescribe;
    private String userId;
    private int expire;
    private String expiration;
    private String expireMillis;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getWorkDescribe() {
        return workDescribe;
    }

    public void setWorkDescribe(String workDescribe) {
        this.workDescribe = workDescribe;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getExpireMillis() {
        return expireMillis;
    }

    public void setExpireMillis(String expireMillis) {
        this.expireMillis = expireMillis;
    }
}
