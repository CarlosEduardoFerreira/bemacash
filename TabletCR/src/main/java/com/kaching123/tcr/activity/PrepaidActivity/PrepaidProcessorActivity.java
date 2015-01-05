package com.kaching123.tcr.activity.PrepaidActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OnActivityResult;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment.prepaidType;

/**
 * Created by teli.yin on 10/28/2014.
 */
@EActivity(R.layout.prepaid_processor_activity)
public class PrepaidProcessorActivity extends SuperBaseActivity implements prepaidType {

    public static final int REQUEST_CODE = 1;
    public static String TRANSACTION_COMPLETE = "TRANSACTION_COMPLETE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static void start(Context context) {
        PrepaidProcessorActivity_.intent(context).start();
    }

    @AfterViews
    public void init() {
    }


    @Override
    public void typeSelected(int type) {
        switch (type) {
            case PrepaidHomeFragment.ACTIVATIONCENTER:

                break;
            case PrepaidHomeFragment.LONGDISTANCE:
                switch2LongDistance();
                break;
            case PrepaidHomeFragment.WIRELESS:
                switch2WirelessRecharge();
                break;
            case PrepaidHomeFragment.PINLESS:
                switch2PinlessRecharge();
                break;
            case PrepaidHomeFragment.INTERNATIONAL:
                switch2InternationalRecharge();
                break;
            case PrepaidHomeFragment.BILLPAYMENT:
                switch2BillPayment();
                break;
            case PrepaidHomeFragment.SUNPASS:
                switch2SunPass();
                break;
            default:
                Toast.makeText(PrepaidProcessorActivity.this, "This part is still in develop", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void switch2LongDistance() {
        PrepaidLongDistanceActivity.start(PrepaidProcessorActivity.this, PrepaidHomeFragment.LONGDISTANCE);
    }

    private void switch2WirelessRecharge() {
        PrepaidLongDistanceActivity.start(PrepaidProcessorActivity.this, PrepaidHomeFragment.WIRELESS);
    }

    private void switch2PinlessRecharge() {
        PrepaidLongDistanceActivity.start(PrepaidProcessorActivity.this, PrepaidHomeFragment.PINLESS);
    }

    private void switch2InternationalRecharge() {
        PrepaidLongDistanceActivity.start(PrepaidProcessorActivity.this, PrepaidHomeFragment.INTERNATIONAL);
    }

    private void switch2BillPayment() {
        PrepaidLongDistanceActivity.start(PrepaidProcessorActivity.this, PrepaidHomeFragment.BILLPAYMENT);
    }

    private void switch2SunPass() {
        PrepaidSunPassActivity.start(PrepaidProcessorActivity.this);
    }


    @OnActivityResult(REQUEST_CODE)
    void onResult(int resultCode, Intent intent) {
        if (resultCode == REQUEST_CODE)
            if (intent != null && intent.getBooleanExtra(TRANSACTION_COMPLETE, false))
                finish();
    }
}
