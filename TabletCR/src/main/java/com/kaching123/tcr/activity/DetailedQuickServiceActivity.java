package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceMainSaleActionsFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceReservedActionsFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceTotalCostFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQuickCategoriesFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQuickItemsFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQuickModifyFragment;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;
import com.kaching123.tcr.fragment.search.SearchItemsListFragment;
import com.kaching123.tcr.model.CustomerModel;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;

import java.math.BigDecimal;

/**
 * Created by mboychenko on 5/26/2017.
 */

@EActivity(R.layout.detailed_quickservice_activity)
@OptionsMenu(R.menu.quick_service_activity)
public class DetailedQuickServiceActivity extends BaseQuickServiceActiviry implements DetailedQServiceMainSaleActionsFragment.IOrderRegisterActionListener,
        DetailedQServiceTotalCostFragment.IOrderPricingListener {

    @FragmentById
    protected DetailedQServiceMainSaleActionsFragment detailedSaleActionsFragment;

    @FragmentById
    protected DetailedQServiceTotalCostFragment detailedTotalFragment;

    @FragmentById
    protected DetailedQServiceReservedActionsFragment detailedReservedActionsFragment;

    @FragmentById
    protected OrderItemListFragment orderItemListFragment;

    @FragmentById
    protected SearchItemsListFragment searchResultFragment;

    @FragmentById
    protected DetailedQuickCategoriesFragment categoriesFragment;

    @FragmentById
    protected DetailedQuickItemsFragment itemsListFragment;

    @FragmentById
    protected DetailedQuickModifyFragment modifyFragment;

    @Override
    public Fragment getSearchResultFragment() {
        return searchResultFragment;
    }

    @Override
    public ListFragment getOrderItemListFragment() {
        return orderItemListFragment;
    }

    @Override
    protected void hideTotalCostFragment() {
        if (detailedTotalFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(detailedTotalFragment).commit();
        }
    }
    @Override
    protected void showTotalCostFragment() {
        if (detailedTotalFragment != null) {
            getSupportFragmentManager().beginTransaction().show(detailedTotalFragment).commit();
        }
    }

    @Override
    protected BigDecimal totalCostGetOrderTotal() {
        return detailedTotalFragment.getOrderTotal();
    }

    @Override
    protected String totalCostGetOrderSubTotal() {
        return detailedTotalFragment.getOrderSubTotal();
    }

    @Override
    protected String totalCostGetOrderDiscountTotal() {
        return detailedTotalFragment.getOrderDiscountTotal();
    }

    @Override
    protected String totalCostGetOrderTaxTotal() {
        return detailedTotalFragment.getOrderTaxTotal();
    }

    @Override
    protected String totalCostGetOrderAmountTotal() {
        return detailedTotalFragment.getOrderAmountTotal();
    }

    @Override
    protected void totalCostSetOrderGuid(String guid) {
        detailedTotalFragment.setOrderGuid(guid);
        detailedSaleActionsFragment.setOrderGuid(guid);
    }

    @Override
    protected void totalCostSetSuspendedItemsCount(int count) {
        detailedSaleActionsFragment.setSuspendedItemsCount(count);
    }

    @Override
    protected void totalCostSetCustomer(CustomerModel customerModel) {
        detailedSaleActionsFragment.setCustomer(customerModel);
    }

    @Override
    protected void totalCostSetCreateReturnOrder(boolean isCreateReturnOrder) {
        detailedSaleActionsFragment.setCreateReturnOrder(isCreateReturnOrder);
    }

    @Override
    protected void totalCostSetCustomerButtonEnabled(boolean isCreateReturnOrder) {
        detailedSaleActionsFragment.setCustomerButtonEnabled(isCreateReturnOrder);
    }

    @Override
    protected Fragment getModifierFragment() {
        return modifyFragment;
    }

    @Override
    protected Fragment getItemListFragment() {
        return itemsListFragment;
    }

    @Override
    protected Fragment getCategoriesFragment() {
        return categoriesFragment;
    }

    public static void start(Context context) {
        DetailedQuickServiceActivity_.intent(context).start();
    }

    public static void start4Return(Context context) {
        DetailedQuickServiceActivity_.intent(context).isCreateReturnOrder(true).start();
    }
}
