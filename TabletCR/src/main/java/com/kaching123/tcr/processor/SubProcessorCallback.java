package com.kaching123.tcr.processor;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by dot on 15.02.14.
 */
    interface SubProcessorCallback {

        void onComplete(final FragmentActivity context);
        void onCancel(final FragmentActivity context);
        void onPaymentRequested(final FragmentActivity context,
                                BigDecimal amount,
                                String description,
                                PrepaidType type,
                                SubProcessorDelegate delegate);
        void onPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info);
        void onVoidRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions);
    }
