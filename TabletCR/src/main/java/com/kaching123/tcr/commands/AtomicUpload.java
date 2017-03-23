package com.kaching123.tcr.commands;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

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

    public void upload(BatchSqlCommand sql, UploadType type){
        upload(sql, type, UploadObject.DATA);
    }

    public void upload(BatchSqlCommand sql, UploadType type, UploadObject object) {

        Context context = TcrApplication.get().getApplicationContext();

        if(type.equals(UploadType.LOCAL) || type.equals(UploadType.BOTH)) {
            Log.d("BemaCarl7", "AtomicUpload.upload.type|sql1: " + type + "|" + sql.toJson());
            ContentValues values = getContentValues(sql, System.currentTimeMillis(), true);
            context.getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandHostTable.URI_CONTENT), values);

        }

        if(type.equals(UploadType.WEB) || type.equals(UploadType.BOTH)) {
            Log.d("BemaCarl7", "AtomicUpload.upload.type|sql2: " + type + "|" + sql.toJson());
            ContentValues values = getContentValues(sql, System.currentTimeMillis(), false);
            context.getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandTable.URI_CONTENT), values);

            if(object.equals(UploadObject.DATA)) {
                OfflineCommandsService.startUpload(context);
            }else{
                OfflineCommandsService.startemployeeTableUpload(context);
            }
        }

    }


}
