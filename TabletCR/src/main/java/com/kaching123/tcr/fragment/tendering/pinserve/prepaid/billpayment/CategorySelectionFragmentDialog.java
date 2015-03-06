package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BillPaymentOptionsAdapter;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.GetBillerCategoriesCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.GetMasterBillerPaymentOptionsCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.GetMasterBillersByCategoryCommand;
import com.kaching123.tcr.component.AccountNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.CredentialsDialogWithCustomEditViewBase;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetBillerCategoriesRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetMasterBillerPaymentOptionsRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetMasterBillersByCategoryRequest;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BillerCategoriesResponse;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.MasterBillerPaymentOptionsResponse;
import com.kaching123.tcr.websvc.api.prepaid.MasterBillersByCategoryResponse;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;
import com.kaching123.tcr.websvc.api.prepaid.VectorCategory;
import com.kaching123.tcr.websvc.api.prepaid.VectorMasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.VectorPaymentOption;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class CategorySelectionFragmentDialog extends CredentialsDialogWithCustomEditViewBase implements CustomEditBox.IKeyboardSupport, BillPaymentOptionsAdapter.BillPaymentOptionsListener {

    private final String SELECT_CATEGORY = "Category";
    private final String CHOOSE_BILLER = "Biller";
    private final String CHOOSE_PAYMENT_OPTION= "Payment Option";
    private final String EMPTY_ACCOUNT_NUMER= "Account number";
    private final String NOT_EQUAL_ACCOUNT_NUMER= "The account numbers are not match";
    private final String EMPTY_AMOUNT= "Amount";
    private final String INVALID_AMOUNT= "Amount";
    private static final int DEFAULT_ID = 0;
    private static final String DIALOG_NAME = "CategorySelectionFragmentDialog";

    private final String DOLLAR_AMPERSAND = "$";

    private boolean isDefaultCategory;
    private boolean isDefaultBiller;

    private ArrayAdapter<Category> categoryArrayAdapter;
    private ArrayAdapter<MasterBiller> billerArrayAdapter;

    private BillPaymentOptionsAdapter optionsArrayAdapter;

    private Category chosenCategory;

    private MasterBiller chosenBiller;

    private PaymentOption chosenOption;

    private BillerLoadRecord billerData;

    private MetaInfo2 accNum;
    private MetaInfo2 accNumValidation;

    private BigDecimal chosenAmount;
    private BigDecimal minAmount = BigDecimal.ZERO;
    private BigDecimal maxAmount = BigDecimal.ZERO;
    private BigDecimal minAccountNumber = BigDecimal.ZERO;
    private BigDecimal maxAccountNumber = BigDecimal.ZERO;

    public boolean isChargeValid() {
        return isChargeValid;
    }

    public void setChargeValid(boolean isChargeValid) {
        this.isChargeValid = isChargeValid;
    }

    private boolean isChargeValid;
    protected CategorySelectionFragmentDialogCallback callback;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;

    @ViewById
    protected KeyboardView keyboard;
    @ViewById
    protected ListView listview;
    @ViewById
    protected Spinner category;
    @ViewById
    protected Spinner biller;
    @ViewById
    protected CustomEditBox accountNumberValidate;
    @ViewById
    protected CustomEditBox accountNumber;
    @ViewById
    protected CustomEditBox charge;
    @ViewById
    protected TextView feeTextview;
    @ViewById
    protected TextView totalTextview;
    @ViewById
    protected TextView amountTextview;
    @ViewById
    protected TextView error;
    @ViewById
    protected TextView errorContent;
    @ViewById
    protected LinearLayout linearlayout1;
    @ViewById
    protected LinearLayout linearlayout2;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;


    public void setCallback(CategorySelectionFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {
        keyboard.attachEditView(accountNumber);
        keyboard.setEnabled(false);
        setChargeView();
        setAccountValidateView();
        setAccountView();
        setChargeValid(false);
        setSpinners();
        WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
        getBillerCategories(getActivity());
    }

    private void initialMetaInfo2(int maxLength, int minLength) {
        accNum = new MetaInfo2(accountNumber, true, false, maxLength, minLength);
        accNumValidation = new MetaInfo2(accountNumberValidate, true, false, maxLength, minLength);
    }

    @AfterTextChange
    protected void accountNumberAfterTextChanged(Editable s) {
        setErrorGone();
        String numText = s.toString();
        String numValidationText = accNumValidation.editable.getText().toString();
        accNumValidation.validated = accNum.validated = numText.length() >= accNum.min && numValidationText.equals(numText);
        enableFinish();
    }

    @AfterTextChange
    protected void accountNumberValidateAfterTextChanged(Editable s) {
        setErrorGone();
        accountNumberAfterTextChanged((Editable) accNum.editable.getText());
    }

    protected void setLinearLayout(int visible) {
        setViewVisible(visible, linearlayout1);
        setViewVisible(visible, linearlayout2);
    }

    protected void setViewVisible(int visible, View view) {
        view.setVisibility(visible);
    }

    private void setListView(VectorPaymentOption options) {
        optionsArrayAdapter = new BillPaymentOptionsAdapter(getActivity(), options, this);
        listview.setAdapter(optionsArrayAdapter);
        optionsArrayAdapter.notifyDataSetChanged();
    }

    protected void setSpinners() {
        biller.setEnabled(false);
        category.setEnabled(false);

        ArrayList<MasterBiller> valuesBiller = new ArrayList<MasterBiller>(1);
        valuesBiller.add(new DefaultBiller());

        billerArrayAdapter = new ArrayAdapter<MasterBiller>(getActivity(), R.layout.prepaid_spinner_item, valuesBiller);
        billerArrayAdapter.setDropDownViewResource(R.layout.prepaid_spinner_dropdown_item);

        biller.setAdapter(billerArrayAdapter);
        biller.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> ignore1, View ignore2, int position, long ignore3) {
                setErrorGone();
                keyboard.setEnabled(false);
                if (position == DEFAULT_ID) {

                    isDefaultBiller = true;
                    setLinearLayout(View.GONE);

                    return;
                }
                isDefaultBiller = false;
                chosenBiller = (MasterBiller) ignore1.getAdapter().getItem(position);
                keyboard.setEnabled(true);
                WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
                getBillerOptions(getActivity(), chosenBiller.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {

            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<Category> valuesCategory = new ArrayList<Category>(1);
        valuesCategory.add(new DefaultCategory());

        categoryArrayAdapter = new ArrayAdapter<Category>(getActivity(), R.layout.prepaid_spinner_item, valuesCategory);
        categoryArrayAdapter.setDropDownViewResource(R.layout.prepaid_spinner_dropdown_item);

        category.setAdapter(categoryArrayAdapter);
        category.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> ignore1, View ignore2, int position, long ignore3) {
                setErrorGone();
                chosenBiller = null;
                resetTextViews();
                if (position == DEFAULT_ID) {
                    isDefaultCategory = true;
                    billerArrayAdapter.clear();
                    billerArrayAdapter.add(new DefaultBiller());
                    biller.setEnabled(false);
                    return;
                }
                isDefaultCategory = false;
                chosenCategory = (Category) ignore1.getAdapter().getItem(position);
                WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
                getMasterBillers(getActivity(), chosenCategory.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {

            }
        });
    }

    private void setChargeView() {
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return complete();
            }
        });
    }

    private void setAccountFilter(BigDecimal min, BigDecimal max) {
        initialMetaInfo2(min.intValue(), max.intValue());
        resetEditViews();
        resetTextViews();
        accountNumber.setFilters(new InputFilter[]{new AccountNumberCurrencyFormatInputFilter(min.toString(), max.toString())});
        accountNumberValidate.setFilters(new InputFilter[]{new AccountNumberCurrencyFormatInputFilter(min.toString(), max.toString())});

    }

    private void resetEditViews() {
        accountNumber.setText("");
        accountNumberValidate.setText("");
        charge.setText("");
    }

    private void resetTextViews() {
        feeTextview.setText("");
        amountTextview.setText("");
        totalTextview.setText("");
        enableFinish();
    }

    private void setAccountView() {
        accountNumber.setKeyboardSupportConteiner(this);
        accountNumber.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return complete();
            }
        });
    }

    private void setAmountTextView(String s) {
        if (s.length() > 0)
            amountTextview.setText(DOLLAR_AMPERSAND + s);
    }

    private void setFeeTextView(PaymentOption paymentOption) {
        feeTextview.setText(DOLLAR_AMPERSAND + paymentOption.feeAmount);
    }

    private void setTotalTextView() {
        double fee = 0.0;
        double amount = 0.0;
        if (feeTextview.getText().toString() != null && feeTextview.getText().toString() != "") {
            fee = new BigDecimal(feeTextview.getText().toString().substring(1)).doubleValue();
        }
        if (amountTextview.getText().toString() != null && amountTextview.getText().toString() != "") {
            amount = new BigDecimal((amountTextview.getText().toString().substring(1))).doubleValue();
        }
        totalTextview.setText(DOLLAR_AMPERSAND + String.valueOf(fee + amount));
    }

    private void setAccountValidateView() {
        accountNumberValidate.setKeyboardSupportConteiner(this);
        accountNumberValidate.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return complete();
            }
        });
    }


    @AfterTextChange
    protected void chargeAfterTextChanged(Editable s) {
        setErrorGone();
        if (s == null) {
            setChargeValid(false);
            return;
        }
        setAmountTextView(s.toString());
        setTotalTextView();
        BigDecimal amount;
        if(s.toString().length() > 0)
         amount = new BigDecimal(UiHelper.valueOf(new BigDecimal(s.toString())));
        else
         amount = new BigDecimal(0);
        try {
            assert minAmount != null;
            assert maxAmount != null;
            chosenAmount = amount;
            if (chosenAmount.compareTo(minAmount) >= 0) {
                if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && chosenAmount.compareTo(maxAmount) > 0) {
                    chosenAmount = maxAmount;
                    charge.setText(UiHelper.valueOf(maxAmount));
                    return;
                }
                setChargeValid(true);
            } else {
                setChargeValid(false);
            }
        } catch (NumberFormatException e) {
            setChargeValid(false);
        }
        enableFinish();
    }


    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        if (v == accountNumber)
            keyboard.setDotEnabled(false);
        else if (v == accountNumberValidate)
            keyboard.setDotEnabled(false);
        else if (v == charge)
            keyboard.setDotEnabled(true);
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
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
                getResources().getDimensionPixelOffset(R.dimen.prepaid_category_dlg_width),
                getDialog().getWindow().getAttributes().height);
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
        return R.layout.prepaid_category_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_wireless_bill_payment_title;
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
        if(isValid())
            callback.onConditionSelected(chosenCategory, chosenBiller, chosenOption, chosenAmount, billerData, accountNumber.getText().toString());
        else
        {
            setErrorVisible();
        }
        return true;
    }

    public static void show(FragmentActivity context, String transactionMode, String cashierId, PrepaidUser user, CategorySelectionFragmentDialogCallback listener) {
        CategorySelectionFragmentDialog dialog = CategorySelectionFragmentDialog_.builder()
                .transactionMode(transactionMode)
                .cashierId(cashierId)
                .user(user)
                .build();
        dialog.setCallback(listener);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected void enableFinish() {
//        boolean isValid = isValid();
        getPositiveButton().setEnabled(true);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(isValid ? colorOk : colorDisabled);
//        keyboard.setEnterEnabled(isValid);
    }
    private void setViewVisible(View view)
    {
        view.setVisibility(View.VISIBLE);
    }
    private void setErrorVisible()
    {
        if(!errorContent.getText().toString().equalsIgnoreCase(NOT_EQUAL_ACCOUNT_NUMER))
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
    private boolean isValid() {
        if(chosenCategory == null || isDefaultCategory)
        {
            errorContent.setText(SELECT_CATEGORY);
            return false;
        }
        if(chosenBiller == null || isDefaultBiller)
        {
            errorContent.setText(CHOOSE_BILLER);
            return false;
        }
        if(chosenOption == null)
        {
            errorContent.setText(CHOOSE_PAYMENT_OPTION);
            return false;
        }
        if(accountNumber.length() == 0)
        {
            errorContent.setText(EMPTY_ACCOUNT_NUMER);
            return false;
        }
        if(accountNumber.equals(accountNumberValidate))
        {
            errorContent.setText(NOT_EQUAL_ACCOUNT_NUMER);
            return false;
        }
        if(chosenAmount == null || chosenAmount.doubleValue() == BigDecimal.ZERO.doubleValue())
        {
            errorContent.setText(EMPTY_AMOUNT + " invalid, range must be from "+ minAmount + " to "+maxAmount);
            return false;
        }
        if(charge == null || charge.length() == 0 || chosenAmount == null || chosenAmount.doubleValue() < minAmount.doubleValue())
        {
            errorContent.setText(INVALID_AMOUNT+" invalid, range must be from "+ minAmount + " to "+maxAmount);
            return false;
        }
        return true;

//        return chosenBiller != null && accNum.validated && accNumValidation.validated && isChargeValid() && feeTextview.getText().toString() != null && feeTextview.getText().toString() != "";
    }

    @Override
    public void OnClicked(int position, PaymentOption paymentOption) {

        setErrorGone();
        chosenOption = paymentOption;
        setFeeTextView(paymentOption);
        setTotalTextView();
        enableFinish();
    }

    public interface CategorySelectionFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onConditionSelected(Category chosenCategory,
                                                 MasterBiller chosenBiller,
                                                 PaymentOption chosenOption,
                                                 BigDecimal amount,
                                                 BillerLoadRecord bilelrData,
                                                 String accountNumber);
    }

    @OnSuccess(GetBillerCategoriesCommand.class)
    public void onGetBillerCategoriesCommandSuccess(@Param(GetBillerCategoriesCommand.ARG_RESULT) BillerCategoriesResponse result) {
        WaitDialogFragment.hide(getActivity());
        VectorCategory categories = result.categories;
        assert categories != null && !categories.isEmpty();

        ArrayList<Category> values = new ArrayList<Category>(categories.size() + 1);
        values.add(new DefaultCategory());
        values.addAll(categories);

        categoryArrayAdapter.clear();
        categoryArrayAdapter.addAll(values);

        category.setEnabled(true);
    }

    @OnFailure(GetBillerCategoriesCommand.class)
    public void onGetBillerCategoriesCommandFail(@Param(GetBillerCategoriesCommand.ARG_RESULT) BillerCategoriesResponse result) {
        WaitDialogFragment.hide(getActivity());
        biller.setEnabled(false);
        callback.onError(getString(R.string.bill_payment_get_category_error));
    }

    @OnSuccess(GetMasterBillersByCategoryCommand.class)
    public void onGetMasterBillersByCategoryCommandSuccess(@Param(GetMasterBillersByCategoryCommand.ARG_RESULT) MasterBillersByCategoryResponse result) {
        WaitDialogFragment.hide(getActivity());
        VectorMasterBiller billers = result.masterBillers;
        assert billers != null && !billers.isEmpty();
        billerArrayAdapter.clear();
        billerArrayAdapter.add(new DefaultBiller());
        this.biller.setSelection(0);
        billerArrayAdapter.addAll(billers);
        biller.setEnabled(true);
    }

    @OnFailure(GetMasterBillersByCategoryCommand.class)
    public void onGetMasterBillersByCategoryCommandFail(@Param(GetMasterBillersByCategoryCommand.ARG_RESULT) MasterBillersByCategoryResponse result) {
        WaitDialogFragment.hide(getActivity());
        callback.onError(getString(R.string.bill_payment_get_master_error));
    }

    @OnSuccess(GetMasterBillerPaymentOptionsCommand.class)
    public void onGetMasterBillerPaymentOptionsCommandSuccess(@Param(GetMasterBillerPaymentOptionsCommand.ARG_RESULT) MasterBillerPaymentOptionsResponse result) {
        WaitDialogFragment.hide(getActivity());
        VectorPaymentOption options = result.paymentOptions;
        assert options != null && !options.isEmpty();

        setLinearLayout(View.VISIBLE);
        setListView(options);
        this.billerData = result.billerData;

        minAmount = new BigDecimal(billerData.vendorTranAmtMin);
        maxAmount = new BigDecimal(billerData.vendorTranAmtMax);

        minAccountNumber = new BigDecimal(billerData.vendorAccountLengthMin);
        maxAccountNumber = new BigDecimal(billerData.vendorAccountLengthMax);

        if (minAmount.compareTo(BigDecimal.ZERO) != 0 || maxAmount.compareTo(BigDecimal.ZERO) != 0) {
            charge.setHint(getString(R.string.prepaid_dialog_amount_limitations, UiHelper.valueOf(minAmount), UiHelper.valueOf(maxAmount)));
        } else {
            charge.setHint(R.string.prepaid_dialog_amount_no_limitation);
        }
        setAccountFilter(minAccountNumber, maxAccountNumber);
    }

    @OnFailure(GetMasterBillerPaymentOptionsCommand.class)
    public void onGetMasterBillerPaymentOptionsCommandFail(@Param(GetMasterBillerPaymentOptionsCommand.ARG_RESULT) MasterBillerPaymentOptionsResponse result) {
        WaitDialogFragment.hide(getActivity());
        callback.onError(getString(R.string.bill_payment_get_option_error));
    }

    public void getBillerCategories(final FragmentActivity context) {
        GetBillerCategoriesRequest request = new GetBillerCategoriesRequest();
        request.amount = BigDecimal.ZERO;
        request.transactionId = PrepaidProcessor.generateId();
        request.TransactionMode = transactionMode;
        request.Cashier = cashierId;
        request.MID = String.valueOf(user.getMid());
        request.TID = String.valueOf(user.getTid());
        request.Password = String.valueOf(user.getPassword());
        GetBillerCategoriesCommand.start(context, this, request);
    }

    public void getMasterBillers(final FragmentActivity context, String caretodyId) {
        GetMasterBillersByCategoryRequest request = new GetMasterBillersByCategoryRequest();
        request.amount = BigDecimal.ZERO;
        request.transactionId = PrepaidProcessor.generateId();
        request.TransactionMode = transactionMode;
        request.Cashier = cashierId;
        request.CaregoryId = caretodyId;
        request.MID = String.valueOf(user.getMid());
        request.TID = String.valueOf(user.getTid());
        request.Password = String.valueOf(user.getPassword());
        GetMasterBillersByCategoryCommand.start(context, new GetMasterBillersByCategoryCommand.MasterBillerCategoryCommand() {
            @Override
            protected void onSuccess(MasterBillersByCategoryResponse result) {

            }

            @Override
            protected void onFailure(MasterBillersByCategoryResponse result) {

            }
        }, request);
    }

    public void getBillerOptions(final FragmentActivity context, String billerId) {
        GetMasterBillerPaymentOptionsRequest request = new GetMasterBillerPaymentOptionsRequest();
        request.amount = BigDecimal.ZERO;
        request.transactionId = PrepaidProcessor.generateId();
        request.TransactionMode = transactionMode;
        request.Cashier = cashierId;
        request.masterBillerCaregoryId = billerId;
        request.MID = String.valueOf(user.getMid());
        request.TID = String.valueOf(user.getTid());
        request.Password = String.valueOf(user.getPassword());
        GetMasterBillerPaymentOptionsCommand.start(context, this, request);
    }

    private class DefaultCategory extends Category {

        private final String stringValue;

        private DefaultCategory() {
            stringValue = getString(R.string.bill_payment_category_dialog_category_placeholder);
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    private class DefaultBiller extends MasterBiller {

        private final String stringValue;

        private DefaultBiller() {
            stringValue = getString(R.string.bill_payment_category_dialog_biller_placeholder);
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

}
