package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.WaitForCashInDrawerCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.cash.CashRefundCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener.IDrawerFriend;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.util.AnimationUtils;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */

@EFragment
public class RefundCashFragmentDialog extends StyledDialogFragment implements IDrawerFriend {

    private static final String DIALOG_NAME = "VoidProcessingFragmentDialog";

    @FragmentArg
    protected boolean refundTips;

    @FragmentArg
    protected boolean isManualReturn;

    @ViewById
    protected ImageView sign;

    @ViewById
    protected ProgressBar progressBar;

    @ViewById
    protected RelativeLayout amountHolder;

    @ViewById
    protected TextView total;

    @ViewById
    protected TextView message;

    @ViewById
    protected TextView description;

    @ViewById
    protected ViewFlipper flipper;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;

    private TaskHandler waitCashTask;
    private SaleOrderModel returnOrder;

    private LinkedBlockingQueue<PaymentTransactionModel> cashTransactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    private ArrayList<PaymentTransactionModel> refundChildTransactions = new ArrayList<PaymentTransactionModel>();
    private IRefundProgressListener listener;
    private BigDecimal amount;
    private BigDecimal savedTotalAvailable = BigDecimal.ZERO;
    private int max;
    private int current = 0;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.voiding_processor;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.base_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
//        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        getNegativeButton().setTextColor(getResources().getColor(R.color.gray_dark));
        getNegativeButton().setEnabled(false);
        AnimationUtils.applyFlippingEffect(getActivity(), flipper);
        message.setText(String.format("voided %d of %d", 0, max));
        message.setVisibility(View.VISIBLE);
        init();
    }

    private boolean complete() {
        description.setText(R.string.refund_result_ok);
        enableButton(true);
        flipper.setDisplayedChild(2);
        return true;
    }

    private BigDecimal getCashAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentTransactionModel transaction : cashTransactions) {
            total = total.add(transaction.availableAmount);
        }
        return total;
    }

    private boolean init() {
        amountHolder.setVisibility(View.VISIBLE);
        message.setVisibility(View.GONE);
//            biller = getCashAmount().min(biller);
        total.setText(UiHelper.valueOf(getCashAmount().min(amount)));
        enableButton(false);
        try2GetCash(false);
        return true;
    }

    protected void next() {
        Logger.d("Welcome to new round, biller is %s", amount.toString());
        PaymentTransactionModel transaction = cashTransactions.poll();
        if (transaction == null || BigDecimal.ZERO.compareTo(amount) == 0) {
            complete();
            return;
        }
        message.setText(String.format("voided %d of %d", current++, max));
        BigDecimal possibleAmount = amount.min(transaction.availableAmount);
        if (possibleAmount.compareTo(BigDecimal.ZERO) > 0) {

            PaymentGateway.CASH.gateway().refund(getActivity(), this, null, null, transaction, possibleAmount, returnOrder, refundTips, isManualReturn);
        } else {
            next();
        }
    }

    @OnSuccess(CashRefundCommand.class)
    public void onVoidCashSuccess(@Param(CashRefundCommand.RESULT_DATA) PaymentTransactionModel transaction,
                                  @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        this.returnOrder = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        amount = amount.add(transaction.amount);//transaction.biller will be negative
        refundChildTransactions.add(transaction);
        next();
    }

    @OnFailure(CashRefundCommand.class)
    public void onVoidCashFail(@Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        this.returnOrder = childOrderModel;
        next();
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    recalcLeftAmountRounding();
                    listener.onComplete(amount, refundChildTransactions, returnOrder);
                }
                return false;
            }
        };
    }

    private void enableButton(boolean enabled) {

        getNegativeButton().setEnabled(enabled);
        getNegativeButton().setTextColor(enabled ? colorPaymentOk : colorPaymentDisabled);

    }

    @Override
    protected int getDialogTitle() {
        return R.string.refund_process_title;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public RefundCashFragmentDialog setTransactions(List<PaymentTransactionModel> transactions) {
        max = transactions.size();
        cashTransactions.addAll(transactions);
        for (PaymentTransactionModel transaction : transactions) {
            savedTotalAvailable = savedTotalAvailable.add(transaction.availableAmount);
        }
        return this;
    }

    public RefundCashFragmentDialog setListener(IRefundProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public RefundCashFragmentDialog setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public RefundCashFragmentDialog setRefundOrder(SaleOrderModel orderModel) {
        this.returnOrder = orderModel;
        return this;
    }

    @Override
    public void onDrawerOpened() {
        Logger.d("RefundCashFragmentDialog: onDrawerOpened()");
        WaitDialogFragment.hide(getActivity());
        flipper.setDisplayedChild(1);
    }

    @Override
    public void onFailure() {
        Logger.d("RefundCashFragmentDialog: onFailure()");
        WaitDialogFragment.hide(getActivity());
    }

    @Override
    public void onPopupCancelled() {
        recalcLeftAmountRounding();
        listener.onComplete(amount, refundChildTransactions, returnOrder);
    }

    private void recalcLeftAmountRounding(){
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (PaymentTransactionModel refundChildTransaction : refundChildTransactions) {
            totalRefund = totalRefund.add(refundChildTransaction.amount.abs());
        }
        BigDecimal tmpAmount = savedTotalAvailable.subtract(totalRefund).abs();
        if (amount.abs().compareTo(BigDecimal.valueOf(0.01)) == 0 && tmpAmount.compareTo(BigDecimal.ZERO) == 0){
            amount = BigDecimal.ZERO;
        }
    }

    @Override
    public void onCashReceived() {
        Logger.d("RefundCashFragmentDialog: onCashReceived()");
        WaitDialogFragment.hide(getActivity());
        flipper.setDisplayedChild(0);
        amountHolder.setVisibility(View.INVISIBLE);
        sign.setImageResource(R.drawable.check_green);
        description.setText(R.string.refund_result_ok);
        next();
    }

    @Override
    public void cancelWaitCashTask() {
        if (waitCashTask != null) {
            waitCashTask.cancel(getActivity(), 0, null);
            waitCashTask = null;
        }
    }

    @Override
    public boolean try2GetCash(boolean searchByMac) {
        Logger.d("RefundCashFragmentDialog: try2GetCash()");
        if (getCashAmount().compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(getActivity(), R.string.pay_toast_zero, Toast.LENGTH_LONG).show();
            return false;
        }
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_open_drawer));
        waitCashTask = WaitForCashInDrawerCommand.start(getActivity(), searchByMac, new OpenDrawerListener(this));

        return false;
    }

    @Override
    public TaskHandler getHandler() {
        return waitCashTask;
    }

    @Override
    public void setHandler(TaskHandler handler) {
        waitCashTask = handler;
    }

    public static interface IRefundProgressListener {

        void onComplete(BigDecimal amountAfterRefund, ArrayList<PaymentTransactionModel> refundChildTransactions, SaleOrderModel childOrderModel);

    }

    public static void show(FragmentActivity context,
                            List<PaymentTransactionModel> transactions,
                            BigDecimal amount,
                            IRefundProgressListener listener,
                            boolean useOnlyCashTransaction,
                            SaleOrderModel childOrderModel,
                            boolean refundTips,
                            boolean isManualReturn) {
        List<PaymentTransactionModel> filteredTransactions = new ArrayList<PaymentTransactionModel>();
        for (PaymentTransactionModel transaction : transactions) {
            if (!PaymentType.SALE.equals(transaction.paymentType)) {
                Logger.d("We skip this unwanted transaction - it is no SALE one");
                continue;
            }
            if (!useOnlyCashTransaction || PaymentGateway.CASH.equals(transaction.gateway)) {
                filteredTransactions.add(transaction);
            }
        }
        DialogUtil.show(context, DIALOG_NAME, RefundCashFragmentDialog_.builder().refundTips(refundTips).isManualReturn(isManualReturn).build())
                  .setListener(listener)
                  .setTransactions(filteredTransactions)
                  .setAmount(amount)
                  .setRefundOrder(childOrderModel);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
