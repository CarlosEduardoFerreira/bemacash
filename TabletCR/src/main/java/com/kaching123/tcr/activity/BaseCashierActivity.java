package com.kaching123.tcr.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment;
import com.kaching123.tcr.fragment.search.SearchItemsListFragment;


import com.kaching123.tcr.model.CustomerModel;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

import java.math.BigDecimal;


@EActivity
public abstract class BaseCashierActivity extends SuperBaseCashierActivity {

    @FragmentById
    protected TotalCostFragment totalCostFragment;

    @FragmentById
    protected OrderItemListFragment orderItemListFragment;

    @FragmentById
    protected SearchItemsListFragment searchResultFragment;

    @Override
    public Fragment getSearchResultFragment() {
        return searchResultFragment;
    }

    @Override
    public ListFragment getOrderItemListFragment() {
        return orderItemListFragment;
    }

    protected void hideTotalCostFragment() {
        if (totalCostFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(totalCostFragment).commit();
        }
    }

    protected void showTotalCostFragment() {
        if (totalCostFragment != null) {
            getSupportFragmentManager().beginTransaction().show(totalCostFragment).commit();
        }
    }

    protected String totalCostGetOrderSubTotal() {
        return totalCostFragment.getOrderSubTotal();
    }

    protected String totalCostGetOrderDiscountTotal() {
        return totalCostFragment.getOrderDiscountTotal();
    }
    protected String totalCostGetOrderTaxTotal() {
        return totalCostFragment.getOrderTaxTotal();
    }

    protected String totalCostGetOrderAmountTotal() {
        return totalCostFragment.getOrderAmountTotal();
    }
    protected void totalCostSetOrderGuid(String guid) {
        totalCostFragment.setOrderGuid(guid);
    }

    protected void totalCostSetSuspendedItemsCount(int count) {
        totalCostFragment.setSuspendedItemsCount(count);
    }
    protected void totalCostSetCustomer(CustomerModel customerModel) {
        totalCostFragment.setCustomer(customerModel);
    }
    protected void totalCostSetCreateReturnOrder(boolean isCreateReturnOrder) {
        totalCostFragment.setCreateReturnOrder(isCreateReturnOrder);
    }
    protected BigDecimal totalCostGetOrderTotal() {
        return totalCostFragment.getOrderTotal();
    }

    protected void totalCostSetCustomerButtonEnabled(boolean enabled) {
        totalCostFragment.setCustomerButtonEnabled(enabled);
    }

}
