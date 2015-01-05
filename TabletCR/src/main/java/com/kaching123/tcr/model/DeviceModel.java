package com.kaching123.tcr.model;

/**
 * Created by pkabakov on 28.02.14.
 */
public class DeviceModel {

    private String name;
    private String address;

    public DeviceModel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

}
