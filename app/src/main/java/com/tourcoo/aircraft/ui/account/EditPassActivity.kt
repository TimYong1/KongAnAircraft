package com.tourcoo.aircraft.ui.account

import android.os.Bundle
import android.text.InputType
import android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.apkfuns.logutils.LogUtils
import com.tourcoo.account.AccountHelper
import com.tourcoo.aircraftmanager.R
import com.tourcoo.entity.BaseCommonResult
import com.tourcoo.retrofit.BaseLoadingObserver
import com.tourcoo.retrofit.RequestConfig
import com.tourcoo.retrofit.repository.ApiRepository
import com.tourcoo.util.RsaUtils
import com.tourcoo.util.RsaUtils.PUBLISH_KEY
import com.tourcoo.util.ToastUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_pass.*

/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2021年05月21日10:26
 * @Email: 971613168@qq.com
 */
class EditPassActivity : RxAppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pass)
        ivBack.setOnClickListener(this)
        tvEditConfirm.setOnClickListener(this)
        controlPassVisible(etPassOld, ivPassVisible1)
        controlPassVisible(etUserPassNew, ivPassNewVisible)
        controlPassVisible(etPassConfirm, ivPassConfirmVisible)
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
        showUserInfo()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvEditConfirm -> {
                doEditPass()
            }
            else -> {
            }
        }
    }


    private fun hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        val decorView: View = this.window.decorView
        decorView.systemUiVisibility = 0
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = uiOptions
    }

    private fun showUserInfo() {
        if (AccountHelper.getInstance().userInfo == null || (!AccountHelper.getInstance().isLogin)) {
            AccountHelper.getInstance().skipLogin()
            return
        }
        tvUserAccount.text = AccountHelper.getInstance().userInfo.username
    }


    private fun requestEditPass(pass: String, newPass: String) {
        ApiRepository.getInstance().requestEditPass(pass, newPass).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseCommonResult<Any?>?>() {

            override fun onRequestSuccess(entity: BaseCommonResult<Any?>?) {
                handleEditSuccess(entity)
                hideNavigation()
            }

            override fun onRequestError(throwable: Throwable) {
                super.onRequestError(throwable)
                LogUtils.tag(TAG).e("onRequestError=$throwable")
                hideNavigation()
            }


        })
    }


    private fun handleEditSuccess(entity: BaseCommonResult<Any?>?) {
        if (entity == null) {
            ToastUtil.showFailed("服务器走丢了")
            return
        }
        if (RequestConfig.RESPONSE_CODE_SUCCESS == entity.status) {
            ToastUtil.showSuccess("密码修改成功，请重新登录")
            requestLogout()
        } else {
            ToastUtil.showNormal(entity.message)
        }
    }


    private fun doEditPass() {
        if (TextUtils.isEmpty(etPassOld.text.toString())) {
            ToastUtil.showNormal("请输入原密码")
            return
        }
        if (TextUtils.isEmpty(etUserPassNew.text.toString())) {
            ToastUtil.showNormal("请输入新密码")
            return
        }
        if (TextUtils.isEmpty(etPassConfirm.text.toString())) {
            ToastUtil.showNormal("请输入确认密码")
            return
        }
        if (etPassConfirm.text.toString() != (etUserPassNew.text.toString())) {
            ToastUtil.showNormal("两次密码输入不一致")
            return
        }
        val oldPassEnCode = RsaUtils.encryptByPublicKey(PUBLISH_KEY, etPassOld.text.toString())
        val newPassEnCode = RsaUtils.encryptByPublicKey(PUBLISH_KEY, etUserPassNew.text.toString())
        requestEditPass(oldPassEnCode, newPassEnCode)
    }

    private fun controlPassVisible(et: EditText, iv: ImageView) {
        iv.setOnClickListener {
            if (et.inputType == TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // 输入一个对用户可见的密码
                // mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                iv.setImageResource(R.mipmap.ic_login_eye_open)
            } else {
                // 输入一个对用户不可见的密码
                //mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iv.setImageResource(R.drawable.ic_login_eye_close)
            }
            moveCursor(et)
        }

    }

    private fun moveCursor(et: EditText) {
        et.setSelection(et.text.length)
    }

    private fun requestLogout() {
        ApiRepository.getInstance().requestLogout().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseCommonResult<Any?>?>() {
            override fun onRequestSuccess(entity: BaseCommonResult<Any?>?) {
                if (entity == null) {
                    return
                }
                if (entity.status == RequestConfig.RESPONSE_CODE_SUCCESS) {
                    AccountHelper.getInstance().logoutAndSkipLogin()
                    LogUtils.w(TAG + "账号已退出")
                } else {
                    ToastUtil.showNormal(entity.message)
                }
            }

            override fun onRequestError(throwable: Throwable) {
                super.onRequestError(throwable)
                LogUtils.tag(TAG).i("onRequestError=$throwable")
            }
        })
    }

}