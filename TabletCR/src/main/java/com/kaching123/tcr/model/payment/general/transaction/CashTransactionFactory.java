package com.kaching123.tcr.model.payment.general.transaction;

import com.kaching123.tcr.commands.payment.TransactionFactory;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.cash.CashTransaction;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold transaction data
 */
public class CashTransactionFactory extends TransactionFactory {


    private CashTransactionFactory() {
    }

    public static CashTransaction create(String operatorGuid, BigDecimal amount, String orderGuid) {
        return new CashTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.SALE);
    }

    public static CashTransaction createChild(String operatorGuid, BigDecimal amount, String orderGuid, String parentGuid, String cardName, boolean isPreauth) {
        amount = CalculationUtil.negative(amount);
        return new CashTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
//                .setCardName(cardName)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.REFUND)
                .setParentTransactionGuid(parentGuid)
                .setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY)
                .setIsPreauth(isPreauth);
    }

}
