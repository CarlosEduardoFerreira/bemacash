package com.kaching123.tcr.fragment;

import android.support.v4.app.Fragment;

import com.kaching123.tcr.TcrApplication;

/**
 * Created by gdubina on 04.12.13.
 */
public class SuperBaseFragment extends Fragment{

    protected TcrApplication getApp(){
        if(getActivity() == null)
            throw new IllegalStateException("getActivity returned null");
        return (TcrApplication)getActivity().getApplicationContext();
    }
}
