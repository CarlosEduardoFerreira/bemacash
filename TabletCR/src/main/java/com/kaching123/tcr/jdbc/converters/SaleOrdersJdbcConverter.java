package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OnHoldStatus;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SaleOrdersJdbcConverter extends JdbcConverter<SaleOrderModel> {

    public static final String SALE_ORDER_TABLE_NAME = "SALE_ORDER";

    private static final String ID = "ID";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String OPERATOR_ID = "OPERATOR_ID";
    private static final String SHIFT_ID = "SHIFT_ID";
    private static final String DISCOUNT = "DISCOUNT";
    private static final String DISCOUNT_TYPE = "DISCOUNT_TYPE";
    private static final String ORDER_STATUS = "ORDER_STATUS";
    private static final String HOLD_STATUS = "ON_HOLD_STATUS";
    private static final String HOLD_NAME = "HOLD_NAME";
    private static final String HOLD_PHONE = "HOLD_PHONE";
    private static final String DEFINED_ON_HOLD_GUID = "DEFINED_ON_HOLD_GUID";
    private static final String TAXABLE = "TAXABLE";
    private static final String PRINT_SEQ_NUM = "PRINT_SEQ_NUM";
    private static final String REGISTER_ID = "REGISTER_ID";
    private static final String PARENT_ID = "PARENT_ID";
    private static final String CUSTOMER_ID = "CUSTOMER_ID";

    private static final String ORDER_TYPE = "ORDER_TYPE";
    private static final String IS_TIPPED = "IS_TIPPED";
    private static final String KITCHEN_PRINT_STATUS = "KITCHEN_PRINT_STATUS";
    private static final String KDS_SEND_STATUS = "KDS_SEND_STATUS";
    private static final String TRANSACTION_FEE = "TRANSACTION_FEE";

    private static final String ON_REGISTER = "ON_REGISTER";

    @Override
    public SaleOrderModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.SaleOrderTable.GUID);
        if (!rs.has(CREATE_TIME)) ignoreFields.add(ShopStore.SaleOrderTable.CREATE_TIME);
        if (!rs.has(OPERATOR_ID)) ignoreFields.add(ShopStore.SaleOrderTable.OPERATOR_GUID);
        if (!rs.has(SHIFT_ID)) ignoreFields.add(ShopStore.SaleOrderTable.SHIFT_GUID);
        if (!rs.has(CUSTOMER_ID)) ignoreFields.add(ShopStore.SaleOrderTable.CUSTOMER_GUID);
        if (!rs.has(DISCOUNT)) ignoreFields.add(ShopStore.SaleOrderTable.DISCOUNT);
        if (!rs.has(DISCOUNT_TYPE)) ignoreFields.add(ShopStore.SaleOrderTable.DISCOUNT_TYPE);
        if (!rs.has(ORDER_STATUS)) ignoreFields.add(ShopStore.SaleOrderTable.STATUS);
        if (!rs.has(HOLD_NAME)) ignoreFields.add(ShopStore.SaleOrderTable.HOLD_NAME);
        if (!rs.has(HOLD_STATUS)) ignoreFields.add(ShopStore.SaleOrderTable.HOLD_STATUS);
        if (!rs.has(HOLD_PHONE)) ignoreFields.add(ShopStore.SaleOrderTable.HOLD_TEL);
        if (!rs.has(DEFINED_ON_HOLD_GUID)) ignoreFields.add(ShopStore.SaleOrderTable.DEFINED_ON_HOLD_ID);
        if (!rs.has(TAXABLE)) ignoreFields.add(ShopStore.SaleOrderTable.TAXABLE);
        if (!rs.has(PRINT_SEQ_NUM)) ignoreFields.add(ShopStore.SaleOrderTable.PRINT_SEQ_NUM);
        if (!rs.has(REGISTER_ID)) ignoreFields.add(ShopStore.SaleOrderTable.REGISTER_ID);
        if (!rs.has(PARENT_ID)) ignoreFields.add(ShopStore.SaleOrderTable.PARENT_ID);
        if (!rs.has(ORDER_TYPE)) ignoreFields.add(ShopStore.SaleOrderTable.ORDER_TYPE);
        if (!rs.has(IS_TIPPED)) ignoreFields.add(ShopStore.SaleOrderTable.IS_TIPPED);
        if (!rs.has(KITCHEN_PRINT_STATUS)) ignoreFields.add(ShopStore.SaleOrderTable.KITCHEN_PRINT_STATUS);
        if (!rs.has(KDS_SEND_STATUS)) ignoreFields.add(ShopStore.SaleOrderTable.KDS_SEND_STATUS);
        if (!rs.has(TRANSACTION_FEE)) ignoreFields.add(ShopStore.SaleOrderTable.TRANSACTION_FEE);
        if (!rs.has(ON_REGISTER)) ignoreFields.add(ShopStore.SaleOrderTable.ON_REGISTER);

        return new SaleOrderModel(
                rs.getString(ID),
                rs.getDate(CREATE_TIME),
                rs.getString(OPERATOR_ID), // TcrApplication.get().getOperatorGuid()
                rs.getString(SHIFT_ID),
                rs.getString(CUSTOMER_ID),
                rs.getBigDecimal(DISCOUNT),
                _enum(DiscountType.class, rs.getString(DISCOUNT_TYPE), null),
                _enum(OrderStatus.class, rs.getString(ORDER_STATUS), OrderStatus.CANCELED),
                rs.getString(HOLD_NAME),
                rs.getString(DEFINED_ON_HOLD_GUID),
                rs.getString(HOLD_PHONE),
                OnHoldStatus.values()[(rs.getInt(HOLD_STATUS))],
                rs.getBoolean(TAXABLE),
                null,
                null,
                null,
                rs.getInt(PRINT_SEQ_NUM),
                rs.isNull(REGISTER_ID) || rs.get(REGISTER_ID).equals("null") ? 0L : rs.getLong(REGISTER_ID),
                rs.getString(PARENT_ID),
                _enum(OrderType.class, rs.getString(ORDER_TYPE), OrderType.SALE),
                rs.getBoolean(IS_TIPPED),
                _enum(KitchenPrintStatus.class, rs.getString(KITCHEN_PRINT_STATUS), KitchenPrintStatus.PRINT),
                _enum(PrintOrderToKdsCommand.KDSSendStatus.class, rs.getString(KDS_SEND_STATUS), PrintOrderToKdsCommand.KDSSendStatus.PRINT),
                rs.getBigDecimal(TRANSACTION_FEE),
                rs.getBoolean(ON_REGISTER),
                ignoreFields);
    }

    @Override
    public String getTableName() {
        return SALE_ORDER_TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.SaleOrderTable.GUID;
    }

    @Override
    public String getParentGuidColumn() {
        return PARENT_ID;
    }

    @Override
    public JSONObject getJSONObject(SaleOrderModel order){
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ID, order.guid)
                    .put(CREATE_TIME, order.createTime)
                    .put(OPERATOR_ID, order.operatorGuid)
                    .put(SHIFT_ID, order.shiftGuid)
                    .put(CUSTOMER_ID, order.customerGuid)
                    .put(DISCOUNT, order.discount)
                    .put(DISCOUNT_TYPE, order.discountType)
                    .put(ORDER_STATUS, order.orderStatus.name())
                    .put(HOLD_NAME, order.getHoldName())
                    .put(DEFINED_ON_HOLD_GUID, order.definedOnHoldGuid)
                    .put(HOLD_PHONE, order.holdPhone)
                    .put(HOLD_STATUS, order.holdStatus.ordinal())
                    .put(TAXABLE, order.taxable)
                    .put(PRINT_SEQ_NUM, order.printSeqNum)
                    .put(REGISTER_ID, order.registerId)
                    .put(PARENT_ID, order.parentGuid)
                    .put(ORDER_TYPE, order.type.name())
                    .put(IS_TIPPED, order.isTipped)
                    .put(KITCHEN_PRINT_STATUS, order.kitchenPrintStatus)
                    .put(KDS_SEND_STATUS, order.kdsSendStatus)
                    .put(ON_REGISTER, order.orderOnRegister)
                    .put(TRANSACTION_FEE, order.transactionFee);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _insert(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(ID, order.guid)
                .add(CREATE_TIME, order.createTime)
                .add(OPERATOR_ID, order.operatorGuid) //.add(OPERATOR_ID, TcrApplication.get().getOperatorGuid())
                .add(SHIFT_ID, order.shiftGuid)
                .add(CUSTOMER_ID, order.customerGuid)
                .add(DISCOUNT, order.discount)
                .add(DISCOUNT_TYPE, order.discountType)
                .add(ORDER_STATUS, order.orderStatus.name())
                .add(HOLD_NAME, order.getHoldName())
                .add(DEFINED_ON_HOLD_GUID, order.definedOnHoldGuid)
                .add(HOLD_PHONE, order.holdPhone)
                .add(HOLD_STATUS, order.holdStatus.ordinal())
                .add(TAXABLE, order.taxable)
                .add(PRINT_SEQ_NUM, order.printSeqNum)
                .add(REGISTER_ID, order.registerId)
                .add(PARENT_ID, order.parentGuid)
                .add(ORDER_TYPE, order.type.name())
                .add(IS_TIPPED, order.isTipped)
                .add(KITCHEN_PRINT_STATUS, order.kitchenPrintStatus)
                .add(TRANSACTION_FEE, order.transactionFee)
                .add(ON_REGISTER, order.orderOnRegister)
                .build(JdbcFactory.getApiMethod(order));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(CREATE_TIME, order.createTime)
                .add(OPERATOR_ID, order.operatorGuid) //.add(OPERATOR_ID, TcrApplication.get().getOperatorGuid())
                .add(SHIFT_ID, order.shiftGuid)
                .add(CUSTOMER_ID, order.customerGuid)
                .add(DISCOUNT, order.discount)
                .add(DISCOUNT_TYPE, order.discountType)
                .add(ORDER_STATUS, order.orderStatus.name())
                .add(HOLD_NAME, order.getHoldName())
                .add(DEFINED_ON_HOLD_GUID, order.definedOnHoldGuid)
                .add(HOLD_PHONE, order.holdPhone)
                .add(HOLD_STATUS, order.holdStatus.ordinal())
                .add(TAXABLE, order.taxable)
                .add(PRINT_SEQ_NUM, order.printSeqNum)
                .add(REGISTER_ID, order.registerId)
                .add(PARENT_ID, order.parentGuid)
                .add(ORDER_TYPE, order.type.name())
                .add(IS_TIPPED, order.isTipped)
                .add(KITCHEN_PRINT_STATUS, order.kitchenPrintStatus)
                .add(TRANSACTION_FEE, order.transactionFee)
                .add(ON_REGISTER, order.orderOnRegister)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateStatusWithWhere(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(ORDER_STATUS, order.orderStatus)
                .where(ID, order.guid)
                .where(ORDER_STATUS, OrderStatus.ACTIVE.toString())
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateTax(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return  _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(TAXABLE, order.taxable)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateDiscount(SaleOrderModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(DISCOUNT, model.discount)
                .add(DISCOUNT_TYPE, model.discountType)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateCustomer(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(CUSTOMER_ID, order.customerGuid)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateStatus(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(ORDER_STATUS, order.orderStatus)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateIsTipped(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(IS_TIPPED, order.isTipped)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateKitchenPrintStatus(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return  _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(KITCHEN_PRINT_STATUS, order.kitchenPrintStatus)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateKdsPrintStatus(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(KDS_SEND_STATUS, order.kdsSendStatus)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand deleteUpdateStatus(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(ORDER_STATUS, order.orderStatus)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateOnRegisterStatus(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(ON_REGISTER, order.orderOnRegister)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }




    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
