package com.kaching123.tcr.fragment.tendering.pax;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.pax.PaxBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.PaxBalanceCommand.PaxBalanceCommandBaseCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class BalancePAXPendingFragmentDialog extends TransactionPendingFragmentDialogBase<BalancePAXPendingFragmentDialog, SaleResponse> {

    private static final String DIALOG_NAME = "IBalanceSaleProgressListener";

    @ViewById
    protected TextView message;

    protected IBalanceSaleProgressListener listener;

    public BalancePAXPendingFragmentDialog setListener(IBalanceSaleProgressListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        message.setSingleLine(false);
        message.setText(R.string.pax_balance_instructions);
    }

    @Override
    protected void doCommand() {
        PaxBalanceCommand.startBalanceRequest(getActivity(), PaxModel.get(), new PaxBalanceCommandBaseCallback() {

            @Override
            protected void handleSuccess(BigDecimal result, String last4, String errorReason) {
                listener.onComplete(result, last4, errorReason);

            }

            @Override
            protected void handleError() {
                listener.onCancel();
            }
        });
    }

    public interface IBalanceSaleProgressListener {

        void onComplete(BigDecimal balance, String last4, String errorReason);

        void onCancel();
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public static void show(FragmentActivity context,
                            IBalanceSaleProgressListener listener) {
        DialogUtil.show(context, DIALOG_NAME, BalancePAXPendingFragmentDialog_.builder().build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}