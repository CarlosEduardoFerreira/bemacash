package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeHeadBaseFragment;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceHeadFragment extends PrepaidHomeHeadBaseFragment {

    @ViewById
    protected TextView headInstruction;
    @ViewById
    protected TextView homeButton;
    @ViewById
    protected ImageView backButton;
    @ViewById
    protected TextView headPrepaidName;

    public static final int SELECT_COUNTRY = 19;
    public static final int SELECT_BILLER = 20;
    public static final int SELECT_CATEGORY = 21;
    public static final int ENTER_ADDITIONAL_DATA = 6;
    public static final int SELECT_CATEGORY_OR_MOST_POPULAR_BILLER = 7;
    public static final int PURCHASE_SUMMARY = 9;
    public static final int ENTER_ACCOUNT_AND_AMOUNT = 8;
    public static final int AMOUNT_ZERO_ERROR = 10;
    public static final int PHONE_NUMBER_NULL_ERROR = 11;
    public static final int PHONE_NUMBER_NULL_AND_AMOUNT_ZERO_ERROR = 12;
    public static final int ACCOUNT_NUMBER_NULL = 13;
    public static final int CONFIRM_ACCOUNT_NUMBER_NULL = 14;
    public static final int ACCOUNT_NUMBER_NOT_EQUAL = 15;
    public static final int FEE_NULL = 16;
    public static final int INVALID_FIELDS = 17;
    public static final int AMOUNT_MINIMUN_ERROR = 18;

    @FragmentArg
    protected int prepaidMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_head_fragment, container, false);
    }

    @AfterViews
    public void init() {
        updateUI();
    }

    private void updateUI() {
        switch (prepaidMode) {
            case PrepaidHomeFragment.LONGDISTANCE:
                headPrepaidName.setText(getString(R.string.prepaid_head_long_distance_text));
                break;
            case PrepaidHomeFragment.WIRELESS:
                headPrepaidName.setText(getString(R.string.prepaid_head_wireless_text));
                break;
            case PrepaidHomeFragment.PINLESS:
                headPrepaidName.setText(getString(R.string.prepaid_head_pinless_text));
                break;
            case PrepaidHomeFragment.INTERNATIONAL:
                headPrepaidName.setText(getString(R.string.prepaid_head_international_text));
                break;
            case PrepaidHomeFragment.BILLPAYMENT:
                headPrepaidName.setText(getString(R.string.prepaid_head_bill_payment_text));
                break;
            default:
                break;
        }

    }

    private LongDistanceHeadInterface longDistanceHeadInterface;

    public void setCallback(LongDistanceHeadInterface callback) {
        this.longDistanceHeadInterface = callback;
    }

    @Click
    void homeButton() {
        longDistanceHeadInterface.onHomePressed();
    }

    @Click
    void backButton() {
        longDistanceHeadInterface.onBackButtonPressed();
    }

    public interface LongDistanceHeadInterface {
        void onHomePressed();

        void onBackButtonPressed();
    }

    public void setInstroTextView(int position) {
        if (isAdded()) {
            if (position < 10 || position > 18)
                headInstruction.setTextColor(getResources().getColor(R.color.gray_dark));
            else
                headInstruction.setTextColor(Color.RED);
            switch (position) {
                case PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT:
                    if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE)
                        headInstruction.setText(getResources().getString(R.string.select_amount));
                    else
                        headInstruction.setText(getResources().getString(R.string.prepaid_wireless_amount_and_phone_number_hint));
                    break;
                case PrepaidLongDistanceProductInfoMenuFragment.VIEW_RATE:
                    headInstruction.setText(getResources().getString(R.string.view_rates));
                    break;
                case PrepaidLongDistanceProductInfoMenuFragment.TERMS_OF_USE:
                    headInstruction.setText(getResources().getString(R.string.terms_of_use));
                    break;
                case PrepaidLongDistanceProductInfoMenuFragment.ACCESS_NUMBERS:
                    headInstruction.setText(getResources().getString(R.string.access_numbers));
                    break;
                case PrepaidLongDistanceProductInfoMenuFragment.SELECT_PRODUCT:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_select_the_products));
                    break;
                case PrepaidLongDistanceProductInfoMenuFragment.ENTER_ADDITIONAL_INFO:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_enter_additional_info));
                    break;
                case ENTER_ACCOUNT_AND_AMOUNT:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_enter_account_and_amount));
                    break;
                case SELECT_COUNTRY:
                    headInstruction.setText(getResources().getString(R.string.prepaid_select_country));
                    break;
                case SELECT_BILLER:
                    headInstruction.setText(getResources().getString(R.string.prepaid_select_biller));
                    break;
                case SELECT_CATEGORY:
                    headInstruction.setText(getResources().getString(R.string.prepaid_select_category));
                    break;
                case ENTER_ADDITIONAL_DATA:
                    headInstruction.setText(getResources().getString(R.string.enter_additional_data));
                    break;
                case SELECT_CATEGORY_OR_MOST_POPULAR_BILLER:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_select_category_or_most_popular_biller));
                    break;
                case PURCHASE_SUMMARY:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_purchase_saummary));
                    break;
                case AMOUNT_ZERO_ERROR:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_amount_zero_error));
                    break;
                case PHONE_NUMBER_NULL_ERROR:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_phone_number_null_error));
                    break;
                case PHONE_NUMBER_NULL_AND_AMOUNT_ZERO_ERROR:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_phone_number_null_and_amount_zero_error));
                    break;
                case ACCOUNT_NUMBER_NULL:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_account_number_null_error));
                    break;
                case CONFIRM_ACCOUNT_NUMBER_NULL:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_confirma_account_number_null_error));
                    break;
                case ACCOUNT_NUMBER_NOT_EQUAL:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_account_number_not_equal_error));
                    break;
                case FEE_NULL:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_fee_null_error));
                    break;
                case INVALID_FIELDS:
                    headInstruction.setText(getResources().getString(R.string.invalid_fields));
                    break;
                case AMOUNT_MINIMUN_ERROR:
                    headInstruction.setText(getResources().getString(R.string.amount_minimum_error));
                    break;
            }
        }

    }

}
