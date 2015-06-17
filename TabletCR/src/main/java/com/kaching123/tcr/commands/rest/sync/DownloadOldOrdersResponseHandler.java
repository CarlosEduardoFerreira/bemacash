package com.kaching123.tcr.commands.rest.sync;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.service.response.BaseOrdersResponseHandler;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopProviderExt.Method;
import com.kaching123.tcr.store.ShopSchemaEx;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.IBemaSyncTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by pkabakov on 05.02.2015.
 */
public class DownloadOldOrdersResponseHandler extends BaseOrdersResponseHandler {

    private final Context context;

    private ArrayList<String> orderGuids = new ArrayList<String>();

    public DownloadOldOrdersResponseHandler(Context context) {
        this.context = context;
    }

    public String[] handleOrdersResponse(GetArrayResponse response) throws JSONException {
        handleResponse(response);
        return orderGuids.toArray(new String[orderGuids.size()]);
    }

    @Override
    public boolean handleResponse(GetArrayResponse response) throws JSONException {
        orderGuids.clear();
        JdbcJSONArray data = response.getEntity();
        if (data == null){
            Logger.w("DownloadOldOrdersResponseHandler: empty response");
            return false;
        }

        ShopProviderExt.callMethod(context, Method.TRANSACTION_START, null, null);
        try {
            boolean hasData = parseResponse(data);

            markRecordsAsLive();

            ShopProviderExt.callMethod(context, Method.TRANSACTION_COMMIT, null, null);

            return hasData;
        } finally {
            ShopProviderExt.callMethod(context, Method.TRANSACTION_END, null, null);
        }
    }

    private void markRecordsAsLive() {
        for (String uri : TABLES_URIS) {
            ProviderAction.update(ShopProvider.contentUri(uri))
                    .where(IBemaSyncTable.UPDATE_IS_DRAFT + " = ?", "1")
                    .value(IBemaSyncTable.UPDATE_IS_DRAFT, 0)
                    .perform(context);
        }
    }

    @Override
    protected ContentValues parseItem(JdbcJSONObject rs, JdbcConverter converter) throws JSONException {
        ContentValues values = super.parseItem(rs, converter);

        if (converter instanceof SaleOrdersJdbcConverter &&
                (!values.containsKey(SaleOrderTable.PARENT_ID) || TextUtils.isEmpty(values.getAsString(SaleOrderTable.PARENT_ID)))) {
            orderGuids.add(values.getAsString(SaleOrderTable.GUID));
        }

        return values;
    }

    @Override
    protected boolean supportUpdateTimeFlag() {
        return false;
    }

    @Override
    protected void saveResult(String localTableName, String idColumn, ArrayList<ContentValues> result) {
        ShopProviderExt.insertUpdateValues(context, localTableName, idColumn, result.toArray(new ContentValues[result.size()]));
    }

    @Override
    protected void saveResult(String localTableName, String idColumn, ContentValues result) {
        ShopProviderExt.insertUpdateValues(context, localTableName, idColumn, new ContentValues[]{result});
    }
}
