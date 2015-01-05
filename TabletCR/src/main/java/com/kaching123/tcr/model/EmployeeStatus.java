package com.kaching123.tcr.model;

import com.kaching123.tcr.R;

/**
 * Created by hamst_000 on 08/11/13.
 */
public enum EmployeeStatus {
    ACTIVE(R.string.employee_status_label_active),
    BLOCKED(R.string.employee_status_label_blocked);

    private final int labelRes;

    EmployeeStatus(int labelRes) {
        this.labelRes = labelRes;
    }

    public int getLabelRes() {
        return labelRes;
    }
}
