package com.kaching123.tcr.fragment.tendering.pax;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneSaleCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorSaleCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PAXBalanceFragmentDialog extends TransactionPendingFragmentDialogBase<PAXBalanceFragmentDialog, SaleResponse> {

    @FragmentArg
    protected SaleActionResponse reloadResponse;

    @ViewById
    protected TextView message;

    protected IPaxBalanceListener listener;

    public PAXBalanceFragmentDialog setListener(IPaxBalanceListener listener) {
        this.listener = listener;
        return this;
    }

    private static final String DIALOG_NAME = "PAXBalanceFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        message.setSingleLine(false);
        message.setText(R.string.pax_instructions);
    }

    private Object reloadGiftCardCallBack(){
        return new PaxProcessorBalanceCommand.PaxBalanceCommandBaseCallback(){

            @Override
            protected void handleSuccess(BigDecimal result, String last4, String errorReason) {
                listener.onComplete(result, errorReason);
            }

            @Override
            protected void handleError() {
                listener.onComplete(null, WebCommand.ErrorReason.UNKNOWN.getDescription());
            }
        };
    }


    @Override
    protected void doCommand() {
        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
        paxGateway.doBalance(getActivity(), reloadGiftCardCallBack());
    }

    public interface IPaxBalanceListener {

        void onComplete(BigDecimal amount, String errorReason);

        void onCancel();

    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public static void show(FragmentActivity context,
                            IPaxBalanceListener listener) {
        DialogUtil.show(context, DIALOG_NAME, PAXBalanceFragmentDialog_.builder().build())
                .setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}