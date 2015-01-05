package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold all data for SAle request
 */
public class SaleRequest extends RequestBase<SaleRequest> {

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(card, flags);
        dest.writeParcelable(transaction, flags);
    }

    public static Creator<SaleRequest> CREATOR = new Creator<SaleRequest>() {

        @Override
        public SaleRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            CreditCard card = source.readParcelable(CreditCard.class.getClassLoader());
            Transaction transaction = source.readParcelable(Transaction.class.getClassLoader());
            return new SaleRequest().setUser(user).setCard(card).setTransaction(transaction);
        }

        @Override
        public SaleRequest[] newArray(int size) {
            return new SaleRequest[size];
        }
    };
}
