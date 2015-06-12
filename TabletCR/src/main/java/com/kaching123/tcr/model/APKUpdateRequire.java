package com.kaching123.tcr.model;

import com.kaching123.tcr.R;

/**
 * Created by teli on 6/2/2015.
 */

public enum APKUpdateRequire {
    CRITICAL(R.string.apk_update_require_label_critical),
    MAJOR(R.string.apk_update_require_label_major),
    MINOR(R.string.apk_update_require_label_minor),
    TIMER(R.string.apk_update_require_label_timer);

    private final int labelRes;

    APKUpdateRequire(int labelRes) {
        this.labelRes = labelRes;
    }

    public int getLabelRes() {
        return labelRes;
    }
}
