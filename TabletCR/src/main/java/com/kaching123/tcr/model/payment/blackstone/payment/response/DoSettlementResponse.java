package com.kaching123.tcr.model.payment.blackstone.payment.response;

import android.os.Parcel;

import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkabakov on 23.05.2014.
 */
public class DoSettlementResponse extends ResponseBase {

    public DoSettlementResponse(List<String> msg, TransactionStatusCode responseCode) {
        super(msg, responseCode);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(msg);
        dest.writeInt(responseCode.getCode());
    }

    public static Creator<DoSettlementResponse> CREATOR = new Creator<DoSettlementResponse>() {

        @Override
        public DoSettlementResponse createFromParcel(Parcel source) {
            ArrayList<String> msg = new ArrayList<String>();
            source.readList(msg, ArrayList.class.getClassLoader());
            TransactionStatusCode responseCode = TransactionStatusCode.valueOf(source.readInt());
            return new DoSettlementResponse(msg, responseCode);
        }

        @Override
        public DoSettlementResponse[] newArray(int size) {
            return new DoSettlementResponse[size];
        }
    };
}
