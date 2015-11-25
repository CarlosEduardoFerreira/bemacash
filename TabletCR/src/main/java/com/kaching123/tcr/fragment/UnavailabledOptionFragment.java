package com.kaching123.tcr.fragment;

import android.support.v4.app.Fragment;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EFragment;

/**
 * Created by alboyko on 25.11.2015.
 */
@EFragment(R.layout.unavailable_option_fragment)
public class UnavailabledOptionFragment extends SuperBaseFragment {
    public static Fragment instance() {
        return UnavailabledOptionFragment_.builder().build();
    }
}