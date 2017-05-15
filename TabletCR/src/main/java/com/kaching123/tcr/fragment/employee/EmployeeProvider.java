package com.kaching123.tcr.fragment.employee;

import com.kaching123.tcr.activity.BaseEmployeeActivity.EmployeeMode;
import com.kaching123.tcr.model.EmployeeModel;

/**
 * Created by mboychenko on 5/11/2017.
 */

public interface EmployeeProvider {
    EmployeeModel getEmployee();
    EmployeeMode getMode();
}
