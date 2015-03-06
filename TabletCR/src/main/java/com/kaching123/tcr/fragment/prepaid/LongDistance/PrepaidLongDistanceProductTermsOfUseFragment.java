package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductTermsOfUseFragment extends PrepaidLongDistanceBaseBodyFragment {

    @FragmentArg
    protected WirelessItem chosenCategory;
    @ViewById
    protected TextView text;

    @ViewById
    protected LinearLayout closeLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_terms_of_use_fragment, container, false);
    }

    public void setCallback(closeLayoutCallback callback) {
        this.callback = callback;
    }

    @Click
    void closeLayout() {
        callback.pageSelected(PrepaidLongDistanceProductInfoMenuFragment.TERMS_OF_USE);
    }

    @AfterViews
    public void init() {
        text.setText(chosenCategory.TermsAndConditions);
    }


}
