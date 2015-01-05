package com.kaching123.tcr.model.payment.blackstone.payment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivan v. Rikhmayer
 *         RESPONSE CODES for blackstone
 */
public enum TransactionStatusCode {

    NULL(0, "", true),
    INVALID_CREDENTIALS(1, "Invalid Credentials", true),
    INVALID_HOST_CREDENTIALS(2, "Invalid Host Credentials", true),
    TRANSACTION_ERROR(3, "Transaction Error", true),
    IMPOSSIBLE_SETTLEMENT(4, "Impossible to do Settlement", true),
    UNAUTHORIZED_REFERENCE_NUMBER(5, "Trying to send an unauthorized reference number", true),
    IMPOSSIBLE_REFUND(6, "Impossible to do Refund", true),
    IMPOSSIBLE_VOID(7, "Impossible to do Void", true),
    IMPOSSIBLE_USER(8, "Impossible to create user", true),
    IMPOSSIBLE_LIMIT_EXCEEDED(9, "Impossible to do transaction: Credit Limit exceeded.", true),
    IMPOSSIBLE_LIMITS_NOTVFOUND(10, "Impossible to do transaction: Credit Limits for merchant not found.", true),
    MERCHANT_NUMBER_NOT_AUTHORIZED(11, "This Merchant Number is not authorized to do this operation.", true),
    IMPOSSIBLE_AUTOMATIC_SETTLEMENT(12, " Impossible to addItemDiscount value for this merchant on the automatic settlement", true),
    EMPTY_REQUEST(13, " Empty Request", true),
    IMPOSSIBLE_CHECK(14, " Impossible to process check, look at the response messages for more details", true),
    IMPOSSIBLE_INCORRECT_DATA(15, " Impossible to process check, Incorrect data passed", true),
    VOIDCHECKTRANSACTION(16, " To void a transaction call the method VoidCheckTransaction", true),
    PROCESSCHECK(17, " To process a transaction call the method ProcessCheck", true),
    IMPOSSIBLE_TRANSACTION(18, " Impossible to do transaction, look at the error message for more details", true),
    INCORRECT_CHECKAMOUNT(19, " Incorrect data unitsLabel: field CheckAmount", true),
    IMPOSSIBLE_NOT_FUNDED(20, " Impossible to do void, transaction not funded on the system", true),
    IMPOSSIBLE_PROCESSED_BEFORE(21, " Impossible to process check, check processed before", true),
    IMPOSSIBLE_VOID_PROCESSED_BEFORE(22, " Impossible to do void, void processed before", true),
    IMPOSSIBLE_TRANSACTION_NOT_FOUND(23, " Impossible to do void, transaction not found", true),
    IMPOSSIBLE_SETTLEMENT_NOT_AUTHORIZED(24, " Impossible to do settlement, this merchant is not authorized to execute this operation", true),
    IMPOSSIBLE_TOKEN(25, " Impossible to generate token", true),
    IMPOSSIBLE_PREAUTHORIZATION(26, " Impossible to do pre authorization", true),
    NOT_AUTHORIZED_TASK(28, " This application unitsLabel is not authorized to execute this task", true),
    USERTRANSACTIONNUMBER_NOT_FOUND(29, " UserTransactionNumber not found", true),
    USERTRANSACTIONNUMBER_EMPTY(30, " UserTransactionNumber null or empty", true),
    SERVICETRANSACTIONNUMBER_FOUND(31, " ServiceTransactionNumber not found", true),
    PREAUTHORIZATION_NOT_FOUND(50, " Preauthorization record not found", true),
    PREAUTHORIZATION_NOT_CLOSABLE(51, " Preauthorization was not set as closable", true),
    REFERENCED_PREAUTHORIZATION_DECLINED(52, " Referenced Preauthorization record was declined", true),
    REFERENCED_PREAUTHORIZATION_COMPLETED(53, " Referenced Preauthorization record was completed before", false),
    SIGNATURE_DATA_EMPTY(54, " Signature Data is null or empty", true),
    MERCHANT_CLOSE(57, " Cannot update time: Merchant is not enrolled in Automatic Batch Close", true),
    OPERATION_COMPLETED_SUCCESSFULLY(200, " Operation completed successfully", false),
    SYSTEM_ERROR(300, "System Error", true),

    UNKNOWN(9000, "Unhandeled response code", true),
    NEW(9001, "new transaction", false),
    IN_PROGRESS(9002, "new transaction", false);

    private final int code;
    private final boolean eligibleForRetry;
    private final String description;

    TransactionStatusCode(int code, String description, boolean eligibleForRetry) {
        this.code = code;
        this.description = description;
        this.eligibleForRetry = eligibleForRetry;
    }

    public boolean retryMayHelp() {
        return eligibleForRetry;
    }

    public boolean success() {
        return this == OPERATION_COMPLETED_SUCCESSFULLY;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static Map<Integer, TransactionStatusCode> map = new HashMap<Integer, TransactionStatusCode>();

    static {
        for (TransactionStatusCode legEnum : TransactionStatusCode.values()) {
            map.put(legEnum.code, legEnum);
        }
    }

    public static TransactionStatusCode valueOf(int legNo) {
        return map.containsKey(legNo) ? map.get(legNo) : UNKNOWN;
    }
}
