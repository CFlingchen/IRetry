package com.lingchen.iretrydemo.rx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2017/10/26
 * Function  网络监听
 */

public class NetWorksFlowable implements FlowableOnSubscribe<Boolean> {
    private static final String TAG = "NetWorksFlowable";

    private final Context context;

    private volatile boolean isRegister;

    /**
     * 记录上次的结果 如果当前结果跟上次结果一样 则不需要再次发送
     */
    private volatile boolean lastResult;

    public static Flowable<Boolean> single(Context context) {
        return Flowable.create(new NetWorksFlowable(context), BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public NetWorksFlowable(Context context) {
        this.context = context;
    }


    @Override
    public void subscribe(FlowableEmitter<Boolean> e) throws Exception {
        checked(e);

        BroadcastReceiver receiver = registerReceiver(e);

        e.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                Log.e(TAG, "cancel() called");
                unregisterReceiver(receiver);
            }
        });

    }

    private void checked(FlowableEmitter<Boolean> e) {
        if (context == null) {
            e.onError(new Exception("context is null"));
        }
    }

    private BroadcastReceiver registerReceiver(FlowableEmitter<Boolean> e) {
        //构建 IntentFilter
        IntentFilter mFilter = new IntentFilter();
        //添加网络监听
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //只监听网络
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                    Boolean result = netInfo != null && netInfo.isConnected();
                    Log.e(TAG, "onReceive() called with: result-->" + result);
                    if (lastResult == result) {
                        Log.e(TAG, "onReceive() called with:result==lastResult" + result);
                    } else {
                        //发送数据
                        e.onNext(result);
                        lastResult = result;
                    }
                }

            }
        };

        //注册
        context.registerReceiver(receiver, mFilter);
        isRegister = true;
        Log.e(TAG, "registerReceiver() called with: isRegister");
        return receiver;
    }

    private void unregisterReceiver(BroadcastReceiver receiver) {
        if (isRegister) {
            context.unregisterReceiver(receiver);
            isRegister = false;
            Log.e(TAG, "unregisterReceiver() called with");
        }
    }

}
