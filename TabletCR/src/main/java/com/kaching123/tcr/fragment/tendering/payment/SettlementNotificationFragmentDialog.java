package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.NotificationFragmentDialogBase;

@EFragment
public class SettlementNotificationFragmentDialog extends NotificationFragmentDialogBase<SettlementNotificationFragmentDialog> {

    private static final String DIALOG_NAME = SettlementNotificationFragmentDialog.class.getSimpleName();

    @FragmentArg
    protected boolean transactionsClosed;



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @Override
    protected int getDialogTitle() {
        return success ? R.string.blackstone_settlement_confirm_title : R.string.blackstone_settlement_confirm_title_failed;
    }

    @Override
    protected boolean hasNegativeButton() {
        return !success || !transactionsClosed;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return allowRetry && (!success || !transactionsClosed) ? R.string.btn_retry : R.string.btn_continue;
    }


    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (!success || !transactionsClosed) {
                    abort();
                } else {
                    Logger.d("Impossible alg flow! Btn will not work");
                }
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (success && transactionsClosed) {
                    confirm();
                } else if (allowRetry) {
                    retry();
                } else {
                    Logger.d("Impossible alg flow! Btn will not work");
                }
                return false;
            }
        };
    }

    public static void show(FragmentActivity context, boolean allowRetry, boolean success, boolean transactionsClosed, Spannable message, INotificationConfirmListener listener) {
        DialogUtil.show(context, DIALOG_NAME, SettlementNotificationFragmentDialog_.builder().transactionsClosed(transactionsClosed).build())
                .setListener(listener)
                .setAllowRetry(allowRetry)
                .setSuccess(success)
                .setMessage(message);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}