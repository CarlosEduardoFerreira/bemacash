package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackDoSettlementCommand.BaseDoSettlementCallback;

import com.kaching123.tcr.commands.payment.blackstone.payment.BlackGateway;

import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneBaseCommand;


import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneSettlementCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorSettlementCommand;


import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBaseCommand;


import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoSettlementResponse;

@EFragment
public class SettlementTransPendingFragmentDialog extends TransactionPendingFragmentDialogBase<SettlementTransPendingFragmentDialog, DoSettlementResponse> {

    private static final String DIALOG_NAME = SettlementTransPendingFragmentDialog.class.getSimpleName();

    private ISettlementProgressListener settlementListener;

    public SettlementTransPendingFragmentDialog setSettlementListener(ISettlementProgressListener listener) {
        this.settlementListener = listener;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @Override
    protected void doCommand() {
        if (getApp().isPaxConfigured()) {
            PaxGateway paxGateway = (PaxGateway)PaymentGateway.PAX.gateway();
            paxGateway.doSettlement(getActivity(), returnPaxCallBack());
            return;
        }
        BlackGateway blackstoneGateway = (BlackGateway) PaymentGateway.BLACKSTONE.gateway();
        blackstoneGateway.doSettlement(getActivity(), doSettlementCallback, TcrApplication.get().getBlackStoneUser());
    }

    private void complete(TransactionStatusCode responseCode, ErrorReason errorReason, boolean transactionsClosed) {
        if (settlementListener != null) {
            settlementListener.onComplete(responseCode, errorReason, transactionsClosed);
        }
    }

    private void complete(TransactionStatusCode responseCode, PaxGateway.Error error, boolean transactionsClosed) {
        if (settlementListener != null) {
            settlementListener.onComplete(responseCode, error, transactionsClosed);
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    private BaseDoSettlementCallback doSettlementCallback = new BaseDoSettlementCallback() {

        @Override
        protected void handleSuccess(TransactionStatusCode responseCode, boolean transactionsClosed) {
            Logger.d("BaseDoSettlementCallback.handleSuccess(): responseCode: " + responseCode + ", transactionsClosed: " + transactionsClosed);
            complete(responseCode, (ErrorReason)null, transactionsClosed);
        }

        @Override
        protected void handleFailure(TransactionStatusCode responseCode, ErrorReason errorReason) {
            Logger.e("BaseDoSettlementCallback.handleFailure(): responseCode: " + responseCode + ", errorReason: " + errorReason);
            complete(responseCode, errorReason, false);
        }
    };

    private Object returnPaxCallBack () {
        if (!TcrApplication.get().isBlackstonePax()) {
            return  doSettlementProcessorPaxCallback;
        }
        return doSettlementPaxCallback;
    }
    private PaxBlackstoneSettlementCommand.SettlementCommandBaseCallback doSettlementPaxCallback = new PaxBlackstoneSettlementCommand.SettlementCommandBaseCallback() {

        @Override
        protected void handleSuccess(TransactionStatusCode responseCode, boolean transactionsClosed) {
            Logger.d("SettlementCommandBaseCallback.handleSuccess(): responseCode: " + responseCode + ", transactionsClosed: " + transactionsClosed);
            complete(responseCode,  (PaxGateway.Error) null, transactionsClosed);
        }

        @Override
        protected void handleError(PaxGateway.Error error, TransactionStatusCode errorCode) {
            Logger.e("SettlementCommandBaseCallback.handleFailure(): errorCode: " + errorCode + ", error: " + error);
            complete(errorCode, error, false);
        }
    };
    private PaxProcessorSettlementCommand.SettlementCommandBaseCallback doSettlementProcessorPaxCallback = new PaxProcessorSettlementCommand.SettlementCommandBaseCallback() {

        @Override
        protected void handleSuccess(TransactionStatusCode responseCode, boolean transactionsClosed) {
            Logger.d("SettlementCommandBaseCallback.handleSuccess(): responseCode: " + responseCode + ", transactionsClosed: " + transactionsClosed);
            complete(responseCode, (PaxGateway.Error) null, transactionsClosed);
        }

        @Override
        protected void handleError(PaxGateway.Error error, TransactionStatusCode errorCode) {
            Logger.e("SettlementCommandBaseCallback.handleFailure(): errorCode: " + errorCode + ", error: " + error);
            complete(errorCode, error, false);
        }
    };

    public interface ISettlementProgressListener {

        void onComplete(TransactionStatusCode responseCode, ErrorReason errorReason, boolean transactionsClosed);

        void onComplete(TransactionStatusCode responseCode, PaxGateway.Error error, boolean transactionsClosed);

    }

    public static void show(FragmentActivity context,
                            ISettlementProgressListener listener) {
        DialogUtil.show(context, DIALOG_NAME, SettlementTransPendingFragmentDialog_.builder().build())
                .setSettlementListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
