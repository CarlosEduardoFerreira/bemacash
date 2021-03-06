package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;

import java.util.ArrayList;

/**
 * Created by mboychenko on 3/3/2017.
 */

public class UpdateSaleOrderOnRegisterCommand extends UpdateSaleOrderCommand {

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_ON_REGISTER = "ARG_ON_REGISTER";

    @Override
    protected SaleOrderModel readOrder() {
        String guid = getStringArg(ARG_ORDER_GUID);
        boolean onRegister = getBooleanArg(ARG_ON_REGISTER, false);

        if(guid == null) {
            failed();
            return null;
        }
        Cursor c = ProviderAction.query(URI_ORDER)
                .where(ShopStore.SaleOrderTable.GUID + " = ?", guid)
                .where(ShopStore.SaleOrderTable.STATUS + " = ?", OrderStatus.HOLDON.ordinal())
                .perform(getContext());

        SaleOrderModel order = null;
        if (c.moveToFirst()) {
            order = new SaleOrderModel(c);
        }
        c.close();

        if (order == null)
            return null;

        order.setOrderOnRegister(onRegister);
        return order;

    }

    public static void start(Context context, String orderGuid, boolean onRegister) {
        create(UpdateSaleOrderOnRegisterCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ON_REGISTER, onRegister)
                .queueUsing(context);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withSelection(ShopStore.SaleOrderTable.GUID + " = ?", new String[]{order.guid})
                .withValue(ShopStore.SaleOrderTable.ON_REGISTER, order.orderOnRegister)
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(ShopStore.SaleOrderTable.TABLE_NAME);

        BatchSqlCommand batch = batchUpdate(order);
        batch.add(converter.updateOnRegisterStatus(order, getAppCommandContext()));

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
    }
}
