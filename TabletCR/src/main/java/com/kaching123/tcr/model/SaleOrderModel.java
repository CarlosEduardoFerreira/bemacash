package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.AsyncTaskCommand;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand.KDSSendStatus;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.model.converter.OrderCheckoutState;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.util.DateUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._kdsSendStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._kitchenPrintStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._onHoldStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;
import static com.kaching123.tcr.model.ContentValuesUtil._putDiscount;

public class SaleOrderModel implements Serializable, IValueModel {

    private static final long serialVersionUID = 1L;

    private static final Uri URI_ORDER = ShopProvider.contentUri(SaleOrderTable.URI_CONTENT);

    public String guid;
    public Date createTime;
    public String operatorGuid;
    public String shiftGuid;
    public String customerGuid;

    public BigDecimal discount;
    public DiscountType discountType;
    public OrderStatus orderStatus = OrderStatus.ACTIVE;

    private String holdName;
    public String definedOnHoldGuid;
    public String holdPhone;
    public OnHoldStatus holdStatus = OnHoldStatus.NONE;

    public boolean taxable;

    public int printSeqNum;
    public long registerId;
    public String parentGuid;

    public OrderType type = OrderType.SALE;

    public BigDecimal tmpTotalPrice;
    public BigDecimal tmpTotalTax;
    public BigDecimal tmpTotalDiscount;

    public boolean isTipped;
    public KitchenPrintStatus kitchenPrintStatus;
    public KDSSendStatus kdsSendStatus;

    public BigDecimal transactionFee;

    private List<String> mIgnoreFields;

    public SaleOrderModel(String guid) {
        this.guid = guid;
    }

    public static SaleOrderModel fromView(Cursor c){
       return new SaleOrderModel(
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.GUID)),
                new Date(c.getLong(c.getColumnIndex(SaleOrderView2.SaleOrderTable.CREATE_TIME))),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.OPERATOR_GUID)),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.SHIFT_GUID)),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.CUSTOMER_GUID)),
                _decimal(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.DISCOUNT), BigDecimal.ZERO),
                _discountType(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.DISCOUNT_TYPE)),
                _orderStatus(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.STATUS)),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.HOLD_NAME)),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.DEFINED_ON_HOLD_ID)),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.HOLD_TEL)),
                _onHoldStatus(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.HOLD_STATUS)),
                _bool(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.TAXABLE)),
                _decimal(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.TML_TOTAL_PRICE), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.TML_TOTAL_TAX), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.TML_TOTAL_DISCOUNT), BigDecimal.ZERO),
                c.getInt(c.getColumnIndex(SaleOrderView2.SaleOrderTable.PRINT_SEQ_NUM)),
                c.getLong(c.getColumnIndex(SaleOrderView2.SaleOrderTable.REGISTER_ID)),
                c.getString(c.getColumnIndex(SaleOrderView2.SaleOrderTable.PARENT_ID)),
                _orderType(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.ORDER_TYPE)),
                _bool(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.IS_TIPPED)),
                _kitchenPrintStatus(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.KITCHEN_PRINT_STATUS)),
                _kdsSendStatus(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.KDS_SEND_STATUS)),
                _decimal(c, c.getColumnIndex(SaleOrderView2.SaleOrderTable.TRANSACTION_FEE), BigDecimal.ZERO),
                null
        );
    }

    public SaleOrderModel(Cursor c) {
        this(c.getString(c.getColumnIndex(ShopStore.SaleOrderTable.GUID)),
                new Date(c.getLong(c.getColumnIndex(ShopStore.SaleOrderTable.CREATE_TIME))),
                c.getString(c.getColumnIndex(ShopStore.SaleOrderTable.OPERATOR_GUID)),
                c.getString(c.getColumnIndex(ShopStore.SaleOrderTable.SHIFT_GUID)),
                c.getString(c.getColumnIndex(SaleOrderTable.CUSTOMER_GUID)),
                _decimal(c, c.getColumnIndex(ShopStore.SaleOrderTable.DISCOUNT), BigDecimal.ZERO),
                _discountType(c, c.getColumnIndex(ShopStore.SaleOrderTable.DISCOUNT_TYPE)),
                _orderStatus(c, c.getColumnIndex(ShopStore.SaleOrderTable.STATUS)),
                c.getString(c.getColumnIndex(SaleOrderTable.HOLD_NAME)),
                c.getString(c.getColumnIndex(SaleOrderTable.DEFINED_ON_HOLD_ID)),
                c.getString(c.getColumnIndex(SaleOrderTable.HOLD_TEL)),
                _onHoldStatus(c, c.getColumnIndex(SaleOrderTable.HOLD_STATUS)),
                _bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                _decimal(c, c.getColumnIndex(SaleOrderTable.TML_TOTAL_PRICE), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleOrderTable.TML_TOTAL_TAX), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleOrderTable.TML_TOTAL_DISCOUNT), BigDecimal.ZERO),
                c.getInt(c.getColumnIndex(SaleOrderTable.PRINT_SEQ_NUM)),
                c.getLong(c.getColumnIndex(SaleOrderTable.REGISTER_ID)),
                c.getString(c.getColumnIndex(SaleOrderTable.PARENT_ID)),
                _orderType(c, c.getColumnIndex(SaleOrderTable.ORDER_TYPE)),
                _bool(c, c.getColumnIndex(SaleOrderTable.IS_TIPPED)),
                _kitchenPrintStatus(c, c.getColumnIndex(SaleOrderTable.KITCHEN_PRINT_STATUS)),
                _kdsSendStatus(c, c.getColumnIndex(SaleOrderTable.KDS_SEND_STATUS)),
                _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE), BigDecimal.ZERO),
                null
        );
    }

    public SaleOrderModel(
            String guid,
            Date createTime,
            String operatorGuid,
            String shiftGuid,
            String customerGuid,
            BigDecimal discount,
            DiscountType discountType,
            OrderStatus orderStatus,
            String holdName,
            String definedOnHoldGuid,
            String holdPhone,
            OnHoldStatus holdStatus,
            boolean taxable,
            BigDecimal tmpTotalPrice,
            BigDecimal tmpTotalTax,
            BigDecimal tmpTotalDiscount,
            int printSeqNum,
            long registerId,
            String parentGuid,
            OrderType type,
            KitchenPrintStatus kitchenPrintStatus,
            KDSSendStatus kdsSendStatus,
            BigDecimal transactionFee) {
        this(guid, createTime, operatorGuid, shiftGuid, customerGuid, discount, discountType, orderStatus, holdName, definedOnHoldGuid, holdPhone, holdStatus, taxable, tmpTotalPrice, tmpTotalTax, tmpTotalDiscount, printSeqNum, registerId, parentGuid, type, false, kitchenPrintStatus, kdsSendStatus, transactionFee, null);
    }

    public SaleOrderModel(
            String guid,
            Date createTime,
            String operatorGuid,
            String shiftGuid,
            String customerGuid,
            BigDecimal discount,
            DiscountType discountType,
            OrderStatus orderStatus,
            String holdName,
            String definedOnHoldGuid,
            String holdPhone,
            OnHoldStatus holdStatus,
            boolean taxable,
            BigDecimal tmpTotalPrice,
            BigDecimal tmpTotalTax,
            BigDecimal tmpTotalDiscount,
            int printSeqNum,
            long registerId,
            String parentGuid,
            OrderType type,
            boolean isTipped,
            KitchenPrintStatus kitchenPrintStatus,
            KDSSendStatus kdsSendStatus,
            BigDecimal transactionFee,
            List<String> ignoreFields) {

        super();
        this.guid = guid;
        this.createTime = createTime;
        this.operatorGuid = operatorGuid;
        this.shiftGuid = shiftGuid;
        this.customerGuid = customerGuid;

        this.discount = discount;
        this.discountType = discountType;
        this.orderStatus = orderStatus;

        setHoldName(holdName);
        this.definedOnHoldGuid = definedOnHoldGuid;
        this.holdPhone = holdPhone;

        if(holdStatus != null) {
            this.holdStatus = holdStatus;
        }

        this.taxable = taxable;

        this.printSeqNum = printSeqNum;
        this.registerId = registerId;
        this.parentGuid = parentGuid;

        this.tmpTotalPrice = tmpTotalPrice;
        this.tmpTotalTax = tmpTotalTax;
        this.tmpTotalDiscount = tmpTotalDiscount;
        this.type = type;
        this.isTipped = isTipped;
        this.kitchenPrintStatus = kitchenPrintStatus;
        this.kdsSendStatus = kdsSendStatus;
        this.transactionFee = transactionFee;
        this.mIgnoreFields = ignoreFields;
    }


    public static SaleOrderModel getById(final Context context, final String orderId) {
        if (orderId == null) return null;

        Cursor orderCursor = ProviderAction.query(URI_ORDER)
                    .where(SaleOrderTable.GUID + " = ?", orderId)
                    .perform(context);

        SaleOrderModel orderModel = null;
        if (orderCursor != null && orderCursor.moveToFirst()) {
            orderModel = new SaleOrderModel(orderCursor);
        }
        return orderModel;

    }

    public String getHoldName() {
        return holdName;
    }

    public String getHoldPhone() {
        return holdPhone;
    }

    public OnHoldStatus getHoldStatus() {
        return holdStatus;
    }

    public void setHoldName(String holdName) {
        if (TextUtils.isEmpty(holdName)) {
            this.holdName = getDefaultName(this);
        } else {
            this.holdName = holdName;
        }
    }

    public String getDefinedOnHoldGuid() {
        return definedOnHoldGuid;
    }

    public void setDefinedOnHoldGuid(String definedOnHoldGuid) {
        this.definedOnHoldGuid = definedOnHoldGuid;
    }

    public void setHoldPhone(String holdPhone) {
        this.holdPhone = holdPhone;
    }

    public void setHoldStatus(OnHoldStatus holdStatus) {
        if(holdStatus == null){
            this.holdStatus = OnHoldStatus.NONE;
            return;
        }
        this.holdStatus = holdStatus;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.GUID)) values.put(SaleOrderTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.CREATE_TIME)) values.put(SaleOrderTable.CREATE_TIME, createTime.getTime());
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.OPERATOR_GUID)) values.put(SaleOrderTable.OPERATOR_GUID, operatorGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.SHIFT_GUID)) values.put(SaleOrderTable.SHIFT_GUID, shiftGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.CUSTOMER_GUID)) values.put(SaleOrderTable.CUSTOMER_GUID, customerGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.DISCOUNT)) values.put(SaleOrderTable.DISCOUNT, _decimal(discount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.DISCOUNT_TYPE)) _putDiscount(values, SaleOrderTable.DISCOUNT_TYPE, discountType);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.STATUS)) values.put(SaleOrderTable.STATUS, orderStatus.ordinal());

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.HOLD_NAME)) values.put(SaleOrderTable.HOLD_NAME, holdName);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.DEFINED_ON_HOLD_ID)) values.put(SaleOrderTable.DEFINED_ON_HOLD_ID, definedOnHoldGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.HOLD_TEL)) values.put(SaleOrderTable.HOLD_TEL, holdPhone);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.HOLD_STATUS)) values.put(SaleOrderTable.HOLD_STATUS, holdStatus.ordinal());

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.TAXABLE)) values.put(SaleOrderTable.TAXABLE, taxable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.PRINT_SEQ_NUM)) values.put(SaleOrderTable.PRINT_SEQ_NUM, printSeqNum);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.REGISTER_ID)) values.put(SaleOrderTable.REGISTER_ID, registerId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.PARENT_ID)) values.put(SaleOrderTable.PARENT_ID, parentGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.ORDER_TYPE)) values.put(SaleOrderTable.ORDER_TYPE, type.ordinal());
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.IS_TIPPED)) values.put(SaleOrderTable.IS_TIPPED, isTipped);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.KITCHEN_PRINT_STATUS)) values.put(SaleOrderTable.KITCHEN_PRINT_STATUS, kitchenPrintStatus.ordinal());
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.KDS_SEND_STATUS)) values.put(SaleOrderTable.KDS_SEND_STATUS, kdsSendStatus.ordinal());
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleOrderTable.TRANSACTION_FEE)) values.put(SaleOrderTable.TRANSACTION_FEE, _decimal(transactionFee));

        return values;
    }

    @Override
    public String getIdColumn() {
        return SaleOrderTable.GUID;
    }

    public ContentValues toSetTippedValues() {
        ContentValues values = new ContentValues(1);
        values.put(SaleOrderTable.IS_TIPPED, true);
        return values;
    }

    public static String getDefaultName(SaleOrderModel model) {
        return DateUtils.format(model.createTime);
    }

    public static SaleOrderModel loadSync(Context context, String guid){
        Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleOrderTable.URI_CONTENT))
                .where(SaleOrderTable.GUID + " = ?", guid)
                .perform(context);
        SaleOrderModel result = null;
        if (c.moveToFirst()){
            result = new SaleOrderModel(c);
        }
        c.close();

        return result;
    }

}
