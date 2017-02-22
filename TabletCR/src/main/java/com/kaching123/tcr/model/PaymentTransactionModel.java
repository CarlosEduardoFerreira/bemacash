package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.ITransaction;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.BlackStoneTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.cash.CashTransaction;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;

import junit.framework.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentType;
import static com.kaching123.tcr.util.ContentValuesUtilBase._bool;
import static com.kaching123.tcr.util.ContentValuesUtilBase._date;

/**
 * Created by gdubina on 20/11/13.
 */
public class PaymentTransactionModel implements IValueModel, Serializable {

    public String guid;
    public String orderGuid;
    public String parentTransactionGuid;
    public BigDecimal amount;
    public PaymentType paymentType = PaymentType.SALE;
    public PaymentStatus status = PaymentStatus.IN_PROGRESS;
    public String operatorId;
    public PaymentGateway gateway;
    public String paymentId;
    public String preauthPaymentId;
    public String declineReason;
    public Date createTime;
    public String shiftGuid;
    public String cardName;
    public BigDecimal changeAmount;
    public String lastFour;
    public BigDecimal availableAmount;
    public String authorizationNumber;
    public boolean isPreauth;
    public String closedPerauthGuid;
    public BigDecimal balance;
    public BigDecimal cashBack;
    public String applicationIdentifier;
    public String resultCode;
    public String entryMethod;
    public String applicationCryptogramType;
    public String customerName;
    public String paxDigitalSignature = null;

    public boolean allowReload; // tmp var

    private List<String> mIgnoreFields;

    public PaymentTransactionModel() {
    }


    public PaymentTransactionModel(Cursor c) {
        this.guid = c.getString(c.getColumnIndex(PaymentTransactionTable.GUID));
        this.parentTransactionGuid = c.getString(c.getColumnIndex(PaymentTransactionTable.PARENT_GUID));
        this.orderGuid = c.getString(c.getColumnIndex(PaymentTransactionTable.ORDER_GUID));
        this.amount = new BigDecimal(c.getColumnIndex(PaymentTransactionTable.AMOUNT));
        this.paymentType = _paymentType(c, c.getColumnIndex(PaymentTransactionTable.TYPE));
        this.status = _paymentStatus(c, c.getColumnIndex(PaymentTransactionTable.STATUS));
        this.operatorId = c.getString(c.getColumnIndex(PaymentTransactionTable.OPERATOR_GUID));
        this.gateway = _paymentGateway(c, c.getColumnIndex(PaymentTransactionTable.GATEWAY));
        this.paymentId = c.getString(c.getColumnIndex(PaymentTransactionTable.GATEWAY_PAYMENT_ID));
        this.preauthPaymentId = c.getString(c.getColumnIndex(PaymentTransactionTable.GATEWAY_PREAUTH_PAYMENT_ID));
        this.closedPerauthGuid = c.getString(c.getColumnIndex(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID));
        this.declineReason = c.getString(c.getColumnIndex(PaymentTransactionTable.DECLINE_REASON));
        this.createTime = _date(c.getString(c.getColumnIndex(PaymentTransactionTable.CREATE_TIME)));
        this.shiftGuid = c.getString(c.getColumnIndex(PaymentTransactionTable.SHIFT_GUID));
        this.cardName = c.getString(c.getColumnIndex(PaymentTransactionTable.CARD_NAME));
        this.changeAmount =  new BigDecimal(c.getColumnIndex(PaymentTransactionTable.CHANGE_AMOUNT));
        this.isPreauth = _bool(c, c.getColumnIndex(PaymentTransactionTable.IS_PREAUTH));
        this.cashBack =  new BigDecimal(c.getColumnIndex(PaymentTransactionTable.CASH_BACK));
        this.balance =  new BigDecimal(c.getColumnIndex(PaymentTransactionTable.BALANCE));

        this.lastFour = c.getString(c.getColumnIndex(PaymentTransactionTable.LAST_FOUR));
        this.entryMethod = c.getString(c.getColumnIndex(PaymentTransactionTable.ENTRY_METHOD));
        this.applicationIdentifier = c.getString(c.getColumnIndex(PaymentTransactionTable.APPLICATION_IDENTIFIER));
        this.applicationCryptogramType = c.getString(c.getColumnIndex(PaymentTransactionTable.APPLICATION_CRYPTOGRAM_TYPE));
        this.authorizationNumber = c.getString(c.getColumnIndex(PaymentTransactionTable.AUTHORIZATION_NUMBER));
        this.paxDigitalSignature = c.getString(c.getColumnIndex(PaymentTransactionTable.SIGNATURE_BYTES));

    }

    public PaymentTransactionModel(String guid,
                                   String parentTransactionGuid,
                                   String orderGuid,
                                   BigDecimal amount,
                                   PaymentType paymentType,
                                   PaymentStatus status,
                                   String operatorId,
                                   PaymentGateway gateway,
                                   String paymentId,
                                   String preauthPaymentId,
                                   String closedPerauthGuid,
                                   String declineReason,
                                   Date createTime,
                                   String shiftGuid,
                                   String cardName,
                                   BigDecimal changeAmount,
                                   boolean isPreauth,
                                   BigDecimal cashBack,
                                   BigDecimal ebtBalance,
                                   String lastFour,
                                   String entryMethod,
                                   String applicationIdentifier,
                                   String applicationCryptogramType,
                                   String authorizationNumber,
                                   String paxDigitalSignature,
                                   List<String> ignoreFields) {
        this.guid = guid;
        this.orderGuid = orderGuid;
        this.amount = amount;
        this.status = status;

        this.gateway = gateway;
        this.paymentId = paymentId;
        this.preauthPaymentId = preauthPaymentId;
        this.closedPerauthGuid = closedPerauthGuid;
        this.declineReason = declineReason;

        this.paymentType = paymentType;
        this.parentTransactionGuid = parentTransactionGuid;
        this.operatorId = operatorId;
        this.createTime = createTime == null ? new Date() : createTime;
        this.shiftGuid = shiftGuid;
        this.cardName = cardName;
        this.changeAmount = changeAmount;
        this.availableAmount = BigDecimal.ZERO;
        this.isPreauth = isPreauth;
        this.cashBack = cashBack;
        this.balance = ebtBalance;

        this.lastFour = lastFour;
        this.entryMethod = entryMethod;
        this.applicationIdentifier = applicationIdentifier;
        this.applicationCryptogramType = applicationCryptogramType;
        this.authorizationNumber = authorizationNumber;
        this.paxDigitalSignature = paxDigitalSignature;

        this.mIgnoreFields = ignoreFields;
    }

    public PaymentTransactionModel(String guid, String shiftGuid, Date createTime, ITransaction transaction) {
        this.guid = guid;
        this.orderGuid = transaction.getOrderGuid();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();
        this.gateway = transaction.getGateway();
        this.paymentId = transaction.getPaymentId();
        this.closedPerauthGuid = transaction.getGuid();
        this.declineReason = transaction.getDeclineReason();
        this.parentTransactionGuid = transaction.getParentTransactionGuid();
        this.paymentType = transaction.getPaymentType();
        this.operatorId = transaction.getOperatorId();
        this.createTime = createTime;
        this.shiftGuid = shiftGuid;
        this.cardName = transaction.getCardName();
        this.changeAmount = transaction.getChangeAmount();
        this.availableAmount = transaction.getAvailableAmount();
        this.isPreauth = transaction.getIsPreauth();
        this.cashBack = transaction.getCashBack();
        this.balance = transaction.getBalance();
        this.resultCode = transaction.getResultCode();
        this.customerName = transaction.getCustomerName();

        this.lastFour = transaction.getLastFour();
        this.entryMethod = transaction.getEntryMethod();
        this.applicationIdentifier = transaction.getApplicationIdentifier();
        this.applicationCryptogramType = transaction.getApplicationCryptogramType();
        this.authorizationNumber = transaction.getAuthorizationNumber();
        this.paxDigitalSignature = transaction.getPaxDigitalSignature();
    }

    public PaymentTransactionModel(String shiftGuid, ITransaction transaction) {
        this.guid = transaction.getGuid();
        this.orderGuid = transaction.getOrderGuid();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();
        this.gateway = transaction.getGateway();
        this.isPreauth = transaction.getIsPreauth();
        this.paymentType = transaction.getPaymentType();
        if (isPreauth && PaymentType.SALE.equals(paymentType)) {
            this.preauthPaymentId = transaction.getPaymentId();
        }else {
            this.paymentId = transaction.getPaymentId();
        }
        this.declineReason = transaction.getDeclineReason();
        this.parentTransactionGuid = transaction.getParentTransactionGuid();
        this.operatorId = transaction.getOperatorId();
        //TODO fix it
        this.createTime = new Date();
        this.shiftGuid = shiftGuid;
        this.cardName = transaction.getCardName();
        this.changeAmount = transaction.getChangeAmount();
        this.availableAmount = transaction.getAvailableAmount();
        this.cashBack = transaction.getCashBack();
        this.balance = transaction.getBalance();
        this.resultCode = transaction.getResultCode();
        this.customerName = transaction.getCustomerName();

        this.lastFour = transaction.getLastFour();
        this.entryMethod = transaction.getEntryMethod();
        this.applicationIdentifier = transaction.getApplicationIdentifier();
        this.applicationCryptogramType = transaction.getApplicationCryptogramType();
        this.authorizationNumber = transaction.getAuthorizationNumber();
        this.paxDigitalSignature = transaction.getPaxDigitalSignature();
    }

    public Transaction toTransaction() {
        Transaction result;
        if (PaymentGateway.BLACKSTONE.equals(gateway)) {
            result = new BlackStoneTransaction(this);
        } else if (PaymentGateway.PAX.equals(gateway)
                || PaymentGateway.PAX_DEBIT.equals(gateway)
                || PaymentGateway.PAX_EBT_CASH.equals(gateway)
                || PaymentGateway.PAX_EBT_FOODSTAMP.equals(gateway)
                || PaymentGateway.PAX_GIFT_CARD.equals(gateway)) {
            result = new PaxTransaction(this).setGateway(gateway);
        } else {
            Assert.assertNotNull(amount);
            result = new CashTransaction(guid, amount);
        }
        result.userTransactionNumber = guid;
        result.orderId = orderGuid;
        result.amount = amount;
        result.type = TransactionType.valueOf(gateway);
        result.serviceTransactionNumber = (TextUtils.isEmpty(preauthPaymentId) ? paymentId : preauthPaymentId);
        result.parentTransactionGuid = parentTransactionGuid;
        result.paymentType = paymentType;
        result.operatorId = operatorId;
        result.code = TransactionStatusCode.UNKNOWN;
        result.cardName = cardName;
        result.availableValue = availableAmount;
        result.isPreauth = isPreauth;
        result.balance = balance;
        result.allowReload = allowReload;
        result.cashBack = cashBack;
        result.balance = balance;
        result.customerName = customerName;
        result.resultCode = resultCode;

        result.lastFour = lastFour;
        result.entryMethod = entryMethod;
        result.applicationIdentifier = applicationIdentifier;
        result.applicationCryptogramType = applicationCryptogramType;
        result.authorizationNumber = authorizationNumber;
        result.paxDigitalSignature = paxDigitalSignature;

        return result;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder("resulting data : ");
        sb.append("\nguid : ").append(guid)
                .append("\norderGuid : ").append(orderGuid)
                .append("\nbiller : ").append(amount)
                .append("\nstatus : ").append(status)
                .append("\ngateway : ").append(gateway)
                .append("\npaymentId : ").append(paymentId)
                .append("\npreauthPaymentId : ").append(preauthPaymentId)
                .append("\nclosedPerauthGuid : ").append(closedPerauthGuid)
                .append("\nauthorizationNumber : ").append(authorizationNumber)
                .append("\nparentTransactionGuid : ").append(parentTransactionGuid)
                .append("\npaymentType : ").append(paymentType)
                .append("\ndeclineReason : ").append(declineReason)
                .append("\ncardName : ").append(cardName)
                .append("\nisPreauth : ").append(isPreauth)
                .append("\ncashBack : ").append(cashBack)
                .append("\nbalance : ").append(balance)
                .append("\napplicationIdentifier : ").append(applicationIdentifier)
                .append("\nentryMethod : ").append(entryMethod)
                .append("\nresultCode : ").append(resultCode)
                .append("\napplicationCryptogramType : ").append(applicationCryptogramType)
                .append("\ncustomerName : ").append(customerName);
        return sb.toString();
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.GUID)) v.put(PaymentTransactionTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.ORDER_GUID)) v.put(PaymentTransactionTable.ORDER_GUID, orderGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.PARENT_GUID)) v.put(PaymentTransactionTable.PARENT_GUID, parentTransactionGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.AMOUNT)) v.put(PaymentTransactionTable.AMOUNT, _decimal(amount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.TYPE)) v.put(PaymentTransactionTable.TYPE, _enum(paymentType));
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.STATUS)) v.put(PaymentTransactionTable.STATUS, _enum(status));
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.GATEWAY)) v.put(PaymentTransactionTable.GATEWAY, _enum(gateway == null ? PaymentGateway.CASH : gateway));
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.GATEWAY_PAYMENT_ID)) v.put(PaymentTransactionTable.GATEWAY_PAYMENT_ID, paymentId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.GATEWAY_PREAUTH_PAYMENT_ID)) v.put(PaymentTransactionTable.GATEWAY_PREAUTH_PAYMENT_ID, preauthPaymentId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID)) v.put(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID, closedPerauthGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.DECLINE_REASON)) v.put(PaymentTransactionTable.DECLINE_REASON, declineReason);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.OPERATOR_GUID)) v.put(PaymentTransactionTable.OPERATOR_GUID, operatorId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.CREATE_TIME)) v.put(PaymentTransactionTable.CREATE_TIME, createTime.getTime());
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.SHIFT_GUID)) v.put(PaymentTransactionTable.SHIFT_GUID, shiftGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.CARD_NAME)) v.put(PaymentTransactionTable.CARD_NAME, cardName);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.CHANGE_AMOUNT)) v.put(PaymentTransactionTable.CHANGE_AMOUNT, _decimal(changeAmount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.IS_PREAUTH)) v.put(PaymentTransactionTable.IS_PREAUTH, isPreauth);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.CASH_BACK)) v.put(PaymentTransactionTable.CASH_BACK, _decimal(cashBack));
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.BALANCE)) v.put(PaymentTransactionTable.BALANCE, _decimal(balance));

        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.LAST_FOUR)) v.put(PaymentTransactionTable.LAST_FOUR, lastFour);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.ENTRY_METHOD)) v.put(PaymentTransactionTable.ENTRY_METHOD, entryMethod);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.APPLICATION_IDENTIFIER)) v.put(PaymentTransactionTable.APPLICATION_IDENTIFIER, applicationIdentifier);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.APPLICATION_CRYPTOGRAM_TYPE)) v.put(PaymentTransactionTable.APPLICATION_CRYPTOGRAM_TYPE, applicationCryptogramType);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.AUTHORIZATION_NUMBER)) v.put(PaymentTransactionTable.AUTHORIZATION_NUMBER, authorizationNumber);
        if (mIgnoreFields == null || !mIgnoreFields.contains(PaymentTransactionTable.SIGNATURE_BYTES)) v.put(PaymentTransactionTable.SIGNATURE_BYTES, paxDigitalSignature);
        return v;
    }

    @Override
    public String getIdColumn() {
        return PaymentTransactionTable.GUID;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        v.put(PaymentTransactionTable.STATUS, _enum(status));
        v.put(PaymentTransactionTable.AMOUNT, _decimal(amount));
        v.put(PaymentTransactionTable.GATEWAY_PAYMENT_ID, paymentId);
        v.put(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID, closedPerauthGuid);
        v.put(PaymentTransactionTable.DECLINE_REASON, declineReason);
        return v;
    }

    public ContentValues getUpdatePaymentStatus() {
        ContentValues v = new ContentValues(1);
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        v.put(PaymentTransactionTable.STATUS, _enum(status));
        return v;
    }

    public enum PaymentStatus {
        SUCCESS, FAILED, IN_PROGRESS, CANCELED, PRE_AUTHORIZED;

        public static PaymentStatus valueOf(boolean passed) {
            return passed ? SUCCESS : FAILED;
        }

        public boolean isSuccessful() {
            return this == PRE_AUTHORIZED || this == SUCCESS;
        }
    }

    public enum PaymentType {
        SALE, VOID, REFUND;

        public static PaymentType valueOf(int id) {
            return values()[id];
        }
    }

}
