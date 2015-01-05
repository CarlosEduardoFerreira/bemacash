package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.NotificationFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class RefundNotificationFragmentDialog extends NotificationFragmentDialogBase<RefundNotificationFragmentDialog> {

    private static final String DIALOG_NAME = "RefundNotificationFragmentDialog";


    @Override
    protected int getDialogTitle() {
        return success ? R.string.refund_confirm_title : R.string.refund_confirm_title_failed;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    public static void show(FragmentActivity context, boolean allowRetry, boolean success, Spannable message, INotificationConfirmListener listener) {
        DialogUtil.show(context, DIALOG_NAME, RefundNotificationFragmentDialog_.builder().build())
                .setListener(listener)
                .setAllowRetry(allowRetry)
                .setSuccess(success)
                .setMessage(message);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}