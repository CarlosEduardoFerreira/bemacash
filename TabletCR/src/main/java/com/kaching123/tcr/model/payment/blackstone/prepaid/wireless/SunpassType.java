package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless;


public enum SunpassType {
    SUNPASS_PAY_YOUR_DOCUMENT("SUNPASS PAY_YOUR_DOCUMENT"),
    SUNPASS_TRANSPONDER("SUNPASS TRANSPONDER");
    private String name;

    SunpassType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SunpassType fromString(String name) {
        if (SUNPASS_PAY_YOUR_DOCUMENT.name.equals(name)) {
            return SUNPASS_PAY_YOUR_DOCUMENT;
        } else if (SUNPASS_TRANSPONDER.name.equals(name)) {
            return SUNPASS_TRANSPONDER;
        } else return null;
    }

}
