package com.kaching123.tcr.fragment.tendering.history;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.commands.print.digital.ResendDigitalOrderCommand;
import com.kaching123.tcr.commands.print.digital.ResendDigitalOrderCommand.BaseResendDigitalOrderCallback;
import com.kaching123.tcr.commands.print.digital.SendDigitalOrderForGiftCardCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;
import com.kaching123.tcr.model.CustomerModel;

import org.androidannotations.annotations.EFragment;

/**
 * Created by pkabakov on 14.02.14.
 */
@EFragment
public class EmailOrderFragmentDialog extends ChooseCustomerBaseDialog {

    private static final String DIALOG_NAME = EmailOrderFragmentDialog.class.getSimpleName();

    private EmailOrderCompleteListener listener;
    @Override
    protected void sendDigitalOrder(final String email) {
        ResendDigitalOrderCommand.start(getActivity(), orderGuid, email, new BaseResendDigitalOrderCallback() {
            @Override
            protected void onDigitalOrderSent() {
//                Toast.makeText(getContext(), getContext().getString(R.string.send_email_toast_msg) + " " + email, Toast.LENGTH_LONG).show();
                dismiss();
                listener.onConfirmed(email);
            }

            @Override
            protected void onDigitalOrderSendError() {
                dismiss();
            }
        });
        dismiss();
    }

    @Override
    protected void onCustomerPicked(CustomerModel customer) {
        ResendDigitalOrderCommand.start(getActivity(), orderGuid, customer.email, (BaseResendDigitalOrderCallback) null);
		dismiss();    }

    @Override
    protected void sendDigitalOrderForGiftCard(final String email, String amount) {
        SendDigitalOrderForGiftCardCommand.start(getActivity(), email, amount, new SendDigitalOrderForGiftCardCommand.BaseSendDigitalOrderCallback() {
            @Override
            protected void onDigitalOrderSent() {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.send_email_toast_msg) + " " + email, Toast.LENGTH_LONG).show();
                dismiss();
                listener.onConfirmed(email);
            }

            @Override
            protected void onDigitalOrderSendError() {
                dismiss();
            }
        });
        dismiss();
    }

    public static void show(FragmentActivity context, String orderGuid, EmailOrderCompleteListener listener) {
        DialogUtil.show(context, DIALOG_NAME, EmailOrderFragmentDialog_.builder().orderGuid(orderGuid).build()).setListener(listener);
    }

    public void setListener(EmailOrderCompleteListener listener)
    {
        this.listener = listener;
    }
    public  interface EmailOrderCompleteListener {

        void onConfirmed(String email);
    }
}
