package com.kaching123.tcr.commands.payment.other;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.other.CheckTransaction;
import com.kaching123.tcr.model.payment.other.CheckTransactionFactory;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

public class CheckGateway implements IPaymentGateway<CheckTransaction, Void> {

    private static final BigDecimal MIN_VALUE = new BigDecimal("0.01");


    @Override
    public TaskHandler sale(Context context, Object callback, User user, Void ignore, Transaction transaction) {
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        return OtherSaleCommand.start(context, callback, transaction);
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
        return CheckRefundCommand.start(context, callback, transaction, amount, childOrderModel, isManualReturn);
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
        return CheckVoidCommand.start(context, callback, transaction, childOrderModel, needToCancel);
    }

    @Override
    public BigDecimal minimalAmount() {
        return MIN_VALUE;
    }

    @Override
    public CheckTransaction createTransaction(Context context, BigDecimal amount, String orderGuid) {
        return CheckTransactionFactory.create(TcrApplication.get().getOperatorGuid(), amount, orderGuid);
    }

    @Override
    public boolean enabled() {
        return TcrApplication.get().isCheckEnabled();
    }
}
