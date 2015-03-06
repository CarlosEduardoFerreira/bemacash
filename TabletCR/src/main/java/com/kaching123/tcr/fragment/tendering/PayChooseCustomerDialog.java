package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.commands.print.digital.SendDigitalOrderCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.PaymentTransactionModel;

import java.util.ArrayList;

/**
 * Created by gdubina on 03/03/14.
 */
@EFragment
public class PayChooseCustomerDialog extends ChooseCustomerBaseDialog {

    public static final String DIALOG_NAME = "PAY_CHOOSE_CUSTOMER";

    @Override
    protected void sendDigitalOrder(String email) {
        SendDigitalOrderCommand.start(getActivity(), orderGuid, email, null, transactions);
        dismiss();
        listener.onComplete();
    }

    public static void show(FragmentActivity activity, String orderGuid, ArrayList<PaymentTransactionModel> transactions, emailSenderListener listener) {
        DialogUtil.show(activity, DIALOG_NAME, PayChooseCustomerDialog_.builder().transactions(transactions).orderGuid(orderGuid).build()).setListener(listener);
    }

}
