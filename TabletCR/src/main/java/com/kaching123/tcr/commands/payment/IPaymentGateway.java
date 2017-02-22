package com.kaching123.tcr.commands.payment;

import android.content.Context;

import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
public interface IPaymentGateway<T extends Transaction<T>, E> {

    /**
     * Go on with a sale
     */
    public <C> TaskHandler sale(Context context, C callback,
                            User user, E card, Transaction transaction);

    /**
     * Go on with a refund
     */
    public TaskHandler refund(Context context, Object callback,
                              User user, E card, PaymentTransactionModel transaction, BigDecimal amount, SaleOrderModel childOrderModel, boolean refundTips, boolean isManualReturn);

    /**
     * Go on with a void
     */
    public TaskHandler voidMe(Context context, Object callback,
                              User user, PaymentTransactionModel transaction, SaleOrderModel childOrderModel, boolean needToCancel);


    public BigDecimal minimalAmount();

    public T createTransaction(Context context, BigDecimal amount, String orderGuid);

    public boolean  enabled();
}
