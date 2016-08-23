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

import com.bematechus.bemaUtils.PortInfo;
import com.kaching123.display.scale.BemaScale;
import com.kaching123.tcr.TcrApplication;

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
        if(scale != null){
            scale.close();
        }
        scale = null;
    }

    public String readScale() {
        if (scale != null)
            return scale.readScale();
        return "0.00";
    }

    public boolean isUnitsLabelMatch(String unitsLabel) {
        if (scale != null)
            return scale.getUnitsLabel().equalsIgnoreCase(unitsLabel);
        return true;
    }

//    public String readScale(String unitsLabel) {
//        BigDecimal qty = new BigDecimal(scale.readScale());
//        if (scale != null) {
//            if(!scale.getUnitsLabel().equalsIgnoreCase(unitsLabel)){
//                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                        ScaleService.this);
//                alertDialogBuilder.setTitle("Scale Warning");
//                if(unitsLabel.equalsIgnoreCase("OZ")){
//                    qty = qty.multiply(new BigDecimal(16));
//                }else if(unitsLabel.equalsIgnoreCase("LB")){
//                    qty = qty.multiply(new BigDecimal(0.0625));
//                }
//            }
//            return qty.toString();
//        }
//        return "0.00";
//    }

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
