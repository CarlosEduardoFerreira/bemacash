package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.commands.print.digital.SendDigitalPrepaidOrderCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;

/**
 * Created by gdubina on 03/03/14.
 */
@EFragment
public class PrepaidChooseCustomerDialog extends ChooseCustomerBaseDialog {

    public static final String DIALOG_NAME = PrepaidChooseCustomerDialog.class.getSimpleName();

    @FragmentArg
    protected IPrePaidInfo prepaidInfo;

    @Override
    protected void sendDigitalOrder(String email) {
        SendDigitalPrepaidOrderCommand.start(getActivity(), orderGuid, email, prepaidInfo, null);
        dismiss();
        listener.onComplete();
    }

    public static void show(FragmentActivity activity, String orderGuid, IPrePaidInfo prepaidInfo, emailSenderListener listener1) {
        DialogUtil.show(activity, DIALOG_NAME, PrepaidChooseCustomerDialog_.builder().orderGuid(orderGuid).prepaidInfo(prepaidInfo).build()).setListener(listener1);
    }
}
