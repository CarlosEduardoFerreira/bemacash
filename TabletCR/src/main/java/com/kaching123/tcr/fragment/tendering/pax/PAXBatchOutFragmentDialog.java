package com.kaching123.tcr.fragment.tendering.pax;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBatchOutCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorGiftCardReloadCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PAXBatchOutFragmentDialog extends TransactionPendingFragmentDialogBase<PAXBatchOutFragmentDialog, SaleResponse> {

    @FragmentArg
    protected SaleActionResponse reloadResponse;
    @FragmentArg
    protected PaxModel model;

    @ViewById
    protected TextView message;
    @ViewById
    protected ProgressBar progressBar;

    protected IPaxBatchOutListener listener;

    public PAXBatchOutFragmentDialog setListener(IPaxBatchOutListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                reTry();
//                listener.onRetry();
                return false;
            }
        };
    }

    private void reTry() {
        message.setSingleLine(false);
        message.setText(R.string.pax_instructions);
        getPositiveButton().setVisibility(View.INVISIBLE);
        getNegativeButton().setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        doCommand();
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                listener.onCancel();
                return false;
            }
        };
    }

    private static final String DIALOG_NAME = "PAXReloadFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        message.setSingleLine(false);
        message.setText(R.string.pax_instructions);
        message.setTextColor(Color.WHITE);
        getPositiveButton().setVisibility(View.INVISIBLE);
        getNegativeButton().setVisibility(View.INVISIBLE);
    }


    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_item_cancel;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.blackstone_pay_reload_btn;
    }

    @Override
    protected void doCommand() {
//        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
//        paxGateway.doBalance(getActivity(), reloadGiftCardCallBack());

        PaxProcessorBatchOutCommand.start(getContext(), new PaxProcessorBatchOutCommand.PaxProcessorBatchOutCommandCallback() {

            @Override
            protected void handleSuccess(String errorReason) {
                if (!errorReason.equalsIgnoreCase(PaxProcessorBatchOutCommand.SUCCESS)) {
                    message.setText(errorReason);
                    message.setTextColor(Color.RED);
                    getPositiveButton().setEnabled(true);
                    getNegativeButton().setEnabled(true);
                    hasNegativeButton();
                    getPositiveButton().setVisibility(View.VISIBLE);
                    getNegativeButton().setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else
                    listener.onComplete(errorReason);
            }

            @Override
            protected void handleError(String response) {
                if (!response.equalsIgnoreCase(PaxProcessorBatchOutCommand.SUCCESS)) {
                    message.setText(response);
                    message.setTextColor(Color.RED);
                    getPositiveButton().setEnabled(true);
                    getNegativeButton().setEnabled(true);
                    hasNegativeButton();
                    getPositiveButton().setVisibility(View.VISIBLE);
                    getNegativeButton().setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
//        PaxProcessorGiftCardReloadCommand.startSale(getContext(), model, amount, new PaxProcessorGiftCardReloadCommand.PaxGiftCardReloadCallback() {
//
//            @Override
//            protected void handleSuccess(String errorReason) {
//
//            }
//
//            @Override
//            protected void handleError() {
//                message.setText(R.string.error_dialog_title);
//
//                listener.onCancel();
//            }
//        });
    }

    public interface IPaxBatchOutListener {

        void onComplete(String msg);

        void onCancel();

        void onRetry();

    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public static void show(FragmentActivity context,
                            IPaxBatchOutListener listener,
                            PaxModel paxTerminal) {
        DialogUtil.show(context, DIALOG_NAME, PAXBatchOutFragmentDialog_.builder().model(paxTerminal).build())
                .setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}