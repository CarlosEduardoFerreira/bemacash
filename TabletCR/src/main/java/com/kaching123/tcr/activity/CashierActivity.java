package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.itempick.DrawerCategoriesFragment;
import com.kaching123.tcr.fragment.itempick.ItemsListFragment;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment.OnAddonsChangedListener;
import com.kaching123.tcr.fragment.modify.ModifyFragment;
import com.kaching123.tcr.model.ItemExModel;

import java.util.ArrayList;

@EActivity(R.layout.saleorder_cashier_activity)
@OptionsMenu(R.menu.cashier_activity)
public class CashierActivity extends BaseCashierActivity implements CustomEditBox.IKeyboardSupport{

    @FragmentById
    protected ItemsListFragment itemsListFragment;

    @FragmentById
    protected DrawerCategoriesFragment drawerCategoriesFragment;

    @ViewById
    protected DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    @ViewById
    protected KeyboardView keyboard;

    @ViewById
    protected CustomEditBox scannerInput;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void init() {
        //CustomEditBox editText = (CustomEditBox)scannerFakeInput;
        scannerInput.setKeyboardSupportConteiner(this);
        keyboard.setDotEnabled(false);
        keyboard.attachEditView(scannerInput);
        scannerInput.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                tryToSearchBarCode(scannerInput);
                return true;
            }
        });
        super.init();
        initDrawer();
        getSupportFragmentManager().beginTransaction().hide(searchResultFragment).commit();

        drawerCategoriesFragment.setListener(new DrawerCategoriesFragment.ICategoryListener() {

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
    }

    @Override
    protected void showEditItemModifiers(final String saleItemGuid, final String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, String selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {
        ModifyFragment.show(
                this,
                itemGuid,
                modifiersCount,
                addonsCount,
                optionalsCount,
                selectedModifierGuid,
                selectedAddonsGuids,
                selectedOptionalsGuids,
                new OnAddonsChangedListener() {
                    @Override
                    public void onAddonsChanged(String modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {

                        UpdateSaleItemAddonsCommand.start(CashierActivity.this, saleItemGuid, itemGuid, modifierGuid, addonsGuid, optionalsGuid, updateSaleItemAddonsCallback);
                    }
                }
        );

    }

    @Override
    protected void closeAllPickers() {
        super.closeAllPickers();
        drawerLayout.closeDrawers();
    }

    private void initDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void showSearchFragment() {
        drawerLayout.closeDrawers();
        super.showSearchFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        //keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        //keyboard.detachEditView();
    }

    private BaseUpdateSaleItemAddonsCallback updateSaleItemAddonsCallback = new BaseUpdateSaleItemAddonsCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            startCommand(new DisplaySaleItemCommand(saleItemGuid));
        }
    };

    public static void start(Context context){
        CashierActivity_.intent(context).start();
    }

    public static void start4Return(Context context){
        CashierActivity_.intent(context).isCreateReturnOrder(true).start();
    }

}
