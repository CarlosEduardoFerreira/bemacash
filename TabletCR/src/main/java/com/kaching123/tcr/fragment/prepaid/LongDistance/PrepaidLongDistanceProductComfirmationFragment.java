package com.kaching123.tcr.fragment.prepaid.LongDistance;

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
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
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

    @ViewById
    protected TextView productName, productNameDisplay, total, submit;
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
        total.setText(amount.toString());
    }

    @Click
    void submit() {
        pcCallback.comfirm(phoneNumber, amount, chosenCategory);
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
