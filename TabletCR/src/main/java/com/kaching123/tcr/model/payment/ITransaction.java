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

    public String getParentTransactionGuid();

    public PaymentType getPaymentType();

    public String getGuid();

    public String getOrderGuid();

    public BigDecimal getAmount();

    public BigDecimal getAvailableAmount();

    public PaymentStatus getStatus();

    public PaymentGateway getGateway();

    public String getPaymentId();

    public String getAuthorizationNumber();

    public String getDeclineReason();

    public String getOperatorId();

    public String getCardName();

    public String getLastFour();

    BigDecimal getChangeAmount();

    public boolean getIsPreauth();

    public BigDecimal getCashBack();

    public BigDecimal getBalance();

    public String getApplicationIdentifier();

    public String getResultCode();

    public String getEntryMethod();

    public String getApplicationCryptogramType();

}
