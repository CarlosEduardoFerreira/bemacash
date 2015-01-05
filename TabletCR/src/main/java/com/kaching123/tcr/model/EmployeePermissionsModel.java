package com.kaching123.tcr.model;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by pkabakov on 17.12.13.
 */
public class EmployeePermissionsModel implements Serializable {

    public EmployeeModel employee;
    public Set<Permission> permissions;

    public EmployeePermissionsModel(EmployeeModel employee, Set<Permission> permissions) {
        this.employee = employee;
        this.permissions = permissions;
    }
}
