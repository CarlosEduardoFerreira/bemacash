package com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass;

import com.kaching123.tcr.model.payment.blackstone.prepaid.PrePaidInfo;

/**
 * Created by pkabakov on 28.03.14.
 */
public class SunpassInfo extends PrePaidInfo {

    public String transponderId;
    public String purchaseId;
    public long referenceId;

    public SunpassInfo() {
        fieldsEmty = true;
    }

    public SunpassInfo(String transponderId, String purchaseId, long referenceId) {
        this.transponderId = transponderId;
        this.purchaseId = purchaseId;
        this.referenceId = referenceId;
    }
}
