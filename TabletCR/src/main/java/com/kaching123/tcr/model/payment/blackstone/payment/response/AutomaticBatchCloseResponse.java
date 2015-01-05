package com.kaching123.tcr.model.payment.blackstone.payment.response;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkabakov on 11.06.2014.
 */
public class AutomaticBatchCloseResponse extends ResponseBase {

    public AutomaticBatchCloseResponse(List<String> msg, TransactionStatusCode responseCode) {
        super(msg, responseCode);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(msg);
        dest.writeInt(responseCode.getCode());
    }

    public static Creator<AutomaticBatchCloseResponse> CREATOR = new Creator<AutomaticBatchCloseResponse>() {

        @Override
        public AutomaticBatchCloseResponse createFromParcel(Parcel source) {
            ArrayList<String> msg = new ArrayList<String>();
            source.readList(msg, ArrayList.class.getClassLoader());
            TransactionStatusCode responseCode = TransactionStatusCode.valueOf(source.readInt());
            return new AutomaticBatchCloseResponse(msg, responseCode);
        }

        @Override
        public AutomaticBatchCloseResponse[] newArray(int size) {
            return new AutomaticBatchCloseResponse[size];
        }
    };
}
