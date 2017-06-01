package com.kaching123.tcr.fragment.saleorder;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
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
@EFragment(R.layout.detailed_qservice_total_fragment)
public class DetailedQServiceTotalCostFragment extends Fragment {
    public static final int LOADER_ITEMS = 1;

    @ViewById(R.id.total_cost_subtotal)
    protected TextView subTotal;
    @ViewById(R.id.total_cost_discount)
    protected TextView discount;
    @ViewById(R.id.total_cost_tax)
    protected TextView tax;
    @ViewById(R.id.total_cost_total)
    protected TextView orderTotal;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int taxColorNormal;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int taxColorEmpty;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int discountColorNormal;

    @ColorRes(R.color.subtotal_table_discount_additional)
    protected int discountColorAdditional;

    private String orderGuid;

    private BigDecimal orderTotalVal;

    OrderItemListFragment orderItemListFragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setZero();
        orderItemListFragment = (OrderItemListFragment) getFragmentManager().findFragmentById(R.id.order_item_list_fragment);
    }

    public void setOrderGuid(String orderGuid) {
        Logger.d("setting guid : " + orderGuid);
        this.orderGuid = orderGuid;
        if (getActivity() == null) {
            return;
        }
        if (TextUtils.isEmpty(orderGuid)) {
            setZero();
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

    }

    public BigDecimal getOrderTotal() {
        return this.orderTotalVal;
    }

    public String getOrderDiscountTotal() {
        return this.discount.getText().toString();
    }

    public String getOrderTaxTotal() {
        return this.tax.getText().toString();
    }

    public String getOrderSubTotal() {
        return this.subTotal.getText().toString();
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

    public IOrderPricingListener getActionListener() {
        if (getActivity() instanceof IOrderPricingListener)
            return (IOrderPricingListener) getActivity();
        return null;
    }

    @Click
    protected void orderDiscountBlockClicked() {
        BigDecimal subTotal = (BigDecimal) this.subTotal.getTag();
        if (subTotal == null) {
            subTotal = BigDecimal.ZERO;
        }
        getActionListener().onDiscount(subTotal);
    }

    @Click
    protected void taxBlockClicked() {
        getActionListener().onTax();
    }


    public static interface IOrderPricingListener {

        void onDiscount(BigDecimal itemsSubTotal);

        void onTax();
    }
}
