package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TenderFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.payment.IPaymentDialogListener.IRefundListener;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.PaymentMethod;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class RefundTenderFragmentDialog extends TenderFragmentDialogBase<RefundTenderFragmentDialog, IRefundListener> {

    private static final String DIALOG_NAME = "RefundTenderFragmentDialog";

    private static final Handler handler = new Handler();

    private BigDecimal amountToRefund;

    private boolean allowImmediateCancel;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        checkboxSingle.setVisibility(View.GONE);
    }

    @Override
    protected boolean isRefund() {
        return true;
    }

    public RefundTenderFragmentDialog setAllowImmediateCancel(boolean allowImmediateCancel) {
        this.allowImmediateCancel = allowImmediateCancel;
        return this;
    }

    @Click
    protected void btnCashClicked() {
        listener.onRefundMethodSelected(PaymentMethod.CASH, saleOrderModels);
    }

    @Click
    protected void btnPaxEbtFoodstamp() {
        listener.onRefundMethodSelected(PaymentMethod.PAX_EBT_FOODSTAMP, saleOrderModels);
    }

    @Click
    protected void btnPaxDebitClicked() {
        listener.onRefundMethodSelected(PaymentMethod.PAX_DEBIT, saleOrderModels);
    }

    @Click
    protected void btnCardClicked() {
        listener.onRefundMethodSelected(PaymentMethod.CREDIT_CARD, saleOrderModels);
    }

    @Click
    protected void btnCreditReceiptClicked() {
        listener.onRefundMethodSelected(PaymentMethod.CREDIT_RECEIPT, saleOrderModels);
    }

    @Click
    protected void btnOfflineCreditClicked() {
        listener.onRefundMethodSelected(PaymentMethod.OFFLINE_CREDIT, saleOrderModels);
    }

    @Override
    public void onLoadComplete(List<PaymentTransactionModel> loaded) {
        if (loaded == null || loaded.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.error_message_no_payments), Toast.LENGTH_SHORT).show();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onCancel();
                }
            });
            return;
        }

        super.onLoadComplete(loaded);
    }


    @Override
    protected void updateAfterCalculated() {
        boolean displayCCBtn = false;
        boolean displayCashBtn = false;
        boolean displayDebitBtn = false;
        boolean displayEbtBtn = false;
        boolean displayOfflineCreditBtn = false;
        PaxGateway paxGateway = (PaxGateway)PaymentGateway.PAX.gateway();
        final boolean paxReady = getApp().isPaxConfigured();
        if (saleOrderModels != null) {
            for (PaymentTransactionModel item : saleOrderModels) {
                displayCCBtn |= ( PaymentGateway.PAX.equals(item.gateway) && paxReady && paxGateway.acceptPaxCreditEnabled())
                            ||  ( PaymentGateway.BLACKSTONE.equals(item.gateway) && !paxReady );
                displayCashBtn |= PaymentGateway.CASH.equals(item.gateway);
                displayOfflineCreditBtn |= PaymentGateway.OFFLINE_CREDIT.equals(item.gateway);
                if (paxReady) {
                    if (paxGateway.acceptPaxDebitEnabled()) {
                        displayDebitBtn = PaymentGateway.PAX_DEBIT.equals(item.gateway);
                    }
                    if (paxGateway.acceptPaxEbtEnabled()) {
                        displayEbtBtn |= PaymentGateway.PAX_EBT_FOODSTAMP.equals(item.gateway);
                    }
                }
            }
        }
        if (displayDebitBtn || displayEbtBtn) {
            btnCard.setVisibility(View.GONE);
            btnPaxDebit.setEnabled(displayDebitBtn);
//            btnPaxEbtFoodstamp.setEnabled(displayEbtBtn);
            btnPaxDebit.setVisibility(displayDebitBtn ? View.VISIBLE : View.GONE);
//            btnPaxEbtFoodstamp.setVisibility(displayEbtBtn ? View.VISIBLE : View.GONE);
        } else {
            //btnCard.setEnabled(displayCCBtn);
        }
//        btnCash.setEnabled(displayCashBtn);
        btnCash.setEnabled(true); // TODO this may vary in next sprint - when this flow melts because of paychecks and prepaid stuff
        btnOfflineCredit.setEnabled(displayOfflineCreditBtn);
    }

    @Override
    protected void enable(boolean on) {
        on = !hasCompletedTransactions();
        getPositiveButton().setEnabled(on);
        getPositiveButton().setTextColor(on ? colorPaymentOk : colorPaymentDisabled);
    }

    @Override
    protected boolean hasCompletedTransactions() {
        return orderTotal.compareTo(amountToRefund) != 0 || !allowImmediateCancel;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.refund_tender_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_tender_title;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_cancel;
    }

    public RefundTenderFragmentDialog setAmountToRefund(BigDecimal amountToRefund) {
        this.amountToRefund = amountToRefund;
        return this;
    }


    public RefundTenderFragmentDialog setTransactions(ArrayList<PaymentTransactionModel> transactions) {
        injectFakeTransactions(transactions);
        return this;
    }

    public static void show(FragmentActivity context,
                            String orderGuid,
                            OrderType orderType,
                            BigDecimal orderTotal,
                            BigDecimal amountToRefund,
                            ArrayList<PaymentTransactionModel> transactions,
                            IRefundListener listener,
                            int customAnimationResource,
                            final boolean allowImmediateCancel) {

        DialogUtil.show(context, DIALOG_NAME, RefundTenderFragmentDialog_.builder().build())
                .setAllowImmediateCancel(allowImmediateCancel)
                .setAmountToRefund(amountToRefund)
                .setOrderTotal(orderTotal)
                .setListener(listener)
                .setOrderGuid(orderGuid, orderType)
                .setTransactions(transactions)
                .setCustomAnimationResource(customAnimationResource);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    protected void calcTotal(BigDecimal totalOrderPrice, BigDecimal totalOrderEbtPrice) {

        showPrice(this.total, orderTotal);
        showPrice(this.difference, amountToRefund);
        if (orderTotal != null) {
            updateAfterCalculated();
        }
    }
}
