package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.GetMasterBillerPaymentOptionsCommand;
import com.kaching123.tcr.component.AccountNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.PrepaidKeyboardView;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetMasterBillerPaymentOptionsRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBillerPaymentOptionsResponse;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidBillPaymentProductFragment extends Fragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface, CustomEditBox.IKeyboardSupport {

    public static final String ARG_ICON_URL = "ARG_ICON_URL";
    public static final String ARG_PRODUCT_NAME = "ARG_PRODUCT_NAME";
    @ViewById
    protected TextView productName, enterAmountInterval, totalLinearAmountContent, totalLinearFeeContent, totalLinearTotalContent;
    @ViewById
    protected ListView list;
    @ViewById
    protected CustomEditBox accountNumber, confirmAccountNumber, amount;
    @ViewById
    protected PrepaidKeyboardView keyboard;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected Category chosenCategory;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected BillPaymentItem billPaymentItem;
    @FragmentArg
    protected long transactionId;
    @FragmentArg
    protected int searchMode;
    private List paymentOptions;
    private BillerLoadRecord billerData;
    private boolean amountValid;
    private BigDecimal chosenAmount;
    private BigDecimal fee;
    private BigDecimal total;
    private BigDecimal minAmount = BigDecimal.ZERO;
    private BigDecimal maxAmount = BigDecimal.ZERO;
    private final String MIN = "Min";
    private final String MAX = "Max";
    private PaymentOption chosenOption;

    private String accountNumberInput;
    private String confirmAccountNumberInput;

    private optionAdapter adapter;

    private PrepaidLongDistanceProductFragment.ProductFragmentCallback productFragmentCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_bill_payment_product_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateUI() {
        productName.setText(billPaymentItem.masterBillerId.toUpperCase());
    }

    @AfterViews
    public void init() {
        keyboard.attachEditView(accountNumber);
        setAmountView();
        setAccountValidateView();
        setAccountView();
        paymentOptions = new ArrayList<PaymentOption>();
        initFragment();
        updateUI();

    }

    private void setAccountFilter(String min, String max) {
        accountNumber.setFilters(new InputFilter[]{new AccountNumberCurrencyFormatInputFilter(min, max)});
        confirmAccountNumber.setFilters(new InputFilter[]{new AccountNumberCurrencyFormatInputFilter(min, max)});

    }

    private void setAmountView() {
        amount.setKeyboardSupportConteiner(this);
        amount.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        amount.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                complete();
                return false;
            }
        });
    }

    private void setAccountValidateView() {
        confirmAccountNumber.setKeyboardSupportConteiner(this);
        confirmAccountNumber.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                complete();
                return false;
            }
        });
    }

    private void setAccountView() {
        accountNumber.setKeyboardSupportConteiner(this);
        accountNumber.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                complete();
                return false;
            }
        });
    }

    @Click
    protected void check() {
        complete();
    }

    private void complete() {
        if (isValidate())
            productFragmentCallback.additionalDataRequired(chosenCategory, billPaymentItem, chosenOption, cashierId, user, transactionMode, fee, chosenAmount, total, billerData, accountNumberInput);
    }

    @AfterTextChange
    protected void accountNumberAfterTextChanged(Editable s) {
        setErrorGone();
        accountNumberInput = s.toString();
    }

    @AfterTextChange
    protected void confirmAccountNumberAfterTextChanged(Editable s) {
        setErrorGone();
        confirmAccountNumberInput = s.toString();
    }

    @AfterTextChange
    protected void amountAfterTextChanged(Editable s) {
        setErrorGone();
        if (s == null) {
            amountValid = false;
            return;
        }


        BigDecimal amountTemp;
        if (s.toString().length() > 0)
            amountTemp = new BigDecimal(UiHelper.valueOf(new BigDecimal(s.toString())));
        else
            amountTemp = new BigDecimal(0);

        try {
            assert minAmount != null;
            assert maxAmount != null;
            chosenAmount = amountTemp;
            setAmountTextView(amountTemp.toString());
            if (chosenAmount.compareTo(minAmount) >= 0) {
                if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && chosenAmount.compareTo(maxAmount) > 0) {
                    chosenAmount = maxAmount;
                    amount.setText(UiHelper.valueOf(maxAmount));
                    return;
                }
                amountValid = true;
            } else {
                amountValid = false;
            }
        } catch (NumberFormatException e) {
            amountValid = false;
        }
    }

    private void setErrorGone() {
        productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.ENTER_ACCOUNT_AND_AMOUNT);
    }

    private void setAmountTextView(String s) {
        totalLinearAmountContent.setText(commaPriceFormat(new BigDecimal(s)));
        setTotalView();
    }

    private void setFeeTextView(BigDecimal s) {
        totalLinearFeeContent.setText(commaPriceFormat(s));
        setTotalView();
    }

    private void setTotalView() {
        fee = totalLinearFeeContent.getText() == null || totalLinearFeeContent.getText().toString().equalsIgnoreCase("") ? BigDecimal.ZERO : new BigDecimal(totalLinearFeeContent.getText().toString().substring(2));
        total = fee.add(chosenAmount == null ? BigDecimal.ZERO : chosenAmount);
        totalLinearTotalContent.setText(commaPriceFormat(total));
    }

    private boolean isValidate() {


        if (accountNumberInput == null || accountNumberInput.equalsIgnoreCase("")) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.ACCOUNT_NUMBER_NULL);
            return false;
        }
        if (confirmAccountNumberInput == null || confirmAccountNumberInput.equalsIgnoreCase("")) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.CONFIRM_ACCOUNT_NUMBER_NULL);
            return false;
        }
        if (!accountNumberInput.equalsIgnoreCase(confirmAccountNumberInput)) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.ACCOUNT_NUMBER_NOT_EQUAL);
            return false;
        }
        if (chosenAmount == null || chosenAmount == BigDecimal.ZERO) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.AMOUNT_ZERO_ERROR);
            return false;
        }
        if (chosenAmount == null || chosenAmount == BigDecimal.ZERO) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.AMOUNT_ZERO_ERROR);
            return false;
        }
        if (chosenAmount != null && chosenAmount.compareTo(minAmount) < 0) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.AMOUNT_MINIMUN_ERROR);
            return false;
        }
        if (totalLinearFeeContent.getText() == null || totalLinearFeeContent.getText().toString().equalsIgnoreCase("")) {
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.FEE_NULL);
            return false;
        }
        return true;
    }

    private void initFragment() {
        WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
        getBillerOptions(getActivity(), billPaymentItem.masterBillerId);
    }

    private void updateAmountInterval() {
        enterAmountInterval.setText(MIN + " " + commaPriceFormat(new BigDecimal(billerData.vendorTranAmtMin)) + "/" + MAX + commaPriceFormat(new BigDecimal(billerData.vendorAccountLengthMax)));
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

    @Override
    public void onBackPressed() {
        if (searchMode == PrepaidLongDistanceActivity.COUNTRY_SEARCH)
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_BILLER);
        else
            productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_CATEGORY_OR_MOST_POPULAR_BILLER);
        productFragmentCallback.popUpFragment(searchMode);
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        if (v == accountNumber)
            keyboard.setDotEnabled(false);
        else if (v == confirmAccountNumber)
            keyboard.setDotEnabled(false);
        else if (v == amount)
            keyboard.setDotEnabled(true);
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    @Override
    public void onResume() {
        super.onResume();
        productFragmentCallback.headMessage(PrepaidLongDistanceHeadFragment.ENTER_ACCOUNT_AND_AMOUNT);
    }

    class ProductInfoMenuFragmentCallback implements PrepaidLongDistanceProductInfoMenuFragment.LongDistanceProductInfoMenuInterface {
        @Override
        public void menuSelected(int position) {
            productFragmentCallback.menuSelected(position);
        }

        @Override
        public void conditionSelected(BigDecimal amount, String phoneNumber, BigDecimal feeAmount) {
            productFragmentCallback.conditionSelected(amount, phoneNumber, feeAmount);
        }

        @Override
        public void headMessage(int errorCode) {
            productFragmentCallback.headMessage(errorCode);
        }
    }

    public void setCallback(PrepaidLongDistanceProductFragment.ProductFragmentCallback callback) {
        this.productFragmentCallback = callback;
    }

    @OnSuccess(GetMasterBillerPaymentOptionsCommand.class)
    public void onGetMasterBillerPaymentOptionsCommandSuccess(@Param(GetMasterBillerPaymentOptionsCommand.ARG_RESULT) MasterBillerPaymentOptionsResponse result) {
        WaitDialogFragment.hide(getActivity());
        paymentOptions.addAll(result.paymentOptions);
        billerData = result.billerData;
        minAmount = new BigDecimal(billerData.vendorTranAmtMin);
        maxAmount = new BigDecimal(billerData.vendorTranAmtMax);

        BigDecimal minLength = new BigDecimal(billerData.vendorAccountLengthMin);
        BigDecimal maxLength = new BigDecimal(billerData.vendorAccountLengthMax);

        if (minLength.compareTo(maxLength) == 0)
            minLength = minLength.subtract(BigDecimal.ONE);
        setAccountFilter(minLength.toString(), maxLength.toString());
        updateAmountInterval();
        adapter = new optionAdapter();
        list.setAdapter(adapter);
    }

    @OnFailure(GetMasterBillerPaymentOptionsCommand.class)
    public void onGetMasterBillerPaymentOptionsCommandFail(@Param(GetMasterBillerPaymentOptionsCommand.ARG_RESULT) MasterBillerPaymentOptionsResponse result) {
        WaitDialogFragment.hide(getActivity());
        error();
    }

    @UiThread
    protected void error() {
        productFragmentCallback.error("Bill Payment network error");
    }

    class optionAdapter extends BaseAdapter {
        private String selectedTextAmount;

        @Override
        public int getCount() {
            return paymentOptions.size();
        }

        @Override
        public Object getItem(int position) {
            return paymentOptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            MyViewHolder mViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.productlist_item_bill_payment_options_view, parent, false);
                mViewHolder = new MyViewHolder();
                mViewHolder.amountOption = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            String sDescription = updateOptionDes(position);
            ;


            final PaymentOption paymentOption = (PaymentOption) paymentOptions.get(position);
            mViewHolder.amountOption.setText(commaPriceFormat(new BigDecimal(paymentOption.feeAmount)) + " " + paymentOption.paymentType + "\n" + sDescription);
            if (selectedTextAmount != null && selectedTextAmount.contains(commaPriceFormat(new BigDecimal(paymentOption.feeAmount)))) {
                mViewHolder.amountOption.setTextColor(getResources().getColor(R.color.prepaid_dialog_white));
                mViewHolder.amountOption.setBackgroundResource(R.drawable.amount_button_background_pressed);
            } else {
                mViewHolder.amountOption.setTextColor(getResources().getColor(R.color.prepaid_blue_divider));
                mViewHolder.amountOption.setBackgroundResource(R.drawable.amount_button_background_normal);
            }


            mViewHolder.amountOption.setPadding(20, 10, 0, 0);

            mViewHolder.amountOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setErrorGone();
                    TextView tx = (TextView) v;
                    selectedTextAmount = tx.getText().toString();
                    chosenOption = paymentOption;
                    setFeeTextView(new BigDecimal(paymentOption.feeAmount));
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    class MyViewHolder {
        TextView amountOption;
    }

    private String updateOptionDes(int position) {
        String sDescription = null;
        if (((PaymentOption) paymentOptions.get(position)).paymentType.equalsIgnoreCase(getString(R.string.SAME_DAY)))
            sDescription = InCutOffTime() == true ? (getString(R.string.SAME_DAY) + getString(R.string.SAME_DAY_IN_CUTOFFTIME_HOURS)) : (getString(R.string.SAME_DAY) + getString(R.string.SAME_DAY_OVER_CUTOFFTIME_HOURS));
        else if (((PaymentOption) paymentOptions.get(position)).paymentType.equalsIgnoreCase(getString(R.string.NEXT_DAY)))
            sDescription = InCutOffTime() == true ? (getString(R.string.NEXT_DAY) + getString(R.string.NEXT_DAY_IN_CUTOFFTIME_HOURS)) : (getString(R.string.NEXT_DAY) + getString(R.string.NEXT_DAY_OVER_CUTOFFTIME_HOURS));
        else if (((PaymentOption) paymentOptions.get(position)).paymentType.equalsIgnoreCase(getString(R.string.STANDARD)))
            sDescription = InCutOffTime() == true ? (getString(R.string.STANDARD) + getString(R.string.STANDARD_IN_CUTOFFTIME_HOURS)) : (getString(R.string.STANDARD) + getString(R.string.STANDARD_OVER_CUTOFFTIME_HOURS));
        return sDescription;
    }

    private boolean InCutOffTime() {
        boolean InCutOffTime = false;
        try {
            String pattern = "HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date cutOffTime = sdf.parse("17:00");
            String UTCTime = sdf.format(new Date());
            Date UTCTimes = sdf.parse(UTCTime);
            InCutOffTime = (UTCTimes.compareTo(cutOffTime) < 0);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return InCutOffTime;
    }
}
