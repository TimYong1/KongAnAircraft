package com.tourcoo.aircraft.ui.splash

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import com.apkfuns.logutils.LogUtils
import com.tourcoo.account.AccountHelper
import com.tourcoo.aircraft.product.ProductManager
import com.tourcoo.aircraft.ui.account.LoginNewActivity
import com.tourcoo.aircraft.ui.home.HomeActivity
import com.tourcoo.aircraftmanager.R
import com.tourcoo.constant.AccountConstant.PREF_IS_FIRST_START
import com.tourcoo.constant.PermissionConstant
import com.tourcoo.manager.RxJavaManager
import com.tourcoo.retrofit.BaseObserver
import com.tourcoo.retrofit.NetworkUtil
import com.tourcoo.util.SpUtil
import com.tourcoo.util.ToastUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import java.util.*

/**
 *@description : 启动页
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2021年04月16日10:00
 * @Email: 971613168@qq.com
 */
class SplashActivity : RxAppCompatActivity() {
    private var mContext: Activity? = null
    private val missingPermission: MutableList<String> = ArrayList()
    private var mHandler : Handler ? = Handler(Looper.getMainLooper())
    companion object {
        private var isAppStarted = false
        private const val SKIP_TIME = 1500L

        fun isAppStarted(): Boolean {
            return isAppStarted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
//        LoggerManager.i(TAG, "isTaskRoot:" + isTaskRoot() + ";getCurrent:" + StackUtil.getInstance().getCurrent());
        //防止应用后台后点击桌面图标造成重启的假象---MIUI及Flyme上发现过(原生未发现)
        if (!isTaskRoot) {
            finish()
            return
        }
        setContentView(R.layout.activity_splash)
        hideNavigation()
        init()

    }


    override fun onResume() {
        super.onResume()
        isAppStarted = true
    }

    private fun init() {
        //根据条件注册
        registerSdkByCondition()
        RxJavaManager.getInstance().setTimer(SKIP_TIME)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(object : BaseObserver<Long?>() {
                    override fun onComplete() {
                        super.onComplete()
                        val isFirstStart = SpUtil.getBoolean(PREF_IS_FIRST_START, true)
                        if (isFirstStart!!) {
                            SpUtil.put(PREF_IS_FIRST_START, false)
                            //如果是第一次启动APP则必须判断网络连接
                            if (!NetworkUtil.isConnected(applicationContext)) {
                                ToastUtil.showWarning("请检查网络连接")
                                SpUtil.put(PREF_IS_FIRST_START, true)
                                finish()
                                return
                            }
                        }
                        skipByCondition()
                        finish()
                    }

                    override fun onRequestSuccess(entity: Long?) {}
                })
    }


    private fun skipByCondition() {
        val intent = Intent()
        if (AccountHelper.getInstance().isLogin) {
            intent.setClass(mContext!!, HomeActivity::class.java)
        } else {
            intent.setClass(mContext!!, LoginNewActivity::class.java)
        }
        startActivity(intent)
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

    private fun registerSdkByCondition() {
        // 检测权限
        for (eachPermission in PermissionConstant.REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission)
            }
        }
        if (missingPermission.isEmpty()) {
            //说明权限充足 直接发起注册
                try {
                    mHandler?.postDelayed( Runnable {
                        ProductManager.getInstance().startSDKRegistration()
                    },500 )
                }catch (e :Exception){
                    e.printStackTrace()
                }
        } else {
            LogUtils.e("权限不足，暂时不能注册")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler?.removeCallbacksAndMessages(null)
    }
}