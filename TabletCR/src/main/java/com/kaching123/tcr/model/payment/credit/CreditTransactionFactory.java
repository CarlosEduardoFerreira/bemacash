package com.kaching123.tcr.model.payment.credit;

import com.kaching123.tcr.commands.payment.TransactionFactory;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditTransactionFactory {

    public static CreditTransaction create(String operatorGuid, BigDecimal amount, String orderGuid, String creditReceiptGuid) {

        return new CreditTransaction(TransactionFactory.getTransactionUID(), amount, creditReceiptGuid)
                .setOrderId(orderGuid)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.SALE);
    }

    public static CreditTransaction createChild(String operatorGuid,
                                                BigDecimal amount,
                                                String orderGuid,
                                                String parentGuid,
                                                String creditReceiptGuid,
                                                boolean isPreauth) {
        amount = CalculationUtil.negative(amount);
        return new CreditTransaction(TransactionFactory.getTransactionUID(), amount, creditReceiptGuid)
                .setOrderId(orderGuid)
                .setOperatorId(operatorGuid)
                .setPaymentType(PaymentType.REFUND)
                .setParentTransactionGuid(parentGuid)
                .setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY)
                .setIsPreauth(isPreauth);
    }
}
