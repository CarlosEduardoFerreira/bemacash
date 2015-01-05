package com.kaching123.tcr.model.payment.general.transaction;

import com.kaching123.tcr.commands.payment.TransactionFactory;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.BlackStoneTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold transaction data
 */
public class BlackStoneTransactionFactory extends TransactionFactory {

    private BlackStoneTransactionFactory() {
    }

    public static BlackStoneTransaction create(String operatorGuid, BigDecimal amount, String orderGuid, boolean isPreauth) {
        return new BlackStoneTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.SALE)
                .setIsPreauth(isPreauth);
    }

    public static BlackStoneTransaction createRefundChild(String operatorGuid, BigDecimal amount, String orderGuid, String parentGuid, String cardName, boolean isPreauth) {
        return new BlackStoneTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setCardName(cardName)
                .setOperatorId(operatorGuid)
                .setParentTransactionGuid(parentGuid)
                .setPaymentType(PaymentType.REFUND)
                .setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY)
                .setIsPreauth(isPreauth);
    }

}
