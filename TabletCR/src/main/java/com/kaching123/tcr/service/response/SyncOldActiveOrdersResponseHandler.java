package com.kaching123.tcr.service.response;

import android.content.ContentValues;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CommissionsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeCommissionsTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pkabakov on 19.01.2015.
 */
public class SyncOldActiveOrdersResponseHandler extends BaseOrdersResponseHandler {

    private final SyncOpenHelper syncOpenHelper;

    public SyncOldActiveOrdersResponseHandler(SyncOpenHelper syncOpenHelper) {
        this.syncOpenHelper = syncOpenHelper;
    }

    @Override
    public boolean handleResponse(GetArrayResponse response) throws JSONException {
        JdbcJSONArray data = response.getEntity();
        if(data == null){
            Logger.w("SyncOldActiveOrdersResponseHandler: empty response");
            return false;
        }

        syncOpenHelper.beginTransaction();
        try {
            //TODO: improve?
            boolean hasData = parseResponse(data);

            syncOpenHelper.setTransactionSuccessful();

            return hasData;
        } finally {
            syncOpenHelper.endTransaction();
        }
    }

    @Override
    protected void saveResult(String localTableName, String idColumn, ArrayList<ContentValues> result) {
        boolean success = syncOpenHelper.insert(localTableName, result.toArray(new ContentValues[result.size()]), false);
        if (!success) {
            throw new RuntimeException("some data was not saved");
        }
    }

    @Override
    protected void saveResult(String localTableName, String idColumn, ContentValues result) {
        boolean success = syncOpenHelper.insert(localTableName, new ContentValues[]{result}, false);
        if (!success) {
            throw new RuntimeException("some data was not saved");
        }
    }
}
