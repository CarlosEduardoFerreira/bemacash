package com.kaching123.tcr.model.payment.blackstone.payment.response;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 *
 * some minor data returned for voiding request
 */
public class DoFullRefundResponse extends ResponseBase {

    public DoFullRefundResponse(List<String> msg, TransactionStatusCode responseCode) {
        super(msg, responseCode);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(msg);
        dest.writeInt(responseCode.getCode());
    }

    public static Creator<DoFullRefundResponse> CREATOR = new Creator<DoFullRefundResponse>() {

        @Override
        public DoFullRefundResponse createFromParcel(Parcel source) {
            ArrayList<String> msg = new ArrayList<String>();
            source.readList(msg, ArrayList.class.getClassLoader());
            TransactionStatusCode responseCode = TransactionStatusCode.valueOf(source.readInt());
            return new DoFullRefundResponse(msg, responseCode);
        }

        @Override
        public DoFullRefundResponse[] newArray(int size) {
            return new DoFullRefundResponse[size];
        }
    };
}
