package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.itempick.ItemsListFragment;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.fragment.quickservice.QuickCategoriesFragment;
import com.kaching123.tcr.fragment.quickservice.QuickItemsFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment.OnCancelListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.PlanOptions;
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
public class QuickServiceActivity extends BaseCashierActivity implements CustomEditBox.IKeyboardSupport {

@FragmentById
    protected QuickCategoriesFragment categoriesFragment;

    @FragmentById
    protected QuickItemsFragment itemsListFragment;

    @FragmentById
    protected QuickModifyFragment modifyFragment;
    private boolean isVisiable = true;

    @ViewById
    protected KeyboardView keyboard;

    @Override
    protected void init() {
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
        scannerInput.requestFocus();
    }

    @Override
    protected void makeScannerInputFocus() {
        scannerInput.setFocusableInTouchMode(true);
        scannerInput.requestFocus();
    }

    @Override
    protected void completeOrder() {
        super.completeOrder();
        hideModifiersFragment();

        strItemCount = "0";
        saleItemCount = 0;
        updateItemCountMsg();
    }

    @Override
    protected void showEditItemModifiers(final String saleItemGuid, final String itemGuid) {
        modifyFragment.setupParams(itemGuid, saleItemGuid, new ItemModifiersFragment.OnAddonsChangedListener() {

            @Override
            public void onAddonsChanged(ArrayList<String> modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {
                hideModifiersFragment();
                UpdateSaleItemAddonsCommand.start(QuickServiceActivity.this,
                        saleItemGuid,
                        itemGuid,
                        modifierGuid,
                        addonsGuid,
                        optionalsGuid,
                        updateSaleItemAddonsCallback);
            }
        });
        showModifiersFragment();
    }

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED);
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(QuickServiceActivity.this);
        isVisiable = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressReceiver.unregister(QuickServiceActivity.this);
        isVisiable = false;
    }

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED.equals(intent.getAction())) {
                if (!intent.getBooleanExtra(UploadTaskV2.EXTRA_SUCCESS, false))
                    if (intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE) != null && intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE).equalsIgnoreCase("400"))
                        Toast.makeText(QuickServiceActivity.this, R.string.warning_transaction_upload_fail, Toast.LENGTH_LONG).show();
            }
            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED.equals(intent.getAction())) {

            }
        }
    };

    @Override
    protected void actionBarItemClicked() {
        hideModifiersFragment();
    }

    public void hideModifiersFragment() {
        if (modifyFragment == null)
            return;
        if (isVisiable) {
            getSupportFragmentManager().beginTransaction().hide(modifyFragment).commit();
            getSupportFragmentManager().popBackStack();
            showQuickModifyFragment();
        }
    }

    public void showModifiersFragment() {
        getSupportFragmentManager().beginTransaction().show(modifyFragment).addToBackStack(null).commit();
        hideQuickModifyFragment();
    }

    @Override
    protected void tryToAddItem(final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit) {

        CollectModifiersCommand.start(this, model.guid, null, price, model, quantity, unit, true, collectionCallback);

    }

    private CollectModifiersCommand.BaseCollectModifiersCallback collectionCallback = new CollectModifiersCommand.BaseCollectModifiersCallback() {
        @Override
        public void onCollected(ArrayList<CollectModifiersCommand.SelectedModifierExModel> modifiers, final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit, boolean hasAutoApply) {

            if (hasAutoApply) {
                tryToAddCheckPriceType(model, getModifiers(modifiers), null, null, price, quantity, unit);
                return;
            }

            boolean hasModifiers = model.modifiersCount > 0 || model.addonsCount > 0 || model.optionalCount > 0;
            if (!hasModifiers || !PlanOptions.isModifiersAllowed()) {
                tryToAddCheckPriceType(model, null, null, null, price, quantity, unit);
                return;
            }

            modifyFragment.setupParams(model.guid, new ItemModifiersFragment.OnAddonsChangedListener() {

                @Override
                public void onAddonsChanged(ArrayList<String> modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {
                    hideModifiersFragment();
                    tryToAddCheckPriceType(model, modifierGuid, addonsGuid, optionalsGuid, price, quantity, unit);
                }
            });
            showModifiersFragment();
        }
    };

    private BaseUpdateSaleItemAddonsCallback updateSaleItemAddonsCallback = new BaseUpdateSaleItemAddonsCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            startCommand(new DisplaySaleItemCommand(saleItemGuid));
        }
    };

    public static void start(Context context) {
        QuickServiceActivity_.intent(context).start();
    }

    public static void start4Return(Context context) {
        QuickServiceActivity_.intent(context).isCreateReturnOrder(true).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showQuickModifyFragment();
    }


    @ViewById
    protected CustomEditBox scannerInput;

    private Timer timer;

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

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {

    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {

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

    private void notifyHandler() {
        USBHandler.sendEmptyMessage(TIMES_UP);
    }

    private final int TIMES_UP = 0;
}
