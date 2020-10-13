package com.biang.blemodule.BleUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.biang.blemodule.BleUtils.callBack.ScanCallback;
import com.biang.blemodule.BleUtils.callBack.BleDevceScanCallback;
import com.biang.blemodule.BleUtils.callBack.ConnectCallback;
import com.biang.blemodule.BleUtils.callBack.OnReceiverCallback;
import com.biang.blemodule.BleUtils.callBack.OnWriteCallback;
import com.biang.blemodule.entity.PostEntity;
import com.biang.blemodule.request.ReceiverRequestQueue;
import com.biang.blemodule.utils.HexUtil;
import com.biang.blemodule.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author BleController
 */
public class BleController {

    private static final String TAG = "BleController";

    private static BleController mBleController;
    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBleAdapter;
    public BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mBleGattCharacteristic;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BleGattCallback mGattCallback;
    private OnWriteCallback writeCallback;

    private boolean mScanning;
    private BluetoothDevice remoteDevice;
    public String connectState;

    /**
     * 读操作请求队列
     */
    private ReceiverRequestQueue mReceiverRequestQueue = new ReceiverRequestQueue();
    /**
     * 默认扫描时间：10s
     */
    private static final int SCAN_TIME = 10000;
    /**
     * 默认连接超时时间:10s
     */
    private static final int CONNECTION_TIME_OUT = 10000;
    /**
     * 获取到所有服务的集合
     */
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();

    /**
     * 连接请求是否ok
     */
    private boolean isConnectOk = false;
    /**
     * 是否是用户手动断开
     */
    public boolean isMyBreak = false;
    /**
     * 连接结果的回调
     */
    public ConnectCallback connectCallback;

    /**
     * 系统提供接受通知自带的UUID，此属性一般不用修改
     * "00002902-0000-1000-8000-00805f9b34fb"
     */
    private static final String BLUETOOTH_NOTIFY_D = "00002902-0000-1000-8000-00805f9b34fb";

    //TODO 以下uuid根据公司硬件改变
//    public static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
//    public static final String UUID_NOTIFY = "0000fff3-0000-1000-8000-00805f9b34fb";
//    public static final String UUID_WRITE = "0000fff3-0000-1000-8000-00805f9b34fb";

    /**
     * 设备自身的UUID （我们获取到的蓝牙设备的UUID就是这个，然后它下面会有读与写两个UUID。一般来说，蓝牙设备的UUID都是固定的，而读与写的UUID都是硬件开发师自定义的，以便可以跟别的蓝牙设备区分）
     * 0000fff0-0000-1000-8000-00805f9b34fb
     */
    public static String UUID_SERVICE = "0000ff00-0000-1000-8000-00805f9b34fb";

    /**
     * 读时使用的UUID
     */
    public static String UUID_NOTIFY = "0000fff1-0000-1000-8000-00805f9b34fb";
    /**
     * 写时使用的UUID
     */
    public static String UUID_WRITE = "0000fff2-0000-1000-8000-00805f9b34fb";

    public static synchronized BleController getInstance() {
        if (null == mBleController) {
            mBleController = new BleController();
        }
        return mBleController;
    }

    /**
     * 初始化设备
     *
     * @param context
     * @return
     */
    public BleController initBle(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null == mBluetoothManager) {
                LogUtil.e("BluetoothManager初始化错误!");
            }

            mBleAdapter = mBluetoothManager.getAdapter();
            if (null == mBleAdapter) {
                LogUtil.e("BluetoothManager初始化错误!");
            }

            mGattCallback = new BleGattCallback();
        }
        return this;
    }

    /**
     * 扫描设备
     * 默认扫描10s(也可自定义扫描时间)
     *
     * @param scanCallback
     */
    public void ScanBle(final boolean enable, final ScanCallback scanCallback) {
        ScanBle(SCAN_TIME, enable, scanCallback);
    }


    /**
     * 扫描蓝牙设备
     *
     * @param time         指定扫描时间
     * @param scanCallback 扫描回调
     */
    public void ScanBle(int time, final boolean enable, final ScanCallback scanCallback) {
        if (!isEnable()) {
            mBleAdapter.enable();
            LogUtil.e("蓝牙未打开!");
        }
        if (null != mBleGatt && !isMyBreak) {
            mBleGatt.close();
            LogUtil.e("关闭的gatt" + mBleGatt);
        }
        reset();
        final BleDevceScanCallback bleDeviceScanCallback = new BleDevceScanCallback(scanCallback);
        if (enable) {
            if (mScanning) {
                return;
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    //time后停止扫描
                    mBleAdapter.stopLeScan(bleDeviceScanCallback);
                    scanCallback.onSuccess();
                }
            }, time <= 0 ? SCAN_TIME : time);
            mScanning = true;
            mBleAdapter.startLeScan(bleDeviceScanCallback);
        } else {
            mScanning = false;
            mBleAdapter.stopLeScan(bleDeviceScanCallback);
        }
    }

    /**
     * 连接设备
     *
     * @param address         设备mac地址
     * @param connectCallback 连接回调
     *                        默认连接时间10s,也可自定义连接时间
     */
    public void Connect(final String address, ConnectCallback connectCallback) {
        Connect(CONNECTION_TIME_OUT, address, connectCallback);
    }


    /**
     * 连接设备
     *
     * @param connectionTimeOut 指定连接超时
     * @param address           设备mac地址
     * @param connectCallback   连接回调
     */
    public void Connect(final int connectionTimeOut, final String address, ConnectCallback connectCallback) {

        if (mBleAdapter == null || address == null) {
            LogUtil.e("在此地址下找不到设备：" + address);
            return;
        }
        remoteDevice = mBleAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            Log.w(TAG, "找不到设备。无法连接！");
            return;
        }
        this.connectCallback = connectCallback;
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mBleGatt = remoteDevice.connectGatt(mContext, false, mGattCallback);
            }
        });

        refreshDeviceCache(mBleGatt);

        LogUtil.e("连接的mac地址:" + address + "    gatt:" + mBleGatt);
        delayConnectResponse(connectionTimeOut);
    }

    /**
     * 发送数据
     *
     * @param value         指令
     * @param writeCallback 发送回调方法
     */
    public void WriteBuffer(String value, OnWriteCallback writeCallback) {
        this.writeCallback = writeCallback;
        if (!isEnable()) {
            writeCallback.onFailed(OnWriteCallback.FAILED_BLUETOOTH_DISABLE);
            LogUtil.e("蓝牙禁用失败");
            return;
        }

        if (mBleGattCharacteristic == null) {
            mBleGattCharacteristic = getBluetoothGattCharacteristic(UUID_SERVICE, UUID_WRITE);
        }

        if (null == mBleGattCharacteristic) {
            writeCallback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            LogUtil.e("失败的无效字符");
            return;
        }

        //设置数组进去
        mBleGattCharacteristic.setValue(HexUtil.hexStringToBytes(value));
        //发送
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        boolean b = mBleGatt.writeCharacteristic(mBleGattCharacteristic);
        LogUtil.e("发送状态是：" + b + "  发送的数据是：" + value);
    }

    /**
     * 设置读取数据的监听
     *
     * @param requestKey
     * @param onReceiverCallback
     */
    public void readListener(String requestKey, OnReceiverCallback onReceiverCallback) {
        mReceiverRequestQueue.set(requestKey, onReceiverCallback);
    }

    /**
     * 移除读取数据的监听
     *
     * @param requestKey
     */
    public void removeReadListener(String requestKey) {
        mReceiverRequestQueue.removeRequest(requestKey);
    }

    /**
     * 手动断开Ble连接
     */
    public void closeBleConn() {
        isMyBreak = true;
        mBleGattCharacteristic = null;
        mBluetoothManager = null;
        disConnection();
    }


//------------------------------------分割线--------------------------------------

    /**
     * 当前蓝牙是否打开
     */
    private boolean isEnable() {
        if (null != mBleAdapter) {
            return mBleAdapter.isEnabled();
        }
        return false;
    }

    /**
     * 重置数据
     */
    private void reset() {
        isConnectOk = false;
        servicesMap.clear();
    }

    /**
     * 超时断开
     *
     * @param connectionTimeOut
     */
    private void delayConnectResponse(int connectionTimeOut) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnectOk && !isMyBreak) {
                    LogUtil.e("连接超时");
                    PostEntity postEntity = new PostEntity();
                    postEntity.setWhtat(444);
                    EventBus.getDefault().post(postEntity);
                    disConnection();
                    reConnect();
                } else {
                    isMyBreak = false;
                }
            }
        }, connectionTimeOut <= 0 ? CONNECTION_TIME_OUT : connectionTimeOut);
    }

    /**
     * 断开连接
     */
    private void disConnection() {
        if (null == mBleAdapter || null == mBleGatt) {
            LogUtil.e("断开连接错误，可能没有初始化");
            return;
        }
        mBleGatt.disconnect();
        reset();
    }

    /**
     * 蓝牙GATT连接及操作事件回调
     */
    private BluetoothGatt oldGatt = null;

    private class BleGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            connectState ="oldState:"+status+",newState:"+newState;
            LogUtil.e("status,newStatus:" + status + "," + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                LogUtil.e("连接成功回调");
                oldGatt = gatt;
                isMyBreak = false;
                isConnectOk = true;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBleGatt.discoverServices();
                LogUtil.e("连接成功的gatt" + gatt);
                connSuccess();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {   //断开连接
                LogUtil.e("连接失败回调");
                if (oldGatt != gatt) {
                    gatt.close();
                    gatt = null;
                    LogUtil.e("oldGatt!=gatt,gatt:" + gatt);
                }
                boolean a = (gatt == mBleGatt);
                LogUtil.e("close的gatt:" + gatt + a);
                PostEntity postEntity = new PostEntity();
                postEntity.setWhtat(444);
                EventBus.getDefault().post(postEntity);
                disConnection();
                if (!isMyBreak) {
                    reConnect();
                }
                reset();
            }
        }

        //发现新服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (null != mBleGatt && status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = mBleGatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>(); // 0000180f-0000-1000-8000-00805f9b34fb
                    BluetoothGattService bluetoothGattService = services.get(i);
                    String serviceUuid = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                    }
                    servicesMap.put(serviceUuid, charMap);
                }
                BluetoothGattCharacteristic NotificationCharacteristic = getBluetoothGattCharacteristic(UUID_SERVICE, UUID_NOTIFY);
                if (NotificationCharacteristic == null) {
                    return;
                }
                enableNotification(true, NotificationCharacteristic);
            }
        }

        //读数据
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        //写数据
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (null != writeCallback) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onSuccess();
                        }
                    });
                    LogUtil.e("发送指令成功!");
                } else {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onFailed(OnWriteCallback.FAILED_OPERATION);
                        }
                    });
                    LogUtil.e("发送指令失败!");
                }
            }
        }

        //通知数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (null != mReceiverRequestQueue) {
                HashMap<String, OnReceiverCallback> map = mReceiverRequestQueue.getMap();
                final byte[] rec = characteristic.getValue();
                for (int i = 0; i < rec.length; i++) {
                    LogUtil.e("" + rec[i]);
                }
                for (String key : mReceiverRequestQueue.getMap().keySet()) {
                    final OnReceiverCallback onReceiverCallback = map.get(key);
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onReceiverCallback.onReceiver(rec);
                        }
                    });
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
    }

    /**
     * 设置通知
     *
     * @param enable         true为开启,false为关闭
     * @param characteristic 通知特征
     * @return
     */
    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (mBleGatt == null || characteristic == null) {
            return false;
        }

        if (!mBleGatt.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_NOTIFY_D));
        if (clientConfig == null) {
            return false;
        }

        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBleGatt.writeDescriptor(clientConfig);
    }

    public BluetoothGattService getService(UUID uuid) {
        if (mBleAdapter == null || mBleGatt == null) {
            LogUtil.e("蓝牙适配器未初始化");
            return null;
        }
        return mBleGatt.getService(uuid);
    }


    /**
     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
        if (!isEnable()) {
            throw new IllegalArgumentException(" Bluetooth is no enable please call BluetoothAdapter.enable()");
        }
        if (null == mBleGatt) {
            LogUtil.e("mBluetoothGatt is null");
            return null;
        }

        //找服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            LogUtil.e("未找到serviceUUID!");
            return null;
        }

        //找特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }
        return gattCharacteristic;
    }


    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (mHandler != null) {
                mHandler.post(runnable);
            }
        }
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    /**
     * 此方法断开连接或连接失败时会被调用。可在此处理自动重连,内部代码可自行修改，如发送广播
     */
    public void reConnect() {
        LogUtil.e("前gatt" + mBleGatt);
        if (connectCallback != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    connectCallback.onConnFailed();
                }
            });
        }
        LogUtil.e("Ble断开或连接失败，尝试重连");
        //     mBleGatt.close();
        //     LogUtils.e("reConnect close的gatt"+mBleGatt);
    }

    /**
     * 此方法Notify成功时会被调用。可在通知界面连接成功,内部代码可自行修改，如发送广播
     */
    private void connSuccess() {
        if (connectCallback != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    connectCallback.onConnSuccess();
                }
            });
        }
        LogUtil.e("蓝牙连接成功!");
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            LogUtil.e("An exception occurred while refreshing device");
        }
        return false;
    }
    /**
     * @param serviceUUID,readUUID,writeUUID
     * @return boolean
     * @description 设置uuid号
     */
    public boolean setUUID(String serviceUUID, String notifyUUID, String writeUUID) {
        UUID_SERVICE = serviceUUID;
        UUID_NOTIFY = notifyUUID;
        UUID_WRITE = writeUUID;
        return true;
    }
}


//    /*private BluetoothLeScanner mBluetoothLeScanner;
//    *//**
//     * 扫描设备
//     *//*
//    public void scanDevices() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (mBluetoothLeScanner != null) {
//                    mBluetoothLeScanner.stopScan(scanCallback);
//                }
//            }
//        },  1000);
//        mBluetoothLeScanner.startScan(null, createScanSetting(), scanCallback);
//    }
//    *//**
//     * 扫描广播数据设置
//     *
//     * @return
//     *//*
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public ScanSettings createScanSetting() {
//        ScanSettings.Builder builder = new ScanSettings.Builder();
//        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
//        //builder.setReportDelay(100);//设置延迟返回时间
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
//        }
//        return builder.build();
//    }
//    *//**
//     * 回调
//     *//*
//    private ScanCallback scanCallback=new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            byte[] scanData=result.getScanRecord().getBytes();
//            //把byte数组转成16进制字符串，方便查看
//
//        }
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//        }
//    };
//}
//*/