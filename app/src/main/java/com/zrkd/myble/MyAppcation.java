package com.zrkd.myble;

import android.app.Application;


public class MyAppcation extends Application {

    private static MyAppcation app;
    private boolean connected;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static MyAppcation getInstance() {
        return app;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
