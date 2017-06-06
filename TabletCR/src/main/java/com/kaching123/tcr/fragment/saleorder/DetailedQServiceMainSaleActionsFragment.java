package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DiscountType;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.integralIntegerFormat;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by mboychenko on 5/29/2017.
 */
@EFragment(R.layout.detailed_qservice_main_sale_actions_fragment)
public class DetailedQServiceMainSaleActionsFragment  extends Fragment {
    public static final int LOADER_ITEMS = 1;

    @ViewById
    protected View btnHold;
    @ViewById
    protected View btnVoid;
    @ViewById
    protected View btnPay;
    @ViewById
    protected View btnCustomer;
    @ViewById
    protected TextView btnPayText;
    @ViewById
    protected TextView customerLabel;

    private boolean isCreateReturnOrder;
    private String orderGuid;
    private BigDecimal orderTotalVal;
    private OrderItemListFragment orderItemListFragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnHold.setVisibility(isCreateReturnOrder ? View.GONE : View.VISIBLE);
        setPayButtonView();
        setZero();
        updateClickable(false);

        orderItemListFragment = (OrderItemListFragment) getFragmentManager().findFragmentById(R.id.order_item_list_fragment);
    }

    private void setPayButtonView() {
        if (!isCreateReturnOrder) {
//            btnPay.setBackgroundResource(R.drawable.pay_btn_drawable);
//            btnPayText.setTextSize(getActivity().getResources().getDimensionPixelOffset(R.dimen.button_pay_text_big));
        } else {
//            btnPay.setBackgroundResource(R.drawable.return_btn);
            btnPayText.setText(R.string.button_return);
        }
    }

    public void setOrderGuid(String orderGuid) {
        Logger.d("setting guid : " + orderGuid);
        this.orderGuid = orderGuid;
        if (getActivity() == null) {
            return;
        }
        if (TextUtils.isEmpty(orderGuid)) {
            updateClickable(false);
            setZero();
            customerLabel.setText(null);
            getLoaderManager().destroyLoader(LOADER_ITEMS);
            return;
        }
        updateClickable(true);
        getLoaderManager().restartLoader(LOADER_ITEMS, null, new OrderTotalPriceLoaderCallback(getActivity(), orderGuid) {

            @Override
            public void onZeroValue() {
                setZero();
            }

            @Override
            public void onCalcTotal(
                    boolean taxable,
                    BigDecimal orderDiscount,
                    DiscountType orderDiscountType,
                    BigDecimal orderDiscountVal,
                    BigDecimal totalItemTotal,
                    BigDecimal totalTaxVatValue,
                    BigDecimal totalItemDiscount,
                    BigDecimal totalOrderPrice,
                    BigDecimal totalOrderEbtPrice,
                    BigDecimal availableDiscount,
                    BigDecimal transactionFee) {
                calcTotal(taxable,
                        orderDiscountVal,
                        totalItemTotal,
                        totalTaxVatValue,
                        totalItemDiscount,
                        totalOrderPrice,
                        totalOrderEbtPrice,
                        availableDiscount
                );
            }
        });
    }

    public void setCustomer(CustomerModel customer){
        if (customer == null){
            customerLabel.setText(null);
        }else{
            customerLabel.setText(String.format("%s\n%s pts", customer.getFullName(), integralIntegerFormat(customer.loyaltyPoints)));
        }
    }

    public void setCustomerButtonEnabled(boolean enabled){
        btnCustomer.setEnabled(enabled);
    }

    private void updateClickable(boolean clickable) {
        btnHold.setEnabled(!isCreateReturnOrder && clickable);
        btnVoid.setEnabled(clickable);
        btnPay.setEnabled(clickable && (!isCreateReturnOrder || getItemCount() > 0));
    }

    private int getItemCount(){
        orderItemListFragment = (OrderItemListFragment) getFragmentManager().findFragmentById(R.id.order_item_list_fragment);
        if (orderItemListFragment == null){
            return 0;
        }else{
            return orderItemListFragment.getListAdapter().getCount();
        }
    }


    private void calcTotal(
            boolean taxable,
            BigDecimal orderDiscountVal,
            BigDecimal totalItemTotal,
            BigDecimal totalTaxVatValue,
            BigDecimal totalItemDiscount,
            BigDecimal totalOrderPrice,
            BigDecimal totalOrderEbtPrice,
            BigDecimal availableDiscount) {

        this.orderTotalVal = totalOrderPrice;

        //in regular case we allow to proceed 0-price order; in return order we don't
        if (isCreateReturnOrder){
            this.btnPay.setEnabled(BigDecimal.ZERO.compareTo(totalOrderPrice) == -1);
        }
    }

    public BigDecimal getOrderTotal() {
        return this.orderTotalVal;
    }

    private void setZero() {
        this.orderTotalVal = BigDecimal.ZERO;
    }

    public IOrderRegisterActionListener getActionListener() {
        if (getActivity() instanceof IOrderRegisterActionListener)
            return (IOrderRegisterActionListener) getActivity();
        return null;
    }

    @Click
    protected void btnPayClicked() {
        orderItemListFragment.adapter.carlHighlightDoIt = false;
        getActionListener().onPay();
    }

    @Click
    protected void btnHoldClicked() {
        orderItemListFragment.adapter.carlHighlightDoIt = false;
        getActionListener().onHold();
    }

    @Click
    protected void btnVoidClicked() {
        getActionListener().onVoid();
    }

    @Click
    protected void btnCustomerClicked() {
        orderItemListFragment.adapter.carlHighlightDoIt = false;
        getActionListener().onCustomer();
    }

    public void setSuspendedItemsCount(int count) {
        btnHold.setEnabled(!isCreateReturnOrder && !TextUtils.isEmpty(this.orderGuid) && count < TcrApplication.get().getSuspendedMaxCount());
    }

    public void setCreateReturnOrder(boolean isCreateReturnOrder) {
        this.isCreateReturnOrder = isCreateReturnOrder;
    }

    public static interface IOrderRegisterActionListener {
        void onPay();

        void onHold();

        void onVoid();

        void onCustomer();
    }
}
