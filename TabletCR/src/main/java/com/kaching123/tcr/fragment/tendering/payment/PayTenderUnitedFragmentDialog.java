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
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.adapter.BondItemAdapter;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.UiHelper;
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

/**
 * Created by alboyko on 03.08.2016.
 */
@EFragment
public class PayTenderUnitedFragmentDialog extends TenderFragmentDialogBase<PayTenderUnitedFragmentDialog, IPaymentDialogListener.IPayTenderUnitedListener> {
    private static final String DIALOG_NAME = "paymenttenderunitedDialog";

    protected CurrencyTextWatcher currencyTextWatcher;

    private static final List<Integer> BONDS_LIST = new ArrayList<>();

    public BigDecimal exactValue;

    static {
        BONDS_LIST.add(1);
        //BONDS_LIST.add(new Integer(2));
        BONDS_LIST.add(5);
        BONDS_LIST.add(10);
        BONDS_LIST.add(20);
        BONDS_LIST.add(50);
        BONDS_LIST.add(BondItemAdapter.ARG_EXACT_BUTTON_POSITION);
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
    protected TextView pending;

    @ViewById
    protected TextView total_;

    @ViewById
    protected TextView totalCostTotal;

    protected BigDecimal totalValue;
    protected BigDecimal pendingValue;

    @ViewById
    protected KeyboardView keyboard;

    @ViewById
    protected  Button btnAccept;

    @ViewById
    protected  Button btnNewCash;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;


    //@AfterViews
    protected void attachViews() {
        keyboard.attachEditView(charge);

        setChargeView();
        String totalTxt = total.getText().toString();
        String differenceTxt = difference.getText().toString();

        totalValue = new BigDecimal(30);
        pendingValue = new BigDecimal(30);


        final String amount = UiHelper.valueOf(pendingValue);
        total_.setText(amount);
        charge.setText(amount);
        difference.setText(amount);
        pending.setText(UiHelper.valueOf(BigDecimal.ZERO));
        BigDecimal value = totalValue.subtract(pendingValue);
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            totalCostTotal.setText(UiHelper.valueOf(totalValue));
        } else {
            totalCostTotal.setVisibility(View.GONE);
        }
    }

    private void setChargeView() {
        currencyTextWatcher = new CurrencyTextWatcher(charge);
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
                int value = (int) id;
                if(value == BondItemAdapter.ARG_EXACT_BUTTON_POSITION){
                    BigDecimal alreadyPayed = orderTotal.subtract(completedAmount);
                    charge.setText(UiHelper.valueOf(alreadyPayed));
                } else {
                    charge.setText(String.valueOf(value));
                }
            }
        });

    }

    @Click
    protected void btnExactClicked(){
        BigDecimal alreadyPayed = orderTotal.subtract(completedAmount);
        charge.setText(UiHelper.valueOf(alreadyPayed));
    }

    private boolean tryProceed(PaymentMethod method) {
        if (listener != null && String.valueOf(charge.getText()).length() > 0) {
            listener.onUnitedPaymentAmountSelected(method, orderTotal, getDecimalValue());
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
            BigDecimal entered = BigDecimal.ZERO;
            try {
                entered = UiHelper.parseBrandDecimalInput(value);
                if (value.length() > 0 && pendingValue.compareTo(entered) < 0) {
                    entered = pendingValue;
                    isEditMode = true;
                    charge.setText(UiHelper.valueOf(entered));
                }
            } catch (NumberFormatException e) {
                entered = BigDecimal.ZERO;
                Logger.e("Number format mis parsing", e);
            }

            if (getDisplayBinder() != null) {
                getDisplayBinder().startCommand(new DisplayTenderCommand(entered, null));
            }

            enablePositiveButtons(entered.compareTo(PaymentGateway.getCreditCardPaymentMethod().minimalAmount()) >= 0);
            BigDecimal pending1 = pendingValue.subtract(entered);
            pending.setText(UiHelper.valueOf(pending1));
            pending.setTextColor(pending1.compareTo(BigDecimal.ZERO) > 0 ? colorPaymentPending : colorPaymentOk);
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
    }

    @Override
    protected void calculateDlgHeight(){
        boolean expand = saleOrderModels != null && saleOrderModels.size() > 0;

        int height = expand ? R.dimen.pay_tender_dialog_height_3_expanded : R.dimen.pay_tender_dialog_height_3;

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.pay_tender_dialog_width),
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
            PaxGateway paxGateway = (PaxGateway)PaymentGateway.PAX.gateway();
            if (!paxGateway.acceptPaxCreditEnabled()) {
                btnCard.setVisibility(View.GONE);
            } else {
                btnCard.setVisibility(View.VISIBLE);
                btnCard.setEnabled(true);
            }
            if (paxGateway.acceptPaxDebitEnabled()) {
                btnPaxDebit.setVisibility(View.VISIBLE);
            }
            if (paxGateway.acceptPaxEbtEnabled()) {
                btnPaxEbtCash.setVisibility(View.VISIBLE);
                btnPaxEbtFoodstamp.setVisibility(View.VISIBLE);
            }
        }

        attachViews();
    }

    @Override
    public void onLoadComplete() {
        super.onLoadComplete();
        setSingleTenderCheckbox();
    }

    private void setSingleTenderCheckbox() {
        boolean on =  (singleTenderEnabled == null || singleTenderEnabled.booleanValue());
        checkboxSingle.setChecked(on);
        checkboxSingle.setVisibility(!hasCompletedTransactions() || on ? View.VISIBLE : View.GONE);

        checkboxSingle.setVisibility(View.GONE);
        checkboxSingle.setChecked(false);
    }

    protected boolean hasCompletedTransactions() {
        return BigDecimal.ZERO.compareTo(completedAmount) < 0;
    }

    @Click
    protected void btnAcceptClicked(){
        Log.d("TAG", "btnAcceptClicked" );
        tryProceed(PaymentMethod.OFFLINE_CREDIT);
    }

    @Click
    protected void btnNewCashClicked(){
        Log.d("TAG", "btnAcceptClicked" );
        tryProceed(PaymentMethod.CASH);
    }

        @Click
    protected void btnPaxDebitClicked(){
             tryProceed(PaymentMethod.PAX_DEBIT);
    }

    @Click
    protected void btnPaxEbtFoodstampClicked(){
        tryProceed(PaymentMethod.PAX_EBT_FOODSTAMP);
    }

    @Click
    protected void btnPaxEbtCashClicked(){
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
    protected void btnCashClicked(){
        tryProceed(PaymentMethod.CASH);
    }

    @Click
    protected void btnCardClicked(){
        tryProceed(PaymentMethod.CREDIT_CARD);
    }

    @Click
    protected void btnCreditReceiptClicked(){
        tryProceed(PaymentMethod.CREDIT_RECEIPT);
    }

    @Click
    protected void btnOfflineCreditClicked(){
        tryProceed(PaymentMethod.OFFLINE_CREDIT);
    }

    @Click
    protected void btnCheckClicked(){
        tryProceed(PaymentMethod.CHECK);
    }


    @Override
    protected void updateAfterCalculated() {
        if (hasCompletedTransactions() || BigDecimal.ZERO.compareTo(orderTotal) == 0) {
            BigDecimal alreadyPayed = orderTotal.subtract(completedAmount);
            listener.onDataLoaded(completedAmount, orderTotal, saleOrderModels);
            difference.setText(UiHelper.valueOf(alreadyPayed));
            total_.setText(UiHelper.valueOf(alreadyPayed));
            charge.setText(UiHelper.valueOf(alreadyPayed));

        } /*else {
            difference.setVisibility(View.GONE);
            dots.setVisibility(View.GONE);
            a2.setVisibility(View.GONE);
        }*/
        enable(true);
        getPositiveButton().setText(hasCompletedTransactions() ? R.string.btn_void : R.string.btn_cancel);
    }

    protected void enable(final boolean on) {
        super.enable(on);
        btnOfflineCredit.setEnabled(on);
        btnCheck.setEnabled(on);
        btnPaxEbtCash.setEnabled(true);
        btnPaxEbtCash.setVisibility(View.VISIBLE);
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
