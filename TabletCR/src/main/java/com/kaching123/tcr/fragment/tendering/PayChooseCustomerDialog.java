package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.commands.print.digital.SendDigitalOrderCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;

/**
 * Created by gdubina on 03/03/14.
 */
@EFragment
public class PayChooseCustomerDialog extends ChooseCustomerBaseDialog {

    public static final String DIALOG_NAME = "PAY_CHOOSE_CUSTOMER";

    @Override
    protected void onCustomerPicked(CustomerModel customer) {
        SendDigitalOrderCommand.start(getActivity(), orderGuid, customer.email, null, transactions, releaseResultList);
        dismiss();
        listener.onComplete();
    }

    public static void show(FragmentActivity activity, String orderGuid, ArrayList<PaymentTransactionModel> transactions, emailSenderListener listener, ArrayList<PrepaidReleaseResult> releaseResultList) {
        DialogUtil.show(activity, DIALOG_NAME, PayChooseCustomerDialog_.builder().transactions(transactions).orderGuid(orderGuid).releaseResultList(releaseResultList).build()).setListener(listener);
    }

}
