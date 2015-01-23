package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class CheckForMerchantFlagUpdateRequest extends RequestBase {

    public String mID;
    public String tID;
    public String password;
    public long transactionId;

    @Override
    public String toString() {
        return "CheckForUpdateRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password" +
                ", transactionId=" + transactionId +
                '}';
    }
}
