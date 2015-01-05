package com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass;

/**
 * @author Ivan v. Rikhmayer
 */
public enum  SunpassResponseCode {
    SUCCESS(0, "Success", "OK, No error", false),
    INVALID_TERMINAL(1, "Invalid Terminal", "Terminal id is invalid. Check credentials.", false),
    MERCHANT_IS_CLOSED(3, "Merchant is closed", "Problem with your Merchant account, please contact PinservE customer service.", false),
    MERCHANT_IS_SUSPENDED(4, "Merchant is suspended", "Problem with your Merchant account, please contact PinservE customer service.", false),
    INVALID_REQUEST(5, "Invalid Request", "The request format is not valid.", false),
    TIMEOUT_EXPIRED(6, "Timeout Expired", "Transaction could not be completed after an established period of time. Retry transaction and if the problem persists please contact PinservE IT Department.", false),
    INVALID_TRANSPONDER_ID(7, "Invalid Transponder Id", "Transponder is not valid", false),
    TRANSACTION_COULD_NOT_BE_STORED(8, "Transaction could not be stored", "There was a problem with the database that caused the transaction not to be stored. . Retry transaction and if the problem persists please contact PinservE IT Department.", true),
    INVALID_AMOUNT(10, "Invalid amount", "Amount is out of the limits, please try again using another amount.", true),
    INVALID_FEE_AMOUNT(11, "Invalid fee amount", "Fee amount is out of the limits, please try again using another amount.", false),
    INVALID_PURCHASE_ID(12, "Invalid purchase id", "Purchase id is invalid.", false),
    SYSTEM_ERROR(13, "System error", "Problem with the system. Retry transaction and if the problem persists please contact PinservE IT Department.", true),
    TRANSACTION_REJECTED(14, "Transaction rejected", "Transaction has been rejected.", false),
    NOT_ELIGIBLE_FOR_PAYMENT(16, "Not eligible for payment. No more documents", "The entered document is not eligible for payment and there are no other pending documents.", false),
    OTHER_DOCUMENTS(17, "Not eligible for payment. There are other documents", "The entered document is not eligible for payment but there are other pending documents that can be paid.", false),
    CREDIT_LIMIT_HAS_BEEN_EXCEEDED(18, "Credit Limit has been exceeded", "Merchant account has exceeded its credit limit.", false),
    DOCUMENT_PAYMENT_REJECTED(19, "Document payment rejected", "The payment of the FDOT Document has failed. Please refer to the ResponseMessage member", false);

    int id;
    String description;
    String solution;
    boolean retry;

    SunpassResponseCode(int id, String description, String solution, boolean retry) {
        this.id = id;
        this.description = description;
        this.solution = solution;
        this.retry = retry;
    }
}