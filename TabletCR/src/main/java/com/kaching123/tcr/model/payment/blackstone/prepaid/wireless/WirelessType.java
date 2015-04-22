package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public enum WirelessType {
    SUNPASS_TOLL("SUNPASS TOLL"),
    NATIONAL_TOP_UP("NATIONAL TOP UP"),
    INTERNATIONAL_WIRELESS_PIN("INTERNATIONAL WIRELESS PIN"),
    PINLESS("PINLESS"),
    INTERNATIONAL_TOP_UP("INTERNATIONAL TOP UP"),
    LONG_DISTANCE_PIN("LONG DISTANCE PIN"),
    NATIONAL_WIRELESS_PIN("NATIONAL WIRELESS PIN"),
    UNKNOWN_PRODCUT("UNKNOWN_PRODCUT");
    private String name;

    WirelessType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static WirelessType fromString(String name) {
        if (SUNPASS_TOLL.name.equals(name)) {
            return SUNPASS_TOLL;
        } else if (NATIONAL_TOP_UP.name.equals(name)) {
            return NATIONAL_TOP_UP;
        } else if (INTERNATIONAL_WIRELESS_PIN.name.equals(name)) {
            return INTERNATIONAL_WIRELESS_PIN;
        } else if (PINLESS.name.equals(name)) {
            return PINLESS;
        } else if (INTERNATIONAL_TOP_UP.name.equals(name)) {
            return INTERNATIONAL_TOP_UP;
        } else if (LONG_DISTANCE_PIN.name.equals(name)) {
            return LONG_DISTANCE_PIN;
        } else if (NATIONAL_WIRELESS_PIN.name.equals(name)) {
            return NATIONAL_WIRELESS_PIN;
        } else if (UNKNOWN_PRODCUT.name.equals(name)) {
            return UNKNOWN_PRODCUT;
        } else
            return null;
    }

    public boolean isLongDistance() {
        return equals(LONG_DISTANCE_PIN);
    }

    public boolean isWireless() {
        return equals(NATIONAL_TOP_UP) || equals(NATIONAL_WIRELESS_PIN);
    }

    public boolean isPinless() {
        return equals(PINLESS);
    }

    public boolean isWirelessInternational() {
        return equals(INTERNATIONAL_WIRELESS_PIN) || equals(INTERNATIONAL_TOP_UP);
    }

    public boolean isPin() {
        return equals(INTERNATIONAL_WIRELESS_PIN) || equals(LONG_DISTANCE_PIN) || equals(NATIONAL_WIRELESS_PIN);
    }
}
