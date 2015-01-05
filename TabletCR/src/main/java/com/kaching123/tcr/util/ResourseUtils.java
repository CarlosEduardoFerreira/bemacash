package com.kaching123.tcr.util;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;

/**
 * @author Ivan v. Rikhmayer
 */
public class ResourseUtils {

    private ResourseUtils() {

    }

    public static int getMiniIconForTransactionType(PaymentType type) {
        switch (type) {
            case SALE:
                return 0;
            case VOID:
                return R.drawable.check_for_history;
            default:
                return 0;
        }
    }

    public static int getMiniIconForTransactionType(PaymentGateway type) {
        switch (type) {
            case PAYPAL:
            case BLACKSTONE:
                return R.drawable.credit_card_for_history;
            case CASH:
                return R.drawable.cash_for_history;
            case CREDIT:
                return R.drawable.credit_receipt_for_history;
            case OFFLINE_CREDIT:
                return R.drawable.offline_credit_for_history;
            case CHECK:
                return R.drawable.check_2_for_history;
            default:
                return R.drawable.credit_card_for_history;
        }
    }

    public static int getMiniIconForTransactionType(TransactionType type) {
        switch (type) {
            case CREDIT:
                return R.drawable.credit_card_for_history;
            case CASH:
                return R.drawable.cash_for_history;
            case OFFLINE_CREDIT:
                return R.drawable.offline_credit_for_history;
            case CHECK:
                return R.drawable.check_2_for_history;
            default:
                return R.drawable.credit_card_for_history;
        }
    }
}
