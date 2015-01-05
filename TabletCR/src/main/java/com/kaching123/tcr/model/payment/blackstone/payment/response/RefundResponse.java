package com.kaching123.tcr.model.payment.blackstone.payment.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
public class RefundResponse extends ResponseBase {
    
    public RefundResponse(List<String> msg, TransactionStatusCode responseCode) {
        super(msg, responseCode);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(msg);
        dest.writeInt(responseCode.getCode());
    }

    public static Parcelable.Creator<RefundResponse> CREATOR = new Parcelable.Creator<RefundResponse>() {

        @Override
        public RefundResponse createFromParcel(Parcel source) {
            ArrayList<String> msg = new ArrayList<String>();
            source.readList(msg, ArrayList.class.getClassLoader());
            TransactionStatusCode responseCode = TransactionStatusCode.valueOf(source.readInt());
            return new RefundResponse(msg, responseCode);
        }

        @Override
        public RefundResponse[] newArray(int size) {
            return new RefundResponse[size];
        }
    };
}
