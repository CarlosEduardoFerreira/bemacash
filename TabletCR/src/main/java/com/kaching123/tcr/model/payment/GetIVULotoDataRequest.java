package com.kaching123.tcr.model.payment;

import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;
import com.kaching123.tcr.websvc.api.prepaid.Receipt;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetIVULotoDataRequest extends RequestBase {

    public String MID;
    public String TID;
    public String Password;
    public long transactionId;
    public BigDecimal amount;
    public Receipt receipt;

    public  GetIVULotoDataRequest()
    {

    }

    public GetIVULotoDataRequest(String MID, String TID, String Password, long transactionId, BigDecimal amount, Receipt receipt)
    {
        this.MID = MID;
        this.TID = TID;
        this.Password = Password;
        this.transactionId = transactionId;
        this.amount = amount;
        this.receipt = receipt;
    }

    @Override
    public String toString() {
        return "GetIVULotoDataRequest{" +
                "MID='" + MID + '\'' +
                ", TID='" + TID + '\'' +
                ", Amount='" + amount + '\'' +
                ", Password" +
                ", transactionId=" + transactionId +
                ", Receipt merchantId=" + receipt.merchantId +
                ", Receipt municipalTax=" + receipt.municipalTax +
                ", Receipt stateTax=" + receipt.stateTax +
                ", Receipt subTotal=" + receipt.subTotal +
                ", Receipt tenderType=" + receipt.tenderType +
                ", Receipt terminalId=" + receipt.terminalId +
                ", Receipt terminalPassword=" + receipt.terminalPassword +
                ", Receipt total=" + receipt.total +
                ", Receipt txDate=" + receipt.txDate +
                ", Receipt txType=" + receipt.txType +
                '}';
    }
}
