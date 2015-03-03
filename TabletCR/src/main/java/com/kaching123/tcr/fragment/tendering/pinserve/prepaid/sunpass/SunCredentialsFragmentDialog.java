package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass.GetSunPassBalancCommand;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.SunpassCredencialEditNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.CredentialsDialogWithCustomEditViewBase;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunEntryRequest;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class SunCredentialsFragmentDialog extends CredentialsDialogWithCustomEditViewBase implements CustomEditBox.IKeyboardSupport {
    private final String EMPTY_TRANSPONDER_NUMBER = "Transponder number";
    private final String NOT_EQUAL_TRANSPONDER_NUMBERS = "The transponder numbers are not match";
    private final String ZERO_AMOUNT = "Amount";
    private final String INCORRECT_AMOUNT = "Amount";
    private static final String DIALOG_NAME = "SunCredentialsFragmentDialog";
    private static final String BALANCE_ERROR = "The current balance is below 0";
    private static final String Y = "Y";
    private BigDecimal minAmount = BigDecimal.TEN;
    private BigDecimal maxAmount = new BigDecimal(500);
    private final static BigDecimal FEE_AMOUNT = new BigDecimal("1.5");
    private CredentialsDialogWithCustomEditViewBase.MetaInfo2 accNum;
    private CredentialsDialogWithCustomEditViewBase.MetaInfo2 accNumValidation;
    private BalanceResponse response;
    @ViewById
    protected KeyboardView keyboard;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected String transactionMode;
    @ViewById
    protected CustomEditBox charge;
    @ViewById
    protected Spinner biller;
    @ViewById
    protected LinearLayout linearlayout1;
    @ViewById
    protected LinearLayout linearlayout2;
    @ViewById
    protected LinearLayout linearlayout3;
    @ViewById
    protected TextView balanceHeader;
    @ViewById
    protected TextView reviewOrderDetail;
    @ViewById
    protected TextView transponderNumber;
    @ViewById
    protected TextView lastKnownBalance;
    @ViewById
    protected TextView minimumRechargeAmount;
    @ViewById
    protected TextView error;
    @ViewById
    protected TextView errorContent;
    protected SunCredentialsFragmentDialogCallback callback;
    private ArrayAdapter<String> billerAdapter;
    private BigDecimal chosenAmount;
    //    private String chosenBiller;
    private final String BILLER_DEFAULT = "Select Amount";
    final int DEFAULT_ITEM_POSITION = 0;
    private final String DollarAmpsand = "$";
    private boolean isError;
    @ViewById
    protected CustomEditBox accountNumber;
    @ViewById
    protected CustomEditBox accountNumberValidate;

    public void setCallback(SunCredentialsFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {

        enableFinish(false);
        final int maxLength = 13;
        final int minLength = 1;

        accNum = new MetaInfo2(accountNumber, true, false, maxLength, minLength);
        accNumValidation = new MetaInfo2(accountNumberValidate, true, false, maxLength, minLength);

        keyboard.attachEditView(accountNumber);
        keyboard.setEnabled(true);
        keyboard.setEnterEnabled(false);
        keyboard.setDotEnabled(false);
        setAccountNumberEditView();
        setAccountNumberValidateLabelEditView();
        setAmountBlanceViewGone();
        refresh();
    }

    private void setChargeView() {
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                if (isValid())
                    callback.onComplete(accNum.toString(), response, chosenAmount.toString(), FEE_AMOUNT);
                else {
                    setErrorVisible();
                }
                return true;
            }
        });
    }

    @AfterTextChange
    protected void chargeAfterTextChanged(Editable s) {

        setViewGone(error);
        setViewGone(errorContent);
        try {
            BigDecimal amount = new BigDecimal(UiHelper.valueOf(new BigDecimal(s.toString())));

            assert minAmount != null;
            assert maxAmount != null;
            chosenAmount = amount;
            if (chosenAmount.compareTo(minAmount) >= 0) {

                if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && chosenAmount.compareTo(maxAmount) > 0) {
                    charge.setText(UiHelper.valueOf(maxAmount));
                    return;
                }

            }
        } catch (NumberFormatException e) {
            enableFinish(true);
        }
    }

    //    private void setSpinner()
//    {
//        biller.setEnabled(true);
//        ArrayList<String> list = new ArrayList<String>();
//        list.add(BILLER_DEFAULT);
//        if(!isError) {
//            list.add("10");
//            list.add("30");
//            list.add("50");
//            list.add("100");
//            list.add("150");
//        }
//        billerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.prepaid_spinner_item, list);
//        billerAdapter.setDropDownViewResource(R.layout.prepaid_spinner_dropdown_item);
//        biller.setAdapter(billerAdapter);
//        biller.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == DEFAULT_ITEM_POSITION) {
//                    charge.setText("");
//                    return;
//                }
//                chosenBiller = (String)parent.getAdapter().getItem(position);
//                if(Double.parseDouble(chosenBiller) > response.minimumReplenishmentAmount)
//                    enablePositiveButton();
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> ignore) {
//            }
//        });
//    }
    private void setAmountBlanceViewGone() {
        setViewGone(balanceHeader);
        setViewGone(reviewOrderDetail);
        setViewGone(linearlayout1);
        setViewGone(linearlayout2);
        setViewGone(linearlayout3);
        setViewGone(biller);
        chosenAmount = BigDecimal.ZERO;
        if (billerAdapter != null)
            billerAdapter.clear();
        setViewGone(error);
        setViewGone(errorContent);
    }

    private void setAmountBalanceViewVisible(BalanceResponse result) {
        if (result.currentBalance < 0)
            isError = true;
        else
            isError = false;
        setViewVisible(balanceHeader);
        setViewVisible(reviewOrderDetail);
        transponderNumber.setText(accNum.toString());
        setViewVisible(linearlayout1);
        lastKnownBalance.setText(DollarAmpsand + String.valueOf(result.currentBalance));
        setViewVisible(linearlayout2);
        minimumRechargeAmount.setText(DollarAmpsand + String.valueOf(result.minimumReplenishmentAmount));
        setViewVisible(linearlayout3);
//        setSpinner();
        charge.setHint(getString(R.string.prepaid_dialog_amount_limitations, UiHelper.valueOf(minAmount), UiHelper.valueOf(maxAmount)));
        setViewVisible(charge);

        if (isError) {
            errorContent.setText(BALANCE_ERROR);
            setErrorVisible();
            getPositiveButton().setEnabled(false);
            biller.setVisibility(View.GONE);
        }
        refresh();
    }

    private void setViewGone(View view) {
        view.setVisibility(View.GONE);
    }

    private void setErrorVisible() {
        if (!errorContent.getText().toString().equalsIgnoreCase(NOT_EQUAL_TRANSPONDER_NUMBERS))
            setViewVisible(error);
        setViewVisible(errorContent);
    }

    private void setViewVisible(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void setAccountNumberEditView() {
        accountNumber.setKeyboardSupportConteiner(this);
        accountNumber.setFilters(new InputFilter[]{new SunpassCredencialEditNumberCurrencyFormatInputFilter()});
        accountNumber.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                if (isValid())
                    complete();
                else {
                    setErrorVisible();
                }
                return true;
            }
        });
    }

    private void setAccountNumberValidateLabelEditView() {
        accountNumberValidate.setKeyboardSupportConteiner(this);
        accountNumberValidate.setFilters(new InputFilter[]{new SunpassCredencialEditNumberCurrencyFormatInputFilter()});
        accountNumberValidate.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                if (isValid())
                    complete();
                else {
                    setErrorVisible();
                }
                return true;
            }
        });
    }

    @AfterTextChange
    protected void accountNumberAfterTextChanged(Editable s) {
        String numText = s.toString();
        String numValidationText = accNumValidation.editable.getText().toString();
        accNumValidation.validated = accNum.validated = numText.length() >= accNum.min && numValidationText.equals(numText);
        setAmountBlanceViewGone();
        refresh();
    }

    @AfterTextChange
    protected void accountNumberValidateAfterTextChanged(Editable s) {

        accountNumberAfterTextChanged((Editable) accNum.editable.getText());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getNegativeButton().setTextSize(25);
        getNegativeButton().setTypeface(Typeface.DEFAULT_BOLD);
        getPositiveButton().setTypeface(Typeface.DEFAULT_BOLD);
        getPositiveButton().setTextSize(25);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_sunpass_credencial_width),
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_heigth));
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.icon_sun_pass;
    }

    @Override
    protected int getTitleTextColor() {
        return Color.WHITE;
    }

    @Override
    protected int getSeparatorColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleViewBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_title_background_color);
    }

    @Override
    protected int getButtonsBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_buttons_background_color);
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    ;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.sunpass_credentials_fragment;
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
                if (isValid()) {
                    if (!isChargeVisible()) {
                        complete();
                        getPositiveButton().setText("Next");
                    } else
                        callback.onComplete(accNum.toString(), response, chosenAmount.toString(), FEE_AMOUNT);
                } else {
                    setErrorVisible();
                }
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

    private void refresh() {
        keyboard.setEnterEnabled(true);
        getPositiveButton().setEnabled(true);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(isValid ? colorOk : colorDisabled);
    }

    //    private void enablePositiveButton()
//    {
//        getPositiveButton().setEnabled(true);
//        getPositiveButton().setTextColor(true ? colorOk : colorDisabled);
//    }
    private boolean isValid() {
        if (accountNumber.length() == 0) {
            errorContent.setText(EMPTY_TRANSPONDER_NUMBER);
            return false;
        }
        if (!(accNum.validated && accNumValidation.validated)) {
            errorContent.setText(NOT_EQUAL_TRANSPONDER_NUMBERS);
            return false;
        }
        if (isChargeVisible()) {
            if (chosenAmount == BigDecimal.ZERO) {
                errorContent.setText(ZERO_AMOUNT + " invalid, range must be from " + minAmount + " to " + maxAmount);
                return false;
            }
            if (chosenAmount.doubleValue() < minAmount.doubleValue()) {
                errorContent.setText(INCORRECT_AMOUNT + " invalid, range must be from " + minAmount + " to " + maxAmount);
                return false;
            }
        }
        return true;
    }

    private boolean isChargeVisible() {
        return charge.getVisibility() == 0;
    }

    protected boolean complete() {
        WaitDialogFragment.show(getActivity(), "Loading..");
        getBill();
        return true;
    }

    @OnSuccess(GetSunPassBalancCommand.class)
    public void onGetSunPassBalancCommandSuccess(@Param(GetSunPassBalancCommand.ARG_RESULT) BalanceResponse result) {
        WaitDialogFragment.hide(getActivity());
        checkBalanceView(result);
    }

    @OnFailure(GetSunPassBalancCommand.class)
    public void onGetSunPassBalancCommandCommandFail(@Param(GetSunPassBalancCommand.ARG_RESULT) BalanceResponse result) {
        WaitDialogFragment.hide(getActivity());
        checkBalanceView(result);
    }

    private void checkBalanceView(BalanceResponse result) {
        accountNumber.setEnabled(false);
        accountNumberValidate.setEnabled(false);
        this.response = result;
        setAmountBalanceViewVisible(result);
        keyboard.setEnabled(true);
        keyboard.attachEditView(charge);
        setChargeView();
    }

    public void getBill() {
        SunEntryRequest request = new SunEntryRequest();
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.cashier = cashierId;
        request.accountNumber = accNum.toString();
        request.transactionId = PrepaidProcessor.generateId();
        request.transactionMode = transactionMode;
        GetSunPassBalancCommand.start(getActivity(), this, request);
    }

    public static void show(FragmentActivity context,
                            String cashierId,
                            PrepaidUser user,
                            String transactionMode,
                            SunCredentialsFragmentDialogCallback callback) {
        SunCredentialsFragmentDialog dialog = SunCredentialsFragmentDialog_.builder()
                .cashierId(cashierId)
                .user(user)
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

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    public interface SunCredentialsFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete(String accountNumber, BalanceResponse formedRequest, String amount, BigDecimal transcationFee);
    }
}
