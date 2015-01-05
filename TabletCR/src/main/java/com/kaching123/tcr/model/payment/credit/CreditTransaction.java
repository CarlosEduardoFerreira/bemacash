package com.kaching123.tcr.model.payment.credit;

import android.os.Parcel;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.math.BigDecimal;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditTransaction extends Transaction<CreditTransaction>{

    private String creditReceiptGuid;

    public CreditTransaction(String userTransactionNumber, BigDecimal amount, String creditReceiptGuid) {
        super(userTransactionNumber, amount);
        cardName = "Credit";
        this.creditReceiptGuid = creditReceiptGuid;
    }

    private CreditTransaction() {
        super();
        cardName = "Credit";
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(creditReceiptGuid);
    }

    @Override
    public CreditTransaction initFromParcelableSource(Parcel source) {
        super.initFromParcelableSource(source);
        this.creditReceiptGuid = source.readString();
        return this;
    }

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.CREDIT;
    }

    public static Creator<CreditTransaction> CREATOR = new Creator<CreditTransaction>() {

        @Override
        public CreditTransaction createFromParcel(Parcel source) {
            return new CreditTransaction().initFromParcelableSource(source);
        }

        @Override
        public CreditTransaction[] newArray(int size) {
            return new CreditTransaction[size];
        }
    };
}
