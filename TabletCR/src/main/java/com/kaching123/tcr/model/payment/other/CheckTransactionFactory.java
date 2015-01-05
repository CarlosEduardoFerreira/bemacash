package com.kaching123.tcr.model.payment.other;

import com.kaching123.tcr.commands.payment.TransactionFactory;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

public class CheckTransactionFactory extends TransactionFactory {


    private CheckTransactionFactory() {
    }

    public static CheckTransaction create(String operatorGuid, BigDecimal amount, String orderGuid) {
        return new CheckTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.SALE);
    }

    public static CheckTransaction createChild(String operatorGuid, BigDecimal amount, String orderGuid, String parentGuid, String cardName) {
        amount = CalculationUtil.negative(amount);
        return new CheckTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setCardName(cardName)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.REFUND)
                .setParentTransactionGuid(parentGuid)
                .setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
    }

}
