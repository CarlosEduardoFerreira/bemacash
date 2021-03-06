package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.DiscountType;
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

/**
 * Created by gdubina on 15/11/13.
 */
public class DiscountSaleOrderItemCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);

    private static final String ARG_SALE_ITEM_GUID = "arg_sale_item_guid";
    private static final String ARG_DISCOUNT = "arg_discount";
    private static final String ARG_DISCOUNT_TYPE = "arg_discount_type";
    private static final String ARG_DISCOUNT_BUNDLE_ID = "ARG_DISCOUNT_BUNDLE_ID";

    private static final String PARAM_SALE_ITEM_GUID = "param_sale_item_guid";

    private String saleItemId;
    private BigDecimal discount;
    private DiscountType discountType;
    private String discountBundleId;

    @Override
    protected TaskResult doCommand() {
        saleItemId = getStringArg(ARG_SALE_ITEM_GUID);
        discount = (BigDecimal) getArgs().getSerializable(ARG_DISCOUNT);
        discountType = (DiscountType) getArgs().getSerializable(ARG_DISCOUNT_TYPE);
        discountBundleId = getStringArg(ARG_DISCOUNT_BUNDLE_ID);

        return succeeded().add(PARAM_SALE_ITEM_GUID, saleItemId);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemId})
                .withValue(SaleItemTable.DISCOUNT, _decimal(discount))
                .withValue(SaleItemTable.DISCOUNT_TYPE, discountType == null ? null : discountType.ordinal())
                .withValue(SaleItemTable.DISCOUNT_BUNDLE_ID, discountBundleId)
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderItemJdbcConverter converter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(SaleOrderItemModel.class);
        return converter.updateDiscount(saleItemId, discount, discountType);
    }

    public static void start(Context context, String saleItemGuid, BigDecimal discount, DiscountType discountType, String discountBundleId, BaseDiscountSaleOrderItemCallback callback) {
        create(DiscountSaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .arg(ARG_DISCOUNT, discount)
                .arg(ARG_DISCOUNT_TYPE, discountType)
                .arg(ARG_DISCOUNT_BUNDLE_ID, discountBundleId)
                .callback(callback)
                .queueUsing(context);
    }

    public SyncResult syncDependent(Context context, String saleItemGuid, BigDecimal discount, DiscountType discountType, String discountBundleId, IAppCommandContext appCommandContext){
        Bundle args = new Bundle();
        args.putString(ARG_SALE_ITEM_GUID, saleItemGuid);
        args.putSerializable(ARG_DISCOUNT, discount);
        args.putSerializable(ARG_DISCOUNT_TYPE, discountType);
        args.putString(ARG_DISCOUNT_BUNDLE_ID, discountBundleId);
        return syncDependent(context, args, appCommandContext);
    }

    public static abstract class BaseDiscountSaleOrderItemCallback {

        @OnSuccess(DiscountSaleOrderItemCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String saleItemGuid) {
            onSuccess(saleItemGuid);
        }

        protected abstract void onSuccess(String saleItemGuid);

    }
}
