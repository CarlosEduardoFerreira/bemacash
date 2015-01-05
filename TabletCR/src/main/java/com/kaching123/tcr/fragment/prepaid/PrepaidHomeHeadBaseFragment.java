package com.kaching123.tcr.fragment.prepaid;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidProcessorActivity;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidHomeHeadBaseFragment extends Fragment {

    public final static String HEAD_INSTRUCTION = "HEAD_INSTRUCTION";
    public final static String SELECT_AMOUNT = "SELECT_AMOUNT";
    public final static String SELECT_PRODUCT = "SELECT_PRODUCT";

    @ViewById
    protected TextView homeButton;


    @Click
    void homeButton()
    {
        PrepaidProcessorActivity.start(getActivity());
        Logger.d("Trace homeButton click");
    }


}
