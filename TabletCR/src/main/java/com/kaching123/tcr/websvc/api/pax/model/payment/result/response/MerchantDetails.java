package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API.MIDownloadCommand.MIObject;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class MerchantDetails implements Serializable {

    @Expose
    @SerializedName(MIObject.PARAM_MERCHANTID)
    private int merchantId;

    @Expose
    @SerializedName(MIObject.PARAM_CUSTOM1)
    private String custom1;

    @Expose
    @SerializedName(MIObject.PARAM_CUSTOM2)
    private String custom2;

    @Expose
    @SerializedName(MIObject.PARAM_CUSTOM3)
    private String custom3;

    @Expose
    @SerializedName(MIObject.PARAM_CUSTOM4)
    private String custom4;

    @Expose
    @SerializedName(MIObject.PARAM_CREDITENABLED)
    private int creditEnabled;

    @Expose
    @SerializedName(MIObject.PARAM_DEBITENABLED)
    private int debitEnabled;

    @Expose
    @SerializedName(MIObject.PARAM_EBTENABLED)
    private int EBTEnabled;

    @Expose
    @SerializedName(MIObject.PARAM_GIFTCARDENABLED)
    private int giftCardEnabled;

    @Expose
    @SerializedName(MIObject.PARAM_LOYALTYCARDENABLED)
    private int loyaltyCardEnabled;

    @Expose
    @SerializedName(MIObject.PARAM_TIPSENABLED)
    private int tipsEnabled;

    @Expose
    @SerializedName(MIObject.PARAM_TIPSSUGGESTED)
    private int tipsSuggested;

    @Expose
    @SerializedName(MIObject.PARAM_TIPSSUGGESTION1)
    private double tipsSuggestion1;

    @Expose
    @SerializedName(MIObject.PARAM_TIPSSUGGESTION2)
    private double tipsSuggestion2;

    @Expose
    @SerializedName(MIObject.PARAM_TIPSSUGGESTION3)
    private double tipsSuggestion3;

    @Expose
    @SerializedName(MIObject.PARAM_ASKFORZIPCODE)
    private int askForZipCode;

    @Expose
    @SerializedName(MIObject.PARAM_ASKFORCVN)
    private int askForCVN;

    @Expose
    @SerializedName(MIObject.PARAM_REFUNDSNEEDPASSWORD)
    private int refundsNeedPassword;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETREFUND)
    private String ticketRefund;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETREPORTHEADER)
    private String ticketReportHeader;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETREPORTMIDDLE)
    private String ticketReportMiddle;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETREPORTFOOTER)
    private String ticketReportFooter;

    @Expose
    @SerializedName(MIObject.PARAM_EXTERNALPINPAD)
    private int externalPinpad;

    @Expose
    @SerializedName(MIObject.PARAM_UPDATEDON)
    private String updatedOn;

    @Expose
    @SerializedName(MIObject.PARAM_UPDATEDBY)
    private String updatedBy;

    @Expose
    @SerializedName(MIObject.PARAM_NAME)
    private String name;

    @Expose
    @SerializedName(MIObject.PARAM_ADDRESS1)
    private String address1;

    @Expose
    @SerializedName(MIObject.PARAM_ADDRESS2)
    private String address2;

    @Expose
    @SerializedName(MIObject.PARAM_ADDRESS3)
    private String address3;

    @Expose
    @SerializedName(MIObject.PARAM_PHONE)
    private String phone;

    @Expose
    @SerializedName(MIObject.PARAM_ASKFORCASHBACK)
    private int askForCashback;

    @Expose
    @SerializedName(MIObject.PARAM_MAXCASHBACKAMOUNT)
    private double maxCashbackAmount;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETCREDITSALES)
    private String ticketCreditSales;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETDEBITSALES)
    private String ticketDebitSales;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETEBTSALES)
    private String ticketEBTSales;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETGIFTCARDSALES)
    private String ticketGiftCardSales;

    @Expose
    @SerializedName(MIObject.PARAM_SETTLEMENTREPORTHEADER)
    private String settlementReportHeader;

    @Expose
    @SerializedName(MIObject.PARAM_SETTLEMENTREPORTMIDDLE)
    private String settlementReportMiddle;

    @Expose
    @SerializedName(MIObject.PARAM_SETTLEMENTREPORTFOOTER)
    private String settlementReportFooter;

    @Expose
    @SerializedName(MIObject.PARAM_MANUALAVSONLY)
    private int manualAVSOnly;

    @Expose
    @SerializedName(MIObject.PARAM_MANUALCVVONLY)
    private int manualCVVOnly;

    @Expose
    @SerializedName(MIObject.PARAM_DEBITAVS)
    private int debitAVS;

    @Expose
    @SerializedName(MIObject.PARAM_DEBITCVV)
    private int debitCVV;

    @Expose
    @SerializedName(MIObject.PARAM_DISCLAIMER)
    private String disclaimer;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETEBTBALANCE)
    private String ticketEBTBalance;

    @Expose
    @SerializedName(MIObject.PARAM_MINCARDCHARGE)
    private double minCardCharge;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETGIFTCARDACTIVATION)
    private String ticketGiftCardActivation;

    @Expose
    @SerializedName(MIObject.PARAM_TICKETGIFTCARDBALANCE)
    private String ticketGiftCardBalance;

    @Expose
    @SerializedName(MIObject.PARAM_FEEAMEXPERCENT)
    private double feeAmexPercent;

    @Expose
    @SerializedName(MIObject.PARAM_FEEAMEXVALUE)
    private double feeAmexValue;

    @Expose
    @SerializedName(MIObject.PARAM_FEEVISAPERCENT)
    private double feeVisaPercent;

    @Expose
    @SerializedName(MIObject.PARAM_FEEVISAVALUE)
    private double feeVisaValue;

    @Expose
    @SerializedName(MIObject.PARAM_FEEMCPERCENT)
    private double feeMCPercent;

    @Expose
    @SerializedName(MIObject.PARAM_FEEMCVALUE)
    private double feeMCValue;

    @Expose
    @SerializedName(MIObject.PARAM_FEEOTHERSPERCEN)
    private double feeOthersPercen;

    @Expose
    @SerializedName(MIObject.PARAM_FEEOTHERSVALUE)
    private double feeOthersValue;

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    public void setCustom4(String custom4) {
        this.custom4 = custom4;
    }

    public void setCreditEnabled(int creditEnabled) {
        this.creditEnabled = creditEnabled;
    }

    public void setDebitEnabled(int debitEnabled) {
        this.debitEnabled = debitEnabled;
    }

    public void setEBTEnabled(int EBTEnabled) {
        this.EBTEnabled = EBTEnabled;
    }

    public void setGiftCardEnabled(int giftCardEnabled) {
        this.giftCardEnabled = giftCardEnabled;
    }

    public void setLoyaltyCardEnabled(int loyaltyCardEnabled) {
        this.loyaltyCardEnabled = loyaltyCardEnabled;
    }

    public void setTipsEnabled(int tipsEnabled) {
        this.tipsEnabled = tipsEnabled;
    }

    public void setTipsSuggested(int tipsSuggested) {
        this.tipsSuggested = tipsSuggested;
    }

    public void setTipsSuggestion1(double tipsSuggestion1) {
        this.tipsSuggestion1 = tipsSuggestion1;
    }

    public void setTipsSuggestion2(double tipsSuggestion2) {
        this.tipsSuggestion2 = tipsSuggestion2;
    }

    public void setTipsSuggestion3(double tipsSuggestion3) {
        this.tipsSuggestion3 = tipsSuggestion3;
    }

    public void setAskForZipCode(int askForZipCode) {
        this.askForZipCode = askForZipCode;
    }

    public void setAskForCVN(int askForCVN) {
        this.askForCVN = askForCVN;
    }

    public void setRefundsNeedPassword(int refundsNeedPassword) {
        this.refundsNeedPassword = refundsNeedPassword;
    }

    public void setTicketRefund(String ticketRefund) {
        this.ticketRefund = ticketRefund;
    }

    public void setTicketReportHeader(String ticketReportHeader) {
        this.ticketReportHeader = ticketReportHeader;
    }

    public void setTicketReportMiddle(String ticketReportMiddle) {
        this.ticketReportMiddle = ticketReportMiddle;
    }

    public void setTicketReportFooter(String ticketReportFooter) {
        this.ticketReportFooter = ticketReportFooter;
    }

    public void setExternalPinpad(int externalPinpad) {
        this.externalPinpad = externalPinpad;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAskForCashback(int askForCashback) {
        this.askForCashback = askForCashback;
    }

    public void setMaxCashbackAmount(double maxCashbackAmount) {
        this.maxCashbackAmount = maxCashbackAmount;
    }

    public void setTicketCreditSales(String ticketCreditSales) {
        this.ticketCreditSales = ticketCreditSales;
    }

    public void setTicketDebitSales(String ticketDebitSales) {
        this.ticketDebitSales = ticketDebitSales;
    }

    public void setTicketEBTSales(String ticketEBTSales) {
        this.ticketEBTSales = ticketEBTSales;
    }

    public void setTicketGiftCardSales(String ticketGiftCardSales) {
        this.ticketGiftCardSales = ticketGiftCardSales;
    }

    public void setSettlementReportHeader(String settlementReportHeader) {
        this.settlementReportHeader = settlementReportHeader;
    }

    public void setSettlementReportMiddle(String settlementReportMiddle) {
        this.settlementReportMiddle = settlementReportMiddle;
    }

    public void setSettlementReportFooter(String settlementReportFooter) {
        this.settlementReportFooter = settlementReportFooter;
    }

    public void setManualAVSOnly(int manualAVSOnly) {
        this.manualAVSOnly = manualAVSOnly;
    }

    public void setManualCVVOnly(int manualCVVOnly) {
        this.manualCVVOnly = manualCVVOnly;
    }

    public void setDebitAVS(int debitAVS) {
        this.debitAVS = debitAVS;
    }

    public void setDebitCVV(int debitCVV) {
        this.debitCVV = debitCVV;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public void setTicketEBTBalance(String ticketEBTBalance) {
        this.ticketEBTBalance = ticketEBTBalance;
    }

    public void setMinCardCharge(double minCardCharge) {
        this.minCardCharge = minCardCharge;
    }

    public void setTicketGiftCardActivation(String ticketGiftCardActivation) {
        this.ticketGiftCardActivation = ticketGiftCardActivation;
    }

    public void setTicketGiftCardBalance(String ticketGiftCardBalance) {
        this.ticketGiftCardBalance = ticketGiftCardBalance;
    }

    public void setFeeAmexPercent(double feeAmexPercent) {
        this.feeAmexPercent = feeAmexPercent;
    }

    public void setFeeAmexValue(double feeAmexValue) {
        this.feeAmexValue = feeAmexValue;
    }

    public void setFeeVisaPercent(double feeVisaPercent) {
        this.feeVisaPercent = feeVisaPercent;
    }

    public void setFeeVisaValue(double feeVisaValue) {
        this.feeVisaValue = feeVisaValue;
    }

    public void setFeeMCPercent(double feeMCPercent) {
        this.feeMCPercent = feeMCPercent;
    }

    public void setFeeMCValue(double feeMCValue) {
        this.feeMCValue = feeMCValue;
    }

    public void setFeeOthersPercen(double feeOthersPercen) {
        this.feeOthersPercen = feeOthersPercen;
    }

    public void setFeeOthersValue(double feeOthersValue) {
        this.feeOthersValue = feeOthersValue;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public String getCustom1() {
        return custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    public String getCustom4() {
        return custom4;
    }

    public int getCreditEnabled() {
        return creditEnabled;
    }

    public int getDebitEnabled() {
        return debitEnabled;
    }

    public int getEBTEnabled() {
        return EBTEnabled;
    }

    public int getGiftCardEnabled() {
        return giftCardEnabled;
    }

    public int getLoyaltyCardEnabled() {
        return loyaltyCardEnabled;
    }

    public int getTipsEnabled() {
        return tipsEnabled;
    }

    public int getTipsSuggested() {
        return tipsSuggested;
    }

    public double getTipsSuggestion1() {
        return tipsSuggestion1;
    }

    public double getTipsSuggestion2() {
        return tipsSuggestion2;
    }

    public double getTipsSuggestion3() {
        return tipsSuggestion3;
    }

    public int getAskForZipCode() {
        return askForZipCode;
    }

    public int getAskForCVN() {
        return askForCVN;
    }

    public int getRefundsNeedPassword() {
        return refundsNeedPassword;
    }

    public String getTicketRefund() {
        return ticketRefund;
    }

    public String getTicketReportHeader() {
        return ticketReportHeader;
    }

    public String getTicketReportMiddle() {
        return ticketReportMiddle;
    }

    public String getTicketReportFooter() {
        return ticketReportFooter;
    }

    public int getExternalPinpad() {
        return externalPinpad;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getName() {
        return name;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress3() {
        return address3;
    }

    public String getPhone() {
        return phone;
    }

    public int getAskForCashback() {
        return askForCashback;
    }

    public double getMaxCashbackAmount() {
        return maxCashbackAmount;
    }

    public String getTicketCreditSales() {
        return ticketCreditSales;
    }

    public String getTicketDebitSales() {
        return ticketDebitSales;
    }

    public String getTicketEBTSales() {
        return ticketEBTSales;
    }

    public String getTicketGiftCardSales() {
        return ticketGiftCardSales;
    }

    public String getSettlementReportHeader() {
        return settlementReportHeader;
    }

    public String getSettlementReportMiddle() {
        return settlementReportMiddle;
    }

    public String getSettlementReportFooter() {
        return settlementReportFooter;
    }

    public int getManualAVSOnly() {
        return manualAVSOnly;
    }

    public int getManualCVVOnly() {
        return manualCVVOnly;
    }

    public int getDebitAVS() {
        return debitAVS;
    }

    public int getDebitCVV() {
        return debitCVV;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public String getTicketEBTBalance() {
        return ticketEBTBalance;
    }

    public double getMinCardCharge() {
        return minCardCharge;
    }

    public String getTicketGiftCardActivation() {
        return ticketGiftCardActivation;
    }

    public String getTicketGiftCardBalance() {
        return ticketGiftCardBalance;
    }

    public double getFeeAmexPercent() {
        return feeAmexPercent;
    }

    public double getFeeAmexValue() {
        return feeAmexValue;
    }

    public double getFeeVisaPercent() {
        return feeVisaPercent;
    }

    public double getFeeVisaValue() {
        return feeVisaValue;
    }

    public double getFeeMCPercent() {
        return feeMCPercent;
    }

    public double getFeeMCValue() {
        return feeMCValue;
    }

    public double getFeeOthersPercen() {
        return feeOthersPercen;
    }

    public double getFeeOthersValue() {
        return feeOthersValue;
    }

    @Override
    public String toString() {
        return "MerchantDetails{" +
                "merchantId=" + merchantId +
                ", custom1='" + custom1 + '\'' +
                ", custom2='" + custom2 + '\'' +
                ", custom3='" + custom3 + '\'' +
                ", custom4='" + custom4 + '\'' +
                ", creditEnabled=" + creditEnabled +
                ", debitEnabled=" + debitEnabled +
                ", EBTEnabled=" + EBTEnabled +
                ", giftCardEnabled=" + giftCardEnabled +
                ", loyaltyCardEnabled=" + loyaltyCardEnabled +
                ", tipsEnabled=" + tipsEnabled +
                ", tipsSuggested=" + tipsSuggested +
                ", tipsSuggestion1=" + tipsSuggestion1 +
                ", tipsSuggestion2=" + tipsSuggestion2 +
                ", tipsSuggestion3=" + tipsSuggestion3 +
                ", askForZipCode=" + askForZipCode +
                ", askForCVN=" + askForCVN +
                ", refundsNeedPassword=" + refundsNeedPassword +
                ", ticketRefund='" + ticketRefund + '\'' +
                ", ticketReportHeader='" + ticketReportHeader + '\'' +
                ", ticketReportMiddle='" + ticketReportMiddle + '\'' +
                ", ticketReportFooter='" + ticketReportFooter + '\'' +
                ", externalPinpad=" + externalPinpad +
                ", updatedOn='" + updatedOn + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", name='" + name + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", address3='" + address3 + '\'' +
                ", phone='" + phone + '\'' +
                ", askForCashback=" + askForCashback +
                ", maxCashbackAmount=" + maxCashbackAmount +
                ", ticketCreditSales='" + ticketCreditSales + '\'' +
                ", ticketDebitSales='" + ticketDebitSales + '\'' +
                ", ticketEBTSales='" + ticketEBTSales + '\'' +
                ", ticketGiftCardSales='" + ticketGiftCardSales + '\'' +
                ", settlementReportHeader='" + settlementReportHeader + '\'' +
                ", settlementReportMiddle='" + settlementReportMiddle + '\'' +
                ", settlementReportFooter='" + settlementReportFooter + '\'' +
                ", manualAVSOnly=" + manualAVSOnly +
                ", manualCVVOnly=" + manualCVVOnly +
                ", debitAVS=" + debitAVS +
                ", debitCVV=" + debitCVV +
                ", disclaimer='" + disclaimer + '\'' +
                ", ticketEBTBalance='" + ticketEBTBalance + '\'' +
                ", minCardCharge=" + minCardCharge +
                ", ticketGiftCardActivation='" + ticketGiftCardActivation + '\'' +
                ", ticketGiftCardBalance='" + ticketGiftCardBalance + '\'' +
                ", feeAmexPercent=" + feeAmexPercent +
                ", feeAmexValue=" + feeAmexValue +
                ", feeVisaPercent=" + feeVisaPercent +
                ", feeVisaValue=" + feeVisaValue +
                ", feeMCPercent=" + feeMCPercent +
                ", feeMCValue=" + feeMCValue +
                ", feeOthersPercen=" + feeOthersPercen +
                ", feeOthersValue=" + feeOthersValue +
                '}';
    }
}
