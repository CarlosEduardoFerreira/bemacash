package com.kaching123.tcr.fragment.creditreceipt;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.commands.print.digital.SendDigitalCreditCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalOrderForGiftCardCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;
import com.kaching123.tcr.model.CustomerModel;

import org.androidannotations.annotations.EFragment;

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

    @Override
    protected void onCustomerPicked(CustomerModel customer) {
        SendDigitalCreditCommand.start(getActivity(), orderGuid, customer.email);
        dismiss();
    }

    @Override
    protected void sendDigitalOrderForGiftCard(String email, String amount) {
        SendDigitalOrderForGiftCardCommand.start(getActivity(),email, amount, null);
        dismiss();
    }

    public static void show(FragmentActivity context, String orderGuid) {
        DialogUtil.show(context, DIALOG_NAME, EmailCreditReceiptFragmentDialog_.builder().orderGuid(orderGuid).build());

    }

}
