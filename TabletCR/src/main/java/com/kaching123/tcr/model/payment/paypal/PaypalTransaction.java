package com.kaching123.tcr.model.payment.paypal;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

/**
 * Created by gdubina on 24/02/14.
 */
public class PaypalTransaction extends Transaction<PaypalTransaction>{
    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.PAYPAL;
    }
}
