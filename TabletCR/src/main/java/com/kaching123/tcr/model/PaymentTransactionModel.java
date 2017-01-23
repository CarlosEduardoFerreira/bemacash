package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.text.TextUtils;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.ITransaction;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.BlackStoneTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.cash.CashTransaction;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;

import junit.framework.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

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
    public byte[] paxDigitalSignature = null;


    public boolean allowReload; // tmp var


    public PaymentTransactionModel() {
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
                                   BigDecimal ebtBalance) {
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
        this.authorizationNumber = transaction.getAuthorizationNumber();
        this.paymentType = transaction.getPaymentType();
        this.operatorId = transaction.getOperatorId();
        this.createTime = createTime;
        this.shiftGuid = shiftGuid;
        this.cardName = transaction.getCardName();
        this.changeAmount = transaction.getChangeAmount();
        this.lastFour = transaction.getLastFour();
        this.availableAmount = transaction.getAvailableAmount();
        this.isPreauth = transaction.getIsPreauth();
        this.cashBack = transaction.getCashBack();
        this.balance = transaction.getBalance();
        this.applicationIdentifier = transaction.getApplicationIdentifier();
        this.resultCode = transaction.getResultCode();
        this.entryMethod = transaction.getEntryMethod();
        this.applicationCryptogramType = transaction.getApplicationCryptogramType();
        this.customerName = transaction.getCustomerName();
    }

    public PaymentTransactionModel(String shiftGuid, ITransaction transaction) {
        this.guid = transaction.getGuid();
        this.orderGuid = transaction.getOrderGuid();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();

        this.gateway = transaction.getGateway();

        this.isPreauth = transaction.getIsPreauth();
        this.paymentType = transaction.getPaymentType();

        if (isPreauth && PaymentType.SALE.equals(paymentType))
            this.preauthPaymentId = transaction.getPaymentId();
        else
            this.paymentId = transaction.getPaymentId();

        this.declineReason = transaction.getDeclineReason();
        this.parentTransactionGuid = transaction.getParentTransactionGuid();
        this.authorizationNumber = transaction.getAuthorizationNumber();

        this.operatorId = transaction.getOperatorId();
        //TODO fix it
        this.createTime = new Date();
        this.shiftGuid = shiftGuid;
        this.cardName = transaction.getCardName();
        this.changeAmount = transaction.getChangeAmount();
        this.lastFour = transaction.getLastFour();
        this.availableAmount = transaction.getAvailableAmount();
        this.cashBack = transaction.getCashBack();
        this.balance = transaction.getBalance();
        this.applicationIdentifier = transaction.getApplicationIdentifier();
        this.resultCode = transaction.getResultCode();
        this.entryMethod = transaction.getEntryMethod();
        this.applicationCryptogramType = transaction.getApplicationCryptogramType();
        this.customerName = transaction.getCustomerName();
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

        result.authorizationNumber = authorizationNumber;
        result.parentTransactionGuid = parentTransactionGuid;
        result.paymentType = paymentType;
        result.operatorId = operatorId;
        result.code = TransactionStatusCode.UNKNOWN;
        result.cardName = cardName;
        result.lastFour = lastFour;
        result.availableValue = availableAmount;
        result.isPreauth = isPreauth;
        result.balance = balance;
        result.allowReload = allowReload;
        result.cashBack = cashBack;
        result.balance = balance;
        result.applicationIdentifier = applicationIdentifier;
        result.entryMethod = entryMethod;
        result.applicationCryptogramType = applicationCryptogramType;
        result.customerName = customerName;
        result.resultCode = resultCode;
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
        v.put(PaymentTransactionTable.GUID, guid);
        v.put(PaymentTransactionTable.ORDER_GUID, orderGuid);
        v.put(PaymentTransactionTable.PARENT_GUID, parentTransactionGuid);
        v.put(PaymentTransactionTable.AMOUNT, _decimal(amount));
        v.put(PaymentTransactionTable.TYPE, _enum(paymentType));
        v.put(PaymentTransactionTable.STATUS, _enum(status));
        v.put(PaymentTransactionTable.GATEWAY, _enum(gateway == null ? PaymentGateway.CASH : gateway));
        v.put(PaymentTransactionTable.GATEWAY_PAYMENT_ID, paymentId);
        v.put(PaymentTransactionTable.GATEWAY_PREAUTH_PAYMENT_ID, preauthPaymentId);
        v.put(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID, closedPerauthGuid);
        v.put(PaymentTransactionTable.DECLINE_REASON, declineReason);
        v.put(PaymentTransactionTable.OPERATOR_GUID, operatorId);
        v.put(PaymentTransactionTable.CREATE_TIME, createTime.getTime());
        v.put(PaymentTransactionTable.SHIFT_GUID, shiftGuid);
        v.put(PaymentTransactionTable.CARD_NAME, cardName);
        v.put(PaymentTransactionTable.CHANGE_AMOUNT, _decimal(changeAmount));
        v.put(PaymentTransactionTable.IS_PREAUTH, isPreauth);
        v.put(PaymentTransactionTable.BALANCE, _decimal(balance));
        v.put(PaymentTransactionTable.CASH_BACK, _decimal(cashBack));
        v.put(PaymentTransactionTable.BALANCE, _decimal(balance));
        v.put(PaymentTransactionTable.SIGNATURE_BYTES, paxDigitalSignature);
        return v;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(PaymentTransactionTable.STATUS, _enum(status));
        v.put(PaymentTransactionTable.AMOUNT, _decimal(amount));
        v.put(PaymentTransactionTable.GATEWAY_PAYMENT_ID, paymentId);
        v.put(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID, closedPerauthGuid);
        v.put(PaymentTransactionTable.DECLINE_REASON, declineReason);
        return v;
    }

    public ContentValues getUpdatePaymentStatus() {
        ContentValues v = new ContentValues(1);
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
