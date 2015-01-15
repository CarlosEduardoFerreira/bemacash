package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity.PrepaidDistanceBackInterface;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceCountryCharacterGridFragment.CountryCharacterFragmentCallback;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductCountryFlagFragment.CountryFlagFragmentCallbak;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductCountrySearchFragment extends PrepaidLongDistanceBaseBodyFragment implements PrepaidDistanceBackInterface {

    @FragmentArg
    protected int prepaidMode;
    @ViewById
    protected TextView countrySearchButton;

    @ViewById
    protected TextView mostPopularButton;

    @ViewById
    protected TextView viewAllButton;

    @ViewById
    protected FrameLayout coutryCharacterGridView;

    @ViewById
    protected ImageView countryFlag;

    @ViewById
    protected TextView countryName;

    private boolean isProductLayer;

    private String keyWord;

    private PrepaidLongDistanceProductCountryFlagFragment productGridView;

    private PrepaidLongDistanceCountryCharacterGridFragment countryCharacterFragment;

    private PrepaidLongDistanceProductAllSearchFragment.LongDistanceSearchCallback searchCallback;

    private CountryFlagSelectionCallback countryFlagSelectionCallback = new CountryFlagSelectionCallback();

    private final ProductCountryInitSearchCallback productCountryInitSearchCallback = new ProductCountryInitSearchCallback();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_country_search_fragment, container, false);
    }

    @AfterViews
    public void init() {
        initFragment();
        updateUI();
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
            searchCallback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_CATEGORY_OR_MOST_POPULAR_BILLER);
    }

    @UiThread
    protected void show() {
        WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
    }

    @UiThread
    protected void hide() {
        WaitDialogFragment.hide(getActivity());
    }

    private void updateUI() {
        switch (prepaidMode) {
            case PrepaidHomeFragment.WIRELESS:
                countrySearchButton.setText(getString(R.string.carrier_search));
                break;
            case PrepaidHomeFragment.PINLESS:
                countrySearchButton.setText(getString(R.string.carrier_search));
                break;
            case PrepaidHomeFragment.INTERNATIONAL:
                countrySearchButton.setText(getString(R.string.country_search));
                break;
            case PrepaidHomeFragment.LONGDISTANCE:
                countrySearchButton.setText(getString(R.string.country_search));
                searchCallback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_COUNTRY);
                break;
            case PrepaidHomeFragment.BILLPAYMENT:
                countrySearchButton.setText(getString(R.string.category_search));
                break;
            default:
                break;
        }
    }

    private void initFragment() {
        countryCharacterFragment = PrepaidLongDistanceCountryCharacterGridFragment_.builder().build();
        countryCharacterFragment.setCallback(productCountryInitSearchCallback);
        productGridView = PrepaidLongDistanceProductCountryFlagFragment_.builder().prepaidMode(prepaidMode).build();
        productGridView.setCountryFlagFramentCallback(countryFlagSelectionCallback);

        getChildFragmentManager().beginTransaction().add(R.id.coutry_character_grid_view, countryCharacterFragment).add(R.id.coutry_search_product_grid_view, productGridView).commit();

    }

    public void setCallback(PrepaidLongDistanceProductAllSearchFragment.LongDistanceSearchCallback callback) {
        this.searchCallback = callback;
    }

    @Click
    void countrySearchButton() {
        Logger.d("trace--------");

    }

    @Click
    void mostPopularButton() {
        searchCallback.searchModeSelected(PrepaidLongDistanceActivity.MOST_POPULAR);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (countryFlag != null && countryFlag.getVisibility() == View.VISIBLE && coutryCharacterGridView != null &&
                coutryCharacterGridView.getVisibility() == View.GONE)
            isProductLayer = true;
    }

    @Click
    void viewAllButton() {
        searchCallback.searchModeSelected(PrepaidLongDistanceActivity.ALL_SEARCH);
    }

    class CountryFlagSelectionCallback implements CountryFlagFragmentCallbak {

        @Override
        public void productSelected(WirelessItem item, int searchMode) {
            searchCallback.productSelected(item, searchMode);
        }

        @Override
        public void billPaymentItemSelected(BillPaymentItem billPaymentItem, int mode) {
            searchCallback.onBillPaymentItemSelected(billPaymentItem, mode);
        }

        @Override
        public void showCountryGridFragment() {
            showCountryCharacterFragment();
        }

        @Override
        public void hideCountryGridFragment(String name, int drawable) {
            hideCountryCharacterFragment(name, drawable);
        }

        @Override
        public void hideCarrierGridFragment(String name, String url) {
            hideCarrierCharacterFragment(name, url);
        }

        @Override
        public void refreshCountryCharacter(String[] chs) {
            countryCharacterFragment.setCountryInis(chs);
        }

        @Override
        public void onProductLayer(boolean layout_two) {
            isProductLayer = layout_two;
        }

        @Override
        public void billPaymentCategoryChosen(String key) {
            setCountryName(key);
        }

        @Override
        public void headMessage(int mode) {
            searchCallback.headMessage(mode);
        }
    }


    public void startSearchByCountryName(String name) {
        productGridView.startSearch(name);
    }

    class ProductCountryInitSearchCallback implements CountryCharacterFragmentCallback {
        @Override
        public void selectCountryInit(String countryIni) {
            searchCallback.searchProductByCountryName(countryIni);
            setCountryName(countryIni);
        }
    }

    private void setCountryName(String keyword) {
        this.keyWord = keyword;
    }


    private void showCountryCharacterFragment() {
        coutryCharacterGridView.setVisibility(View.VISIBLE);
        countrySearchButton.setVisibility(View.VISIBLE);
        countryFlag.setVisibility(View.GONE);
        countryName.setVisibility(View.GONE);
    }

    private void hideCountryCharacterFragment(String name, int drawable) {
        coutryCharacterGridView.setVisibility(View.GONE);
        countrySearchButton.setVisibility(View.GONE);
        countryFlag.setImageDrawable(getResources().getDrawable(drawable));
        countryFlag.setVisibility(View.VISIBLE);
        countryName.setText(name);
        countryName.setVisibility(View.VISIBLE);
    }

    private void hideCarrierCharacterFragment(String name, String url) {
        coutryCharacterGridView.setVisibility(View.GONE);
        countrySearchButton.setVisibility(View.GONE);
        UrlImageViewHelper.setUrlDrawable(countryFlag, url, R.drawable.operator_default_icon, 60000);
        countryFlag.setVisibility(View.VISIBLE);
        countryName.setText(name);
        countryName.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (isProductLayer) {
            showCountryCharacterFragment();
            startSearchByCountryName(keyWord);
            isProductLayer = false;
            if (prepaidMode != PrepaidHomeFragment.BILLPAYMENT)
                searchCallback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_COUNTRY);

        } else if (keyWord != null) {
            startSearchByCountryName(keyWord = null);
            if (prepaidMode != PrepaidHomeFragment.BILLPAYMENT)
                searchCallback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_COUNTRY);
            countryCharacterFragment.clearSelectedCharacter();
        } else {
            searchCallback.popUpFragment();
        }

    }

}
