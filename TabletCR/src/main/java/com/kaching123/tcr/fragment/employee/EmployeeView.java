package com.kaching123.tcr.fragment.employee;

import com.kaching123.tcr.model.EmployeeModel;

/**
 * Created by mboychenko on 5/11/2017.
 */

public interface EmployeeView {

    void collectDataToModel(EmployeeModel model);
    void setFieldsEnabled(boolean enabled);
    boolean validateView();
    boolean hasChanges(EmployeeModel initModel);
}
