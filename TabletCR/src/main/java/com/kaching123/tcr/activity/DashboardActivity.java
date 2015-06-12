package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.getbase.android.db.cursors.FluentCursor;
import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.kaching123.tcr.AutoUpdateService;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidProcessorActivity;
import com.kaching123.tcr.commands.device.OpenDrawerCommand;
import com.kaching123.tcr.commands.device.OpenDrawerCommand.BaseOpenDrawerCallback;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.device.WaitForCloseDrawerCommand;
import com.kaching123.tcr.commands.device.WaitForCloseDrawerCommand.BaseWaitForCloseDrawerCallback;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintDropPayoutCommand;
import com.kaching123.tcr.commands.store.settings.ExportDatabaseCommand;
import com.kaching123.tcr.commands.store.settings.ExportDatabaseCommand.BaseExportDatabaseCallback;
import com.kaching123.tcr.commands.store.user.AddCashDrawerMovementCommand;
import com.kaching123.tcr.commands.store.user.ClockInCommand;
import com.kaching123.tcr.commands.store.user.ClockOutCommand;
import com.kaching123.tcr.commands.store.user.StartShiftCommand;
import com.kaching123.tcr.commands.store.user.StopShiftCommand;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.commands.support.SendLogCommand.BaseSendLogCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.edit.CashDrawerMovementEditFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.fragment.shift.CloseAmountFragment;
import com.kaching123.tcr.fragment.shift.CloseDrawerFragment;
import com.kaching123.tcr.fragment.shift.OpenAmountFragment;
import com.kaching123.tcr.fragment.shift.PrintXReportFragment;
import com.kaching123.tcr.fragment.shift.PutCashFragment;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.ActivationTypeChoosingFragmentDialog;
import com.kaching123.tcr.fragment.user.LoginFragment.Mode;
import com.kaching123.tcr.fragment.user.LoginFragment.OnLoginCompleteListener;
import com.kaching123.tcr.fragment.user.LoginOuterFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.fragment.user.TimesheetFragment;
import com.kaching123.tcr.fragment.user.TimesheetNewFragment;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ActivationCarrierTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;
import com.kaching123.tcr.store.ShopStore.TotalSalesQuery;
import com.kaching123.tcr.util.DateUtils;
import com.kaching123.tcr.util.ScreenUtils;
import com.telly.groundy.TaskHandler;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._castToReal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.model.ContentValuesUtil._tipsPaymentType;

/**
 * Created by pkabakov on 03.12.13.
 */
@EActivity(R.layout.dashboard_activity)
@OptionsMenu(R.menu.dashboard_activity)
public class DashboardActivity extends SuperBaseActivity {

    private static final Uri ITEMS_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final Uri SHIFT_URI = ShopProvider.getContentWithLimitUri(ShiftTable.URI_CONTENT, 1);
    private static final Uri TOTAL_SALES_URI = ShopProvider.getContentUri(TotalSalesQuery.CONTENT_PATH);
    private static final Uri TIPS_URI = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);
    private static final Uri CASHBACK_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);
    private static final Uri OPENED_TRANSACTIONS_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);
    private static final Uri TIMESHEET_URI = ShopProvider.getContentWithLimitUri(EmployeeTimesheetTable.URI_CONTENT, 1);
    private static final Uri SQL_COMMAND_URI = ShopProvider.contentUriWithLimit(SqlCommandTable.URI_CONTENT, 1);

    private static final int LOADER_SHIFT_ID = 1;
    private static final int LOADER_ALERT_ID = 2;
    private static final int LOADER_OPENED_TRANSACTIONS_ID = 3;
    private static final int LOADER_TIMESHEET_ID = 4;
    private static final int LOADER_ACTIVATION_ID = 5;
    private static final int LOADER_SQL_COMMAND = 6;

    @ViewById
    protected TextView operatorNameValueLabel;

    @ViewById
    protected ViewGroup totalSalesContainer;
    @ViewById
    protected TextView totalSalesValueLabel;
    @ViewById
    protected TextView totalSalesCashValueLabel;
    @ViewById
    protected TextView totalSalesCreditValueLabel;
    @ViewById
    protected TextView totalSalesDebitValueLabel;
    @ViewById
    protected TextView totalSalesEbtValueLabel;
    @ViewById
    protected TextView totalSalesOtherValueLabel;

    @ViewById
    protected ViewGroup cashOnDrawerContainer;
    @ViewById
    protected TextView cashOnDrawerLabel;
    @ViewById
    protected TextView openingAmountLabel;
    @ViewById
    protected TextView cashOnDrawerValueLabel;
    @ViewById
    protected TextView openingAmountValueLabel;

    @ViewById
    protected TextView shiftTimeLabel;
    @ViewById
    protected View shiftActionImage;

    @ViewById
    protected TextView closeShiftLabel;
    @ViewById
    protected TextView openShiftLabel;

    @ViewById
    protected ViewGroup registerButton;
    @ViewById
    protected ViewGroup inventoryButton;
    @ViewById
    protected ViewGroup noSaleButton;
    @ViewById
    protected ViewGroup dropsAndPayoutsButton;
    @ViewById
    protected ViewGroup employeesButton;
    @ViewById
    protected ViewGroup customersButton;
    @ViewById
    protected ViewGroup reportsButton;
    @ViewById
    protected ViewGroup prepaidButton;

    @ViewById
    protected EditText usbScannerInput;

    private MenuItem alertCounterItem;
    private TextView alertCounterView;

    private SalesStatisticsModel salesStatisticsModel;
    private boolean isShiftOpened;

    private BigDecimal closeAmount;
    private BigDecimal openAmount;
    private BigDecimal cashDrawerMovementAmount;

    private BigDecimal dropAmount;
    private String dropComment;
    private MovementType dropType;

    private TaskHandler closeDrawerCommandHandler;

    private static final Handler handler = new Handler();

    private int alertCounter = 0;
    private NoSaleOpenDrowerCallback noSaleOpenDrowerCallback = new NoSaleOpenDrowerCallback();
    private WaitForCloseDrawerCallback waitForCloseDrawerCallback = new WaitForCloseDrawerCallback();
    private OpenDrawerCallback openDrawerCallback = new OpenDrawerCallback();
    private StartShiftCallback startShiftCallback = new StartShiftCallback();
    private StopShiftCallback stopShiftCallback = new StopShiftCallback();
    private ClockInCallback clockInCallback = new ClockInCallback();
    private ClockOutCallback clockOutCallback = new ClockOutCallback();
    private MovementsOpenDrawerCallback movementsOpenDrawerCallback = new MovementsOpenDrawerCallback();
    private MovementWaitForCloseDrawerCallback movementWaitForCloseDrawerCallback = new MovementWaitForCloseDrawerCallback();
    private PrintDropPayoutCallback printDropPayoutCallback = new PrintDropPayoutCallback();

    private MenuItem activationMenuItem;

    private List<ActivationCarrierModel> activationCarriers;
    private int openedTrnsactionsCount;

    private boolean goToSaleOrder;
    private boolean isSqlCommandLoaderFinished = true;
    private boolean isShiftLoaderFinished = true;
    private boolean isEmployeeTimeshiftLoaderFinished = true;

    public static final int EXTRA_CODE = 1;

    public static String EXTRA_FORCE_LOGOUT = "EXTRA_FORCE_LOGOUT";

    public static void startClearTop(Context context) {
        DashboardActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
    }

    public static void start(Context context) {
        DashboardActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Crittercism.initialize(getApplicationContext(), "5537af9f7365f84f7d3d6f29");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        if (getApp().isUserLogin() && !getApp().isTrainingMode() && getApp().isOfflineModeExpired()) {
            logout(false);
            Toast.makeText(this, R.string.offline_mode_error_toast_message_logout, Toast.LENGTH_SHORT).show();
        }

        if (usbScannerInput != null)
            usbScannerInput.setInputType(0);
    }

    @AfterTextChange
    protected void usbScannerInputAfterTextChanged(Editable s) {
        String st = s.toString();
        if (st.contains("\n") || st.contains("\r") || st.contains("\r\n")) {
            barcodeReceivedFromSerialPort(st);
            s.clear();
        }
    }


    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        Logger.d("DashboardActivity barcodeReceivedFromSerialPort onReceive:" + barcode);
        errorAlarm();
    }

    protected void showCheckInOrOutDialog() {
        TimesheetNewFragment.show(this, new TimesheetNewFragment.OnTimesheetListener() {
            @Override
            public void onCheckInSelected() {

                TimesheetFragment.show(DashboardActivity.this, TimesheetFragment.Type.CLOCK_IN, null, new TimesheetFragment.OnTimesheetListener() {
                    @Override
                    public void onCredentialsEntered(String login/*, String password*/) {
                        TimesheetFragment.hide(DashboardActivity.this);
                        WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_clock_in));
                        ClockInCommand.start(DashboardActivity.this, login/*, password*/, clockInCallback);
                    }
                });
            }

            @Override
            public void onCancelSelected() {

            }

            @Override
            public void onCheckOutSelected() {
                TimesheetFragment.show(DashboardActivity.this, TimesheetFragment.Type.CLOCK_OUT, null, new TimesheetFragment.OnTimesheetListener() {
                    @Override
                    public void onCredentialsEntered(String login/*, String password*/) {
                        TimesheetFragment.hide(DashboardActivity.this);
                        WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_clock_out));
                        ClockOutCommand.start(DashboardActivity.this, login/*, password*/, clockOutCallback);
                    }
                });
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (getApp().isUserLogin() && !getApp().isTrainingMode() && getApp().isOfflineModeExpired()) {
            logout(false);
            Toast.makeText(this, R.string.offline_mode_error_toast_message_logout, Toast.LENGTH_SHORT).show();

            initViews();
        }
    }

    /*@SuppressWarnings("PointlessBooleanExpression")
    private void checkDeviceSettings() {
        //noinspection ConstantConditions

        if (BuildConfig.SUPPORT_PRINTER && !getApp().getShopPref().ignoreDevices().get() *//*&& !getApp().getShopPref().printerIp().exists()*//*) {
            AlertDialogFragment.showAlert(this, R.string.device_error, getString(R.string.printer_not_configured), R.string.btn_configure,
                    new DeviceAlertCallback(),
                    new DeviceAlertCancelCallback());
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }*/

    @AfterViews
    protected void initViews() {

        if (!getApp().isUserLogin()) {
            showLoginFragment();
            updateUI(null);
        }
        if (!getApp().hasPermission(Permission.CASH_DRAWER_MONEY)) {
            cashOnDrawerContainer.setVisibility(View.GONE);
            totalSalesContainer.setVisibility(View.GONE);
        }
    }

    private void showLoginFragment() {
        LoginOuterFragment.show(this, new OnLoginCompleteListener() {

            @Override
            public void onLoginComplete() {
                onLogin();
            }

            @Override
            public boolean onLoginComplete(String lastUncompletedSaleOrderGuid) {
                if (!TextUtils.isEmpty(lastUncompletedSaleOrderGuid)) {
                    goToSaleOrder = true;
                    isSqlCommandLoaderFinished = true;
                    isShiftLoaderFinished = true;
                    isEmployeeTimeshiftLoaderFinished = true;
                    setCurrentOrder(lastUncompletedSaleOrderGuid);
                }

                onLogin();

                return true;
            }

            private void setCurrentOrder(String lastUncompletedSaleOrderGuid) {
                if (lastUncompletedSaleOrderGuid.equals(getApp().getCurrentOrderGuid()))
                    return;

                getApp().setCurrentOrderGuid(lastUncompletedSaleOrderGuid);
                HashSet<String> salesmanGuids = new HashSet<String>();
                if (getApp().getOperator().commissionEligible) {
                    salesmanGuids.add(getApp().getOperatorGuid());
                }
                getApp().setSalesmanGuids(salesmanGuids);
            }

        }, Mode.LOGIN);
    }

    private void onLogin() {
        //checkDeviceSettings();
        onLogin(false);
        startCheckUpdateService(false);
    }


    private void onLogin(boolean tempLogin) {
        setTitle();
        setOperatorName();
        setStatisticsContainers();
        if (!tempLogin) {
            if (getApp().getShopPref().need2DownloadAfter1stLaunch().get()) {
                if (goToSaleOrder)
                    isSqlCommandLoaderFinished = false;
                getSupportLoaderManager().restartLoader(LOADER_SQL_COMMAND, null, sqlCommandLoader);
            } else {
                OfflineCommandsService.startUpload(DashboardActivity.this);
                OfflineCommandsService.scheduleSyncAction(DashboardActivity.this);
            }

            if (getApp().isTipsEnabledWasChanged()) {
                getApp().setTipsEnabledWasChanged(false);
                Toast.makeText(this, getApp().isTipsEnabled() ? R.string.warning_message_tips_enabled : R.string.warning_message_tips_disabled, Toast.LENGTH_LONG).show();
            }
            if (goToSaleOrder) {
                isShiftLoaderFinished = false;
                isEmployeeTimeshiftLoaderFinished = false;
            }
        }
        need2CollectData();
        invalidateOptionsMenu();
    }

    private void setTitle() {
        CharSequence title;
        String activityTitle = getString(R.string.dashboard_activity_label);
        String shopName = getApp().getShopInfo().name;
        title = getString(R.string.dashboard_activity_title_tmpl, activityTitle, shopName);
        if (getApp().isTrainingMode()) {
            SpannableString spannableTitle = new SpannableString(getString(R.string.training_mode_title_tmpl, title));
            spannableTitle.setSpan(new ForegroundColorSpan(Color.RED), title.length(), spannableTitle.length(), 0);
            title = spannableTitle;
        }
        getActionBar().setTitle(title);
    }

    private void setStatisticsContainers() {
        boolean hasShiftPermission = getApp().hasPermission(Permission.OPEN_CLOSE_SHIFT);
        totalSalesContainer.setVisibility(isShiftOpened && hasShiftPermission && getApp().hasPermission(Permission.CASH_DRAWER_MONEY) ? View.VISIBLE : View.GONE);
        cashOnDrawerContainer.setVisibility(hasShiftPermission && getApp().hasPermission(Permission.CASH_DRAWER_MONEY) ? View.VISIBLE : View.GONE);
    }

    private void need2CollectData() {
        if (!getApp().isUserLogin())
            return;
        getSupportLoaderManager().restartLoader(LOADER_SHIFT_ID, null, shiftLoader);
        getSupportLoaderManager().restartLoader(LOADER_ALERT_ID, null, alertCounterLoader);
        getSupportLoaderManager().restartLoader(LOADER_OPENED_TRANSACTIONS_ID, null, openedTransactionsLoader);
        getSupportLoaderManager().restartLoader(LOADER_TIMESHEET_ID, null, timesheetLoader);
        getSupportLoaderManager().restartLoader(LOADER_ACTIVATION_ID, null, activationLoader);
    }

    private void need2StopCollectData() {
        getSupportLoaderManager().destroyLoader(LOADER_SHIFT_ID);
        getSupportLoaderManager().destroyLoader(LOADER_ALERT_ID);
        getSupportLoaderManager().destroyLoader(LOADER_OPENED_TRANSACTIONS_ID);
        getSupportLoaderManager().destroyLoader(LOADER_TIMESHEET_ID);
        getSupportLoaderManager().destroyLoader(LOADER_ACTIVATION_ID);
    }

    @OnActivityResult(EXTRA_CODE)
    protected void onResult(int code, Intent data) {
        if (data != null && code == EXTRA_CODE) {
            if (data.getBooleanExtra(EXTRA_FORCE_LOGOUT, false))
                logout(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        setOperatorName();
        need2CollectData();
        startUpdateShiftTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        need2StopCollectData();
        stopUpdateShiftTime();
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    private void checkGoToSaleOrder() {
        if (!goToSaleOrder)
            return;

        if (isSqlCommandLoaderFinished && isShiftLoaderFinished && isEmployeeTimeshiftLoaderFinished) {
            goToSaleOrder = false;

            if (isFinishing() || isDestroyed() || !getApp().isUserLogin())
                return;

            registerButtonClicked();
        }
    }

    private void clearGoToSaleOrder() {
        goToSaleOrder = false;
        isSqlCommandLoaderFinished = true;
        isShiftLoaderFinished = true;
        isEmployeeTimeshiftLoaderFinished = true;
    }

    @OptionsItem
    protected void actionCreditReceiptsSelected() {
        if (!getApp().hasPermission(Permission.REPORTS)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    actionCreditReceiptsSelected();
                }
            }, Permission.REPORTS);
            return;
        }
        CreditReceiptsActivity.start(this);
    }

    @OptionsItem
    protected void actionSendLogSelected() {
        WaitDialogFragment.show(this, getString(R.string.wait_message_email));
        SendLogCommand.start(this, false, new SendLogCallback());
    }

    @OptionsItem
    protected void actionExportDbSelected() {
        if (!getApp().hasPermission(Permission.ADMIN)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    actionExportDbSelected();
                }
            }, Permission.ADMIN);
            return;
        }

        FileChooserFragment.show(this, Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_export_db));
                ExportDatabaseCommand.start(DashboardActivity.this, file.getAbsolutePath(), new ExportDatabaseCallback());
            }
        });
    }

    @OptionsItem
    protected void actionOrderItemsSelected() {
        if (!getApp().hasPermission(Permission.SALES_RETURN)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    actionOrderItemsSelected();
                }
            }, Permission.SALES_RETURN);
            return;
        }
        HistoryActivity.start(DashboardActivity.this);
    }

    @OptionsItem
    protected void actionActivationSelected() {
        if (activationCarriers == null || activationCarriers.isEmpty())
            return;

        if (activationCarriers.size() == 1) {
            ActivationActivity.start(this, activationCarriers.get(0).url);
        } else {
            ActivationTypeChoosingFragmentDialog.show(this, activationCarriers, false);
        }
    }

    @OptionsItem
    protected void actionLogoutSelected() {
        logout(true);
    }

    @OptionsItem
    protected void actionOptionsSelected() {
        if (!getApp().hasPermission(Permission.ADMIN)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    actionOptionsSelected();
                }
            }, Permission.ADMIN);
            return;
        }
        SettingsActivity.start(this);
    }


    @Override
    protected void onTempLogout() {
        onLogin(true);
    }

    private void logout(boolean isManual) {
        clearGoToSaleOrder();
        restoreSystemScreenOffTimeout();
        getApp().setOperatorWithPermissions(null, true);

        if (!isManual)
            return;

        need2StopCollectData();
        setOperatorName();
        showLoginFragment();
        stopService(new Intent(this, AutoUpdateService.class));
    }

    private void restoreSystemScreenOffTimeout() {
        ScreenUtils.setScreenOffTimeout(this, getApp().getShopPref().prevScreenTimeout().get());
    }

    @Click
    protected void prepaidButtonClicked() {
        PrepaidProcessorActivity.start(this, getBillpaymentActivate(), getSunpassActivate());
    }

    @Click
    protected void registerButtonClicked() {
        if (getApp().getStartView() == ShopInfoViewJdbcConverter.ShopInfo.ViewType.QUICK_SERVICE) {
            QuickServiceActivity.start(this);
        } else {
            CashierActivity.start(this);
        }
    }

    private boolean getBillpaymentActivate() {
        return TcrApplication.get().getBillPaymentActivated();
    }

    private boolean getSunpassActivate() {
        return TcrApplication.get().getSunpassActivated();
    }

    @Click
    protected void inventoryButtonClicked() {
        runInventory(false);
    }

    private void runInventory(final boolean nearTheEnd) {
        if (!getApp().hasPermission(Permission.INVENTORY_MODULE)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    runInventory(nearTheEnd);
                }
            }, Permission.INVENTORY_MODULE);
            return;
        }
        InventoryActivity.start(this, nearTheEnd);
    }

    @Click
    protected void noSaleButtonClicked() {
        try2OpenDrawer(false, noSaleOpenDrowerCallback);
    }

    @Click
    protected void dropsAndPayoutsButtonClicked() {
        try2OpenDrawer(false, movementsOpenDrawerCallback);
    }

    @Click
    protected void reportsButtonClicked() {
        ReportsActivity.start(this);
    }

    @Click
    protected void shiftActionButtonContainerClicked() {
        if (!getApp().hasPermission(Permission.OPEN_CLOSE_SHIFT)) {
            PermissionFragment.showCancelable(this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    onLogin(true);
                    shiftActionButtonContainerClicked();
                }
            }, Permission.OPEN_CLOSE_SHIFT);
            return;
        }

        if (getApp().isTipsEnabled() && isShiftOpened && openedTrnsactionsCount > 0) {
            AlertDialogFragment.showAlertWithSkip(DashboardActivity.this,
                    R.string.dashboard_activity_opened_transactions_warning_title,
                    getString(R.string.dashboard_activity_opened_transactions_warning_message),
                    R.string.btn_close, new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            HistoryActivity.start(DashboardActivity.this, true);
                            return true;
                        }
                    }, new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            startShiftAction();
                            return true;
                        }
                    });
            return;
        }

        startShiftAction();
    }

    private void startShiftAction() {
        if (!isShiftOpened) {
            showOpenAmountDialog();
        } else {
            try2OpenDrawer(false, openDrawerCallback);
        }
    }

    private void try2OpenDrawer(boolean searchByMac, BaseOpenDrawerCallback callback) {
        WaitDialogFragment.show(this, getString(R.string.wait_message_open_drawer));
        OpenDrawerCommand.start(DashboardActivity.this, searchByMac, callback);
    }

    @Click
    protected void clockInButtonContainerClicked() {
        TimesheetFragment.show(this, TimesheetFragment.Type.CLOCK_IN, null, new TimesheetFragment.OnTimesheetListener() {
            @Override
            public void onCredentialsEntered(String login/*, String password*/) {
                TimesheetFragment.hide(DashboardActivity.this);
                WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_clock_in));
                ClockInCommand.start(DashboardActivity.this, login/*, password*/, clockInCallback);
            }
        });
    }

    @Click
    protected void clockOutButtonContainerClicked() {
        TimesheetFragment.show(this, TimesheetFragment.Type.CLOCK_OUT, null, new TimesheetFragment.OnTimesheetListener() {
            @Override
            public void onCredentialsEntered(String login/*, String password*/) {
                TimesheetFragment.hide(DashboardActivity.this);
                WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_clock_out));
                ClockOutCommand.start(DashboardActivity.this, login/*, password*/, clockOutCallback);
            }
        });
    }

    @Click
    protected void employeesButtonClicked() {
        EmployeesActivity.start(this);
    }

    @Click
    protected void customersButtonClicked() {
        CustomersActivity.start(this);
    }

    private void onCloseAmountEntered(boolean skipDrawer, BigDecimal value) {
        closeAmount = value;

        if (skipDrawer) {
            waitForCloseDrawerCallback.onDrawerClosed();
        } else {
            showCloseDrawerDialog(true);
            closeDrawerCommandHandler = WaitForCloseDrawerCommand.start(DashboardActivity.this, waitForCloseDrawerCallback);
        }
    }

    private void onEmptyOpenAmount(BigDecimal value) {
        WaitDialogFragment.show(this, getString(R.string.wait_message_start_shift));
        StartShiftCommand.start(this, startShiftCallback, value);
    }

    private void showPrintXReportDialog() {
        final String shiftGuid = salesStatisticsModel.shiftModel.guid;
        PrintXReportFragment.show(this, shiftGuid);
    }

    private void startStopShift() {
        WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_stop_shift));
        StopShiftCommand.start(DashboardActivity.this, stopShiftCallback, salesStatisticsModel.shiftModel.guid, closeAmount);
        closeAmount = null;
    }

    private void showCloseDrawerDialog(boolean showBackButton) {
        CloseDrawerFragment.show(this, showBackButton, new CloseDrawerFragment.OnCloseDrawerListener() {
            @Override
            public void onBack() {
                closeDrawerCommandHandler.cancel(DashboardActivity.this, 0, null);
                showCloseAmountDialog(false, closeAmount);
            }
        });
    }

    private void showCloseAmountDialog(boolean skipDrawer) {
        showCloseAmountDialog(skipDrawer, null);
    }

    private void showCloseAmountDialog(boolean skipDrawer, BigDecimal closeAmount) {
        this.closeAmount = null;
        CloseAmountFragment.show(this, skipDrawer, closeAmount, new CloseAmountFragment.OnEditAmountListener() {
            @Override
            public void onConfirm(boolean skipDrawer, BigDecimal value) {
                onCloseAmountEntered(skipDrawer, value);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void showOpenAmountDialog() {
        OpenAmountFragment.show(this, new OpenAmountFragment.OnEditAmountListener() {
            @Override
            public void onConfirm(BigDecimal value) {
                onEmptyOpenAmount(value);
            }

            @Override
            public void onPutCash(boolean ignoreDrawable, BigDecimal value) {
                openAmount = value;
                if (ignoreDrawable) {
                    waitForCloseDrawerCallback.onDrawerClosed();
                } else {
                    showPutCashDialog();
                    closeDrawerCommandHandler = WaitForCloseDrawerCommand.start(DashboardActivity.this, waitForCloseDrawerCallback);
                }
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void showPutCashDialog() {
        PutCashFragment.show(this, true);
    }

    private void showCashDrawerMovementEditDialog() {
        CashDrawerMovementEditFragment.show(this, salesStatisticsModel.getCashOnDrawer(), new CashDrawerMovementEditFragment.OnEditAmountListener() {
            @Override
            public void onConfirm(BigDecimal value, String comment, MovementType movementType) {
                AddCashDrawerMovementCommand.start(DashboardActivity.this, null, salesStatisticsModel.shiftModel.guid, movementType, value, comment);
                PutCashFragment.show(DashboardActivity.this, false);
                WaitForCloseDrawerCommand.start(DashboardActivity.this, movementWaitForCloseDrawerCallback);
            }
        });
    }

    private void startUpdateShiftTime() {
        if (isShiftOpened) {
            long secondDelay = 1000L - (System.currentTimeMillis() % 1000L);
            handler.postDelayed(shiftTimeRunnable, secondDelay);
        }
    }

    private void stopUpdateShiftTime() {
        handler.removeCallbacks(shiftTimeRunnable);
    }

    private void setShiftTime() {
        ShiftModel shiftModel = salesStatisticsModel.shiftModel;
        long startTime = shiftModel.startTime.getTime();
        long endTime = shiftModel.endTime == null ? 0L : shiftModel.endTime.getTime();
        long currentTime = new Date().getTime();
        shiftTimeLabel.setText(DateUtils.formatInterval(startTime == 0L || endTime != 0L ? 0L : (currentTime - startTime)));
    }

    private void setOperatorName() {
        operatorNameValueLabel.setText(getApp().isUserLogin() ? getApp().getOperator().fullName() : "");
    }

    private void updateUI(SalesStatisticsModel salesStatisticsModel) {
        this.salesStatisticsModel = salesStatisticsModel;
        ShiftModel shiftModel = salesStatisticsModel == null ? null : salesStatisticsModel.shiftModel;

        isShiftOpened = shiftModel != null && shiftModel.endTime == null;

        setModuleEnabled(inventoryButton, getApp().hasPermission(Permission.INVENTORY_MODULE));
        setModuleEnabled(noSaleButton, getApp().hasPermission(Permission.NO_SALE));
        setModuleEnabled(dropsAndPayoutsButton, getApp().hasPermission(Permission.DROPS_AND_PAYOUTS) && isShiftOpened);
        setModuleEnabled(employeesButton, getApp().hasPermission(Permission.EMPLOYEE_MANAGEMENT));
        setModuleEnabled(customersButton, getApp().hasPermission(Permission.CUSTOMER_MANAGEMENT));
        setModuleEnabled(reportsButton, getApp().hasPermission(Permission.REPORTS));
        setModuleEnabled(prepaidButton, isShiftOpened && getApp().isPrepaidUserValid());

        setShiftActionLabel(isShiftOpened);
        if (isShiftOpened) {
            shiftActionImage.setBackgroundResource(R.drawable.shift_close_btn_bg);

            UiHelper.showPrice(totalSalesValueLabel, salesStatisticsModel.getTotalSales());
            UiHelper.showPrice(totalSalesCashValueLabel, salesStatisticsModel.cash);
            UiHelper.showPrice(totalSalesCreditValueLabel, salesStatisticsModel.credit);
            UiHelper.showPrice(totalSalesDebitValueLabel, salesStatisticsModel.debit);
            UiHelper.showPrice(totalSalesEbtValueLabel, salesStatisticsModel.ebt);
            UiHelper.showPrice(totalSalesOtherValueLabel, salesStatisticsModel.other);

            cashOnDrawerLabel.setText(getString(R.string.dashboard_activity_cash_in_drawer_label));
            openingAmountLabel.setText(getString(R.string.dashboard_activity_opening_amount));
            UiHelper.showPrice(cashOnDrawerValueLabel, salesStatisticsModel.getCashOnDrawer());
            UiHelper.showPrice(openingAmountValueLabel, shiftModel.openAmount);

            shiftTimeLabel.setVisibility(View.VISIBLE);
            setShiftTime();
            startUpdateShiftTime();

            boolean registerPermitted = getApp().hasPermission(Permission.SALES_TRANSACTION);
            setModuleEnabled(registerButton, registerPermitted);
        } else {
            boolean isShiftExists = shiftModel != null;

            shiftActionImage.setBackgroundResource(R.drawable.shift_open_btn_bg);

            cashOnDrawerLabel.setText(getString(R.string.dashboard_activity_actual_cash_in_drawer_label));
            openingAmountLabel.setText(getString(R.string.dashboard_activity_drawer_difference));
            UiHelper.showPrice(cashOnDrawerValueLabel, isShiftExists ? shiftModel.closeAmount : BigDecimal.ZERO);
            UiHelper.showPrice(openingAmountValueLabel, isShiftExists ? salesStatisticsModel.getDrawerDifference() : BigDecimal.ZERO);

            shiftTimeLabel.setVisibility(View.GONE);
            stopUpdateShiftTime();

            setModuleEnabled(registerButton, false);
        }

        setStatisticsContainers();
    }

    private void setShiftActionLabel(boolean isShiftOpened) {
        closeShiftLabel.setVisibility(isShiftOpened ? View.VISIBLE : View.GONE);
        openShiftLabel.setVisibility(!isShiftOpened ? View.VISIBLE : View.GONE);
    }

    private void setModuleEnabled(ViewGroup module, boolean enabled) {
        module.setEnabled(enabled);
        module.setActivated(!enabled);
    }

    private void storeShiftGuid(SalesStatisticsModel salesStatisticsModel) {
        ShiftModel shiftModel = salesStatisticsModel == null ? null : salesStatisticsModel.shiftModel;
        boolean isShiftOpened = shiftModel != null && shiftModel.endTime == null;
        getApp().setShiftGuid(shiftModel == null ? null : shiftModel.guid);
        getApp().setShiftOpened(isShiftOpened);
    }

    private Runnable shiftTimeRunnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 1000L);
            setShiftTime();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean r = super.onCreateOptionsMenu(menu);
        //menu.findItem(R.id.action_send_log).setVisible(BuildConfig.DEBUG);
        alertCounterItem = menu.findItem(R.id.action_alerts);
        alertCounterView = (TextView) alertCounterItem.getActionView().findViewById(R.id.ab_counter);
        alertCounterItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runInventory(true);
            }
        });
        return r;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_credit_receipts).setVisible(getApp().getShopInfo().useCreditReceipt);
        activationMenuItem = menu.findItem(R.id.action_activation);
        activationMenuItem.setEnabled(activationCarriers != null && !activationCarriers.isEmpty());
        updateAlertCounter(alertCounter);
        return true;
    }

    private void updateAlertCounter(int count) {
        alertCounterView.setText(String.valueOf(count));
        boolean enable = count != 0;
        alertCounterItem.getActionView().setEnabled(enable);
        alertCounterItem.getActionView().setActivated(enable);
        alertCounterItem.setVisible(enable);
    }

    private LoaderCallbacks<Optional<SalesStatisticsModel>> shiftLoader = new LoaderCallbacks<Optional<SalesStatisticsModel>>() {

        @Override
        public Loader<Optional<SalesStatisticsModel>> onCreateLoader(int arg0, Bundle arg1) {
            return CursorLoaderBuilder.forUri(SHIFT_URI)
                    .where(ShiftTable.REGISTER_ID + " = ?", getApp().getRegisterId())
                    .orderBy(ShopStore.ShiftTable.START_TIME + " DESC")
                    .wrap(new SalesStatisticsConverter(DashboardActivity.this))
                    .build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Optional<SalesStatisticsModel>> loader, Optional<SalesStatisticsModel> salesStatisticsModel) {
            storeShiftGuid(salesStatisticsModel.orNull());
            updateUI(salesStatisticsModel.orNull());

            if (goToSaleOrder && !isShiftLoaderFinished) {
                isShiftLoaderFinished = true;
                checkGoToSaleOrder();
            }
        }

        @Override
        public void onLoaderReset(Loader<Optional<SalesStatisticsModel>> loader) {
            if (!getApp().isUserLogin())
                updateUI(null);
        }

    };

    private LoaderCallbacks activationLoader = new LoaderCallbacks<List<ActivationCarrierModel>>() {

        @Override
        public Loader<List<ActivationCarrierModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ActivationCarrierTable.URI_CONTENT))
                    .where(ActivationCarrierTable.IS_ACTIVE + " = ?", 1)
                    .transform(new ListConverterFunction<ActivationCarrierModel>() {
                        @Override
                        public ActivationCarrierModel apply(Cursor cursor) {
                            return new ActivationCarrierModel(cursor);
                        }
                    }).build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<ActivationCarrierModel>> loader, List<ActivationCarrierModel> data) {
            activationCarriers = data;
            if (activationMenuItem != null) {
                activationMenuItem.setEnabled(data != null && !data.isEmpty());
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ActivationCarrierModel>> loader) {
        }
    };

    public static class SalesStatisticsConverter extends ListConverterFunction<Optional<SalesStatisticsModel>> {

        private final Context context;

        public SalesStatisticsConverter(Context context) {
            this.context = context;
        }

        @Override
        public Optional<SalesStatisticsModel> apply(Cursor c) {
            super.apply(c);

            SalesStatisticsModel salesStatisticsModel = null;
            if (c.moveToFirst()) {
                ShiftModel shiftModel = getShiftModel(c);
                salesStatisticsModel = new SalesStatisticsModel(shiftModel);

                Cursor cursor = loadSalesStatistics(shiftModel);
                while (cursor.moveToNext()) {
                    gatherSalesStatistics(cursor, salesStatisticsModel);
                }

                cursor = loadTipsStatisctics(shiftModel);
                while (cursor.moveToNext()) {
                    gatherTipsStaticstics(cursor, salesStatisticsModel);
                }

                cursor = loadCashBackStatisctics(shiftModel);
                while (cursor.moveToNext()) {
                    gatherCashBackStaticstics(cursor, salesStatisticsModel);
                }

                cursor.close();
            }
            return Optional.fromNullable(salesStatisticsModel);
        }

        private ShiftModel getShiftModel(Cursor c) {
            return new ShiftModel(
                    c.getString(indexHolder.get(ShopStore.ShiftTable.GUID)),
                    _nullableDate(c, indexHolder.get(ShopStore.ShiftTable.START_TIME)),
                    _nullableDate(c, indexHolder.get(ShopStore.ShiftTable.END_TIME)),
                    c.getString(indexHolder.get(ShiftTable.OPEN_MANAGER_ID)),
                    c.getString(indexHolder.get(ShiftTable.CLOSE_MANAGER_ID)),
                    c.getLong(indexHolder.get(ShiftTable.REGISTER_ID)),
                    _decimal(c, indexHolder.get(ShopStore.ShiftTable.OPEN_AMOUNT)),
                    _decimal(c, indexHolder.get(ShopStore.ShiftTable.CLOSE_AMOUNT)));
        }

        private FluentCursor loadSalesStatistics(ShiftModel shiftModel) {
            return ProviderAction.query(TOTAL_SALES_URI)
                    .where("",
                            shiftModel.guid,
                            PaymentTransactionModel.PaymentStatus.FAILED.ordinal(),
                            shiftModel.guid)
                    .perform(context);
        }

        private void gatherSalesStatistics(Cursor cursor, SalesStatisticsModel salesStatisticsModel) {
            BigDecimal amount = _decimal(cursor, 0).subtract(cursor.isNull(2) ? BigDecimal.ZERO : _decimal(cursor, 2));
            Integer gatewayOrdinal = cursor.isNull(1) ? null : cursor.getInt(1);
            PaymentGateway gateway = gatewayOrdinal == null ? null : PaymentGateway.values()[gatewayOrdinal];

            if (gateway != null) {
                switch (gateway) {
                    /*CASH*/
                    case CASH:
                        salesStatisticsModel.cash = salesStatisticsModel.cash.add(amount);
                        break;
                    /*CREDIT*/
                    case BLACKSTONE:
                    case PAX:
                    case PAYPAL:
                        salesStatisticsModel.credit = salesStatisticsModel.credit.add(amount);
                        break;
                    /*DEBIT*/
                    case PAX_DEBIT:
                        salesStatisticsModel.debit = salesStatisticsModel.debit.add(amount);
                        break;
                    /*EBT*/
                    case PAX_EBT_FOODSTAMP:
                    case PAX_EBT_CASH:
                        salesStatisticsModel.ebt = salesStatisticsModel.ebt.add(amount);
                        break;
                    /*OTHER*/
                    default:
                        salesStatisticsModel.other = salesStatisticsModel.other.add(amount);
                }
            } else {
                salesStatisticsModel.movements = salesStatisticsModel.movements.add(amount);
            }
        }

        private FluentCursor loadTipsStatisctics(ShiftModel shiftModel) {
            return ProviderAction.query(TIPS_URI)
                    .projection(EmployeeTipsTable.AMOUNT, EmployeeTipsTable.PAYMENT_TYPE)
                    .where(EmployeeTipsTable.SHIFT_ID + " = ?", shiftModel.guid)
                    .perform(context);
        }

        private void gatherTipsStaticstics(Cursor c, SalesStatisticsModel salesStatisticsModel) {
            BigDecimal amount = _decimal(c, 0);
            TipsModel.PaymentType paymentType = _tipsPaymentType(c, 1);
            if (paymentType == TipsModel.PaymentType.CASH) {
                salesStatisticsModel.cashTips = salesStatisticsModel.cashTips.add(amount);
            } else {
                salesStatisticsModel.creditTips = salesStatisticsModel.creditTips.add(amount);
            }
        }

        private FluentCursor loadCashBackStatisctics(ShiftModel shiftModel) {
            return ProviderAction.query(CASHBACK_URI)
                    .projection(PaymentTransactionTable.CASH_BACK)
                    .where(PaymentTransactionTable.SHIFT_GUID + " = ?", shiftModel.guid)
                    .perform(context);
        }

        private void gatherCashBackStaticstics(Cursor c, SalesStatisticsModel salesStatisticsModel) {
            BigDecimal cashBackAmount = _decimal(c, 0);
            salesStatisticsModel.cashBack = salesStatisticsModel.cashBack.add(cashBackAmount);

        }

    }

    public static class SalesStatisticsModel {

        public final ShiftModel shiftModel;

        public BigDecimal cash = BigDecimal.ZERO;
        public BigDecimal credit = BigDecimal.ZERO;
        public BigDecimal debit = BigDecimal.ZERO;
        public BigDecimal ebt = BigDecimal.ZERO;
        public BigDecimal other = BigDecimal.ZERO;
        public BigDecimal movements = BigDecimal.ZERO;
        public BigDecimal cashTips = BigDecimal.ZERO;
        public BigDecimal creditTips = BigDecimal.ZERO;
        public BigDecimal cashBack = BigDecimal.ZERO;

        public SalesStatisticsModel(ShiftModel shiftModel) {
            this.shiftModel = shiftModel;
        }

        public BigDecimal getTotalSales() {
            return cash.add(credit).add(debit).add(ebt).add(other);
        }

        public BigDecimal getCashOnDrawer() {
            return cash.add(movements).add(shiftModel.openAmount).add(cashTips).subtract(cashBack);
        }

        public BigDecimal getDrawerDifference() {
            return shiftModel.closeAmount.subtract(getCashOnDrawer());
        }

    }

    public class WaitForCloseDrawerCallback extends BaseWaitForCloseDrawerCallback {

        @Override
        protected void onDrawerClosed() {
            if (isShiftOpened) {
                CloseDrawerFragment.hide(DashboardActivity.this);
                CloseAmountFragment.hide(DashboardActivity.this);
                if (closeAmount != null) {
                    startStopShift();
                }
            } else {
                PutCashFragment.hide(DashboardActivity.this);
                WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_start_shift));
                StartShiftCommand.start(DashboardActivity.this, startShiftCallback, openAmount);
                openAmount = null;
            }
        }

        private void handleCancelBtn() {
            if (isShiftOpened) {
                CloseDrawerFragment.hide(DashboardActivity.this);
                showCloseAmountDialog(false, closeAmount);
            } else {
                PutCashFragment.hide(DashboardActivity.this);
                showOpenAmountDialog();
            }
        }

        private void handleTryAgainBtn() {
            closeDrawerCommandHandler = WaitForCloseDrawerCommand.start(DashboardActivity.this, this);
        }

        @Override
        public void onDrawerCloseError(PrinterError error) {
            showErrorMessage(R.string.close_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)));
        }

        @Override
        protected void onDrawerTimeoutError() {
            showErrorMessage(R.string.close_drawer_error_title, getString(R.string.close_drawer_error_msg_close_id));
        }

        private void showErrorMessage(int title, String message) {
            AlertDialogFragment.showAlertWithSkip(DashboardActivity.this,
                    title,
                    message,
                    R.string.btn_try_again,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            handleTryAgainBtn();
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            handleCancelBtn();
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            onDrawerClosed();
                            return true;
                        }
                    }
            );
        }

    }

    public class OpenDrawerCallback extends BaseOpenDrawerCallback {

        @Override
        protected void onDrawerIPnoFound() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(
                    DashboardActivity.this,
                    R.string.open_drawer_error_title,
                    getString(R.string.error_message_drawer_ip_not_found),
                    R.string.btn_ok,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(true, openDrawerCallback);
                            return true;
                        }
                    }
            );
        }

        @Override
        public void onDrawerOpened() {
            onDrawerOpened(false);
        }

        private void onDrawerOpened(boolean skipDrawer) {
            WaitDialogFragment.hide(DashboardActivity.this);
            if (isShiftOpened) {
                showCloseAmountDialog(skipDrawer);
            }
        }

        @Override
        public void onDrawerOpenError(PrinterError error) {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlertWithSkip(DashboardActivity.this, R.string.open_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)), new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(false, openDrawerCallback);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            onDrawerOpened(true);
                            return true;
                        }
                    }
            );
        }
    }

    private class NoSaleOpenDrowerCallback extends BaseOpenDrawerCallback {

        @Override
        protected void onDrawerIPnoFound() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(
                    DashboardActivity.this,
                    R.string.open_drawer_error_title,
                    getString(R.string.error_message_drawer_ip_not_found),
                    R.string.btn_ok,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(true, noSaleOpenDrowerCallback);
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onDrawerOpened() {
            WaitDialogFragment.hide(DashboardActivity.this);
        }

        @Override
        protected void onDrawerOpenError(PrinterError error) {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(DashboardActivity.this, R.string.open_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)));
        }
    }

    private class MovementsOpenDrawerCallback extends BaseOpenDrawerCallback {

        @Override
        protected void onDrawerIPnoFound() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(
                    DashboardActivity.this,
                    R.string.open_drawer_error_title,
                    getString(R.string.error_message_drawer_ip_not_found),
                    R.string.btn_ok,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(true, movementsOpenDrawerCallback);
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onDrawerOpened() {
            WaitDialogFragment.hide(DashboardActivity.this);

            showCashDrawerMovementEditDialog();
        }

        @Override
        protected void onDrawerOpenError(PrinterError error) {
            WaitDialogFragment.hide(DashboardActivity.this);

            AlertDialogFragment.showAlert(DashboardActivity.this, R.string.open_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)), R.string.btn_try_again,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            WaitDialogFragment.show(DashboardActivity.this, getString(R.string.wait_message_open_drawer));
                            OpenDrawerCommand.start(DashboardActivity.this, false, movementsOpenDrawerCallback);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    }
            );
        }
    }

    private class MovementWaitForCloseDrawerCallback extends BaseWaitForCloseDrawerCallback {

        @Override
        protected void onDrawerClosed() {
            PutCashFragment.hide(DashboardActivity.this);
            printDropAndPayout(false, false);
        }

        @Override
        protected void onDrawerCloseError(PrinterError error) {
            PutCashFragment.hide(DashboardActivity.this);

            showErrorDialog(R.string.close_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)));
        }

        @Override
        protected void onDrawerTimeoutError() {
            PutCashFragment.hide(DashboardActivity.this);

            showErrorDialog(R.string.close_drawer_error_title, getString(R.string.close_drawer_error_msg_close_id));
        }

        private void showErrorDialog(int title, String message) {
            AlertDialogFragment.showAlertWithSkip(DashboardActivity.this, title, message, R.string.btn_try_again,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            PutCashFragment.show(DashboardActivity.this, true);
                            WaitForCloseDrawerCommand.start(DashboardActivity.this, movementWaitForCloseDrawerCallback);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    }
            );
        }
    }

    private class PrintDropPayoutCallback extends BasePrintCallback {

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printDropAndPayout(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };


        @Override
        protected void onPrintSuccess() {
            WaitDialogFragment.hide(DashboardActivity.this);
        }

        @Override
        protected void onPrintError(PrinterError error) {
            PrintCallbackHelper2.onPrintError(DashboardActivity.this, error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(DashboardActivity.this, retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(DashboardActivity.this, retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(DashboardActivity.this, retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(DashboardActivity.this, retryListener);
        }
    }

    private void printDropAndPayout(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(this, getString(R.string.wait_printing));
        PrintDropPayoutCommand.start(this, null, skipPaperWarning, searchByMac, printDropPayoutCallback);
    }

    private class StartShiftCallback extends StartShiftCommand.BaseStartShiftCallback {

        @Override
        protected void onShiftOpened() {
            WaitDialogFragment.hide(DashboardActivity.this);
        }

        @Override
        protected void onShiftOpenedError() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(DashboardActivity.this, R.string.error_dialog_title, getString(R.string.error_message_shift));
        }

    }

    private class StopShiftCallback extends StopShiftCommand.BaseStopShiftCallback {

        @Override
        protected void onShiftClosed() {
            WaitDialogFragment.hide(DashboardActivity.this);
            showPrintXReportDialog();
        }

        @Override
        protected void onShiftClosedError() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(DashboardActivity.this, R.string.error_dialog_title, getString(R.string.error_message_shift));
        }

    }

    private class ClockInCallback extends ClockInCommand.BaseClockInCallback {

        @Override
        protected void onClockIn(String guid, String fullName, Date time) {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showComplete(DashboardActivity.this, R.string.btn_clock_in,
                    getString(R.string.dashboard_clock_in_msg, fullName, DateUtils.timeOnlyAttendanceFormat(time)));
        }

        @Override
        protected void onClockInError(ClockInCommand.ClockInOutError error) {
            WaitDialogFragment.hide(DashboardActivity.this);
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
            AlertDialogFragment.showAlert(DashboardActivity.this, R.string.error_dialog_title, getString(messageId));
        }
    }

    private class ClockOutCallback extends ClockOutCommand.BaseClockOutCallback {

        @Override
        protected void onClockOut(String guid, String fullName, Date time) {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showComplete(DashboardActivity.this, R.string.btn_clock_out,
                    getString(R.string.dashboard_clock_out_msg, fullName, DateUtils.timeOnlyAttendanceFormat(time)));
        }

        @Override
        protected void onClockOutError(ClockOutCommand.ClockInOutError error) {
            WaitDialogFragment.hide(DashboardActivity.this);
            int messageId = R.string.error_message_timesheet;
            switch (error) {
                case ALREADY_CLOCKED_OUT:
                    messageId = R.string.error_message_already_clocked_out;
                    break;
                case USER_DOES_NOT_EXIST:
                    messageId = R.string.error_message_employee_does_not_exist;
                    break;
                case EMPLOYEE_NOT_ACTIVE:
                    messageId = R.string.error_message_employee_not_active;
                    break;
            }
            AlertDialogFragment.showAlert(DashboardActivity.this, R.string.error_dialog_title, getString(messageId));
        }

    }

    private LoaderCallbacks<Integer> alertCounterLoader = new LoaderCallbacks<Integer>() {

        @Override
        public Loader<Integer> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(ITEMS_URI)
                    .projection("count(" + ItemTable.GUID + ")")
                    .where(ItemTable.ACTIVE_STATUS + " = ?", 1)
                    .where(ItemTable.STOCK_TRACKING + " = ?", 1)
                    .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castToReal(ItemTable.MINIMUM_QTY))
                    .wrap(new Function<Cursor, Integer>() {
                        @Override
                        public Integer apply(Cursor c) {
                            if (c.moveToFirst()) {
                                return c.getInt(0);
                            }
                            return 0;
                        }
                    }).build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Integer> integerLoader, Integer value) {
            DashboardActivity.this.alertCounter = value == null ? 0 : value;
            supportInvalidateOptionsMenu();
        }

        @Override
        public void onLoaderReset(Loader<Integer> integerLoader) {
            //updateAlertCounter(0);
        }
    };

    private LoaderCallbacks<Integer> openedTransactionsLoader = new LoaderCallbacks<Integer>() {

        @Override
        public Loader<Integer> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(OPENED_TRANSACTIONS_URI)
                    .projection("count(" + PaymentTransactionTable.GUID + ")")
                    .where(PaymentTransactionTable.STATUS + " = ?", PaymentStatus.PRE_AUTHORIZED.ordinal())
                    .wrap(new Function<Cursor, Integer>() {
                        @Override
                        public Integer apply(Cursor c) {
                            if (c.moveToFirst()) {
                                return c.getInt(0);
                            }
                            return 0;
                        }
                    }).build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Integer> integerLoader, Integer value) {
            DashboardActivity.this.openedTrnsactionsCount = value == null ? 0 : value;
        }

        @Override
        public void onLoaderReset(Loader<Integer> integerLoader) {

        }
    };

    private LoaderCallbacks<Boolean> timesheetLoader = new LoaderCallbacks<Boolean>() {
        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            final long curDate = new Date().getTime();
            return CursorLoaderBuilder.forUri(TIMESHEET_URI)
                    .projection(EmployeeTimesheetTable.GUID + " IS NOT NULL")
                    .where(EmployeeTimesheetTable.EMPLOYEE_GUID + " = ?", getApp().getOperatorGuid())
                    .where(EmployeeTimesheetTable.CLOCK_IN + " < ?", curDate)
                    .where(EmployeeTimesheetTable.CLOCK_OUT + " IS NULL or " + EmployeeTimesheetTable.CLOCK_OUT + " > ?", curDate)
                    .wrap(new Function<Cursor, Boolean>() {
                        @Override
                        public Boolean apply(Cursor c) {
                            boolean success = false;
                            if (c.moveToFirst())
                                success = c.getInt(0) > 0;
                            return success;
                        }
                    }).build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean isOperatorClockedIn) {
            getApp().setOperatorClockedIn(isOperatorClockedIn);

            if (goToSaleOrder && !isEmployeeTimeshiftLoaderFinished) {
                isEmployeeTimeshiftLoaderFinished = true;
                checkGoToSaleOrder();
            }
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    };

    private LoaderCallbacks<Cursor> sqlCommandLoader = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(SQL_COMMAND_URI).projection("1").build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            OfflineCommandsService.startUpload(DashboardActivity.this);
            OfflineCommandsService.scheduleSyncAction(DashboardActivity.this);

            boolean isEmpty = cursor.getCount() == 0;
            if (!isEmpty) {
                OfflineCommandsService.startDownload(DashboardActivity.this);
            }
            getApp().getShopPref().need2DownloadAfter1stLaunch().put(false);
            getSupportLoaderManager().destroyLoader(loader.getId());

            if (goToSaleOrder && !isSqlCommandLoaderFinished) {
                isSqlCommandLoaderFinished = true;
                checkGoToSaleOrder();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    public class SendLogCallback extends BaseSendLogCallback {

        @Override
        protected void handleOnSuccess() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showComplete(
                    DashboardActivity.this,
                    R.string.dlg_completed_tialog,
                    getString(R.string.success_message_email)
            );
        }

        @Override
        protected void handleOnFailure() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(DashboardActivity.this,
                    R.string.error_dialog_title,
                    getString(R.string.error_message_email)
            );
        }
    }

    public class ExportDatabaseCallback extends BaseExportDatabaseCallback {

        @Override
        protected void handleOnSuccess() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showComplete(
                    DashboardActivity.this,
                    R.string.dlg_completed_tialog,
                    getString(R.string.success_message_export_db)
            );
        }

        @Override
        protected void handleOnFailure() {
            WaitDialogFragment.hide(DashboardActivity.this);
            AlertDialogFragment.showAlert(DashboardActivity.this,
                    R.string.error_dialog_title,
                    getString(R.string.error_message_export_db)
            );
        }
    }

}
