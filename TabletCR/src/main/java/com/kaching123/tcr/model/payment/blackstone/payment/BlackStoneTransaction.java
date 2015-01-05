package com.kaching123.tcr.model.payment.blackstone.payment;

import android.os.Parcel;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is a transaction related variables
 */
public class BlackStoneTransaction extends Transaction<BlackStoneTransaction> {

    public BlackStoneTransaction(PaymentTransactionModel mdoel) {
        super(mdoel);
    }

    public BlackStoneTransaction(String userTransactionNumber, BigDecimal amount) {
        super(userTransactionNumber, amount);
    }

    private BlackStoneTransaction() {
        super();
    }

    public static Creator<BlackStoneTransaction> CREATOR = new Creator<BlackStoneTransaction>() {

        @Override
        public BlackStoneTransaction createFromParcel(Parcel source) {
            return new BlackStoneTransaction().initFromParcelableSource(source);
        }

        @Override
        public BlackStoneTransaction[] newArray(int size) {
            return new BlackStoneTransaction[size];
        }
    };

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.BLACKSTONE;
    }
}

