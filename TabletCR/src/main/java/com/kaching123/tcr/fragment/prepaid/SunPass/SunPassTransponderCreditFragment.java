package com.kaching123.tcr.fragment.prepaid.SunPass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidSunPassActivity;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass.GetSunPassBalancCommand;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.PrepaidKeyboardView;
import com.kaching123.tcr.component.SunpassCredencialEditNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunEntryRequest;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;


/**
 * Created by teli.yin on 12/5/2014.
 */
@EFragment
public class SunPassTransponderCreditFragment extends Fragment implements CustomEditBox.IKeyboardSupport, PrepaidSunPassActivity.PrepaidSunPassInterface{
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected String transactionMode;
    @ViewById
    protected CustomEditBox accountNumber, amount, accountNumberValidate;
    @ViewById
    protected PrepaidKeyboardView keyboard;
    @ViewById
    protected TextView enterAmountInterval, check, amountTitle, transponderNumber, lastKnownBalance, minimunRechargeAmount;
    @ViewById
    protected LinearLayout balanceLayout;

    private SunPassTransponderCreditCallBack callBack;
    private BigDecimal chosenAmount;
    private String strAccountNumber;
    private String strVerifyAccountNumber;
    private BigDecimal minAmount = BigDecimal.TEN;
    private BigDecimal maxAmount = new BigDecimal(500);
    private final static BigDecimal FEE_AMOUNT = new BigDecimal("1.5");
    private final String MIN = "MIN";
    private final String MAX = "MAX";
    private BalanceResponse response;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sunpass_transponder_fragment, container, false);
    }

    public void setCallBack(SunPassTransponderCreditCallBack callBack) {
        this.callBack = callBack;

    }

    @AfterViews
    protected void init() {
        setTitleWithTransponderMSG();
        hideAmountView();
        keyboard.attachEditView(accountNumber);
        keyboard.setEnabled(true);
        keyboard.setDotEnabled(false);
        setAccountNumberEditView();
        setAccountNumberValidateLabelEditView();
        setChargeView();
    }

    @AfterTextChange
    protected void accountNumberAfterTextChanged(Editable s) {
        setTitleWithTransponderMSG();
        strAccountNumber = s.toString();
    }

    @AfterTextChange
    protected void accountNumberValidateAfterTextChanged(Editable s) {
        setTitleWithTransponderMSG();
        strVerifyAccountNumber = s.toString();
    }

    @AfterTextChange
    protected void amountAfterTextChanged(Editable s) {
        setTitleWithAmountMSG();
        try {
            BigDecimal mAmount = new BigDecimal(UiHelper.valueOf(new BigDecimal(s.toString())));

            assert minAmount != null;
            assert maxAmount != null;
            chosenAmount = mAmount;
            if (chosenAmount.compareTo(minAmount) >= 0) {

                if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && chosenAmount.compareTo(maxAmount) > 0) {
                    amount.setText(UiHelper.valueOf(maxAmount));
                    return;
                }

            }
        } catch (NumberFormatException e) {
            Logger.d(e.toString());
        }
    }

    private void setTitleWithTransponderMSG() {
        callBack.headMessage(PrepaidSunPassHeadFragment.SELECT_TRANSPONDER_NUMBER);
    }

    private void setTitleWithAmountMSG() {
        callBack.headMessage(PrepaidSunPassHeadFragment.SELECT_AMOUNT);
    }

    private void setChargeView() {
        amount.setKeyboardSupportConteiner(this);
        amount.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        amount.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                complate();
                return false;
            }
        });
    }

    private void setAccountNumberEditView() {
        accountNumber.setKeyboardSupportConteiner(this);
        accountNumber.setFilters(new InputFilter[]{new SunpassCredencialEditNumberCurrencyFormatInputFilter()});
        accountNumber.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                complate();
                return false;
            }
        });
    }

    private void setAccountNumberValidateLabelEditView() {
        accountNumberValidate.setKeyboardSupportConteiner(this);
        accountNumberValidate.setFilters(new InputFilter[]{new SunpassCredencialEditNumberCurrencyFormatInputFilter()});
        accountNumberValidate.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                complate();
                return false;
            }
        });
    }

    @Click
    protected void check() {
        complate();
    }

    private void complate() {
        if (amount.getVisibility() == View.VISIBLE)
            submit();
        else
            getBill();
    }

    private void submit() {
        if (isSubmitValid()) {
            callBack.onComplete(strAccountNumber, response, chosenAmount, FEE_AMOUNT);

        }
    }

    private boolean isCheckValid() {

        if (strAccountNumber == null) {
            callBack.headMessage(PrepaidSunPassHeadFragment.ACCOUNT_NUMBER_NULL);
            return false;
        }
        if (!strAccountNumber.equalsIgnoreCase(strVerifyAccountNumber)) {
            callBack.headMessage(PrepaidSunPassHeadFragment.ACCOUNT_NUMBER_NOT_EQUAL);
            return false;
        }
        return true;
    }

    private boolean isSubmitValid() {
        if (strAccountNumber == null) {
            callBack.headMessage(PrepaidSunPassHeadFragment.ACCOUNT_NUMBER_NULL);
            return false;
        }
        if (!strAccountNumber.equalsIgnoreCase(strVerifyAccountNumber)) {
            callBack.headMessage(PrepaidSunPassHeadFragment.ACCOUNT_NUMBER_NOT_EQUAL);
            return false;
        }
        if (chosenAmount == null || chosenAmount.compareTo(BigDecimal.ZERO) == 0) {
            callBack.headMessage(PrepaidSunPassHeadFragment.AMOUNT_ZERO_ERROR);
            return false;
        }
        return true;
    }

    private void hideAmountView() {
        amountTitle.setVisibility(View.GONE);
        enterAmountInterval.setVisibility(View.GONE);
        amount.setVisibility(View.GONE);
    }

    private void showAmountView() {
        amountTitle.setVisibility(View.VISIBLE);
        enterAmountInterval.setVisibility(View.VISIBLE);
        enterAmountInterval.setText(MIN + " " + commaPriceFormat(minAmount) + "/" + MAX + commaPriceFormat(maxAmount));
        amount.setVisibility(View.VISIBLE);
    }

    private void setCheckView(BalanceResponse result) {
        setTitleWithAmountMSG();
        response = result;
        balanceLayout.setVisibility(View.VISIBLE);
        showAmountView();
        accountNumber.setEnabled(false);
        accountNumberValidate.setEnabled(false);
        keyboard.detachEditView();
        keyboard.setDotEnabled(true);
//        keyboard.setVisibility(View.GONE);
        transponderNumber.setText(strAccountNumber);
        lastKnownBalance.setText(commaPriceFormat(new BigDecimal(String.valueOf(result.currentBalance))));
        minimunRechargeAmount.setText(commaPriceFormat(new BigDecimal(result.minimumReplenishmentAmount)));
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    @Override
    public void onBackPressed() {
        callBack.onBackButtonPressed();
        callBack.headMessage(PrepaidSunPassHeadFragment.SELECT_SUNPASS_CATEGORY);
    }
    @Override
    public void onResume() {
        super.onResume();
        callBack.headMessage(PrepaidSunPassHeadFragment.SELECT_TRANSPONDER_NUMBER);
    }
    public interface SunPassTransponderCreditCallBack {
        void headMessage(int mode);

        void onBackButtonPressed();

        void onComplete(String accountNumber, BalanceResponse formedRequest, BigDecimal amount, BigDecimal transcationFee);
    }

    @OnSuccess(GetSunPassBalancCommand.class)
    public void onGetSunPassBalancCommandSuccess(@Param(GetSunPassBalancCommand.ARG_RESULT) BalanceResponse result) {
        WaitDialogFragment.hide(getActivity());
        setCheckView(result);
    }

    @OnFailure(GetSunPassBalancCommand.class)
    public void onGetSunPassBalancCommandCommandFail(@Param(GetSunPassBalancCommand.ARG_RESULT) BalanceResponse result) {
        WaitDialogFragment.hide(getActivity());
        setCheckView(result);
    }

    public void getBill() {
        if (isCheckValid()) {
            WaitDialogFragment.show(getActivity(), "Loading..");
            SunEntryRequest request = new SunEntryRequest();
            request.mID = String.valueOf(user.getMid());
            request.tID = String.valueOf(user.getTid());
            request.password = user.getPassword();
            request.cashier = cashierId;
            request.accountNumber = strAccountNumber;
            request.transactionId = PrepaidProcessor.generateId();
            request.transactionMode = transactionMode;
            GetSunPassBalancCommand.start(getActivity(), this, request);
        }

    }
}
