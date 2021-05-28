package dji.ux.beta.core.base.widget;

import dji.common.error.DJIError;
import dji.keysdk.DJIKey;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月27日16:54
 * @Email: 971613168@qq.com
 */
public interface DJIKeyActionCallback {

    void onSuccess(DJIKey djiKey);

    void onFailed(DJIKey key,DJIError e);
}
