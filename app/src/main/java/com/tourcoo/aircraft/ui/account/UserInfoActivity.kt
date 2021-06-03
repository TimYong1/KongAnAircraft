package com.tourcoo.aircraft.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.apkfuns.logutils.LogUtils
import com.tourcoo.account.AccountHelper
import com.tourcoo.account.UserInfo
import com.tourcoo.aircraftmanager.R
import com.tourcoo.dialog.TourCooDialog
import com.tourcoo.entity.BaseResultOld
import com.tourcoo.retrofit.BaseLoadingObserver
import com.tourcoo.retrofit.BaseObserver
import com.tourcoo.retrofit.RequestConfig
import com.tourcoo.retrofit.repository.ApiRepository
import com.tourcoo.util.StringUtil
import com.tourcoo.util.ToastUtil
import com.tourcoo.util.cache.CacheDataManager
import com.tourcoo.util.cache.ExecuteListener
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_login_new.*
import kotlinx.android.synthetic.main.activity_my_info.*

/**
 *@description : 我的页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2021年04月20日14:04
 * @Email: 971613168@qq.com
 */
class UserInfoActivity : RxAppCompatActivity(), View.OnClickListener {
    private var mContext: Activity? = null
    private  var cacheUtil: CacheDataManager ? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)
        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }
        mContext = this
        tvLogout.setOnClickListener {
            requestLogout()
        }
        llEditPass.setOnClickListener(this)
        llClearCache.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
        showCache()
        requestUserInfo()
    }

    private fun showUserInfo() {
        if (!AccountHelper.getInstance().isLogin) {
            return
        }
        val user = AccountHelper.getInstance().userInfo
        if (user == null) {
            etUserName.setText("-")
            etGender.setText("-")
            etUserPhone.setText("-")
        } else {
            etUserName.setText(StringUtil.getNotNullValueLine(user.username))
            etGender.setText(StringUtil.getNotNullValueLine(""))
            etPhone.setText(StringUtil.getNotNullValueLine(user.phone))
            etGender.setText(StringUtil.getNotNullValueLine(user.gender))
        }

    }


    private fun requestUserInfo() {
        ApiRepository.getInstance().requestUserInfo().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseObserver<BaseResultOld<UserInfo?>?>() {
            override fun onRequestSuccess(entity: BaseResultOld<UserInfo?>?) {
                if (entity == null) {
                    return
                }
                if (entity.status == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
                    AccountHelper.getInstance().userInfo = entity.data
                    showUserInfo()
                } else {
                    ToastUtil.showNormal(entity.message)
                }
            }

            override fun onRequestError(throwable: Throwable) {
                super.onRequestError(throwable)
                LogUtils.tag(TAG).i(TAG + "onRequestError=" + throwable.toString())
            }


        })
    }

    private fun requestLogout() {
        ApiRepository.getInstance().requestLogout().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResultOld<Any?>?>() {
            override fun onRequestSuccess(entity: BaseResultOld<Any?>?) {
                if (entity == null) {
                    return
                }
                if (entity.status == RequestConfig.RESPONSE_CODE_SUCCESS) {
                    AccountHelper.getInstance().logoutAndSkipLogin()
                    LogUtils.w(TAG + "已退出登录")
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llEditPass -> {
                skipEditPass()
            }
            R.id.llClearCache -> {
                showDialog()
            }
            else -> {
            }
        }
    }


    private fun skipEditPass() {
        val intent = Intent()
        if (!AccountHelper.getInstance().isLogin) {
            AccountHelper.getInstance().skipLogin()
        } else {
            intent.setClass(mContext!!, EditPassActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun showCache() {
        tvCacheSize.text =  cacheUtil?.getTotalCacheSize(this)
       if(cacheUtil != null){
            cacheUtil?.getTotalCacheSize(applicationContext)
        }else{
            cacheUtil = CacheDataManager(object : ExecuteListener {
                override fun onExecuteStart() {
                    runOnUiThread {
                        tvCacheSize.text = "正在计算..."
                        llClearCache.isEnabled = false
                    }
                }

                override fun onExecuteFinish(result: String?) {
                    runOnUiThread {
                        tvCacheSize.text = result
                        llClearCache.isEnabled = true
                    }
                }

                override fun onError(e: Throwable?) {
                    runOnUiThread {
                        tvCacheSize.text = "0.00MB"
                        llClearCache.isEnabled = true
                    }
                }

            })
            cacheUtil?.getTotalCacheSize(applicationContext)
        }

    }


    private fun showDialog() {
        TourCooDialog(this@UserInfoActivity)
                .init()
                .setMsg("是否确定清除缓存?")
                .setPositiveButton("确认", View.OnClickListener {
                    cacheUtil?.clearAllCache(applicationContext)
                    showCache()
                })
                .setNegativeButton("取消", View.OnClickListener { }).show()
    }

}