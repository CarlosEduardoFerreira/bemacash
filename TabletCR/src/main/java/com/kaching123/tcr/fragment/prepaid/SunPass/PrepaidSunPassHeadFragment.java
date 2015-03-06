package com.kaching123.tcr.fragment.prepaid.SunPass;

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
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeHeadBaseFragment;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidSunPassHeadFragment extends PrepaidHomeHeadBaseFragment {

    @ViewById
    protected TextView headInstruction;
    @ViewById
    protected TextView homeButton;
    @ViewById
    protected ImageView backButton;
    @ViewById
    protected TextView headPrepaidName;



    public static final int SELECT_YOUR_PAYMENT_METHOD = 5;
    public static final int SELECT_SUNPASS_CATEGORY = 4;
    public static final int ENTER_DOCUMENT_ID_AND_LICENSE_PLATE_NUMBER = 6;
    public static final int PURCHASE_SUMMARY = 9;
    public static final int SELECT_AMOUNT = 8;
    public static final int SELECT_TRANSPONDER_NUMBER = 7;
    public static final int AMOUNT_ZERO_ERROR = 10;
    public static final int DOCUMENT_ID_NULL = 11;
    public static final int LICENSE_PLATE_NUMBER_NULL = 12;
    public static final int CHOOSE_DOCUMENT_PAYMENT_METHOD = 19;
    public static final int ACCOUNT_NUMBER_NULL = 13;
    public static final int CONFIRM_ACCOUNT_NUMBER_NULL = 14;
    public static final int ACCOUNT_NUMBER_NOT_EQUAL = 15;
    public static final int FEE_NULL = 16;
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
        headPrepaidName.setText(getString(R.string.prepaid_head_sunpass_text));
        headInstruction.setText(getResources().getString(R.string.sunpass_select_category));

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
            if (position < 10)
                headInstruction.setTextColor(getResources().getColor(R.color.gray_dark));
            else
                headInstruction.setTextColor(Color.RED);
            switch (position) {
                case SELECT_SUNPASS_CATEGORY:
                    headInstruction.setText(getResources().getString(R.string.sunpass_select_category));
                    break;
                case SELECT_YOUR_PAYMENT_METHOD:
                    headInstruction.setText(getResources().getString(R.string.sunopass_select_your_payment_method));
                    break;
                case ENTER_DOCUMENT_ID_AND_LICENSE_PLATE_NUMBER:
                    headInstruction.setText(getResources().getString(R.string.sunopass_enter_document_id_and_license_plate_number));
                    break;
                case SELECT_TRANSPONDER_NUMBER:
                    headInstruction.setText(getResources().getString(R.string.prepaid_sunpass_select_transponder_number));
                    break;
                case SELECT_AMOUNT:
                    headInstruction.setText(getResources().getString(R.string.select_amount));
                    break;
                case PURCHASE_SUMMARY:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_purchase_saummary));
                    break;
                case AMOUNT_ZERO_ERROR:
                    headInstruction.setText(getResources().getString(R.string.prepaid_head_amount_zero_error));
                    break;

                case CHOOSE_DOCUMENT_PAYMENT_METHOD:
                    headInstruction.setText(getResources().getString(R.string.sunopass_choose_document_payment_method));
                    break;
                case LICENSE_PLATE_NUMBER_NULL:
                    headInstruction.setText(getResources().getString(R.string.sunopass_license_plate_number_null));
                    break;
                case DOCUMENT_ID_NULL:
                    headInstruction.setText(getResources().getString(R.string.sunopass_document_id_null));
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
                case AMOUNT_MINIMUN_ERROR:
                    headInstruction.setText(getResources().getString(R.string.amount_minimum_error));
                    break;
            }
        }

    }

}
