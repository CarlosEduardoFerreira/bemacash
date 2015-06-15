package com.kaching123.tcr.fragment.tendering;

import android.os.Bundle;
import android.text.Spannable;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public abstract class NotificationFragmentDialogBase<T extends NotificationFragmentDialogBase> extends StyledDialogFragment {

    protected INotificationConfirmListener listener;
    protected boolean allowRetry;
    protected boolean success;
    protected Spannable spannableMessage;

    @ViewById
    protected TextView message;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
//        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        initText();
    }

    protected void initText() {
        message.setText(spannableMessage);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_notification;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_abort;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return allowRetry && !success ? R.string.btn_retry : R.string.btn_continue;
    }

    @Override
    protected boolean hasPositiveButton() {
        return success || allowRetry;
    }

    @Override
    protected boolean hasNegativeButton() {
        return !success;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (!success) {
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
                if (success) {
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

    protected boolean abort() {
        if (listener != null) {
            listener.onCancel();
            return true;
        }
        return false;
    }

    protected boolean retry() {
        if (listener != null) {
            listener.onRetry();
            return true;
        }
        return false;
    }

    protected boolean confirm() {
        if (listener != null) {
            listener.onConfirmed();
            return true;
        }
        return false;
    }

    public T setListener(INotificationConfirmListener listener) {
        this.listener = listener;
        return (T)this;
    }

    public T setAllowRetry(boolean allowRetry) {
        this.allowRetry = allowRetry;
        return (T)this;
    }

    public T setSuccess(boolean success) {
        this.success = success;
        return (T)this;
    }

    public T setMessage(Spannable message) {
        this.spannableMessage = message;
        return (T)this;
    }
}
