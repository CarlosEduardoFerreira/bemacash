package com.kaching123.tcr.activity;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.employee.EmployeeAttendanceFragment;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by gdubina on 05/02/14.
 */
@EActivity(R.layout.employee_attendance_activity)
public class EmployeeTimeAttendanceActivity extends SuperBaseActivity{

    @Extra
    protected String employeeGuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().add(R.id.container, EmployeeAttendanceFragment.instance(employeeGuid)).commit();

    }

    public static void start(Context context, String employeeGuid){
        EmployeeTimeAttendanceActivity_.intent(context).employeeGuid(employeeGuid).start();
    }

}
