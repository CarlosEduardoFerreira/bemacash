package com.kaching123.tcr.fragment;

import android.support.v4.app.DialogFragment;

import com.kaching123.tcr.TcrApplication;

/**
 * Created by pkabakov on 27.12.13.
 */
public class SuperBaseDialogFragment extends DialogFragment {

    protected TcrApplication getApp(){
        if(getActivity() == null)
            throw new IllegalStateException("getActivity returned null");
        return (TcrApplication)getActivity().getApplicationContext();
    }
}
