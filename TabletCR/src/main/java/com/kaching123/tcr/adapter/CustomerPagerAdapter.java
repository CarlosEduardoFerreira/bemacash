package com.kaching123.tcr.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kaching123.tcr.fragment.customer.CustomerBaseFragment;
import com.kaching123.tcr.fragment.customer.CustomerGeneralInfoFragment_;
import com.kaching123.tcr.fragment.customer.CustomerHistoryFragment_;
import com.kaching123.tcr.fragment.customer.CustomerNotesFragment_;

/**
 * Created by vkompaniets on 24.06.2016.
 */
public class CustomerPagerAdapter extends FragmentPagerAdapter {

    private String[] pageTitles;
    private CustomerBaseFragment[] fragments;

    public CustomerPagerAdapter(FragmentManager fm, String[] pageTitles) {
        super(fm);
        this.pageTitles = pageTitles;
        this.fragments = new CustomerBaseFragment[]{new CustomerGeneralInfoFragment_(), new CustomerNotesFragment_(), new CustomerHistoryFragment_()};
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
