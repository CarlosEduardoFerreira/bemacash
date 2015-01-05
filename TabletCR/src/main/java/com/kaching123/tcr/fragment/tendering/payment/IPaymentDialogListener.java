package com.kaching123.tcr.fragment.tendering.payment;

import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.PaymentMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public interface IPaymentDialogListener {

    void onSingleTenderCheck(boolean singleTenderEnabled);

    void onOtherMethodsShown(boolean otherMethodsShown);

    void onCancel();

    public interface IRefundListener extends IPaymentDialogListener {
        void onRefundMethodSelected(PaymentMethod method, final List<PaymentTransactionModel> transactions);

        void onDataLoaded(BigDecimal alreadyPayed, BigDecimal orderTotal, ArrayList<PaymentTransactionModel> transactions);
    }

    public interface ISaleTenderListener extends IPaymentDialogListener{
        void onPaymentMethodSelected(PaymentMethod method, BigDecimal orderTotal, BigDecimal pendingAmount, boolean singleTender);
    }

    public interface IPayTenderListener extends ISaleTenderListener {
        void onVoidRequested(List<PaymentTransactionModel> transactions);

        void onDataLoaded(BigDecimal alreadyPayed, BigDecimal orderTotal, ArrayList<PaymentTransactionModel> transactions);
    }
}
