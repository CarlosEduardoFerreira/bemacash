package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.adapter.BondItemAdapter;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.PayTenderUnitedKeyboardView;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.EBTPaymentTypeChooserDialogFragment;
import com.kaching123.tcr.fragment.tendering.TenderFragmentDialogBase;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.payment.PaymentMethod;
import com.kaching123.tcr.service.DisplayService;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.valueOf;

/**
 * Created by alboyko on 03.08.2016.
 */
@EFragment
public class PayTenderUnitedFragmentDialog extends TenderFragmentDialogBase<PayTenderUnitedFragmentDialog, IPaymentDialogListener.IPayTenderUnitedListener> {
    private static final String DIALOG_NAME = "paymenttenderunitedDialog";

    protected CurrencyTextWatcher currencyTextWatcher;

    private static final List<Integer> BONDS_LIST = new ArrayList<>();

    private BigDecimal orderRemaining = BigDecimal.ZERO;

    static {
        BONDS_LIST.add(1);
        BONDS_LIST.add(5);
        BONDS_LIST.add(10);
        BONDS_LIST.add(20);
        BONDS_LIST.add(50);
        BONDS_LIST.add(100);
    }

    @ViewById
    protected GridView bonds;

    @ViewById
    protected View btnExact;

    @ViewById
    protected Button btnCheck;

    @FragmentArg
    protected Boolean singleTenderEnabled;

    @ViewById
    protected CustomEditBox charge;

    @ViewById
    protected PayTenderUnitedKeyboardView keyboard;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;

    private void setChargeView() {
        currencyTextWatcher = new CurrencyTextWatcher(charge);
        Log.d("BemaCarl25", "PayTenderUnitedFragmentDialog.setChargeView.orderTotal: " + orderTotal);
        charge.setKeyboardSupportConteiner(new CustomEditBox.IKeyboardSupport() {
            @Override
            public void attachMe2Keyboard(CustomEditBox v) {
                keyboard.attachEditView(v);
            }

            @Override
            public void detachMe4Keyboard(CustomEditBox v) {
                keyboard.detachEditView();
            }
        });

        keyboard.setEnterVisibility(View.GONE);

        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.addTextChangedListener(currencyTextWatcher);
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return false;//tryProceed();
            }
        });
        charge.addTextChangedListener(new ChargeTextWatcher(charge));

        bonds.setAdapter(new BondItemAdapter(getContext(), BONDS_LIST));
        bonds.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                charge.setText(String.valueOf(id));
                tryProceed(PaymentMethod.CASH);
            }
        });
    }

    private void setTenderButtonsVisibilityWithServerSettings() {
        btnGiftCard.setVisibility(getApp().isGiftCardEnabled() ? View.VISIBLE : View.GONE);
        btnCard.setVisibility(getApp().isPaxConfigured() &&
                getApp().isCreditCardEnabled() ? View.VISIBLE : View.GONE);
        btnPaxDebit.setVisibility(getApp().isPaxConfigured() &&
                getApp().isDebitCardEnabled() ? View.VISIBLE : View.GONE);
        btnPaxEbtCash.setVisibility(orderEbtTotal != null
                && orderEbtTotal.subtract(completedEbtAmount).compareTo(BigDecimal.ZERO) > 0
                && getApp().isPaxConfigured()
                && getApp().isEbtEnabled()
                && !hasNonEbtTransactions ? View.VISIBLE : View.GONE);

        btnOfflineCredit.setVisibility(getApp().isOfflineCreditEnabled() ? View.VISIBLE : View.GONE);
        btnCheck.setVisibility(getApp().isCheckEnabled() ? View.VISIBLE : View.GONE);

    }

    private boolean tryProceed(PaymentMethod method) {
        BigDecimal receivedAmount = getDecimalValue();
        BigDecimal remainingAmount = orderTotal.subtract(completedAmount);
        BigDecimal remainingEbtAmount = orderEbtTotal.subtract(completedEbtAmount);

        if (receivedAmount.equals(BigDecimal.ZERO)) {
            charge.selectAll();
            AlertDialogFragment.showAlert(getActivity(), R.string.pay_tender_wrong_amount_title, getString(R.string.pay_tender_zero_amount_error_message));
            return false;
        }

        if (method != PaymentMethod.CASH && receivedAmount.compareTo(remainingAmount) > 0) {
            charge.setText(UiHelper.valueOf(remainingAmount));
            charge.selectAll();
            AlertDialogFragment.showAlert(getActivity(), R.string.pay_tender_wrong_amount_title, getString(R.string.pay_tender_wrong_amount_error_message));
            return false;
        }

        if (method == PaymentMethod.PAX_EBT_FOODSTAMP || method == PaymentMethod.PAX_EBT_CASH){
            if (remainingEbtAmount.compareTo(receivedAmount) < 0){
                receivedAmount = remainingEbtAmount;
                charge.setText(valueOf(receivedAmount));
                charge.selectAll();
            }
        }

        if (listener != null){
            listener.onUnitedPaymentAmountSelected(method, orderTotal, receivedAmount);
            return true;
        }

        Toast.makeText(getActivity(), R.string.pay_toast_zero, Toast.LENGTH_LONG).show();
        return false;
    }

    protected BigDecimal getDecimalValue() {
        try {
            return UiHelper.parseBrandDecimalInput(charge.getText().toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private class ChargeTextWatcher extends CurrencyTextWatcher {

        public ChargeTextWatcher(TextView view) {
            super(view);
        }

        @Override
        public synchronized void afterTextChanged(Editable amount) {
            super.afterTextChanged(amount);
            final String value = charge.getText().toString();
            BigDecimal entered;
            try {
                entered = UiHelper.parseBrandDecimalInput(value);
            } catch (NumberFormatException e) {
                entered = BigDecimal.ZERO;
                Logger.e("Number format mis parsing", e);
            }

            orderRemaining = orderTotal.subtract(completedAmount);
            Log.d("BemaCarl25", "PayTenderUnitedFragmentDialog.afterTextChanged.orderTotal:           1:" + orderTotal);
            Log.d("BemaCarl25", "PayTenderUnitedFragmentDialog.afterTextChanged.completedAmount:      1:" + completedAmount);
            Log.d("BemaCarl25", "PayTenderUnitedFragmentDialog.afterTextChanged.orderRemaining:       1:" + orderRemaining);

            BigDecimal change = BigDecimal.ZERO;
            if(entered.compareTo(orderRemaining) == 1) {
                change = entered.subtract(orderRemaining);
            }

            if (getDisplayBinder() != null) {
                Log.d("BemaCarl25", "PayTenderUnitedFragmentDialog.afterTextChanged.entered:                " + entered);
                Log.d("BemaCarl25", "PayTenderUnitedFragmentDialog.afterTextChanged.change:                 " + change);
                getDisplayBinder().startCommand(new DisplayTenderCommand(entered, change));
            }

        }
    }

    private DisplayService.IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof DisplayService.IDisplayBinder) {
            return (DisplayService.IDisplayBinder) getActivity();
        }
        return null;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return false;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.pay_tender_dialog_width_large),
                getResources().getDimensionPixelOffset(R.dimen.pay_tender_dialog_height_3_large)
        );
    }

    @Override
    protected void calculateDlgHeight() {
        boolean expand = saleOrderModels != null && saleOrderModels.size() > 0;

        int height = expand ? R.dimen.pay_tender_dialog_height_3_expanded_large : R.dimen.pay_tender_dialog_height_3_large;

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.pay_tender_dialog_width_large),
                getResources().getDimensionPixelOffset(height)
        );
    }

    @Override
    protected boolean isRefund() {
        return false;
    }

    @Override
    protected void onCreateViews() {
        super.onCreateViews();

        checkboxSingle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onSingleTenderCheck(isChecked);
            }
        });
        checkboxSingle.setEnabled(orderType != OrderType.PREPAID);

        getNegativeButton().setVisibility(View.GONE);
        if (getApp().isPaxConfigured()) {
            PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
            if (!paxGateway.acceptPaxCreditEnabled()) {
                btnCard.setVisibility(View.GONE);
            } else {
                btnCard.setVisibility(View.VISIBLE);
            }
            if (paxGateway.acceptPaxDebitEnabled()) {
                btnPaxDebit.setVisibility(View.VISIBLE);
            }
            if (paxGateway.acceptPaxEbtEnabled()) {
                btnPaxEbtCash.setVisibility(View.VISIBLE);
            }
        }

        keyboard.attachEditView(charge);
        setChargeView();

    }

    @Override
    public void onLoadComplete() {
        super.onLoadComplete();
        setSingleTenderCheckbox();
    }

    private void setSingleTenderCheckbox() {
        boolean on = (singleTenderEnabled == null || singleTenderEnabled);
        checkboxSingle.setChecked(on);
        checkboxSingle.setVisibility(!hasCompletedTransactions() || on ? View.VISIBLE : View.GONE);

        checkboxSingle.setVisibility(View.GONE);
        checkboxSingle.setChecked(false);
    }

    protected boolean hasCompletedTransactions() {
        return BigDecimal.ZERO.compareTo(completedAmount) < 0;
    }

    @Click
    protected void btnPaxDebitClicked() {
        tryProceed(PaymentMethod.PAX_DEBIT);
    }

    @Click
    protected void btnGiftCardClicked() {
        tryProceed(PaymentMethod.GIFT_CARD);
    }

    @Click
    protected void btnPaxEbtCashClicked() {
        EBTPaymentTypeChooserDialogFragment.show(getActivity(), new EBTPaymentTypeChooserDialogFragment.EBTTypeChooseListener() {
            @Override
            public void onEBTCashTypeChosen() {
                tryProceed(PaymentMethod.PAX_EBT_CASH);
                EBTPaymentTypeChooserDialogFragment.hide(getActivity());
            }

            @Override
            public void onEBTFoodStampTypeChosen() {
                tryProceed(PaymentMethod.PAX_EBT_FOODSTAMP);
                EBTPaymentTypeChooserDialogFragment.hide(getActivity());
            }
        });
    }

    @Click
    protected void btnCashClicked() {
        tryProceed(PaymentMethod.CASH);
    }

    @Click
    protected void btnCardClicked() {
        tryProceed(PaymentMethod.CREDIT_CARD);
    }

    @Click
    protected void btnCreditReceiptClicked() {
        tryProceed(PaymentMethod.CREDIT_RECEIPT);
    }

    @Click
    protected void btnOfflineCreditClicked() {
        tryProceed(PaymentMethod.OFFLINE_CREDIT);
    }

    @Click
    protected void btnCheckClicked() {
        tryProceed(PaymentMethod.CHECK);
    }


    @Override
    protected void updateAfterCalculated() {
        BigDecimal remainingTotal = orderTotal.subtract(completedAmount);
        BigDecimal remainingEbt = orderEbtTotal.subtract(completedEbtAmount);

        showPrice(this.difference, remainingTotal);
        showPrice(this.remainingEbt, remainingEbt);
        showPrice(this.charge, getApp().isAutoFillPaymentAmount() ? orderTotal.subtract(completedAmount) : null);
        charge.selectAll();

        if (hasCompletedTransactions() || BigDecimal.ZERO.compareTo(orderTotal) == 0) {
            listener.onDataLoaded(completedAmount, orderTotal, saleOrderModels);
        }

        enable(true);
        getPositiveButton().setText(hasCompletedTransactions() ? R.string.btn_void : R.string.btn_cancel);
    }

    protected void enable(final boolean on) {
        super.enable(on);
        setTenderButtonsVisibilityWithServerSettings();
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_tender_united_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_tender_title;
    }


    @Override
    protected int getPositiveButtonTitle() {
        return hasCompletedTransactions() ? R.string.btn_void : R.string.btn_cancel;
    }


    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    if (hasCompletedTransactions()) {
                        boolean voidSalesPermitted = getApp().hasPermission(Permission.VOID_SALES);
                        if (!voidSalesPermitted) {
                            PermissionFragment.showCancelable(getActivity(), new SuperBaseActivity.BaseTempLoginListener(getActivity()), Permission.VOID_SALES);
                            return false;
                        }
                        Logger.d("Requesting a void for %d items", saleOrderModels.size());
                        listener.onVoidRequested(saleOrderModels);
                    } else {
                        listener.onCancel();
                    }
                }
                return false;
            }
        };
    }

    public static void show(FragmentActivity context,
                            String orderGuid,
                            OrderType orderType,
                            IPaymentDialogListener.IPayTenderUnitedListener listener,
                            int customAnimationResource,
                            Boolean singleTenderEnabled) {
        DialogUtil.show(context, DIALOG_NAME, PayTenderUnitedFragmentDialog_.builder().singleTenderEnabled(singleTenderEnabled).build())
                .setListener(listener)
                .setOrderGuid(orderGuid, orderType)
                .setCustomAnimationResource(customAnimationResource);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
