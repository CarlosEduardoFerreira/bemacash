package com.kaching123.tcr.model.payment.blackstone.payment;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.WebAPI;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold up data common for all requests
 */
public class User implements Parcelable {

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_USERNAME)
    protected String username;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_PASSWORD)
    protected String password;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_MID)
    protected int mid;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_CID)
    protected int cid;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_APPKEY)
    protected String appkey;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.REQUEST_PARAM_APPTYPE)
    protected int apptype;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMid() {
        return mid;
    }

    public int getCid() {
        return cid;
    }

    public String getAppkey() {
        return appkey;
    }

    public int getApptype() {
        return apptype;
    }

    public User() {
    }

    public User(User user) {
        this(user.username, user.password, user.mid, user.cid, user.appkey, user.apptype);
    }

    public User(String username, String password, int mid, int cid, String appkey, int apptype) {
        this.username = username;
        this.password = password;
        this.mid = mid;
        this.cid = cid;
        this.appkey = appkey;
        this.apptype = apptype;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeInt(mid);
        dest.writeInt(cid);
        dest.writeString(appkey);
        dest.writeInt(apptype);
    }

    public static Creator<User> CREATOR = new Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            User user = new User(source.readString(), source.readString(), source.readInt(), source.readInt(), source.readString(), source.readInt());
            return user;
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public boolean isValid() {
        return  mid != 0 &&
                cid != 0 &&
                !TextUtils.isEmpty(username) &&
                !TextUtils.isEmpty(password) &&
                !TextUtils.isEmpty(appkey) &&
                apptype != 0;
    }
}
