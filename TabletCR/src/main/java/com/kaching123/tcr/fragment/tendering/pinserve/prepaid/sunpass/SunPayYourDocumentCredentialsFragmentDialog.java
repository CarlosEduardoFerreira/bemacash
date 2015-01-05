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
import android.widget.EditText;
import android.widget.TextView;


import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass.GetSunPassDocumentInquiryCommand;
import com.kaching123.tcr.component.DocumentNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.component.LicensePlateNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentInquiryRequest;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class SunPayYourDocumentCredentialsFragmentDialog extends StyledDialogFragment {

    private final String EMPTY_DOCUMENT_NUMBER ="Empty document number";
    private final String Empty_PLATE_NUMBER ="Empty plate number";
    private static final String DIALOG_NAME = "SunCredentialsFragmentDialog";
    private static final String Y = "Y";
    private BigDecimal minAmount = BigDecimal.TEN;
    private BigDecimal maxAmount = new BigDecimal(500);
    private final static BigDecimal FEE_AMOUNT = new BigDecimal("1.5");
    private BalanceResponse response;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected String transactionMode;

    protected SunDocumentCredentialsFragmentDialogCallback callback;
    private ArrayAdapter<String> billerAdapter;
    private String chosenBiller;
    private final String BILLER_DEFAULT = "Select Amount";
    final int DEFAULT_ITEM_POSITION = 0;
    private final String DollarAmpsand = "$";
    private boolean isError;
    private String responseDescription;
    private String dNumber;
    private String lpNumber;
    @ViewById
    protected EditText documentNumber;
    @ViewById
    protected EditText plateNumber;
    @ViewById
    protected TextView error;
    @ViewById
    protected TextView errorContent;
    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;
    public void setCallback(SunDocumentCredentialsFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {
        enableFinish(true);
        documentNumber.setFilters(new InputFilter[]{new DocumentNumberCurrencyFormatInputFilter()});
        plateNumber.setFilters(new InputFilter[]{new LicensePlateNumberCurrencyFormatInputFilter()});
    }

    @AfterTextChange
    protected void documentNumberAfterTextChanged(Editable s) {
        dNumber = s.toString();
        InvisibleErrorView();
        refresh();
    }

    @AfterTextChange
    protected void plateNumberAfterTextChanged(Editable s) {
        lpNumber = s.toString();
        InvisibleErrorView();
        refresh();
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
        return R.layout.sunpass_pay_your_document_credentials_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_sunpass_pay_your_document_title;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if(isValid())
                    complete();
                else{
                    setViewVisible(error);
                    setViewVisible(errorContent);
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
        boolean isValid = isValid();
        getPositiveButton().setEnabled(true);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(isValid ? colorOk : colorDisabled);
        if(isError)
            setErrorView();
    }

    private void setErrorView()
    {
        setViewVisible(error);
        errorContent.setText(responseDescription);
        setViewVisible(errorContent);
        isError = false;
    }
    private void setViewVisible(View view)
    {
        view.setVisibility(View.VISIBLE);
    }
    private void InvisibleErrorView()
    {
        error.setVisibility(View.INVISIBLE);
        errorContent.setVisibility(View.INVISIBLE);
    }

    private boolean isValid()
    {
        if(dNumber != null && dNumber.length() == 0)
        {
            errorContent.setText(EMPTY_DOCUMENT_NUMBER);
            return false;
        }
        if(plateNumber != null && plateNumber.length() == 0)
        {
            errorContent.setText(Empty_PLATE_NUMBER);
            return false;
        }
        return true;
    }

    protected boolean complete() {
        WaitDialogFragment.show(getActivity(), "Loading..");
        getBill();
        return true;
    }

    @OnSuccess(GetSunPassDocumentInquiryCommand.class)
    public void onGetSunPassDocumentInquiryCommandSuccess(@Param(GetSunPassDocumentInquiryCommand.ARG_RESULT) DocumentInquiryResponse result) {
        WaitDialogFragment.hide(getActivity());
        callback.onComplete(dNumber, result, lpNumber, FEE_AMOUNT);
        isError = false;
    }

    @OnFailure(GetSunPassDocumentInquiryCommand.class)
    public void onGetSunPassDocumentInquiryCommandFail(@Param(GetSunPassDocumentInquiryCommand.ARG_RESULT) DocumentInquiryResponse result) {
        WaitDialogFragment.hide(getActivity());
        isError = true;
        responseDescription = result.responseDescription;
        //shold remove later
        callback.onComplete(dNumber, result, lpNumber, FEE_AMOUNT);
        refresh();
    }

    public void getBill() {
        SunPassDocumentInquiryRequest request = new SunPassDocumentInquiryRequest();
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.cashier = cashierId;
        request.accountNumber = dNumber;
        request.transactionId = PrepaidProcessor.generateId();
        request.transactionMode = transactionMode;
        request.licensePlateNumber = lpNumber;
        GetSunPassDocumentInquiryCommand.start(getActivity(), this, request);
    }

    public static void show(FragmentActivity context,
                            String cashierId,
                            PrepaidUser user,
                            String transactionMode,
                            SunDocumentCredentialsFragmentDialogCallback callback) {
        SunPayYourDocumentCredentialsFragmentDialog dialog = SunPayYourDocumentCredentialsFragmentDialog_.builder()
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

    public interface SunDocumentCredentialsFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete(String accountNumber, DocumentInquiryResponse formedRequest, String licensePlateNumber, BigDecimal transcationFee);
    }
    protected void enableFinish(Boolean enabled) {
        getPositiveButton().setEnabled(enabled);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(enabled ? colorOk : colorDisabled);
    }
}
