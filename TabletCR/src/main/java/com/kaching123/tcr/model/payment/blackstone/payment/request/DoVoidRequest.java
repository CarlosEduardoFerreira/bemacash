package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 */
public class DoVoidRequest extends RequestBase<DoVoidRequest> {

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(transaction, flags);
        dest.writeSerializable(transactionModel);
    }

    public static Creator<DoVoidRequest> CREATOR = new Creator<DoVoidRequest>() {

        @Override
        public DoVoidRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            Transaction transaction = source.readParcelable(Transaction.class.getClassLoader());
            Serializable result = source.readSerializable();
            PaymentTransactionModel model = null;
            if (result != null) {
                model = (PaymentTransactionModel) result;
            }
            return new DoVoidRequest().setUser(user).setTransaction(transaction).setTransactionModel(model);
        }

        @Override
        public DoVoidRequest[] newArray(int size) {
            return new DoVoidRequest[size];
        }
    };


}
