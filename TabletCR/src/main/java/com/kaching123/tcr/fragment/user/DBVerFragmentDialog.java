package com.kaching123.tcr.fragment.user;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.NotificationFragmentDialogBase;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class DBVerFragmentDialog extends NotificationFragmentDialogBase<DBVerFragmentDialog> {

    private static final String DIALOG_NAME = "DBVerFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SpannableString messageSpannable = new SpannableString(text);
        messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setMessage(messageSpannable);
    }

    @FragmentArg
    protected String text;

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return true;
            }
        };
    }

    @Override
    protected int getDialogTitle() {
        return R.string.sync_diff;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_close;
    }

    public static void show(FragmentActivity context, String text) {
        DialogUtil.show(context,
                DIALOG_NAME,
                DBVerFragmentDialog_.builder().text(text).build());
    }
}