package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.WebAPI.BlackStoneAPI;

import java.math.BigDecimal;

public class ClosePreauthRequest extends RequestBase<ClosePreauthRequest> {

    @Expose
    @SerializedName(BlackStoneAPI.REQUEST_PARAM_ADDITIONALTIP)
    private BigDecimal additionalTipsAmount;

    public BigDecimal getAdditionalTipsAmount() {
        return additionalTipsAmount;
    }

    public ClosePreauthRequest setAdditionalTipsAmount(BigDecimal additionalTipsAmount) {
        this.additionalTipsAmount = additionalTipsAmount;
        return this;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(transaction, flags);
        dest.writeSerializable(transactionModel);
        dest.writeSerializable(additionalTipsAmount);
    }

    public static Creator<ClosePreauthRequest> CREATOR = new Creator<ClosePreauthRequest>() {

        @Override
        public ClosePreauthRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            Transaction transaction = source.readParcelable(Transaction.class.getClassLoader());
            PaymentTransactionModel transactionModel = (PaymentTransactionModel) source.readSerializable();
            BigDecimal additionalTipsAmount = (BigDecimal) source.readSerializable();
            return new ClosePreauthRequest().setUser(user).setTransaction(transaction).setTransactionModel(transactionModel).setAdditionalTipsAmount(additionalTipsAmount);
        }

        @Override
        public ClosePreauthRequest[] newArray(int size) {
            return new ClosePreauthRequest[size];
        }
    };

}
