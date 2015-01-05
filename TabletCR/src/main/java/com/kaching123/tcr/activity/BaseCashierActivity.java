package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidProcessorActivity;
import com.kaching123.tcr.commands.device.GetPrinterStatusCommand;
import com.kaching123.tcr.commands.device.GetPrinterStatusCommand.BasePrinterStatusCallback;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.commands.local.EndTransactionCommand;
import com.kaching123.tcr.commands.local.StartTransactionCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackRefundCommand;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackSaleCommand;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackVoidCommand;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.commands.print.pos.PrintOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.AddItem2SaleOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.AddItem2SaleOrderCommand.BaseAddItem2SaleOrderCallback;
import com.kaching123.tcr.commands.store.saleorder.AddSaleOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.AddSaleOrderCommand.BaseAddSaleOrderCommandCallback;
import com.kaching123.tcr.commands.store.saleorder.GetItemsForFakeVoidCommand;
import com.kaching123.tcr.commands.store.saleorder.GetItemsForFakeVoidCommand.BaseGetItemsForFaickVoidCallback;
import com.kaching123.tcr.commands.store.saleorder.HoldOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.HoldOrderCommand.BaseHoldOrderCallback;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.RevertSuccessOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.SuccessOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.SuccessOrderCommand.BaseSuccessOrderCommandCallback;
import com.kaching123.tcr.commands.store.user.ClockInCommand;
import com.kaching123.tcr.commands.store.user.ClockInCommand.BaseClockInCallback;
import com.kaching123.tcr.commands.wireless.UnitOrderDoubleCheckCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.barcode.SearchBarcodeFragment;
import com.kaching123.tcr.fragment.barcode.SearchBarcodeLoader;
import com.kaching123.tcr.fragment.commission.CommissionDialog;
import com.kaching123.tcr.fragment.commission.CommissionDialog.ICommissionDialogListener;
import com.kaching123.tcr.fragment.data.MsrDataFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.edit.PriceEditFragment;
import com.kaching123.tcr.fragment.edit.PriceEditFragment.OnEditPriceListener;
import com.kaching123.tcr.fragment.edit.SaleOrderDiscountEditFragment;
import com.kaching123.tcr.fragment.edit.TaxEditFragment;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment.OnAddonsChangedListener;
import com.kaching123.tcr.fragment.modify.ModifyFragment;
import com.kaching123.tcr.fragment.saleorder.HoldFragmentDialog;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment.IItemsListHandlerHandler;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment.IOrderActionListener;
import com.kaching123.tcr.fragment.search.SearchItemsListFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment.RefundAmount;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.fragment.user.TimesheetFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.fragment.wireless.UnitsSaleFragment;
import com.kaching123.tcr.model.BarcodeListenerHolder;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoFullRefundResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.RefundResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.processor.MoneybackProcessor;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.processor.PaxBalanceProcessor;
import com.kaching123.tcr.processor.PaymentProcessor;
import com.kaching123.tcr.processor.PaymentProcessor.IPaymentProcessor;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.service.DisplayService;
import com.kaching123.tcr.service.DisplayService.Command;
import com.kaching123.tcr.service.DisplayService.DisplayBinder;
import com.kaching123.tcr.service.DisplayService.DisplayListener;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.util.DateUtils;
import com.kaching123.tcr.util.KeyboardUtils;
import com.telly.groundy.annotations.OnCancel;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EActivity
public abstract class BaseCashierActivity extends ScannerBaseActivity implements IOrderActionListener, IDisplayBinder, BarcodeListenerHolder {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.SALES_TRANSACTION);
    }

    private static final Uri ORDER_URI = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);
    private static final Uri PAYMENTS_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private static final int LOADER_ORDER_TITLE = 1;
    private static final int LOADER_ORDERS_COUNT = 2;
    private static final int LOADER_CHECK_ORDER = 3;
    private static final int LOADER_CHECK_ORDER_PAYMENTS = 4;
    private static final int LOADER_SEARCH_BARCODE = 10;

    private int barcodeLoaderId = LOADER_SEARCH_BARCODE;

    private static final long BACK_TIMEOUT = 2000L;
    private long lastBackPressedTime;

    @FragmentById
    protected TotalCostFragment totalCostFragment;

    @FragmentById
    protected OrderItemListFragment orderItemListFragment;

    @FragmentById
    protected SearchItemsListFragment searchResultFragment;

    @ViewById
    protected View scannerWaitBlock;

    protected MenuItem searchItem;

    private MenuItem holdCounterItem;
    private TextView holdCounterView;

    private OrderInfoLoader orderInfoLoader = new OrderInfoLoader();
    private OrdersCountLoader ordersCountLoader = new OrdersCountLoader();
    private AddItem2SaleOrderCallback addItemCallback = new AddItem2SaleOrderCallback();
    private AddSaleOrderCallback addOrderCallback = new AddSaleOrderCallback();
    private PrinterStatusCallback printerStatusCallback = new PrinterStatusCallback();
    private CheckOrderPaymentsLoader checkOrderPaymentsLoader = new CheckOrderPaymentsLoader();

    private String orderGuid;
    private SaleOrderModel saleOrderModel;
    private String orderTitle;
    private OrdersStatInfo ordersCount;
    private Ringtone alarmRingtone;

    private DisplayBinder displayBinder;

    @Extra
    protected boolean isCreateReturnOrder;

    private BarcodeListener barcodeListener;

    private boolean isPaying;

    private CharSequence title;

    private ArrayList<SaleOrderItemModelWrapper> waitList = new ArrayList<SaleOrderItemModelWrapper>();

    private boolean creatingOrder;


    private HashSet<String> salesmanGuids = new HashSet<String>();

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public void setBarcodeListener(BarcodeListener barcodeListener) {
        this.barcodeListener = barcodeListener;
    }

    @Override
    public void setDefaultBarcodeListener() {
        setBarcodeListener(defaultBarcodeListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindToDisplayService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindFromDisplayService();

        if (!isFinishing())
            return;

        if (isCreateReturnOrder && !TextUtils.isEmpty(orderGuid)) {
            RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
        }
    }

    private void bindToDisplayService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().displayAddress().get());

        if (displayConfigured)
            DisplayService.bind(this, displayServiceConnection);
    }

    private void unbindFromDisplayService() {
        startCommand(new DisplayWelcomeMessageCommand());

        if (displayBinder != null) {
            displayBinder = null;
            unbindService(displayServiceConnection);
        }
    }

    @Override
    public void startCommand(Command displayCommand) {
        if (displayBinder != null)
            displayBinder.startCommand(displayCommand);
    }

    @Override
    public void setDisplayListener(DisplayListener displayListener) {
        if (displayBinder != null)
            displayBinder.setDisplayListener(displayListener);
    }

    @Override
    public void tryReconnectDisplay() {
        if (displayBinder != null)
            displayBinder.tryReconnectDisplay();
    }

    @AfterViews
    protected void init() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getSupportFragmentManager().beginTransaction().hide(searchResultFragment).commit();

        orderItemListFragment.setItemsListHandler(new IItemsListHandlerHandler() {

/*
            @Override
            public void onBarcodeSearched(ItemExModel item, String barcode) {
                tryToAddByBarcode(item, barcode);
            }
*/

            @Override
            public void onEditItemModifiers(String saleItemGuid, String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, String selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {
                showEditItemModifiers(
                        saleItemGuid,
                        itemGuid,
                        modifiersCount,
                        addonsCount,
                        optionalsCount,
                        selectedModifierGuid,
                        selectedAddonsGuids,
                        selectedOptionalsGuids
                );
            }

            @Override
            public void onRemoveLastItem() {
                RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
            }

            @Override
            public void onOrderLoaded(SaleOrderItemViewModel lastItem) {
                if (lastItem == null) {
                    startCommand(new DisplayWelcomeMessageCommand());
                } else {
                    startCommand(new DisplaySaleItemCommand(lastItem.getSaleItemGuid()));
                }
            }
        });

        searchResultFragment.setListener(new SearchItemsListFragment.IItemListener() {

            @Override
            public void onItemSelected(long id, ItemExModel model) {
                tryToAddItem(model);
                closeSearch();
            }
        });

        initTitle();
        updateTitle(null);

        String curOrderGuid = getApp().getCurrentOrderGuid();
        if (!TextUtils.isEmpty(curOrderGuid)) {
            try2LoadActiveOrder(curOrderGuid);
        }
        totalCostFragment.setCreateReturnOrder(isCreateReturnOrder);
        orderItemListFragment.setCreateReturnOrder(isCreateReturnOrder);

        setDefaultBarcodeListener();
        //ScannerProcessor.get(barcodeListener).start();
    }

    private void initTitle() {
        if (!isCreateReturnOrder) {
            title = getString(R.string.cashier_title_new_order);
        } else {
            SpannableString titleString = new SpannableString(getString(R.string.cashier_title_return_items));
            titleString.setSpan(new ForegroundColorSpan(Color.RED), 0, titleString.length(), 0);
            titleString.setSpan(new StyleSpan(Typeface.BOLD), 0, titleString.length(), 0);
            title = titleString;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        alarmRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

        Fragment frm = getSupportFragmentManager().findFragmentByTag(MsrDataFragment.FTAG);
        if (frm == null) {
            getSupportFragmentManager().beginTransaction().add(MsrDataFragment.newInstance(), MsrDataFragment.FTAG).commit();
        }
    }

    protected abstract void showEditItemModifiers(final String saleItemGuid, final String itemGuid, final int modifiersCount, final int addonsCount, final int optionalsCount, final String selectedModifierGuid, final ArrayList<String> selectedAddonsGuids, final ArrayList<String> selectedOptionalsGuids);

    protected void tryToAddItem(final ItemExModel model) {
        tryToAddItem(model, null, null, null);
    }

    protected void tryToAddItem(final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        boolean hasModifiers = model.modifiersCount > 0 || model.addonsCount > 0 || model.optionalCount > 0;
        if (!hasModifiers) {
            tryToAddCheckPriceType(model, null, null, null, price, quantity, unit);
            return;
        }
        ModifyFragment.show(this,
                model.guid,
                model.modifiersCount,
                model.addonsCount,
                model.optionalCount,
                model.defaultModifierGuid,
                new OnAddonsChangedListener() {
                    @Override
                    public void onAddonsChanged(String modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid) {
                        tryToAddCheckPriceType(model, modifierGuid, addonsGuid, optionalsGuid, price, quantity, unit);
                    }
                }
        );
    }

    protected void tryToAddCheckPriceType(final ItemExModel model,
                                          final String modifierGiud,
                                          final ArrayList<String> addonsGuids,
                                          final ArrayList<String> optionalGuids,
                                          final BigDecimal price,
                                          final BigDecimal quantity,
                                          final Unit unit) {
        if (PriceType.OPEN == model.priceType && price == null) {
            PriceEditFragment.show(this, model.price, new OnEditPriceListener() {

                @Override
                public void onConfirm(BigDecimal value) {
                    model.price = value;
                    addItemModel(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                    closeAllPickers();
                }
            });
        } else {
            addItemModel(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
            closeAllPickers();
        }
    }

    protected void tryToAddByBarcode(final ItemExModel item, final String barcode, BigDecimal price, BigDecimal quantity, final boolean fromScanner, Unit unit) {
        if (item == null) {
            Logger.d("Scanner: tryToAddByBarcode - show no item dialog ");
            if (fromScanner)
                disconnectScanner();
            playAlarm();
            AlertDialogFragment.show(BaseCashierActivity.this,
                    DialogType.CONFIRM_NONE,
                    R.string.dlg_search_barcode_err_title,
                    getString(R.string.dlg_search_barcode_err_msg, barcode),
                    R.string.btn_add,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            if (fromScanner)
                                tryReconnectScanner();
                            boolean inventoryPermitted = getApp().hasPermission(Permission.INVENTORY_MODULE);
                            if (!inventoryPermitted) {
                                PermissionFragment.showCancelable(BaseCashierActivity.this, new BaseTempLoginListener(BaseCashierActivity.this) {
                                    @Override
                                    public void onLoginComplete() {
                                        super.onLoginComplete();
                                        AddItemActivity.start(BaseCashierActivity.this, barcode);
                                    }
                                }, Permission.INVENTORY_MODULE);
                                return true;
                            }
                            AddItemActivity.start(BaseCashierActivity.this, barcode);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            if (fromScanner)
                                tryReconnectScanner();
                            return true;
                        }
                    }, null
            );
            return;
        }

        if (!item.isActiveStatus) {
            Logger.d("Scanner: tryToAddByBarcode - show item not active dialog ");
            if (fromScanner)
                disconnectScanner();
            playAlarm();
            AlertDialogFragment.show(BaseCashierActivity.this,
                    DialogType.CONFIRM_NONE,
                    R.string.dlg_search_barcode_err_not_active_title,
                    getString(R.string.dlg_search_barcode_err_not_active_msg, barcode),
                    R.string.btn_edit,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            if (fromScanner)
                                tryReconnectScanner();
                            boolean inventoryPermitted = getApp().hasPermission(Permission.INVENTORY_MODULE);
                            if (!inventoryPermitted) {
                                PermissionFragment.showCancelable(BaseCashierActivity.this, new BaseTempLoginListener(BaseCashierActivity.this) {
                                    @Override
                                    public void onLoginComplete() {
                                        super.onLoginComplete();
                                        EditItemActivity.start(BaseCashierActivity.this, item);
                                    }
                                }, Permission.INVENTORY_MODULE);
                                return true;
                            }
                            EditItemActivity.start(BaseCashierActivity.this, item);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            if (fromScanner)
                                tryReconnectScanner();
                            return true;
                        }
                    }, null
            );
            return;
        }

//        if (item.serializable && item.tmpUnit.size() == 0) {
//            playAlarm();
//            tryToAddSerializedItem(item, null, null, null, price, quantity, false);
//            return;
//        }


        Logger.d("Scanner: tryToAddByBarcode - addItemDiscount item");
        tryToAddItem(item, price, quantity, unit);
    }

    private void playAlarm() {
        if (alarmRingtone == null || alarmRingtone.isPlaying())
            return;
        alarmRingtone.play();
    }

    protected void closeAllPickers() {
        closeSearch();
    }

    protected void setOrderGuid(String newOrderGuid, boolean clearSalesmans) {
        getApp().setCurrentOrderGuid(newOrderGuid);
        getApp().setSalesmanGuids(salesmanGuids);
        this.orderGuid = newOrderGuid;
        orderItemListFragment.setOrderGuid(this.orderGuid);
        totalCostFragment.setOrderGuid(this.orderGuid);
        if (clearSalesmans) {
            salesmanGuids.clear();
            if (getApp().getOperator().commissionEligible) {
                salesmanGuids.add(getApp().getOperatorGuid());
            }
        }

        if (isFinishing() || isDestroyed())
            return;

        supportInvalidateOptionsMenu();

        updateHoldButton();

        if (this.orderGuid == null) {
            getSupportLoaderManager().destroyLoader(LOADER_ORDER_TITLE);
        } else {
            getSupportLoaderManager().restartLoader(LOADER_ORDER_TITLE, null, orderInfoLoader);
        }
        getSupportLoaderManager().restartLoader(LOADER_ORDERS_COUNT, null, ordersCountLoader);
    }

    private void updateTitle(SaleOrderModel saleOrderModel) {
        CharSequence title;
        this.saleOrderModel = saleOrderModel;
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }

        if (saleOrderModel != null) {
            this.orderTitle = saleOrderModel.getHoldName();
        }
        if (TextUtils.isEmpty(this.orderGuid)) {
            this.orderTitle = null;
            title = this.title;
        } else {
            title = getString(R.string.cashier_order_title_tmpl, orderTitle);
        }
        if (getApp().isTrainingMode()) {
            SpannableString spannableTitle = new SpannableString(getString(R.string.training_mode_title_tmpl, title));
            spannableTitle.setSpan(new ForegroundColorSpan(Color.RED), title.length(), spannableTitle.length(), 0);
            title = spannableTitle;
        }
        actionBar.setTitle(title);
        if (lockItem != null)
            updatePrintMenuItem();

    }

    private void updateHoldButton() {
        totalCostFragment.setSuspendedItemsCount(this.ordersCount == null ? 0 : this.ordersCount.totalCounts);
    }

    private void enableHoldCounterIcon() {
        enableHoldCounterIcon(TextUtils.isEmpty(this.orderGuid));
    }

    private void enableHoldCounterIcon(boolean enable) {
        holdCounterItem.setEnabled(enable);
        assert holdCounterItem.getActionView() != null;
        holdCounterItem.getActionView().setEnabled(enable);
        holdCounterItem.getActionView().setActivated(enable);
    }

    private void updateVisibilityHoldCounterIcon() {
        if (holdCounterItem == null) {
            return;
        }
        holdCounterItem.setVisible(!isCreateReturnOrder && this.ordersCount != null && !this.ordersCount.isEmpty());
    }

    private void updateCounter(OrdersStatInfo info) {
        this.ordersCount = info;
        if (holdCounterView == null) {
            supportInvalidateOptionsMenu();
            return;
        }
        checkHoldInfo();
    }

    /**
     * search fragment **
     */
    protected void showSearchFragment() {
        getSupportFragmentManager().beginTransaction().show(searchResultFragment).commit();
    }

    private void hideSearchFragment() {
        getSupportFragmentManager().beginTransaction().hide(searchResultFragment).commit();
    }

    private void closeSearch() {
        if (searchItem == null)
            return;
        searchItem.collapseActionView();
    }

    private void filterSearchFragment(String text) {
        searchResultFragment.setSearchText(text);
    }
    /*** search fragment end ***/

    /**
     * * menu start ***
     */
    protected PrintOrderCallback printOrderCallback = new PrintOrderCallback();

    public class PrintOrderCallback extends BasePrintCommand.BasePrintCallback {

        PrintCallbackHelper2.IPrintCallback callback = new PrintCallbackHelper2.IPrintCallback() {
            @Override
            public void onRetry(boolean searchByMac, boolean ignorePaperEnd) {
//                printOrder(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };

        @Override
        public void onPrintSuccess() {
//            orderPrinted = true;
//            printReceipts();
            WaitDialogFragment.hide(BaseCashierActivity.this);
        }

        @Override
        public void onPrintError(PrinterCommand.PrinterError errorType) {
            PrintCallbackHelper2.onPrintError(BaseCashierActivity.this, errorType, callback);
        }

        @Override
        public void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(BaseCashierActivity.this, callback);
        }

        @Override
        public void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(BaseCashierActivity.this, callback);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(BaseCashierActivity.this, callback);
        }

        @Override
        public void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(BaseCashierActivity.this, callback);
        }

    }

    private MenuItem lockItem;

    private void updatePrintMenuItem() {
        if (orderTitle == null || orderTitle.equalsIgnoreCase("New order"))
            lockItem.setEnabled(false);
        else
            lockItem.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        lockItem = menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, getResources().getInteger(R.integer.menu_order_last), R.string.action_print_label);
        updatePrintMenuItem();
        lockItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                WaitDialogFragment.show(BaseCashierActivity.this, getString(R.string.wait_printing));
                PrintOrderCommand.start(BaseCashierActivity.this, false, false, orderGuid, printOrderCallback, orderTitle, totalCostFragment.getOrderSubTotal(), totalCostFragment.getOrderDiscountTotal(), totalCostFragment.getOrderTaxTotal(), totalCostFragment.getOrderAmountTotal());
                return true;
            }
        });
        boolean b = super.onCreateOptionsMenu(menu);
        searchItem = menu.findItem(R.id.action_search);
        assert searchItem != null;
        initSearchView();

        holdCounterItem = menu.findItem(R.id.action_hold_counter);
        assert holdCounterItem != null && holdCounterItem.getActionView() != null;

        holdCounterItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionHoldCounterSelected();
            }
        });
        holdCounterView = (TextView) holdCounterItem.getActionView().findViewById(R.id.ab_counter);
        assert holdCounterView != null;


        getSupportLoaderManager().restartLoader(LOADER_ORDERS_COUNT, null, ordersCountLoader);
        return b;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        checkHoldInfo();
        menu.findItem(R.id.action_prepaid).setEnabled(getApp().isPrepaidUserValid());
        menu.findItem(R.id.action_order_items).setVisible(!isCreateReturnOrder);
        menu.findItem(R.id.action_prepaid).setVisible(!isCreateReturnOrder);
        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX_EBT_CASH.gateway();
        menu.findItem(R.id.action_balance).setVisible(getApp().isPaxConfigured() && paxGateway.acceptPaxEbtEnabled());
        menu.findItem(R.id.action_commission).setVisible(getApp().isCommissionsEnabled() && !isCreateReturnOrder);
        return true;
    }

    private void checkHoldInfo() {
        enableHoldCounterIcon();
        updateVisibilityHoldCounterIcon();
        updateHoldButton();
        if (ordersCount != null && holdCounterView != null) {
            holdCounterView.setText(String.valueOf(ordersCount.totalCounts));
        }
    }

    @OptionsItem
    protected void actionBalanceSelected() {
        actionBarItemClicked();
        PaxBalanceProcessor.get().checkBalance(this);
    }

    protected void initSearchView() {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideSearchFragment();
                return true;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionBarItemClicked();
                showSearchFragment();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                KeyboardUtils.hideKeyboard(BaseCashierActivity.this, searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSearchFragment(newText);
                return true;
            }
        });
    }

    protected void actionHoldCounterSelected() {
        actionBarItemClicked();
        HoldFragmentDialog.show(this, null, null, false, new HoldFragmentDialog.IHoldListener() {
            @Override
            public void onSwap2Order(String holdName, String nextOrderGuid) {
                setOrderGuid(nextOrderGuid, true);
            }
        });
    }

    protected void tryToSearchBarCode(TextView scannerInput) {
        String barcode = scannerInput.getText().toString();
        scannerInput.setText(null);
        tryToSearchBarCode(barcode, false);
    }

    private void itemPerfect(final String barcode, final boolean fromScanner,
                             final ItemExModel itemExModel, final Unit unit,
                             final BigDecimal price, final BigDecimal quantity) {
        tryToAddByBarcode(itemExModel, barcode, price, quantity, fromScanner, unit);
    }

    private void itemNotPerfect(final String barcode, final boolean fromScanner,
                                final ItemExModel itemExModel, final Unit unit,
                                final BigDecimal price, final BigDecimal quantity) {
        AlertDialogWithCancelFragment.showWithTwo(BaseCashierActivity.this,
                R.string.wireless_already_item_title,
                "This item has been marked as " + unit.status.toString() + ". Would you like to sell it again?",
                R.string.btn_confirm,
                new AlertDialogWithCancelFragment.OnDialogListener() {
                    @Override
                    public boolean onClick() {
                        itemPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                        return true;
                    }

                    @Override
                    public boolean onCancel() {
                        return true;
                    }
                }
        );
    }

    private void itemAlreadyIn() {
        AlertDialogWithCancelFragment.show(BaseCashierActivity.this,
                R.string.wireless_already_item_title,
                getString(R.string.wireless_already_in_label),
                R.string.btn_ok,
                new AlertDialogWithCancelFragment.OnDialogListener() {
                    @Override
                    public boolean onClick() {
                        tryReconnectScanner();
                        return true;
                    }

                    @Override
                    public boolean onCancel() {
                        tryReconnectScanner();
                        return true;
                    }
                }
        );
    }

    private void doubleCheck(final String barcode, final boolean fromScanner,
                             final ItemExModel itemExModel, final Unit unit,
                             final BigDecimal price, final BigDecimal quantity) {
        if (unit.orderId.equals(orderGuid)) {
            itemAlreadyIn();
            return;
        }
        UnitOrderDoubleCheckCommand.start(BaseCashierActivity.this, unit, orderGuid, new UnitOrderDoubleCheckCommand.UnitCallback() {
            @Override
            protected void handleSuccess() {

                AlertDialogWithCancelFragment.showWithTwo(BaseCashierActivity.this,
                        R.string.wireless_already_item_title,
                        "This item was bound to some other order, which is currently not completed. Would you like to sell it anyway?",
                        R.string.btn_ok,
                        new AlertDialogWithCancelFragment.OnDialogListener() {
                            @Override
                            public boolean onClick() {
                                if (!unit.status.equals(Unit.Status.NEW)) {
                                    itemNotPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                                } else {
                                    itemPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                                }
                                return true;
                            }

                            @Override
                            public boolean onCancel() {
                                return true;
                            }
                        }
                );
            }

            @Override
            protected void handleError() {
                disconnectScanner();
                itemAlreadyIn();
            }

            @Override
            protected void handleFeelFreeToAdd() {
                if (!unit.status.equals(Unit.Status.NEW)) {
                    itemNotPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                } else {
                    itemPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                }
            }
        });

    }

    protected void tryToSearchBarCode(final String barcode, final boolean fromScanner) {
        if (barcode.length() >= TcrApplication.BARCODE_MIN_LEN) {
            new SearchBarcodeLoader(this, nextBarcodeLoaderId(), barcode) {

                @Override
                protected void onPreExecute() {
                    showScannerWaitBlock(true);
                }

                @Override
                protected void onMultipleSerialCodeFound(final List<Unit> unitList, final ItemExModel model) {
                    showScannerWaitBlock(false);
                    AlertDialogFragment.showNotification(BaseCashierActivity.this,
                            R.string.dlg_multiple_serial_codes_title,
                            getResources().getString(R.string.dlg_multiple_serial_codes_msg));
                }

                @Override
                protected void onPostExecute(final ItemExModel itemExModel, final Unit unit,
                                             final BigDecimal price, final BigDecimal quantity) {
                    showScannerWaitBlock(false);
                    if (unit.orderId == null) {

                        if (!unit.status.equals(Unit.Status.NEW)) {
                            itemNotPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                        } else {
                            itemPerfect(barcode, fromScanner, itemExModel, unit, price, quantity);
                        }

                    } else {
                        doubleCheck(barcode, fromScanner, itemExModel, unit, price, quantity);
                    }
                }

                @Override
                protected void onPostExecute(ItemExModel itemExModel, BigDecimal price, BigDecimal quantity) {
                    showScannerWaitBlock(false);
                    tryToAddByBarcode(itemExModel, barcode, price, quantity, fromScanner, null);
                }
            }.execute();
        } else {
            showScannerWaitBlock(false);
        }
    }

    private int nextBarcodeLoaderId() {
        if (barcodeLoaderId == Integer.MAX_VALUE)
            barcodeLoaderId = LOADER_SEARCH_BARCODE;

        return barcodeLoaderId++;
    }

    private void showScannerWaitBlock(boolean visible) {
        scannerWaitBlock.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void actionBarItemClicked() {

    }

    @OptionsItem
    protected void actionPrepaidSelected() {
        if (!getApp().isOperatorClockedIn() && getApp().getShopInfo().clockinRequired4Sales) {
            try2ClockIn();
            return;
        }
        actionBarItemClicked();
//        PrepaidProcessor.create().init(this);
        PrepaidProcessorActivity.start(this);
    }


    @OptionsItem
    protected void actionBarcodeSelected() {
        actionBarItemClicked();
        SearchBarcodeFragment.show(this, new SearchBarcodeFragment.OnSearchListener() {

            @Override
            public void onBarcodeSearch(ItemExModel item, final String barcode, BigDecimal price, BigDecimal quantity) {
                tryToAddByBarcode(item, barcode, price, quantity, false, null);
            }
        });
    }

    @OptionsItem
    protected void actionOrderItemsSelected() {
        actionBarItemClicked();
        boolean salesReturnPermitted = getApp().hasPermission(Permission.SALES_RETURN);
        if (!salesReturnPermitted) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    actionOrderItemsSelected();
                }
            }, Permission.SALES_RETURN);
            return;
        }
        Logger.d("Lets show some history");
        MsrDataFragment msr = getMsr();
        if (msr != null) {
            msr.releaseNow();
        }
        HistoryActivity.start(BaseCashierActivity.this);
    }

    protected MsrDataFragment getMsr() {
        return (MsrDataFragment) getSupportFragmentManager().findFragmentByTag(MsrDataFragment.FTAG);
    }

    @OptionsItem
    protected void actionCommissionSelected() {
        CommissionDialog.show(this, salesmanGuids, new ICommissionDialogListener() {
            @Override
            public void onSalesmanPicked(HashSet<String> salesmanGuids) {
                BaseCashierActivity.this.salesmanGuids = salesmanGuids;
            }
        });
    }

    /**
     * * menu end ***
     */

    private void addItemModel(ItemExModel model, String modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, Unit unit) {
        addItemModel(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, false, unit);
    }

    private void generateOrderId() {
        if (!creatingOrder) {
            AddSaleOrderCommand.start(this, null, true, addOrderCallback);
            creatingOrder = true;
        }
    }

    private void tryToAddSerializedItem(final ItemExModel model, final String modifierGiud, final ArrayList<String> addonsGuids,
                                        final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final boolean checkDrawerState) {
        UnitsSaleFragment.show(BaseCashierActivity.this, model, null, UnitsSaleFragment.UnitActionType.ADD_TO_ORDER,
                model.codeType, new UnitsSaleFragment.UnitSaleCallback() {

            @Override
            public void handleSuccess(final Unit unit) {
                Toast.makeText(BaseCashierActivity.this, getString(R.string.unit_edit_completed), Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addItem(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, checkDrawerState, unit);
                    }
                });
                hide();
            }

            @Override
            public void handleError(String message) {
                disconnectScanner();
                AlertDialogWithCancelFragment.show(BaseCashierActivity.this,
                        R.string.wireless_already_item_title,
                        message,
                        R.string.btn_ok,
                        new AlertDialogWithCancelFragment.OnDialogListener() {
                            @Override
                            public boolean onClick() {
                                tryReconnectScanner();
                                return true;
                            }

                            @Override
                            public boolean onCancel() {
                                tryReconnectScanner();
                                return true;
                            }
                        }
                );
                hide();
            }

            @Override
            public void handleCancelling() {
            }

            private void hide() {
                UnitsSaleFragment.hide(BaseCashierActivity.this);
            }

        });
    }

    private void addItemModel(final ItemExModel model, final String modifierGiud, final ArrayList<String> addonsGuids,
                              final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final boolean checkDrawerState, Unit unit) {
        if (model == null)
            return;

        if (model.serializable && model.tmpUnit.size() == 0) {
            tryToAddSerializedItem(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, checkDrawerState);
            return;
        }
        addItem(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, checkDrawerState, unit);
    }


    private void addItem(ItemExModel model, String modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, boolean checkDrawerState, Unit unit) {
        if (checkDrawerState && !checkDrawerState(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit))
            return;

        orderItemListFragment.setNeed2ScrollList(true);

        SaleOrderItemModel itemModel = new SaleOrderItemModel(
                UUID.randomUUID().toString(),
                this.orderGuid,
                model.guid,
                quantity == null ? BigDecimal.ONE : quantity,
                BigDecimal.ZERO,
                isCreateReturnOrder ? PriceType.OPEN : model.priceType,
                price == null ? model.price : price,
                isCreateReturnOrder || model.isDiscountable,
                model.isDiscountable ? model.discount : null,
                model.isDiscountable ? model.discountType : null,
                model.isTaxable,
                TextUtils.isEmpty(model.taxGroupGuid) ? getApp().getTaxVat() : model.tax,
                0,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                !isCreateReturnOrder && model.hasNotes);

        if (unit != null && orderGuid != null) {
            unit.orderId = orderGuid;
        }

        if (this.orderGuid == null) {
            waitList.add(new SaleOrderItemModelWrapper(itemModel, modifierGiud, addonsGuids, optionalGuids, unit));
            generateOrderId();
            return;
        }

        AddItem2SaleOrderCommand.start(this,
                addItemCallback,
                itemModel,
                modifierGiud,
                addonsGuids,
                optionalGuids,
                unit
        );
    }

    private boolean checkDrawerState(ItemExModel model, String modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, Unit unit) {
        boolean creatingOrder = TextUtils.isEmpty(this.orderGuid);
        if (creatingOrder && getApp().getShopInfo().drawerClosedForSale) {
            WaitDialogFragment.show(this, getString(R.string.check_drawer_state_wait_dialog_message));

            printerStatusCallback.setItem(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
            checkDrawerState(false);
            return false;
        }
        printerStatusCallback.clearItem();

        return true;
    }

    private void checkDrawerState(boolean searchByMac){
        GetPrinterStatusCommand.start(this, searchByMac, printerStatusCallback);
    }

    private void skipDrawerStateCheck(final ItemExModel model, final String modifierGiud, final ArrayList<String> addonsGuids, final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        if (!getApp().hasPermission(Permission.DROPS_AND_PAYOUTS)) {
            PermissionFragment.showCancelable(BaseCashierActivity.this,
                    new OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            showDrawerStateErrorDialog(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                            return true;
                        }
                    },
                    new BaseTempLoginListener(BaseCashierActivity.this) {
                        @Override
                        public void onLoginComplete() {
                            super.onLoginComplete();
                            skipDrawerStateCheck(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                        }
                    }, Permission.DROPS_AND_PAYOUTS
            );
            return;
        }
        addItemModel(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, false, unit);
    }

    private void showDrawerStateErrorDialog(final ItemExModel itemModel, final String modifierGiud, final ArrayList<String> addonsGuids, final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        AlertDialogFragment.showAlertWithSkip(BaseCashierActivity.this, R.string.error_dialog_title, getString(R.string.check_drawer_state_error_dialog_message), R.string.btn_retry,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        addItemModel(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        skipDrawerStateCheck(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                        return true;
                    }
                }
        );
    }

    /**
     * buttons block **
     */
    @Override
    public void onPay() {
        if (!getApp().isOperatorClockedIn() && getApp().getShopInfo().clockinRequired4Sales) {
            try2ClockIn();
            return;
        }
        if (saleOrderModel == null || TextUtils.isEmpty(orderGuid)) {
            Toast.makeText(this, "Please select an order to continue", Toast.LENGTH_LONG).show();
        } else if (isCreateReturnOrder) {
            //TODO super void here
            WaitDialogFragment.show(this, getString(R.string.wait_message_manual_return));
            StartTransactionCommand.start(BaseCashierActivity.this);
            SuccessOrderCommand.start(this, orderGuid, isCreateReturnOrder, new SuccessOrderCommand4ReturnCallback());
        } else if (!isPaying) {
            isPaying = true;
            StartTransactionCommand.start(BaseCashierActivity.this, orderGuid);
            PaymentProcessor.create(orderGuid, OrderType.SALE, saleOrderModel.kitchenPrintStatus, salesmanGuids.toArray(new String[salesmanGuids.size()]))
                    .callback(new IPaymentProcessor() {

                        @Override
                        public void onSuccess() {
                            EndTransactionCommand.start(BaseCashierActivity.this, true);
                            isPaying = false;
                            completeOrder();
                            checkOfflineMode();
                        }

                        @Override
                        public void onCancel() {
                            EndTransactionCommand.start(BaseCashierActivity.this);
                            isPaying = false;
                            SaleOrderItemViewModel lastItem = orderItemListFragment == null ? null : orderItemListFragment.getLastItem();
                            if (lastItem == null) {
                                startCommand(new DisplayWelcomeMessageCommand());
                            } else {
                                startCommand(new DisplaySaleItemCommand(lastItem.getSaleItemGuid()));
                            }
                            checkOfflineMode();
                        }

                        @Override
                        public void onPrintValues(String order, ArrayList<PaymentTransactionModel> list, BigDecimal changeAmount) {

                        }
                    })
                    .init(this);
        }
    }

    private void try2ClockIn() {
        TimesheetFragment.show(this, TimesheetFragment.Type.CLOCK_IN, getString(R.string.timesheet_dialog_clarify_message), new TimesheetFragment.OnTimesheetListener() {
            @Override
            public void onCredentialsEntered(String login/*, String password*/) {
                TimesheetFragment.hide(BaseCashierActivity.this);
                WaitDialogFragment.show(BaseCashierActivity.this, getString(R.string.wait_message_clock_in));
                ClockInCommand.start(BaseCashierActivity.this, login/*, password*/, new BaseClockInCallback() {
                    @Override
                    protected void onClockIn(String guid, String fullName, Date time) {
                        WaitDialogFragment.hide(BaseCashierActivity.this);
                        if (guid.equals(getApp().getOperatorGuid())) {
                            getApp().setOperatorClockedIn(true);
                        }
                        AlertDialogFragment.showComplete(BaseCashierActivity.this, R.string.btn_clock_in,
                                getString(R.string.dashboard_clock_in_msg, fullName, DateUtils.timeOnlyAttendanceFormat(time)));
                    }

                    @Override
                    protected void onClockInError(ClockInCommand.ClockInOutError error) {
                        WaitDialogFragment.hide(BaseCashierActivity.this);
                        int messageId = R.string.error_message_timesheet;
                        switch (error) {
                            case ALREADY_CLOCKED_IN:
                                messageId = R.string.error_message_already_clocked_in;
                                break;
                            case USER_DOES_NOT_EXIST:
                                messageId = R.string.error_message_employee_does_not_exist;
                                break;
                            case EMPLOYEE_NOT_ACTIVE:
                                messageId = R.string.error_message_employee_not_active;
                                break;
                        }
                        AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.error_dialog_title, getString(messageId));
                    }
                });
            }
        });
    }

    private void checkOfflineMode() {
        if (getApp().isTrainingMode())
            return;

        if (getApp().isOfflineModeExpired()) {
            AlertDialogFragment.show(this, DialogType.ALERT, R.string.offline_mode_error_dialog_title, getString(R.string.offline_mode_error_dialog_message), R.string.btn_logout, new OnDialogClickListener() {
                @Override
                public boolean onClick() {
                    DashboardActivity.startClearTop(BaseCashierActivity.this);
                    return true;
                }
            }, null, null);
        } else if (getApp().isOfflineModeNearExpiration()) {
            AlertDialogFragment.showAlert(this, R.string.offline_mode_warning_dialog_title, getString(R.string.offline_mode_warning_dialog_message));
        }
    }

    @OnFailure(BlackVoidCommand.class)
    public void onVoidFail(@Param(WebCommand.RESULT_DATA) DoFullRefundResponse result, @Param(WebCommand.RESULT_REASON) WebCommand.ErrorReason reason) {
        WaitDialogFragment.hide(this);
        Logger.d("OnFailure Data received : %s", result.toDebugString());
        final String message = result != null ? result.toString() : "";
        final String errorMessage = reason != null ? reason.toString() : "unknown";
        AlertDialogFragment.showNotification(this,
                R.string.blackstone_pay_title,
                getString(R.string.blackstone_pay_failed, message.length() > 0 ? message : errorMessage));
    }

    @OnFailure(BlackRefundCommand.class)
    public void onRefundFail(@Param(WebCommand.RESULT_DATA) RefundResponse result, @Param(WebCommand.RESULT_REASON) WebCommand.ErrorReason reason) {
        WaitDialogFragment.hide(this);
        Logger.d("OnFailure Data received : %s", result.toDebugString());
        final String message = result != null ? result.toString() : "";
        final String errorMessage = reason != null ? reason.toString() : "unknown";
        AlertDialogFragment.showNotification(this,
                R.string.blackstone_pay_title,
                getString(R.string.blackstone_pay_failed, message.length() > 0 ? message : errorMessage));
    }

    @OnCancel(BlackSaleCommand.class)
    public void onBeautifulCancel(@Param(WebCommand.RESULT_DATA) SaleResponse result) {
        WaitDialogFragment.hide(this);
        Logger.d("OnCancel Data received : %s", result.toDebugString());
    }

    @Override
    public void onHold() {
        HoldFragmentDialog.show(this, this.orderGuid, this.orderTitle, orderItemListFragment.hasKitchenItems(), new HoldFragmentDialog.IHoldListener() {

            @Override
            public void onSwap2Order(String holdName, String nextOrderGuid) {
                if (!TextUtils.isEmpty(BaseCashierActivity.this.orderGuid)) {
                    HoldOrderCommand.start(BaseCashierActivity.this, holdOrderCallback, BaseCashierActivity.this.orderGuid, holdName);
                }
                setOrderGuid(nextOrderGuid, true);
            }
        });
    }

    @Override
    public void onVoid() {
        if (TextUtils.isEmpty(this.orderGuid))
            return;

        checkOrderPayments();
    }

    private void checkOrderPayments() {
        getSupportLoaderManager().restartLoader(LOADER_CHECK_ORDER_PAYMENTS, null, checkOrderPaymentsLoader);
    }

    private void showVoidConfirmDialog() {
        if (TextUtils.isEmpty(this.orderGuid))
            return;

        AlertDialogFragment.showConfirmationNoImage(this,
                R.string.dlg_void_title,
                getString(R.string.dlg_void_msg),
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
                        return true;
                    }
                }
        );
    }

    protected void completeOrder() {
        if (TextUtils.isEmpty(this.orderGuid))
            return;

        setupNewOrder();
    }

    @Override
    public void onDiscount(final BigDecimal itemsSubTotal) {
        if (saleOrderModel == null)
            return;

        boolean saleOrderDiscountPermitted = getApp().hasPermission(Permission.SALES_DISCOUNTS);
        if (!saleOrderDiscountPermitted) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    onDiscount(itemsSubTotal);
                }
            }, Permission.SALES_DISCOUNTS);
            return;
        }

        SaleOrderDiscountEditFragment.show(this, orderGuid, itemsSubTotal, saleOrderModel.discount, saleOrderModel.discountType);
    }

    @Override
    public void onTax() {
        if (saleOrderModel == null)
            return;

        boolean saleTaxPermitted = getApp().hasPermission(Permission.SALES_TAX);
        if (!saleTaxPermitted) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    onTax();
                }
            }, Permission.SALES_TAX);
            return;
        }
        TaxEditFragment.show(this, orderGuid, saleOrderModel.taxable);
    }

    @OnSuccess(RemoveSaleOrderCommand.class)
    public void onVoidApplied() {
        setupNewOrder();
    }

    private void setupNewOrder() {
        startCommand(new DisplayWelcomeMessageCommand());
        setOrderGuid(null, true);
        /*if (ordersCount != null && !ordersCount.isEmpty()) {
            HoldFragmentDialog.show(this, null, null, new HoldFragmentDialog.IHoldListener() {
                @Override
                public void onSwap2Order(String holdName, String nextOrderGuid) {
                    setOrderGuid(nextOrderGuid);
                }
            });
        }*/
    }

    private void try2LoadActiveOrder(String curOrderGuid) {
        if (curOrderGuid == null)
            return;
        Bundle bundle = new Bundle(1);
        bundle.putString(CheckOrderTask.ARG_ORDER_GUID, curOrderGuid);
        getSupportLoaderManager().restartLoader(LOADER_CHECK_ORDER, bundle, new CheckOrderTask());
        //new CheckOrderTask(curOrderGuid).execute();
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        if (barcodeListener != null) {
            Logger.d("BaseCashierActivity: scannerListener: onBarcodeReceived(): barcode listener = " + barcodeListener);
            int i = 0;
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof BarcodeReceiver) {
                    BarcodeReceiver orderListFragment = (BarcodeReceiver) fragment;
                    orderListFragment.onBarcodeReceived(barcode);
                    i++;
                }
            }
            if (i == 0) {
                barcodeListener.onBarcodeReceived(barcode);
            }
        } else {
            Logger.d("BaseCashierActivity: scannerListener: onBarcodeReceived(): ignored - barcode listener is not set!");
        }
    }

    private ServiceConnection displayServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            displayBinder = (DisplayBinder) binder;
            setDisplayListener(displayListener);

            startCommand(new DisplayWelcomeMessageCommand());
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            displayBinder = null;
        }
    };

    private DisplayListener displayListener = new DisplayListener() {

        @Override
        public void onDisconnected() {
            if (isFinishing() || isDestroyed())
                return;

            AlertDialogFragment.showAlert(
                    BaseCashierActivity.this,
                    R.string.error_dialog_title,
                    getString(R.string.error_message_display_disconnected),
                    R.string.btn_try_again,
                    new OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            tryReconnectDisplay();
                            return true;
                        }

                    }
            );
        }

        @Override
        public void onError() {
            if (isFinishing() || isDestroyed())
                return;

            Toast.makeText(BaseCashierActivity.this, R.string.error_message_display_command_error, Toast.LENGTH_SHORT).show();
        }

    };

    private BarcodeListener defaultBarcodeListener = new BarcodeListener() {
        @Override
        public void onBarcodeReceived(String barcode) {
            Logger.d("BaseCashierActivity: defaultBarcodeListener: onBarcodeReceived()");
            if (isPaying) {
                Logger.d("BaseCashierActivity: defaultBarcodeListener: onBarcodeReceived(): ignore and exit - payment in progress");
                return;
            }

            Logger.d("BaseCashierActivity: defaultBarcodeListener: onBarcodeReceived(): tryToSearchBarCode()");
            tryToSearchBarCode(barcode, true);
        }
    };

    private class OrdersCountLoader implements LoaderManager.LoaderCallbacks<OrdersStatInfo> {

        @Override
        public Loader<OrdersStatInfo> onCreateLoader(int arg0, Bundle arg1) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT))
                    .projection("count(" + SaleOrderTable.GUID + ")")
                    .where(SaleOrderTable.OPERATOR_GUID + " = ?", getApp().getOperatorGuid() == null ? "" : getApp().getOperatorGuid());
            if (!TextUtils.isEmpty(orderGuid))
                builder.where(SaleOrderTable.GUID + " <> ?", orderGuid);
            builder.where(SaleOrderTable.STATUS + " = ? ", OrderStatus.ACTIVE.ordinal())
                    .orderBy(SaleOrderTable.UPDATE_TIME + " desc ");

            return builder
                    .wrap(new Function<Cursor, OrdersStatInfo>() {
                        @Override
                        public OrdersStatInfo apply(Cursor cursor) {
                            if (cursor.moveToFirst()) {
                                return new OrdersStatInfo(cursor.getInt(0)/*, cursor.getInt(1)*/);
                            }
                            return null;
                        }
                    })
                    .build(BaseCashierActivity.this);
        }

        private String caseWhere(String column, int value) {
            return String.format("case when %s = %d then 1 else 0 end", column, value);
        }

        @Override
        public void onLoadFinished(Loader<OrdersStatInfo> loader, OrdersStatInfo info) {
            updateCounter(info);
        }

        @Override
        public void onLoaderReset(Loader<OrdersStatInfo> loader) {
            //updateCounter(null);
        }

    }

    private static class OrdersStatInfo {
        //int activeCount;
        //int holdCounts;
        int totalCounts;

        public OrdersStatInfo(int activeCount/*, int holdCounts*/) {
            //this.activeCount = activeCount;
            /*this.holdCounts = holdCounts;*/
            this.totalCounts = activeCount/* + holdCounts*/;
        }

        public boolean isEmpty() {
            return totalCounts == 0;
        }
    }

    public boolean isOrderDiscounted() {
        final boolean isDiscounted = (saleOrderModel.discount.equals(BigDecimal.ZERO)) ? false : true;
        Logger.d("Order discount = " + saleOrderModel.discount + " => Can apply discount to items = " + !isDiscounted);
        return isDiscounted;
    }

    private class OrderInfoLoader implements LoaderManager.LoaderCallbacks<SaleOrderModelResult> {

        @Override
        public Loader<SaleOrderModelResult> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ORDER_URI)
                    .where(SaleOrderTable.GUID + " = ?", orderGuid == null ? "" : orderGuid)
                    .transform(new Function<Cursor, SaleOrderModel>() {
                        @Override
                        public SaleOrderModel apply(Cursor cursor) {
                            return new SaleOrderModel(cursor);
                        }
                    }).wrap(new Function<List<SaleOrderModel>, SaleOrderModelResult>() {
                        @Override
                        public SaleOrderModelResult apply(List<SaleOrderModel> saleOrderModels) {
                            if (saleOrderModels == null || saleOrderModels.isEmpty()) {
                                return new SaleOrderModelResult(null);
                            } else {
                                return new SaleOrderModelResult(saleOrderModels.get(0));
                            }
                        }
                    }).build(BaseCashierActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<SaleOrderModelResult> saleOrderModelLoader, SaleOrderModelResult saleOrderModel) {
            updateTitle(saleOrderModel.model);
        }

        @Override
        public void onLoaderReset(Loader<SaleOrderModelResult> saleOrderModelLoader) {
            updateTitle(null);
        }
    }

    private static class SaleOrderModelResult {

        private final SaleOrderModel model;

        private SaleOrderModelResult(SaleOrderModel model) {
            this.model = model;
        }
    }

    private class PrinterStatusCallback extends BasePrinterStatusCallback {

        private ItemExModel itemModel;
        private String modifierGiud;
        private ArrayList<String> addonsGuids;
        private ArrayList<String> optionalGuids;
        private BigDecimal price;
        private BigDecimal quantity;
        private Unit unit;

        public void setItem(ItemExModel itemModel, String modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, Unit unit) {
            this.itemModel = itemModel;
            this.modifierGiud = modifierGiud;
            this.addonsGuids = addonsGuids;
            this.optionalGuids = optionalGuids;
            this.price = price;
            this.quantity = quantity;
            this.unit = unit;
        }

        public void clearItem() {
            this.itemModel = null;
            this.modifierGiud = null;
            this.addonsGuids = null;
            this.optionalGuids = null;
            this.unit = null;
        }

        @Override
        protected void onPrinterStatusSuccess(PrinterStatusEx statusInfo) {
            if (isFinishing() || isDestroyed())
                return;

            WaitDialogFragment.hide(BaseCashierActivity.this);

            if (!statusInfo.offlineStatus.drawerIsClosed) {
                showDrawerStateErrorDialog(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                clearItem();
                return;
            }

            addItemModel(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, false, unit);
            clearItem();
        }

        @Override
        protected void onPrinterStatusError(PrinterError error) {
            if (isFinishing() || isDestroyed())
                return;

            WaitDialogFragment.hide(BaseCashierActivity.this);

            AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.check_drawer_state_error_dialog_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)), R.string.btn_retry,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            addItemModel(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            clearItem();
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onPrinterIpNotFound() {
            if (isFinishing() || isDestroyed())
                return;

            WaitDialogFragment.hide(BaseCashierActivity.this);

            AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.check_drawer_state_error_dialog_title, getString(R.string.error_message_printer_ip_not_found), R.string.btn_ok,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            checkDrawerState(true);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            clearItem();
                            return true;
                        }
                    }
            );
        }
    }

    public class AddSaleOrderCallback extends BaseAddSaleOrderCommandCallback {

        @Override
        protected void onSuccess(String guid) {
            creatingOrder = false;
            setOrderGuid(guid, true);
            pushWaitList2Order(guid);
        }

        @Override
        protected void onFailure() {
            creatingOrder = false;
            waitList.clear();
        }
    }

    private void pushWaitList2Order(String guid) {
        for (SaleOrderItemModelWrapper item : waitList) {
            item.model.orderGuid = guid;
            if (item.unit != null)
                item.unit.orderId = guid;
            AddItem2SaleOrderCommand.start(this, addItemCallback, item.model, item.modifierGiud, item.addonsGuids, item.optionalGuids, item.unit);
        }
        waitList.clear();
    }

    public class AddItem2SaleOrderCallback extends BaseAddItem2SaleOrderCallback {

        @Override
        protected void onItemAdded(SaleOrderItemModel item) {
            if (isFinishing() || isDestroyed())
                return;
            startCommand(new DisplaySaleItemCommand(item.saleItemGuid));
            //orderItemListFragment.needScrollToTheEnd();
        }

        @Override
        protected void onItemAddError() {
            if (isFinishing() || isDestroyed())
                return;
            orderItemListFragment.setNeed2ScrollList(false);
        }

        @Override
        protected void onOrderAdded(String orderGuid) {
            if (isFinishing() || isDestroyed())
                return;
            setOrderGuid(orderGuid, true);
        }
    }


    private BaseHoldOrderCallback holdOrderCallback = new BaseHoldOrderCallback() {

        @Override
        protected void onSuccess() {
            if (isFinishing() || isDestroyed())
                return;
            startCommand(new DisplayWelcomeMessageCommand());
        }
    };

    private class CheckOrderTask implements LoaderCallbacks<Cursor> {

        static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ORDER_URI)
                    .projection(SaleOrderTable.GUID)
                    .where(SaleOrderTable.GUID + " = ?", bundle.getString(ARG_ORDER_GUID))
                    .where(SaleOrderTable.STATUS + " = ?", OrderStatus.ACTIVE.ordinal())
                    .where(SaleOrderTable.OPERATOR_GUID + " = ?", getApp().getOperatorGuid())
                    .build(BaseCashierActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
            String orderGuid = null;
            boolean exists = c.moveToFirst();
            if (exists) {
                orderGuid = c.getString(0);
            }
            getSupportLoaderManager().destroyLoader(loader.getId());
            if (isFinishing() || !exists) {
                return;
            }
            salesmanGuids = getApp().getSalesmanGuids();
            setOrderGuid(orderGuid, false);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    public class GetItemsForFaickVoidCallback extends BaseGetItemsForFaickVoidCallback {

        @Override
        protected void handleSuccess(ArrayList<RefundSaleItemInfo> result) {
            if (isFinishing() || isDestroyed()) {
                RevertSuccessOrderCommand.start(BaseCashierActivity.this, orderGuid);
                EndTransactionCommand.start(BaseCashierActivity.this);
                return;
            }

            WaitDialogFragment.hide(BaseCashierActivity.this);

            PaymentTransactionModel model = new PaymentTransactionModel(
                    UUID.randomUUID().toString(),
                    null,
                    orderGuid,
                    totalCostFragment.getOrderTotal(),
                    PaymentType.SALE,
                    PaymentStatus.SUCCESS,
                    getApp().getOperatorGuid(),
                    PaymentGateway.CASH,
                    null,
                    null,
                    null,
                    null,
                    new Date(),
                    getApp().getShiftGuid(),
                    "CASH",
                    null,
                    false,
                    BigDecimal.ZERO
            );
            model.availableAmount = model.amount;
            ArrayList<PaymentTransactionModel> transactions = new ArrayList<PaymentTransactionModel>();
            transactions.add(model);

            RefundAmount refundAmount = new RefundAmount(model.amount, model.amount, result);
            MoneybackProcessor.create(orderGuid, OrderType.SALE)
                    .callback(new MoneybackProcessor.IRefundCallback() {
                        @Override
                        public void onRefundComplete(SaleOrderModel childOrderModel) {
                            RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
                            EndTransactionCommand.start(BaseCashierActivity.this);
                        }

                        @Override
                        public void onRefundCancelled() {
                            RevertSuccessOrderCommand.start(BaseCashierActivity.this, orderGuid);
                            EndTransactionCommand.start(BaseCashierActivity.this);
                        }

                        @Override
                        public void onRefundFailure() {
                            AlertDialogFragment.show(BaseCashierActivity.this, DialogType.ALERT, R.string.error_dialog_title, getString(R.string.return_order_create_error),
                                    R.string.btn_remove,
                                    new OnDialogClickListener() {
                                        @Override
                                        public boolean onClick() {
                                            RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
                                            EndTransactionCommand.start(BaseCashierActivity.this);
                                            return true;
                                        }
                                    }
                            );
                        }
                    })
                    .initRefundForFake(BaseCashierActivity.this, refundAmount, transactions, getApp().getBlackStoneUser(), true);
        }

        @Override
        protected void handleFailure() {
            EndTransactionCommand.start(BaseCashierActivity.this);

            if (isFinishing() || isDestroyed())
                return;

            WaitDialogFragment.hide(BaseCashierActivity.this);
            AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.error_dialog_title, getString(R.string.error_something_wrong));
        }
    }

    public class SuccessOrderCommand4ReturnCallback extends BaseSuccessOrderCommandCallback {

        @Override
        protected void handleSuccess() {
            GetItemsForFakeVoidCommand.start(BaseCashierActivity.this, orderGuid, new GetItemsForFaickVoidCallback());
        }

        @Override
        protected void handleFailure() {
            EndTransactionCommand.start(BaseCashierActivity.this);

            if (isFinishing() || isDestroyed())
                return;

            WaitDialogFragment.hide(BaseCashierActivity.this);
            AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.error_dialog_title, getString(R.string.error_something_wrong));
        }
    }

    private static final Handler handler = new Handler();

    private class CheckOrderPaymentsLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(PAYMENTS_URI)
                    .projection("1")
                    .where(PaymentTransactionTable.ORDER_GUID + " = ?", BaseCashierActivity.this.orderGuid)
                    .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.SUCCESS.ordinal(), PaymentStatus.PRE_AUTHORIZED.ordinal());

            if (!isCreateReturnOrder) {
                builder.where(PaymentTransactionTable.TYPE + " = ?", PaymentType.SALE.ordinal());
            }

            return builder.build(BaseCashierActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
            final boolean hasPayments = c.getCount() > 0;

            getSupportLoaderManager().destroyLoader(loader.getId());

            if (isFinishing() || isDestroyed()) {
                return;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (hasPayments) {
                        AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.error_dialog_title, getString(R.string.error_message_has_payments));
                        return;
                    }

                    showVoidConfirmDialog();
                }
            });
        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private static class SaleOrderItemModelWrapper {
        private final SaleOrderItemModel model;
        private final String modifierGiud;
        private final ArrayList<String> addonsGuids;
        private final ArrayList<String> optionalGuids;
        private final Unit unit;

        private SaleOrderItemModelWrapper(SaleOrderItemModel model, String modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, Unit unit) {
            this.model = model;
            this.modifierGiud = modifierGiud;
            this.addonsGuids = addonsGuids;
            this.optionalGuids = optionalGuids;
            this.unit = unit;
        }
    }

}