package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;

/**
 * Created by pkabakov on 23.05.2014.
 */
public class DoSettlementRequest extends RequestBase<DoSettlementRequest> {

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
    }

    public static Creator<DoSettlementRequest> CREATOR = new Creator<DoSettlementRequest>() {

        @Override
        public DoSettlementRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            return new DoSettlementRequest().setUser(user);
        }

        @Override
        public DoSettlementRequest[] newArray(int size) {
            return new DoSettlementRequest[size];
        }
    };
}
