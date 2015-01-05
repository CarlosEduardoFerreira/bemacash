package com.kaching123.tcr.fragment.tendering.payment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ProgressBar;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.NotificationFragmentDialogBase;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PayNotificationFragmentDialog extends NotificationFragmentDialogBase<PayNotificationFragmentDialog> {


    @ViewById
    protected ProgressBar swirl;

    private static final String DIALOG_NAME = "PayNotificationFragmentDialog";

    @ColorRes(R.color.dlg_btn_text_color)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;

    @FragmentArg
    protected Boolean allowReload;

    @FragmentArg
    protected boolean abortSwitchToClose;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @Override
    protected int getDialogTitle() {
        return success ? R.string.blackstone_pay_confirm_title : R.string.blackstone_pay_confirm_title_failed;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return abortSwitchToClose ? R.string.btn_close : super.getNegativeButtonTitle();
    }

    @Override
    protected boolean hasPositiveButton() {
        return super.hasPositiveButton() || allowReload;
    }


    @Override
    protected int getPositiveButtonTitle() {
        return super.hasPositiveButton() ? super.getPositiveButtonTitle() : R.string.blackstone_pay_reload_btn;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        if (super.hasPositiveButton()) {
            return super.getPositiveButtonListener();
        } else return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                listener.onReload(PayNotificationFragmentDialog.this);
                return false;
            }
        };
    }

    public void enableReload(boolean allowFurtherReload, String description) {
        startSwirl(false);
        if (!allowFurtherReload) {
            description = getString(R.string.pax_reload_disable_further, description);
        }
        final SpannedString descr;
        final String textMiddle = getString(R.string.pax_reload_after_relaod);
        final Spannable messageSpannable;
            messageSpannable = new SpannableString(textMiddle);
            messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, textMiddle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final Spannable descriptionSpannable;
        descriptionSpannable = new SpannableString(description);
        descriptionSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, description.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        descr = (SpannedString) TextUtils.concat(spannableMessage, messageSpannable, descriptionSpannable);
        getPositiveButton().setEnabled(allowFurtherReload);
        getPositiveButton().setTextColor(allowFurtherReload ? colorPaymentOk : colorPaymentDisabled);
        message.setText(descr);
    }

    public void startSwirl(boolean shown) {
        swirl.setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
    }

    public static void show(FragmentActivity context,
                            boolean allowRetry,
                            boolean success,
                            boolean abortSwitchToClose,
                            Spannable message,
                            Boolean allowReload,
                            INotificationConfirmListener listener) {
        DialogUtil.show(context,
                DIALOG_NAME,
                PayNotificationFragmentDialog_.builder().allowReload(allowReload).abortSwitchToClose(abortSwitchToClose).build())

                .setListener(listener)
                .setAllowRetry(allowRetry)
                .setSuccess(success)
                .setMessage(message);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}