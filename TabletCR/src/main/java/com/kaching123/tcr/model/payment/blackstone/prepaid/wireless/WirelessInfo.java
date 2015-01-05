package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless;

import com.kaching123.tcr.model.payment.blackstone.prepaid.PrePaidInfo;

/**
 * Created by pkabakov on 27.03.14.
 */
public abstract class WirelessInfo extends PrePaidInfo {

    public String controlNumber;
    public String customerServiceEnglish;
    public String transactionID;

    protected WirelessInfo() {
        fieldsEmty = true;
    }

    protected WirelessInfo(String controlNumber, String customerServiceEnglish, String transactionID) {
        this.controlNumber = controlNumber;
        this.customerServiceEnglish = customerServiceEnglish;
        this.transactionID = transactionID;
    }

}
