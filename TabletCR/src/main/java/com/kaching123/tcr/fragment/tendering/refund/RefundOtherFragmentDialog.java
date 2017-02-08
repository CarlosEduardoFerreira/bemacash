package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.other.CheckRefundCommand;
import com.kaching123.tcr.commands.payment.other.OfflineCreditRefundCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.util.AnimationUtils;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@EFragment
public class RefundOtherFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = RefundOtherFragmentDialog.class.getSimpleName();

    @FragmentArg
    protected boolean isManualReturn;

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

    @FragmentArg
    protected Type type;

    public enum Type {
        OFFLINE_CREDIT, CHECK
    }

    private SaleOrderModel returnOrder;

    private LinkedBlockingQueue<PaymentTransactionModel> transactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    private ArrayList<PaymentTransactionModel> refundChildTransactions = new ArrayList<PaymentTransactionModel>();
    private IRefundProgressListener listener;
    private BigDecimal amount;
    private int max;
    private int current;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.voiding_processor;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setCancelable(false);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.base_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        getNegativeButton().setTextColor(getResources().getColor(R.color.gray_dark));
        getNegativeButton().setEnabled(false);

        init();
    }

    private void complete() {
        description.setText(R.string.refund_result_ok);
        enableButton(true);
        flipper.setDisplayedChild(2);
    }

    private void init() {
        AnimationUtils.applyFlippingEffect(getActivity(), flipper);
        message.setText(String.format("voided %d of %d", 0, max));
        enableButton(false);
        next();
    }

    protected void next() {
        Logger.d("Welcome to new round, biller is %s", amount.toString());
        PaymentTransactionModel transaction = transactions.poll();
        if (transaction == null || BigDecimal.ZERO.compareTo(amount) == 0) {
            complete();
            return;
        }
        message.setText(String.format("voided %d of %d", current++, max));
        BigDecimal possibleAmount = amount.min(transaction.availableAmount);
        if (possibleAmount.compareTo(BigDecimal.ZERO) > 0) {
            IPaymentGateway paymentGateway;
            switch (type) {
                case OFFLINE_CREDIT:
                    paymentGateway = PaymentGateway.OFFLINE_CREDIT.gateway();
                    break;
                case CHECK:
                    paymentGateway = PaymentGateway.CHECK.gateway();
                    break;
                default:
                    throw new IllegalArgumentException("unknown type: " + type);
            }
            paymentGateway.refund(getActivity(), this, null, null, transaction, possibleAmount, returnOrder, false, isManualReturn);
        } else {
            next();
        }
    }

    @OnSuccess(OfflineCreditRefundCommand.class)
    public void onOfflineCreditSuccess(@Param(OfflineCreditRefundCommand.RESULT_DATA) PaymentTransactionModel transaction,
                                  @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        onSuccess(transaction, childOrderModel);
    }

    @OnFailure(OfflineCreditRefundCommand.class)
    public void onOfflineCreditFail(@Param(OfflineCreditRefundCommand.RESULT_DATA) PaymentTransactionModel transaction,
                               @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        onFail(childOrderModel);
    }

    @OnSuccess(CheckRefundCommand.class)
    public void onCheckSuccess(@Param(CheckRefundCommand.RESULT_DATA) PaymentTransactionModel transaction,
                          @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        onSuccess(transaction, childOrderModel);
    }

    @OnFailure(CheckRefundCommand.class)
    public void onCheckFail(@Param(CheckRefundCommand.RESULT_DATA) PaymentTransactionModel transaction,
                       @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        onFail(childOrderModel);
    }

    protected void onSuccess(PaymentTransactionModel transaction, SaleOrderModel childOrderModel) {
        this.returnOrder = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        amount = amount.add(transaction.amount);//transaction.biller will be negative
        refundChildTransactions.add(transaction);
        next();
    }

    protected void onFail(SaleOrderModel childOrderModel) {
        this.returnOrder = childOrderModel;
        next();
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
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

    public RefundOtherFragmentDialog setTransactions(List<PaymentTransactionModel> transactions) {
        max = transactions.size();
        this.transactions.addAll(transactions);
        return this;
    }

    public RefundOtherFragmentDialog setListener(IRefundProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public RefundOtherFragmentDialog setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public RefundOtherFragmentDialog setChildOrder(SaleOrderModel childOrder) {
        this.returnOrder = childOrder;
        return this;
    }

    public static interface IRefundProgressListener {

        void onComplete(BigDecimal amountAfterRefund, ArrayList<PaymentTransactionModel> refundChildTransactions, SaleOrderModel childOrderModel);

    }

    public static void show(FragmentActivity context,
                            List<PaymentTransactionModel> transactions,
                            BigDecimal amount,
                            IRefundProgressListener listener,
                            Type type,
                            SaleOrderModel childOrder,
                            boolean isManualReturn) {
        PaymentGateway paymentGateway;
        switch (type) {
            case OFFLINE_CREDIT:
                paymentGateway = PaymentGateway.OFFLINE_CREDIT;
                break;
            case CHECK:
                paymentGateway = PaymentGateway.CHECK;
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
        List<PaymentTransactionModel> filteredTransactions = new ArrayList<PaymentTransactionModel>();
        for (PaymentTransactionModel transaction : transactions) {
            if (!PaymentType.SALE.equals(transaction.paymentType)) {
                Logger.d("We skip this unwanted transaction - it is no SALE one");
                continue;
            }
            if (paymentGateway.equals(transaction.gateway)) {
                filteredTransactions.add(transaction);
            }
        }
        DialogUtil.show(context, DIALOG_NAME, RefundOtherFragmentDialog_.builder().type(type).isManualReturn(isManualReturn).build())
                  .setListener(listener)
                  .setTransactions(filteredTransactions)
                  .setAmount(amount)
                  .setChildOrder(childOrder);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
