package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.kaching123.tcr.Logger;
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

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EActivity(R.layout.saleorder_cashier_activity)
@OptionsMenu(R.menu.cashier_activity)
public class CashierActivity extends BaseCashierActivity implements CustomEditBox.IKeyboardSupport {
    private final int TIMES_UP = 0;

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

    private Timer timer;

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

    private Handler USBHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case TIMES_UP:
                    filtInput();
                    tryToSearchBarCode(scannerInput);
                    timer.interrupt();
                    timer = null;
                    break;

            }
        }

    };

    private void filtInput() {
        String input = scannerInput.getText().toString();
        String result = input.toString().replace("\n", "").replace("\r", "");
        scannerInput.setText(result);
    }

    @Override
    public void focusUsbInput() {
        scannerInput.requestFocus();
    }

    private class Timer extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(200);
                notifyHandler();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    ;

    private void notifyHandler() {
        USBHandler.sendEmptyMessage(TIMES_UP);
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
        scannerInput.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.d("trace key-onKeyDown--" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_CTRL_LEFT:
                timer = new Timer();
                timer.start();
            default:
                return super.onKeyDown(keyCode, event);
        }
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
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

    public static void start(Context context) {
        CashierActivity_.intent(context).start();
    }

    public static void start4Return(Context context) {
        CashierActivity_.intent(context).isCreateReturnOrder(true).start();
    }

}
