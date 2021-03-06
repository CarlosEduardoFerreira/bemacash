package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.commands.print.digital.SendDigitalOrderCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalOrderForGiftCardCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;


import java.util.ArrayList;

/**
 * Created by gdubina on 03/03/14.
 */
@EFragment
public class PayChooseCustomerDialog extends ChooseCustomerBaseDialog {

    public static final String DIALOG_NAME = "PAY_CHOOSE_CUSTOMER";

    @Override
    protected void sendDigitalOrder(String email) {
        SendDigitalOrderCommand.start(getActivity(), orderGuid, email, null, transactions, releaseResultList);
        dismiss();
        listener.onComplete();
    }

    @Override
    protected void onCustomerPicked(CustomerModel customer) {
        dismiss();
        listener.onComplete();
    }

    @Override
    protected void sendDigitalOrderForGiftCard(String email, String amount) {
        SendDigitalOrderForGiftCardCommand.start(getActivity(), email, amount, null);
        dismiss();
        listener.onComplete();
    }

    public static void show(FragmentActivity activity, String orderGuid, ArrayList<PaymentTransactionModel> transactions, emailSenderListener listener, ArrayList<PrepaidReleaseResult> releaseResultList) {
        DialogUtil.show(activity, DIALOG_NAME, PayChooseCustomerDialog_.builder().transactions(transactions).orderGuid(orderGuid).releaseResultList(releaseResultList).build()).setListener(listener);
    }

    public static void show(FragmentActivity activity, boolean isGiftCard, emailSenderListener listener, String amount) {
        DialogUtil.show(activity, DIALOG_NAME, PayChooseCustomerDialog_.builder().isGiftCard(isGiftCard).amount(amount).build()).setListener(listener);
    }

}
