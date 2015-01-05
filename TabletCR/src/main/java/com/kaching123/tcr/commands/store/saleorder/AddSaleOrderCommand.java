package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AddSaleOrderCommand extends AsyncCommand {

    public static final String ARG_ORDER = "arg_order";
    public static final String ARG_SKIP_NOTIFY = "arg_skip_notify";

    private static final String EXTRA_GUID = "EXTRA_GUID";

    private SaleOrderModel order;

    private boolean skipNotify;

    @Override
    protected TaskResult doCommand() {
        Logger.d("AddSaleOrderCommand doCommand");
        order = (SaleOrderModel) getArgs().getSerializable(ARG_ORDER);
        skipNotify = getBooleanArg(ARG_SKIP_NOTIFY);

        if (order == null)
            order = AddItem2SaleOrderCommand.createSaleOrder(getContext(), getAppCommandContext().getRegisterId(), getAppCommandContext().getEmployeeGuid(), getAppCommandContext().getShiftGuid(), OrderType.SALE, BigDecimal.ZERO);

        return succeeded().add(EXTRA_GUID, order.guid);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(skipNotify ? ShopProvider.getNoNotifyContentUri(SaleOrderTable.URI_CONTENT) : ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT))
                .withValues(order.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(order).insertSQL(order, getAppCommandContext());
    }

    public static void start(Context context, SaleOrderModel order, boolean skipNotify, BaseAddSaleOrderCommandCallback callback) {
        create(AddSaleOrderCommand.class).arg(ARG_ORDER, order).arg(ARG_SKIP_NOTIFY, skipNotify).callback(callback).queueUsing(context);
    }

    /**
     * create order from add item 2 sale order. can be standalone *
     */
    public boolean sync(Context context, SaleOrderModel order, IAppCommandContext appCommandContext) {
        TaskResult result = syncStandalone(context, bundle(order), appCommandContext);
        return !isFailed(result);
    }

    public boolean sync(Context context, SaleOrderModel order, boolean skipNotify, IAppCommandContext appCommandContext) {
        TaskResult result = syncStandalone(context, bundle(order, skipNotify), appCommandContext);
        return !isFailed(result);
    }

    private static Bundle bundle(SaleOrderModel order) {
        return bundle(order, false);
    }

    public static Bundle bundle(SaleOrderModel order, boolean skipNotify) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ORDER, order);
        bundle.putBoolean(ARG_SKIP_NOTIFY, skipNotify);
        return bundle;
    }

    public static abstract class BaseAddSaleOrderCommandCallback {

        @OnSuccess(AddSaleOrderCommand.class)
        public void handleSuccess(@Param(EXTRA_GUID) String guid) {
            onSuccess(guid);
        }

        @OnFailure(AddSaleOrderCommand.class)
        public void handleFailure() {
            onFailure();
        }

        protected abstract void onSuccess(String guid);

        protected abstract void onFailure();
    }
}
