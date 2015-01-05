package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidBillPaymentProductComfirmationFragment extends PrepaidLongDistanceBaseBodyFragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface {

    @FragmentArg
    protected Category chosenCategory;
    @FragmentArg
    protected BillPaymentItem chosenBillPaymentItem;
    @FragmentArg
    protected PaymentOption chosenOption;
    @FragmentArg
    protected BigDecimal chosenAmount;
    @FragmentArg
    protected BillerLoadRecord billerData;
    @FragmentArg
    protected String accountNumber;
    @FragmentArg
    protected BillPaymentRequest formedRequest;
    @FragmentArg
    protected BigDecimal total;
    @FragmentArg
    protected BigDecimal transactionFee;


    @ViewById
    protected TextView productNameDisplay, totalText, submit, feeText, amountText;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bill_payment_confirmation_new, container, false);
    }

    @AfterViews
    public void init() {
        productNameDisplay.setText(chosenBillPaymentItem.masterBillerId);
        amountText.setText(chosenAmount.toString());
        feeText.setText(transactionFee.toString());
        totalText.setText(total.toString());
    }

    @Click
    void submit() {

        pcCallback.comfirm();
    }

    private BillpaymentConfirmCallback pcCallback;

    public void setCallback(BillpaymentConfirmCallback pcCallback) {
        this.pcCallback = pcCallback;
    }

    @Override
    public void onBackPressed() {
        pcCallback.popUpFragment();
    }

    public interface BillpaymentConfirmCallback {
        void comfirm();

        void popUpFragment();
    }

}