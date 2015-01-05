package com.kaching123.tcr.model.payment.credit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditReceiptData implements Parcelable {

    //public String receiptCode;

    public String register;
    public String receiptNum;

    public CreditReceiptData(String register, String receiptNum) {
        this.register = register;
        this.receiptNum = receiptNum;
    }

    protected CreditReceiptData(Parcel in) {
        register = in.readString();
        receiptNum = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(register);
        dest.writeString(receiptNum);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CreditReceiptData> CREATOR = new Parcelable.Creator<CreditReceiptData>() {

        @Override
        public CreditReceiptData createFromParcel(Parcel source) {
            return new CreditReceiptData(source);
        }

        @Override
        public CreditReceiptData[] newArray(int size) {
            return new CreditReceiptData[size];
        }
    };

}