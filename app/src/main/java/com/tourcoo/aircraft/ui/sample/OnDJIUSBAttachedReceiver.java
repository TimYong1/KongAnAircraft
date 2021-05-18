
package com.tourcoo.aircraft.ui.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tourcoo.aircraft.ui.home.HomeActivity;
import com.tourcoo.util.ToastUtil;

import dji.sdk.sdkmanager.DJISDKManager;

/**

 *此接收器将检测USB连接事件。

 *它将检查应用程序是否以前启动过。

 *如果应用程序已在运行，它将阻止启动新活动并将

 *堆栈顶部的现有活动。

 */

public class OnDJIUSBAttachedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!HomeActivity.isStarted()) {
            ToastUtil.showSuccessDebug("重新启动");
            Intent startIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());
            startIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(startIntent);
        } else {
            Intent attachedIntent = new Intent();
            attachedIntent.setAction(DJISDKManager.USB_ACCESSORY_ATTACHED);
            context.sendBroadcast(attachedIntent);
            ToastUtil.showSuccessDebug("当前activity已在运行");
        }
    }
}
