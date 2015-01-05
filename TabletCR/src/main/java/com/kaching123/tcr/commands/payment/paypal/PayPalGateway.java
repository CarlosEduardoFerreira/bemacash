package com.kaching123.tcr.commands.payment.paypal;

import android.content.Context;

import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.paypal.PaypalTransaction;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold future implementations of the pay pal service
 */
public class PayPalGateway implements IPaymentGateway<PaypalTransaction, CreditCard> {

    private static final BigDecimal MIN_VALUE = new BigDecimal("0.01");

    @Override
    public TaskHandler sale(Context context, Object callback, User user, CreditCard card, Transaction transaction) {
        return null;
    }

    @Override
    public TaskHandler refund(Context context, Object callback, User user, CreditCard card, PaymentTransactionModel transaction, BigDecimal amount, SaleOrderModel childOrderModel, boolean refundTips, boolean isManualReturn) {
        return null;
    }

    @Override
    public TaskHandler voidMe(Context context, Object callback, User user, PaymentTransactionModel transaction, SaleOrderModel childOrderModel, boolean needToCancel) {
        return null;
    }


    @Override
    public BigDecimal minimalAmount() {
        return MIN_VALUE;
    }

    @Override
    public PaypalTransaction createTransaction(Context context, BigDecimal amount, String orderGuid) {
        return null;
    }
}
