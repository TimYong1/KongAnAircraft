package com.tourcoo.aircraft.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.apkfuns.logutils.LogUtils
import com.tourcoo.account.AccountHelper
import com.tourcoo.account.TokenInfo
import com.tourcoo.aircraft.ui.home.HomeActivity
import com.tourcoo.aircraft.ui.sample.AircraftApplication
import com.tourcoo.aircraftmanager.R
import com.tourcoo.config.AppConfig.APP_TYPE
import com.tourcoo.constant.CommonConstant.*
import com.tourcoo.entity.BaseResultOld
import com.tourcoo.retrofit.BaseLoadingObserver
import com.tourcoo.retrofit.RequestConfig
import com.tourcoo.retrofit.repository.ApiRepository
import com.tourcoo.threadpool.ThreadManager
import com.tourcoo.util.SizeUtil
import com.tourcoo.util.SpUtil
import com.tourcoo.util.StringUtil
import com.tourcoo.util.ToastUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_login_new.*


/**
 *@description : JenkinsZhou
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2021年04月16日9:54
 * @Email: 971613168@qq.com
 */
class LoginNewActivity : RxAppCompatActivity(), View.OnClickListener {
    private var mContext: Activity? = null
    private var screenWidth: Int? = null
    private var screenHeight: Int? = null
    private var passVisible = true
    private var rememberPass: Boolean? = null
    private var lastPhone: String? = null
    private var lastPass: String? = null

    companion object {
        const val PREF_KEY_PHONE = "PREF_KEY_PHONE"
        const val PREF_KEY_PASS = "PREF_KEY_PASS"
        const val PREF_KEY_IS_REMIND = "PREF_KEY_IS_REMIND"


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        LogUtils.d("像素点：" + SizeUtil.px2dp(460f))
        setContentView(R.layout.activity_login_new)
        tvLogin.setOnClickListener(this)
        initViewSize()
        hideNavigation()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
        loadLastInput()
    }

    private fun hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        val decorView: View = mContext!!.window.decorView
        decorView.systemUiVisibility = 0
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = uiOptions
    }

    private fun initViewSize() {
        val outMetrics = DisplayMetrics()
        mContext!!.windowManager.defaultDisplay.getRealMetrics(outMetrics)
        screenWidth = outMetrics.widthPixels
        screenHeight = outMetrics.heightPixels
        val contentHeightPercent = 450f / 540f
        val widthPercent = 580f / 960f
        val inputViewWidthPercent = 360f / 500f
        val inputViewHeightPercent = 40f / 360f
        val inputWidth: Int
        val params = nScrollView.layoutParams as ConstraintLayout.LayoutParams
        val inputPhoneParams = llPhone.layoutParams as LinearLayout.LayoutParams
        val inputSasParams = llSas.layoutParams as LinearLayout.LayoutParams
        val inputPassParams = llPass.layoutParams as LinearLayout.LayoutParams
        val tvLoginPassParams = tvLogin.layoutParams as LinearLayout.LayoutParams
        val cBoxRemindPassParams = cBoxRemindPass.layoutParams as LinearLayout.LayoutParams
        val parentWidth: Int = (widthPercent * screenWidth!!).toInt()
        params.width = parentWidth
        params.height = (contentHeightPercent * screenHeight!!).toInt()
        inputWidth = (inputViewWidthPercent * parentWidth).toInt()
        val inputHeight: Int = (inputViewHeightPercent * inputWidth).toInt()
        inputPhoneParams.width = inputWidth
        inputPhoneParams.height = inputHeight
        inputPassParams.width = inputWidth
        tvLoginPassParams.width = inputWidth
        inputPassParams.height = inputHeight
        inputSasParams.width = inputWidth
        inputSasParams.height = inputHeight
        cBoxRemindPassParams.setMargins((parentWidth - inputWidth) / 2, 0, 0, 0)
        LogUtils.d("屏幕尺寸" + params.width + "---" + params.height + "inputParams.inputHeight=" + inputHeight)

        when (APP_TYPE) {
            APP_TYPE_KONG_AN, APP_TYPE_PRO -> {
                setViewVisible(llSas, false)
            }
            APP_TYPE_SAS -> {
                setViewVisible(llSas, true)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvLogin -> {
                doLogin()
            }
            else -> {
            }
        }
    }

    private fun skipByCondition() {
        val intent = Intent()
        if (AccountHelper.getInstance().isLogin) {
            intent.setClass(mContext!!, HomeActivity::class.java)
        } else {
            intent.setClass(mContext!!, LoginNewActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun requestLogin(user: String, pass: String) {
        ApiRepository.getInstance().requestAppLogin(user, pass).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResultOld<TokenInfo?>?>() {

            override fun onRequestSuccess(entity: BaseResultOld<TokenInfo?>?) {
                handleLoginSuccess(entity)
                hideNavigation()
            }

            override fun onRequestError(throwable: Throwable) {
                super.onRequestError(throwable)
                LogUtils.tag(TAG).e("onRequestError=$throwable")
                hideNavigation()
            }


        })
    }

    private fun handleLoginSuccess(entity: BaseResultOld<TokenInfo?>?) {
        if (entity == null) {
            Toast.makeText(mContext, "服务器数据异常", Toast.LENGTH_SHORT).show()
            return
        }
        if (RequestConfig.RESPONSE_CODE_SUCCESS == entity.status && null != entity.data) {
            AccountHelper.getInstance().login(entity.data)
            skipByCondition()
            ThreadManager.getDefault().execute { AircraftApplication.initRongYun() }

        } else {
            ToastUtil.showNormal(entity.message)
        }
    }

    private fun doLogin() {
        if (TextUtils.isEmpty(etUserPhone.text.toString())) {
            ToastUtil.showNormal("请输入用户名")
            return
        }
        if (TextUtils.isEmpty(etUserPass.text.toString())) {
            ToastUtil.showNormal("请输入密码")
            return
        }
        if (cBoxRemindPass.isChecked) {
            SpUtil.put(PREF_KEY_PHONE, etUserPhone.text.toString())
            SpUtil.put(PREF_KEY_PASS, etUserPass.text.toString())
        } else {
            SpUtil.put(PREF_KEY_PHONE, "")
            SpUtil.put(PREF_KEY_PASS, "")
        }
        when (APP_TYPE) {
            APP_TYPE_KONG_AN, APP_TYPE_PRO -> {

            }
            APP_TYPE_SAS -> {
                if (TextUtils.isEmpty(etSasNum.text.toString())) {
                    ToastUtil.showNormal("请输入租户号")
                    return
                }
            }
        }

        SpUtil.put(PREF_KEY_IS_REMIND, cBoxRemindPass.isChecked)



        requestLogin(etUserPhone.text.toString(), etUserPass.text.toString())
    }

    private fun skipHome() {
        val intent = Intent()
        intent.setClass(this@LoginNewActivity, HomeActivity::class.java)
        startActivity(intent)
    }


    private fun listenInput(editText: EditText, imageView: ImageView) {
        imageView.setOnClickListener {
            editText.setText("")
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                setViewVisible(imageView, s != null && s.toString().isNotEmpty())
            }

        })

    }

    private fun checkInput() {
        etUserPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                setViewVisible(ivInputRight, StringUtil.isPhoneNumber(s.toString()))
            }

        })
    }

    fun setViewVisible(view: View?, visible: Boolean) {
        if (view == null) {
            return
        }
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    fun setViewGone(view: View?, visible: Boolean) {
        if (view == null) {
            return
        }
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun initListener() {
        listenInput(etUserPhone, ivClearPhone)
        listenInput(etUserPass, ivClearPass)
        listenInput(etSasNum, ivClearSas)
        checkInput()
        ivPassVisible.setOnClickListener {
            controlPassVisible()
        }
        loadLastInput()
        controlPassVisible()
    }

    private fun controlPassVisible() {
        if (passVisible) {
            // 输入一个对用户可见的密码
            // mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etUserPass.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            ivPassVisible.setImageResource(R.mipmap.ic_login_eye_open)
        } else {
            // 输入一个对用户不可见的密码
            //mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etUserPass.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivPassVisible.setImageResource(R.drawable.ic_login_eye_close)
        }
        moveCursor()
        passVisible = !passVisible

    }

    private fun loadLastInput() {
        rememberPass = SpUtil.getBoolean(PREF_KEY_IS_REMIND)
        if (rememberPass != null) {
            cBoxRemindPass.isChecked = rememberPass!!
            lastPhone = StringUtil.getNotNullValue(SpUtil.getString(PREF_KEY_PHONE))
            lastPass = StringUtil.getNotNullValue(SpUtil.getString(PREF_KEY_PASS))
            etUserPhone.setText(lastPhone)
            moveCursor()
            etUserPass.setText(StringUtil.getNotNullValue(lastPass))
        } else {
            cBoxRemindPass.isChecked = false
            etUserPhone.setText("")
            etUserPass.setText("")
        }
    }

    private fun moveCursor() {
        etUserPhone.setSelection(etUserPhone.text.length)
        etUserPass.setSelection(etUserPass.text.length)
    }


}