package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoProductRatesRequest;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductFragment extends Fragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface {

    public static final String ARG_ICON_URL = "ARG_ICON_URL";
    public static final String ARG_PRODUCT_NAME = "ARG_PRODUCT_NAME";
    @FragmentArg
    protected WirelessItem chosenCategory;
    @FragmentArg
    protected int prepaidMode;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected long transactionId;
    @FragmentArg
    protected int searchMode;

    private PrepaidLongDistanceProductInfoMenuFragment productInfoMenuFragment;
    private ProductFragmentCallback productFragmentCallback;
    private final ProductInfoMenuFragmentCallback productInfoMenuFragmentCallback = new ProductInfoMenuFragmentCallback();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateUI() {
        switch (prepaidMode) {
            case PrepaidHomeFragment.WIRELESS:
                break;
            default:
                break;
        }
    }

    @AfterViews
    public void init() {

        initFragment();
        updateUI();

    }

    private void initFragment() {
        DoProductRatesRequest doProductRatesRequest = composeProductRatesRequest();
        productInfoMenuFragment = PrepaidLongDistanceProductInfoMenuFragment_.builder().chosenCategory(chosenCategory).prepaidMode(prepaidMode).doProductRatesRequest(doProductRatesRequest).build();
        productInfoMenuFragment.setCallback(productInfoMenuFragmentCallback);
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                .add(R.id.product_info, productInfoMenuFragment).commit();
    }

    @Override
    public void onBackPressed() {
        productFragmentCallback.popUpFragment(searchMode);
    }


    class ProductInfoMenuFragmentCallback implements PrepaidLongDistanceProductInfoMenuFragment.LongDistanceProductInfoMenuInterface {
        @Override
        public void menuSelected(int position) {
            productFragmentCallback.menuSelected(position);
        }

        @Override
        public void conditionSelected(BigDecimal amount, String phoneNumber) {
            productFragmentCallback.conditionSelected(amount, phoneNumber);
        }

        @Override
        public void headMessage(int errorCode) {
            productFragmentCallback.headMessage(errorCode);
        }
    }

    public void setCallback(ProductFragmentCallback callback) {
        this.productFragmentCallback = callback;
    }

    public interface ProductFragmentCallback extends PrepaidLongDistanceProductInfoMenuFragment.LongDistanceProductInfoMenuInterface {
        void popUpFragment(int searchMode);
        void additionalDataRequired(Category chosenCategory,
                                    BillPaymentItem billPaymentItem,
                                    PaymentOption chosenOption,
                                    String cashierId,
                                    PrepaidUser user,
                                    String transactionMode,
                                    BigDecimal fee, BigDecimal chosenAmount,BigDecimal total,
                                    BillerLoadRecord billerData,
                                    String accountNumber);
        void error(String message);
    }

    private DoProductRatesRequest composeProductRatesRequest() {
        DoProductRatesRequest request = new DoProductRatesRequest();
        request.mID = String.valueOf(this.user.getMid());
        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.productMainCode = chosenCategory.code;
        request.transactionId = this.transactionId;
        return request;
    }


}
