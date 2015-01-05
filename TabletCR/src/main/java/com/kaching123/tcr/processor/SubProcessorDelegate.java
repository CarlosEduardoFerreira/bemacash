package com.kaching123.tcr.processor;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.model.PaymentTransactionModel;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 */
interface SubProcessorDelegate {

    void onComplete(final FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long orderId);
}
