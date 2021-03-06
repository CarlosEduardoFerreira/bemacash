package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand;
import com.kaching123.tcr.commands.store.saleorder.ItemsNegativeStockTrackingCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleItemAddonsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderAgeVeridiedCommand;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.fragment.itempick.ItemsListFragment;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.fragment.quickservice.AgeVerificationFragment;
import com.kaching123.tcr.fragment.quickservice.QuickCategoriesFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by mboychenko on 6/1/2017.
 */
@EActivity
public abstract class BaseQuickServiceActiviry extends BaseCashierActivity implements CustomEditBox.IKeyboardSupport {

    private final int TIMES_UP = 0;

    @ViewById
    protected KeyboardView keyboard;

    @ViewById
    protected CustomEditBox scannerInput;

    private Timer timer;

    private static final IntentFilter intentFilter = new IntentFilter();
    static {
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED);
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED);
    }

    protected boolean isVisiable = true;


    private void notifyHandler() {
        USBHandler.sendEmptyMessage(TIMES_UP);
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

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {

    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {

    }

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

        getSupportFragmentManager().beginTransaction().hide(getSearchResultFragment()).commit();

        ((ICategoryFragmentBaseActions)getCategoriesFragment()).setListener(new QuickCategoriesFragment.ICategoryListener() {

            @Override
            public void onCategoryChanged(long id, String depGuid, String catGuid) {
                ((IItemListFragmentBaseActions)getItemListFragment()).setCategory(catGuid);
            }
        });

        ((IItemListFragmentBaseActions)getItemListFragment()).setListener(new ItemsListFragment.IItemListener() {

            @Override
            public void onItemSelected(long id, final ItemExModel model) {
                tryToAddItem(model);
            }
        });
        ((IModifierFragmentBaseActions)getModifierFragment()).setCancelListener(new QuickModifyFragment.OnCancelListener() {

            @Override
            public void onFragmentCanceled() {
                hideModifiersFragment();
            }
        });
        hideModifiersFragment();
        scannerInput.requestFocus();
    }

    @Override
    protected void showEditItemModifiers(final String saleItemGuid, final String itemGuid) {
        ((IModifierFragmentBaseActions)getModifierFragment()).setupParams(itemGuid, saleItemGuid, new ItemModifiersFragment.OnAddonsChangedListener() {
            @Override
            public void onAddonsChanged(ArrayList<String> modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {
                hideModifiersFragment();
                UpdateSaleItemAddonsCommand.start(BaseQuickServiceActiviry.this,
                        saleItemGuid,
                        itemGuid,
                        modifierGuid,
                        addonsGuid,
                        optionalsGuid,
                        updateSaleItemAddonsCallback);
            }

            @Override
            public void onModifiersCountInsufficient(ModifierGroupModel group) {
                showModifiersInsufficientCountDialog(group);
            }
        });
        showModifiersFragment();
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
    public void onResume() {
        super.onResume();
        progressReceiver.register(BaseQuickServiceActiviry.this);
        isVisiable = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressReceiver.unregister(BaseQuickServiceActiviry.this);
        isVisiable = false;
    }

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED.equals(intent.getAction())) {
                if (!intent.getBooleanExtra(UploadTaskV2.EXTRA_SUCCESS, false))
                    if (intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE) != null && intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE).equalsIgnoreCase("400"))
                        Toast.makeText(BaseQuickServiceActiviry.this, R.string.warning_transaction_upload_fail, Toast.LENGTH_LONG).show();
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
        if (getModifierFragment() == null)
            return;
        if (isVisiable) {
            getSupportFragmentManager().beginTransaction().hide(getModifierFragment()).commit();
            getSupportFragmentManager().popBackStack();
            showTotalCostFragment();
        }
    }

    public void showModifiersFragment() {
        getSupportFragmentManager().beginTransaction().show(getModifierFragment()).addToBackStack(null).commit();
        hideTotalCostFragment();
    }

    @Override
    protected void tryToAddItem(final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        final SaleOrderModel saleOrderModel = getSaleOrderModel();
        if(model.ageVerification > 0 && saleOrderModel != null && saleOrderModel.getAgeVerified() > 0) {
            if (saleOrderModel.getAgeVerified() >= model.ageVerification) {
                continueAddingItem(model, price, quantity, unit);
                return;
            } else {
                Toast.makeText(BaseQuickServiceActiviry.this, getString(R.string.age_verification_customer_must_be_older, model.ageVerification), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if(model.ageVerification > 0) {
            AgeVerificationFragment.show(BaseQuickServiceActiviry.this, model.getGuid(), new AgeVerificationFragment.AgeVerifiedListener() {
                @Override
                public void onAgeVerified(int customerAge) {
                    if(saleOrderModel != null) {
                        saleOrderModel.setAgeVerified(customerAge);
                        UpdateSaleOrderAgeVeridiedCommand.start(BaseQuickServiceActiviry.this, saleOrderModel.getGuid(), customerAge);
                    } else {
                        model.setTmpAgeVerified(customerAge);
                    }
                    continueAddingItem(model, price, quantity, unit);
                }
            });
        } else {
            continueAddingItem(model, price, quantity, unit);
        }
    }

    private void continueAddingItem(final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit){
        if(!model.hasModificators() && !checkTrackedQty(model)){
            return;
        } else if (model.isAComposisiton) {
            ItemsNegativeStockTrackingCommand.start(BaseQuickServiceActiviry.this, model.getGuid(), ItemsNegativeStockTrackingCommand.ItemType.COMPOSITION, new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                @Override
                protected void handleSuccess(boolean result) {
                    if(!result){
                        Toast.makeText(BaseQuickServiceActiviry.this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CollectModifiersCommand.start(BaseQuickServiceActiviry.this, model.guid, null, price, model, quantity, unit, true, collectionCallback);
                }
            });
        } else {
            CollectModifiersCommand.start(this, model.guid, null, price, model, quantity, unit, true, collectionCallback);
        }
    }

    public CollectModifiersCommand.BaseCollectModifiersCallback collectionCallback = new CollectModifiersCommand.BaseCollectModifiersCallback() {
        @Override
        public void onCollected(final ArrayList<CollectModifiersCommand.SelectedModifierExModel> modifiers, final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit, boolean hasAutoApply) {

            if (hasAutoApply) {
                ItemsNegativeStockTrackingCommand.start(BaseQuickServiceActiviry.this, ItemsNegativeStockTrackingCommand.ItemType.MODIFIER, model.getGuid(),  getModifiers(modifiers), null, null,
                        new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                            @Override
                            protected void handleSuccess(boolean result) {
                                if (!result) {
                                    Toast.makeText(BaseQuickServiceActiviry.this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                tryToAddCheckPriceType(model, getModifiers(modifiers), null, null, price, quantity, unit);
                            }
                        });
                return;
            }

            boolean hasModifiers = model.modifiersCount > 0 || model.addonsCount > 0 || model.optionalCount > 0;
            if (!hasModifiers || !PlanOptions.isModifiersAllowed()) {
                tryToAddCheckPriceType(model, null, null, null, price, quantity, unit);
                return;
            }

            ((IModifierFragmentBaseActions)getModifierFragment()).setupParams(model.guid, new ItemModifiersFragment.OnAddonsChangedListener() {

                @Override
                public void onAddonsChanged(final ArrayList<String> modifierGuid, final ArrayList<String> addonsGuid, final ArrayList<String> optionalsGuid) {
                    hideModifiersFragment();
                    ItemsNegativeStockTrackingCommand.start(BaseQuickServiceActiviry.this, ItemsNegativeStockTrackingCommand.ItemType.MODIFIER, model.getGuid(), modifierGuid, addonsGuid, optionalsGuid,
                            new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                                @Override
                                protected void handleSuccess(boolean result) {
                                    if (!result) {
                                        Toast.makeText(BaseQuickServiceActiviry.this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    tryToAddCheckPriceType(model, modifierGuid, addonsGuid, optionalsGuid, price, quantity, unit);
                                }
                            });
                }

                @Override
                public void onModifiersCountInsufficient(ModifierGroupModel group) {
                    showModifiersInsufficientCountDialog(group);
                }
            });
            showModifiersFragment();
        }
    };

    private UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback updateSaleItemAddonsCallback = new UpdateSaleItemAddonsCommand.BaseUpdateSaleItemAddonsCallback() {
        @Override
        protected void onSuccess(String saleItemGuid) {
            startCommand(new DisplaySaleItemCommand(saleItemGuid));
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showTotalCostFragment();
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

    protected abstract Fragment getModifierFragment();
    protected abstract Fragment getItemListFragment();
    protected abstract Fragment getCategoriesFragment();

    public interface ICategoryFragmentBaseActions<T extends BaseCategoriesFragment.ICategoryListener> {
        void setListener(T listener);
    }
    public interface IItemListFragmentBaseActions {
        void setListener(ItemsListFragment.IItemListener listener);
        void setCategory(String guid);
    }
    public interface IModifierFragmentBaseActions {
        void setCancelListener(QuickModifyFragment.OnCancelListener listener);
        void setupParams(String itemGuid, String saleItemGuid, ItemModifiersFragment.OnAddonsChangedListener listener);
        void setupParams(String itemGuid, ItemModifiersFragment.OnAddonsChangedListener listener);
    }
}
