package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
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
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorGiftCardReloadCommand;
import com.kaching123.tcr.commands.prepaid.BillPaymentDescriptionCommand;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.commands.print.pos.PrintOrderCommand;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand;
import com.kaching123.tcr.commands.store.saleorder.AddItem2SaleOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.AddItem2SaleOrderCommand.BaseAddItem2SaleOrderCallback;
import com.kaching123.tcr.commands.store.saleorder.AddSaleOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.AddSaleOrderCommand.BaseAddSaleOrderCommandCallback;
import com.kaching123.tcr.commands.store.saleorder.ApplyMultipleDiscountCommand;
import com.kaching123.tcr.commands.store.saleorder.GetItemsForFakeVoidCommand;
import com.kaching123.tcr.commands.store.saleorder.GetItemsForFakeVoidCommand.BaseGetItemsForFaickVoidCallback;
import com.kaching123.tcr.commands.store.saleorder.HoldOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.HoldOrderCommand.BaseHoldOrderCallback;
import com.kaching123.tcr.commands.store.saleorder.ItemsNegativeStockTrackingCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.RevertSuccessOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.SuccessOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.SuccessOrderCommand.BaseSuccessOrderCommandCallback;
import com.kaching123.tcr.commands.store.saleorder.UpdateQtySaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderItemMovementsCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderOnRegisterCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderTaxStatusCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderTaxStatusCommand.TaxCallback;
import com.kaching123.tcr.commands.store.user.ClockInCommand;
import com.kaching123.tcr.commands.store.user.ClockInCommand.BaseClockInCallback;
import com.kaching123.tcr.commands.wireless.UnitOrderDoubleCheckCommand;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.barcode.SearchBarcodeFragment;
import com.kaching123.tcr.fragment.barcode.SearchBarcodeLoader;
import com.kaching123.tcr.fragment.commission.CommissionDialog;
import com.kaching123.tcr.fragment.commission.CommissionDialog.ICommissionDialogListener;
import com.kaching123.tcr.fragment.data.MsrDataFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.AlertDialogListFragment_;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.edit.PriceEditFragment;
import com.kaching123.tcr.fragment.edit.PriceEditFragment.OnEditPriceListener;
import com.kaching123.tcr.fragment.edit.QtyEditFragment;
import com.kaching123.tcr.fragment.edit.SaleOrderDiscountEditFragment;
import com.kaching123.tcr.fragment.edit.TaxEditFragment;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.fragment.modify.ModifyFragment;
import com.kaching123.tcr.fragment.saleorder.AddOnHoldDialogFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceTotalCostFragment;
import com.kaching123.tcr.fragment.saleorder.DetailedQServiceMainSaleActionsFragment;
import com.kaching123.tcr.fragment.saleorder.GiftCardFragmentDialog;
import com.kaching123.tcr.fragment.saleorder.IOrderDelivery;
import com.kaching123.tcr.fragment.saleorder.OnHoldListDialogFragment;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment.IItemsListHandlerHandler;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment;
import com.kaching123.tcr.fragment.saleorder.TotalCostFragment.IOrderActionListener;
import com.kaching123.tcr.fragment.search.SearchItemsListFragment;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerDialog;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment;
import com.kaching123.tcr.fragment.tendering.pax.PAXReloadFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.GiftCardAmountFragmentDialog;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.fragment.wireless.UnitsSaleFragment;
import com.kaching123.tcr.function.MultipleDiscountWrapFunction;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.BarcodeListenerHolder;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DiscountBundle;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.LoyaltyViewModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.OnHoldStatus;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PrepaidSendResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.StartMode;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.IntegerFunction;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoFullRefundResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.RefundResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.print.processor.GiftCardBillingResult;
import com.kaching123.tcr.processor.LoyaltyProcessor;
import com.kaching123.tcr.processor.LoyaltyProcessor.LoyaltyProcessorCallback;
import com.kaching123.tcr.processor.MoneybackProcessor;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.processor.PaymentProcessor;
import com.kaching123.tcr.processor.PaymentProcessor.IPaymentProcessor;
import com.kaching123.tcr.service.DisplayService;
import com.kaching123.tcr.service.DisplayService.Command;
import com.kaching123.tcr.service.DisplayService.DisplayBinder;
import com.kaching123.tcr.service.DisplayService.DisplayListener;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.service.ScaleService;
import com.kaching123.tcr.service.ScaleService.ScaleBinder;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopSchema2.TBPRegisterView2.TbpTable;
import com.kaching123.tcr.store.ShopSchema2.TBPRegisterView2.TbpXRegisterTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.MultipleDiscountTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;
import com.kaching123.tcr.store.ShopStore.TBPRegisterView;
import com.kaching123.tcr.util.DateUtils;
import com.kaching123.tcr.util.KeyboardUtils;
import com.kaching123.tcr.util.UnitUtil;
import com.telly.groundy.annotations.OnCancel;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.EXTRA_ALIAS_TITLE;
import static com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.EXTRA_PRINTER;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CursorUtil._wrap;

@EActivity
public abstract class BaseCashierActivity extends ScannerBaseActivity implements DisplayService.IDisplayBinder, BarcodeListenerHolder, IOrderDelivery,
        DetailedQServiceMainSaleActionsFragment.IOrderRegisterActionListener, DetailedQServiceTotalCostFragment.IOrderPricingListener {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(ShopStore.SaleOrderItemsView.URI_CONTENT);
    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);
    private static final int NUMBER_OF_LETTERS_TO_START_SEARCH = 2;

    private static final Uri CUSTOMER_URI = ShopProvider.contentUri(ShopStore.CustomerTable.URI_CONTENT);

    static {
        permissions.add(Permission.SALES_TRANSACTION);
    }

    private static final Uri ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    private static final Uri ORDER_VIEW_URI = ShopProvider.getContentUri(ShopStore.SaleOrderView.URI_CONTENT);
    private static final Uri PAYMENTS_URI = ShopProvider.getContentUri(ShopStore.PaymentTransactionTable.URI_CONTENT);

    private static final int LOADER_ORDER_TITLE = 1;
    private static final int LOADER_ITEM_COUNT_TITLE = 11;
    private static final int LOADER_ORDERS_COUNT = 2;
    private static final int LOADER_CHECK_ORDER = 3;
    private static final int LOADER_CHECK_ORDER_PAYMENTS = 4;
    private static final int LOADER_CHECK_ITEM_PRINT_STATUS = 5;
    private static final int LOADER_SEARCH_BARCODE = 10;
    private static final int LOADER_SALE_INCENTIVES = 12;
    private static final int LOADER_DISCOUNT_BUNDLES = 13;
    private static final int LOADER_ORDER_REAL_AVAILABLE_QTY = 14;

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
    protected MenuItem prepaidItem;
    protected MenuItem giftcardItem;
    protected MenuItem itemCount;

    private MenuItem holdCounterItem;
    private TextView holdCounterView;

    private OrderInfoLoader orderInfoLoader = new OrderInfoLoader();
    private OrderHoldItemsQtyLoader orderHoldQtyLoader = new OrderHoldItemsQtyLoader();
    //    private SaleItemCountLoader saleItemCountLoader = new SaleItemCountLoader();
    private OrdersCountLoader ordersCountLoader = new OrdersCountLoader();
    private AddItem2SaleOrderCallback addItemCallback = new AddItem2SaleOrderCallback();
    private AddSaleOrderCallback addOrderCallback = new AddSaleOrderCallback();
    private PrinterStatusCallback printerStatusCallback = new PrinterStatusCallback();
    private CheckOrderPaymentsLoader checkOrderPaymentsLoader = new CheckOrderPaymentsLoader();
    private SaleIncentivesLoader saleIncentivesLoader = new SaleIncentivesLoader();
    private DiscountBundleLoader discountBundleLoader = new DiscountBundleLoader();


    private String orderGuid;

    protected SaleOrderModel getSaleOrderModel() {
        return saleOrderModel;
    }

    private SaleOrderModel saleOrderModel;
    private String orderTitle;
    private OrdersStatInfo ordersCount;
    private Ringtone alarmRingtone;
    private CustomerModel customer;

    private DisplayService.DisplayBinder displayBinder;
    private ScaleService.ScaleBinder scaleBinder;

    @Extra
    protected boolean isCreateReturnOrder;

    private BarcodeListener barcodeListener;

    private boolean isPaying;

    private CharSequence title;

    private ArrayList<SaleOrderItemModelWrapper> waitList = new ArrayList<SaleOrderItemModelWrapper>();

    private boolean creatingOrder;


    private HashSet<String> salesmanGuids = new HashSet<String>();
    private HashMap<String, BigDecimal> itemsQtyFromOnHold = new HashMap<>();

    private Calendar calendar = Calendar.getInstance();

    private static final Handler handler = new Handler();
    private long lastClickTime;
    private boolean stop;

    private final static int REQUEST_CODE_SEND = 0;
    private final static int REQUEST_CODE_RELEASE = 1;
    private final static String PREPAID_MINI_PACKAGE_ID = "com.pinserve.prepaidmini";
    private final static String PREPAID_MINI_COMMAND = "COMMAND";
    private final static String PREPAID_MINI_PRODUCT = "PRODUCT";
    private final static String PREPAID_MINI_TRANSACTIONID = "TRANSACTIONID";
    private final static String PREPAID_MINI_START = "START";
    private final static String PREPAID_MINI_RELEASE = "RELEASE";
    private final static String PREPAID_MINI_START_ALL = "all";

    private boolean isPrepaidItemStart = false;
    private boolean isGiftCardReload = false;
    private boolean isPrepaidItemRelease = false;

    private PaymentProcessor processor;
    private LoyaltyProcessor loyaltyProcessor;
    private boolean isItemFromLoyalty;
    private ArrayList<PaymentTransactionModel> successfullCCtransactionModels;
    private List<SaleOrderItemViewModel> prepaidList;
    private List<SaleOrderItemViewModel> giftcardList;
    private ArrayList<PrepaidReleaseResult> releaseResultList;
    private ArrayList<GiftCardBillingResult> giftCardResultList;
    protected String strItemCount;
    protected int saleItemCount;
    private List<Integer> priceLevels = Collections.EMPTY_LIST;
    private List<DiscountBundle> discountBundles = Collections.EMPTY_LIST;
    private boolean hasPrefixes;

    static public boolean customerWasChosen = true;

    static public boolean runLoyaltyBirthday = false;


    public interface ISearchFragmentActions {
        void setListener(SearchItemsListFragment.IItemListener listener);
        void setSearchText(String searchText);
    }

    public interface IOrderItemListFragmentActions {
        void setItemsListHandler(OrderItemListFragment.IItemsListHandlerHandler itemsListHandler);
        void setOrderGuid(String orderGuid);
        void setNeed2ScrollList(boolean need2ScrollList);
        void setIgnorRecalc(boolean ignorRecalc);
        SaleOrderItemViewModel getLastItem();
        HashSet<String> printerAlias();
        boolean hasKitchenItems();
        void cleanAll();
        void doRemoceClickLine(String itemGuid);
        void setCreateReturnOrder(boolean isCreateReturnOrder);
    }
    protected abstract Fragment getSearchResultFragment();
    protected abstract ListFragment getOrderItemListFragment();

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        onBarcodeReceived(barcode);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public void setBarcodeListener(BarcodeListener barcodeListener) {
        this.barcodeListener = barcodeListener;
    }

    @Override
    public SaleOrderModel getOrder() {
        return getSaleOrderModel();
    }

    @Override
    public void setDefaultBarcodeListener() {
        setBarcodeListener(defaultBarcodeListener);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (data == null || data.getStringExtra(PrepaidSendResult.ARG_ITEMPRICE) == null)
            return;
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SEND) {
            isPrepaidItemStart = true;
            PrepaidSendResult result = new PrepaidSendResult(data);
            result.print();
            final ItemExModel model = new ItemExModel(result);
            tryToAddItem(model);

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_RELEASE) {
//            callback.onBillingSuccess(BaseCashierActivity.this, releaseResult);
            isPrepaidItemRelease = true;
            PrepaidReleaseResult releaseResult = new PrepaidReleaseResult(data.getStringExtra(ScannerBaseActivity.EXTRA_ACTION), data.getStringExtra(ScannerBaseActivity.EXTRA_ERROR), data.getStringExtra(ScannerBaseActivity.EXTRA_ERRORMSG), data.getStringExtra(ScannerBaseActivity.EXTRA_RECEIPT), prepaidList.get(prepaidList.size() - prepaidCount));
            releaseResultList.add(releaseResult);
            if (--prepaidCount > 0) {
                callReleaseSingleMini(PREPAID_MINI_RELEASE, prepaidList.get(prepaidList.size() - prepaidCount).productCode);
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (releaseResultList != null && releaseResultList.size() > 0 && !isPrepaidItemStart && isPrepaidItemRelease && prepaidCount == 0) {
            isPrepaidItemRelease = false;
            processor.proceedToPrepaidCheck(this, successfullCCtransactionModels, releaseResultList, new PaymentProcessor.PrepaidCheckCallBack() {
                @Override
                public void finish() {
                    OnGiftCardBilling();
                }
            });
            releaseResultList = new ArrayList<>();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindToDisplayService();
        bindToScaleService();
        tbpLoadFuture = tbpLoadScheduler.scheduleWithFixedDelay(tbpLoadTask, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindFromDisplayService();
        unbindFromScaleService();
        if (tbpLoadFuture != null)
            tbpLoadFuture.cancel(true);
        if (!isFinishing())
            return;

        if (isCreateReturnOrder && !TextUtils.isEmpty(orderGuid)) {
            RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
        }
    }

    @Override
    public void onBackPressed() {
        if (saleOrderModel != null && saleOrderModel.orderStatus == OrderStatus.HOLDON) {
            Toast.makeText(this, R.string.nav_back_order_is_on_hold_msg, Toast.LENGTH_LONG).show();
            return;
        }
        super.onBackPressed();
    }

    private void bindToDisplayService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().displayAddress().get()); //Serial Port?
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

    private void bindToScaleService() {
        boolean scaleConfigured = !TextUtils.isEmpty(getApp().getShopPref().scaleName().get()); //Serial Port?
        if (scaleConfigured) {
            ScaleService.bind(this, scaleServiceConnection);
        }
    }

    private void unbindFromScaleService() {
        if (scaleBinder != null) {
            scaleBinder = null;
            unbindService(scaleServiceConnection);
        }
    }
    private boolean scaleServiceBound;
    private boolean isItemWithPrefixedBarCodeAlreadyWeighed;
    private ScaleService scaleService;

    protected ServiceConnection scaleServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scaleServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scaleBinder = (ScaleService.ScaleBinder) service;
            scaleService = scaleBinder.getService();
            scaleServiceBound = true;
        }
    };

    @Override
    public void startCommand(DisplayService.Command displayCommand) {
        if (displayBinder != null)
            displayBinder.startCommand(displayCommand);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void setDisplayListener(DisplayService.DisplayListener displayListener) {
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

        getSupportFragmentManager().beginTransaction().hide(getSearchResultFragment()).commit();

        ((IOrderItemListFragmentActions)getOrderItemListFragment()).setItemsListHandler(new OrderItemListFragment.IItemsListHandlerHandler() {

            @Override
            public void onEditItemModifiers(String saleItemGuid,
                                            String itemGuid) {
                showEditItemModifiers(
                        saleItemGuid,
                        itemGuid);
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

            @Override
            public void onBarcodeReceivedFromUSB(String barcode) {
                if (isPaying) {
                    return;
                }

                tryToSearchBarCode(barcode, true);
            }

            @Override
            public void onTotolQtyUpdated(String qty, boolean remove, List<SaleOrderItemViewModel> list) {
                if (list != null)
                    saleItemCount = list.size();
                if (itemCount != null) {
                    if (remove) {
                        strItemCount = new BigDecimal(strItemCount).subtract(new BigDecimal(qty)).toString();
                        updateItemCountMsg();
                    } else {
                        strItemCount = qty;
                        updateItemCountMsg();
                    }
                }
            }
        });

        ((ISearchFragmentActions)getSearchResultFragment()).setListener(new SearchItemsListFragment.IItemListener() {

            @Override
            public void onItemSelected(long id, ItemExModel model) {
                tryToAddItem(model);
                closeSearch();
            }
        });

        initTitle();
        updateOrderInfo(null, null);

        String curOrderGuid = getApp().getCurrentOrderGuid();
        if (!TextUtils.isEmpty(curOrderGuid)) {
            try2LoadActiveOrder(curOrderGuid);
        }

        totalCostSetCreateReturnOrder(isCreateReturnOrder);
        ((IOrderItemListFragmentActions)getOrderItemListFragment()).setCreateReturnOrder(isCreateReturnOrder);

        setDefaultBarcodeListener();
        //ScannerProcessor.get(barcodeListener).start();

        releaseResultList = new ArrayList<>();
        giftCardResultList = new ArrayList<>();

        getSupportLoaderManager().restartLoader(LOADER_DISCOUNT_BUNDLES, null, discountBundleLoader);
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
        lastClickTime = System.currentTimeMillis();
        if (!isSPMSRSet()) {
            Fragment frm = getSupportFragmentManager().findFragmentByTag(MsrDataFragment.FTAG);
            if (frm == null) {
                getSupportFragmentManager().beginTransaction().add(MsrDataFragment.newInstance(), MsrDataFragment.FTAG).commit();
            }
        }
    }

    @Override
    protected void onPause() {
        UpdateSaleOrderOnRegisterCommand.start(getApplicationContext(), orderGuid, false);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncGapReceiver);
        stop = true;
        super.onPause();
    }


    protected boolean isSPMSRSet() {
        return (!TextUtils.isEmpty(getApp().getShopPref().usbMSRName().get()));
    }

    protected abstract void showEditItemModifiers(final String saleItemGuid,
                                                  final String itemGuid);

    protected boolean checkTrackedQty(ItemExModel model){
        if(model.isLimitQtySelected()) {
            HashMap<String, BigDecimal> map = new HashMap<>();
            map.putAll(app.getOrderItemsQty());

            BigDecimal availableQty = model.availableQty;
            if (saleOrderModel != null && saleOrderModel.orderStatus == OrderStatus.HOLDON && itemsQtyFromOnHold.containsKey(model.guid)) {
                availableQty =  model.availableQty.add(itemsQtyFromOnHold.get(model.guid));
                if(map.size() == 0) {
                    map.putAll(itemsQtyFromOnHold);
                }
            }

            if (!map.isEmpty()) {
                BigDecimal count = map.containsKey(model.getGuid()) ? map.get(model.getGuid()) : BigDecimal.ZERO;

                if (availableQty.subtract(BigDecimal.ONE.add(count)).compareTo(BigDecimal.ZERO) < 0) {
                    Toast.makeText(this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                    return false;
                }

                map.put(model.getGuid(), count.add(BigDecimal.ONE));
                app.addCurrentOrderItemsQty(map);
            } else {
                if (availableQty.subtract(BigDecimal.ONE).compareTo(BigDecimal.ZERO) < 0) {
                    Toast.makeText(this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                    return false;
                }
                HashMap<String, BigDecimal> newOrderItem = new HashMap<>(1);
                newOrderItem.put(model.getGuid(), BigDecimal.ONE);
                app.addCurrentOrderItemsQty(newOrderItem);
            }
        }
        return true;
    }

    protected void tryToAddItem(final ItemExModel model) {
        if (!TcrApplication.getCountryFunctionality().isMultiTaxGroup()) {
            tryToAddItem(model, null, null, null);
        }
        else {
            if (saleItemCount < 10)
                tryToAddItem(model, null, null, null);
            else {
                AlertDialogFragment.showAlert(this, R.string.sale_item_limit_error_dialog_title, getString(R.string.sale_item_limit_error_message_no_payments));
            }
        }
    }

    protected void tryToAddItem(final ItemExModel model, final BigDecimal price, final BigDecimal quantity, final Unit unit) {

        if(!model.hasModificators() && !checkTrackedQty(model)){
            return;
        } else if (model.isAComposisiton) {
            ItemsNegativeStockTrackingCommand.start(BaseCashierActivity.this, model.getGuid(), ItemsNegativeStockTrackingCommand.ItemType.COMPOSITION, new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                @Override
                protected void handleSuccess(boolean result) {
                    if(!result){
                        Toast.makeText(BaseCashierActivity.this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CollectModifiersCommand.start(BaseCashierActivity.this, model.guid, null, price, model, quantity, unit, true, collectionCallback);
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
                ItemsNegativeStockTrackingCommand.start(BaseCashierActivity.this, ItemsNegativeStockTrackingCommand.ItemType.MODIFIER, model.getGuid(),  getModifiers(modifiers), null, null,
                        new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                            @Override
                            protected void handleSuccess(boolean result) {
                                if (!result) {
                                    Toast.makeText(BaseCashierActivity.this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
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
            ModifyFragment.show(BaseCashierActivity.this,
                    model.guid,
                    new ItemModifiersFragment.OnAddonsChangedListener() {
                        @Override
                        public void onAddonsChanged(final ArrayList<String> modifierGuid,
                                                    final ArrayList<String> addonsGuid,
                                                    final ArrayList<String> optionalsGuid) {
                            ItemsNegativeStockTrackingCommand.start(BaseCashierActivity.this, ItemsNegativeStockTrackingCommand.ItemType.MODIFIER, model.getGuid(), modifierGuid, addonsGuid, optionalsGuid,
                                    new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                                        @Override
                                        protected void handleSuccess(boolean result) {
                                            if (!result) {
                                                Toast.makeText(BaseCashierActivity.this, R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
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
                    }
            );
        }
    };

    protected ArrayList<String> getModifiers(ArrayList<CollectModifiersCommand.SelectedModifierExModel> modifiers) {
        ArrayList<String> list = new ArrayList<>();
        for (CollectModifiersCommand.SelectedModifierExModel item : modifiers) {
            list.add(item.modifierGuid);
        }
        return list;
    }

    protected void showModifiersInsufficientCountDialog(ModifierGroupModel group){
        AlertDialogFragment.showNotification(self(), R.string.warning_dialog_title, getString(R.string.modifiers_count_insufficient_msg, group.title, group.conditionValue));
    }

    protected void tryToAddCheckPriceType(final ItemExModel model,
                                          final ArrayList<String> modifierGiud,
                                          final ArrayList<String> addonsGuids,
                                          final ArrayList<String> optionalGuids,
                                          final BigDecimal price,
                                          final BigDecimal quantity,
                                          final Unit unit) {
        if (PriceType.OPEN == model.priceType && price == null) {
            PriceEditFragment.show(this, model.price, new PriceEditFragment.OnEditPriceListener() {

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
//            if (fromScanner)
//                disconnectScanner();
            playAlarm();
            AlertDialogFragment.show(BaseCashierActivity.this,
                    AlertDialogFragment.DialogType.CONFIRM_NONE,
                    R.string.dlg_search_barcode_err_title,
                    getString(R.string.dlg_search_barcode_err_msg, barcode),
                    R.string.btn_add,
                    new StyledDialogFragment.OnDialogClickListener() {
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
                                        ItemExModel model = new ItemExModel();
                                        model.tmpBarcode = barcode;
                                        BaseItemActivity2.start(self(), model, ItemRefType.Simple, StartMode.ADD);
                                    }
                                }, Permission.INVENTORY_MODULE);
                                return true;
                            }
                            ItemExModel model = new ItemExModel();
                            model.tmpBarcode = barcode;
                            BaseItemActivity2.start(self(), model, ItemRefType.Simple, StartMode.ADD);
                            return true;
                        }
                    },
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            if (fromScanner)
                                tryReconnectScanner();
                            focusUsbInput();
                            return true;
                        }
                    }, null
            );
            return;
        }

        if (!item.isActiveStatus && !item.isSalable) {
//            if (fromScanner)
//                disconnectScanner();
            playAlarm();
            AlertDialogFragment.show(BaseCashierActivity.this,
                    AlertDialogFragment.DialogType.CONFIRM_NONE,
                    R.string.dlg_search_barcode_err_not_active_title,
                    getString(R.string.dlg_search_barcode_err_not_active_msg, barcode),
                    R.string.btn_edit,
                    new StyledDialogFragment.OnDialogClickListener() {
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
                                        BaseItemActivity2.start(self(), item, ItemRefType.Simple, StartMode.EDIT);
                                    }
                                }, Permission.INVENTORY_MODULE);
                                return true;
                            }
                            BaseItemActivity2.start(self(), item, ItemRefType.Simple, StartMode.EDIT);
                            return true;
                        }
                    },
                    new StyledDialogFragment.OnDialogClickListener() {
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

        isItemWithPrefixedBarCodeAlreadyWeighed =  isItemWithPrefixedBarCodeAlreadyWeighed(item, price, quantity);
        tryToAddItem(item, price, quantity, unit);
    }

    private boolean isItemWithPrefixedBarCodeAlreadyWeighed(ItemExModel item, BigDecimal price, BigDecimal quantity) {
        return item.priceType == PriceType.UNIT_PRICE
                && UnitUtil.isUnitLbs(item.shortCut)
                && hasPrefixes;}

    public void focusUsbInput() {

    }

    private void playAlarm() {
        if (alarmRingtone == null || alarmRingtone.isPlaying())
            return;
        alarmRingtone.play();
    }

    protected void closeAllPickers() {
        closeSearch();
    }

    protected void setCountZero() {
        saleItemCount = 0;
        strItemCount = "0";
    }

    protected void setOrderGuid(String newOrderGuid, boolean clearSalesmans) {
        setCountZero();
        getApp().setCurrentOrderGuid(newOrderGuid);
        getApp().setSalesmanGuids(salesmanGuids);
        this.orderGuid = newOrderGuid;
        ((IOrderItemListFragmentActions)getOrderItemListFragment()).setOrderGuid(this.orderGuid);
        totalCostSetOrderGuid(this.orderGuid);
        if (clearSalesmans) {
            salesmanGuids.clear();
            if (getApp().getOperator().commissionEligible) {
                salesmanGuids.add(getApp().getOperatorGuid());
            }
        }

        if (isFinishing() || isDestroyed())
            return;

        if (this.orderGuid == null) {
            getSupportLoaderManager().destroyLoader(LOADER_ORDER_TITLE);
        } else {
            getSupportLoaderManager().restartLoader(LOADER_ORDERS_COUNT, null, ordersCountLoader);
            getSupportLoaderManager().restartLoader(LOADER_ORDER_TITLE, null, orderInfoLoader);
            updateHoldButton();
            supportInvalidateOptionsMenu();
        }
        getSupportLoaderManager().restartLoader(LOADER_SALE_INCENTIVES, null, saleIncentivesLoader);
    }

    private void updateOrderInfo(SaleOrderModel saleOrderModel, CustomerModel customerModel) {
        CharSequence title;
        this.saleOrderModel = saleOrderModel;
        this.customer = customerModel;
        totalCostSetCustomer(customerModel);
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }

        if (saleOrderModel != null) {
            this.orderTitle = saleOrderModel.getHoldName();
            if (saleOrderModel.orderStatus == OrderStatus.HOLDON) {
                getSupportLoaderManager().restartLoader(LOADER_ORDER_REAL_AVAILABLE_QTY, null, orderHoldQtyLoader);
            }
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
        totalCostSetSuspendedItemsCount(this.ordersCount == null ? 0 : this.ordersCount.totalCounts);
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
        getSupportFragmentManager().beginTransaction().show(getSearchResultFragment()).commitAllowingStateLoss();
    }

    private void hideSearchFragment() {
        if (!isFinishing()) {
            getSupportFragmentManager().beginTransaction().hide(getSearchResultFragment()).commitAllowingStateLoss();
        }
    }

    private void closeSearch() {
        if (searchItem == null)
            return;
        searchItem.collapseActionView();
    }

    private void filterSearchFragment(String text) {
        ((ISearchFragmentActions)getSearchResultFragment()).setSearchText(text);
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

    private ArrayList<String> getData() {
        ArrayList<String> list = new ArrayList<>();
        list.add("BlackStone");
        return list;
    }

    protected final String DIALOG_NAME = "prepaidListDialogFragment";

    public void show(FragmentActivity activity, String message, ListAdapter adapter) {
        DialogUtil.show(activity, DIALOG_NAME,
                AlertDialogListFragment_.builder()
                        .titleId(R.string.prepaid_dialog_title)
                        .errorMsg(message)
                        .positiveButtonTitleId(R.string.btn_ok)
                        .dialogType(AlertDialogFragment.DialogType.ALERT).build())
                .setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        lockItem = menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, getResources().getInteger(R.integer.menu_order_last), R.string.action_print_label);
        updatePrintMenuItem();
        lockItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                WaitDialogFragment.show(BaseCashierActivity.this, getString(R.string.wait_printing));
                for (int i = 0; i < getApp().getShopPref().printReceiptTwice().get(); i++)
                    PrintOrderCommand.start(BaseCashierActivity.this, false, false, orderGuid, printOrderCallback, orderTitle, totalCostGetOrderSubTotal(), totalCostGetOrderDiscountTotal(), totalCostGetOrderTaxTotal(), totalCostGetOrderAmountTotal());
                return true;
            }
        });
        boolean b = super.onCreateOptionsMenu(menu);
        searchItem = menu.findItem(R.id.action_search);
        prepaidItem = menu.findItem(R.id.action_prepaid);
        prepaidItem.setVisible(getApp().getBlackStonePrepaidSolution());
        assert searchItem != null;
        assert prepaidItem != null;


        prepaidItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

//                show(BaseCashierActivity.this, getString(R.string.prepaid_dialog_des), new PrepaidListAdapter(getData()));

                try {
                    callPrepaidMini(PREPAID_MINI_START, PREPAID_MINI_START_ALL);
                    return false;
                } catch (ActivityNotFoundException exception) {
                    AlertDialogFragment.showAlert(BaseCashierActivity.this,
                            R.string.dlg_prepaid_app_missing_title,
                            getString(R.string.dlg_prepaid_app_missing));

                }
                return false;
            }
        });

        giftcardItem = menu.findItem(R.id.action_card);
        giftcardItem.setVisible(getApp().isGiftCardEnabled());
        giftcardItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //todo add gift card listner

                if (!getApp().isPaxConfigured()) {
                    AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.error_dialog_title, getString(R.string.gift_card_require_pax_configure));
                } else {
                    GiftCardFragmentDialog.show(BaseCashierActivity.this, new GiftCardFragmentDialog.IGiftCardListener() {
                        @Override
                        public void onReload() {

                            GiftCardAmountFragmentDialog.show(BaseCashierActivity.this, new GiftCardAmountFragmentDialog.IGiftCardListener() {
                                @Override
                                public void onPaymentAmountSelected(BigDecimal amount) {

                                    isGiftCardReload = true;
                                    ItemExModel model = new ItemExModel(amount);
                                    tryToAddItem(model);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                        @Override
                        public void onBalance(BigDecimal result, String last4, String errorReason) {

                        }

                        @Override
                        public void onSuccess() {
                            GiftCardFragmentDialog.hide(BaseCashierActivity.this);
                        }
                    });
                }
                return false;
            }
        });


        itemCount = menu.findItem(R.id.action_item_account);
        if (itemCount != null && strItemCount != null)
            updateItemCountMsg();
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        // Getting the 'search_plate' LinearLayout.
        View searchPlate = searchView.findViewById(searchPlateId);
        // Setting background of 'search_plate' to earlier defined drawable.
        searchPlate.setBackgroundResource(R.drawable.textfield_searchview_holo_light);

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

    class PrepaidListAdapter extends BaseAdapter {
        private ViewHolder holder;
        private ArrayList<String> list;
        private LayoutInflater mInflater;

        PrepaidListAdapter(ArrayList<String> list) {
            this.list = list;
            this.mInflater = LayoutInflater.from(BaseCashierActivity.this);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.prepaid_list_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linear_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(list.get(position));
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        DialogUtil.hide(BaseCashierActivity.this, DIALOG_NAME);
                        callPrepaidMini(PREPAID_MINI_START, PREPAID_MINI_START_ALL);

                    } catch (ActivityNotFoundException exception) {
                        AlertDialogFragment.showAlert(BaseCashierActivity.this,
                                R.string.dlg_prepaid_app_missing_title,
                                getString(R.string.dlg_prepaid_app_missing));


                    }
                }
            });
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        DialogUtil.hide(BaseCashierActivity.this, DIALOG_NAME);
                        callPrepaidMini(PREPAID_MINI_START, PREPAID_MINI_START_ALL);

                    } catch (ActivityNotFoundException exception) {
                        AlertDialogFragment.showAlert(BaseCashierActivity.this,
                                R.string.dlg_prepaid_app_missing_title,
                                getString(R.string.dlg_prepaid_app_missing));


                    }
                }
            });

            return convertView;
        }

        public final class ViewHolder {
            public TextView name;
            public LinearLayout linearLayout;
        }
    }

    protected void callPrepaidMini(String state, String extra) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(PREPAID_MINI_PACKAGE_ID);
        sendIntent.putExtra(PREPAID_MINI_COMMAND, state);
        sendIntent.putExtra(PREPAID_MINI_PRODUCT, extra);
        startActivityForResult(sendIntent, REQUEST_CODE_SEND);
    }


    protected void callReleaseSingleMini(String releaseValue, String transactionId) {
        Intent releaseIntent = new Intent();
        releaseIntent.setAction(PREPAID_MINI_PACKAGE_ID);
        releaseIntent.putExtra(PREPAID_MINI_COMMAND, releaseValue);
        releaseIntent.putExtra(PREPAID_MINI_TRANSACTIONID, transactionId);
        startActivityForResult(releaseIntent, REQUEST_CODE_RELEASE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        checkHoldInfo();
        menu.findItem(R.id.action_order_items).setVisible(!isCreateReturnOrder);
        menu.findItem(R.id.action_balance).setVisible(false);
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
                new Handler().post(new Runnable() {
                    public void run() {
                        hideSearchFragment();
                    }
                });
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
                if(!TextUtils.isEmpty(newText) && newText.length() > NUMBER_OF_LETTERS_TO_START_SEARCH) {
                    filterSearchFragment(newText);
                }
                return true;
            }
        });
    }

    protected void actionHoldCounterSelected() {
        actionBarItemClicked();
        OnHoldListDialogFragment.show(this, this.orderGuid, this.orderTitle, OnHoldListDialogFragment.HoldOnAction.GET_ORDER, new IHoldListener() {
            @Override
            public void onSwap2Order(final String holdName, final String holdPhone, final OnHoldStatus status, final String nextOrderGuid, final String definedOnHoldGuid) {
                UpdateSaleOrderOnRegisterCommand.start(getApplicationContext(), nextOrderGuid, true);
                app.clearCurrentOrderItemsQty();
                itemsQtyFromOnHold.clear();
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
                    hasPrefixes = hasPrefixes();
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
        makeScannerInputFocus();
    }

    protected void makeScannerInputFocus() {

    }

    protected void actionBarItemClicked() {

    }

    private boolean getBillpaymentActivate() {
        return TcrApplication.get().getBillPaymentActivated();
    }

    private boolean getSunpassActivate() {
        return TcrApplication.get().getSunpassActivated();
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
//        boolean salesReturnPermitted = getApp().hasPermission(Permission.SALES_RETURN);
//        if (!salesReturnPermitted) {
//            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
//                @Override
//                public void onLoginComplete() {
//                    super.onLoginComplete();
//                    actionOrderItemsSelected();
//                }
//            }, Permission.SALES_RETURN);
//            return;
//        }
        if (!isSPMSRSet()) {
            MsrDataFragment msr = getMsr();
            if (msr != null) {
                msr.releaseNow();
            }
        }
        if (!getApp().hasPermission(Permission.SALES_HISTORY)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    HistoryActivity.start(BaseCashierActivity.this);
                }
            }, Permission.ADMIN);
            return;
        } else {
            HistoryActivity.start(this);
        }
    }

    protected MsrDataFragment getMsr() {
        return (MsrDataFragment) getSupportFragmentManager().findFragmentByTag(MsrDataFragment.FTAG);
    }

    @OptionsItem
    protected void actionCommissionSelected() {
        CommissionDialog.show(this, salesmanGuids, new CommissionDialog.ICommissionDialogListener() {
            @Override
            public void onSalesmanPicked(HashSet<String> salesmanGuids) {
                BaseCashierActivity.this.salesmanGuids = salesmanGuids;
            }
        });
    }

    /**
     * * menu end ***
     */

    private void addItemModel(ItemExModel model, ArrayList<String> modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, Unit unit) {
        addItemModel(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, false, unit);
    }

    private void generateOrderId(ItemExModel firstItemInOrder) {
        if (!creatingOrder) {
            if(firstItemInOrder != null && firstItemInOrder.getTmpAgeVerified() != 0) {
                AddSaleOrderCommand.start(this, null, true, firstItemInOrder.getTmpAgeVerified(), addOrderCallback);
            } else {
                AddSaleOrderCommand.start(this, null, true, addOrderCallback);
            }
            creatingOrder = true;
        }
    }

    private void tryToAddSerializedItem(final ItemExModel model,
                                        final ArrayList<String> modifierGiud,
                                        final ArrayList<String> addonsGuids,
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
                        ItemsNegativeStockTrackingCommand.start(BaseCashierActivity.this, ItemsNegativeStockTrackingCommand.ItemType.REMOVE, model.getGuid(), modifierGiud, addonsGuids, optionalGuids, null);
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
                        notifyLoyaltyProcessorItemAddedToOrder(false);
                        hide();
                    }

                    @Override
                    public void handleCancelling() {
                        ItemsNegativeStockTrackingCommand.start(BaseCashierActivity.this, ItemsNegativeStockTrackingCommand.ItemType.REMOVE, model.getGuid(), modifierGiud, addonsGuids, optionalGuids, null);
                        notifyLoyaltyProcessorItemAddedToOrder(false);
                    }

                    private void hide() {
                        UnitsSaleFragment.hide(BaseCashierActivity.this);
                    }

                });
    }

    private void addItemModel(final ItemExModel model, final ArrayList<String> modifierGiud, final ArrayList<String> addonsGuids,
                              final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final boolean checkDrawerState, Unit unit) {
        if (model == null){
            notifyLoyaltyProcessorItemAddedToOrder(false);
            return;
        }

        if (model.serializable && model.tmpUnit.size() == 0 && PlanOptions.isSerializableAllowed()) {
            tryToAddSerializedItem(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, checkDrawerState);
            return;
        }
        addItem(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, checkDrawerState, unit);
    }


    private void addItem(ItemExModel model,
                         ArrayList<String> modifierGiud,
                         ArrayList<String> addonsGuids,
                         ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, boolean checkDrawerState, Unit unit) {
        if (checkDrawerState && !checkDrawerState(model, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit)){
            notifyLoyaltyProcessorItemAddedToOrder(false);
            return;
        }
        if (model.priceType == PriceType.UNIT_PRICE && scaleServiceBound) {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                lastClickTime = System.currentTimeMillis();
                notifyLoyaltyProcessorItemAddedToOrder(false);
                return;
            }
            lastClickTime = System.currentTimeMillis();
        }

        ((IOrderItemListFragmentActions)getOrderItemListFragment()).setNeed2ScrollList(true);
        String saleOrderItemGuid = UUID.randomUUID().toString();

        SaleOrderItemModel itemModel = new SaleOrderItemModel(
                saleOrderItemGuid,
                this.orderGuid,
                model.guid,
                model.description,
                quantity == null ? (model.priceType == PriceType.UNIT_PRICE ? BigDecimal.ZERO : BigDecimal.ONE) : quantity,
                BigDecimal.ZERO,
                isCreateReturnOrder ? PriceType.OPEN : model.priceType,
                price == null ? model.getCurrentPrice() : price,
                isCreateReturnOrder || model.isDiscountable,
                model.isDiscountable ? model.discount : null,
                model.isDiscountable ? model.discountType : null,
                model.isTaxable,
                isPrepaidItemStart ? model.tax : TextUtils.isEmpty(model.taxGroupGuid) ? getApp().getTaxVat() : model.tax,
                TextUtils.isEmpty(model.taxGroupGuid2) ? null : model.tax2,
                0,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,//final tax
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                !isCreateReturnOrder && model.hasNotes,
                isPrepaidItemStart,
                isGiftCardReload,
                model.isIncentive || model.excludeFromLoyaltyPlan ? BigDecimal.ZERO : model.loyaltyPoints,
                !(model.isIncentive || model.excludeFromLoyaltyPlan) && getApp().getShopInfo().loyaltyPointsForDollarAmount,
                null,
                model.isEbtEligible,
                BigDecimal.ZERO
        );

        if (unit != null && orderGuid != null) {
            unit.orderId = orderGuid;
            unit.saleItemId = itemModel.saleItemGuid;
        }
        boolean isOrderGuid = true;

        if (this.orderGuid == null) {
            waitList.add(new SaleOrderItemModelWrapper(itemModel, modifierGiud, addonsGuids, optionalGuids, unit));
            generateOrderId(model);
            isOrderGuid = false;
        }
        if (isPrepaidItemStart || isGiftCardReload) {
            isPrepaidItemStart = false;
            BillPaymentDescriptionModel billPaymentDescriptionModel = null;
            if (!isGiftCardReload)
                billPaymentDescriptionModel = new BillPaymentDescriptionModel(UUID.randomUUID().toString(), model.description, BillPaymentDescriptionModel.PrepaidType.WIRELESS_TOPUP, Integer.parseInt(model.productCode), false, false, saleOrderItemGuid, null);
            else
                billPaymentDescriptionModel = new BillPaymentDescriptionModel(UUID.randomUUID().toString(), model.description, BillPaymentDescriptionModel.PrepaidType.GIFT_CARD_RELOAD, 0, false, false, saleOrderItemGuid, null);

            isGiftCardReload = false;

            BillPaymentDescriptionCommand.start(this, billPaymentDescriptionModel, isOrderGuid, new BillPaymentDescriptionCommand.BillPaymentDescriptionCallback() {
                @Override
                protected void onSuccess(boolean isOrderGuid) {
                    if (!isOrderGuid)
                        return;
                }

                @Override
                protected void onFailure() {
                    //todo dealing with failure.
                    return;
                }
            });
        }

        if (isOrderGuid) {
            AddItem2SaleOrderCommand.start(this,
                    addItemCallback,
                    itemModel,
                    modifierGiud,
                    addonsGuids,
                    optionalGuids,
                    unit
            );
        }

    }

    private boolean checkDrawerState(ItemExModel model,
                                     ArrayList<String> modifierGiud,
                                     ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, Unit unit) {
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

    private void checkDrawerState(boolean searchByMac) {
        GetPrinterStatusCommand.start(this, searchByMac, printerStatusCallback);
    }

    private void skipDrawerStateCheck(final ItemExModel model, final ArrayList<String> modifierGiud, final ArrayList<String> addonsGuids, final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        if (!getApp().hasPermission(Permission.DROPS_AND_PAYOUTS)) {
            PermissionFragment.showCancelable(BaseCashierActivity.this,
                    new StyledDialogFragment.OnDialogClickListener() {

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

    private void showDrawerStateErrorDialog(final ItemExModel itemModel, final ArrayList<String> modifierGiud, final ArrayList<String> addonsGuids, final ArrayList<String> optionalGuids, final BigDecimal price, final BigDecimal quantity, final Unit unit) {
        AlertDialogFragment.showAlertWithSkip(BaseCashierActivity.this, R.string.error_dialog_title, getString(R.string.check_drawer_state_error_dialog_message), R.string.btn_retry,
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        addItemModel(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                        return true;
                    }
                },
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        return true;
                    }
                },
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        skipDrawerStateCheck(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                        return true;
                    }
                }
        );
    }

    public interface PrepaidBillingCallback {
        void onBillingSuccess(FragmentActivity context, PrepaidReleaseResult releaseResult);

        void onBillingFailure();
    }

    public PrepaidBillingCallback callback;

    public void setCallback(PrepaidBillingCallback callback) {
        this.callback = callback;
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
        ((IOrderItemListFragmentActions)getOrderItemListFragment()).setIgnorRecalc(true);

        if (saleOrderModel == null || TextUtils.isEmpty(orderGuid)) {
            Toast.makeText(this, "Please select an order to continue", Toast.LENGTH_LONG).show();
        } else if (isCreateReturnOrder) {
            //TODO super void here
            WaitDialogFragment.show(this, getString(R.string.wait_message_manual_return));
            StartTransactionCommand.start(BaseCashierActivity.this);
            SuccessOrderCommand.start(this, orderGuid, isCreateReturnOrder, new SuccessOrderCommand4ReturnCallback());
        } else if (!isPaying) {
            if (customer == null || customer.loyaltyPlanId == null){
                doPayment();
            }else {
                loyaltyProcessor = LoyaltyProcessor.create(customer.guid, orderGuid);
                loyaltyProcessor.setCallback(new LoyaltyProcessor.LoyaltyProcessorCallback() {
                    @Override
                    public void onAddItemRequest(ItemExModel item, BigDecimal price, BigDecimal qty, boolean isGiftCard) {
                        isItemFromLoyalty = true;
                        isGiftCardReload = isGiftCard;
                        tryToAddItem(item, price, qty, null);
                    }

                    @Override
                    public void onComplete() {
                        doPayment();
                    }
                }).init(self());
            }
        }
    }

    private void doPayment() {
        if (getOrderItemListFragment().getListView().getCount() == 0){
            Toast.makeText(self(), "No items in the order", Toast.LENGTH_SHORT).show();
            return;
        }

        isPaying = true;
        StartTransactionCommand.start(BaseCashierActivity.this, orderGuid);
        processor = PaymentProcessor.create(orderGuid, OrderType.SALE, saleOrderModel.kitchenPrintStatus, salesmanGuids.toArray(new String[salesmanGuids.size()]))
                .callback(new PaymentProcessor.IPaymentProcessor() {

                    @Override
                    public void onSuccess() {

                        EndTransactionCommand.start(BaseCashierActivity.this, true);
                        isPaying = false;
                        completeOrder();
                        checkOfflineMode();
                    }

                    @Override
                    public void onCancel() {
                        UpdateSaleOrderTaxStatusCommand.start(BaseCashierActivity.this, orderGuid, true,
                                new UpdateSaleOrderTaxStatusCommand.TaxCallback() {
                                    @Override
                                    protected void onSuccess(String orderGuid) {
                                        Logger.d("[SaleOrder] onTaxUpdate");
                                        totalCostSetOrderGuid(orderGuid);
                                    }
                                });

                        EndTransactionCommand.start(BaseCashierActivity.this);
                        isPaying = false;
                        SaleOrderItemViewModel lastItem =  getOrderItemListFragment() == null ? null : ((IOrderItemListFragmentActions)getOrderItemListFragment()).getLastItem();
                        if (lastItem == null) {
                            startCommand(new DisplayWelcomeMessageCommand());
                        } else {
                            startCommand(new DisplaySaleItemCommand(lastItem.getSaleItemGuid()));
                        }
                        checkOfflineMode();
                        updateItemCountMsg();
                    }

                    @Override
                    public void onPrintValues(String order, ArrayList<PaymentTransactionModel> list, BigDecimal changeAmount) {

                    }

                    @Override
                    public void onBilling(ArrayList<PaymentTransactionModel> transactionModels, List<SaleOrderItemViewModel> prepaidResultList, List<SaleOrderItemViewModel> giftCardList) {
//                        callback.onBillingSuccess();
                        EndTransactionCommand.start(BaseCashierActivity.this);
                        isPaying = false;

                        prepaidList = prepaidResultList;
                        prepaidCount = prepaidList.size();

                        giftcardList = giftCardList;
                        giftcardCount = giftcardList.size();

                        successfullCCtransactionModels = transactionModels;

                        if (prepaidCount > 0)
                            OnPrepaidBilling();
                        else if (giftcardCount > 0)
                            OnGiftCardBilling();
                    }

                    @Override
                    public void onRefund(HistoryDetailedOrderItemListFragment.RefundAmount a) {
                        amount = a;
                        getSupportLoaderManager().restartLoader(0, null, refundTransactionsLoaderCallback);
                    }

                    @Override
                    public void onUpdateOrderList() {
                        isPaying = false;
                        completeOrder();
                        checkOfflineMode();
                        getSupportLoaderManager().restartLoader(LOADER_ORDERS_COUNT, null, ordersCountLoader);
                    }

                    @Override
                    public void onPrintComplete() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                updateItemCountMsg();
                            }
                        });
                    }

                }).setCustomer(customer);
        setCallback(processor);
        processor.init(this);
    }

    private void testLoyaltyProgram() {

    }

    private void notifyLoyaltyProcessorItemAddedToOrder(boolean success){
        if (loyaltyProcessor == null)
            return;

        if (isItemFromLoyalty){
            loyaltyProcessor.onItemAddedToOrder(orderGuid, success);
            isItemFromLoyalty = false;
        }
    }

    public void updateItemCountMsg() {
        itemCount.setTitle(itemCount.getTitle().toString().substring(0, 10) + " " + strItemCount);
    }

    public int prepaidCount;
    public int giftcardCount;

    private void OnPrepaidBilling() {
        callReleaseSingleMini(PREPAID_MINI_RELEASE, prepaidList.get(0).productCode);
    }

    private void OnGiftCardBilling() {

        if (giftcardList.size() != 0) {
            PAXReloadFragmentDialog.show(this, new PAXReloadFragmentDialog.IPaxReloadListener() {

                @Override
                public void onComplete(String msg, String balance) {
                    if (PaxProcessorGiftCardReloadCommand.SUCCESS.equalsIgnoreCase(msg)) {
                        ProceedToGiftCard(true, giftcardList.get(giftcardList.size() - giftcardCount), balance);
                        PAXReloadFragmentDialog.hide(BaseCashierActivity.this);
                    } else {
                        ProceedToGiftCard(false, giftcardList.get(giftcardList.size() - giftcardCount), PaxProcessorGiftCardReloadCommand.DEFAULT_GIFT_CARD_BALANCE);
                    }

//                    else {
//                        processor.proceedToTipsApply(BaseCashierActivity.this, successfullCCtransactionModels);
//                    }
                }

                @Override
                public void onCancel() {
                    PAXReloadFragmentDialog.hide(BaseCashierActivity.this);
                    ProceedToGiftCard(false, giftcardList.get(giftcardList.size() - giftcardCount), PaxProcessorGiftCardReloadCommand.DEFAULT_GIFT_CARD_BALANCE);
//                    if (--giftcardCount > 0)
//                        OnGiftCardBilling();
//                    else {
//                        processor.proceedToTipsApply(BaseCashierActivity.this, successfullCCtransactionModels);
//                    }
                }

                @Override
                public void onRetry() {
                    OnGiftCardBilling();
                }
            }, PaxModel.get(), giftcardList.get(giftcardList.size() - giftcardCount).getPrice().toString());
        }


//            PaxProcessorGiftCardReloadCommand.startSale(this,
//                    PaxModel.get(),
//                    giftcardList.get(giftcardList.size() - giftcardCount).getPrice().toString(),
//                    new PaxProcessorGiftCardReloadCommand.PaxGiftCardReloadCallback() {
//                        @Override
//                        protected void handleSuccess(String errorReason) {
//                            if (PaxProcessorGiftCardReloadCommand.SUCCESS.equalsIgnoreCase(errorReason)) {
//                                ProceedToGiftCard(true, giftcardList.get(giftcardList.size() - giftcardCount--));
//                            } else {
//                                ProceedToGiftCard(false, giftcardList.get(giftcardList.size() - giftcardCount--));
//                            }
//                            OnGiftCardBilling();
//                        }
//
//                        @Override
//                        protected void handleError() {
//                            ProceedToGiftCard(false, giftcardList.get(giftcardList.size() - giftcardCount));
//                            OnGiftCardBilling();
//                        }
//                    });
    }

    private void ProceedToGiftCard(boolean success, SaleOrderItemViewModel model, String balance) {
        if (success)
            giftCardResultList.add(new GiftCardBillingResult(PaxProcessorGiftCardReloadCommand.SUCCESS, model, balance));
        else
            giftCardResultList.add(new GiftCardBillingResult(PaxProcessorGiftCardReloadCommand.FAIL, model, balance));

        if (--giftcardCount > 0)
            OnGiftCardBilling();
        if (giftcardCount == 0) {
            processor.setOrderChange(balance);
            processor.proceedToGiftCard(BaseCashierActivity.this, successfullCCtransactionModels, giftCardResultList);
            giftCardResultList = new ArrayList<GiftCardBillingResult>();
        }
    }

    @OnSuccess(PaxProcessorGiftCardReloadCommand.class)
    public void handleSuccess(@Param(PaxProcessorGiftCardReloadCommand.RESULT_ERROR_REASON) String errorReason, @Param(PaxProcessorGiftCardReloadCommand.RESULT_GIFT_CARD_BALANCE) String balance) {

    }

    @OnFailure(PaxProcessorGiftCardReloadCommand.class)
    public void handleFailure() {

    }

    private HistoryDetailedOrderItemListFragment.RefundAmount amount;
    private LoaderManager.LoaderCallbacks<ArrayList<PaymentTransactionModel>> refundTransactionsLoaderCallback = new LoaderManager.LoaderCallbacks<ArrayList<PaymentTransactionModel>>() {
        @Override
        public Loader<ArrayList<PaymentTransactionModel>> onCreateLoader(int loaderId, Bundle args) {
            return ReadPaymentTransactionsFunction.createLoaderOnlySaleOrderByAmount(BaseCashierActivity.this, orderGuid);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PaymentTransactionModel>> listLoader, final ArrayList<PaymentTransactionModel> transactions) {
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    processor.initRefund(BaseCashierActivity.this, transactions, amount);
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PaymentTransactionModel>> arrayListLoader) {
        }
    };

    private void try2ClockIn() {
        WaitDialogFragment.show(BaseCashierActivity.this, getString(R.string.wait_message_clock_in));
        ClockInCommand.start(BaseCashierActivity.this, getApp().getOperatorLogin()/*, password*/, new ClockInCommand.BaseClockInCallback() {
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


    @Override
    public void onResume() {
        super.onResume();
        stop = false;
        LocalBroadcastManager.getInstance(this).registerReceiver(syncGapReceiver, new IntentFilter(SyncCommand.ACTION_SYNC_GAP));
    }

    private void checkOfflineMode() {
        if (getApp().isTrainingMode())
            return;

        if (getApp().isOfflineModeExpired()) {
            AlertDialogFragment.show(this, AlertDialogFragment.DialogType.ALERT, R.string.offline_mode_error_dialog_title, getString(R.string.offline_mode_error_dialog_message), R.string.btn_logout, new StyledDialogFragment.OnDialogClickListener() {
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
        boolean isDefinedOnHold = app.getShopInfo().definedOnHold;
        IOrderItemListFragmentActions itemsFragment = ((IOrderItemListFragmentActions)getOrderItemListFragment());
        if(isDefinedOnHold) {
            OnHoldListDialogFragment.show(this, this.orderGuid, this.orderTitle, OnHoldListDialogFragment.HoldOnAction.ADD_ORDER, new ArrayList<String>(itemsFragment.printerAlias()), new IHoldListener() {
                @Override
                public void onSwap2Order(String holdName, String holdPhone, OnHoldStatus status, String nextOrderGuid, String definedOnHoldGuid) {
                    onSwap2OrderAction(BaseCashierActivity.this.orderTitle, null, null, null, definedOnHoldGuid);
                }
            });
        } else {
            AddOnHoldDialogFragment.show(this, this.orderGuid, this.orderTitle, saleOrderModel.holdPhone,
                    saleOrderModel.holdStatus, itemsFragment.hasKitchenItems(), new ArrayList<String>(itemsFragment.printerAlias()), new IHoldListener() {
                        @Override
                        public void onSwap2Order(String holdName, String holdPhone, OnHoldStatus status, String nextOrderGuid, String definedOnHoldGuid) {
                            onSwap2OrderAction(holdName, holdPhone, status, nextOrderGuid, null);
                        }
                    });
        }
    }

    private void onSwap2OrderAction(String holdName, String holdPhone, OnHoldStatus status, String nextOrderGuid, String definedOnHoldGuid) {
        if (!TextUtils.isEmpty(orderGuid)) {
            UpdateSaleOrderOnRegisterCommand.start(getApplicationContext(), orderGuid, false);
            HoldOrderCommand.start(BaseCashierActivity.this, holdOrderCallback, orderGuid, holdName, holdPhone, definedOnHoldGuid, status, HoldOrderCommand.HoldOnAction.ADD);
            UpdateSaleOrderItemMovementsCommand.start(BaseCashierActivity.this, orderGuid);
        }
        setOrderGuid(nextOrderGuid, true);
    }

    @Override
    public void onVoid() {
        if (TextUtils.isEmpty(this.orderGuid))
            return;
        ((IOrderItemListFragmentActions)getOrderItemListFragment()).setIgnorRecalc(true);
        setCountZero();
        checkOrderPayments();
    }

    @Override
    public void onCustomer() {
        ChooseCustomerDialog.show(self(), this.orderGuid, new ChooseCustomerDialog.CustomerPickListener() {
            @Override
            public void onCustomerPicked(CustomerModel customer) {

            }

            @Override
            public void onOrderAdded(String orderGuid) {
                if (isFinishing() || isDestroyed())
                    return;
                setOrderGuid(orderGuid, true);
            }
        });
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
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        ((IOrderItemListFragmentActions)getOrderItemListFragment()).cleanAll();
                        PrintOrderToKdsCommand.start(BaseCashierActivity.this, orderGuid, true, null);
                        RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
                        return true;
                    }
                }
        );
    }

    private void showErrorVoidMessage() {
        AlertDialogFragment.showAlert(this,
                R.string.dlg_void_title,
                getString(R.string.dlg_void_forbidden_msg),
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        return true;
                    }
                }
        );
    }

    protected void completeOrder() {
        getApp().clearCurrentOrderItemsQty();
        ((IOrderItemListFragmentActions)getOrderItemListFragment()).cleanAll();
        if (TextUtils.isEmpty(this.orderGuid)) {
            return;
        }
        setCountZero();
        updateItemCountMsg();
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
        TaxEditFragment.show(this, orderGuid, saleOrderModel.taxable, new TaxEditFragment.SaleOrderTaxListener() {
            @Override
            public void onAppliedTax(String orderGuid, boolean isTaxSwitch) {
                UpdateSaleOrderTaxStatusCommand.start(BaseCashierActivity.this, orderGuid, isTaxSwitch,
                        new UpdateSaleOrderTaxStatusCommand.TaxCallback() {
                            @Override
                            protected void onSuccess(String orderGuid) {
                                Logger.d("[SaleOrder] onTaxUpdate");
                                totalCostSetOrderGuid(orderGuid);
                            }
                        });
            }
        });
    }

    @OnSuccess(RemoveSaleOrderCommand.class)
    public void onVoidApplied() {
        setupNewOrder();
        getApp().clearCurrentOrderItemsQty();
    }

    @OnFailure(RemoveSaleOrderCommand.class)
    public void onVoidFailed(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterCommand.PrinterError printerError,
                             @Param(EXTRA_PRINTER) String fromPrinter,
                             @Param(EXTRA_ALIAS_TITLE) String aliasTitle) {

        KitchenPrintCallbackHelper.IKitchenPrintCallback callback = new KitchenPrintCallbackHelper.IKitchenPrintCallback() {
            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                RemoveSaleOrderCommand.start(BaseCashierActivity.this, BaseCashierActivity.this, BaseCashierActivity.this.orderGuid, true);
            }
        };

        if (printerError != null && printerError == PrinterCommand.PrinterError.DISCONNECTED) {
            KitchenPrintCallbackHelper.onPrinterDisconnected(this, fromPrinter, aliasTitle, callback);
            return;
        }
        if (printerError != null && printerError == PrinterCommand.PrinterError.IP_NOT_FOUND) {
            KitchenPrintCallbackHelper.onPrinterIPnotfound(this, fromPrinter, aliasTitle, callback);
            return;
        }
        if (printerError != null && printerError == PrinterCommand.PrinterError.NOT_CONFIGURED) {
            KitchenPrintCallbackHelper.onPrinterNotConfigured(this, fromPrinter, aliasTitle, callback);
            return;
        }
        if (printerError != null && printerError == PrinterCommand.PrinterError.PAPER_IS_NEAR_END) {
            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(this, fromPrinter, aliasTitle, callback);
            return;
        }
        KitchenPrintCallbackHelper.onPrintError(this, printerError, fromPrinter, aliasTitle, callback);
    }

    private void setupNewOrder() {
        startCommand(new DisplayWelcomeMessageCommand());
        setOrderGuid(null, true);
    }

    private void try2LoadActiveOrder(String curOrderGuid) {
        if (curOrderGuid == null)
            return;
        Bundle bundle = new Bundle(1);
        bundle.putString(CheckOrderTask.ARG_ORDER_GUID, curOrderGuid);
        getSupportLoaderManager().restartLoader(LOADER_CHECK_ORDER, bundle, new CheckOrderTask());
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        if (barcodeListener != null) {
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
            displayBinder = (DisplayService.DisplayBinder) binder;
            setDisplayListener(displayListener);

            startCommand(new DisplayWelcomeMessageCommand());
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            displayBinder = null;
        }
    };

    private DisplayService.DisplayListener displayListener = new DisplayService.DisplayListener() {

        @Override
        public void onDisconnected() {
            if (isFinishing() || isDestroyed())
                return;

            AlertDialogFragment.showAlert(
                    BaseCashierActivity.this,
                    R.string.error_dialog_title,
                    getString(R.string.error_message_display_disconnected),
                    R.string.btn_try_again,
                    new StyledDialogFragment.OnDialogClickListener() {

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
            if (isPaying) {
                playAlarm();
                return;
            }

            tryToSearchBarCode(barcode, true);
        }
    };

    private class OrdersCountLoader implements LoaderManager.LoaderCallbacks<OrdersStatInfo> {

        @Override
        public Loader<OrdersStatInfo> onCreateLoader(int arg0, Bundle arg1) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.SaleOrderTable.URI_CONTENT))
                    .projection("count(" + ShopStore.SaleOrderTable.GUID + ")");
            if (!getApp().getShopInfo().definedOnHold && !getApp().hasPermission(Permission.ON_HOLD_GLOBAL)) {
                builder.where(ShopStore.SaleOrderTable.OPERATOR_GUID + " = ?", getApp().getOperatorGuid() == null ? "" : getApp().getOperatorGuid());
            }
            if (!TextUtils.isEmpty(orderGuid))
                builder.where(ShopStore.SaleOrderTable.GUID + " <> ?", orderGuid);
            Date minCreateTime = getApp().getMinSalesHistoryLimitDateDayRounded(calendar);
            if (minCreateTime != null)
                builder.where(ShopStore.SaleOrderTable.CREATE_TIME + " >= ?", minCreateTime.getTime());
            builder.where(ShopStore.SaleOrderTable.STATUS + " = ? ", OrderStatus.HOLDON.ordinal())
                    .orderBy(ShopStore.SaleOrderTable.UPDATE_TIME + " desc ");

            return builder
                    .transform(new Function<Cursor, OrdersStatInfo>() {
                        @Override
                        public OrdersStatInfo apply(Cursor cursor) {
                            if (cursor.moveToFirst()) {
                                return new OrdersStatInfo(cursor.getInt(0));
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
        }

    }

    private static final Uri SALE_ITEM_ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleItemTable.URI_CONTENT);

    protected BigDecimal getSaleItemAmount(String orderGuid) {
        BigDecimal saleItemAmount = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(SALE_ITEM_ORDER_URI)
                .where(ShopStore.SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .perform(BaseCashierActivity.this);
        BigDecimal itemQty = BigDecimal.ZERO;
        if (c.moveToFirst()) {
            do {
                itemQty = _decimal(c.getString(c.getColumnIndex(ShopStore.SaleItemTable.QUANTITY)), BigDecimal.ZERO);
                saleItemAmount = saleItemAmount.add(itemQty);
            } while (c.moveToNext());
            c.close();
        }
        return saleItemAmount;
    }

    private static class OrdersStatInfo {
        int totalCounts;

        public OrdersStatInfo(int activeCount) {
            this.totalCounts = activeCount;
        }

        public boolean isEmpty() {
            return totalCounts == 0;
        }
    }


    public boolean isOrderDiscounted() {
        final boolean isDiscounted = !saleOrderModel.discount.equals(BigDecimal.ZERO);
        Logger.d("Order discount = " + saleOrderModel.discount + " => Can apply discount to items = " + !isDiscounted);
        return isDiscounted;
    }

    private class OrderInfoLoader implements LoaderManager.LoaderCallbacks<SaleOrderViewResult> {

        @Override
        public Loader<SaleOrderViewResult> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ORDER_VIEW_URI)
                    .where(ShopSchema2.SaleOrderView2.SaleOrderTable.GUID + " = ?", orderGuid == null ? "" : orderGuid)
                    .transform(new Function<Cursor, SaleOrderViewResult>() {
                        @Override
                        public SaleOrderViewResult apply(Cursor cursor) {
                            if (!cursor.moveToFirst()) {
                                return new SaleOrderViewResult(null, null);
                            } else {
                                SaleOrderModel order = SaleOrderModel.fromView(cursor);
                                //if(customer == null) {
                                    if (!cursor.isNull(cursor.getColumnIndex(SaleOrderView2.SaleOrderTable.CUSTOMER_GUID))) {
                                        Cursor c1 = ProviderAction.query(CUSTOMER_URI)
                                            .where(ShopStore.CustomerTable.GUID + " = ?", cursor.getString(cursor.getColumnIndex(SaleOrderView2.SaleOrderTable.CUSTOMER_GUID)))
                                            .perform(TcrApplication.get().getApplicationContext());
                                        if(c1.moveToNext()) {
                                            customer = new CustomerModel(c1);
            Log.d("BemaCarl23", "BaseCashierActivity.onCreateLoader 1: " + cursor.getString(cursor.getColumnIndex(SaleOrderView2.SaleOrderTable.CUSTOMER_GUID)));
                                        }else {
                                            customer = CustomerModel.fromOrderView(cursor);
            Log.d("BemaCarl23", "BaseCashierActivity.onCreateLoader 2: " + cursor.getString(cursor.getColumnIndex(SaleOrderView2.SaleOrderTable.CUSTOMER_GUID)));
                                        }
                                    }
                                //}
                                if (customer != null) {
            Log.d("BemaCarl23","BaseCashierActivity.onCreateLoader.customer.birthdayRewardReceivedDate: " + customer.birthdayRewardApplyDate);
                                }

                                return new SaleOrderViewResult(order, customer);
                            }
                        }
                    }).build(BaseCashierActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<SaleOrderViewResult> saleOrderModelLoader, SaleOrderViewResult result) {
            if(result.customer != null) {
                Log.d("BemaCarl23", "BaseCashierActivity.onLoadFinished.result.customer.birthdayRewardReceivedDate: " + result.customer.birthdayRewardApplyDate);
            }
            updateOrderInfo(result.order, result.customer);
        }

        @Override
        public void onLoaderReset(Loader<SaleOrderViewResult> saleOrderModelLoader) {
            updateOrderInfo(null, null);
        }
    }

    private class OrderHoldItemsQtyLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ITEM_MOVEMENT_URI)
                    .where(ShopStore.ItemMovementTable.ORDER_GUID + " = ?", saleOrderModel.guid)
                    .build(BaseCashierActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> itemQty, Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {
                ArrayList<ItemMovementModel> historyMovementModels = new ArrayList<>(cursor.getCount());
                do {
                    historyMovementModels.add(new ItemMovementModel(cursor));
                } while (cursor.moveToNext());
                cursor.close();
                if (historyMovementModels.size() > 0) {
                    HashMap<String, BigDecimal> itemQtyMovementHistory = new HashMap<>(historyMovementModels.size());
                    for (ItemMovementModel historyMovementModel : historyMovementModels) {
                        if (itemQtyMovementHistory.containsKey(historyMovementModel.itemGuid)) {
                            itemQtyMovementHistory.put(historyMovementModel.itemGuid,
                                    itemQtyMovementHistory.get(historyMovementModel.itemGuid).add(historyMovementModel.qty.abs()));
                        } else {
                            itemQtyMovementHistory.put(historyMovementModel.itemGuid, historyMovementModel.qty.abs());
                        }
                    }
                    itemsQtyFromOnHold.clear();
                    itemsQtyFromOnHold.putAll(itemQtyMovementHistory);
                }
            } else {
                itemsQtyFromOnHold.clear();
                if (cursor != null) {
                    cursor.close();
                }
            }
            getSupportLoaderManager().destroyLoader(LOADER_ORDER_REAL_AVAILABLE_QTY);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursor) {
        }
    }

    public static class SaleOrderViewResult {

        public final SaleOrderModel order;
        public final CustomerModel customer;

        public SaleOrderViewResult(SaleOrderModel order, CustomerModel customer) {
            this.order = order;
            this.customer = customer;
        }
    }

    private class PrinterStatusCallback extends GetPrinterStatusCommand.BasePrinterStatusCallback {

        private ItemExModel itemModel;
        private ArrayList<String> modifierGiud;
        private ArrayList<String> addonsGuids;
        private ArrayList<String> optionalGuids;
        private BigDecimal price;
        private BigDecimal quantity;
        private Unit unit;

        public void setItem(ItemExModel itemModel,
                            ArrayList<String> modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, BigDecimal price, BigDecimal quantity, Unit unit) {
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
        protected void onPrinterStatusError(PrinterCommand.PrinterError error) {
            if (isFinishing() || isDestroyed())
                return;

            WaitDialogFragment.hide(BaseCashierActivity.this);

            AlertDialogFragment.showAlert(BaseCashierActivity.this, R.string.check_drawer_state_error_dialog_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)), R.string.btn_retry,
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            addItemModel(itemModel, modifierGiud, addonsGuids, optionalGuids, price, quantity, unit);
                            return true;
                        }
                    },
                    new StyledDialogFragment.OnDialogClickListener() {
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
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            checkDrawerState(true);
                            return true;
                        }
                    },
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            clearItem();
                            return true;
                        }
                    }
            );
        }
    }

    public class AddSaleOrderCallback extends AddSaleOrderCommand.BaseAddSaleOrderCommandCallback {

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
            if (item.unit != null) {
                item.unit.orderId = guid;
                item.unit.saleItemId = item.model.saleItemGuid;
            }
            AddItem2SaleOrderCommand.start(this, addItemCallback, item.model, item.modifierGiud, item.addonsGuids, item.optionalGuids, item.unit);
        }
        waitList.clear();
    }

    private UpdateQtySaleOrderItemCommand.BaseUpdateQtySaleOrderItemCallback updateQtySaleOrderItemCallback = new UpdateQtySaleOrderItemCommand.BaseUpdateQtySaleOrderItemCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            if (displayBinder != null)
                displayBinder.startCommand(new DisplaySaleItemCommand(saleItemGuid));
            Log.d("BemaCarl15", "BaseCashierActivity 2884");
            ApplyMultipleDiscountCommand.start(self(), orderGuid, new ArrayList<>(discountBundles));
        }
    };

    private void onScaleItemAdded(final SaleOrderItemModel item) {
        final SaleOrderItemViewModel model = getSaleItem(item.saleItemGuid);
        if (model!=null && scaleService.getStatus() == 0 && scaleService.isUnitsLabelMatch(model.unitsLabel)) {
            BigDecimal newQty = new BigDecimal(scaleService.readScale());
            UpdateQtySaleOrderItemCommand.start(BaseCashierActivity.this, item.getGuid(), item.qty.add(newQty), updateQtySaleOrderItemCallback);
        } else {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    BaseCashierActivity.this);
            alertDialogBuilder.setTitle(getString(R.string.scale_dlg_warning_title));

            String header = !scaleServiceBound || scaleService.getStatus() < 0 ?
                    getString(R.string.scale_dlg_warning_message_check_connection) :
                    String.format(getString(R.string.scale_dlg_warning_message_place_on_scale),item.description);

            alertDialogBuilder
                    .setMessage(header)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.scale_dlg_warning_negative_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (item.qty.equals(BigDecimal.ZERO)) {
                                ((IOrderItemListFragmentActions)getOrderItemListFragment()).doRemoceClickLine(item.getGuid());
                            }
                            dialog.cancel();
                        }
                    })
                    .setNeutralButton(getString(R.string.scale_dlg_warning_neutral_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (item.qty.equals(BigDecimal.ZERO)) {
                                ((IOrderItemListFragmentActions)getOrderItemListFragment()).doRemoceClickLine(item.getGuid());
                            }
                            dialog.cancel();
                            SettingsActivity.start(BaseCashierActivity.this);
                        }
                    })
                    .setPositiveButton(getString(R.string.scale_dlg_warning_positive_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (getOperatorPermissions().contains(Permission.CHANGE_QTY)) {
                                QtyEditFragment.showCancelable(BaseCashierActivity.this, item.getGuid(),
                                        item.qty, item.priceType != PriceType.UNIT_PRICE, new QtyEditFragment.OnEditQtyListener() {
                                            @Override
                                            public void onConfirm(BigDecimal value) {
                                                UpdateQtySaleOrderItemCommand.start(BaseCashierActivity.this,
                                                        item.getGuid(), item.qty.add(value), updateQtySaleOrderItemCallback);
                                            }
                                        });
                            } else {
                                PermissionFragment.showCancelable(BaseCashierActivity.this, new StyledDialogFragment.OnDialogClickListener() {
                                            @Override
                                            public boolean onClick() {
                                                if (item.qty.equals(BigDecimal.ZERO)) {
                                                    ((IOrderItemListFragmentActions)getOrderItemListFragment()).doRemoceClickLine(item.getGuid());
                                                }
                                                return true;
                                            }
                                        },
                                        new BaseTempLoginListener(BaseCashierActivity.this) {
                                            @Override
                                            public void onLoginComplete() {
                                                super.onLoginComplete();
                                                QtyEditFragment.showCancelable(BaseCashierActivity.this,
                                                        item.getGuid(), item.qty, item.priceType != PriceType.UNIT_PRICE, new QtyEditFragment.OnEditQtyListener() {
                                                            @Override
                                                            public void onConfirm(BigDecimal value) {
                                                                UpdateQtySaleOrderItemCommand.start(BaseCashierActivity.this,
                                                                        item.getGuid(), item.qty.add(value), updateQtySaleOrderItemCallback);
                                                            }
                                                        });
                                            }
                                        }, Permission.CHANGE_QTY);
                            }
                        }
                    });

            final AlertDialog alertDialog = alertDialogBuilder.create();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    BigDecimal newQty = null;
                    while (scaleService != null) {
                        newQty = new BigDecimal(scaleService.readScale());
                        if (!alertDialog.isShowing())
                            break;

                        if (!scaleService.isUnitsLabelMatch(model.unitsLabel)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    alertDialog.setMessage(
                                            String.format(getString(R.string.scale_dlg_warning_unit_does_not_match),  model.unitsLabel.toUpperCase()));
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                                }
                            });
                        } else if (newQty.compareTo(BigDecimal.ZERO) != 1) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    String header = !scaleServiceBound ?
                                            getString(R.string.scale_dlg_warning_message_check_connection) :
                                            String.format(getString(R.string.scale_dlg_warning_message_place_on_scale),item.description);


                                    alertDialog.setMessage(header);
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            // units label match && qty > 0
                            break;
                        }
                        if (stop) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ((IOrderItemListFragmentActions)getOrderItemListFragment()).doRemoceClickLine(item.getGuid());
                                    alertDialog.dismiss();
                                }
                            });
                            break;
                        }
                    }
                    // dialog shown, not been dismissed.
                    if (alertDialog.isShowing()) {
                        final BigDecimal finalNewQty = newQty;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                UpdateQtySaleOrderItemCommand.start(BaseCashierActivity.this, item.getGuid(),
                                        item.qty.add(finalNewQty), updateQtySaleOrderItemCallback);
                                alertDialog.dismiss();
                            }
                        });
                    }
                }
            });
            alertDialog.show();
            // hide button which not necessary
            if (scaleService.getStatus() >= 0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                if (!scaleService.isUnitsLabelMatch(model.unitsLabel)) {
                    Logger.d("Status = " + scaleService.getStatus());
                    alertDialog.setMessage(
                            String.format(getString(R.string.scale_dlg_warning_unit_does_not_match),  model.unitsLabel.toUpperCase()));
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                }
            }
            //if not bound to service, do not need start thread
            if (scaleService.getStatus() >= 0) {
                thread.start();
            }
        }
    }

    public class AddItem2SaleOrderCallback extends AddItem2SaleOrderCommand.BaseAddItem2SaleOrderCallback {

        @Override
        protected void onItemAdded(final SaleOrderItemModel item) {
            notifyLoyaltyProcessorItemAddedToOrder(true);
            if (isFinishing() || isDestroyed())
                return;
            startCommand(new DisplaySaleItemCommand(item.saleItemGuid));
            if (item.priceType == PriceType.UNIT_PRICE) {
                if (scaleServiceBound && !isItemWithPrefixedBarCodeAlreadyWeighed) { //if item was scanned and has correct barcode we don't need to weight it again
                    onScaleItemAdded(item);
                }
                isItemWithPrefixedBarCodeAlreadyWeighed = false;
            }
            if (discountBundles != null && !discountBundles.isEmpty()){
                Log.d("BemaCarl15", "BaseCashierActivity 3053");
                ApplyMultipleDiscountCommand.start(self(), orderGuid, new ArrayList<>(discountBundles));
            }
        }

        @Override
        protected void onItemAddError() {
            isItemWithPrefixedBarCodeAlreadyWeighed = false;
            notifyLoyaltyProcessorItemAddedToOrder(false);
            if (isFinishing() || isDestroyed())
                return;
            ((IOrderItemListFragmentActions)getOrderItemListFragment()).setNeed2ScrollList(false);
        }

        @Override
        protected void onOrderAdded(String orderGuid) {
            isItemWithPrefixedBarCodeAlreadyWeighed = false;
            if (isFinishing() || isDestroyed())
                return;
            setOrderGuid(orderGuid, true);
        }
    }

    private SaleOrderItemViewModel getSaleItem(String saleItemGuid) {

        Cursor cursor = ProviderAction.query(URI_SALE_ITEMS)
                .where(ShopSchema2.SaleOrderItemsView2.SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemGuid)
                .perform(this);
        List<SaleOrderItemViewModel> saleItemsList = _wrap(cursor,
                new SaleOrderItemViewModelWrapFunction(this));
        return saleItemsList == null || saleItemsList.isEmpty() ? null : saleItemsList.get(0);
    }


    private HoldOrderCommand.BaseHoldOrderCallback holdOrderCallback = new HoldOrderCommand.BaseHoldOrderCallback() {

        @Override
        protected void onSuccess() {
            getApp().clearCurrentOrderItemsQty();
            if (isFinishing() || isDestroyed())
                return;
            startCommand(new DisplayWelcomeMessageCommand());
            ((IOrderItemListFragmentActions)getOrderItemListFragment()).cleanAll();
        }
    };

    private class CheckOrderTask implements LoaderManager.LoaderCallbacks<Cursor> {

        static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ORDER_URI)
                    .projection(ShopStore.SaleOrderTable.GUID)
                    .where(ShopStore.SaleOrderTable.GUID + " = ?", bundle.getString(ARG_ORDER_GUID))
                    .where(ShopStore.SaleOrderTable.STATUS + " = ?", OrderStatus.ACTIVE.ordinal())
                    .where(ShopStore.SaleOrderTable.OPERATOR_GUID + " = ?", getApp().getOperatorGuid());
            Date minCreateTime = getApp().getMinSalesHistoryLimitDateDayRounded(calendar);
            if (minCreateTime != null)
                builder.where(ShopStore.SaleOrderTable.CREATE_TIME + " >= ?", minCreateTime.getTime());
            return builder.build(BaseCashierActivity.this);
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

    public class GetItemsForFaickVoidCallback extends GetItemsForFakeVoidCommand.BaseGetItemsForFaickVoidCallback {

        @Override
        protected void handleSuccess(ArrayList<MoneybackProcessor.RefundSaleItemInfo> result) {
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
                    totalCostGetOrderTotal(),
                    PaymentTransactionModel.PaymentType.SALE,
                    PaymentTransactionModel.PaymentStatus.SUCCESS,
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
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            model.availableAmount = model.amount;
            ArrayList<PaymentTransactionModel> transactions = new ArrayList<PaymentTransactionModel>();
            transactions.add(model);

            HistoryDetailedOrderItemListFragment.RefundAmount refundAmount = new HistoryDetailedOrderItemListFragment.RefundAmount(model.amount, model.amount, result);
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
                            AlertDialogFragment.show(BaseCashierActivity.this, AlertDialogFragment.DialogType.ALERT, R.string.error_dialog_title, getString(R.string.return_order_create_error),
                                    R.string.btn_remove,
                                    new StyledDialogFragment.OnDialogClickListener() {
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

    public class SuccessOrderCommand4ReturnCallback extends SuccessOrderCommand.BaseSuccessOrderCommandCallback {

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

    private class CheckOrderPaymentsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(PAYMENTS_URI)
                    .projection("1")
                    .where(ShopStore.PaymentTransactionTable.ORDER_GUID + " = ?", BaseCashierActivity.this.orderGuid)
                    .where("(" + ShopStore.PaymentTransactionTable.STATUS + " = ? OR " + ShopStore.PaymentTransactionTable.STATUS + " = ?)", PaymentTransactionModel.PaymentStatus.SUCCESS.ordinal(), PaymentTransactionModel.PaymentStatus.PRE_AUTHORIZED.ordinal());

            if (!isCreateReturnOrder) {
                builder.where(ShopStore.PaymentTransactionTable.TYPE + " = ?", PaymentTransactionModel.PaymentType.SALE.ordinal());
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

//                    getSupportLoaderManager().restartLoader(LOADER_CHECK_ITEM_PRINT_STATUS, null, checkItemPrintStatusLoader);
                    isVoidNeedPermission();
                }
            });
        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class SaleIncentivesLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.SaleIncentiveTable.URI_CONTENT))
                    .projection("1")
                    .where(ShopStore.SaleIncentiveTable.ORDER_ID + " = ?", orderGuid == null ? "" : orderGuid)
                    .build(self());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            totalCostSetCustomerButtonEnabled(data.getCount() == 0);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class DiscountBundleLoader implements LoaderManager.LoaderCallbacks<List<DiscountBundle>> {

        @Override
        public Loader<List<DiscountBundle>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.MultipleDiscountTable.URI_CONTENT))
                    .where(ShopStore.MultipleDiscountTable.IS_ACTIVE + " = ?", 1)
                    .orderBy(ShopStore.MultipleDiscountTable.BUNDLE_ID)
                    .transform(new MultipleDiscountWrapFunction())
                    .build(self());
        }

        @Override
        public void onLoadFinished(Loader<List<DiscountBundle>> loader, List<DiscountBundle> data) {
            discountBundles = data;
        }

        @Override
        public void onLoaderReset(Loader<List<DiscountBundle>> loader) {

        }
    }

    public List<Integer> getPriceLevels(){
        return priceLevels;
    }

    public interface IPriceLevelListener {
        void onPriceLevelChanged(List<Integer> priceLevels);
    }

    private boolean isVoidNeedPermission() {
        Cursor c = ProviderAction.query(ORDER_URI)
                .projection(
                        ShopStore.SaleOrderTable.GUID,
                        ShopStore.SaleOrderTable.STATUS,
                        ShopStore.SaleOrderTable.KITCHEN_PRINT_STATUS
                )
                .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid == null ? "" : orderGuid)
                .perform(BaseCashierActivity.this);
        SaleOrderPrintInfo saleOrderPrintInfo = null;
        if (c.moveToFirst()) {
            saleOrderPrintInfo = new SaleOrderPrintInfo(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            );
        }
        if (saleOrderPrintInfo.kitchenPrintStatus != OrderStatus.COMPLETED.name() && saleOrderPrintInfo.Guid != null)
            if ((saleOrderPrintInfo.kitchenPrintStatus == PrintItemsForKitchenCommand.KitchenPrintStatus.PRINTED.name()) || getOperatorPermissions().contains(Permission.VOID_SALES))
                showVoidConfirmDialog();
            else {
                PermissionFragment.showCancelable(BaseCashierActivity.this, new BaseTempLoginListener(BaseCashierActivity.this) {
                    @Override
                    public void onLoginComplete() {
                        super.onLoginComplete();
                        showVoidConfirmDialog();
                    }
                }, Permission.VOID_SALES);
            }
        c.close();

        return false;
    }

    class SaleOrderPrintInfo {
        String Guid;
        String status;
        String kitchenPrintStatus;

        public SaleOrderPrintInfo(String Guid, String status, String kitchenPrintStatus) {
            this.Guid = Guid;
            this.status = status;
            this.kitchenPrintStatus = kitchenPrintStatus;
        }
    }

    private BroadcastReceiver syncGapReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d("[SYNC GAP] Cashier Activity: restart orders on hold count loader");
            getSupportLoaderManager().restartLoader(LOADER_ORDERS_COUNT, null, ordersCountLoader);

            String curOrderGuid = getApp().getCurrentOrderGuid();
            if (!TextUtils.isEmpty(curOrderGuid)) {
                try2LoadActiveOrder(curOrderGuid);

            } else {
                setupNewOrder();
            }
        }

    };

    public static interface IHoldListener {
        void onSwap2Order(String holdName, String holdPhone, OnHoldStatus status, String nextOrderGuid, String definedOnHoldGuid);
        //void onCancelHold();
    }

    private static class SaleOrderItemModelWrapper {
        private final SaleOrderItemModel model;
        private final ArrayList<String> modifierGiud;
        private final ArrayList<String> addonsGuids;
        private final ArrayList<String> optionalGuids;
        private final Unit unit;

        public SaleOrderItemModelWrapper(SaleOrderItemModel model,
                                         ArrayList<String> modifierGiud,
                                         ArrayList<String> addonsGuids,
                                         ArrayList<String> optionalGuids, Unit unit) {
            this.model = model;
            this.modifierGiud = modifierGiud;
            this.addonsGuids = addonsGuids;
            this.optionalGuids = optionalGuids;
            this.unit = unit;
        }
    }

    protected abstract void hideTotalCostFragment();
    protected abstract void showTotalCostFragment();

    private Set<Permission> getOperatorPermissions() {
        return getApp().getOperatorPermissions();
    }

    private Future tbpLoadFuture;
    private ScheduledExecutorService tbpLoadScheduler = Executors.newSingleThreadScheduledExecutor();
    private Runnable tbpLoadTask = new Runnable() {
        @Override
        public void run() {
            Query query = ProviderAction.query(ShopProvider.contentUri(ShopStore.TBPRegisterView.URI_CONTENT))
                    .projection(ShopSchema2.TBPRegisterView2.TbpTable.PRICE_LEVEL)
                    .orderBy(ShopSchema2.TBPRegisterView2.TbpTable.PRICE_LEVEL);


            query.where(ShopSchema2.TBPRegisterView2.TbpXRegisterTable.REGISTER_ID + " = ?", getApp().getRegisterId());
            query.where(ShopSchema2.TBPRegisterView2.TbpTable.IS_ACTIVE + " = ?", 1);

            Calendar current = Calendar.getInstance();
            current.setTime(new Date());
            String currentTime = DateUtils.timeOnlyFullFormat(current.getTime());

            String columnStart = null;
            String columnEnd = null;
            switch(calendar.get(Calendar.DAY_OF_WEEK)){
                case Calendar.MONDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.MON_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.MON_END;
                    break;
                case Calendar.TUESDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.TUE_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.TUE_END;
                    break;
                case Calendar.WEDNESDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.WED_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.WED_END;
                    break;
                case Calendar.THURSDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.THU_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.THU_END;
                    break;
                case Calendar.FRIDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.FRI_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.FRI_END;
                    break;
                case Calendar.SATURDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.SAT_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.SAT_END;
                    break;
                case Calendar.SUNDAY:
                    columnStart = ShopSchema2.TBPRegisterView2.TbpTable.SUN_START;
                    columnEnd = ShopSchema2.TBPRegisterView2.TbpTable.SUN_END;
                    break;
            }

            query.where(columnStart + " < ?", currentTime);
            query.where(columnEnd + " > ?", currentTime);

            List<Integer> priceLevels = query.perform(self()).toFluentIterable(new IntegerFunction()).toList();

            if (!BaseCashierActivity.this.priceLevels.equals(priceLevels)){
                BaseCashierActivity.this.priceLevels = priceLevels;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyFragmentsPriceLevelChanged();
                    }
                });
            }


        }
    };

    private void notifyFragmentsPriceLevelChanged(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fr : fragments){
            if (fr instanceof  IPriceLevelListener){
                ((IPriceLevelListener) fr).onPriceLevelChanged(priceLevels);
            }
        }
    }

    protected abstract void totalCostSetOrderGuid(String guid);
    protected abstract void totalCostSetSuspendedItemsCount(int count);
    protected abstract void totalCostSetCustomer(CustomerModel customerModel);
    protected abstract void totalCostSetCreateReturnOrder(boolean isCreateReturnOrder);
    protected abstract void totalCostSetCustomerButtonEnabled(boolean isCreateReturnOrder);
    protected abstract BigDecimal totalCostGetOrderTotal();
    protected abstract String totalCostGetOrderSubTotal();
    protected abstract String totalCostGetOrderDiscountTotal();
    protected abstract String totalCostGetOrderTaxTotal();
    protected abstract String totalCostGetOrderAmountTotal();

}
