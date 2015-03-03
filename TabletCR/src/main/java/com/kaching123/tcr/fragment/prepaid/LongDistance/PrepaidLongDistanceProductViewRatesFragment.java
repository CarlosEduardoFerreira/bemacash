package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.DoProductRatesCommand;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.prepaid.utilities.ViewRatesAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoProductRatesRequest;
import com.kaching123.tcr.websvc.api.prepaid.ProductRatesResponse;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductViewRatesFragment extends PrepaidLongDistanceBaseBodyFragment {

    @FragmentArg
    protected DoProductRatesRequest doProductRatesRequest;

    @ViewById
    protected TextView text;

    @ViewById
    protected ListView listview;
    @ViewById
    protected LinearLayout closeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_rates_fragment, container, false);
    }

    public void setCallback(closeLayoutCallback callback) {
        this.callback = callback;
    }

    @Click
    void closeLayout() {
        callback.pageSelected(PrepaidLongDistanceProductInfoMenuFragment.VIEW_RATE);
    }

    @AfterViews
    public void init() {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        DoProductRatesCommand.start(getActivity(), productRatesCommandCallback, doProductRatesRequest);
    }

    private DoProductRatesCommand.ProductRatesCommandCallback productRatesCommandCallback = new DoProductRatesCommand.ProductRatesCommandCallback() {

        @Override
        protected void handleSuccess(ProductRatesResponse response) {
            WaitDialogFragment.hide(getActivity());
            listview.setAdapter(new ViewRatesAdapter(getActivity(), response.rates));
        }

        @Override
        protected void handleFailure(ProductRatesResponse result) {
            WaitDialogFragment.hide(getActivity());

        }
    };


}
