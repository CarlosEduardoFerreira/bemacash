package com.kaching123.tcr.model.payment;


import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public interface ITransaction {

    String getParentTransactionGuid();

    PaymentType getPaymentType();

    String getGuid();

    String getOrderGuid();

    BigDecimal getAmount();

    BigDecimal getAvailableAmount();

    PaymentStatus getStatus();

    PaymentGateway getGateway();

    String getPaymentId();

    String getAuthorizationNumber();

    String getDeclineReason();

    String getOperatorId();

    String getCardName();

    String getLastFour();

    BigDecimal getChangeAmount();

    boolean getIsPreauth();

    BigDecimal getCashBack();

    BigDecimal getBalance();

    String getApplicationIdentifier();

    String getResultCode();

    String getEntryMethod();

    String getApplicationCryptogramType();

    String getCustomerName();

    byte[] getPaxDigitalSignature();

}
