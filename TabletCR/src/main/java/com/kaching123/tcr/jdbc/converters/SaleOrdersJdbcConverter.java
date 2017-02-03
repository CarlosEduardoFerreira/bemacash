package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OnHoldStatus;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SaleOrdersJdbcConverter extends JdbcConverter<SaleOrderModel> {

    private static final String SALE_ORDER_TABLE_NAME = "SALE_ORDER";

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

    @Override
    public SaleOrderModel toValues(JdbcJSONObject rs) throws JSONException {
        return new SaleOrderModel(
                rs.getString(ID),
                rs.getDate(CREATE_TIME),
                rs.getString(OPERATOR_ID),
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
                rs.getLong(REGISTER_ID),
                rs.getString(PARENT_ID),
                _enum(OrderType.class, rs.getString(ORDER_TYPE), OrderType.SALE),
                rs.getBoolean(IS_TIPPED),
                _enum(KitchenPrintStatus.class, rs.getString(KITCHEN_PRINT_STATUS), KitchenPrintStatus.PRINT),
                _enum(PrintOrderToKdsCommand.KDSSendStatus.class, rs.getString(KDS_SEND_STATUS), PrintOrderToKdsCommand.KDSSendStatus.PRINT),
                rs.getBigDecimal(TRANSACTION_FEE)
        );
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
    public String getParentGuidColumn() {
        return PARENT_ID;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _insert(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(ID, order.guid)
                .add(CREATE_TIME, order.createTime)
                .add(OPERATOR_ID, order.operatorGuid)
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
                .build(JdbcFactory.getApiMethod(order));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(DISCOUNT, order.discount)
                .add(DISCOUNT_TYPE, order.discountType)
                .add(ORDER_STATUS, order.orderStatus.name())
                .add(SHIFT_ID, order.shiftGuid)
                .add(HOLD_NAME, order.getHoldName())
                .add(DEFINED_ON_HOLD_GUID, order.definedOnHoldGuid)
                .add(HOLD_PHONE, order.holdPhone)
                .add(HOLD_STATUS, order.holdStatus.ordinal())
                .add(CREATE_TIME, order.createTime)
                .add(TAXABLE, order.taxable)
                .add(PARENT_ID, order.parentGuid)
                .add(TRANSACTION_FEE, order.transactionFee)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateTransactionFee(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(TRANSACTION_FEE, order.transactionFee)
                .where(ID, order.guid)
                .build(JdbcFactory.getApiMethod(order));
    }

    public SingleSqlCommand updateTax(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
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

    public SingleSqlCommand updateDate(SaleOrderModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(CREATE_TIME, model.createTime)
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

    public SingleSqlCommand updateOnHoldStatus(SaleOrderModel order, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
                .add(HOLD_STATUS, order.holdStatus.ordinal())
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
        return _update(SALE_ORDER_TABLE_NAME, appCommandContext)
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

}
