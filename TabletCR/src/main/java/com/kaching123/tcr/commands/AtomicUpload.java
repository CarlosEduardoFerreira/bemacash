package com.kaching123.tcr.commands;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by Carlos on 17/03/2017.
 */

public class AtomicUpload {

    public enum UploadType{
        LOCAL, WEB, BOTH
    }

    public enum UploadObject{
        DATA, EMPLOYEE
    }

    Context context;

    public void upload(BatchSqlCommand sql, UploadType type){
        upload(sql, type, UploadObject.DATA);
    }

    public void upload(BatchSqlCommand sql, UploadType type, UploadObject object) {

        context = TcrApplication.get().getApplicationContext();

        if(hasInternetConnection()) {

            if (type.equals(UploadType.LOCAL) || type.equals(UploadType.BOTH)) {
                Log.d("BemaCarl7", "AtomicUpload.upload.type|sql1: " + type + "|" + sql.toJson());
                ContentValues values = getContentValues(sql, System.currentTimeMillis(), true);
                context.getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandHostTable.URI_CONTENT), values);

            }

            if (type.equals(UploadType.WEB) || type.equals(UploadType.BOTH)) {
                Log.d("BemaCarl7", "AtomicUpload.upload.type|sql2: " + type + "|" + sql.toJson());
                ContentValues values = getContentValues(sql, System.currentTimeMillis(), false);
                context.getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandTable.URI_CONTENT), values);

                if (object.equals(UploadObject.DATA)) {
                    OfflineCommandsService.startUpload(context);
                } else {
                    OfflineCommandsService.startemployeeTableUpload(context);
                }
            }

        }

    }

    public boolean hasInternetConnection(){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if(isConnected) {
            Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection: Internet is connected.");
            //Toast.makeText(context,"Internet is connected",Toast.LENGTH_SHORT).show();
        }else {
            Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection: Internet is not connected.");
            //Toast.makeText(context, "Internet is not connected, please check your settings", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
