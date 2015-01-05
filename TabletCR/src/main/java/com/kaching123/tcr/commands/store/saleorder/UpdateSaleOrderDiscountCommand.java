package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 15/11/13.
 */
public class UpdateSaleOrderDiscountCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private static final String ARG_SALE_ORDER_GUID = "arg_sale_order_guid";
    private static final String ARG_DISCOUNT = "arg_discount";
    private static final String ARG_DISCOUNT_TYPE = "arg_discount_type";

    private String saleOrderId;
    private BigDecimal discount;
    private DiscountType discountType;

    @Override
    protected TaskResult doCommand() {
        saleOrderId = getStringArg(ARG_SALE_ORDER_GUID);
        discount = (BigDecimal) getArgs().getSerializable(ARG_DISCOUNT);
        discountType = (DiscountType) getArgs().getSerializable(ARG_DISCOUNT_TYPE);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withSelection(SaleOrderTable.GUID + " = ?", new String[]{saleOrderId})
                .withValue(SaleOrderTable.DISCOUNT, _decimal(discount))
                .withValue(SaleOrderTable.DISCOUNT_TYPE, discountType == null ? null : _enum(discountType))
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderModel model = new SaleOrderModel(saleOrderId);
        model.discount = discount;
        model.discountType = discountType;

        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(model);
        return converter.updateDiscount(model, getAppCommandContext());
    }

    public static void start(Context context, String saleOrderGuid, BigDecimal discount, DiscountType discountType) {
        create(UpdateSaleOrderDiscountCommand.class)
                .arg(ARG_SALE_ORDER_GUID, saleOrderGuid)
                .arg(ARG_DISCOUNT, discount)
                .arg(ARG_DISCOUNT_TYPE, discountType)
                .queueUsing(context);
    }

}
