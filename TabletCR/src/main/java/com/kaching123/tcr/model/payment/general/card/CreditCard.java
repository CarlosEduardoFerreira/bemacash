package com.kaching123.tcr.model.payment.general.card;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.WebAPI;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class CreditCard implements Parcelable {

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_ZIPCODE)
    private String zipcode;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_CARDNUMBER)
    private String cardnumber;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_EXPDATE)
    private String expdate;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_NAMEONCARD)
    private String nameoncard;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_CVN)
    private String cvn;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_STREET)
    private String street;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_TRACK2)
    protected String track2;

    /**
     * No-swipe
     */
    public CreditCard(String zipcode, String cardnumber, String expdate, String nameoncard, String cvn, String street) {
        this.zipcode = zipcode;
        this.cardnumber = cardnumber;
        this.expdate = expdate;
        this.nameoncard = nameoncard;
        this.cvn = cvn;
        this.street = street;
    }

    /**
     * Swipe
     */
    public CreditCard(String track2) {
        this.track2 = track2;
    }

    /**
     * for groundy purposes
     */
    public CreditCard(String zipcode, String cardnumber, String expdate, String nameoncard, String cvn, String street, String track2) {
        this.zipcode = zipcode;
        this.cardnumber = cardnumber;
        this.expdate = expdate;
        this.nameoncard = nameoncard;
        this.cvn = cvn;
        this.street = street;
        this.track2 = track2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(zipcode);
        dest.writeString(cardnumber);
        dest.writeString(expdate);
        dest.writeString(nameoncard);
        dest.writeString(cvn);
        dest.writeString(street);
        dest.writeString(track2);
    }

    public static Creator<CreditCard> CREATOR = new Creator<CreditCard>() {

        @Override
        public CreditCard createFromParcel(Parcel source) {
            String zipcode = source.readString();
            String cardnumber = source.readString();
            String expdate = source.readString();
            String nameoncard = source.readString();
            String cvn = source.readString();
            String street = source.readString();
            String track2 = source.readString();
            return new CreditCard(zipcode,
                    cardnumber,
                    expdate,
                    nameoncard,
                    cvn,
                    street,
                    track2);
        }

        @Override
        public CreditCard[] newArray(int size) {
            return new CreditCard[size];
        }
    };
}
