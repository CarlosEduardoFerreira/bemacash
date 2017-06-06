package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand;
import com.kaching123.tcr.commands.store.saleorder.ItemsNegativeStockTrackingCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderAgeVeridiedCommand;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.itempick.ItemsListFragment;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.fragment.quickservice.AgeVerificationFragment;
import com.kaching123.tcr.fragment.quickservice.QuickCategoriesFragment;
import com.kaching123.tcr.fragment.quickservice.QuickItemsFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment.OnCancelListener;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment;
import com.kaching123.tcr.fragment.search.SearchItemsListFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by gdubina on 22/11/13.
 */

@EActivity(R.layout.quickservice_activity)
@OptionsMenu(R.menu.quick_service_activity)
public class QuickServiceActivity extends BaseQuickServiceActiviry {

    @FragmentById
    protected QuickCategoriesFragment categoriesFragment;

    @FragmentById
    protected QuickItemsFragment itemsListFragment;

    @FragmentById
    protected QuickModifyFragment modifyFragment;

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

    public static void start(Context context) {
        QuickServiceActivity_.intent(context).start();
    }

    public static void start4Return(Context context) {
        QuickServiceActivity_.intent(context).isCreateReturnOrder(true).start();
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
}
