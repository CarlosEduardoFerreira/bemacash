package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by gdubina on 25/02/14.
 */
public abstract class RefundBaseFragmentDialog extends StyledDialogFragment {

    protected BigDecimal refundAmount;

    protected int initTransactionsCount;
    protected int currentStep = 0;

    protected LinkedBlockingQueue<PaymentTransactionModel> transactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    protected ArrayList<PaymentTransactionModel> refundChildTransactions = new ArrayList<PaymentTransactionModel>();
    protected IRefundProgressListener refundListener;
    protected SaleOrderModel returnOrder;

    public RefundBaseFragmentDialog setReturnOrder(SaleOrderModel returnOrder) {
        this.returnOrder = returnOrder;
        return this;
    }

    protected RefundBaseFragmentDialog setTransactions(List<PaymentTransactionModel> transactions) {
        initTransactionsCount = transactions.size();
        this.transactions.addAll(transactions);
        return this;
    }

    public RefundBaseFragmentDialog setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    public RefundBaseFragmentDialog setRefundListener(IRefundProgressListener refundListener) {
        this.refundListener = refundListener;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        enablePositiveButtons(false);
    }

    protected void completeRefund(){
        refundListener.onComplete(refundAmount, refundChildTransactions, returnOrder);
    }

    protected abstract void updateUI(int currentStep, int steps);
    protected abstract void callRefundCommand(PaymentTransactionModel transaction, BigDecimal amount, SaleOrderModel returnOrder);

    protected void onRefundCommandSuccess(PaymentTransactionModel childTransaction, BigDecimal refundedAmount, SaleOrderModel returnOrder){
        this.returnOrder = returnOrder;
        this.refundChildTransactions.add(childTransaction);
        this.refundAmount = this.refundAmount.add(refundedAmount);
        newRefundIteration();
    }

    protected void newRefundIteration() {
        Logger.d("Welcome to new round, biller is %s", refundAmount);
        PaymentTransactionModel transaction = transactions.poll();
        if (transaction == null || BigDecimal.ZERO.compareTo(refundAmount) == 0) {
            completeRefund();
            return;
        }
        updateUI(currentStep, initTransactionsCount);
        currentStep++;
        BigDecimal possibleAmount = refundAmount.min(transaction.availableAmount);
        if (possibleAmount.compareTo(BigDecimal.ZERO) > 0) {
            callRefundCommand(transaction, possibleAmount, returnOrder);
        } else {
            newRefundIteration();
        }
    }

    public static interface IRefundProgressListener {
        void onComplete(BigDecimal amountAfterRefund, ArrayList<PaymentTransactionModel> refundChildTransactions, SaleOrderModel childOrderModel);
    }
}
