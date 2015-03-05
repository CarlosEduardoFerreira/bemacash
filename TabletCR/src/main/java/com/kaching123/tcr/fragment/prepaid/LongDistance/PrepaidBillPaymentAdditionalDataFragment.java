package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadFormDetails;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidBillPaymentAdditionalDataFragment extends PrepaidLongDistanceBaseBodyFragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface {

    @FragmentArg
    protected Category chosenCategory;
    @FragmentArg
    protected BillPaymentItem chosenBillPaymentItem;
    @FragmentArg
    protected PaymentOption chosenOption;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected BigDecimal fee;
    @FragmentArg
    protected BigDecimal chosenAmount;
    @FragmentArg
    protected BigDecimal total;
    @FragmentArg
    protected BillerLoadRecord billerData;
    @FragmentArg
    protected String accountNumber;

    @ViewById
    protected TextView productName, totalLinearAmountContent, totalLinearFeeContent, totalLinearTotalContent, accountNumberAlternativeTitle, accountAddFirstTitle, accountAddSecondTitle;
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
    private static final String Y = "Y";
    private MetaInfo2 accCustFirst;
    private MetaInfo2 accCustLast;
    private MetaInfo2 accSendFirst;
    private MetaInfo2 accSendLast;
    private MetaInfo2 accNumAlter;
    private MetaInfo2 accAddFirst;
    private MetaInfo2 accAddSecond;
    private int TYPE_NUMBER = InputType.TYPE_CLASS_NUMBER;
    private int TYPE_TEXT = InputType.TYPE_CLASS_TEXT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.enter_additional_data, container, false);
    }

    @AfterViews
    public void init() {
        productName.setText(chosenBillPaymentItem.masterBillerId);
        pcCallback.headMessage(PrepaidLongDistanceHeadFragment.ENTER_ADDITIONAL_DATA);
        initialEditView();
        setTotalView();
    }

    private void setTotalView() {
        totalLinearAmountContent.setText(chosenAmount.toString());
        totalLinearFeeContent.setText(fee.toString());
        totalLinearTotalContent.setText(total.toString());
    }


    private void initialEditView() {
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
                setAccountNumberAlternativeViewGone();
            }
            if (addInfoMaxLen1 <= 0) {
                accAddFirst.validated = true;
                setFirstContainerViewGone();
            }
            if (addInfoMaxLen2 <= 0) {
                accAddSecond.validated = true;
                setSecondContainerViewGone();
            }
        } else {
            accNumAlter.setInputType(TYPE_NUMBER);
            accAddFirst.setInputType(TYPE_NUMBER);
            accAddSecond.setInputType(TYPE_NUMBER);
        }
    }

    private void setAccountNumberAlternativeViewGone() {
        accountNumberAlternative.setVisibility(View.GONE);
        accountNumberAlternativeTitle.setVisibility(View.GONE);
    }

    private void setFirstContainerViewGone() {
        accountAddFirst.setVisibility(View.GONE);
        accountAddFirstTitle.setVisibility(View.GONE);

    }

    private void setSecondContainerViewGone() {
        accountAddSecond.setVisibility(View.GONE);
        accountAddSecondTitle.setVisibility(View.GONE);
    }

    @AfterTextChange
    protected void accountCustomerLastNameAfterTextChanged(Editable s) {
        accCustLast.revalidate();
    }

    @AfterTextChange
    protected void accountCustomerFirstNameAfterTextChanged(Editable s) {
        accCustFirst.revalidate();
    }

    @AfterTextChange
    protected void accountSenderFirstNameAfterTextChanged(Editable s) {
        accSendFirst.revalidate();
    }

    @AfterTextChange
    protected void accountSenderLastNameAfterTextChanged(Editable s) {
        accSendLast.revalidate();
    }

    @AfterTextChange
    protected void accountAddFirstAfterTextChanged(Editable s) {
        accAddFirst.revalidate();
    }

    @AfterTextChange
    protected void accountAddSecondAfterTextChanged(Editable s) {
        accAddSecond.revalidate();
    }

    @AfterTextChange
    protected void accountNumberAlternativeAfterTextChanged(Editable s) {
        accNumAlter.revalidate();
    }

    @Click
    void send() {
        if (isValid())
            complete();

    }

    private boolean isValid() {
        if (!(accCustFirst.validated
                && accCustLast.validated
                && accSendFirst.validated
                && accSendLast.validated
                && accNumAlter.validated
                && accAddFirst.validated
                && accAddSecond.validated)) {
            pcCallback.headMessage(PrepaidLongDistanceHeadFragment.INVALID_FIELDS);
            return false;
        }

        return true;
    }

    protected boolean complete() {
        BillPaymentRequest request = new BillPaymentRequest();
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.cashier = cashierId;
        request.vendorId = billerData.vendorID;
        request.accountNumber = this.accountNumber;
        request.altAccountNumber = accNumAlter.editable.getText().toString().trim();
        request.additAccountNumber1 = accAddFirst.editable.getText().toString().trim();
        request.additAccountNumber2 = accAddSecond.editable.getText().toString().trim();
        request.paymentAmount = total;
        request.feeAmount = chosenOption.feeAmount;
        request.customerFirstName = accCustFirst.editable.getText().toString().trim();
        request.customerLastName = accCustLast.editable.getText().toString().trim();
        request.paymentType = chosenOption.paymentType;
        request.senderFirstName = accSendFirst.editable.getText().toString().trim();
        request.senderLastName = accSendLast.editable.getText().toString().trim();
        request.transactionMode = transactionMode;
        pcCallback.complete(chosenCategory, chosenBillPaymentItem, chosenOption, chosenAmount, billerData, accountNumber, request, total, fee);
        return true;
    }

    private ProductAdditionalDataCallback pcCallback;

    public void setCallback(ProductAdditionalDataCallback pcCallback) {
        this.pcCallback = pcCallback;
    }

    @Override
    public void onBackPressed() {
        pcCallback.headMessage(PrepaidLongDistanceHeadFragment.ENTER_ACCOUNT_AND_AMOUNT);
        pcCallback.popUpFragment();
    }

    public interface ProductAdditionalDataCallback {
        void complete(Category chosenCategory,
                      BillPaymentItem chosenBillPaymentItem,
                      PaymentOption chosenOption,
                      BigDecimal amount,
                      BillerLoadRecord billerData,
                      String accountNumber,
                      BillPaymentRequest formedRequest,
                      BigDecimal total,
                      BigDecimal transactionFee);

        void popUpFragment();

        void headMessage(int code);
    }

    public class MetaInfo2 {

        public int max;
        public int min;
        public TextView editable;
        public boolean wanted;
        public boolean validated;

        public MetaInfo2(TextView editable, boolean wanted, boolean validated) {
            this(editable, wanted, validated, 0, 0);
        }

        public MetaInfo2(TextView editable, boolean wanted, boolean validated, int max, int min) {
            this.editable = editable;
            this.wanted = wanted;
            this.validated = validated;
            if (max >= min && max > 0) {
                setMax(max);
            }
        }

        public void setHint(String hint) {
            String mHint = hint;
            if (mHint != null) {
                if (wanted) {
                    SpannableString sshint = new SpannableString(hint + "*");
                    sshint.setSpan(new ForegroundColorSpan(Color.RED), hint.length(), hint.length() + 1, 0);
                    this.editable.setHint(sshint);
                } else
                    this.editable.setHint(hint);
            }


        }

        public void setMax(int max) {
            this.max = max;
            applyValidation();
        }

        public void applyValidation() {
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(this.max);
            this.editable.setFilters(filterArray);
        }

        public void setInputType(int inputType) {
            this.editable.setInputType(inputType);
        }

        public void revalidate() {
            String text = this.editable.getText().toString();
            int length = text != null ? text.length() : 0;
            this.validated = !this.wanted || (length >= this.min && (this.max <= this.min || length <= this.max));
        }
    }
}
