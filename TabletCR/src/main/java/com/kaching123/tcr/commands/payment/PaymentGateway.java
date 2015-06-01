package com.kaching123.tcr.commands.payment;

import com.kaching123.tcr.commands.payment.blackstone.payment.BlackGateway;
import com.kaching123.tcr.commands.payment.cash.CashGateway;
import com.kaching123.tcr.commands.payment.credit.CreditGateway;
import com.kaching123.tcr.commands.payment.other.CheckGateway;
import com.kaching123.tcr.commands.payment.other.OfflineCreditGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.paypal.PayPalGateway;

/**
 * @author Ivan v. Rikhmayer
 */
public enum PaymentGateway {

    /*0*/PAX(PaxGateway.credit()),
    /*1*/PAX_EBT_FOODSTAMP(PaxGateway.ebtFoodstamp()),
    /*2*/PAX_EBT_CASH(PaxGateway.ebtCash()),
    /*3*/PAX_DEBIT(PaxGateway.debit()),
    /*4*/BLACKSTONE(new BlackGateway()),//gateway for credit card, shop can use just one
    /*5*/CASH(new CashGateway()),
    /*6*/CREDIT(new CreditGateway()),//credit receipt, NOT credit card
    /*7*/OFFLINE_CREDIT(new OfflineCreditGateway()),
    /*8*/CHECK(new CheckGateway()),
    /*9*/PAYPAL(new PayPalGateway());//gateway for credit card, shop can use just one, this is not implemented yet


    private IPaymentGateway gateway;

    private PaymentGateway(IPaymentGateway blackstone) {
        this.gateway = blackstone;
    }

    public IPaymentGateway gateway() {
        return gateway;
    }

    public static IPaymentGateway getCreditCardPaymentMethod() {//get gateway for credit card
        //shopinfo.getSelectedCreditCardGateway
        return BLACKSTONE.gateway;
    }

    public boolean isCreditCard() {
        return this == BLACKSTONE || this == PAYPAL || this == PAX
                || this == PAX_EBT_CASH || this == PAX_EBT_FOODSTAMP || this == PAX_DEBIT;
    }
    public boolean isEbt ()
    {
        return this == PAX_EBT_CASH || this == PAX_EBT_FOODSTAMP;
    }



    public boolean isTrueCreditCard(){ //not ebt, not debit
        return this == BLACKSTONE || this == PAX;
    }
}
