package com.kaching123.tcr.activity.PrepaidActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.TransactionMode;

/**
 * Created by teli.yin on 10/30/2014.
 */
public class PrepaidBaseFragmentActivity extends SuperBaseActivity {

    PrepaidUser user;
    long transactionId;
    String transactionMode;
    String cashierId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = TcrApplication.get().getPrepaidUser();
        transactionId = getTransactionId();
        transactionMode = getPrepaidTransactionMode();
        cashierId = getCashierId();
    }
    private String getCashierId()
    {
        return  TcrApplication.get().getOperator().login;
    }
    private long getTransactionId()
    {
        return 0;
    }
    private String getPrepaidTransactionMode() {
        return TcrApplication.get().isTrainingMode() ? TransactionMode.getTransactionMode(true) : TcrApplication.get().getShopInfo().prepaidTransactionMode;
    }
    public void switchContent(Fragment from, Fragment to, Fragment mContent, FragmentManager mFragmentMan, int frame) {
        if (mContent != to) {
            mContent = to;
            FragmentTransaction transaction = mFragmentMan.beginTransaction().setCustomAnimations(
                    R.animator.fragment_slide_left_enter, R.animator.fragment_slide_right_exit);
            if (!to.isAdded()) {
                transaction.hide(from).add(frame, to).commit();
            } else {
                transaction.hide(from).show(to).commit();
            }
        }
    }
}
