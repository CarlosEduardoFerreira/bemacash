package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._orderType;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 * create new one or follow up existent one
 */
public class AddReturnOrderCommand extends AsyncCommand {

    public static final String ARG_ORDER_MODEL_CHILD = "arg_child_guid";

    public static final String RESULT_CHILD_ORDER_MODEL = "result_child_obj";

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private SaleOrderModel orderModel;
    private String parentGuid;


    @Override
    protected TaskResult doCommand() {

        Integer seq = _wrap(ProviderAction
                .query(URI_ORDER)
                .projection("max(" + SaleOrderTable.PRINT_SEQ_NUM + ")")
                .where(SaleOrderTable.REGISTER_ID + " = ?", getAppCommandContext().getRegisterId())
                .perform(getContext()), new Function<Cursor, Integer>() {
            @Override
            public Integer apply(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0) + 1;
                }
                return 1;
            }
        });

        OrderType type = OrderType.SALE;
        String customerGuid = null;
        if (!TextUtils.isEmpty(parentGuid)) {
            Cursor c = ProviderAction.query(URI_ORDER)
                    .projection(SaleOrderTable.ORDER_TYPE, SaleOrderTable.CUSTOMER_GUID)
                    .where(SaleOrderTable.GUID + " = ?", parentGuid)
                    .perform(getContext());
            if (c.moveToFirst()) {
                type = _orderType(c, 0);
                customerGuid = c.getString(1);
            }
            c.close();
        }

        orderModel =  new SaleOrderModel(UUID.randomUUID().toString(),
                new Date(),
                getAppCommandContext().getEmployeeGuid(),
                getAppCommandContext().getShiftGuid(),
                customerGuid,
                BigDecimal.ZERO,
                DiscountType.VALUE,
                OrderStatus.RETURN,
                null,
                null,
                null,
                null,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                seq,
                getAppCommandContext().getRegisterId(),
                parentGuid,
                type,
                KitchenPrintStatus.PRINT,
                PrintOrderToKdsCommand.KDSSendStatus.PRINT,
                false,
                BigDecimal.ZERO
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT))
                .withValues(orderModel.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(orderModel).insertSQL(orderModel, getAppCommandContext());
    }

    public AddReturnOrderResult sync(Context context, Bundle args, String parentGuid, IAppCommandContext appCommandContext) {
        this.orderModel = args == null ? null : (SaleOrderModel) args.getSerializable(ARG_ORDER_MODEL_CHILD);
        this.parentGuid = parentGuid;

        if (orderModel != null) {
            return new AddReturnOrderResult(orderModel);
        }

        SyncResult result = syncDependent(context, appCommandContext);
        return new AddReturnOrderResult(result, this.orderModel);
    }

    public static class AddReturnOrderResult extends SyncResult {

        private boolean isSuccessful;
        private SaleOrderModel orderModel;
        private boolean orderWasCreatedPreviously;


        public AddReturnOrderResult(SaleOrderModel orderModel) {
            this.orderModel = orderModel;
            this.isSuccessful = true;
            this.orderWasCreatedPreviously = true;
        }

        public AddReturnOrderResult(SyncResult result, SaleOrderModel orderModel) {
            super(result == null ? null : result.getSqlCmd(), result == null ? null : result.getLocalDbOperations());
            this.orderModel = result == null ? null : orderModel;
            this.isSuccessful = result != null;
        }

        public boolean isSuccessful() {
            return isSuccessful;
        }

        public SaleOrderModel getOrderModel() {
            return orderModel;
        }

        public boolean isOrderWasCreatedPreviously() {
            return orderWasCreatedPreviously;
        }

    }
}
