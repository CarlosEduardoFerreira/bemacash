package com.kaching123.tcr.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

/**
 * Created by mboychenko on 5/29/2017.
 */

public interface IRegisterFragmentsImplementation {
    Fragment getSearchResultFragment();
    ListFragment getOrderItemListFragment();
}
