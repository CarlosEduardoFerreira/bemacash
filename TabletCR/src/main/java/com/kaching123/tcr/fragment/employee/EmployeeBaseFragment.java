package com.kaching123.tcr.fragment.employee;

import android.content.Context;

import com.kaching123.tcr.activity.BaseEmployeeActivity.EmployeeMode;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.model.EmployeeModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by mboychenko on 5/11/2017.
 */
@EFragment
public abstract class EmployeeBaseFragment extends SuperBaseFragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(getActivity() instanceof EmployeeProvider))
            throw new IllegalStateException("host should implement EmployeeProvider interface");
    }

    protected EmployeeModel getEmployee(){
        return ((EmployeeProvider) getActivity()).getEmployee();
    }
    protected EmployeeMode getMode(){
        return ((EmployeeProvider) getActivity()).getMode();
    }

    @AfterViews
    protected void init(){
        setViews();
        if (getEmployee() != null)
            setEmployee();
    }

    protected void setViews() {}
    protected void setEmployee() {}
}
