package com.kaching123.tcr.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kaching123.tcr.fragment.customer.CustomerPersonalInfoFragment_;

/**
 * Created by vkompaniets on 24.06.2016.
 */
public class CustomerPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private String[] pageTitles;

    public CustomerPagerAdapter(Context context, FragmentManager fm, String[] pageTitles) {
        super(fm);
        this.context = context;
        this.pageTitles = pageTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return new CustomerPersonalInfoFragment_();
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
