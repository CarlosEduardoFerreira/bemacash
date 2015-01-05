package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.CredentialsDialogBase;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class WirelessCredentialsFragmentDialog extends CredentialsDialogBase {

    private static final String DIALOG_NAME = "WirelessCredentialsFragmentDialog";

    private static final int WIRELESS_PROFILE_ID = 15;

    private MetaInfo accNum;
    private MetaInfo accNumValidation;

    @FragmentArg protected String cashierId;
    @FragmentArg protected WirelessItem item;
    @FragmentArg protected PrepaidUser user;
    @FragmentArg protected String transactionMode;

    protected WirelessCredentialsFragmentDialogCallback callback;

    @ViewById protected EditText accountNumber;
    @ViewById protected EditText accountNumberValidate;

    @ViewById protected TextView accountNumberLabel;
    @ViewById protected TextView accountNumberValidateLabel;


    public void setCallback(WirelessCredentialsFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews  protected void init() {
        enableFinish(false);

        final int maxLength = 20;
        final int minLength = 1;

        accNum = new MetaInfo(accountNumber, accountNumberLabel, true, false, maxLength, minLength);
        accNumValidation = new MetaInfo(accountNumberValidate, accountNumberValidateLabel, true, false, maxLength, minLength);
        refresh();
//        Locale locale = new Locale(item.carrierName.substring(0,2));
//        accNum.editable.setText(locale.);
    }

    @AfterTextChange protected void accountNumberAfterTextChanged(Editable s) {
        String numText = s.toString();
        String numValidationText = accNumValidation.editable.getText().toString();
        accNumValidation.validated = accNum.validated = numText.length() >= accNum.min && numValidationText.equals(numText);
        refresh();
    }

    @AfterTextChange protected void accountNumberValidateAfterTextChanged(Editable s) {
        accountNumberAfterTextChanged(accNum.editable.getText());
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.wireless_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_heigth));
    }

    @Override protected int getDialogContentLayout() {
        return R.layout.wireless_credentials_fragment;
    }
    @Override protected int getDialogTitle() {
        return R.string.prepaid_dialog_credentials_title;
    }
    @Override protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                complete();
                return false;
            }
        };
    }
    @Override protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onCancel();
                return false;
            }
        };
    }

    private void refresh() {
        boolean valid =
                   accNum.validated
                && accNumValidation.validated;
        getPositiveButton().setEnabled(valid);
        getPositiveButton().setTextColor(valid ? colorOk : colorDisabled);
    }

    protected boolean complete() {
        callback.onComplete(accNum.editable.getText().toString(), WIRELESS_PROFILE_ID);
        return true;
    }

    public static void show(FragmentActivity context,
                            String cashierId,
                            PrepaidUser user,
                            String transactionMode,
                            WirelessItem item,
                            WirelessCredentialsFragmentDialogCallback callback) {
        WirelessCredentialsFragmentDialog dialog = WirelessCredentialsFragmentDialog_.builder()
                .cashierId(cashierId)
                .user(user)
                .item(item)
                .transactionMode(transactionMode)
                .build();
        dialog.setCallback(callback);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface WirelessCredentialsFragmentDialogCallback {

        public abstract void onCancel();
        public abstract void onError(String message);
        public abstract void onComplete(String phoneNumber, int profileId);
    }
}
