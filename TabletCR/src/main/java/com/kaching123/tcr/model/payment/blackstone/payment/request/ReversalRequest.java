package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

/**
 * @author Ivan v. Rikhmayer
 */
public class ReversalRequest extends RequestBase<ReversalRequest> {

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(transaction, flags);
    }

    public static Creator<ReversalRequest> CREATOR = new Creator<ReversalRequest>() {

        @Override
        public ReversalRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            Transaction transaction = source.readParcelable(Transaction.class.getClassLoader());
            return new ReversalRequest().setUser(user).setTransaction(transaction);
        }

        @Override
        public ReversalRequest[] newArray(int size) {
            return new ReversalRequest[size];
        }
    };
}
