package com.kaching123.tcr.model.payment.cash;

import android.os.Parcel;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
public class CashTransaction extends Transaction<CashTransaction> {

    public CashTransaction(String userTransactionNumber, BigDecimal amount) {
        super(userTransactionNumber, amount);
        cardName = "Cash";
    }

    private CashTransaction() {
        super();
        cardName = "Cash";
    }

    public static Creator<CashTransaction> CREATOR = new Creator<CashTransaction>() {

        @Override
        public CashTransaction createFromParcel(Parcel source) {
            return new CashTransaction().initFromParcelableSource(source);
        }

        @Override
        public CashTransaction[] newArray(int size) {
            return new CashTransaction[size];
        }
    };

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.CASH;
    }
}
