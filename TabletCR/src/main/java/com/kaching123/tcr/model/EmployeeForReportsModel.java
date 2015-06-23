package com.kaching123.tcr.model;

import android.database.Cursor;

import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import java.io.Serializable;

/**
 * Created by gdubina on 08/11/13.
 */
public class EmployeeForReportsModel implements Serializable {

    public final String guid;
    public String firstName;
    public String lastName;
    public String login;


    public EmployeeForReportsModel(String guid, String firstName, String lastName, String login) {
        this.guid = guid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
    }


    public EmployeeForReportsModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(EmployeeTable.GUID)),
                c.getString(c.getColumnIndex(EmployeeTable.FIRST_NAME)),
                c.getString(c.getColumnIndex(EmployeeTable.LAST_NAME)),
                c.getString(c.getColumnIndex(EmployeeTable.LOGIN))
        );
    }

    public String fullName() {
        return UiHelper.concatFullname(firstName, lastName);
    }


}
