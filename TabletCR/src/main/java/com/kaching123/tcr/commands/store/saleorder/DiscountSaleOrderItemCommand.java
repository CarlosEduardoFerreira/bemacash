package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

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

    private static final String PARAM_SALE_ITEM_GUID = "param_sale_item_guid";

    private String saleItemId;
    private BigDecimal discount;
    private DiscountType discountType;

    @Override
    protected TaskResult doCommand() {
        saleItemId = getStringArg(ARG_SALE_ITEM_GUID);
        discount = (BigDecimal) getArgs().getSerializable(ARG_DISCOUNT);
        discountType = (DiscountType) getArgs().getSerializable(ARG_DISCOUNT_TYPE);

        return succeeded().add(PARAM_SALE_ITEM_GUID, saleItemId);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemId})
                .withValue(SaleItemTable.DISCOUNT, _decimal(discount))
                .withValue(SaleItemTable.DISCOUNT_TYPE, discountType == null ? null : discountType.ordinal())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderItemModel model = new SaleOrderItemModel(saleItemId);
        model.discount = discount;
        model.discountType = discountType;
        SaleOrderItemJdbcConverter converter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(model);
        return converter.updateDiscount(model, getAppCommandContext());
    }

    public static void start(Context context, String saleItemGuid, BigDecimal discount, DiscountType discountType, BaseDiscountSaleOrderItemCallback callback) {
        create(DiscountSaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .arg(ARG_DISCOUNT, discount)
                .arg(ARG_DISCOUNT_TYPE, discountType)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseDiscountSaleOrderItemCallback {

        @OnSuccess(DiscountSaleOrderItemCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String saleItemGuid) {
            onSuccess(saleItemGuid);
        }

        protected abstract void onSuccess(String saleItemGuid);

    }
}
