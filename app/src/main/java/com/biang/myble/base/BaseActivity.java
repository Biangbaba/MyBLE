package com.biang.myble.base;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.biang.blemodule.entity.PostEntity;
import com.biang.blemodule.utils.LogUtil;
import com.biang.blemodule.utils.StatusBarUtil;
import com.biang.myble.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author BaseActivity(用于提取公共因素，优化代码用) 使用泛型继承 ViewBinding ，
 *  由继承 BaseActivity 类的子类来实现，就可直接对控件进行操作了
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Activity mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 为状态栏添加颜色
        StatusBarUtil.setWindowStatusBarColor(this, R.color.bbb37);
        /*
         *  判断 EventBus 是否注册过，如果没注册，才会注册。如果已注册过，则不需要注册，否则会报 EventBusException 错误
         */
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        mContext=this;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PostEntity entity){
        if (!TextUtils.isEmpty(entity.getEvent())){
            LogUtil.e("==================="+entity.getEvent());
            bluetoothHandle(entity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onBack();
    }

    public void onBack() {
        finish();
    }

    /**
     * 处理蓝牙数据
     * @param entity
     */
    public abstract void bluetoothHandle(PostEntity entity);
}
