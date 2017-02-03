package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by gdubina on 11/11/13.
 */
public class HoldOrderCommand extends UpdateSaleOrderCommand {

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_ACTION = "ARG_ACTION";
    private static final String ARG_TITLE = "ARG_ORDER_TITLE";

    public enum HoldOnAction{
        ADD,
        REMOVE
    }

    private HoldOnAction action;

    @Override
    protected SaleOrderModel readOrder() {
        String guid = getStringArg(ARG_ORDER_GUID);
        String title = getStringArg(ARG_TITLE);
        action = (HoldOnAction) getArgs().getSerializable(ARG_ACTION);

        Cursor c = ProviderAction.query(URI_ORDER)
            .where(ShopStore.SaleOrderTable.GUID + " = ?", guid)
            .perform(getContext());
        try {
            SaleOrderModel order = null;
            if (c.moveToFirst()) {
                order = new SaleOrderModel(c);
            }
            if (order == null)
                return null;

            order.setHoldName(action == HoldOnAction.ADD ? title : "Canceled because of quantity");

            return order;
        } finally {
            c.close();
        }
    }

    public static void start(Context context, BaseHoldOrderCallback callback, String orderGuid, String title, HoldOnAction action) {
        create(HoldOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ACTION, action)
                .arg(ARG_TITLE, title).callback(callback).queueUsing(context);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        if(action == HoldOnAction.ADD) {
            operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                    .withValues(order.toValues())
                    .withSelection(ShopStore.SaleOrderTable.GUID + " = ?", new String[]{order.guid})
                    .build());
        }
        ContentValues values = new ContentValues();
        values.put(ShopStore.SaleOrderTable.STATUS, action == HoldOnAction.ADD ? OrderStatus.HOLDON.ordinal() : OrderStatus.CANCELED.ordinal());
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withValues(values)
                .withSelection(ShopStore.SaleOrderTable.GUID + " = ?", new String[]{getStringArg(ARG_ORDER_GUID)})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        order.orderStatus = action == HoldOnAction.ADD ? OrderStatus.HOLDON : OrderStatus.CANCELED;
        return JdbcFactory.getConverter(order).updateSQL(order, getAppCommandContext());
    }

    public static abstract class BaseHoldOrderCallback {

        @OnSuccess(HoldOrderCommand.class)
        public void handleSuccess(){
            onSuccess();
        }

        protected abstract void onSuccess();

    }
}
