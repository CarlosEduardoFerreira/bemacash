package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.DiscountType;

import java.math.BigDecimal;

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
    protected View taxBlock;
    @ViewById
    protected View orderDiscountBlock;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnHold.setVisibility(isCreateReturnOrder ? View.GONE : View.VISIBLE);
        setPayButtonView();
        setZero();
        updateClickable(false);
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
                    BigDecimal availableDiscount,
                    BigDecimal transactionFee) {
                calcTotal(taxable,
                        orderDiscountVal,
                        totalItemTotal,
                        totalTaxVatValue,
                        totalItemDiscount,
                        totalOrderPrice,
                        availableDiscount
                );
            }
        });
    }

    private void updateClickable(boolean clickable) {
        btnHold.setEnabled(!isCreateReturnOrder && clickable);
        btnVoid.setEnabled(clickable);
        btnPay.setEnabled(clickable);
        taxBlock.setEnabled(clickable);
        orderDiscountBlock.setEnabled(clickable);
    }

    private void calcTotal(
            boolean taxable,
            BigDecimal orderDiscountVal,
            BigDecimal totalItemTotal,
            BigDecimal totalTaxVatValue,
            BigDecimal totalItemDiscount,
            BigDecimal totalOrderPrice,
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
        } else {
            this.tax.setTextColor(taxColorEmpty);
        }

        this.orderTotalVal = totalOrderPrice;
        showPrice(this.orderTotal, totalOrderPrice);
        if (BigDecimal.ZERO.compareTo(totalItemTotal) != 0 && BigDecimal.ZERO.compareTo(totalOrderPrice) != -1) {
            this.orderTotal.setTextColor(taxColorEmpty);
            this.btnPay.setEnabled(false);
        } else {
            this.orderTotal.setTextColor(taxColorNormal);
            this.btnPay.setEnabled(true);
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

    public IOrderActionListener getActionListener() {
        assert getActivity() instanceof IOrderActionListener;
        return (IOrderActionListener) getActivity();
    }

    @Click
    protected void btnPayClicked() {
        getActionListener().onPay();
    }

    @Click
    protected void btnHoldClicked() {
        getActionListener().onHold();
    }

    @Click
    protected void btnVoidClicked() {
        getActionListener().onVoid();
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

    public void setSuspendedItemsCount(int count) {
        btnHold.setEnabled(!isCreateReturnOrder && !TextUtils.isEmpty(this.orderGuid) && count < TcrApplication.get().getSuspendedMaxCount());
    }

    public void setCreateReturnOrder(boolean isCreateReturnOrder) {
        this.isCreateReturnOrder = isCreateReturnOrder;
    }

    public static interface IOrderActionListener {
        void onPay();

        void onHold();

        void onVoid();

        void onDiscount(BigDecimal itemsSubTotal);

        void onTax();
    }


}
