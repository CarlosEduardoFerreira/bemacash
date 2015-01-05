package com.kaching123.tcr.model.payment.blackstone.pax;

import android.os.Parcel;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.LastTrasnactionResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is a transaction related variables
 */
public class PaxTransaction extends Transaction<PaxTransaction> {

    public boolean allowReload;

    public PaxTransaction(String userTransactionNumber, BigDecimal amount) {
        super(userTransactionNumber, amount);
    }

    public PaxTransaction(String userTransactionNumber, SaleActionResponse data) {
        super(userTransactionNumber, null);
        cardName = "Credit";
        type = TransactionType.PAX;
        paymentType = PaymentType.SALE;
        if (data != null) updateWith(data);
    }

    public void updateWith(SaleActionResponse data) {
        cardName = data.getDetails().getSale().getType();
        amount = new BigDecimal( data.getDetails().getAmount() );
        serviceTransactionNumber = data.getDetails().getTransactionNumber();
        authorizationNumber = data.getDetails().getSale().getAuthNumber();
        lastFour = data.getDetails().getDigits();
        userTransactionNumber = data.getDetails().getTransactionNumber();
        String balanceStr = data.getDetails().getSale().getBalance();
        if (balanceStr != null) {
            balance = new BigDecimal(balanceStr);
        }
    }

    public void updateWith(LastTrasnactionResponse data) {
        cardName = data.getDetails().getDetails().getSale().getType();
        amount = new BigDecimal( data.getDetails().getDetails().getAmount() );
        serviceTransactionNumber = data.getDetails().getDetails().getTransactionNumber();
        authorizationNumber = data.getDetails().getDetails().getSale().getAuthNumber();
        lastFour = data.getDetails().getDetails().getDigits();
        userTransactionNumber = data.getDetails().getDetails().getTransactionNumber();
        String balanceStr = data.getDetails().getDetails().getSale().getBalance();
        if (balanceStr != null) {
            balance = new BigDecimal(balanceStr);
        }
    }

    private PaxTransaction() {
        super();
    }
    public PaxTransaction(PaymentTransactionModel mdoel) {
        super(mdoel);
    }

    public static Creator<PaxTransaction> CREATOR = new Creator<PaxTransaction>() {

        @Override
        public PaxTransaction createFromParcel(Parcel source) {
            return new PaxTransaction().initFromParcelableSource(source);
        }

        @Override
        public PaxTransaction[] newArray(int size) {
            return new PaxTransaction[size];
        }
    };

    public PaxTransaction setGateway(PaymentGateway gateway) {
        this.gateway = gateway;
        return this;
    }

    @Override
    public PaymentGateway getGateway() {
        return gateway;
    }

}

