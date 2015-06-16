package com.kaching123.tcr.service.response;

import android.content.ContentValues;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
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
public class SyncOldActiveOrdersResponseHandler {

    private static final String SALE_ORDER_ITEMS = "SALE_ORDER_ITEMS";
    private static final String SALE_ORDER_ITEM_ADDONS = "SALE_ORDER_ITEM_ADDONS";
    private static final String PAYMENT_TRANSACTIONS = "PAYMENT_TRANSACTIONS";
    private static final String BILL_PAYMENTS_DESCRIPTIONS = "BILL_PAYMENTS_DESCRIPTIONS";
    private static final String RECEIVED_TIPS = "RECEIVED_TIPS";

    private final SyncOpenHelper syncOpenHelper;

    public SyncOldActiveOrdersResponseHandler(SyncOpenHelper syncOpenHelper) {
        this.syncOpenHelper = syncOpenHelper;
    }

    public boolean handleResponse(GetArrayResponse response) throws JSONException {
        JdbcJSONArray data = response.getEntity();
        if(data == null){
            Logger.d("Empty response");
            return false;
        }

        syncOpenHelper.beginTransaction();
        try {
            ArrayList<ContentValues> values = parseSaleOrderArray(data);
            if (values.size() != 0) {
                saveResult(SaleOrderTable.TABLE_NAME, values);
            }

            syncOpenHelper.setTransactionSuccessful();

            return values.size() > 0;
        } finally {
            syncOpenHelper.endTransaction();
        }
    }

    protected ArrayList<ContentValues> parseSaleOrderArray(JdbcJSONArray data) throws JSONException {
        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        for (int i = 0; i < data.length(); i++) {
            JdbcJSONObject rs = data.getJSONObject(i);
            values.add(parseItem(rs, JdbcFactory.getConverter(SaleOrderTable.TABLE_NAME)));


            JdbcJSONArray saleOrderItems = rs.isNull(SALE_ORDER_ITEMS) ? null : rs.getJSONArray(SALE_ORDER_ITEMS);
            if (saleOrderItems != null) {
                ArrayList<ContentValues> saleItemValues = parseSaleOrderItemArray(saleOrderItems);
                if (saleItemValues.size() != 0) {
                    saveResult(SaleItemTable.TABLE_NAME, saleItemValues);
                }
            }

            JdbcJSONArray paymentTransactions = rs.isNull(PAYMENT_TRANSACTIONS) ? null : rs.getJSONArray(PAYMENT_TRANSACTIONS);
            if (paymentTransactions != null) {
                ArrayList<ContentValues> paymentValues = parseResponseArray(paymentTransactions, JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME));
                if (paymentValues.size() != 0) {
                    saveResult(PaymentTransactionTable.TABLE_NAME, paymentValues);
                }
            }

            JdbcJSONArray receivedTips = rs.isNull(RECEIVED_TIPS) ? null : rs.getJSONArray(RECEIVED_TIPS);
            if (receivedTips != null) {
                ArrayList<ContentValues> tipsValues = parseResponseArray(receivedTips, JdbcFactory.getConverter(EmployeeTipsTable.TABLE_NAME));
                if (tipsValues.size() != 0) {
                    saveResult(EmployeeTipsTable.TABLE_NAME, tipsValues);
                }
            }

            JdbcJSONArray billPaymentDescriptions = rs.isNull(BILL_PAYMENTS_DESCRIPTIONS) ? null : rs.getJSONArray(BILL_PAYMENTS_DESCRIPTIONS);
            if (billPaymentDescriptions != null) {
                ArrayList<ContentValues> billPaymentValues = parseResponseArray(billPaymentDescriptions, JdbcFactory.getConverter(BillPaymentDescriptionTable.TABLE_NAME));
                if (billPaymentValues.size() != 0) {
                    saveResult(BillPaymentDescriptionTable.TABLE_NAME, billPaymentValues);
                }
            }

        }
        return values;
    }

    protected ArrayList<ContentValues> parseSaleOrderItemArray(JdbcJSONArray data) throws JSONException {
        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        for (int i = 0; i < data.length(); i++) {
            JdbcJSONObject rs = data.getJSONObject(i);
            values.add(parseItem(rs, JdbcFactory.getConverter(SaleItemTable.TABLE_NAME)));

            JdbcJSONArray saleOrderItemAddons = rs.isNull(SALE_ORDER_ITEM_ADDONS) ? null : rs.getJSONArray(SALE_ORDER_ITEM_ADDONS);
            if (saleOrderItemAddons != null) {
                ArrayList<ContentValues> saleAddonValues = parseResponseArray(saleOrderItemAddons, JdbcFactory.getConverter(SaleAddonTable.TABLE_NAME));
                if (saleAddonValues.size() != 0) {
                    saveResult(SaleAddonTable.TABLE_NAME, saleAddonValues);
                }
            }
        }
        return values;
    }

    protected ArrayList<ContentValues> parseResponseArray(JdbcJSONArray data, JdbcConverter converter) throws JSONException {
        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        for (int i = 0; i < data.length(); i++) {
            JdbcJSONObject rs = data.getJSONObject(i);
            values.add(parseItem(rs, converter));
        }
        return values;
    }

    protected ContentValues parseItem(JdbcJSONObject rs, JdbcConverter converter) throws JSONException {
        IValueModel valuesModel = converter.toValues(rs);
        ContentValues v = valuesModel.toValues();
        if(converter.supportUpdateTimeFlag()) {
            v.put(ShopStore.DEFAULT_UPDATE_TIME, rs.getTimestamp(JdbcBuilder.FIELD_UPDATE_TIME).getTime());
        }
        if (converter.supportDeleteFlag()) {
            v.put(ShopStore.DEFAULT_IS_DELETED, rs.getBoolean(JdbcBuilder.FIELD_IS_DELETED));
        }
        if(converter.supportDraftFlag()) {
            v.put(ShopStore.DEFAULT_IS_DRAFT, 1);
        }
        return v;
    }

    protected void saveResult(String localTableName, ArrayList<ContentValues> result) {
        boolean success = syncOpenHelper.insert(localTableName, result.toArray(new ContentValues[result.size()]), false);
        if (!success) {
            throw new RuntimeException("some data was not saved");
        }
    }

}
