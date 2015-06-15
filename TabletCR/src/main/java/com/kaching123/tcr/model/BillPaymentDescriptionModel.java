package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;

import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;

/**
 * Created by gdubina on 06/02/14.
 */
public class BillPaymentDescriptionModel implements IValueModel{

    public String guid;
    public String description;
    public PrepaidType type;
    public boolean isVoided;
    public long orderId;
    public boolean isFailed;
    public String saleOrderId;

    public BillPaymentDescriptionModel(String guid) {
        this.guid = guid;
    }

    public BillPaymentDescriptionModel(String guid, String description, PrepaidType type, long orderId, String saleOrderId) {
        this(guid, description, type, orderId, false, false, saleOrderId);
    }

    public BillPaymentDescriptionModel(String guid, String description, PrepaidType type, long orderId, boolean isVoided, boolean isFailed, String saleOrderId) {
        this.guid = guid;
        this.description = description;
        this.type = type;
        this.orderId = orderId;
        this.isVoided = isVoided;
        this.isFailed = isFailed;
        this.saleOrderId = saleOrderId;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(BillPaymentDescriptionTable.GUID, guid);
        values.put(BillPaymentDescriptionTable.DESCRIPTION, description);
        _putEnum(values, BillPaymentDescriptionTable.TYPE, type);
        values.put(BillPaymentDescriptionTable.IS_VOIDED, isVoided);
        values.put(BillPaymentDescriptionTable.PREPAID_ORDER_ID, orderId);
        values.put(BillPaymentDescriptionTable.IS_FAILED, isFailed);
        values.put(BillPaymentDescriptionTable.ORDER_ID, saleOrderId);
        return values;
    }

    public enum PrepaidType {
        WIRELESS_TOPUP, WIRELESS_PIN, BILL_PAYMENT, SUNPASS, SUNPASS_TRANSPONDER, SUNPASS_PAY_YOUR_DOCUMENT
    }
}
