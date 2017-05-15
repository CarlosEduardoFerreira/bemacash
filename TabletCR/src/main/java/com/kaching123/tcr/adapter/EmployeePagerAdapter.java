package com.kaching123.tcr.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kaching123.tcr.fragment.employee.EmployeeBaseFragment;
import com.kaching123.tcr.fragment.employee.EmployeeContactInfoFragment_;
import com.kaching123.tcr.fragment.employee.EmployeeJobRolesFragment_;
import com.kaching123.tcr.fragment.employee.EmployeePermissionFragment_;
import com.kaching123.tcr.fragment.employee.EmployeeSalaryInfoFragment_;

/**
 * Created by mboychenko on 5/12/2017.
 */

public class EmployeePagerAdapter extends FragmentPagerAdapter {
    private String[] pageTitles;
    private EmployeeBaseFragment[] fragments;

    public EmployeePagerAdapter(FragmentManager fm, String[] pageTitles) {
        super(fm);
        this.pageTitles = pageTitles;
        this.fragments = new EmployeeBaseFragment[]{new EmployeeContactInfoFragment_(), new EmployeePermissionFragment_(), new EmployeeSalaryInfoFragment_(), new EmployeeJobRolesFragment_()};
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return pageTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }
}
