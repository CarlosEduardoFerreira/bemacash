package com.kaching123.tcr.model.payment.general.transaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBaseCommand;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.ITransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.RefundResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.websvc.WebAPI;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.Sale;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is a transaction related variables
 */
public abstract class Transaction<T extends Transaction<T>> implements Parcelable, ITransaction {

    protected PaymentGateway gateway;
    public TransactionStatusCode code;

    public String orderId;

    public BigDecimal changeValue = BigDecimal.ZERO;
    public BigDecimal availableValue = BigDecimal.ZERO;

    public String parentTransactionGuid;
    public PaymentType paymentType;

    public String operatorId;
    public String cardName;
    public String lastFour;
    public String authorizationNumber;

    public BigDecimal balance;

    public boolean allowReload;
    public boolean isPreauth;
    private boolean isClosedPreauth;
    public BigDecimal cashBack;
    public String applicationIdentifier;
    public String resultCode;
    public String entryMethod;
    public String applicationCryptogramType;
    public String customerName;

    public byte[] paxDigitalSignature = null;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_AMOUNT)
    public BigDecimal amount;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_USERTRANSACTIONNUMBER)
    public String userTransactionNumber;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_TRANSACTIONTYPE)
    public TransactionType type;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_SERVICETRANSACTIONNUMBER)
    public String serviceTransactionNumber;

    protected Transaction(TransactionType type, String userTransactionNumber, BigDecimal amount) {
        this.amount = amount;
        this.type = type;
        this.userTransactionNumber = userTransactionNumber;
    }

    protected Transaction(String userTransactionNumber, BigDecimal amount) {
        this.amount = amount;
        this.userTransactionNumber = userTransactionNumber;
    }

    protected Transaction(TransactionType type, String userTransactionNumber, String serviceTransactionNumber, BigDecimal amount) {
        this.type = type;
        this.userTransactionNumber = userTransactionNumber;
        this.serviceTransactionNumber = serviceTransactionNumber;
        this.amount = amount;
    }

    public Transaction(PaymentTransactionModel model) {

        userTransactionNumber = model.guid;
        orderId = model.orderGuid;
        amount = model.amount;
        type = TransactionType.valueOf(model.gateway);
        serviceTransactionNumber = TextUtils.isEmpty(model.preauthPaymentId) ? model.paymentId : model.preauthPaymentId;
        authorizationNumber = model.authorizationNumber;
        parentTransactionGuid = model.parentTransactionGuid;
        paymentType = model.paymentType;
        operatorId = model.operatorId;
        code = TransactionStatusCode.UNKNOWN;
        availableValue = model.availableAmount;
        cardName = model.cardName;
        isPreauth = model.isPreauth;
        isClosedPreauth = isPreauth && PaymentType.SALE.equals(paymentType) && PaymentStatus.SUCCESS.equals(model.status);
        cashBack = model.cashBack;
        balance = model.balance;
        paxDigitalSignature = model.paxDigitalSignature;
    }

    public Transaction() {
    }

    public T setType(TransactionType type) {
        this.type = type;
        return (T) this;
    }

    public T setOperatorId(String operatorId) {
        this.operatorId = operatorId;
        return (T) this;
    }

    public T setOrderId(String orderId) {
        this.orderId = orderId;
        return (T) this;
    }

    public T setCode(TransactionStatusCode code) {
        this.code = code;
        return (T) this;
    }

    public T setParentTransactionGuid(String parentTransactionGuid) {
        this.parentTransactionGuid = parentTransactionGuid;
        return (T) this;
    }

    public T setPaymentType(PaymentType paymentTenderType) {
        this.paymentType = paymentTenderType;
        return (T) this;
    }

    public T setCardName(String cardName) {
        this.cardName = cardName;
        return (T) this;
    }

    public T setCashBack(BigDecimal cashBack) {
        this.cashBack = cashBack;
        return (T) this;
    }

    public T setBalance(BigDecimal balance) {
        this.balance = balance;
        return (T) this;
    }

    @Override
    public String getParentTransactionGuid() {
        return parentTransactionGuid;
    }

    @Override
    public PaymentType getPaymentType() {
        return paymentType;
    }

    @Override
    public String getGuid() {
        return userTransactionNumber;
    }

    @Override
    public String getOrderGuid() {
        return orderId;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String getEntryMethod() {
        return entryMethod;
    }

    @Override
    public String getApplicationCryptogramType() {
        return applicationCryptogramType;
    }

    @Override
    public String getCustomerName() {
        return customerName;
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public String getApplicationIdentifier() {
        return applicationIdentifier;
    }

    public void setAvailableAmount(BigDecimal availableValue) {
        this.availableValue = availableValue;
    }

    @Override
    public BigDecimal getAvailableAmount() {
        return availableValue;
    }

    @Override
    public PaymentStatus getStatus() {
        if (code == null)
            return PaymentStatus.FAILED;
        switch (code) {
            case IN_PROGRESS:
            case NEW:
                return PaymentStatus.IN_PROGRESS;
            case OPERATION_COMPLETED_SUCCESSFULLY:
                return ((isPreauth && PaymentType.SALE.equals(paymentType) && !isClosedPreauth) ? PaymentStatus.PRE_AUTHORIZED : PaymentStatus.SUCCESS);
            default:
                return PaymentStatus.FAILED;
        }
    }

    @Override
    public String getPaymentId() {
        return serviceTransactionNumber;
    }

    @Override
    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    @Override
    public String getDeclineReason() {
        return code == null ? null : code.toString();
    }

    @Override
    public String getOperatorId() {
        return operatorId;
    }

    @Override
    public String getLastFour() {
        return lastFour;
    }

    @Override
    public boolean getIsPreauth() {
        return isPreauth;
    }

    @Override
    public BigDecimal getCashBack() {
        return cashBack;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    public T setIsPreauth(boolean isPreauth) {
        this.isPreauth = isPreauth;
        return (T) this;
    }

    public T updateFromSale(SaleResponse sale) {
        if (sale != null) {
            serviceTransactionNumber = sale.getServiceReferenceNumber();
            authorizationNumber = sale.getAuthorizationNumber();
            code = sale.getResponseCode();
            if (sale.getCardType() != null) {
                cardName = sale.getCardType();
                lastFour = sale.getLastFour();
            }
        } else {
            Logger.d("Sale response was null");
        }
        return (T) this;
    }

    public T updateFromRefund(RefundResponse sale) {
        if (sale != null) {
            code = sale.getResponseCode();
        } else {
            Logger.d("Sale response was null");
        }
        return (T) this;
    }

    public T updateFromProcessPreauth(PreauthResponse sale) {
        isClosedPreauth = false;
        if (sale != null) {
            serviceTransactionNumber = sale.getServiceReferenceNumber();
            authorizationNumber = sale.getAuthorizationNumber();
            code = sale.getResponseCode();
            if (sale.getCardType() != null) {
                cardName = sale.getCardType();
            }
        } else {
            Logger.d("Sale response was null");
        }
        return (T) this;
    }

    public T updateFromClosePreauth(PaxProcessorBaseCommand.PaxProcessorResponse response ) {
        isClosedPreauth = true;
        if (response != null) {
            serviceTransactionNumber = response.getResponse().RefNum;
            authorizationNumber = response.getResponse().AuthCode;
            code = response.getStatusCode();
            if ( response.getResponse().CardType != null) {
                cardName = response.getResponse().CardType;
            }
        } else {
            Logger.d("PaxProcessorResponse response was null");
        }
        return (T) this;
    }


    public T updateFromClosePreauth(PreauthResponse sale) {
        isClosedPreauth = true;
        if (sale != null) {
            serviceTransactionNumber = sale.getServiceReferenceNumber();
            authorizationNumber = sale.getAuthorizationNumber();
            code = sale.getResponseCode();
            if (sale.getCardType() != null) {
                cardName = sale.getCardType();
            }
        } else {
            Logger.d("Sale response was null");
        }
        return (T) this;
    }

    public T updateFromClosePreauth(Sale sale) {
        isClosedPreauth = true;
        if (sale != null) {
            serviceTransactionNumber = sale.getTransactionNumber();
            authorizationNumber = sale.getAuthNumber();
            code = TransactionStatusCode.valueOf(sale.getResponseCode());
            if (sale.getType() != null) {
                cardName = sale.getType();
            }
            userTransactionNumber = sale.getTransactionNumber();
            String balanceStr = sale.getBalance();
            if (balanceStr != null) {
                balance = new BigDecimal(balanceStr);
            }
        } else {
            Logger.d("Sale response was null");
        }
        return (T) this;
    }

    public T setAmount(BigDecimal amount) {
        this.amount = amount;
        return (T)this;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public BigDecimal getChangeAmount() {
        return changeValue;
    }

    @Override
    public boolean equals(Object o) {
        Transaction transaction;
        if (o != null && o instanceof Transaction) {
            transaction = (Transaction) o;
            return getGuid().equals(transaction.getGuid());
        }
        return super.equals(o);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type == null ? TransactionType.UNKNOWN.ordinal() : type.ordinal());
        dest.writeString(userTransactionNumber);
        dest.writeString(serviceTransactionNumber);
        dest.writeString(authorizationNumber);
        dest.writeSerializable(amount);
        dest.writeSerializable(availableValue);
        dest.writeSerializable(changeValue);
        dest.writeString(cardName);
        dest.writeInt(code == null ? 0 : code.getCode());
        dest.writeString(orderId);
        dest.writeString(parentTransactionGuid);
        dest.writeInt(paymentType == null ? 0 : paymentType.ordinal());
        dest.writeString(operatorId);
        dest.writeInt(isPreauth ? 1 : 0);
        dest.writeInt(isClosedPreauth ? 1 : 0);
        dest.writeString(gateway == null ? "" : gateway.toString());
        dest.writeSerializable(balance);
        dest.writeInt(allowReload ? 1 : 0);
        dest.writeSerializable(cashBack);
    }

    public T initFromParcelableSource(Parcel source) {
        setType(TransactionType.valueOf(source.readInt()));
        userTransactionNumber = source.readString();
        serviceTransactionNumber = source.readString();
        authorizationNumber = source.readString();
        amount = (BigDecimal) source.readSerializable();
        availableValue = (BigDecimal) source.readSerializable();
        changeValue = (BigDecimal) source.readSerializable();
        cardName = source.readString();
        setCode(TransactionStatusCode.valueOf(source.readInt()))
                .setOrderId(source.readString())
                .setParentTransactionGuid(source.readString())
                .setPaymentType(PaymentType.valueOf(source.readInt()))
                .setOperatorId(source.readString());
        isPreauth = source.readInt() > 0;
        isClosedPreauth = source.readInt() > 0;
        String s = source.readString();
        gateway = s == null || s.length() == 0 ? null : PaymentGateway.valueOf(s);
        Object os = source.readSerializable();
        balance = os == null ? null : (BigDecimal)os;
        allowReload = source.readInt() > 0;
        cashBack = (BigDecimal) source.readSerializable();
        return (T) this;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "gateway=" + gateway +
                ", code=" + code +
                ", orderId='" + orderId + '\'' +
                ", changeValue=" + changeValue +
                ", availableValue=" + availableValue +
                ", parentTransactionGuid='" + parentTransactionGuid + '\'' +
                ", paymentType=" + paymentType +
                ", operatorId='" + operatorId + '\'' +
                ", cardName='" + cardName + '\'' +
                ", lastFour='" + lastFour + '\'' +
                ", authorizationNumber='" + authorizationNumber + '\'' +
                ", balance=" + balance +
                ", allowReload=" + allowReload +
                ", isPreauth=" + isPreauth +
                ", isClosedPreauth=" + isClosedPreauth +
                ", amount=" + amount +
                ", userTransactionNumber='" + userTransactionNumber + '\'' +
                ", type=" + type +
                ", serviceTransactionNumber='" + serviceTransactionNumber + '\'' +
                ", cashback='" + cashBack + '\'' +
                '}';
    }
}
