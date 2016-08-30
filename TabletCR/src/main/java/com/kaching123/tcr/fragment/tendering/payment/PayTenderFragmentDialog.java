package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TenderFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.payment.IPaymentDialogListener.IPayTenderListener;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.payment.PaymentMethod;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class represents all void and refund possible situations
 */
@EFragment
public class PayTenderFragmentDialog extends TenderFragmentDialogBase<PayTenderFragmentDialog, IPayTenderListener> {

    private static final String DIALOG_NAME = "paymenttenderDialog";

    @ViewById protected Button btnCheck;

    @FragmentArg
    protected Boolean singleTenderEnabled;

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
            }
            if (paxGateway.acceptPaxDebitEnabled()) {
                btnPaxDebit.setVisibility(View.VISIBLE);
            }
            if (paxGateway.acceptPaxEbtEnabled()) {
                btnPaxEbtCash.setVisibility(View.VISIBLE);
            }
        }
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
    }

    protected boolean hasCompletedTransactions() {
        return BigDecimal.ZERO.compareTo(completedAmount) < 0;
    }

    @Click
    protected void btnPaxDebitClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.PAX_DEBIT,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnPaxEbtFoodstampClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.PAX_EBT_FOODSTAMP,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnPaxEbtCashClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.PAX_EBT_CASH,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnCashClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.CASH,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnCardClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.CREDIT_CARD,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnCreditReceiptClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.CREDIT_RECEIPT,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnOfflineCreditClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.OFFLINE_CREDIT,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }

    @Click
    protected void btnCheckClicked(){
        listener.onPaymentMethodSelected(PaymentMethod.CHECK,
                orderTotal,
                orderTotal.subtract(completedAmount),
                checkboxSingle.isChecked());
    }


    @Override
    protected void updateAfterCalculated() {
        if (hasCompletedTransactions() || BigDecimal.ZERO.compareTo(orderTotal) == 0) {
            BigDecimal alreadyPayed = orderTotal.subtract(completedAmount);
            listener.onDataLoaded(completedAmount, orderTotal, saleOrderModels);
            difference.setText(UiHelper.valueOf(alreadyPayed));
        } else {
            difference.setVisibility(View.GONE);
            dots.setVisibility(View.GONE);
            a2.setVisibility(View.GONE);
        }
        enable(true);
        getPositiveButton().setText(hasCompletedTransactions() ? R.string.btn_void : R.string.btn_cancel);
    }

    protected void enable(final boolean on) {
        super.enable(on);
        btnOfflineCredit.setEnabled(on);
        btnCheck.setEnabled(on);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_tender_fragment;
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
                            IPayTenderListener listener,
                            int customAnimationResource,
                            Boolean singleTenderEnabled) {
        DialogUtil.show(context, DIALOG_NAME, PayTenderFragmentDialog_.builder().singleTenderEnabled(singleTenderEnabled).build())
                .setListener(listener)
                .setOrderGuid(orderGuid, orderType)
                .setCustomAnimationResource(customAnimationResource);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
