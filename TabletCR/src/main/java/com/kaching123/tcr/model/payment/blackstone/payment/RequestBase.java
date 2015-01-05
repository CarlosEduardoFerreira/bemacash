package com.kaching123.tcr.model.payment.blackstone.payment;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

/**
 * @author Ivan v. Rikhmayer
 * RequestBase class
 */
public abstract class RequestBase<T extends RequestBase<T>> implements Parcelable {

    public static final String GSON_ATTR_USER = "GSON_ATTR_USER";
    public static final String GSON_ATTR_CARD = "GSON_ATTR_card";
    public static final String GSON_ATTR_TRANSACTION = "GSON_ATTR_transaction";

    @Expose
    @SerializedName(GSON_ATTR_USER)
    protected User user;

    @Expose
    @SerializedName(GSON_ATTR_CARD)
    protected CreditCard card;

    @Expose
    @SerializedName(GSON_ATTR_TRANSACTION)
    protected Transaction transaction;

    protected PaymentTransactionModel transactionModel;

    public RequestBase() {
    }

    public T setCard(CreditCard card) {
        if (card != null) {
            this.card = card;
        }
        return (T)this;
    }

    public T setUser(User user) {
        if (user != null) {
            this.user = user;
        }
        return (T)this;
    }

    public T setTransaction(Transaction transaction) {
        if (transaction != null) {
            this.transaction = transaction;
        }

        return (T)this;
    }
    public T setTransactionModel(PaymentTransactionModel transaction) {
        if (transaction != null) {
            this.transactionModel = transaction;
        }
        return (T)this;
    }

    public User getUser() {
        return user;
    }

    public CreditCard getCard() {
        return card;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public PaymentTransactionModel getTransactionModel() {
        return transactionModel;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
