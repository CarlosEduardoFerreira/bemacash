package com.kaching123.tcr.model.payment.other;

import com.kaching123.tcr.commands.payment.TransactionFactory;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

public class OfflineCreditTransactionFactory extends TransactionFactory {


    private OfflineCreditTransactionFactory() {
    }

    public static OfflineCreditTransaction create(String operatorGuid, BigDecimal amount, String orderGuid) {
        return new OfflineCreditTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.SALE);
    }

    public static OfflineCreditTransaction createChild(String operatorGuid, BigDecimal amount, String orderGuid, String parentGuid, String cardName) {
        amount = CalculationUtil.negative(amount);
        return new OfflineCreditTransaction(getTransactionUID(), amount)
                .setOrderId(orderGuid)
                .setCardName(cardName)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.REFUND)
                .setParentTransactionGuid(parentGuid)
                .setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
    }

}
