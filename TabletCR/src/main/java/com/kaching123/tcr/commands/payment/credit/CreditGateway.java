package com.kaching123.tcr.commands.payment.credit;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.commands.payment.credit.CreditRefundCommand.CreditRefundCommandBaseCallback;
import com.kaching123.tcr.commands.payment.credit.CreditSaleCommand.CreditSaleCommandBaseCallback;
import com.kaching123.tcr.commands.payment.credit.CreditVoidCommand.CreditVoidCommandBaseCallback;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.credit.CreditReceiptData;
import com.kaching123.tcr.model.payment.credit.CreditTransaction;
import com.kaching123.tcr.model.payment.credit.CreditTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditGateway implements IPaymentGateway<CreditTransaction, CreditReceiptData> {

    @Override
    public TaskHandler sale(Context context, Object callback, User user, CreditReceiptData card, Transaction transaction) {
        assert callback instanceof CreditSaleCommandBaseCallback;
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        return CreditSaleCommand.start(context, (CreditSaleCommandBaseCallback)callback, transaction, card);
    }

    @Override
    public TaskHandler refund(Context context, Object callback, User user, CreditReceiptData card, PaymentTransactionModel transaction, BigDecimal amount, SaleOrderModel childOrderModel, boolean refundTips, boolean isManualReturn) {
        assert callback instanceof CreditRefundCommandBaseCallback;
        //return CreditRefundCommand.start(context, (CreditRefundCommandBaseCallback)callback, transaction, amount, childOrderModel);
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskHandler voidMe(Context context, Object callback, User user, PaymentTransactionModel transaction, SaleOrderModel childOrderModel, boolean needToCancel) {
        assert callback instanceof CreditVoidCommandBaseCallback;
        return CreditVoidCommand.start(context, (CreditVoidCommandBaseCallback)callback, transaction, childOrderModel, needToCancel);
    }

    @Override
    public BigDecimal minimalAmount() {
        return BigDecimal.ZERO;
    }

    @Override
    public CreditTransaction createTransaction(Context context, BigDecimal amount, String orderGuid) {
        return CreditTransactionFactory.create(TcrApplication.get().getOperatorGuid(), amount, orderGuid, null);
    }
}
