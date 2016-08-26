package com.kaching123.tcr.fragment.tendering;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.tendering.history.TransactionHistoryMiniFragment.ITransactionHistoryMiniFragmentLoader;
import com.kaching123.tcr.fragment.tendering.history.TransactionHistoryMiniFragment_;
import com.kaching123.tcr.fragment.tendering.payment.IPaymentDialogListener;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public abstract class TenderFragmentDialogBase<T extends TenderFragmentDialogBase, C extends IPaymentDialogListener>
        extends StyledDialogFragment implements ITransactionHistoryMiniFragmentLoader {

    public static final int LOADER_ITEMS = 1;


    @ViewById
    protected CheckBox checkboxSingle;

    @ViewById
    protected Button btnCash;

    @ViewById
    protected Button btnCard;

    @ViewById
    protected Button btnCreditReceipt;

    @ViewById
    protected Button btnPaxDebit;

    @ViewById
    protected Button btnPaxEbtCash;

    @ViewById
    protected Button btnOfflineCredit;

    @ViewById
    protected TextView total;

    @ViewById
    protected TextView difference;

    @ViewById
    protected TextView dots;

    @ViewById
    protected TextView a2;

    protected C listener;

    protected String orderGuid;
    protected OrderType orderType;

    protected BigDecimal orderTotal;
    protected int customAnimationResource;

    protected BigDecimal completedAmount = BigDecimal.ZERO;
    protected ArrayList<PaymentTransactionModel> saleOrderModels = new ArrayList<PaymentTransactionModel>();
    protected ArrayList<PaymentTransactionModel> fakeTransactions = new ArrayList<PaymentTransactionModel>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .add(R.id.list_view_holder, TransactionHistoryMiniFragment_
                        .builder()
                        .build()
                        .addListener(this)
                        .injectFakeTransactions(fakeTransactions)
                        .init(orderGuid))
                .commit();
        Window window = getDialog().getWindow();
        window.setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                window.getAttributes().height
        );
        Logger.d("-= Loading initiated =-");
    }

    protected void injectFakeTransactions(ArrayList<PaymentTransactionModel> transactions) {
        if(transactions != null) {
            fakeTransactions.addAll(transactions);
        }
    }

    protected abstract boolean isRefund();

    @AfterViews
    protected void onCreateViews() {
        boolean useCreditReceipt = getApp().getShopInfo().useCreditReceipt;
        if (isRefund() && useCreditReceipt) {
            btnCreditReceipt.setVisibility(View.VISIBLE);
            btnCash.setVisibility(View.GONE);
            btnCard.setVisibility(View.GONE);
            btnOfflineCredit.setVisibility(View.GONE);
        } else if (useCreditReceipt && orderType != OrderType.PREPAID) {
            btnCreditReceipt.setVisibility(View.VISIBLE);
        } else {
            btnCreditReceipt.setVisibility(View.GONE);
        }

        enable(false);
    }

    public T setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
        return (T) this;
    }

    @Override
    public void onLoadComplete(List<PaymentTransactionModel> loaded) {
        for (PaymentTransactionModel saleOrderModel : loaded) {
            this.saleOrderModels.add(saleOrderModel);
        }
        completedAmount = BigDecimal.ZERO;
        for (PaymentTransactionModel transaction : saleOrderModels) {
            if (transaction.status.isSuccessful()
                    && BigDecimal.ZERO.compareTo(transaction.availableAmount) < 0
                    && PaymentType.SALE.equals(transaction.paymentType)) {

                completedAmount = completedAmount.add(transaction.availableAmount);
            }
        }

        calculateDlgHeight();
        Logger.d("-= Loading reached level 1 =-");
        onLoadComplete();
    }

    protected void calculateDlgHeight(){
        boolean useCreditReceipt = getApp().getShopInfo().useCreditReceipt && orderType != OrderType.PREPAID;
        boolean isRefund = isRefund();
        boolean expand = saleOrderModels != null && saleOrderModels.size() > 0;

        int height = expand ? R.dimen.pay_tender_dialog_height_expanded : R.dimen.pay_tender_dialog_height;
        if(isRefund && useCreditReceipt){
            height = R.dimen.pay_tender_dialog_height;
        }else if(isRefund){
            height = R.dimen.pay_tender_dialog_height_3_expanded;
        }else if(useCreditReceipt){
            height = expand ? R.dimen.pay_tender_dialog_height_3_expanded : R.dimen.pay_tender_dialog_height_3;
        }
        //saleOrderModels != null && saleOrderModels.size() > 0 ? R.dimen.pay_tender_dialog_height_expanded : R.dimen.pay_tender_dialog_height)
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getResources().getDimensionPixelOffset(height)
        );
    }

    protected boolean hasCompletedTransactions() {
        return BigDecimal.ZERO.compareTo(completedAmount) < 0;
    }

    protected void enable(final boolean on) {
        btnCash.setEnabled(on);
        btnCard.setEnabled(getApp().isPaymentUserValid() || getApp().isPaxConfigured() && on);
        btnCreditReceipt.setEnabled(on);
        enablePositiveButtons(on);
        Logger.d("Buttons have been enabled " + on);
    }


    public T setOrderGuid(String orderGuid, OrderType orderType) {
        this.orderGuid = orderGuid;
        this.orderType = orderType;
        return (T) this;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return hasCompletedTransactions() ? R.string.btn_void : R.string.btn_cancel;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    listener.onCancel();
                }
                return false;
            }
        };
    }

    public T setCustomAnimationResource(int customAnimationResource) {
        this.customAnimationResource = customAnimationResource;
        return (T) this;
    }

    public T setListener(C listener) {
        this.listener = listener;
        return (T) this;
    }


    public void onLoadComplete() {
        loadOrderData();
    }

    protected void loadOrderData() {
        if (TextUtils.isEmpty(orderGuid)) {
            getLoaderManager().destroyLoader(LOADER_ITEMS);
            return;
        }
        getLoaderManager().restartLoader(LOADER_ITEMS, null, new OrderTotalPriceLoaderCallback(getActivity(), orderGuid) {

            @Override
            public void onZeroValue() {
                setZero();
            }

            @Override
            public void onCalcTotal(
                    boolean isTaxableOrder,
                    BigDecimal orderDiscount,
                    DiscountType orderDiscountType,
                    BigDecimal orderDiscountVal,
                    BigDecimal totalItemTotal,
                    BigDecimal totalTaxVatValue,
                    BigDecimal totalItemDiscount,
                    BigDecimal totalOrderPrice,
                    BigDecimal availableDiscount,
                    BigDecimal transactionFee) {
                Logger.d("-= Loading reached level 2 =-");
                calcTotal(totalOrderPrice.add(transactionFee));

            }
        });
    }

    private void setZero() {
        showPrice(this.total, BigDecimal.ZERO);
    }

    protected void calcTotal(BigDecimal totalOrderPrice) {

        showPrice(this.total, orderTotal = totalOrderPrice);
        if (orderTotal != null) {
            updateAfterCalculated();
        }
    }

    protected abstract void updateAfterCalculated();
}
