package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.print.pos.PrintPrepaidOrderCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;
import com.kaching123.tcr.fragment.tendering.PrepaidChooseCustomerDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayPrintAndFinishFragmentDialog;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PrepaidPrintAndFinishFragmentDialog extends PayPrintAndFinishFragmentDialog {

    private static final String DIALOG_NAME = PrepaidPrintAndFinishFragmentDialog.class.getSimpleName();

    @FragmentArg
    protected IPrePaidInfo info;

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_confirm_title;
    }

    @AfterViews
    protected void initViews() {
        printBox.setChecked(true);
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintPrepaidOrderCommand.start(getActivity(), false, skipPaperWarning, searchByMac, orderGuid, info, printOrderCallback);
    }

    @Override
    protected void sendDigitalOrder() {
        PrepaidChooseCustomerDialog.show(getActivity(), orderGuid, info, new ChooseCustomerBaseDialog.emailSenderListener() {
            @Override
            public void onComplete() {
                listener.onConfirmed();
            }
        });
    }

    public static void show(FragmentActivity context,
                            String orderGuid,
                            IFinishConfirmListener listener,
                            ArrayList<PaymentTransactionModel> transactions,
                            IPrePaidInfo info,
                            BigDecimal changeAmount) {
        DialogUtil.show(context,
                DIALOG_NAME,
                PrepaidPrintAndFinishFragmentDialog_.builder().info(info).transactions(transactions).orderGuid(orderGuid).changeAmount(changeAmount).build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}