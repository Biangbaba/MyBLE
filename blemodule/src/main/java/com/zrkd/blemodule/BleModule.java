package com.zrkd.blemodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.taobao.weex.annotation.JSMethod;
import com.zrkd.blemodule.BleUtils.BleController;
import com.zrkd.blemodule.entity.PostEntity;
import com.zrkd.blemodule.service.MyBleService;
import com.taobao.weex.common.WXModule;
import com.zrkd.blemodule.utils.HexUtil;
import com.zrkd.blemodule.utils.LogUtil;
import com.zrkd.blemodule.utils.SumCheckUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BleModule extends WXModule {
    public Context context;
    private PostEntity postEntity;

    @JSMethod(uiThread = false)
    public void initPlugin() {
        getMyContext();
        BleController.getInstance().setUUID("0000ff00-0000-1000-8000-00805f9b34fb", "0000ff01-0000-1000-8000-00805f9b34fb", "0000ff02-0000-1000-8000-00805f9b34fb");
        startBleService();
    }

    public void getMyContext() {
        if (mWXSDKInstance != null && mWXSDKInstance.getContext() instanceof Activity) {
            context = (Activity) mWXSDKInstance.getContext();
            LogUtil.d("context:" + context);
        } else {
        }
    }

    public void startBleService() {
        Intent intent = new Intent(context, MyBleService.class);
        context.startService(intent);
    }

    @JSMethod(uiThread = false)
    public void getTime() {
        String value = "04 00 00";
        postValue(value);
    }

    @JSMethod(uiThread = false)
    public String getScanResult() {
        return MyBleService.scanResult;
    }

    @JSMethod(uiThread = false)
    public String getServiceResult() {
        return MyBleService.result;
    }

    @JSMethod(uiThread = false)
    public String getConnectResult() {
        return BleController.getInstance().connectState;
    }

    //创建基本线程池
    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(50));
//   检验值未添加，暂时以“00”代替
    private void postValue(final String data) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String rValue = data.replace(" ", "");
                    String value = rValue + "00";
                    postEntity = new PostEntity();
                    postEntity.setEvent(value);
                    postEntity.setTag("send");
                    EventBus.getDefault().post(postEntity);
                } catch (Exception e) {
                    LogUtil.e("" + e);
                }
            }
        };
        //    BleService.bleHandler.post(runnable);
        threadPoolExecutor.execute(runnable);
    }
}
