package com.kaching123.tcr.processor;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.tendering.pax.BalancePAXPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pax.BalancePAXPendingFragmentDialog.IBalanceSaleProgressListener;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class PaxBalanceProcessor {

    private PaxBalanceProcessor() {

    }

    public static PaxBalanceProcessor get() {
        return new PaxBalanceProcessor();
    }

    public void checkBalance(final FragmentActivity context) {
        BalancePAXPendingFragmentDialog.show(context, new IBalanceSaleProgressListener() {
            @Override
            public void onComplete(BigDecimal balance, String last4, String errorReason) {
                hide();
                final Spannable messageSpannable;
                boolean success;
                if (success = balance != null && TextUtils.isEmpty(errorReason)) {
                    String message = context.getString(R.string.pax_balance_response, last4, balance == null ? "unknown" : balance.toString());
                    messageSpannable = new SpannableString(message);
                    messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    messageSpannable = new SpannableString(errorReason);
                    messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, errorReason.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                proceedToCardPaymentNotification(context, success, messageSpannable);
            }

            @Override
            public void onCancel() {
                hide();
            }

            private void hide() {
                BalancePAXPendingFragmentDialog.hide(context);
            }
        });
    }

    /**
     * Notify the user about the result and wait for further commands
     */
    private void proceedToCardPaymentNotification(final FragmentActivity context,
                                                  final boolean success,
                                                  Spannable message) {

        PayNotificationFragmentDialog.show(context, false, success, false, message, false, new INotificationConfirmListener() {

            @Override
            public void onReload(final Object ignore) {
            }

            @Override
            public void onRetry() {
            }

            @Override
            public void onCancel() {
                hide();
            }

            @Override
            public void onConfirmed() {
                hide();
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }
}
