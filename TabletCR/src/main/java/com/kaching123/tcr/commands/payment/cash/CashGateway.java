package com.kaching123.tcr.commands.payment.cash;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.cash.CashTransaction;
import com.kaching123.tcr.model.payment.general.transaction.CashTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
public class CashGateway implements IPaymentGateway<CashTransaction, Void> {

    private static final BigDecimal MIN_VALUE = new BigDecimal("0.01");


    @Override
    public TaskHandler sale(Context context, Object callback, User user, Void ignore, Transaction transaction) {
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        return CashSaleCommand.start(context, callback, transaction);
    }

    @Override
    public TaskHandler refund(Context context,
                              Object callback,
                              User user,
                              Void ignore,
                              PaymentTransactionModel transaction,
                              BigDecimal amount,
                              SaleOrderModel childOrderModel,
                              boolean refundTips,
                              boolean isManualReturn) {
        return CashRefundCommand.start(context, callback, transaction, amount, childOrderModel, refundTips, isManualReturn);
    }

    @Override
    public TaskHandler voidMe(Context context,
                              Object callback,
                              User user,
                              PaymentTransactionModel transaction,
                              SaleOrderModel childOrderModel,
                              boolean needToCancel) {
        transaction.status = PaymentStatus.SUCCESS;
        transaction.paymentType = PaymentType.VOID;
        transaction.declineReason = PaymentStatus.SUCCESS.toString();
        return CashVoidCommand.start(context, callback, transaction, childOrderModel, needToCancel);
    }

    @Override
    public BigDecimal minimalAmount() {
        return MIN_VALUE;
    }

    @Override
    public CashTransaction createTransaction(Context context, BigDecimal amount, String orderGuid) {
        return CashTransactionFactory.create(TcrApplication.get().getOperatorGuid(), amount, orderGuid);
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
