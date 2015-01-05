package com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public enum  ResponseCode {

    TRANSACTION_APPROVED            (0,"Transaction Approved"),
    TRANSACTION_REJECTED_NUMBER     (1,"Transaction Rejected – Invalid Account Number"),
    TRANSACTION_REJECTED_TERMINAL   (2,"Transaction Rejected – Invalid Terminal Number"),
    TRANSACTION_REJECTED_IDD        (4,"Transaction Rejected – Invalid Vendor ID. Update Biller list."),
    TRANSACTION_REJECTED_XML        (5,"Transaction Rejected – Invalid XML"),
    TRANSACTION_REJECTED_AMOUNT     (6,"Transaction Rejected – Invalid Amount"),
    TRANSACTION_REJECTED_FEE        (7,"Transaction Rejected – Invalid Fee"),
    TRANSACTION_REJECTED_TYPE       (8,"Transaction Rejected – Invalid Transaction Type"),
    TRANSACTION_REJECTED_OEM        (9,"Transaction Rejected – Invalid OEM Reference Number"),
    TRANSACTION_REJECTED_DUPLICATE  (10,"Transaction Rejected – Duplicate Reference Number"),
    TRANSACTION_REJECTED_MERCHANT   (11,"Invalid Merchant"),
    TRANSACTION_REJECTED_BATCH      (12,"Invalid Batch Number"),
    TRANSACTION_REJECTED_REQUIRED   (13,"Missing Required Field"),
    TRANSACTION_REJECTED_EXACT      (17,"Transaction Rejected – Exact Duplicate Transaction. This indicates that this transaction is already on record for the same account and the same amount within the hour."),
    TRANSACTION_REJECTED_NAME       (18,"Transaction Rejected – Name is required for this vendor and is not provided."),
    TRANSACTION_REJECTED_PAYMENT    (19,"Transaction Rejected – Payment cannot exceed $2,000 in a 24 hour period to the same account."),
    TRANSACTION_REJECTED_ACTIVE     (20,"Transaction Rejected – Merchant not active. Please call"),
    TRANSACTION_REJECTED_M1         (22,"Transaction Rejected – Less than MIN amount for this biller/vendor"),
    TRANSACTION_REJECTED_M2         (23,"Transaction Rejected – More than MAX amount for this biller/vendor"),
    TRANSACTION_REJECTED_M3         (24,"Transaction Rejected – Less than MIN amount due"),
    TRANSACTION_REJECTED_M4         (025,"Transaction Rejected – More than MAX amount due"),
    TRANSACTION_REJECTED_EQUAL      (26,"Transaction Rejected – Not Equal to amount due"),
    TRANSACTION_REJECTED_AGV        (31,"Transaction Rejected – Biller not in AGV table"),
    TRANSACTION_REJECTED_COMBINATION(33,"Transaction Rejected – Merchant + Payment Vendor combination invalid"),
    TRANSACTION_REJECTED_USER       (37,"Transaction Rejected – Invalid User ID"),
    TRANSACTION_REJECTED_ADDITIONAL (38,"Transaction Rejected – Invalid Additional Account 1 or 2"),
    TRANSACTION_REJECTED_CUSTOMER1  (39,"Transaction Rejected – Invalid Customer Member ID or Type"),
    TRANSACTION_REJECTED_CUSTOMER2  (40,"Transaction Rejected – Invalid Customer First or Last Name"),
    TRANSACTION_REJECTED_MESSAGES   (50,"Transaction Rejected – No messages to retrieve"),
    TRANSACTION_REJECTED_INVALID    (51,"Transaction Rejected – Invalid message id"),
    TRANSACTION_REJECTED_BILL       (52,"Transaction Rejected – Bill Pay Presentment Failure"),
    TRANSACTION_REJECTED_PAY        (53,"Transaction Rejected – Web Pay Post Failure"),
    TRANSACTION_REJECTED_TIMING     (54,"Transaction Rejected – Remit Timing Flag Mismatch"),
    TRANSACTION_REJECTED_MASTER     (55,"Transaction Rejected – No Vendor Master Name Found"),
    TRANSACTION_REJECTED_CREATING   (56,"Transaction Rejected – Problem in Creating Posting Message"),
    TRANSACTION_REJECTED_CALCULATING(57,"Transaction Rejected – Problem in Calculating Commission Fee"),
    TRANSACTION_REJECTED_PERIOD1    (90,"Warning – Exceeds $2,000 in a 24 hours period (only for use where front end can submit compliance form to IPP)"),
    TRANSACTION_REJECTED_FRONT      (91,"Warning – Exceeds $10,000 in a 48 hours period (only for use where front end can submit compliance form to IPP)"),
    TRANSACTION_REJECTED_PRESENTMENT(92,"Warning- Warning message from Presentment"),
    TRANSACTION_REJECTED_HELD       (93,"Transaction Held – Merchant Credit Limit Exceeded"),
    TRANSACTION_REJECTED_TRANS      (94,"Warning – Duplicate trans found – requires review and confirmation"),
    TRANSACTION_REJECTED_CUT        (95,"Warning – Daily Cut-Off Time Passed"),
    TRANSACTION_REJECTED_READ       (96,"Transaction Rejected – Schema Read Error"),
    TRANSACTION_REJECTED_WRITE      (97,"Transaction Rejected – Schema Write Error"),
    TRANSACTION_REJECTED_UPDATE     (98,"Transaction Rejected – Failed SQL Update"),
    TRANSACTION_REJECTED_SYSTEM     (99,"Transaction Rejected – System Error"),
    TRANSACTION_REJECTED_STATUS     (101,"Transaction Rejected – Invalid Merchant Status"),
    TRANSACTION_REJECTED_HOST       (102,"Transaction Rejected – Terminal Host Id Model Mismatch"),
    TRANSACTION_REJECTED_ID         (103,"Transaction Rejected – Invalid Host ID"),
    TRANSACTION_REJECTED_VENDOR     (104,"Transaction Rejected – Invalid Vendor Type"),
    TRANSACTION_REJECTED_TRANSACTION(105,"Transaction Rejected – Invalid Vendor Sub Type"),
    TRANSACTION_REJECTED_TYPE2      (107,"Transaction Rejected – Merchant credit limit exceeded"),
    TRANSACTION_REJECTED_BLOCKED    (108,"Vendor Account Blocked"),
    TRANSACTION_REJECTED_FOUND      (109,"Transaction Rejected – No Vendor Master Name found"),
    TRANSACTION_REJECTED_TENDER     (111,"Transaction Rejected – Invalid Tender Reference Number"),
    TRANSACTION_REJECTED_PARTNER    (9993,"Transaction Rejected – Partner Timeout"),
    TRANSACTION_REJECTED_UNABLE     (9994,"Transaction Rejected – Unable Connect to Remote Server"),
    TRANSACTION_REJECTED_REQUEST    (9996,"Transaction Rejected – Invalid Request"),
    TRANSACTION_REJECTED_REMOTE     (9997,"Transaction Rejected – Unable Connect to Remote Application"),
    TRANSACTION_REJECTED_VENDORID   (9998,"Transaction Rejected – Invalid VendorID"),
    TRANSACTION_REJECTED_SCHEMA     (9999,"Transaction Rejected – Invalid Real Time Webservice Schema");



    private final long id;
    private final String description;


    ResponseCode(long id, String description) {
       this.id = id;
       this.description = description;
    }

    public long getId() {
        return id;
    }

    public static ResponseCode valueOf(long id) {
        for (ResponseCode code : ResponseCode.class.getEnumConstants()) {
            if (id == code.id) {
                return code;
            }
        }
        return null;
    }

}
