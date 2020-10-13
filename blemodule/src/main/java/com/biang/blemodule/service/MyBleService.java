package com.biang.blemodule.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.biang.blemodule.BleUtils.BleController;
import com.biang.blemodule.BleUtils.callBack.ConnectCallback;
import com.biang.blemodule.BleUtils.callBack.OnReceiverCallback;
import com.biang.blemodule.BleUtils.callBack.OnWriteCallback;
import com.biang.blemodule.BleUtils.callBack.ScanCallback;
import com.biang.blemodule.entity.PostEntity;
import com.biang.blemodule.model.Device;
import com.biang.blemodule.utils.HexUtil;
import com.biang.blemodule.utils.LogUtil;
import com.biang.blemodule.utils.DataTransferUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyBleService extends Service {
    /**
     * 蓝牙工具类
     */
    private BleController mBleController;
    private final String IBROW = "S10-2E12"; //"S10-2C05";"S10-2E12"
    private int mRss = -150;
    BluetoothDevice mBleDevice;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    public static String result = null;
    public static String scanResult = null;

    //"-"是因为传过来的十进制是有符号的，超过127（0x7f)就变成负数
    private int SERVICE_TIME = -124;
    private int SERVICE_TIME_GETSUCCESS = 01;
    private int SERVICE_TIME_CURRENTTIME = 8;
    private int ACTION_RECORD = -96;
    private int ACTION_RECORD_GETCURRENTWALK = 13;
    private int ACTION_RECORD_GETHISTORYWALK = 38;
    private int ACTION_RECORD_COMPLETE = 2;
    private int HEALTH_RECORD = -95;
    private int HEALTH_RECORD_GETCURRENTHEALTH = 5;
//    private int HEALTH_RECORD_GETHISTORYWALK =
    private int BATTERY = -89;

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.e("================  onBind  ================");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d("service start");
        try {
            LogUtil.e("onCreate");
            EventBus.getDefault().register(this);
            init();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        mBleController = BleController.getInstance();
        mBleController.initBle(this);
        scanDevices(true);
        mBleController.readListener(TAG, new OnReceiverCallback() {
            @Override
            public void onReceiver(byte[] value) { // byte数组的value数据是字节流的十进制的
                String title = "";
                String result = HexUtil.bytesToHexString(value);
                LogUtil.d("返回数据：" + result);
                if (value[0] == SERVICE_TIME) {
                    if (value[1] == SERVICE_TIME_GETSUCCESS) {
                        title = "返回数据：设备时间设置成功";
                    } else if (value[1] == SERVICE_TIME_CURRENTTIME) {
                        String currentTime = DataTransferUtil.transferToYMDHMSDate(result.substring(6, 20));
                        title = "返回数据：设备时间:" + currentTime;
                    }
                }
                if (value[0] == ACTION_RECORD) {
                    if (value[1] == ACTION_RECORD_GETCURRENTWALK) {
                        String step = DataTransferUtil.stepTransfer(result.substring(8, 16));
                        String calorie = DataTransferUtil.stepTransfer(result.substring(16, 24));
                        String distance = DataTransferUtil.stepTransfer(result.substring(24, 32));
                        title = "当前步数：" + step + ",卡路里：" + calorie + ",距离：" + distance;
                    } else if (value[1] == ACTION_RECORD_GETHISTORYWALK) {
                        title = "历史步数：" + result;
                    } else if (value[3] == 3) {
                        title = "获取最新一次睡眠统计数据" + result;
                    } else if (value[1] == ACTION_RECORD_COMPLETE) {
                        if (value[3] == 1) {
                            title = "历史数据上传成功";
                        } else if (value[3] == 2) {
                            title = "数据清除成功";
                        }
                    } else {
                        title = "步数和睡眠数据返回数据出错" + result;
                    }
                }
                if (value[0] == HEALTH_RECORD) {
                    if (value[1] == HEALTH_RECORD_GETCURRENTHEALTH) {
                        if (value[3] == 0) {
                            String heartRate = DataTransferUtil.hexToInt(result.substring(8, 10));
                            String diastolicPressure = DataTransferUtil.hexToInt(result.substring(10, 12));//舒张压
                            String systolicPressure = DataTransferUtil.hexToInt(result.substring(12, 14));//收缩压
                            String bloodOxygen = DataTransferUtil.hexToInt(result.substring(14, 16));//血氧
                            title = "当前心率：" + heartRate + "，舒张压：" + diastolicPressure + ",收缩压:" + systolicPressure + ",血氧:" + bloodOxygen;
                        }
                    }
//                    else if (value[1]==)
                }
                if (value[0]==BATTERY){
                    if (value[3]==-1){
                        title="正在充电";
                    }else{
                        title="当前电量："+DataTransferUtil.hexToInt(result.substring(6,8));
                    }
                }
                LogUtil.d("title:-------" + title);
                result = title;
            }
        });
    }

    /**
     * 扫描蓝牙设备
     *
     * @param enable
     */
    private void scanDevices(boolean enable) {
        Context mContext = this;
        mBleController.ScanBle(enable, new ScanCallback() {
            @Override
            public void onSuccess() {
                LogUtil.e("mRss ============" + mRss);
                if (mRss == -150) {
                    //                  ToastUtil.shortShow(mContext,"未搜索到设备");
                    scanResult = "未搜索到设备";
                    LogUtil.e("未搜索到设备");
                    scanDevices(true);
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                    List<Device> deviceList = new ArrayList<>();
                    Device device = new Device(mBleDevice.getName(), mBleDevice.getAddress());
                    deviceList.add(device);
                    scanResult = JSON.toJSONString(deviceList);
                    connect();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rss, byte[] scanRecord) {  // rss 信号强弱、
                if (device.getName() != null && !TextUtils.isEmpty(device.getName()) && device.getName().indexOf(IBROW) != -1) {
                    mRss = rss;
                    mBleDevice = device;
                }
            }
        });
    }

    /**
     * 当前连接的mac地址
     */
    public void connect() {
        String mDeviceAddress = mBleDevice.getAddress();
        mBleController.Connect(mDeviceAddress, new ConnectCallback() {
            @Override
            public void onConnSuccess() {
//                            ToastUtil.shortShow(mContext,"连接成功");
                LogUtil.e("连接成功");
            }

            @Override
            public void onConnFailed() {
                connect();
                LogUtil.e("连接超时，请重试");
//                ToastUtil.shortShow(mContext,"连接超时，请重试");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PostEntity entity) {
        if (!TextUtils.isEmpty(entity.getTag()) || "send".equals(entity.getTag())) {
            LogUtil.e("value===================" + entity.getEvent());
            Write(entity.getEvent());
        }
    }

    /**
     * 发送指令
     *
     * @param value
     */
    public void Write(String value) {
        mBleController.WriteBuffer(value, new OnWriteCallback() {
            @Override
            public void onSuccess() {
                LogUtil.e("发送指令成功");
//                ToastUtil.shortShow(mContext, "ok");
            }

            @Override
            public void onFailed(int state) {
                LogUtil.e("发送指令失败");
//                ToastUtil.shortShow(mContext, "fail");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("onDestroy");
        mBleController.closeBleConn();
        //移除接收数据的监听
        mBleController.removeReadListener(TAG);
        EventBus.getDefault().unregister(this);
    }

    private void disConnect() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mBleController.closeBleConn();
                LogUtil.e("主动断开" + BleController.getInstance().mBleGatt);
            }
        });

    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (mainHandler != null) {
                mainHandler.post(runnable);
            }
        }
    }

}

