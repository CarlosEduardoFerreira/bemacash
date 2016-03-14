package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PayChargeFragmentDialog extends KeyboardDialogFragment {

    private static final String DIALOG_NAME = "payChargeFragmentDialog";

    protected CurrencyTextWatcher currencyTextWatcher;

    @ViewById
    protected CustomEditBox charge;

    @ViewById
    protected TextView pending;

    @ViewById
    protected TextView total;

    @ViewById
    protected TextView totalCostTotal;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;

    protected BigDecimal totalValue;
    protected BigDecimal pendingValue;

    protected ISaleChargeListener listener;

    @Override
    protected int getPreferredContentWidth() {
        return R.dimen.pay_charge_dialog_width;
    }


    @AfterViews
    protected void attachViews() {
        keyboard.attachEditView(charge);
        setChargeView();
    }

    private void setChargeView() {
        enablePositiveButtons(true);
        currencyTextWatcher = new CurrencyTextWatcher(charge);
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.addTextChangedListener(currencyTextWatcher);
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return tryProceed();
            }
        });
        charge.addTextChangedListener(new ChargeTextWatcher(charge));
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
                    charge.setText(UiHelper.valueOf(entered).toString());
                }
            } catch (NumberFormatException e) {
                entered = BigDecimal.ZERO;
                Logger.e("Number format mis parsing", e);
            }

            if (getDisplayBinder() != null) {
                getDisplayBinder().startCommand(new DisplayTenderCommand(entered, null));
            }

            enablePositiveButtons(entered.compareTo(PaymentGateway.getCreditCardPaymentMethod().minimalAmount()) >= 0);
            BigDecimal pending = pendingValue.subtract(entered);
            PayChargeFragmentDialog.this.pending.setText(UiHelper.valueOf(pending));
            PayChargeFragmentDialog.this.pending.setTextColor(pending.compareTo(BigDecimal.ZERO) > 0 ? colorPaymentPending : colorPaymentOk);
        }
    }


    private IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof IDisplayBinder) {
            return (IDisplayBinder) getActivity();
        }
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        charge.requestFocus();
        initLabels();
        setCancelable(false);
    }

    private void initLabels() {
        final String amount;
        total.setText(amount = UiHelper.valueOf(pendingValue).toString());
        charge.setText(amount);
        pending.setText(UiHelper.valueOf(BigDecimal.ZERO));
        BigDecimal value = totalValue.subtract(pendingValue);
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            totalCostTotal.setText(UiHelper.valueOf(totalValue));
        } else {
            totalCostTotal.setVisibility(View.GONE);
        }
    }

    public PayChargeFragmentDialog setTotal(BigDecimal total) {
        this.totalValue = total;
        return this;
    }

    public PayChargeFragmentDialog setPending(BigDecimal pending) {
        this.pendingValue = pending;
        return this;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_charge_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_charge_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_accept;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                tryProceed();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                tryCancel();
                return false;
            }
        };
    }

    public PayChargeFragmentDialog setListener(ISaleChargeListener listener) {
        this.listener = listener;
        return this;
    }

    private boolean tryCancel() {
        if (listener != null) {
            listener.onCancel();
            return true;
        }
        return false;
    }

    private boolean tryProceed() {
        if (listener != null && String.valueOf(charge.getText()).length() > 0) {
            listener.onPaymentAmountSelected(getDecimalValue());
            return true;
        }
        Toast.makeText(getActivity(), R.string.pay_toast_zero, Toast.LENGTH_LONG).show();
        return false;
    }

    protected BigDecimal getDecimalValue() {
        try {
            return new BigDecimal(charge.getText().toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public interface ISaleChargeListener {

        void onPaymentAmountSelected(BigDecimal amount);

        void onCancel();

    }

    public static void show(FragmentActivity context, BigDecimal total, BigDecimal pending, ISaleChargeListener listener) {
        Logger.d("About to show second dialog");
        DialogUtil.show(context, DIALOG_NAME, PayChargeFragmentDialog_.builder().build()).setListener(listener).setTotal(total).setPending(pending);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
