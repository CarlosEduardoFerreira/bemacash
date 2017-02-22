package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.AsyncTaskCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.TableOrderTable;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._nullableDate;


/**
 * Created by Rodrigo Busata on 10/06/16.
 */
public class TableOrderModel implements IValueModel, Serializable {

    protected static final Uri URI = ShopProvider.contentUri(TableOrderTable.URI_CONTENT);

    public String guid;
    public String name;
    public String description;
    public Integer qtyCustomerOrder;
    public Integer qtyCurrentCustomerOrder;
    public Date statusTime;
    public BigDecimal amount;

    private List<String> mIgnoreFields;

    public TableOrderModel(Cursor c) {
        this(c.getString(c.getColumnIndex(TableOrderTable.GUID)),
                c.getString(c.getColumnIndex(TableOrderTable.NAME)),
                c.getString(c.getColumnIndex(TableOrderTable.DESCRIPTION)),
                c.getInt(c.getColumnIndex(TableOrderTable.QTY_CUSTOMER_ORDER)),
                c.getInt(c.getColumnIndex(TableOrderTable.QTY_CURRENT_CUSTOMER_ORDER)),
                _nullableDate(c, c.getColumnIndex(TableOrderTable.STATUS_TIME)),
                _decimal(c, c.getColumnIndex(TableOrderTable.AMOUNT)),
                null);

    }

    public TableOrderModel(String guid,
                           String name,
                           String description,
                           Integer qtyCustomerOrder,
                           Integer qtyCurrentCustomerOrder,
                           Date statusTime,
                           BigDecimal amount,
                           List<String> ignoreFields) {
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.qtyCustomerOrder = qtyCustomerOrder;
        this.qtyCurrentCustomerOrder = qtyCurrentCustomerOrder;
        this.statusTime = statusTime;
        this.amount = amount;

        this.mIgnoreFields = ignoreFields;
    }

    public static int countTables(Context context) {
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(TableOrderTable.URI_CONTENT))
                        .perform(context)
        ) {

            return c.getCount();
        }
    }


    public static boolean hasTableName(Context context, String name) {
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(TableOrderTable.URI_CONTENT))
                        .where(TableOrderTable.NAME + " = ?", name)
                        .perform(context)
        ) {

            return c != null && c.getCount() > 0;
        }
    }

    static public boolean changeTable(SuperBaseActivity activity, TableOrderModel modelOld,
                                      Date newStatusTime,
                                      BigDecimal newAmount,
                                      Integer newQtyCustomerOrder,
                                      AsyncTaskCommand.CallBackTask tableOrderTask,
                                      boolean checkStatus) {

        TableOrderModel model = TableOrderModel.getByGuid(activity, modelOld.guid);
        if (model == null) return true;

        if (newStatusTime != null){
            modelOld.statusTime = newStatusTime;
            model.statusTime = newStatusTime;
        }
        if (newAmount != null){
            modelOld.amount = newAmount;
            model.amount = newAmount;
        }
        if (newQtyCustomerOrder != null){
            modelOld.qtyCurrentCustomerOrder = newQtyCustomerOrder;
            model.qtyCurrentCustomerOrder = newQtyCustomerOrder;
        }

        new TableOrderModel.TableOrderTask(activity, false, tableOrderTask).execute(model);

        return true;
    }

    public static TableOrderModel getByGuid(Context context, String guid) {

        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(TableOrderTable.URI_CONTENT))
                        .where(ShopStore.TableOrderTable.GUID + " = ?", guid)
                        .where(ShopStore.TableOrderTable.IS_DELETED + " = 0")
                        .perform(context)
        ) {
            if (c != null && c.moveToFirst()) {
                return new TableOrderModel(c);
            }
        }
        return null;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.GUID)) v.put(TableOrderTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.NAME)) v.put(TableOrderTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.DESCRIPTION)) v.put(TableOrderTable.DESCRIPTION, description);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.QTY_CUSTOMER_ORDER)) v.put(TableOrderTable.QTY_CUSTOMER_ORDER, qtyCustomerOrder);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.QTY_CURRENT_CUSTOMER_ORDER)) v.put(TableOrderTable.QTY_CURRENT_CUSTOMER_ORDER, qtyCurrentCustomerOrder);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.STATUS_TIME)) _nullableDate(v, TableOrderTable.STATUS_TIME, statusTime);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableOrderTable.AMOUNT)) v.put(TableOrderTable.AMOUNT, _decimal(amount));

        return v;
    }

    @Override
    public String getIdColumn() {
        return TableOrderTable.GUID;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TableOrderModel && Objects.equals(((TableOrderModel) other).guid, this.guid);
    }

    static public class TableOrderTask extends AsyncTaskCommand<TableOrderModel> {

        public TableOrderTask(SuperBaseActivity context, boolean isInsert, CallBackTask callBack) {
            super(context, URI, isInsert, callBack);
        }

        public TableOrderTask(SuperBaseActivity context, boolean isInsert, CallBackTask callBack, boolean isDeleting) {
            super(context, URI, isInsert, callBack, isDeleting);
        }
    }
}
