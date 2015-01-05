package com.kaching123.tcr.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pkabakov on 23.01.14.
 */
public class ShiftViewModel extends ShiftModel {

    public String openEmployeeFullName;
    public String closeEmployeeFullName;

    public String registerTitle;

    public ShiftViewModel(String guid, Date startTime, Date endTime,
                          String openManagerId, String closeManagerId,
                          long registerId, BigDecimal openAmount, BigDecimal closeAmount,
                          String openEmployeeFullName, String closeEmployeeFullName,
                          String registerTitle) {
        super(guid, startTime, endTime, openManagerId, closeManagerId, registerId, openAmount, closeAmount);
        this.openEmployeeFullName = openEmployeeFullName;
        this.closeEmployeeFullName = closeEmployeeFullName;
        this.registerTitle = registerTitle;
    }
}
