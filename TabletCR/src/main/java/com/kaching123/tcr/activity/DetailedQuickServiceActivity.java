package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import com.kaching123.tcr.fragment.saleorder.DetailedQServiceMainSaleActionsFragment;
import com.kaching123.tcr.fragment.saleorder.DetaildeQServiceTotalCostFragment;
import com.kaching123.tcr.model.CustomerModel;

import org.androidannotations.annotations.EActivity;

import java.math.BigDecimal;

/**
 * Created by mboychenko on 5/26/2017.
 */

@EActivity
public class DetailedQuickServiceActivity extends SuperBaseCashierActivity implements DetailedQServiceMainSaleActionsFragment.IOrderRegisterActionListener,
        DetaildeQServiceTotalCostFragment.IOrderPricingListener {


    @Override
    protected void showEditItemModifiers(String saleItemGuid, String itemGuid) {

    }

    @Override
    public Fragment getSearchResultFragment() {
        return null;
    }

    @Override
    public ListFragment getOrderItemListFragment() {
        return null;
    }

    @Override
    protected void hideTotalCostFragment() {

    }

    @Override
    protected void showTotalCostFragment() {

    }

    @Override
    protected BigDecimal totalCostGetOrderTotal() {
        return null;
    }

    @Override
    protected String totalCostGetOrderSubTotal() {
        return null;
    }

    @Override
    protected String totalCostGetOrderDiscountTotal() {
        return null;
    }

    @Override
    protected String totalCostGetOrderTaxTotal() {
        return null;
    }

    @Override
    protected String totalCostGetOrderAmountTotal() {
        return null;
    }

    @Override
    protected void totalCostSetOrderGuid(String guid) {

    }

    @Override
    protected void totalCostSetSuspendedItemsCount(int count) {

    }

    @Override
    protected void totalCostSetCustomer(CustomerModel customerModel) {

    }

    @Override
    protected void totalCostSetCreateReturnOrder(boolean isCreateReturnOrder) {
//        totalCostFragment.setCreateReturnOrder(isCreateReturnOrder);
//        orderItemListFragment.setCreateReturnOrder(isCreateReturnOrder);
    }

    @Override
    protected void totalCostSetCustomerButtonEnabled(boolean isCreateReturnOrder) {

    }


    public static void start(Context context) {
        DetailedQuickServiceActivity_.intent(context).start();
    }

}
