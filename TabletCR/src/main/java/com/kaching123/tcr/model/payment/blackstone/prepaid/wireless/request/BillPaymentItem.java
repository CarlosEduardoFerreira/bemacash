package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;

/**
 * Created by teli.yin on 12/18/2014.
 */
public class BillPaymentItem implements IValueModel, Serializable, Parcelable {

    public String masterBillerId;
    public String masterBillerDescription;
    public String categoryId;
    public String categoryDescription;

    public BillPaymentItem(String categoryId, String categoryDescription, String masterBillerId, String masterBillerDescription) {
        this.categoryId = categoryId;
        this.categoryDescription = categoryDescription;
        this.masterBillerId = masterBillerId;
        this.masterBillerDescription = masterBillerDescription;
    }

    public BillPaymentItem(Cursor c) {
        this(c.getString(c.getColumnIndex(ShopStore.BillPayment.CATEGORYID)),
                c.getString(c.getColumnIndex(ShopStore.BillPayment.CATEGORYDESCRIPTION)),
                c.getString(c.getColumnIndex(ShopStore.BillPayment.MASTERBILLERID)),
                c.getString(c.getColumnIndex(ShopStore.BillPayment.MASTERBILLERDESCRIPTION)));

    }

    @Override
    public String getGuid() {
        return masterBillerId;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.BillPayment.CATEGORYID, categoryId);
        v.put(ShopStore.BillPayment.CATEGORYDESCRIPTION, categoryDescription);
        v.put(ShopStore.BillPayment.MASTERBILLERID, masterBillerId);
        v.put(ShopStore.BillPayment.MASTERBILLERDESCRIPTION, masterBillerDescription);
        return v;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryId);
        dest.writeString(categoryDescription);
        dest.writeString(masterBillerId);
        dest.writeString(masterBillerDescription);

    }

    public static Creator<BillPaymentItem> CREATOR = new Creator<BillPaymentItem>() {

        @Override
        public BillPaymentItem createFromParcel(Parcel source) {
            return new BillPaymentItem(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString()

            );
        }

        @Override
        public BillPaymentItem[] newArray(int size) {
            return new BillPaymentItem[size];
        }
    };
}
