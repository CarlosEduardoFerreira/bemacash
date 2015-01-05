package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;

/**
 * @author Ivan v. Rikhmayer
 */
public class SettlementlRequest extends RequestBase<SettlementlRequest> {

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
    }

    public static Creator<SettlementlRequest> CREATOR = new Creator<SettlementlRequest>() {

        @Override
        public SettlementlRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            return new SettlementlRequest().setUser(user);
        }

        @Override
        public SettlementlRequest[] newArray(int size) {
            return new SettlementlRequest[size];
        }
    };
}
