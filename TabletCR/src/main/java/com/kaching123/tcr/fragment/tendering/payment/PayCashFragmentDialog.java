package com.kaching123.tcr.fragment.tendering.payment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.WaitForCashInDrawerCommand;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.cash.CashSaleCommand;
import com.kaching123.tcr.component.CashAdjustableNumpadView;
import com.kaching123.tcr.component.CashAdjustableNumpadView.IExactClickListener;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener.IDrawerFriend;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.util.AnimationUtils;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.priceFormat;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PayCashFragmentDialog extends StyledDialogFragment implements CustomEditBox.IKeyboardSupport, IDrawerFriend {

    private static final String DIALOG_NAME = "PayCashFragmentDialog";

    @ViewById
    protected CustomEditBox charge;

    @ViewById
    protected TextView pending;

    @ViewById
    protected ViewFlipper flipper;

    @ViewById
    protected TextView total;

    @ViewById
    protected CashAdjustableNumpadView containerHolder;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;

    protected Transaction transaction;

    protected ISaleCashListener listener;
    private TaskHandler waitCashTask;

    @Override
    public void onStop() {
        super.onStop();
        cancelWaitCashTask();
    }

    public void cancelWaitCashTask() {
        if (waitCashTask != null) {
            waitCashTask.cancel(getActivity(), 0, null);
            waitCashTask = null;
        }
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        containerHolder.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        containerHolder.detachEditView();
    }

    @AfterViews
    protected void attachViews() {
        containerHolder.attachEditView(charge);
        containerHolder.setExactClickListener(new IExactClickListener() {
            @Override
            public void onExactClicked() {
                getPositiveButtonListener().onClick();
            }
        });
        setChargeView();
        AnimationUtils.applyFlippingEffect(getActivity(), flipper);
    }

    private void setChargeView() {
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.addTextChangedListener(new CurrencyTextWatcher(charge));
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                return try2GetCash(false);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.pay_cash_dialog_width),
                getResources().getDimensionPixelOffset(R.dimen.pay_cash_dialog_height));
//        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        initLabels();
        turnPositiveButton(false);
        setCancelable(false);
    }

    private void initLabels() {
        charge.requestFocus();
        total.setText(UiHelper.valueOf(transaction.getAmount()));
        pending.setText(UiHelper.valueOf(BigDecimal.ZERO));
        getPositiveButton().setTypeface(null, Typeface.BOLD);
        containerHolder.exactValue = transaction.getAmount();
    }

    @AfterTextChange
    protected void chargeAfterTextChanged(Editable text) {
        if (text == null) {
            turnPositiveButton(false);
            return;
        }
        BigDecimal tenderAmount = BigDecimal.ZERO;
        BigDecimal changeAmount = BigDecimal.ZERO;
        String chargeStr = charge.getText().toString();
        try {
            tenderAmount = UiHelper.parseBrandDecimalInput(chargeStr);
            resumeNavigationButtons(tenderAmount);
        } catch (NumberFormatException e) {
            turnPositiveButton(false);
        }
        if (getDisplayBinder() != null) {
            getDisplayBinder().startCommand(new DisplayTenderCommand(tenderAmount, changeAmount));
        }
    }

    private void resumeNavigationButtons(final BigDecimal receivedAmount) {
        BigDecimal changeAmount = receivedAmount.subtract(transaction.getAmount());
        if (changeAmount.compareTo(BigDecimal.ZERO) >= 0) {
            PayCashFragmentDialog.this.pending.setText(priceFormat(changeAmount));
            transaction.changeValue = changeAmount;
            turnPositiveButton(true);
        } else {
            turnPositiveButton(false);
            PayCashFragmentDialog.this.pending.setText(priceFormat(BigDecimal.ZERO));
        }
    }

    private IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof IDisplayBinder) {
            return (IDisplayBinder) getActivity();
        }
        return null;
    }

    public PayCashFragmentDialog setTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_cash_fragment;
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
    protected boolean hasPositiveButton() {
        return true;
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
                return try2GetCash(false);
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

    public PayCashFragmentDialog setListener(ISaleCashListener listener) {
        this.listener = listener;
        return this;
    }

    private boolean tryCancel() {
        if (listener != null) {
            listener.onCancel();
            cancelWaitCashTask();
            return true;
        }
        return false;
    }

    @Override
    public boolean try2GetCash(boolean searchByMac) {
        Logger.d("PayCashFragmentDialog: try2GetCash()");
        if (TextUtils.isEmpty(charge.getText())) {
            Toast.makeText(getActivity(), R.string.pay_toast_zero, Toast.LENGTH_LONG).show();
            return false;
        }
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_open_drawer));
        setButtonsEnabled(false);
        waitCashTask = WaitForCashInDrawerCommand.start(getActivity(), searchByMac, new OpenDrawerListener(this));
        return false;
    }

    @Override
    public TaskHandler getHandler() {
        return waitCashTask;
    }

    @Override
    public void setHandler(TaskHandler handler) {
        waitCashTask = handler;
    }

    private void flip() {
        flipper.showNext();
    }

    @Override
    public void onDrawerOpened() {
        Logger.d("PayCashFragmentDialog: onDrawerOpened()");
        WaitDialogFragment.hide(getActivity());
        //TODO change image ???
        flipper.setDisplayedChild(1);
    }

    @Override
    public void onFailure() {
        Logger.d("PayCashFragmentDialog: onFailure()");
        WaitDialogFragment.hide(getActivity());
    }

    @Override
    public void onPopupCancelled() {
        tryCancel();
    }

    @Override
    public void onCashReceived() {
        Logger.d("PayCashFragmentDialog: onCashReceived()");
        WaitDialogFragment.hide(getActivity());
        PaymentGateway.CASH.gateway().sale(getActivity(), this, null, null, transaction);
    }

    @OnFailure(CashSaleCommand.class)
    public void onTransactionFailure() {
        Logger.d("OnFail AddTransactionCommand received signal");
    }

    @OnSuccess(CashSaleCommand.class)
    public void onTransactionSuccess() {
        getPositiveButton().setEnabled(true);
        listener.onPaymentAmountSelected(transaction.getAmount(), transaction.getChangeAmount());
        Logger.d("OnSuccess AddTransactionCommand received signal");
    }

    private void setButtonsEnabled(boolean on) {
        turnPositiveButton(on);
        turnNegativeButton(on);
    }

    private void turnPositiveButton(boolean on) {
        containerHolder.setEnterEnabled(on);

        getPositiveButton().setEnabled(on);
        getPositiveButton().setTextColor(on ? colorPaymentOk : colorPaymentDisabled);

        BigDecimal pendingAmount = UiHelper.parseBigDecimal(pending, BigDecimal.ZERO);
        pending.setTextColor(pendingAmount.compareTo(BigDecimal.ZERO) == 1 ? colorPaymentOk : colorPaymentDisabled);
    }

    private void turnNegativeButton(boolean on) {
        getNegativeButton().setTextColor(on ? colorPaymentOk : colorPaymentDisabled);
        getNegativeButton().setEnabled(on);
    }

    public static interface ISaleCashListener {

        void onPaymentAmountSelected(BigDecimal amount, BigDecimal changeAmount);

        void onCancel();

    }

    public static PayCashFragmentDialog show(FragmentActivity context, Transaction transaction, ISaleCashListener listener) {
        Logger.d("About to show second dialog");
        return DialogUtil.show(context, DIALOG_NAME, PayCashFragmentDialog_.builder().build()).setListener(listener).setTransaction(transaction);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
