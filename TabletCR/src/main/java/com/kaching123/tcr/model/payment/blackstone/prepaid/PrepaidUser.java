package com.kaching123.tcr.model.payment.blackstone.prepaid;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.WebAPI;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class PrepaidUser implements Serializable {

    //http://services.bstonecorp.com/TransactionBroker/Broker.asmx

    @Expose
    @SerializedName(WebAPI.BlackStonePrepaidAPI.REQUEST_PARAM_PASSWORD)
    protected String password;

    @Expose
    @SerializedName(WebAPI.BlackStonePrepaidAPI.REQUEST_PARAM_MID)
    protected int mid;

    @Expose
    @SerializedName(WebAPI.BlackStonePrepaidAPI.REQUEST_PARAM_TID)
    protected int tid;

    public PrepaidUser() {

    }

    public PrepaidUser(PrepaidUser user) {
        this(user.password, user.mid, user.tid);
    }

    public PrepaidUser(String password, int mid, int tid) {
        this.password = password;
        this.mid = mid;
        this.tid = tid;
    }

    public String getPassword() {
        return password;
    }

    public int getMid() {
        return mid;
    }

    public int getTid() {
        return tid;
    }

    public boolean isValid() {
        return  mid != 0 &&
                tid != 0 &&
                !TextUtils.isEmpty(password);
    }
}
