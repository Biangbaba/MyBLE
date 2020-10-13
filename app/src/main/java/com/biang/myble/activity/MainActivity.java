package com.biang.myble.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.biang.blemodule.BleUtils.BleController;
import com.biang.blemodule.entity.PostEntity;
import com.biang.blemodule.service.MyBleService;
import com.biang.blemodule.utils.LogUtil;
import com.biang.blemodule.utils.SumCheckUtil;
import com.biang.myble.R;

import com.biang.myble.base.BaseActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {
    Button scanBtn, getTimeBtn, setTimeBtn, getCurrentWalkBtn, getHistoryWalkBtn, clearWalkBtn, getCurrentHealthBtn, getHistoryHealthBtn, clearHealthBtn;
    Button getBatteryBtn;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private PostEntity postEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanBtn = findViewById(R.id.btn_scan);
        getTimeBtn = findViewById(R.id.btn_getTime);
        setTimeBtn = findViewById(R.id.btn_setTime);
        getCurrentWalkBtn = findViewById(R.id.btn_getCurrentWalk);
        getHistoryWalkBtn = findViewById(R.id.btn_getHistoryWalk);
        clearWalkBtn = findViewById(R.id.btn_clearWalk);
        getCurrentHealthBtn = findViewById(R.id.btn_getCurrentHealth);
        getHistoryHealthBtn = findViewById(R.id.btn_getHistoryHealth);
        clearHealthBtn = findViewById(R.id.btn_clearHealth);
        getBatteryBtn=findViewById(R.id.btn_getBattery);
        LogUtil.d(SumCheckUtil.makeChecksum("8201000001"));
        checkGps();
        setListener();
        BleController.getInstance().setUUID("0000ff00-0000-1000-8000-00805f9b34fb", "0000ff01-0000-1000-8000-00805f9b34fb", "0000ff02-0000-1000-8000-00805f9b34fb");
    }

    /**
     * 设置控件监听
     */
    public void setListener() {
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSer();
            }
        });
        getTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTime();
            }
        });
        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime();
            }
        });
        getCurrentWalkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentWalk();
            }
        });
        getHistoryWalkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHistoryWalk();
            }
        });
        clearWalkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearWalk();
            }
        });
        getCurrentHealthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentHealth();
            }
        });
        getHistoryHealthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHistoryHealth();
            }
        });
        clearHealthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHealth();
            }
        });
        getBatteryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBattery();
            }
        });
    }

    private void getBattery() {
        String value = "27 00 00";
        postValue(value);
    }

    private void getCurrentHealth() {
        String value = "21 01 00 00";
        postValue(value);
    }

    private void getHistoryHealth() {
        String value = "21 05 00 01 E4 07 09 1c";
        postValue(value);
    }

    private void clearHealth() {
        String value = "20 01 00 02";
        postValue(value);
    }

    private void clearWalk() {
        String value = "20 01 00 02";
        postValue(value);
    }

    private void getHistoryWalk() {
        String value = "20 05 00 01 E4 07 09 1c ";
        postValue(value);
    }

    private void getCurrentWalk() {
        String value = "20 01 00 00 ";
        postValue(value);
    }

    public void startSer() {
        Intent intent = new Intent(this, MyBleService.class);
        startService(intent);
    }

    private void getTime() {
        String value = "04 00 00";
        postValue(value);
    }

    private void setTime() {
        String value = "04 0800 E407 09 19 0E 00 00 08";
        postValue(value);
    }

    /**
     * 处理蓝牙数据
     *
     * @param entity
     */
    @Override
    public void bluetoothHandle(PostEntity entity) {

    }

    /**
     * 开启位置权限
     */
    private void checkGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.d("检查位置权限的开启");
            } else {

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //创建基本线程池
    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(50));

    private void postValue(final String data) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String rValue = data.replace(" ", "");
                    String value = rValue + SumCheckUtil.makeChecksum(data);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleController.getInstance().closeBleConn();
        BleController.getInstance().mBleGatt.close();
    }
}