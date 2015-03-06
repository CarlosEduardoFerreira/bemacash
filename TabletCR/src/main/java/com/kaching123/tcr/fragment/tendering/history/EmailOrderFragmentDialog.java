package com.kaching123.tcr.fragment.tendering.history;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.commands.print.digital.ResendDigitalOrderCommand;
import com.kaching123.tcr.commands.print.digital.ResendDigitalOrderCommand.BaseResendDigitalOrderCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;

/**
 * Created by pkabakov on 14.02.14.
 */
@EFragment
public class EmailOrderFragmentDialog extends ChooseCustomerBaseDialog {

    private static final String DIALOG_NAME = EmailOrderFragmentDialog.class.getSimpleName();

    @Override
    protected void sendDigitalOrder(String email) {
        ResendDigitalOrderCommand.start(getActivity(), orderGuid, email, (BaseResendDigitalOrderCallback) null);
		dismiss();    }

    public static void show(FragmentActivity context, String orderGuid) {
        DialogUtil.show(context, DIALOG_NAME, EmailOrderFragmentDialog_.builder().orderGuid(orderGuid).build());
    }

}
