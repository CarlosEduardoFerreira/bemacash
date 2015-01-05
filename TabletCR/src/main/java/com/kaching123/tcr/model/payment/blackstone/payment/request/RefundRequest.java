package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold all data for RefundRequest request
 */
public class RefundRequest extends RequestBase<RefundRequest> {

    private BigDecimal amount;

    public RefundRequest setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeSerializable(amount);
            dest.writeParcelable(user, flags);
            dest.writeParcelable(card, flags);
            dest.writeSerializable(transactionModel);
            dest.writeParcelable(transaction, flags);
        } catch (Exception e) {
            Logger.e("Failed", e);
        }
    }

    public static Creator<RefundRequest> CREATOR = new Creator<RefundRequest>() {

        @Override
        public RefundRequest createFromParcel(Parcel source) {
            BigDecimal amount = (BigDecimal) source.readSerializable();
            User user = source.readParcelable(User.class.getClassLoader());
            CreditCard card = source.readParcelable(CreditCard.class.getClassLoader());
            PaymentTransactionModel transactionModel = (PaymentTransactionModel)source.readSerializable();
            Transaction transaction = source.readParcelable(Transaction.class.getClassLoader());
            return new RefundRequest().setUser(user).setCard(card).setTransactionModel(transactionModel).setAmount(amount).setTransaction(transaction);
        }

        @Override
        public RefundRequest[] newArray(int size) {
            return new RefundRequest[size];
        }
    };
}




