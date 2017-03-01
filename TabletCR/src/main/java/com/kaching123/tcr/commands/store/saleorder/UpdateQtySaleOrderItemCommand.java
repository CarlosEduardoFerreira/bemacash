package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

public class UpdateQtySaleOrderItemCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);

    private static final String ARG_SALE_ITEM_GUID = "arg_sale_item_guid";
    private static final String ARG_QTY = "arg_item_qty";

    private static final String PARAM_SALE_ITEM_GUID = "param_sale_item_guid";

    private String saleItemId;
    private BigDecimal qty;

    private SaleOrderItemModel model;
    private SyncResult subResult;

    @Override
    protected TaskResult doCommand() {
        saleItemId = getStringArg(ARG_SALE_ITEM_GUID);
        qty = (BigDecimal) getArgs().getSerializable(ARG_QTY);

        model = new SaleOrderItemModel(saleItemId);
        model.qty = qty;

        if (!updateKitchenPrintStatus())
            return failed();

        return succeeded().add(PARAM_SALE_ITEM_GUID, saleItemId);
    }

    private boolean updateKitchenPrintStatus() {
        String orderGuid = null;
        Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(SaleItemTable.URI_CONTENT, 1))
                .projection(SaleItemTable.ORDER_GUID)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemId)
                .perform(getContext());
        if (c.moveToFirst()){
            orderGuid = c.getString(0);
        }
        c.close();
        if (orderGuid == null)
            return false;

        subResult = new UpdateSaleOrderKitchenPrintStatusCommand().sync(getContext(), orderGuid, KitchenPrintStatus.UPDATED, getAppCommandContext());
        return subResult != null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemId})
                .withValue(SaleItemTable.QUANTITY, _decimalQty(qty))
                .build());

        if (subResult.getLocalDbOperations() != null)
            operations.addAll(subResult.getLocalDbOperations());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sqlCommand = batchUpdate(SaleOrderItemModel.class);

        SaleOrderItemJdbcConverter converter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(SaleOrderItemModel.class);
        sqlCommand.add(converter.updateQty(saleItemId, qty));
        sqlCommand.add(subResult.getSqlCmd());

        return sqlCommand;
    }

    public static void start(Context context, String saleItemGuid, BigDecimal qty, BaseUpdateQtySaleOrderItemCallback callback) {
        create(UpdateQtySaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .arg(ARG_QTY, qty)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseUpdateQtySaleOrderItemCallback {

        @OnSuccess(UpdateQtySaleOrderItemCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String saleItemGuid) {
            onSuccess(saleItemGuid);
        }

        protected abstract void onSuccess(String saleItemGuid);

    }
}
