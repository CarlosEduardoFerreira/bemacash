package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackClosePreauthCommand.BaseClosePreauthCallback;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackDoSettlementCommand.BaseDoSettlementCallback;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackProcessPreauthCommand.BaseProcessPreauthCallback;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.BlackStoneTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ClosePreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoFullRefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoSettlementRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoVoidRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ProcessPreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.RefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.SaleRequest;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.BlackStoneTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to proxy comman executions, just in case they would be called from multiple plases
 */
public class BlackGateway implements IPaymentGateway<BlackStoneTransaction, CreditCard> {

    private static final BigDecimal MIN_VALUE = new BigDecimal("0.01");

    @Override
    public TaskHandler sale(Context context,
                            Object callback,
                            User user,
                            CreditCard card,
                            Transaction transaction) {
        return BlackSaleCommand.start(context, callback, new SaleRequest().setCard(card).setUser(user).setTransaction(transaction));
    }

    @Override
    public TaskHandler refund(Context context,
                              Object callback,
                              User user,
                              CreditCard card,
                              PaymentTransactionModel transaction,
                              BigDecimal amount,
                              SaleOrderModel childOrderModel,
                              boolean refundTips,
                              boolean isManualReturn) {
        return BlackRefundCommand.start(context,
                                        callback,
                                        new RefundRequest().setCard(card)
                                                           .setUser(user)
                                                           .setTransactionModel(transaction)
                                                           .setTransaction(transaction.toTransaction())
                                                           .setAmount(amount),
                                        amount,
                                        childOrderModel,
                                        refundTips,
                                        isManualReturn
        );
    }

    @Override
    public TaskHandler voidMe(Context context,
                              Object callback,
                              User user,
                              PaymentTransactionModel transaction,
                              SaleOrderModel childOrderModel,
                              boolean needToCancel) {
        return BlackVoidCommand.start(context,
                                      callback,
                                      new DoFullRefundRequest().setTransactionModel(transaction).setUser(user),
                                      childOrderModel,
                                      needToCancel);
    }

    public TaskHandler processPreauth(Context context,
                            BaseProcessPreauthCallback callback,
                            User user,
                            CreditCard card,
                            Transaction transaction) {
        return BlackProcessPreauthCommand.start(context, callback, new ProcessPreauthRequest().setCard(card).setUser(user).setTransaction(transaction));
    }

    public TaskHandler closePreauth(Context context,
                                      BaseClosePreauthCallback callback,
                                      User user,
                                      PaymentTransactionModel transactionModel,
                                      BigDecimal tipsAmount,
                                      String comments,
                                      String tippedEmployeeId) {
        return BlackClosePreauthCommand.start(context, callback, new ClosePreauthRequest().setUser(user).setTransactionModel(transactionModel).setAdditionalTipsAmount(tipsAmount), comments, tippedEmployeeId);
    }

    public TaskHandler doSettlement(Context context,
                                    BaseDoSettlementCallback callback,
                                    User user) {
        return BlackDoSettlementCommand.start(context, callback, new DoSettlementRequest().setUser(user));
    }

    @Override
    public BigDecimal minimalAmount() {
        return MIN_VALUE;
    }

    @Override
     public BlackStoneTransaction createTransaction(Context context, BigDecimal amount, String orderGuid) {
        return BlackStoneTransactionFactory.create(TcrApplication.get().getOperatorGuid(), amount, orderGuid, false);
    }


    public BlackStoneTransaction createPreauthTransaction(Context context, BigDecimal amount, String orderGuid) {
        return BlackStoneTransactionFactory.create(TcrApplication.get().getOperatorGuid(), amount, orderGuid, true);
    }

}
