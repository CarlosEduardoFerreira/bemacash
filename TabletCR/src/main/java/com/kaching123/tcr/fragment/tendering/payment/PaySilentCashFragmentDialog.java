package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.WaitForCashInDrawerCommand;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.cash.CashSaleCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.DisplayService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import org.androidannotations.annotations.EFragment;

import java.math.BigDecimal;

/**
 * Created by alboyko on 17.08.2016.
 */
@EFragment
public class PaySilentCashFragmentDialog  extends StyledDialogFragment implements /*CustomEditBox.IKeyboardSupport,*/ OpenDrawerListener.IDrawerFriend {

    private static final String DIALOG_NAME = "PaySilentCashFragmentDialog";

   /* @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;*/

    protected Transaction transaction;

    protected ISaleCashListener listener;
    private TaskHandler waitCashTask;
    private BigDecimal pending = BigDecimal.ZERO;

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
/*
    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
    }
*/
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.pay_cash_dialog_width),
                getResources().getDimensionPixelOffset(R.dimen.pay_cash_dialog_height));
        init();
        try2GetCash(false);
        setCancelable(false);
    }


    private void init() {
        BigDecimal tenderAmount = transaction.amount;
        BigDecimal changeAmount = transaction.changeValue;
        if (getDisplayBinder() != null) {
            getDisplayBinder().startCommand(new DisplayTenderCommand(tenderAmount, changeAmount));
        }
    }

    private DisplayService.IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof DisplayService.IDisplayBinder) {
            return (DisplayService.IDisplayBinder) getActivity();
        }
        return null;
    }

    public PaySilentCashFragmentDialog setTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_cash_silent_fragment;
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
        return false;
    }
    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_accept;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public PaySilentCashFragmentDialog setListener(ISaleCashListener listener) {
        this.listener = listener;
        return this;
    }

    public PaySilentCashFragmentDialog setOrderPending(BigDecimal amount) {
        this.pending = amount;
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
        Logger.d("PaySilentCashFragmentDialog: try2GetCash()");
        String s = UiHelper.valueOf(transaction.amount);
        if (TextUtils.isEmpty(s)) {
            Toast.makeText(getActivity(), R.string.pay_toast_zero, Toast.LENGTH_LONG).show();
            return false;
        }
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_open_drawer));
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

    @Override
    public void onDrawerOpened() {
        Logger.d("PaySilentCashFragmentDialog: onDrawerOpened()");
        WaitDialogFragment.hide(getActivity());
    }

    @Override
    public void onFailure() {
        Logger.d("PaySilentCashFragmentDialog: onFailure()");
        WaitDialogFragment.hide(getActivity());
    }

    @Override
    public void onPopupCancelled() {
        tryCancel();
    }

    @Override
    public void onCashReceived() {
        Logger.d("PaySilentCashFragmentDialog: onCashReceived()");
        WaitDialogFragment.hide(getActivity());
        PaymentGateway.CASH.gateway().sale(getActivity(), this, null, null, transaction);
    }

    @OnFailure(CashSaleCommand.class)
    public void onTransactionFailure() {
        Logger.d("OnFail AddTransactionCommand received signal");
    }

    @OnSuccess(CashSaleCommand.class)
    public void onTransactionSuccess() {
        listener.onPaymentAmountSelected(transaction.getAmount(), transaction.getChangeAmount());
        Logger.d("OnSuccess AddTransactionCommand received signal");
    }

    public interface ISaleCashListener {
        void onPaymentAmountSelected(BigDecimal amount, BigDecimal changeAmount);
        void onCancel();
    }

    public static PaySilentCashFragmentDialog show(FragmentActivity context, BigDecimal pending, Transaction transaction, ISaleCashListener listener) {
        Logger.d("About to show second dialog");
        return DialogUtil.show(context, DIALOG_NAME, PaySilentCashFragmentDialog_.builder().build()).setListener(listener)
                .setOrderPending(pending)
                                .setTransaction(transaction);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
