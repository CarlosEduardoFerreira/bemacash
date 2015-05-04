package com.kaching123.tcr.websvc.api.pax.api;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public abstract class WebAPI {

    public final static class WEB_API extends WebAPI {

        public static final class Command {
            public static final String PARAM_API_KEY = "api_key";
            public static final String PARAM_ENTITY = "entity";
            public static final String PARAM_SHOP_ID = "shop_id";

            public static final String PARAM_STATUS = "status";
            public static final String PARAM_MESSAGE = "message";
            public static final String PARAM_R_ENTITY= "entity";
        }
    }

    public final static class PAX_API extends WebAPI {

        public static final class Command {
            public static final String PARAM_MESSAGE = "message";
            public static final String PARAM_RESPONSE = "response";
            public static final String PARAM_DETAILS = "details";
            public static final String PARAM_VERSION = "version";

            public static final String PARAM_RESPONSECODE = "ResponseCode";
        }

        public static final class DisplayCommand {

            public static final String ACTION = "hello";
            public static final int ARG_IGNORE_PRINTER = 0;

            public static final String PARAM_DISPLAY = "display";
            public static final String PARAM_USE_INTERNAL_PRINTER = "use-internal-printer";

        }

        public static final class MIDownloadCommand {

            public static final String ACTION = "merchant-info-download";
            public static final String ACTION_EASY = "merchant-info";

            public static final String PARAM_MERCHANTDETAILS = "MerchantDetails";

            public static final String PARAM_DISPLAY = "display";

            public static final class MIObject {

                public static final String PARAM_MERCHANTID = "MerchantId";
                public static final String PARAM_CUSTOM1 = "Custom1";
                public static final String PARAM_CUSTOM2 = "Custom2";
                public static final String PARAM_CUSTOM3 = "Custom3";
                public static final String PARAM_CUSTOM4 = "Custom4";
                public static final String PARAM_CREDITENABLED = "CreditEnabled";
                public static final String PARAM_DEBITENABLED = "DebitEnabled";
                public static final String PARAM_EBTENABLED = "EBTEnabled";
                public static final String PARAM_GIFTCARDENABLED = "GiftCardEnabled";
                public static final String PARAM_LOYALTYCARDENABLED = "LoyaltyCardEnabled";
                public static final String PARAM_TIPSENABLED = "TipsEnabled";
                public static final String PARAM_TIPSSUGGESTED = "TipsSuggested";
                public static final String PARAM_TIPSSUGGESTION1 = "TipsSuggestion1";
                public static final String PARAM_TIPSSUGGESTION2 = "TipsSuggestion2";
                public static final String PARAM_TIPSSUGGESTION3 = "TipsSuggestion3";
                public static final String PARAM_ASKFORZIPCODE = "AskForZipCode";
                public static final String PARAM_ASKFORCVN = "AskForCVN";
                public static final String PARAM_REFUNDSNEEDPASSWORD = "RefundsNeedPassword";
                public static final String PARAM_TICKETREFUND = "TicketRefund";
                public static final String PARAM_TICKETREPORTHEADER = "TicketReportHeader";
                public static final String PARAM_TICKETREPORTMIDDLE = "TicketReportMiddle";
                public static final String PARAM_TICKETREPORTFOOTER = "TicketReportFooter";
                public static final String PARAM_EXTERNALPINPAD = "ExternalPinpad";
                public static final String PARAM_UPDATEDON = "UpdatedOn";
                public static final String PARAM_UPDATEDBY = "UpdatedBy";
                public static final String PARAM_NAME = "Name";
                public static final String PARAM_ADDRESS1 = "Address1";
                public static final String PARAM_ADDRESS2 = "Address2";
                public static final String PARAM_ADDRESS3 = "Address3";
                public static final String PARAM_PHONE = "Phone";
                public static final String PARAM_ASKFORCASHBACK = "AskForCashback";
                public static final String PARAM_MAXCASHBACKAMOUNT = "MaxCashbackAmount";
                public static final String PARAM_TICKETCREDITSALES = "TicketCreditSales";
                public static final String PARAM_TICKETDEBITSALES = "TicketDebitSales";
                public static final String PARAM_TICKETEBTSALES = "TicketEBTSales";
                public static final String PARAM_TICKETGIFTCARDSALES = "TicketGiftCardSales";
                public static final String PARAM_SETTLEMENTREPORTHEADER = "SettlementReportHeader";
                public static final String PARAM_SETTLEMENTREPORTMIDDLE = "SettlementReportMiddle";
                public static final String PARAM_SETTLEMENTREPORTFOOTER = "SettlementReportFooter";
                public static final String PARAM_MANUALAVSONLY = "ManualAVSOnly";
                public static final String PARAM_MANUALCVVONLY = "ManualCVVOnly";
                public static final String PARAM_DEBITAVS = "DebitAVS";
                public static final String PARAM_DEBITCVV = "DebitCVV";
                public static final String PARAM_DISCLAIMER = "Disclaimer";
                public static final String PARAM_TICKETEBTBALANCE = "TicketEBTBalance";
                public static final String PARAM_MINCARDCHARGE = "MinCardCharge";
                public static final String PARAM_TICKETGIFTCARDACTIVATION = "TicketGiftCardActivation";
                public static final String PARAM_TICKETGIFTCARDBALANCE = "TicketGiftCardBalance";
                public static final String PARAM_FEEAMEXPERCENT = "FeeAmexPercent";
                public static final String PARAM_FEEAMEXVALUE = "FeeAmexValue";
                public static final String PARAM_FEEVISAPERCENT = "FeeVisaPercent";
                public static final String PARAM_FEEVISAVALUE = "FeeVisaValue";
                public static final String PARAM_FEEMCPERCENT = "FeeMCPercent";
                public static final String PARAM_FEEMCVALUE = "FeeMCValue";
                public static final String PARAM_FEEOTHERSPERCEN = "FeeOthersPercen";
                public static final String PARAM_FEEOTHERSVALUE = "FeeOthersValue";
            }

        }

        public static final class TipsCommand {
            public static final class Request {
                public static final String ACTION = "add-tips";

                public static final String PARAM_TRANSACTION_NUMBER = "transaction-number";
                public static final String PARAM_AMOUNT = "amount";
            }
        }

        public static final class LastTransactionCommand {
            public static final class Request {
                public static final String ACTION = "last-transaction";
            }
        }

        public static final class SettlementCommand {
            public static final class Request {
                public static final String ACTION = "settlement";
            }

            public static final class SettlementObject {

                public static final String PARAM_AMOUNTSETTLED = "AmountSettled";
                public static final String PARAM_TRANSACTIONCOUNT = "TransactionCount";
                public static final String PARAM_TRANSACTIONS = "Transactions";
                public static final String PARAM_RESPONSECODE = "ResponseCode";
                public static final String PARAM_VERBIAGE = "Verbiage";

                public static final String PARAM_SETTLED = "Settled";
                public static final String PARAM_BATCHNUMBER = "BatchNumber";
                public static final String PARAM_AMOUNT = "Amount";
                public static final String PARAM_REFERENCENUMBER = "ReferenceNumber";
                public static final String PARAM_TRANSACTIONNUMBER = "TransactionNumber";
                public static final String PARAM_DATE = "Date";
            }
        }

        public static final class SaleCommand {

            public static final class Request {
                public static final String ACTION = "payment-action";

                public static final String PARAM_AMOUNT = "amount";
                public static final String TRANSACTION_ID = "transaction-id";
                public static final String PARAM_PAYMENT_TRANSACTION = "payment-transaction";
            }

            public static final class Response {
                public static final String PARAM_SALE = "sale";
                public static final String PARAM_TRANSACTIONNUMBER = "TransactionNumber";
                public static final String PARAM_CARDLASTDIGITS = "CardLastDigits";
                public static final String PARAM_CUSTOMSTATUS = "CustomStatus";
                public static final String PARAM_VERBIAGE = "Verbiage";
                public static final String PARAM_FEES = "Fees";
                public static final String PARAM_CARDTYPE = "CardType";
                public static final String PARAM_SALEAMOUNT = "SaleAmount";
                public static final String PARAM_TRANSACTION_AMOUNT = "Amount";
                public static final String PARAM_CASHBACKAMOUNT = "CashBackAmount";
                public static final String PARAM_BALANCECASH = "BalanceCash";
                public static final String PARAM_BALANCEFS = "BalanceFS";
            }

            public static final class SaleObject {
                public static final String PARAM_AVS = "avs";
                public static final String PARAM_SERVICEREFERENCENUMBER = "ServiceReferenceNumber";
                public static final String PARAM_MSOFT_CODE = "msoft_code";
                public static final String PARAM_PHARD_CODE = "phard_code";
                public static final String PARAM_CV = "cv";
                public static final String PARAM_AUTHORIZATIONNUMBER = "AuthorizationNumber";
                public static final String PARAM_CARDTYPE = "CardType";
                public static final String PARAM_LASTFOUR = "LastFour";
                public static final String PARAM_BALANCE = "Balance";
                public static final String PARAM_RESPONSECODE = "ResponseCode";
                public static final String PARAM_MSG = "Msg";
                public static final String PARAM_VERBIAGE = "verbiage";
            }
        }
    }

    public enum Transaction {

        DEBIT_CARD(TransactionGroup.Payments, 0, "Debit Card payment"),
        CREDIT_CARD(TransactionGroup.Payments, 1, "Credit Card payment"),
        EBT(TransactionGroup.Payments, 2, "EBT_FOODSTAMP payment"),
        GIFT_CARD(TransactionGroup.Payments, 3, "Gift Card payment"),
        EBT_FOOD_STAMP(TransactionGroup.Payments, 5, "EBT_FOODSTAMP Food Stamp payment"),
        EBT_CASH(TransactionGroup.Payments, 7, "EBT_FOODSTAMP Cash payment"),
        EBT_CASH_ACCOUNT_WITHDRAWAL(TransactionGroup.Payments, 8, "EBT_FOODSTAMP Cash account withdrawal"),
        GIFT_CARD_SALE(TransactionGroup.Payments, 10, "Gift Card sale"),
        PRINT_EBT(TransactionGroup.Balances, 9, "Print EBT_FOODSTAMP card balance"),
        PRINT_GIFT_CARD(TransactionGroup.Balances, 12, "Print a gift card balance"),
        ACTIVATE_GIFT_CARD(TransactionGroup.Activation, 13, "Activate a gift card"),
        REFUND_EBT(TransactionGroup.Refunds, 6, "Refund an EBT_FOODSTAMP transaction"),
        REFUND_GIFT(TransactionGroup.Refunds, 11, "Refund a Gift Card transaction"),
        REFUND_CREDIT_CARD(TransactionGroup.Refunds, 20, "Refund a Credit Card transaction"),
        REFUND_DEBIT_CARD(TransactionGroup.Refunds, 21, "Refund a Debit Card transaction");

        public TransactionGroup group;
        public int value;
        public String descr;

        Transaction(TransactionGroup group, int value, String descr) {

        }
    }

    public enum TransactionGroup {
        Payments,
        Balances,
        Activation,
        Refunds
    }
}