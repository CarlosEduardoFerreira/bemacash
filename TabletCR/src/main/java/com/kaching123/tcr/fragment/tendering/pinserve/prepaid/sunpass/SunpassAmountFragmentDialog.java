package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.view.Gravity;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.TelephoneEditNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class SunpassAmountFragmentDialog extends StyledDialogFragment implements CustomEditBox.IKeyboardSupport{

    private static final String DIALOG_NAME = "SunpassAmountFragmentDialog";

    private BigDecimal minAmount = BigDecimal.TEN;
    private BigDecimal maxAmount = new BigDecimal(500);
    private final static BigDecimal FEE_AMOUNT = new BigDecimal("1.5");
    private final String DollarAmpsand = "$";

    protected SunpassAmountFragmentDialogCallback callback;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected BalanceResponse response;
    @FragmentArg
    protected String accountNumber;
    @FragmentArg
    protected String mAmount;

    @ViewById
    protected KeyboardView keyboard;
    @ViewById
    protected CustomEditBox charge;

    @ViewById
    protected TextView transponderNumber;
    @ViewById
    protected TextView lastKnownBalance;
    @ViewById
    protected TextView minimumRechargeAmount;
    @ViewById
    protected TextView fee;
    @ViewById
    protected TextView total;
    @ViewById
    protected TextView amount;
    @ViewById
    protected CustomEditBox accountNumberEditview;
    @ViewById
    protected CustomEditBox accountNumberValidateEditview;
    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;

    public void setCallback(SunpassAmountFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {
        keyboard.attachEditView(charge);
        setChargeView();
        setAccountNumberEditviewView();
        setAccountNumberValidateEditviewView();
        setReviewOrderDetailView();
        enableFinish(true);
        if (response != null) {
            BigDecimal min = BigDecimal.valueOf(response.minimumReplenishmentAmount);
            if (min != null && BigDecimal.ZERO.compareTo(min) < 0) {
                minAmount = min;
            }
        }
    }
    private void setChargeView() {
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new TelephoneEditNumberCurrencyFormatInputFilter()});
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return true;
            }
        });
    }
    private void setAccountNumberEditviewView() {
        accountNumberEditview.setKeyboardSupportConteiner(this);
        accountNumberEditview.setFilters(new InputFilter[]{new TelephoneEditNumberCurrencyFormatInputFilter()});
        accountNumberEditview.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return true;
            }
        });
    }
    private void setAccountNumberValidateEditviewView() {
        accountNumberValidateEditview.setKeyboardSupportConteiner(this);
        accountNumberValidateEditview.setFilters(new InputFilter[]{new TelephoneEditNumberCurrencyFormatInputFilter()});
        accountNumberValidateEditview.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return true;
            }
        });
    }
    private void setReviewOrderDetailView()
    {
        charge.setText(mAmount);
        transponderNumber.setText(accountNumber);
        lastKnownBalance.setText(DollarAmpsand + String.valueOf(response.currentBalance));
        minimumRechargeAmount.setText(DollarAmpsand + String.valueOf(response.minimumReplenishmentAmount));
        amount.setText(DollarAmpsand + mAmount);
        fee.setText(DollarAmpsand + FEE_AMOUNT.toString());
        total.setText(DollarAmpsand + (Double.parseDouble(mAmount) + FEE_AMOUNT.doubleValue()));
        accountNumberEditview.setEnabled(false);
        accountNumberEditview.setText(accountNumber);
        accountNumberValidateEditview.setText(accountNumber);
    }






    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_sunpass_credencial_width),
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_heigth));
    }
    @Override protected  int getTitleGravity(){return Gravity.LEFT;};

    @Override protected  int getSeparatorColor(){return Color.WHITE;}

    @Override protected  int getTitleTextColor(){return Color.WHITE;}

    @Override protected  int getTitleViewBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_title_background_color); }

    @Override protected  int getButtonsBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_buttons_background_color); }

    @Override
    protected int getTitleIcon() {
        return R.drawable.icon_sun_pass;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_check;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.sunpass_amount_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_sunpass_transponder_title;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                complete();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onCancel();
                return false;
            }
        };
    }

    protected boolean complete() {
        SunReplenishmentRequest request = new SunReplenishmentRequest();
        request.mID = String.valueOf(this.user.getMid());
        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.cashier = this.cashierId;
        request.accountNumber = this.accountNumber;
        request.purchaseId = request.accountNumber;
        request.amount = new BigDecimal(charge.getText().toString());
        request.feeAmount = FEE_AMOUNT.doubleValue();
        request.purchaseId = response == null ? "0" : this.response.purchaseId;
//        request.purchaseId = this.accountNumber;
        request.transactionMode = this.transactionMode;
        callback.onComplete(request);
        return true;
    }


    public static void show(FragmentActivity context, String accountNumber, String transactionMode,
                            String cashierId, PrepaidUser user, BalanceResponse response, String mAmount, SunpassAmountFragmentDialogCallback listener) {
        SunpassAmountFragmentDialog dialog = SunpassAmountFragmentDialog_.builder()
                .transactionMode(transactionMode)
                .cashierId(cashierId)
                .user(user)
                .response(response)
                .accountNumber(accountNumber)
                .mAmount(mAmount)
                .build();
        dialog.setCallback(listener);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected void enableFinish(Boolean enabled) {
        getPositiveButton().setEnabled(enabled);
        getPositiveButton().setTextColor(enabled ? colorOk : colorDisabled);
        keyboard.setEnabled(false);
    }

    public interface SunpassAmountFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete(SunReplenishmentRequest response);
    }
    @Override
    public void attachMe2Keyboard(CustomEditBox v) {

        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }
}
