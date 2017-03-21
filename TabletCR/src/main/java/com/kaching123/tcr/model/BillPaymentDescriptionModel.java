package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;

import java.io.Serializable;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;

/**
 * Created by gdubina on 06/02/14.
 */
public class BillPaymentDescriptionModel implements Serializable, IValueModel{

    public String guid;
    public String description;
    public PrepaidType type;
    public boolean isVoided;
    public long orderId;
    public boolean isFailed;
    public String saleOrderId;

    private List<String> mIgnoreFields;

    public BillPaymentDescriptionModel(String guid) {
        this.guid = guid;
    }

    public BillPaymentDescriptionModel(String guid, String description, PrepaidType type, long orderId, String saleOrderId) {
        this(guid, description, type, orderId, false, false, saleOrderId, null);
    }

    public BillPaymentDescriptionModel(String guid, String description, PrepaidType type, long orderId,
                                       boolean isVoided, boolean isFailed, String saleOrderId, List<String> ignoreFields) {
        this.guid = guid;
        this.description = description;
        this.type = type;
        this.orderId = orderId;
        this.isVoided = isVoided;
        this.isFailed = isFailed;
        this.saleOrderId = saleOrderId;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.GUID)) values.put(BillPaymentDescriptionTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.DESCRIPTION)) values.put(BillPaymentDescriptionTable.DESCRIPTION, description);
        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.TYPE)) _putEnum(values, BillPaymentDescriptionTable.TYPE, type);
        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.IS_VOIDED)) values.put(BillPaymentDescriptionTable.IS_VOIDED, isVoided);
        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.PREPAID_ORDER_ID)) values.put(BillPaymentDescriptionTable.PREPAID_ORDER_ID, orderId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.IS_FAILED)) values.put(BillPaymentDescriptionTable.IS_FAILED, isFailed);
        if (mIgnoreFields == null || !mIgnoreFields.contains(BillPaymentDescriptionTable.ORDER_ID)) values.put(BillPaymentDescriptionTable.ORDER_ID, saleOrderId);
        return values;
    }

    @Override
    public String getIdColumn() {
        return BillPaymentDescriptionTable.GUID;
    }

    public enum PrepaidType {
        WIRELESS_TOPUP, WIRELESS_PIN, BILL_PAYMENT, SUNPASS, SUNPASS_TRANSPONDER, SUNPASS_PAY_YOUR_DOCUMENT, GIFT_CARD_RELOAD
    }
}
