package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.itempick.DrawerCategoriesFragment;
import com.kaching123.tcr.fragment.itempick.ItemsListFragment;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.fragment.modify.ModifyFragment;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment;
import com.kaching123.tcr.fragment.search.SearchItemsListFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
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

    @FragmentById
    protected TotalCostFragment totalCostFragment;

    @FragmentById
    protected OrderItemListFragment orderItemListFragment;

    @FragmentById
    protected SearchItemsListFragment searchResultFragment;

    private Timer timer;

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

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(CashierActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressReceiver.unregister(CashierActivity.this);
    }

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED);
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED);
    }

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED.equals(intent.getAction())) {
                if (!intent.getBooleanExtra(UploadTaskV2.EXTRA_SUCCESS, false))
                    if (intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE) != null && intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE).equalsIgnoreCase("400"))
                        Toast.makeText(CashierActivity.this, R.string.warning_transaction_upload_fail, Toast.LENGTH_LONG).show();
            }
            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED.equals(intent.getAction())) {

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
        scannerInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
//                    Toast.makeText(CashierActivity.this, scannerInput.getText(), Toast.LENGTH_SHORT).show();
                    tryToSearchBarCode(scannerInput);
                    return true;
                }
                return false;
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
    protected void makeScannerInputFocus() {
        scannerInput.setFocusableInTouchMode(true);
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
    protected void showEditItemModifiers(final String saleItemGuid, final String itemGuid) {
        ModifyFragment.show(
                this,
                itemGuid, saleItemGuid,
                new ItemModifiersFragment.OnAddonsChangedListener() {
                    @Override
                    public void onAddonsChanged(ArrayList<String>  modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {

                        UpdateSaleItemAddonsCommand.start(CashierActivity.this,
                                saleItemGuid, itemGuid, modifierGuid, addonsGuid, optionalsGuid, updateSaleItemAddonsCallback);
                    }

                    @Override
                    public void onModifiersCountInsufficient(ModifierGroupModel group) {
                        showModifiersInsufficientCountDialog(group);
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
            startCommand(new DisplaySaleItemCommand(saleItemGuid));// show on display

        }
    };

    public static void start(Context context) {
        CashierActivity_.intent(context).start();
    }

    public static void start4Return(Context context) {
        CashierActivity_.intent(context).isCreateReturnOrder(true).start();
    }

}
