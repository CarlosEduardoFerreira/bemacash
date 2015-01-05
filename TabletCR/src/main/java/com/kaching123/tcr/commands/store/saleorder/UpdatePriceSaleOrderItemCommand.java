package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

public class UpdatePriceSaleOrderItemCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);

    private static final String ARG_SALE_ITEM_GUID = "arg_sale_item_guid";
    private static final String ARG_PRICE = "arg_item_price";

    private static final String PARAM_SALE_ITEM_GUID = "PARAM_SALE_ITEM_GUID";

    private String saleItemId;
    private BigDecimal price;
    private SaleOrderItemModel model;

    @Override
    protected TaskResult doCommand() {
        saleItemId = getStringArg(ARG_SALE_ITEM_GUID);
        price = (BigDecimal) getArgs().getSerializable(ARG_PRICE);

        model = new SaleOrderItemModel(saleItemId);
        model.price = price;

        return succeeded().add(PARAM_SALE_ITEM_GUID, saleItemId);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemId})
                .withValue(SaleItemTable.PRICE, _decimal(price))
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderItemJdbcConverter converter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(model);
        return converter.updatePrice(model, getAppCommandContext());
    }

    public static void start(Context context, String saleItemGuid, BigDecimal price, BaseUpdatePriceSaleOrderItemCallback callback) {
        create(UpdatePriceSaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .arg(ARG_PRICE, price)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseUpdatePriceSaleOrderItemCallback {

        @OnSuccess(UpdatePriceSaleOrderItemCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String saleItemGuid) {
            onSuccess(saleItemGuid);
        }

        protected abstract void onSuccess(String saleItemGuid);

    }
}
