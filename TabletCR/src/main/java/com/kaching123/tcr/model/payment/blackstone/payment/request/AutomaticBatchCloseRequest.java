package com.kaching123.tcr.model.payment.blackstone.payment.request;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.websvc.WebAPI.BlackStoneAPI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pkabakov on 11.06.2014.
 */
public class AutomaticBatchCloseRequest extends RequestBase<AutomaticBatchCloseRequest> {

    private static final ThreadLocal<DateFormat> timeFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm", Locale.US);
        }
    };

    @Expose
    @SerializedName(BlackStoneAPI.REQUEST_PARAM_TIME)
    private String time;

    public AutomaticBatchCloseRequest setTime(Date time) {
        this.time = timeFormatThreadLocal.get().format(time);
        return this;
    }

    public AutomaticBatchCloseRequest setTime(String time) {
        this.time = time;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeString(time);
    }

    public static Creator<AutomaticBatchCloseRequest> CREATOR = new Creator<AutomaticBatchCloseRequest>() {

        @Override
        public AutomaticBatchCloseRequest createFromParcel(Parcel source) {
            User user = source.readParcelable(User.class.getClassLoader());
            String time = source.readString();
            return new AutomaticBatchCloseRequest().setUser(user).setTime(time);
        }

        @Override
        public AutomaticBatchCloseRequest[] newArray(int size) {
            return new AutomaticBatchCloseRequest[size];
        }
    };

}
