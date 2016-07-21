package com.kaching123.tcr.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kaching123.tcr.fragment.customer.CustomerBaseFragment;

/**
 * Created by vkompaniets on 21.07.2016.
 */
public class ItemPagerAdapter extends FragmentPagerAdapter {

    private String[] pageTitles;
    private CustomerBaseFragment[] fragments;

    public ItemPagerAdapter(FragmentManager fm, String[] pageTitles) {
        super(fm);
        this.pageTitles = pageTitles;
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
