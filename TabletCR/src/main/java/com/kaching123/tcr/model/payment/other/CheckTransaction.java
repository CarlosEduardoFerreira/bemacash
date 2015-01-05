package com.kaching123.tcr.model.payment.other;

import android.os.Parcel;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.math.BigDecimal;

public class CheckTransaction extends Transaction<CheckTransaction> {

    public CheckTransaction(String userTransactionNumber, BigDecimal amount) {
        super(userTransactionNumber, amount);
        cardName = "Check";
    }

    private CheckTransaction() {
        super();
        cardName = "Check";
    }

    public static Creator<CheckTransaction> CREATOR = new Creator<CheckTransaction>() {

        @Override
        public CheckTransaction createFromParcel(Parcel source) {
            return new CheckTransaction().initFromParcelableSource(source);
        }

        @Override
        public CheckTransaction[] newArray(int size) {
            return new CheckTransaction[size];
        }
    };

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.CHECK;
    }
}
