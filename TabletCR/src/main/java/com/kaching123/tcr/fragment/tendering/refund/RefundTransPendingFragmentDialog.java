package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackRefundCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.response.RefundResponse;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.annotations.OnCancel;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class RefundTransPendingFragmentDialog extends TransactionPendingFragmentDialogBase<RefundTransPendingFragmentDialog, RefundResponse> {

    private static final String DIALOG_NAME = "RefundTransPendingFragmentDialog";

    @FragmentArg
    protected boolean refundTips;

    @FragmentArg
    protected boolean isManualReturn;

    private BigDecimal amount;

    private SaleOrderModel childOrderModel;

    private IRefundProgressListener listener;


    public RefundTransPendingFragmentDialog setChildOrderModel(SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        return this;
    }

    public RefundTransPendingFragmentDialog setListener(IRefundProgressListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @Override
    protected void doCommand() {
        PaymentGateway.BLACKSTONE.gateway().refund(getActivity(), this, user, card,
                new PaymentTransactionModel(getApp().getShiftGuid(), transaction), amount, childOrderModel, refundTips, isManualReturn);
    }

    @OnSuccess(BlackRefundCommand.class)
    public void onPaySuccess(@Param(RESTWebCommand.RESULT_DATA) RefundResponse result,
                             @Param(BlackRefundCommand.ARG_TRANSACTION_MODEL) PaymentTransactionModel childTransactionModel,
                             @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                             @Param(BlackRefundCommand.ARG_TRANSACTION) Transaction transaction) {
        Logger.d("BlackRefundCommand.onPaySuccess(): result: %s", result.toDebugString());
        this.childOrderModel = childOrderModel;
        tryComplete(childTransactionModel, result, null, transaction, childOrderModel);
    }

    @OnFailure(BlackRefundCommand.class)
    public void onPayFail(@Param(RESTWebCommand.RESULT_DATA) RefundResponse result,
                          @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                          @Param(RESTWebCommand.RESULT_REASON) ErrorReason reason,
                          @Param(BlackRefundCommand.ARG_TRANSACTION) Transaction transaction) {
        Logger.e("BlackRefundCommand.onPayFail(): result: %s" + (result == null ? null : result.toDebugString()) + ", error reason: " + reason);
        this.childOrderModel = childOrderModel;
        tryComplete(null, result, reason, transaction, childOrderModel);
    }

    @OnCancel(BlackRefundCommand.class)
    public void onPayCancel(@Param(RESTWebCommand.RESULT_DATA) RefundResponse result,
                            @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                            @Param(RESTWebCommand.RESULT_REASON) ErrorReason reason,
                            @Param(BlackRefundCommand.ARG_TRANSACTION_MODEL) PaymentTransactionModel childTransactionModel,
                            @Param(BlackRefundCommand.ARG_TRANSACTION) Transaction transaction) {
        Logger.d("BlackRefundCommand.onPayCancel(): result: %s", result == null ? null : result.toDebugString());
        this.childOrderModel = childOrderModel;
        tryComplete(childTransactionModel, result, reason, transaction, childOrderModel);
    }

    protected boolean tryComplete(PaymentTransactionModel child, RefundResponse result, ErrorReason reason, Transaction paren, SaleOrderModel childOrderModel) {
        if (listener != null) {
            listener.onComplete(child, result, reason, paren, childOrderModel);
            return true;
        }
        return false;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public RefundTransPendingFragmentDialog setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public static void show(FragmentActivity context,
                            final Transaction transaction,
                            final CreditCard card,
                            User user,
                            BigDecimal amount,
                            IRefundProgressListener listener,
                            SaleOrderModel childOrderModel,
                            boolean refundTips,
                            boolean isManualReturn) {
        DialogUtil.show(context, DIALOG_NAME, RefundTransPendingFragmentDialog_.builder().refundTips(refundTips).isManualReturn(isManualReturn).build())
                .setChildOrderModel(childOrderModel)
                .setAmount(amount)
                .setListener(listener)
                .setCard(card)
                .setUser(user)
                .setTransaction(transaction);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface IRefundProgressListener {

        void onComplete(PaymentTransactionModel child, RefundResponse result, ErrorReason reason, Transaction parent, SaleOrderModel childOrderModel);

        void onCancel();
    }
}