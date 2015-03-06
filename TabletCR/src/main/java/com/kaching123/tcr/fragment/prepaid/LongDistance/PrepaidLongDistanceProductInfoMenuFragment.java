package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoProductRatesRequest;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductInfoMenuFragment extends Fragment {

    static public final int SELECT_AMOUNT = 0;
    static public final int VIEW_RATE = 1;
    static public final int TERMS_OF_USE = 2;
    static public final int ACCESS_NUMBERS = 3;
    static public final int SELECT_PRODUCT = 4;
    static public final int ENTER_ADDITIONAL_INFO = 5;

    @ViewById
    protected LinearLayout viewRatesLine;
    @ViewById
    protected LinearLayout termsOfUseLine;
    @ViewById
    protected LinearLayout accessNumbersLine;

    @FragmentArg
    protected WirelessItem chosenCategory;
    @FragmentArg
    protected int prepaidMode;
    @FragmentArg
    protected DoProductRatesRequest doProductRatesRequest;

    @ViewById
    protected ImageView imgViewRatesClose;
    @ViewById
    protected ImageView imgTermsOfUseClose;
    @ViewById
    protected ImageView imgAccessNumbersClose;
    @ViewById
    protected ImageView imgViewRatesOpen;
    @ViewById
    protected ImageView imgTermsOfUseOpen;
    @ViewById
    protected ImageView imgAccessNumbersOpen;

    private PrepaidLongDistanceProductAmountFragment productAmountFragment;
    private PrepaidWirelessProductAmountFragment wirelessProductAmountFragment;

    private PrepaidLongDistanceProductViewRatesFragment productViewRatesFragment;
    private PrepaidLongDistanceProductTermsOfUseFragment productTermsOfUseFragment;
    private PrepaidLongDistanceProductAccessNumbersFragment productAccessNumbersFragment;

    private ProductionAmountSelectedCallback productionAmountSelectedCallback = new ProductionAmountSelectedCallback();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_info_menu, container, false);
    }

    @AfterViews
    protected void init() {
        initFragment();
    }


    private void initFragment() {

        initProductMainFragment();
        initViewRatesFragment();
        initTermsOfUseFragment();
        initAccessNumbersFragment();

        getFragmentManager().beginTransaction()
                .add(R.id.product_details, prepaidMode == PrepaidHomeFragment.LONGDISTANCE ? productAmountFragment : wirelessProductAmountFragment)
                .add(R.id.product_details, productViewRatesFragment)
                .add(R.id.product_details, productTermsOfUseFragment)
                .add(R.id.product_details, productAccessNumbersFragment)
                .hide(productViewRatesFragment)
                .hide(productTermsOfUseFragment)
                .hide(productAccessNumbersFragment)
                .commit();

    }

    private void initProductMainFragment() {
        if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE) {
            productAmountFragment = PrepaidLongDistanceProductAmountFragment_.builder().chosenCategory(chosenCategory).build();
            productAmountFragment.setCallback(productionAmountSelectedCallback);
        } else {
            wirelessProductAmountFragment = PrepaidWirelessProductAmountFragment_.builder().chosenCategory(chosenCategory).build();
            wirelessProductAmountFragment.setCallback(productionAmountSelectedCallback);
        }
    }

    private void initViewRatesFragment() {
        productViewRatesFragment = PrepaidLongDistanceProductViewRatesFragment_.builder().doProductRatesRequest(doProductRatesRequest).build();
        productViewRatesFragment.setCallback(closeCallback);
    }

    private void initTermsOfUseFragment() {
        productTermsOfUseFragment = PrepaidLongDistanceProductTermsOfUseFragment_.builder().chosenCategory(chosenCategory).build();
        productTermsOfUseFragment.setCallback(closeCallback);
    }

    private void initAccessNumbersFragment() {
        productAccessNumbersFragment = PrepaidLongDistanceProductAccessNumbersFragment_.builder().chosenCategory(chosenCategory).build();
        productAccessNumbersFragment.setCallback(closeCallback);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    private Broker getBroker(int prepaidMode)
    {
        switch (prepaidMode)
        {
            case PrepaidHomeFragment.BILLPAYMENT:
                return Broker.BILL_PAYMENT;
            case PrepaidHomeFragment.INTERNATIONAL:
                return Broker.INTERNATIONAL_TOPUP;
            case PrepaidHomeFragment.LONGDISTANCE:
                return Broker.LONG_DISTANCE;
            case PrepaidHomeFragment.SUNPASS:
                return Broker.SUNPASS;
            case PrepaidHomeFragment.WIRELESS:
                return Broker.WIRELESS_RECHARGE;
            case PrepaidHomeFragment.PINLESS:
                return Broker.PINLESS;
        }
        return Broker.PINLESS;
    }
    class ProductionAmountSelectedCallback implements PrepaidLongDistanceProductAmountFragment.LongDistanceProductAmount {

        @Override
        public void conditionSelected(BigDecimal amount, String phoneNumber, BigDecimal feeAmount) {
            longDistanceProductInfoMenuInterface.conditionSelected(amount, phoneNumber, feeAmount, getBroker(prepaidMode));
        }

        @Override
        public void headMessage(int errorCode) {
            longDistanceProductInfoMenuInterface.headMessage(errorCode);
        }
    }

    PrepaidLongDistanceBaseBodyFragment.closeLayoutCallback closeCallback = new PrepaidLongDistanceBaseBodyFragment.closeLayoutCallback() {

        @Override
        public void pageSelected(int position) {
            switch (position) {
                case VIEW_RATE:
                    fragmentOperations(true, VIEW_RATE, viewRatesLine, imgViewRatesOpen, imgViewRatesClose);
                    break;
                case TERMS_OF_USE:
                    fragmentOperations(true, TERMS_OF_USE, termsOfUseLine, imgTermsOfUseOpen, imgTermsOfUseClose);
                    break;
                case ACCESS_NUMBERS:
                    fragmentOperations(true, ACCESS_NUMBERS, accessNumbersLine, imgAccessNumbersOpen, imgAccessNumbersClose);
                    break;
            }
        }
    };

    @Click
    void viewRatesLine() {
        hideFragments();
        closeOtherFragment(VIEW_RATE);
        fragmentOperations(isOpened(imgViewRatesClose), VIEW_RATE, viewRatesLine, imgViewRatesOpen, imgViewRatesClose);
        longDistanceProductInfoMenuInterface.menuSelected(VIEW_RATE);
    }


    @Click
    void termsOfUseLine() {
        hideFragments();
        closeOtherFragment(TERMS_OF_USE);
        fragmentOperations(isOpened(imgTermsOfUseClose), TERMS_OF_USE, termsOfUseLine, imgTermsOfUseOpen, imgTermsOfUseClose);
        longDistanceProductInfoMenuInterface.menuSelected(TERMS_OF_USE);
    }

    @Click
    void accessNumbersLine() {
        hideFragments();
        closeOtherFragment(ACCESS_NUMBERS);
        fragmentOperations(isOpened(imgAccessNumbersClose), ACCESS_NUMBERS, accessNumbersLine, imgAccessNumbersOpen, imgAccessNumbersClose);
        longDistanceProductInfoMenuInterface.menuSelected(ACCESS_NUMBERS);
    }

    private void closeOtherFragment(int position) {

        switch (position) {
            case VIEW_RATE:
                if (isOpened(imgTermsOfUseClose)) {
                    imgTermsOfUseClose.setVisibility(View.VISIBLE);
                    imgTermsOfUseOpen.setVisibility(View.GONE);
                    termsOfUseLine.setBackgroundColor(Color.TRANSPARENT);

                }
                if (isOpened(imgAccessNumbersClose)) {
                    imgAccessNumbersClose.setVisibility(View.VISIBLE);
                    imgAccessNumbersOpen.setVisibility(View.GONE);
                    accessNumbersLine.setBackgroundColor(Color.TRANSPARENT);
                }
                break;
            case TERMS_OF_USE:
                if (isOpened(imgViewRatesClose)) {
                    imgViewRatesClose.setVisibility(View.VISIBLE);
                    imgViewRatesOpen.setVisibility(View.GONE);
                    viewRatesLine.setBackgroundColor(Color.TRANSPARENT);
                }
                if (isOpened(imgAccessNumbersClose)) {
                    imgAccessNumbersClose.setVisibility(View.VISIBLE);
                    imgAccessNumbersOpen.setVisibility(View.GONE);
                    accessNumbersLine.setBackgroundColor(Color.TRANSPARENT);
                }
                break;
            case ACCESS_NUMBERS:
                if (isOpened(imgTermsOfUseClose)) {
                    imgTermsOfUseClose.setVisibility(View.VISIBLE);
                    imgTermsOfUseOpen.setVisibility(View.GONE);
                    termsOfUseLine.setBackgroundColor(Color.TRANSPARENT);
                }
                if (isOpened(imgViewRatesClose)) {
                    imgViewRatesClose.setVisibility(View.VISIBLE);
                    imgViewRatesOpen.setVisibility(View.GONE);
                    viewRatesLine.setBackgroundColor(Color.TRANSPARENT);
                }
                break;
        }
    }

    private void hideFragments() {
        getFragmentManager().beginTransaction()
                .hide(productTermsOfUseFragment)
                .hide(productAccessNumbersFragment)
                .hide(productViewRatesFragment)
                .hide(prepaidMode == PrepaidHomeFragment.LONGDISTANCE ? productAmountFragment : wirelessProductAmountFragment)
                .commit();
    }

    private void updateLine(boolean open, LinearLayout line, ImageView openView, ImageView closeView) {
        if (open) {
            line.setBackgroundColor(Color.TRANSPARENT);
            openView.setVisibility(View.GONE);
            closeView.setVisibility(View.VISIBLE);
        } else {
            line.setBackgroundColor(getResources().getColor(R.color.prepaid_instruction_blue_bg));
            closeView.setVisibility(View.GONE);
            openView.setVisibility(View.VISIBLE);
        }
    }

    private void fragmentOperations(boolean open, int position, LinearLayout line, ImageView closeView, ImageView openView) {
        switch (position) {
            case VIEW_RATE:
                updateFragment(productViewRatesFragment, open);
                break;
            case TERMS_OF_USE:
                updateFragment(productTermsOfUseFragment, open);
                break;
            case ACCESS_NUMBERS:
                updateFragment(productAccessNumbersFragment, open);
                break;
        }
        updateLine(open, line, closeView, openView);
    }

    private void updateFragment(Fragment fragment, boolean open) {
        if (open) {
            getFragmentManager().beginTransaction().hide(fragment).commit();
            getFragmentManager().beginTransaction().show(prepaidMode == PrepaidHomeFragment.LONGDISTANCE ? productAmountFragment : wirelessProductAmountFragment).commit();
        } else {
            getFragmentManager().beginTransaction().hide(prepaidMode == PrepaidHomeFragment.LONGDISTANCE ? productAmountFragment : wirelessProductAmountFragment).commit();
            getFragmentManager().beginTransaction().show(fragment).commit();
        }
    }

    private boolean isOpened(ImageView view) {
        boolean open;
        if (view.getVisibility() == View.VISIBLE)
            open = false;
        else
            open = true;

        return open;
    }


    private LongDistanceProductInfoMenuInterface longDistanceProductInfoMenuInterface;

    public void setCallback(LongDistanceProductInfoMenuInterface callback) {
        this.longDistanceProductInfoMenuInterface = callback;
    }


    public interface LongDistanceProductInfoMenuInterface {
        void menuSelected(int position);

        void conditionSelected(BigDecimal amount, String phoneNumber, BigDecimal feeAmount, Broker broker);

        void headMessage(int errorCode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroy();
        if (isAdded())
            longDistanceProductInfoMenuInterface.menuSelected(SELECT_PRODUCT);
    }
}
