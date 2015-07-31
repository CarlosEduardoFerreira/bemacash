package com.kaching123.tcr.service;

/**
 * Created by long.jiao on 7/30/2015.
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.bematechus.bemaUtils.PortInfo;
import com.kaching123.display.scale.BemaScale;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity;

import java.math.BigDecimal;

public class ScaleService extends Service {
    private static String LOG_TAG = "ScaleService";
    private IBinder mBinder = new ScaleBinder();
    private BemaScale scale;

    @Override
    public void onCreate() {
        super.onCreate();
        PortInfo info = BemaScale.scalePortInfo();
        info.setPortName(TcrApplication.get().getShopPref().scaleName().get());
        scale = new BemaScale(info);
        open();
    }

    private void open() {
        scale.open();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String readScale() {
        return scale.readScale();
    }

    public int getStatus(){
        return scale.getStatus();
    }

    public static void bind(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, ScaleService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public class ScaleBinder extends Binder {
        public ScaleService getService() {
            return ScaleService.this;
        }
    }
}
