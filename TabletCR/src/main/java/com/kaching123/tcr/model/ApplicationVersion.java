package com.kaching123.tcr.model;

/**
 * Created by pkabakov on 21.07.2014.
 */
public class ApplicationVersion {

    public final int code;
    public final String name;

    public ApplicationVersion(int versionCode, String versionName) {
        this.code = versionCode;
        this.name = versionName;
    }
}
