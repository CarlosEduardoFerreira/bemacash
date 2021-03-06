package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceMainSaleActionsFragment.IOrderRegisterActionListener;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceTotalCostFragment.IOrderPricingListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.integralIntegerFormat;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

@EFragment(R.layout.saleorder_total_cost_fragment)
public class TotalCostFragment extends Fragment {

    public static final int LOADER_ITEMS = 1;

    @ViewById(R.id.total_cost_subtotal)
    protected TextView subTotal;
    @ViewById(R.id.total_cost_discount)
    protected TextView discount;
    @ViewById(R.id.total_cost_tax)
    protected TextView tax;

    @ViewById(R.id.total_cost_total)
    protected TextView orderTotal;

    @ViewById
    protected View btnHold;
    @ViewById
    protected View btnVoid;
    @ViewById
    protected Button btnPay;
    @ViewById
    protected Button btnCustomer;
    @ViewById
    protected View taxBlock;
    @ViewById
    protected View orderDiscountBlock;

    @ViewById
    protected TextView customerLabel;

    @ColorRes(R.color.subtotal_table_text_main)
    protected int taxColorNormal;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int taxColorEmpty;

    @ColorRes(R.color.subtotal_table_text_main)
    protected int discountColorNormal;

    @ColorRes(R.color.subtotal_table_discount_additional)
    protected int discountColorAdditional;

    private String orderGuid;

    private BigDecimal orderTotalVal;

    private boolean isCreateReturnOrder;

    OrderItemListFragment orderItemListFragment;

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
            btnPay.setBackgroundResource(R.drawable.pay_btn);
            btnPay.setTextSize(getActivity().getResources().getDimensionPixelOffset(R.dimen.button_pay_text_big));
        } else {
            btnPay.setBackgroundResource(R.drawable.return_btn);
            btnPay.setText(R.string.button_return);
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
        taxBlock.setEnabled(clickable);
        orderDiscountBlock.setEnabled(clickable);
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
        if (orderDiscountVal == null || BigDecimal.ZERO.compareTo(orderDiscountVal) == 0) {
            showPrice(this.discount, totalItemDiscount);
            this.discount.setTextColor(discountColorNormal);
        } else {
            this.discount.setTextColor(discountColorAdditional);
            showPrice(this.discount, totalItemDiscount.add(orderDiscountVal));
        }
        showPrice(this.subTotal, totalItemTotal);
        this.subTotal.setTag(availableDiscount);

        showPrice(this.tax, totalTaxVatValue);
        if (taxable) {
            this.tax.setTextColor(taxColorNormal);
        } else if (BigDecimal.ZERO.compareTo(totalTaxVatValue) != 0){
            this.tax.setTextColor(taxColorEmpty);
        }

        this.orderTotalVal = totalOrderPrice;
        showPrice(this.orderTotal, totalOrderPrice);

        //in regular case we allow to proceed 0-price order; in return order we don't
        if (isCreateReturnOrder){
            this.btnPay.setEnabled(BigDecimal.ZERO.compareTo(totalOrderPrice) == -1);
        }
    }

    public BigDecimal getOrderTotal() {
        return this.orderTotalVal;
    }

    public String getOrderSubTotal() {
        return this.subTotal.getText().toString();
    }

    public String getOrderDiscountTotal() {
        return this.discount.getText().toString();
    }

    public String getOrderTaxTotal() {
        return this.tax.getText().toString();
    }

    public String getOrderAmountTotal() {
        return this.orderTotal.getText().toString();
    }

    private void setZero() {
        this.orderTotalVal = BigDecimal.ZERO;
        showPrice(this.discount, BigDecimal.ZERO);
        showPrice(this.subTotal, BigDecimal.ZERO);
        showPrice(this.orderTotal, BigDecimal.ZERO);
        showPrice(this.tax, BigDecimal.ZERO);
        if (this.tax != null) this.tax.setTextColor(taxColorNormal);
    }

    public IOrderRegisterActionListener getActionListener() {
        if (getActivity() instanceof IOrderRegisterActionListener)
            return (IOrderRegisterActionListener) getActivity();
        return null;
    }

    public IOrderPricingListener getPricingListener() {
        if (getActivity() instanceof IOrderPricingListener)
            return (IOrderPricingListener) getActivity();
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

    @Click
    protected void orderDiscountBlockClicked() {
        BigDecimal subTotal = (BigDecimal) this.subTotal.getTag();
        if (subTotal == null) {
            subTotal = BigDecimal.ZERO;
        }
        getPricingListener().onDiscount(subTotal);
    }

    @Click
    protected void taxBlockClicked() {
        getPricingListener().onTax();
    }

    public void setSuspendedItemsCount(int count) {
        btnHold.setEnabled(!isCreateReturnOrder && !TextUtils.isEmpty(this.orderGuid) && count < TcrApplication.get().getSuspendedMaxCount());
    }

    public void setCreateReturnOrder(boolean isCreateReturnOrder) {
        this.isCreateReturnOrder = isCreateReturnOrder;
    }

}
