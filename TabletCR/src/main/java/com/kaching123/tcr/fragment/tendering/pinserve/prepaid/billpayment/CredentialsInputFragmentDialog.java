package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.CredentialsDialogWithCustomEditViewBase;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadFormDetails;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class CredentialsInputFragmentDialog extends CredentialsDialogWithCustomEditViewBase {

    private final String INVALID_FIELDS = "Please fill the lines which has Red star";
    private static final String DIALOG_NAME = "CredentialsInputFragmentDialog";
    private static final String Y = "Y";

    private int TYPE_NUMBER = InputType.TYPE_CLASS_NUMBER;
    private int TYPE_TEXT = InputType.TYPE_CLASS_TEXT;
    private BigDecimal total;
    private final String DOLLAR_AMPERSAND = "$";

    private MetaInfo2 accCustFirst;
    private MetaInfo2 accCustLast;
    private MetaInfo2 accSendFirst;
    private MetaInfo2 accSendLast;
    private MetaInfo2 accNumAlter;
    private MetaInfo2 accAddFirst;
    private MetaInfo2 accAddSecond;

    @FragmentArg
    protected Category chosenCategory;
    @FragmentArg
    protected MasterBiller chosenBiller;
    @FragmentArg
    protected PaymentOption chosenOption;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected BillerLoadRecord billerData;
    @FragmentArg
    protected BigDecimal chosenAmount;
    @FragmentArg
    protected String accountNumber;

    protected CredentialsInputFragmentDialogCallback callback;

    @ViewById
    protected View accountNumberAlternativeContainer;
    @ViewById
    protected View accountAddFirstContainer;
    @ViewById
    protected View accountAddSecondContainer;

    @ViewById
    protected EditText accountNumberAlternative;
    @ViewById
    protected EditText accountAddFirst;
    @ViewById
    protected EditText accountAddSecond;
    @ViewById
    protected EditText accountCustomerFirstName;
    @ViewById
    protected EditText accountCustomerLastName;
    @ViewById
    protected EditText accountSenderFirstName;
    @ViewById
    protected EditText accountSenderLastName;
    @ViewById
    protected TextView amountTextview;
    @ViewById
    protected TextView feeTextview;
    @ViewById
    protected TextView totalTextview;
    @ViewById
    protected TextView error;
    @ViewById
    protected TextView errorContent;

    public void setCallback(CredentialsInputFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {
        enableFinish(false);

        total = new BigDecimal(String.valueOf(chosenAmount.doubleValue() + chosenOption.feeAmount));
        setAmountView();

        final BillerLoadFormDetails load = billerData.formDetails;
        final boolean customerNameWanted = Y.equals(billerData.custNameRequired);
        final boolean senderNameWanted = Y.equals(billerData.senderNameRequired);
        final boolean additionalNameWanted = load != null && Y.equals(billerData.formFlag);
        final boolean additionalNameAlt1Wanted = additionalNameWanted && Y.equals(load.addInfoReqFlag1);
        final boolean additionalNameAlt2Wanted = additionalNameWanted && Y.equals(load.addInfoReqFlag2);

        accCustFirst = new MetaInfo2(accountCustomerFirstName, customerNameWanted, !customerNameWanted);
        accCustLast = new MetaInfo2(accountCustomerLastName, customerNameWanted, !customerNameWanted);
        accSendFirst = new MetaInfo2(accountSenderFirstName, senderNameWanted, !senderNameWanted);
        accSendLast = new MetaInfo2(accountSenderLastName, senderNameWanted, !senderNameWanted);
        accNumAlter = new MetaInfo2(accountNumberAlternative, additionalNameWanted, !additionalNameWanted);
        accAddFirst = new MetaInfo2(accountAddFirst, additionalNameAlt1Wanted, !additionalNameAlt1Wanted);
        accAddSecond = new MetaInfo2(accountAddSecond, additionalNameAlt2Wanted, !additionalNameAlt2Wanted);

        if (additionalNameWanted) {
            String altLookupLabel = load.altLookupLabel;
            String addInfoLabel1 = load.addInfoLabel1;
            String addInfoLabel2 = load.addInfoLabel2;

            int altLookupMaxLength = Integer.parseInt(load.altLookupMaxLen);
            int addInfoMaxLen1 = Integer.parseInt(load.addInfoMaxLen1);
            int addInfoMaxLen2 = Integer.parseInt(load.addInfoMaxLen2);

            accNumAlter.setHint(altLookupLabel);
            accAddFirst.setHint(addInfoLabel1);
            accAddSecond.setHint(addInfoLabel2);
            if (addInfoLabel1 != null && addInfoLabel1.equalsIgnoreCase("EmailID")) {
                accAddFirst.setInputType(TYPE_TEXT);
            } else {
                accAddFirst.setInputType(TYPE_NUMBER);
            }
            accNumAlter.setInputType(TYPE_NUMBER);
            accAddSecond.setInputType(TYPE_NUMBER);
//            accNumAlter.label.setText(altLookupLabel);
//            accAddFirst.label.setText(addInfoLabel1);
//            accAddSecond.label.setText(addInfoLabel2);

            accNumAlter.setMax(altLookupMaxLength);
            accAddFirst.setMax(addInfoMaxLen1);
            accAddSecond.setMax(addInfoMaxLen2);

            if (altLookupMaxLength <= 0) {
                accNumAlter.validated = true;
                accountNumberAlternativeContainer.setVisibility(View.GONE);
            }
            if (addInfoMaxLen1 <= 0) {
                accAddFirst.validated = true;
                accountAddFirstContainer.setVisibility(View.GONE);
            }
            if (addInfoMaxLen2 <= 0) {
                accAddSecond.validated = true;
                accountAddSecondContainer.setVisibility(View.GONE);
            }
        } else {
            accNumAlter.setInputType(TYPE_NUMBER);
            accAddFirst.setInputType(TYPE_NUMBER);
            accAddSecond.setInputType(TYPE_NUMBER);
        }
        refresh();
    }

    protected void setAmountView()
    {
        amountTextview.setText(DOLLAR_AMPERSAND + chosenAmount);
        feeTextview.setText(DOLLAR_AMPERSAND + chosenOption.feeAmount);
        totalTextview.setText(DOLLAR_AMPERSAND + total);
    }

    @AfterTextChange
    protected void accountCustomerLastNameAfterTextChanged(Editable s) {
        accCustLast.revalidate();
        refresh();
    }

    @AfterTextChange
    protected void accountCustomerFirstNameAfterTextChanged(Editable s) {
        accCustFirst.revalidate();
        refresh();
    }

    @AfterTextChange
    protected void accountSenderFirstNameAfterTextChanged(Editable s) {
        accSendFirst.revalidate();
        refresh();
    }

    @AfterTextChange
    protected void accountSenderLastNameAfterTextChanged(Editable s) {
        accSendLast.revalidate();
        refresh();
    }

    @AfterTextChange
    protected void accountAddFirstAfterTextChanged(Editable s) {
        accAddFirst.revalidate();
        refresh();
    }

    @AfterTextChange
    protected void accountAddSecondAfterTextChanged(Editable s) {
        accAddSecond.revalidate();
        refresh();
    }

    @AfterTextChange
    protected void accountNumberAlternativeAfterTextChanged(Editable s) {
        accNumAlter.revalidate();
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
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_heigth));
    }

    @Override
    protected int getSeparatorColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleTextColor() {
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
    protected int getTitleIcon() {
        return R.drawable.icon_bill_payment;
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_next;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.prepaid_credentials_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_credentials_title;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if(isValid())
                    complete();
                else
                {
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
        setErrorGone();
        boolean valid =
                accCustFirst.validated
                        && accCustLast.validated
                        && accSendFirst.validated
                        && accSendLast.validated
                        && accNumAlter.validated
                        && accAddFirst.validated
                        && accAddSecond.validated;
        getPositiveButton().setEnabled(true);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(valid ? colorOk : colorDisabled);
    }
    private boolean isValid()
    {
        if(!(accCustFirst.validated
                && accCustLast.validated
                && accSendFirst.validated
                && accSendLast.validated
                && accNumAlter.validated
                && accAddFirst.validated
                && accAddSecond.validated))
        {
            errorContent.setText(INVALID_FIELDS);
            return false;
        }

        return true;
    }
    private void setViewVisible(View view)
    {
        view.setVisibility(View.VISIBLE);
    }
    private void setErrorVisible()
    {
        setViewVisible(error);
        setViewVisible(errorContent);
    }
    private void setErrorGone()
    {
        setViewGone(error);
        setViewGone(errorContent);
    }
    private void setViewGone(View view)
    {
        view.setVisibility(View.GONE);
    }
    protected boolean complete() {
        BillPaymentRequest request = new BillPaymentRequest();
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.cashier = cashierId;
        request.vendorId = billerData.vendorID;
        request.accountNumber = this.accountNumber;
        request.altAccountNumber = accNumAlter.toString();
        request.additAccountNumber1 = accAddFirst.toString();
        request.additAccountNumber2 = accAddSecond.toString();
        request.paymentAmount = total;
        request.feeAmount = chosenOption.feeAmount;
        request.customerFirstName = accCustFirst.toString();
        request.customerLastName = accCustLast.toString();
        request.paymentType = chosenOption.paymentType;
        request.senderFirstName = accSendFirst.toString();
        request.senderLastName = accSendLast.toString();
        request.transactionMode = transactionMode;

        callback.onComplete(request, total);
        return true;
    }

    public static void show(FragmentActivity context,
                            Category chosenCategory,
                            MasterBiller chosenBiller,
                            PaymentOption chosenOption,
                            String cashierId,
                            PrepaidUser user,
                            String transactionMode,
                            BigDecimal chosenAmount,
                            BillerLoadRecord billerData,
                            String accountNumber,
                            CredentialsInputFragmentDialogCallback callback) {
        CredentialsInputFragmentDialog dialog = CredentialsInputFragmentDialog_.builder()
                .chosenCategory(chosenCategory)
                .chosenBiller(chosenBiller)
                .chosenOption(chosenOption)
                .cashierId(cashierId)
                .user(user)
                .transactionMode(transactionMode)
                .chosenAmount(chosenAmount)
                .billerData(billerData)
                .accountNumber(accountNumber)
                .build();
        dialog.setCallback(callback);
        DialogUtil.show(context, DIALOG_NAME, dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface CredentialsInputFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete(BillPaymentRequest formedRequest, BigDecimal total);
    }
}
