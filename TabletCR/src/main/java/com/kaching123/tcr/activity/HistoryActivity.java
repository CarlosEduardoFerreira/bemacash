package com.kaching123.tcr.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.local.EndTransactionCommand;
import com.kaching123.tcr.commands.local.StartTransactionCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintPrepaidOrderCommand;
import com.kaching123.tcr.commands.print.pos.ReprintOrderCommand;
import com.kaching123.tcr.commands.print.pos.ReprintRefundCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.BaseKitchenPrintCallback;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper.IKitchenPrintCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.data.MsrDataFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.history.EmailOrderFragmentDialog;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemFragment.HistoryDetailedOrderItemListener;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment.IRefundAmountListener;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment.RefundAmount;
import com.kaching123.tcr.fragment.tendering.history.HistoryOrderFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryOrderFragment.ISettlementListener;
import com.kaching123.tcr.fragment.tendering.history.HistoryOrderListFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryOrderListFragment.ILoader;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TransactionsState;
import com.kaching123.tcr.model.payment.PaymentMethod;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.SunpassInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessPinInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessTopupInfo;
import com.kaching123.tcr.processor.MoneybackProcessor;
import com.kaching123.tcr.processor.MoneybackProcessor.IRefundCallback;
import com.kaching123.tcr.processor.PaymentProcessor;
import com.kaching123.tcr.util.AnimationUtils;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Ivan v. Rikhmayer
 */
@EActivity(R.layout.history_activity)
//@OptionsMenu(R.menu.history_activity)
public class HistoryActivity extends ScannerBaseActivity implements ILoader, HistoryDetailedOrderItemListener {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    private static final int LOADER_OPENED_TRANSACTIONS_ID = 3;

//    static {
//        permissions.add(Permission.SALES_RETURN);
//    }

    boolean showingFront = true;

    boolean printOrder;
    boolean printRefund;

    @ViewById
    ViewFlipper listFlipper;

    @ViewById
    ViewFlipper flipper;

    /**
     * ******************* ORDER LIST ***********************
     */
    @FragmentById
    protected HistoryOrderListFragment orderListFragment;

    @FragmentById
    protected HistoryOrderFragment historyFragment;

    /**
     * ******************* ITEM LIST ***********************
     */
    @FragmentById
    protected HistoryDetailedOrderItemFragment totalCostFragment;

    @FragmentById
    protected HistoryDetailedOrderItemListFragment orderItemsListFragment;

    @Extra
    protected Boolean showOpenedTransactions;

    protected ArrayList<String> kitchenAliases = new ArrayList<>();

    public static void start(Context context) {
        HistoryActivity_.intent(context).start();
    }

    public static void start(Context context, boolean showOpenedTransactions) {
        HistoryActivity_.intent(context).showOpenedTransactions(showOpenedTransactions).start();
    }

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public void onBackPressed() {
        if (showingFront) {
            super.onBackPressed();
            return;
        }

        showingFront = true;

        if (totalCostFragment != null)
            totalCostFragment.onHide();
        if (orderItemsListFragment != null)
            orderItemsListFragment.onHide();
        historyFragment.onShow();

        flipper.showPrevious();
        listFlipper.showNext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isSPMSRSet()) {
            Fragment frm = getSupportFragmentManager().findFragmentByTag(MsrDataFragment.FTAG);
            if (frm == null) {
                getSupportFragmentManager().beginTransaction().add(MsrDataFragment.newInstance(), MsrDataFragment.FTAG).commit();
            }
        }
    }

    protected boolean isSPMSRSet() {
        return (!TextUtils.isEmpty(getApp().getShopPref().usbMSRName().get()));
    }

    @Override
    protected boolean isInSettingPage() {
        return true;
    }

    @AfterViews
    protected void init() {
        orderListFragment.addListener(historyFragment);
        orderListFragment.addListener(this);
        totalCostFragment.setListener(this);
        if (showOpenedTransactions != null && showOpenedTransactions)
            historyFragment.setTransactionsState(TransactionsState.OPEN);
        historyFragment.setCallback(orderListFragment);
        historyFragment.setSettlementListener(settlementListener);
        orderItemsListFragment.setRefundAmountListener(new IRefundAmountListener() {
            @Override
            public void onRefundAmountChanged(BigDecimal amount) {
                totalCostFragment.updateRefundAmount(amount);
            }
        });

        AnimationUtils.applyFlippingEffect(this, flipper);
        AnimationUtils.applyFlippingEffect(this, listFlipper);

        getSupportLoaderManager().restartLoader(LOADER_OPENED_TRANSACTIONS_ID, null, openedTransactionsLoader);

    }


    @Override
    public void onReturnClick() {
        Log.d("BemaCarl13","HistoryActivity.onReturnClick");
        if (!getApp().hasPermission(Permission.SALES_RETURN)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                }
            }, Permission.SALES_RETURN);
            return;
        }
        WaitDialogFragment.show(this, getString(R.string.loading_message));
        getSupportLoaderManager().restartLoader(0, null, refundTransactionsLoaderCallback);
    }

    @Override
    public void onTipsRefundClick() {
        WaitDialogFragment.show(this, getString(R.string.loading_message));
        getSupportLoaderManager().restartLoader(0, null, tipsRefundTransactionsLoaderCallback);
    }

    @Override
    public void onCloseClick(ArrayList<PaymentTransactionModel> preauthTransactions, boolean isOrderTipped) {
        if (getApp().isPaxConfigured() && !getApp().isTipsEnabled()) {
            AlertDialogFragment.showAlert(HistoryActivity.this, R.string.error_dialog_title, getString(R.string.blackstone_pax_failure_reason_tips_disabled));
            return;
        }

        PaymentProcessor.create(orderItemsListFragment.guid, OrderType.SALE, null, null).closePreauth(this, isOrderTipped, preauthTransactions, totalCostFragment.getTotalAmount());
    }

    private void startRefund(final ArrayList<PaymentTransactionModel> transactions) {
        Log.d("BemaCarl13","HistoryActivity.startRefund1");
        if (!checkTransactions(transactions))
            return;
        Log.d("BemaCarl13","HistoryActivity.startRefund2");
        final RefundAmount amount = orderItemsListFragment.getReturnAmount();
        Log.d("BemaCarl13","HistoryActivity.startRefund.amount: " + amount);
        Logger.d("About to return %s", UiHelper.valueOf(amount.pickedValue));
        Logger.d("Total is %s", UiHelper.valueOf(amount.orderValue));
        if (BigDecimal.ZERO.compareTo(amount.pickedValue) == 0) {
            Logger.d("just notify");
            AlertDialogFragment.showAlert(HistoryActivity.this, R.string.refund_nothing_selected_title, getString(R.string.refund_nothing_selected_body));
        } else {
            Logger.d("doing refund");
            initRefund(transactions, amount, true);
        }
    }

    private void startRefundTips(final ArrayList<PaymentTransactionModel> transactions) {
        if (!checkTransactions(transactions))
            return;

        RefundAmount tipsRefundAmount = totalCostFragment.getTipsRefundAmount();
        Logger.d("About to return tips: %s", UiHelper.valueOf(tipsRefundAmount.pickedValue));
        if (BigDecimal.ZERO.compareTo(tipsRefundAmount.pickedValue) == 0) {
            Logger.d("just notify");
            AlertDialogFragment.showAlert(HistoryActivity.this, R.string.refund_nothing_selected_title, getString(R.string.refund_nothing_selected_body));
            return;
        }
        if (getApp().isPaxConfigured() && !getApp().isTipsEnabled()) {
            AlertDialogFragment.showAlert(this, R.string.error_dialog_title, getString(R.string.blackstone_pax_failure_reason_tips_disabled));
            return;
        }
        Logger.d("doing refund");
        initRefund(transactions, tipsRefundAmount, true);
    }

    private boolean checkTransactions(ArrayList<PaymentTransactionModel> transactions) {
        Log.d("BemaCarl13","HistoryActivity.checkTransactions.transactions: " + transactions);
        if (transactions != null && !transactions.isEmpty()) {
            return true;
        }
        Log.d("BemaCarl13","HistoryActivity.checkTransactions2");
        AlertDialogFragment.showAlert(this, R.string.error_dialog_title, getString(R.string.error_message_no_payments));
        return false;
    }

    private void initRefund(final ArrayList<PaymentTransactionModel> transactions, final RefundAmount amount, boolean allowImmediateCancel) {
        PaymentMethod returnMethod = calcReturnMethod(transactions);
        orderItemsListFragment.updateUnitsBeforeRefund();

        StartTransactionCommand.start(this);
        MoneybackProcessor.create(orderItemsListFragment.guid, OrderType.SALE).callback(new IRefundCallback() {

            @Override
            public void onRefundComplete(SaleOrderModel childOrderModel) {

                orderItemsListFragment.updateItemQty(HistoryActivity.this);

                orderItemsListFragment.updateList();
                EndTransactionCommand.start(HistoryActivity.this, true);
            }

            @Override
            public void onRefundCancelled() {
                // ignore
                EndTransactionCommand.start(HistoryActivity.this);
            }

            @Override
            public void onRefundFailure() {
                orderItemsListFragment.updateList();
                EndTransactionCommand.start(HistoryActivity.this);
            }
        }).initRefund(this, returnMethod, transactions, amount, getApp().getBlackStoneUser(), allowImmediateCancel);
    }

    private PaymentMethod calcReturnMethod(ArrayList<PaymentTransactionModel> transactions) {
        int cashGatewayCount = 0;
        int creditReceiptCount = 0;
        int creditCardCount = 0;

        for (PaymentTransactionModel t : transactions) {
            if (t.gateway.isCreditCard()) {
                creditCardCount++;
            } else if (t.gateway == PaymentGateway.CASH) {
                cashGatewayCount++;
            } else if (t.gateway == PaymentGateway.CREDIT) {
                creditReceiptCount++;
            }
        }
        if (cashGatewayCount == transactions.size())
            return PaymentMethod.CASH;
        if (creditCardCount == transactions.size())
            return PaymentMethod.CREDIT_CARD;
        if (creditReceiptCount == transactions.size())
            return PaymentMethod.CREDIT_RECEIPT;
        return null;
    }

    @Override
    public void onReprintClick(boolean printOrder, boolean printRefund) {
        this.printOrder = printOrder;
        this.printRefund = printRefund;
        OrderType orderType = totalCostFragment.getOrderType();
        if (printOrder) {
            reprintOrder(false, false);
        } else if (printRefund) {
            reprintRefund(false, false);
        }
    }

    @Override
    public void onEmailClick() {
        EmailOrderFragmentDialog.show(this, orderItemsListFragment.guid, new EmailOrderFragmentDialog.EmailOrderCompleteListener() {
            @Override
            public void onConfirmed(String email) {
                Toast.makeText(HistoryActivity.this, getString(R.string.send_email_toast_msg) + " " + email, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReprintKitchenClick() {
        kitchenAliases = orderItemsListFragment.adapter.getPrinterAliasItems();
        printItemsToKitchen(null, false, false, false, kitchenAliases);
    }

    private void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac, ArrayList<String> printerAliases) {
        WaitDialogFragment.show(this, getString(R.string.wait_printing));

        PrintItemsForKitchenCommand.start(this, skipPaperWarning, searchByMac, orderItemsListFragment.guid,
                fromPrinter, skip, new KitchenPrintCallback(), true, null, false, printerAliases);
    }

    private void reprintOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(this, getString(R.string.wait_printing));
        switch (totalCostFragment.getOrderType()) {
            case SALE:
                ReprintOrderCommand.start(this, skipPaperWarning, searchByMac, orderItemsListFragment.guid, printCallback);
                break;
            case PREPAID:
                PrintPrepaidOrderCommand.start(this, true, skipPaperWarning, searchByMac, orderItemsListFragment.guid, getEmptyPrepaidInfo(totalCostFragment.getPrepaidType()), printCallback);
                break;
            default:
                WaitDialogFragment.hide(this);
                break;
        }
    }

    private static IPrePaidInfo getEmptyPrepaidInfo(PrepaidType type) {
        switch (type) {
            case SUNPASS:
                return new SunpassInfo();
            case WIRELESS_PIN:
                return new WirelessPinInfo();
            case WIRELESS_TOPUP:
                return new WirelessTopupInfo();
            default:
                return null;
        }
    }

    private void reprintRefund(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(this, getString(R.string.wait_printing));
        ReprintRefundCommand.start(this, skipPaperWarning, searchByMac, orderItemsListFragment.guid, printRefundCallback);
    }

    private void onReprintOrderSuccess() {
        WaitDialogFragment.hide(this);
        Toast.makeText(HistoryActivity.this, R.string.peprint_order_receipt_success_message, Toast.LENGTH_SHORT).show();
        if (printRefund) {
            reprintRefund(false, false);
        }
    }

    private void onReprintRefundSuccess() {
        WaitDialogFragment.hide(this);
        Toast.makeText(HistoryActivity.this, R.string.peprint_refund_receipt_success_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoad(int count, BigDecimal min, BigDecimal max) {
//        flipper.showNext();
    }

    @Override
    public void onItemClicked(String guid, BigDecimal totalAmount, Date dateText, String cashierText, String numText, OrderType type, boolean isTipped) {
        if (!showingFront) {
            return;
        }

        orderItemsListFragment.init(guid);
        totalCostFragment.update(guid, totalAmount, dateText, cashierText, numText, type, isTipped);

        historyFragment.onHide();
        totalCostFragment.onShow();

        flipper.showNext();
        listFlipper.showNext();

        showingFront = false;
    }

    @Override
    public boolean onLoadedFromServer(String unitSerial) {
        return false;
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        if (historyFragment != null) {
            int i = 0;
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof BarcodeReceiver) {
                    BarcodeReceiver orderListFragment = (BarcodeReceiver) fragment;
                    orderListFragment.onBarcodeReceived(barcode);
                    i++;
                }
            }
            if (i == 0) {

                Logger.d("HistoryActivity onBarcodeReceived: " + ",Thread, " + Thread.currentThread().getId());

                historyFragment.setOrderNumber(barcode);
            }
        }
    }

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        Logger.d("HistoryActivity onReceive:" + barcode);

        onBarcodeReceived(barcode);
    }

    private ISettlementListener settlementListener = new ISettlementListener() {

        @Override
        public void onSettlementRequested() {
            boolean tipsPermitted = getApp().hasPermission(Permission.TIPS);
            if (!tipsPermitted) {
                PermissionFragment.showCancelable(HistoryActivity.this, new BaseTempLoginListener(HistoryActivity.this) {
                    @Override
                    public void onLoginComplete() {
                        super.onLoginComplete();
                        onSettlementRequested();
                    }
                }, Permission.TIPS);
                return;
            }

            if (getApp().isPaxConfigured() && !getApp().isTipsEnabled()) {
                AlertDialogFragment.showAlert(HistoryActivity.this, R.string.error_dialog_title, getString(R.string.blackstone_pax_failure_reason_tips_disabled));
                return;
            }


            PaymentProcessor.create().doSettlement(HistoryActivity.this);
        }
    };

    private LoaderCallbacks<ArrayList<PaymentTransactionModel>> refundTransactionsLoaderCallback = new LoaderCallbacks<ArrayList<PaymentTransactionModel>>() {
        @Override
        public Loader<ArrayList<PaymentTransactionModel>> onCreateLoader(int loaderId, Bundle args) {
            Log.d("BemaCarl13","HistoryActivity.onLoadFinished.orderItemsListFragment.guid: " + orderItemsListFragment.guid);
            return ReadPaymentTransactionsFunction.createLoaderOnlySaleOrderByAmountToRefund(HistoryActivity.this, orderItemsListFragment.guid);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PaymentTransactionModel>> listLoader, final ArrayList<PaymentTransactionModel> transactions) {
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    getSupportLoaderManager().destroyLoader(0);
                    WaitDialogFragment.hide(HistoryActivity.this);
                    Log.d("BemaCarl13","HistoryActivity.onLoadFinished.transactions: " + transactions);
                    startRefund(transactions);
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PaymentTransactionModel>> arrayListLoader) {
        }
    };

    private LoaderCallbacks<ArrayList<PaymentTransactionModel>> tipsRefundTransactionsLoaderCallback = new LoaderCallbacks<ArrayList<PaymentTransactionModel>>() {
        @Override
        public Loader<ArrayList<PaymentTransactionModel>> onCreateLoader(int loaderId, Bundle args) {
            return ReadPaymentTransactionsFunction.createLoaderOnlySaleOrderByAmount(HistoryActivity.this, orderItemsListFragment.guid);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PaymentTransactionModel>> listLoader, final ArrayList<PaymentTransactionModel> transactions) {
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    getSupportLoaderManager().destroyLoader(0);
                    WaitDialogFragment.hide(HistoryActivity.this);
                    startRefundTips(transactions);
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PaymentTransactionModel>> arrayListLoader) {
        }
    };

    private BasePrintCallback printCallback = new BasePrintCallback() {


        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                reprintOrder(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                //do nothing
            }
        };


        @Override
        protected void onPrintSuccess() {
            HistoryActivity.this.onReprintOrderSuccess();
        }

        @Override
        protected void onPrintError(PrinterError error) {
            PrintCallbackHelper2.onPrintError(HistoryActivity.this, error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(HistoryActivity.this, retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(HistoryActivity.this, retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(HistoryActivity.this, retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(HistoryActivity.this, retryListener);
        }
    };


    private BasePrintCallback printRefundCallback = new BasePrintCallback() {

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                reprintRefund(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                //do nothing
            }
        };

        @Override
        @OnSuccess(ReprintRefundCommand.class)
        public void handleSuccess() {
            super.handleSuccess();
        }

        @Override
        @OnFailure(ReprintRefundCommand.class)
        public void handleFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError printerError) {
            super.handleFailure(printerError);
        }

        @Override
        public void onPrintSuccess() {
            HistoryActivity.this.onReprintRefundSuccess();
        }

        @Override
        protected void onPrintError(PrinterError error) {
            PrintCallbackHelper2.onPrintError(HistoryActivity.this, error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(HistoryActivity.this, retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(HistoryActivity.this, retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(HistoryActivity.this, retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(HistoryActivity.this, retryListener);
        }
    };

    public class KitchenPrintCallback extends BaseKitchenPrintCallback {

        private IKitchenPrintCallback skipListener = new IKitchenPrintCallback() {

            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac, kitchenAliases);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                kitchenAliases.remove(fromPrinter);
                if (kitchenAliases.size() > 0) {
                    printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac, kitchenAliases);
                } else {
                    printItemsToKitchen(fromPrinter, true, ignorePaperEnd, searchByMac, kitchenAliases);
                }
            }
        };

        @Override
        protected void onPrintSuccess() {
            WaitDialogFragment.hide(HistoryActivity.this);
        }

        @Override
        protected void onPrintError(PrinterError error, String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrintError(HistoryActivity.this, error, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterNotConfigured(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterNotConfigured(HistoryActivity.this, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterDisconnected(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterDisconnected(HistoryActivity.this, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterIPnotFound(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterIPnotfound(HistoryActivity.this, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(HistoryActivity.this, fromPrinter, aliasTitle, skipListener);
        }
    }
}
