package com.kaching123.tcr.fragment.prepaid;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
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
        PrepaidProcessorActivity.start(getActivity(),getBillpaymentActivate(), getSunpassActivate());
        Logger.d("Trace homeButton click");
    }

    private boolean getBillpaymentActivate()
    {
        return TcrApplication.get().getBillPaymentActivated();
    }

    private boolean getSunpassActivate()
    {
        return TcrApplication.get().getSunpassActivated();
    }
}
