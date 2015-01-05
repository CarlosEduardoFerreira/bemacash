package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless;

import com.kaching123.tcr.websvc.api.prepaid.AccessPhone;
import com.kaching123.tcr.websvc.api.prepaid.VectorAccessPhone;

/**
 * Created by pkabakov on 27.03.14.
 */
public class WirelessPinInfo extends WirelessInfo {

    public AccessPhone[] localAccessPhones;
    public String pinNumber;
    public String expirationDate;

    public WirelessPinInfo() {
        fieldsEmty = true;
    }

    public WirelessPinInfo(String controlNumber, String customerServiceEnglish, String transactionID, VectorAccessPhone localAccessPhones, String pinNumber, String expirationDate) {
        super(controlNumber, customerServiceEnglish, transactionID);
        if (localAccessPhones == null) {
            this.localAccessPhones = null;
        } else {
            int count = localAccessPhones.getPropertyCount();
            this.localAccessPhones = new AccessPhone[count];
            for (int i = 0; i < count; i++) {
                AccessPhone phone = (AccessPhone) localAccessPhones.getProperty(i);
                this.localAccessPhones[i] = phone;
            }
        }
        this.pinNumber = pinNumber;
        this.expirationDate = expirationDate;
    }
}
