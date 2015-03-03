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
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.print.FormatterUtil;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductComfirmationFragment extends PrepaidLongDistanceBaseBodyFragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface {

    @FragmentArg
    protected BigDecimal amount;
    @FragmentArg
    protected WirelessItem chosenCategory;
    @FragmentArg
    protected String phoneNumber;
    @FragmentArg
    protected BigDecimal feeAmount;

    @ViewById
    protected TextView productName, productNameDisplay, total, submit, feeAmountShows;
    @ViewById
    protected ImageView productImageview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wireless_payment_confirm, container, false);
    }

    @AfterViews
    public void init() {
        productName.setText(chosenCategory.name);
        UrlImageViewHelper.setUrlDrawable(productImageview, chosenCategory.iconUrl, R.drawable.operator_default_icon, 60000);
        productNameDisplay.setText(chosenCategory.name);
        feeAmountShows.setText(FormatterUtil.commaPriceFormat(feeAmount));
        total.setText(FormatterUtil.commaPriceFormat(amount.add(feeAmount)));
    }

    @Click
    void submit() {
        pcCallback.comfirm(phoneNumber, amount.add(feeAmount), chosenCategory);
    }

    private ProductComfirmationCallback pcCallback;

    public void setCallback(ProductComfirmationCallback pcCallback) {
        this.pcCallback = pcCallback;
    }

    @Override
    public void onBackPressed() {
        pcCallback.popUpFragment();
    }

    public interface ProductComfirmationCallback {
        void comfirm(String phoneNumber, BigDecimal amount, WirelessItem chosenCategory);

        void popUpFragment();
    }

}
