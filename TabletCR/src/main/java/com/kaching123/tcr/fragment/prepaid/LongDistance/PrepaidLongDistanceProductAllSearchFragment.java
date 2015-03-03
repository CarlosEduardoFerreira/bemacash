package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.VectorMasterBiller;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductAllSearchFragment extends PrepaidLongDistanceBaseBodyFragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface {

    @ViewById
    protected TextView countrySearchButton;

    @ViewById
    protected TextView mostPopularButton;
    @FragmentArg
    protected int prepaidMode;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    private PrepaidLongDistanceProductGridViewFragment productGridView;

    LongDistanceSearchCallback callback;

    private final ProductGridViewCallback productGridViewCallback = new ProductGridViewCallback();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_all_search_fragment, container, false);
    }

    @AfterViews
    public void init() {
        updateUI();
        if (prepaidMode != PrepaidHomeFragment.BILLPAYMENT) {
            productGridView = PrepaidLongDistanceProductGridViewFragment_.builder().prepaidMode(prepaidMode).build();
            productGridView.setCallback(productGridViewCallback);
            getChildFragmentManager().beginTransaction().add(R.id.product_grid_view, productGridView).commit();
        } else {
            productGridView = PrepaidLongDistanceProductGridViewFragment_.builder().prepaidMode(prepaidMode).build();
            productGridView.setCallback(productGridViewCallback);
            getChildFragmentManager().beginTransaction().add(R.id.product_grid_view, productGridView).commit();
        }
    }

    private void updateUI() {
        if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE)
            countrySearchButton.setText(getString(R.string.country_search));
        if (prepaidMode == PrepaidHomeFragment.PINLESS)
            countrySearchButton.setText(getString(R.string.carrier_search));
        if (prepaidMode == PrepaidHomeFragment.WIRELESS)
            countrySearchButton.setText(getString(R.string.carrier_search));
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
            countrySearchButton.setText(getString(R.string.category_search));
    }

    public void setCallback(LongDistanceSearchCallback callback) {
        this.callback = callback;
    }

    @Click
    void countrySearchButton() {
        Logger.d("trace--------");
        callback.searchModeSelected(PrepaidLongDistanceActivity.COUNTRY_SEARCH);
    }

    @Click
    void mostPopularButton() {
        callback.searchModeSelected(PrepaidLongDistanceActivity.MOST_POPULAR);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
            callback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_CATEGORY_OR_MOST_POPULAR_BILLER);
    }

    @Override
    public void onBackPressed() {
        callback.popUpFragment();
    }

    public interface LongDistanceSearchCallback {
        void productSelected(WirelessItem item, int searchMode);

        void onBillPaymentItemSelected(BillPaymentItem billPaymentItem, int mode);

        void searchModeSelected(int Mode);

        void searchProductByCountryName(String name);

        void popUpFragment();

        void headMessage(int messageCode);

        void error(String error);
    }

    class ProductGridViewCallback implements PrepaidLongDistanceProductGridViewFragment.ProductFridViewInterface {

        @Override
        public void productSelected(WirelessItem item, int searchMode) {
            callback.productSelected(item, searchMode);
        }

        @Override
        public void billPaymentItemSelected(BillPaymentItem item, int searchMode) {
            callback.onBillPaymentItemSelected(item, searchMode);
        }
    }

}
