package com.tourcoo.retrofit.repository;

import android.accounts.NetworkErrorException;

import com.tourcoo.entity.base.BaseCommonResult;
import com.tourcoo.retrofit.DataNullException;
import com.tourcoo.retrofit.RetryWhen;
import com.tourcoo.retrofit.ThreadTransformer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

import static com.tourcoo.retrofit.RequestConfig.RESPONSE_CODE_SUCCESS;


/**
 * @author :JenkinsZhou
 * @description :retrofit封装
 * @company :途酷科技
 * @date 2020年10月29日10:54
 * @Email: 971613168@qq.com
 */
public class BaseRepository {

    /**
     * @param observable 用于解析 统一返回实体统一做相应的错误码--如登录失效
     * @param <T>
     * @return
     */
    protected <T> Observable<T> transform(Observable<BaseCommonResult<T>> observable) {
        return ThreadTransformer.switchSchedulers(
                observable.retryWhen(new RetryWhen())
                        .flatMap((Function<BaseCommonResult<T>, ObservableSource<T>>) result -> {
                            if (result == null) {
                                return Observable.error(new DataNullException());
                            }
                            if (result.getStatus() == RESPONSE_CODE_SUCCESS) {
                                return result.getData() != null ? Observable.just(result.getData())
                                        : Observable.error(new DataNullException());
                            } else {
                                return Observable.error(new NetworkErrorException());
                            }
                        }));
    }
}
