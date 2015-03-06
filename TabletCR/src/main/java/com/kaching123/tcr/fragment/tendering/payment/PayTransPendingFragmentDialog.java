package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackGateway;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackProcessPreauthCommand.BaseProcessPreauthCallback;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackSaleCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.annotations.OnCancel;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PayTransPendingFragmentDialog extends TransactionPendingFragmentDialogBase<PayTransPendingFragmentDialog, ResponseBase> {

    private static final String DIALOG_NAME = "PayTransPendingFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @Override
    protected void doCommand() {
        BlackGateway gateway = (BlackGateway) PaymentGateway.BLACKSTONE.gateway();
        if (!transaction.isPreauth)
            gateway.sale(getActivity(), this, user, card, transaction);
        else
            gateway.processPreauth(getActivity(), processPreauthCallback, user, card, transaction);
    }

    @OnSuccess(BlackSaleCommand.class)
    public void onPaySuccess(@Param(RESTWebCommand.RESULT_DATA) SaleResponse result, @Param(BlackSaleCommand.ARG_TRANSACTION) Transaction transaction) {
        Logger.d("BlackSaleCommand.onPaySuccess(): result: %s", result.toDebugString());
        tryComplete(transaction, result, null);
    }

    @OnFailure(BlackSaleCommand.class)
    public void onPayFail(@Param(RESTWebCommand.RESULT_DATA) SaleResponse result,
                          @Param(RESTWebCommand.RESULT_REASON) WebCommand.ErrorReason reason,
                          @Param(BlackSaleCommand.ARG_TRANSACTION) Transaction transaction) {
        Logger.e("BlackSaleCommand.onPayFail(): result: " + (result == null ? null : result.toDebugString()) + ", error reason: " + reason);
        tryComplete(transaction, result, reason);
    }

    @OnCancel(BlackSaleCommand.class)
    public void onPayCancel(@Param(RESTWebCommand.RESULT_DATA) SaleResponse result,
                            @Param(RESTWebCommand.RESULT_REASON) WebCommand.ErrorReason reason,
                            @Param(BlackSaleCommand.ARG_TRANSACTION) Transaction transaction) {
        Logger.d("BlackSaleCommand.onPayCancel(): result: " + (result == null ? null : result.toDebugString()));
        tryComplete(transaction, result, reason);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    private BaseProcessPreauthCallback processPreauthCallback = new BaseProcessPreauthCallback() {

        @Override
        protected void handleSuccess(PreauthResponse response, Transaction transaction) {
            Logger.d("BlackProcessPreauthCommand.handleSuccess(): response: %s", response.toDebugString());
            tryComplete(transaction, response, null);
        }

        @Override
        protected void handleFailure(PreauthResponse response, ErrorReason errorReason, Transaction transaction) {
            Logger.e("BlackProcessPreauthCommand.handleFailure(): response: " + (response == null ? null : response.toDebugString()) + ", error reason: " + errorReason);
            tryComplete(transaction, response, errorReason);
        }
    };

    public static void show(FragmentActivity context,
                            final Transaction transaction,
                            final CreditCard card,
                            User user,
                            ISaleProgressListener listener) {
        DialogUtil.show(context, DIALOG_NAME, PayTransPendingFragmentDialog_.builder().build())
                .setListener(listener).setCard(card).setUser(user).setTransaction(transaction);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}