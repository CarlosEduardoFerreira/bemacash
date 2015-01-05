package com.kaching123.tcr.model.payment.blackstone.pax;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.TransactionFactory;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import java.math.BigDecimal;

public class PaxTransactionFactory {

    public static PaxTransaction create(String operatorGuid, String orderGuid, PaymentGateway paxGateway, boolean isPreauth) {
        return new PaxTransaction(TransactionFactory.getTransactionUID(), (SaleActionResponse) null)
                .setOrderId(orderGuid)
                .setGateway(paxGateway)
                .setOperatorId(operatorGuid)
                .setIsPreauth(isPreauth);
    }

    public static PaxTransaction createChild(String operatorGuid,
                                             BigDecimal amount,
                                                String orderGuid,
                                                String parentGuid,
                                                String cardName,
                                                PaymentGateway paxGateway,
                                                boolean isPreauth) {
        amount = CalculationUtil.negative(amount);
        return new PaxTransaction(TransactionFactory.getTransactionUID(), (SaleActionResponse) null)
                .setOrderId(orderGuid)
                .setCardName(cardName)
                .setAmount(amount)
                .setGateway(paxGateway)
                .setOperatorId(operatorGuid)
                .setParentTransactionGuid(parentGuid)
                .setPaymentType(PaymentType.REFUND)
                .setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY)
                .setIsPreauth(isPreauth);
    }
}
