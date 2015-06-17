package com.kaching123.tcr.service.response;

import android.content.ContentValues;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.EmployeeCommissionsTable;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by pkabakov on 19.01.2015.
 */
public abstract class BaseOrdersResponseHandler {

    private static final String SALE_ORDER_ITEMS = "SALE_ORDER_ITEMS";
    private static final String SALE_ORDER_ITEM_ADDONS = "SALE_ORDER_ITEM_ADDONS";
    private static final String PAYMENT_TRANSACTIONS = "PAYMENT_TRANSACTIONS";
    private static final String BILL_PAYMENTS_DESCRIPTIONS = "BILL_PAYMENTS_DESCRIPTIONS";
    private static final String RECEIVED_TIPS = "RECEIVED_TIPS";
    private static final String COMMISSIONS = "COMMISSIONS";
    private static final String UNITS = "UNITS";

    public static final String[] TABLES_URIS = new String[] {
            UnitTable.URI_CONTENT,

            SaleOrderTable.URI_CONTENT,
            SaleItemTable.URI_CONTENT,
            SaleAddonTable.URI_CONTENT,
            PaymentTransactionTable.URI_CONTENT,
            BillPaymentDescriptionTable.URI_CONTENT,
            //CreditReceiptTable.URI_CONTENT,
            EmployeeTipsTable.URI_CONTENT,
            EmployeeCommissionsTable.URI_CONTENT
    };

    public abstract boolean handleResponse(GetArrayResponse response) throws JSONException;

    public boolean parseResponse(JdbcJSONArray data) throws JSONException {
        if (data == null){
            Logger.w("BaseOrdersResponseHandler: empty response");
            return false;
        }

        return parseSaleOrderArray(data);
    }

    protected boolean parseSaleOrderArray(JdbcJSONArray data) throws JSONException {
        for (int i = 0; i < data.length(); i++) {
            JdbcJSONObject rs = data.getJSONObject(i);
            ContentValues value = parseItem(rs, JdbcFactory.getConverter(SaleOrderTable.TABLE_NAME));

            saveResult(SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, value);

            JdbcJSONArray saleOrderItems = rs.isNull(SALE_ORDER_ITEMS) ? null : rs.getJSONArray(SALE_ORDER_ITEMS);
            if (saleOrderItems != null) {
                parseSaleOrderItemArray(saleOrderItems);
            }

            JdbcJSONArray billPaymentDescriptions = rs.isNull(BILL_PAYMENTS_DESCRIPTIONS) ? null : rs.getJSONArray(BILL_PAYMENTS_DESCRIPTIONS);
            if (billPaymentDescriptions != null) {
                ArrayList<ContentValues> billPaymentValues = parseResponseArray(billPaymentDescriptions, JdbcFactory.getConverter(BillPaymentDescriptionTable.TABLE_NAME));
                if (billPaymentValues.size() != 0) {
                    saveResult(BillPaymentDescriptionTable.TABLE_NAME, BillPaymentDescriptionTable.GUID, billPaymentValues);
                }
            }

            JdbcJSONArray paymentTransactions = rs.isNull(PAYMENT_TRANSACTIONS) ? null : rs.getJSONArray(PAYMENT_TRANSACTIONS);
            if (paymentTransactions != null) {
                ArrayList<ContentValues> paymentValues = parseResponseArray(paymentTransactions, JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME));
                if (paymentValues.size() != 0) {
                    saveResult(PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID, paymentValues);
                }
            }

            JdbcJSONArray receivedTips = rs.isNull(RECEIVED_TIPS) ? null : rs.getJSONArray(RECEIVED_TIPS);
            if (receivedTips != null) {
                ArrayList<ContentValues> tipsValues = parseResponseArray(receivedTips, JdbcFactory.getConverter(EmployeeTipsTable.TABLE_NAME));
                if (tipsValues.size() != 0) {
                    saveResult(EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID, tipsValues);
                }
            }

            JdbcJSONArray commissions = rs.isNull(COMMISSIONS) ? null : rs.getJSONArray(COMMISSIONS);
            if (commissions != null) {
                ArrayList<ContentValues> commissionsValues = parseResponseArray(commissions, JdbcFactory.getConverter(EmployeeCommissionsTable.TABLE_NAME));
                if (commissionsValues.size() != 0) {
                    saveResult(EmployeeCommissionsTable.TABLE_NAME, EmployeeCommissionsTable.GUID, commissionsValues);
                }
            }

            JdbcJSONArray units = rs.isNull(UNITS) ? null : rs.getJSONArray(UNITS);
            if (units != null) {
                ArrayList<ContentValues> unitsValues = parseResponseArray(units, JdbcFactory.getConverter(UnitTable.TABLE_NAME));
                if (unitsValues.size() != 0) {
                    saveResult(UnitTable.TABLE_NAME, UnitTable.ID, unitsValues);
                }
            }

        }
        return data.length() > 0;
    }

    protected void parseSaleOrderItemArray(JdbcJSONArray data) throws JSONException {
        for (int i = 0; i < data.length(); i++) {
            JdbcJSONObject rs = data.getJSONObject(i);
            ContentValues values = parseItem(rs, JdbcFactory.getConverter(SaleItemTable.TABLE_NAME));

            saveResult(SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID, values);

            JdbcJSONArray saleOrderItemAddons = rs.isNull(SALE_ORDER_ITEM_ADDONS) ? null : rs.getJSONArray(SALE_ORDER_ITEM_ADDONS);
            if (saleOrderItemAddons != null) {
                ArrayList<ContentValues> saleAddonValues = parseResponseArray(saleOrderItemAddons, JdbcFactory.getConverter(SaleAddonTable.TABLE_NAME));
                if (saleAddonValues.size() != 0) {
                    saveResult(SaleAddonTable.TABLE_NAME, SaleAddonTable.GUID, saleAddonValues);
                }
            }
        }

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

    protected abstract void saveResult(String localTableName, String contentPath, ArrayList<ContentValues> result);

    protected abstract void saveResult(String localTableName, String contentPath, ContentValues result);

}
