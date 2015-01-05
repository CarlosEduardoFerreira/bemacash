package com.kaching123.tcr.activity;

import android.content.Context;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback;
import com.kaching123.tcr.fragment.itempick.ItemsListFragment;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment.OnAddonsChangedListener;
import com.kaching123.tcr.fragment.quickservice.QuickCategoriesFragment;
import com.kaching123.tcr.fragment.quickservice.QuickItemsFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment.OnCancelListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by gdubina on 22/11/13.
 */

@EActivity(R.layout.quickservice_activity)
@OptionsMenu(R.menu.quick_service_activity)
public class QuickServiceActivity extends BaseCashierActivity {

    @FragmentById
    protected QuickCategoriesFragment categoriesFragment;

    @FragmentById
    protected QuickItemsFragment itemsListFragment;

    @FragmentById
    protected QuickModifyFragment modifyFragment;

    @Override
    protected void init() {
        super.init();

        getSupportFragmentManager().beginTransaction().hide(searchResultFragment).commit();

        categoriesFragment.setListener(new QuickCategoriesFragment.ICategoryListener() {

            @Override
            public void onCategoryChanged(long id, String depGuid, String catGuid) {
                itemsListFragment.setCategory(catGuid);
            }
        });

        itemsListFragment.setListener(new ItemsListFragment.IItemListener() {

            @Override
            public void onItemSelected(long id, final ItemExModel model) {
                tryToAddItem(model);
            }
        });
        modifyFragment.setCancelListener(new OnCancelListener() {

            @Override
            public void onFragmentCanceled() {
                hideModifiersFragment();
            }
        });
        hideModifiersFragment();
    }

    @Override
    protected void completeOrder() {
        super.completeOrder();
        hideModifiersFragment();
    }

    @Override
    protected void showEditItemModifiers(final String saleItemGuid, final String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, String selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {
        modifyFragment.setupParams(itemGuid, modifiersCount, addonsCount, optionalsCount, selectedModifierGuid, selectedAddonsGuids, selectedOptionalsGuids, new OnAddonsChangedListener() {

            @Override
            public void onAddonsChanged(String modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {
                hideModifiersFragment();
                UpdateSaleItemAddonsCommand.start(QuickServiceActivity.this, saleItemGuid, itemGuid, modifierGuid, addonsGuid, optionalsGuid, updateSaleItemAddonsCallback);
            }
        });
        showModifiersFragment();
    }

    @Override
    protected void actionBarItemClicked() {
        hideModifiersFragment();
    }

    public void hideModifiersFragment() {
        if (modifyFragment == null)
            return;
        getSupportFragmentManager().beginTransaction().hide(modifyFragment).commit();
        getSupportFragmentManager().popBackStack();

    }

    public void showModifiersFragment() {
        getSupportFragmentManager().beginTransaction().show(modifyFragment).addToBackStack(null).commit();
    }

    @Override
    protected void tryToAddItem(final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        boolean hasModifiers = model.modifiersCount > 0 || model.addonsCount > 0 || model.optionalCount > 0;
        if (!hasModifiers) {
            tryToAddCheckPriceType(model, null, null, null, price, quantity, unit);
            return;
        }

        modifyFragment.setupParams(model.guid, model.modifiersCount, model.addonsCount, model.optionalCount, model.defaultModifierGuid, new OnAddonsChangedListener() {

            @Override
            public void onAddonsChanged(String modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {
                hideModifiersFragment();
                tryToAddCheckPriceType(model, modifierGuid, addonsGuid, optionalsGuid, price, quantity, unit);
            }
        });
        showModifiersFragment();
    }

    private BaseUpdateSaleItemAddonsCallback updateSaleItemAddonsCallback = new BaseUpdateSaleItemAddonsCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            startCommand(new DisplaySaleItemCommand(saleItemGuid));
        }
    };

    public static void start(Context context){
        QuickServiceActivity_.intent(context).start();
    }

    public static void start4Return(Context context){
        QuickServiceActivity_.intent(context).isCreateReturnOrder(true).start();
    }
}
