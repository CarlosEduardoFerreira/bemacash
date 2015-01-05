package com.kaching123.tcr.fragment.tendering.pax;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.PaxRefundCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class RefundPAXPendingFragmentDialog extends TransactionPendingFragmentDialogBase<RefundPAXPendingFragmentDialog, SaleResponse> {

    @FragmentArg
    protected SaleActionResponse reloadResponse;

    @FragmentArg
    protected boolean isManualReturn;

    @ViewById
    protected TextView message;

    @FragmentArg
    protected boolean refundTips;

    private BigDecimal amount;
    private IRefundProgressListener listener;
    private SaleOrderModel childOrderModel;

    public RefundPAXPendingFragmentDialog setListener(IRefundProgressListener listener) {
        this.listener = listener;
        return this;
    }

    private static final String DIALOG_NAME = "RefundPAXPendingFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        message.setSingleLine(false);
        message.setText(getString(R.string.pax_refund_instructions, transaction.getGuid()));
    }

    @Override
    protected void doCommand() {
        PaxGateway gateway = (PaxGateway) transaction.getGateway().gateway();
        gateway.refund(getActivity(), new PaxRefundCommand.PaxREFUNDCommandBaseCallback() {

            @Override
            protected void handleSuccess(SaleOrderModel childOrderModel,
                                         PaymentTransactionModel childTransactionModel,
                                         Transaction transaction,
                                         String errorMessage) {
                listener.onComplete(childTransactionModel, transaction, childOrderModel, errorMessage);
            }

            @Override
            protected void handleError() {
                listener.onCancel();
            }
        }, null, null, new PaymentTransactionModel(getApp().getShiftGuid(), transaction), amount, reloadResponse, childOrderModel, refundTips, isManualReturn);
    }

    public RefundPAXPendingFragmentDialog setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public RefundPAXPendingFragmentDialog setChildOrderModel(SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        return this;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public static void show(FragmentActivity context,
                            final Transaction transaction,
                            BigDecimal amount,
                            SaleActionResponse reloadResponse,
                            SaleOrderModel childOrderModel,
                            IRefundProgressListener listener,
                            boolean refundTips,
                            boolean isManualReturn) {
        DialogUtil.show(context, DIALOG_NAME, RefundPAXPendingFragmentDialog_.builder().refundTips(refundTips).isManualReturn(isManualReturn).reloadResponse(reloadResponse).build())
                .setChildOrderModel(childOrderModel)
                .setAmount(amount)
                .setListener(listener)
                .setTransaction(transaction);
    }

    public interface IRefundProgressListener {

        void onComplete(PaymentTransactionModel child, Transaction parent, SaleOrderModel childOrderModel, String errorMessage);

        void onCancel();
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}