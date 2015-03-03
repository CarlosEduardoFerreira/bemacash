package com.kaching123.tcr.fragment.creditreceipt;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.commands.print.digital.SendDigitalCreditCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;

/**
 * Created by vkompaniets on 19.03.14.
 */
@EFragment
public class EmailCreditReceiptFragmentDialog extends ChooseCustomerBaseDialog {

    private static final String DIALOG_NAME = EmailCreditReceiptFragmentDialog.class.getSimpleName();

    @Override
    protected void sendDigitalOrder(String email) {
        SendDigitalCreditCommand.start(getActivity(), orderGuid, email);
        dismiss();
    }

    public static void show(FragmentActivity context, String orderGuid) {
        DialogUtil.show(context, DIALOG_NAME, EmailCreditReceiptFragmentDialog_.builder().orderGuid(orderGuid).build());

    }

}
