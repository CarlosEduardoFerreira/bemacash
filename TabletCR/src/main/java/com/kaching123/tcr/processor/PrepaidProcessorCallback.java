package com.kaching123.tcr.processor;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.Category;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by dot on 15.02.14.
 */
interface PrepaidProcessorCallback {

    void onComplete(final FragmentActivity context);

    void onCancel(final FragmentActivity context);

    void onPaymentRequested(final FragmentActivity context,
                            BigDecimal amount,
                            String description,
                            PrepaidType type,
                            SubProcessorDelegate delegate,
                            BigDecimal transactionFee);

    void onPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info);

    void onVoidRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions);

    void onSunPassPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunReplenishmentRequest request, SunpassType type, BalanceResponse response, BigDecimal transactionFee,String orderNum, String total);

    void onSunPassPYDPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunPassDocumentPaymentRequest request, SunpassType type, BalanceResponse response, BigDecimal transactionFee,String orderNum, String total);

    void onWirelessPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, WirelessItem chosenCategory, BigDecimal transactionFee, String orderNum, String total);

    void onBillPaymentPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, Category chosenCategory, BillPaymentRequest request, BigDecimal amount, BigDecimal transactionFee,String orderNum, String total);
}
