package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.AsyncTaskCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CustomerOrderTable;

/**
 * Created by Rodrigo Busata on 10/06/16.
 */
public class CustomerOrderModel implements IValueModel, Serializable {

    public final static Uri URI = ShopProvider.contentUri(ShopStore.CustomerOrderTable.URI_CONTENT);

    public String guid;
    public String description;
    public String code;
    public String orderGuid;
    public String tableGuid;

    private List<String> mIgnoreFields;

    public CustomerOrderModel(Cursor c) {
        this.guid = c.getString(c.getColumnIndex(CustomerOrderTable.GUID));
        this.code = c.getString(c.getColumnIndex(CustomerOrderTable.CODE));
        this.description = c.getString(c.getColumnIndex(CustomerOrderTable.DESCRIPTION));
        this.orderGuid = c.getString(c.getColumnIndex(CustomerOrderTable.ORDER_GUID));
        this.tableGuid = c.getString(c.getColumnIndex(CustomerOrderTable.TABLE_GUID));

    }

    public CustomerOrderModel(String guid,
                              String code,
                              String description,
                              String orderGuid,
                              String tableGuid,
                              List<String> ignoreFields) {
        this.guid = guid;
        this.code = code;
        this.description = description;
        this.orderGuid = orderGuid;
        this.tableGuid = tableGuid;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(CustomerOrderTable.GUID)) v.put(CustomerOrderTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CustomerOrderTable.CODE)) v.put(CustomerOrderTable.CODE, code);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CustomerOrderTable.DESCRIPTION)) v.put(CustomerOrderTable.DESCRIPTION, description);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CustomerOrderTable.ORDER_GUID)) v.put(CustomerOrderTable.ORDER_GUID, orderGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CustomerOrderTable.TABLE_GUID)) v.put(CustomerOrderTable.TABLE_GUID, tableGuid);

        return v;
    }

    @Override
    public String getIdColumn() {
        return CustomerOrderTable.GUID;
    }

    public static List<String> getSaleOrderGuids(Context context, String tableGuid) {
        List<String> guids = new ArrayList<>();

        try (
                Cursor c = ProviderAction.query(URI)
                        .projection(CustomerOrderTable.ORDER_GUID)
                        .where(CustomerOrderTable.TABLE_GUID + " = ?", tableGuid)
                        .where(CustomerOrderTable.IS_DELETED + " = 0")
                        .perform(context)
        ) {
            while (c != null && c.moveToNext()) {
                guids.add(c.getString(0));
            }
        }
        return guids;
    }

    public static List<CustomerOrderModel> getByTableGuidAndFix(SuperBaseActivity context, String tableGuid, boolean fromSync) {
        List<CustomerOrderModel> saleOrders = new ArrayList<>();

        try (
                Cursor c = ProviderAction.query(URI)
                        .where(CustomerOrderTable.TABLE_GUID + " = ?", tableGuid)
                        .where(CustomerOrderTable.IS_DELETED + " = 0")
                        .perform(context)
        ) {
            while (c != null && c.moveToNext()) {
                CustomerOrderModel customerOrderModel = new CustomerOrderModel(c);
                SaleOrderModel saleOrderModel = SaleOrderModel.getById(context, customerOrderModel.orderGuid);
                if (!fromSync && saleOrderModel!= null && saleOrderModel.orderStatus != OrderStatus.ACTIVE){
                    new CustomerOrderModel.CustomerOrderModelTask(context, false, null, true).execute(customerOrderModel);

                } else {
                    saleOrders.add(customerOrderModel);
                }
            }
        }
        return saleOrders;
    }

    public static CustomerOrderModel getByGuid(Context context, String guid) {

        try (
                Cursor c = ProviderAction.query(URI)
                        .where(CustomerOrderTable.GUID + " = ?", guid)
                        .where(CustomerOrderTable.IS_DELETED + " = 0")
                        .perform(context)
        ) {
            if (c != null && c.moveToFirst()) {
                return new CustomerOrderModel(c);
            }
        }
        return null;
    }

    public static class CustomerOrderModelTask extends AsyncTaskCommand<CustomerOrderModel> {

        public CustomerOrderModelTask(SuperBaseActivity context, boolean isInsert, CallBackTask callBack) {
            super(context, URI, isInsert, callBack);
        }

        public CustomerOrderModelTask(SuperBaseActivity context, boolean isInsert, CallBackTask callBack, boolean isDeleting) {
            super(context, URI, isInsert, callBack, isDeleting);
        }
    }

    public static String getTitleCustomerOrder(Context context, String orderGuid){
        try (
                Cursor c = ProviderAction.query(URI)
                .where(CustomerOrderTable.ORDER_GUID + " = ?", orderGuid)
                .perform(context)
                ){

            if (c != null && c.moveToFirst()) {
                CustomerOrderModel model = new CustomerOrderModel(c);
                TableOrderModel tableModel = TableOrderModel.getByGuid(context, model.tableGuid);

                if (tableModel != null){
                    return String.format("%s %s - %s %s", context.getString(R.string.table), tableModel.name, context.getString(R.string.customer_order), model.code);
                }
            }

            return null;
        }
    }
}
