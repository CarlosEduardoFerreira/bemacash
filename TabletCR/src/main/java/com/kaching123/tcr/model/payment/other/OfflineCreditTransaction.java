package com.kaching123.tcr.model.payment.other;

import android.os.Parcel;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.math.BigDecimal;

public class OfflineCreditTransaction extends Transaction<OfflineCreditTransaction> {

    public OfflineCreditTransaction(String userTransactionNumber, BigDecimal amount) {
        super(userTransactionNumber, amount);
        cardName = "Offline Credit";
    }

    private OfflineCreditTransaction() {
        super();
        cardName = "Offline Credit";
    }

    public static Creator<OfflineCreditTransaction> CREATOR = new Creator<OfflineCreditTransaction>() {

        @Override
        public OfflineCreditTransaction createFromParcel(Parcel source) {
            return new OfflineCreditTransaction().initFromParcelableSource(source);
        }

        @Override
        public OfflineCreditTransaction[] newArray(int size) {
            return new OfflineCreditTransaction[size];
        }
    };

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.OFFLINE_CREDIT;
    }
}
