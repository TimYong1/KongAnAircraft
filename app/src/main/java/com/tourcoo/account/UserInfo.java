package com.tourcoo.account;

/**
 * @author :JenkinsZhou
 * @description : 用户信息
 * @company :途酷科技
 * @date 2021年04月20日15:07
 * @Email: 971613168@qq.com
 */
public class UserInfo {


    /**
     * createTime : 2021-03-19 11:33:25
     * enabled : true
     * nickName : yan
     * phone : 18133676739
     * userId : 1372753043531108352
     * username : yan
     * voice : false
     */

    private String createTime;
    private boolean enabled;
    private String nickName;
    private String phone;
    private String userId;
    private String username;
    private boolean voice;
    private String email;
    private String gender;
    private String userCode;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isVoice() {
        return voice;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
