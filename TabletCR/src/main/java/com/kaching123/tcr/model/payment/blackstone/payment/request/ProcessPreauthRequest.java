package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.WebAPI.BlackStoneAPI;

public class ProcessPreauthRequest extends RequestBase<ProcessPreauthRequest> {

    private static final String IS_CLOSABLE_DEFAULT = "true";

    @Expose
    @SerializedName(BlackStoneAPI.REQUEST_PARAM_COMMENTS)
    private String comments;

    @Expose
    @SerializedName(BlackStoneAPI.REQUEST_PARAM_ISCLOSABLE)
    private String isClosable = IS_CLOSABLE_DEFAULT;

    public String getComments() {
        return comments;
    }

    public ProcessPreauthRequest setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public String getIsClosable() {
        return isClosable;
    }

    public ProcessPreauthRequest setIsClosable(String isClosable) {
        this.isClosable = isClosable;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(card, flags);
        dest.writeParcelable(transaction, flags);
        dest.writeString(comments);
        dest.writeString(isClosable);
    }

    public static Creator<ProcessPreauthRequest> CREATOR = new Creator<ProcessPreauthRequest>() {

        @Override
        public ProcessPreauthRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            CreditCard card = source.readParcelable(CreditCard.class.getClassLoader());
            Transaction transaction = source.readParcelable(Transaction.class.getClassLoader());
            String comments = source.readString();
            String isClosable = source.readString();
            return new ProcessPreauthRequest().setUser(user).setCard(card).setTransaction(transaction).setComments(comments).setIsClosable(isClosable);
        }

        @Override
        public ProcessPreauthRequest[] newArray(int size) {
            return new ProcessPreauthRequest[size];
        }
    };

}
