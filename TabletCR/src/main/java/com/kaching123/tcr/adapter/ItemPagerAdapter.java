package com.kaching123.tcr.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kaching123.tcr.fragment.item.ItemBaseFragment;

/**
 * Created by vkompaniets on 21.07.2016.
 */
public class ItemPagerAdapter extends FragmentPagerAdapter {

    private String[] pageTitles;
    private ItemBaseFragment[] fragments;

    public ItemPagerAdapter(FragmentManager fm, ItemBaseFragment[] fragments, String[] pageTitles) {
        super(fm);
        this.pageTitles = pageTitles;
        this.fragments = fragments;
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
