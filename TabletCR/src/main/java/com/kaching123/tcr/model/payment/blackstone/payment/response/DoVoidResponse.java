package com.kaching123.tcr.model.payment.blackstone.payment.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 *
 * some minor data returned for voiding request
 */
public class DoVoidResponse extends ResponseBase {

    public DoVoidResponse(List<String> msg, TransactionStatusCode responseCode) {
        super(msg, responseCode);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(msg);
        dest.writeInt(responseCode.getCode());
    }

    public static Parcelable.Creator<DoVoidResponse> CREATOR = new Parcelable.Creator<DoVoidResponse>() {

        @Override
        public DoVoidResponse createFromParcel(Parcel source) {
            ArrayList<String> msg = new ArrayList<String>();
            source.readList(msg, ArrayList.class.getClassLoader());
            TransactionStatusCode responseCode = TransactionStatusCode.valueOf(source.readInt());
            return new DoVoidResponse(msg, responseCode);
        }

        @Override
        public DoVoidResponse[] newArray(int size) {
            return new DoVoidResponse[size];
        }
    };
}
