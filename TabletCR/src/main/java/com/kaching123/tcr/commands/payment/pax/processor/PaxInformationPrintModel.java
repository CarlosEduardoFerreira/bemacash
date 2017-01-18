package com.kaching123.tcr.commands.payment.pax.processor;

import java.math.BigDecimal;

/**
 * Created by ferre on 16/01/2017.
 */
public class PaxInformationPrintModel {
    public static String Pax_CardType = "";
    public static String Pax_AccountNumber = "";
    public static String Pax_Entry = "";
    public static String Pax_AID = "";
    public static String Pax_Approval = "";
    public static BigDecimal Pax_Value = BigDecimal.ZERO;
    public static byte[] Pax_DigitalSignature = null;
}
