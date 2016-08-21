package com.kaching123.tcr.fragment.tendering.pax;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorGiftCardReloadCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PAXReloadFragmentDialog extends TransactionPendingFragmentDialogBase<PAXReloadFragmentDialog, SaleResponse> {

    @FragmentArg
    protected SaleActionResponse reloadResponse;
    @FragmentArg
    protected PaxModel model;
    @FragmentArg
    protected String amount;

    @ViewById
    protected TextView message;

    protected IPaxReloadListener listener;

    public PAXReloadFragmentDialog setListener(IPaxReloadListener listener) {
        this.listener = listener;
        return this;
    }

    private static final String DIALOG_NAME = "PAXReloadFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        message.setSingleLine(false);
        message.setText(R.string.pax_instructions);
    }


    @Override
    protected void doCommand() {
//        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
//        paxGateway.doBalance(getActivity(), reloadGiftCardCallBack());
        PaxProcessorGiftCardReloadCommand.startSale(getContext(),model, amount,  new PaxProcessorGiftCardReloadCommand.PaxGiftCardReloadCallback() {

            @Override
            protected void handleSuccess(String errorReason) {
                listener.onComplete(errorReason);
            }

            @Override
            protected void handleError() {
                listener.onCancel();
            }
        });
    }

    public interface IPaxReloadListener {

        void onComplete(String msg);

        void onCancel();

    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public static void show(FragmentActivity context,
                            IPaxReloadListener listener,
                            PaxModel paxTerminal,
                            String amount) {
        DialogUtil.show(context, DIALOG_NAME, PAXReloadFragmentDialog_.builder().model(paxTerminal).amount(amount).build())
                .setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}