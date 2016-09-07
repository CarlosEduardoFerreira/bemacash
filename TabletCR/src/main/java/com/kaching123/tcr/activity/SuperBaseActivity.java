package com.kaching123.tcr.activity;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.AutoUpdateService;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.ApkDownloadCommand;
import com.kaching123.tcr.commands.payment.ClosePreauthBatchCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;
import com.kaching123.tcr.fragment.tendering.pax.PAXBatchOutFragmentDialog;
import com.kaching123.tcr.fragment.user.LoginFragment;
import com.kaching123.tcr.fragment.user.LoginFragment.Mode;
import com.kaching123.tcr.fragment.user.LoginOuterFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.SerialPortScannerService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentType;

/**
 * Created by gdubina on 04.12.13.
 */
@EActivity
@Fullscreen
public class SuperBaseActivity extends SerialPortScannerBaseActivity {

    @App
    protected TcrApplication app;

    private TempLoginActionProvider tempLoginActionProvider;

    private boolean isDestroyed;

    public static final String ACTION_APK_DOWNLOAD_PROGRESS = "ACTION_APK_DOWNLOAD_PROGRESS";
    public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";

    public TcrApplication getApp() {
        return app;
    }

    private Ringtone alarmRingtone;

    protected Set<Permission> getPermissions() {
        return null;
    }

    private static final IntentFilter intentFilter = new IntentFilter();

    private static final Uri OPENED_TRANSACTIONS_URI = ShopProvider.getContentUri(ShopStore.PaymentTransactionTable.URI_CONTENT);

    public int openedTrnsactionsCount;

    static {
        intentFilter.addAction(SerialPortScannerService.ACTION_SERIAL_PORT_SCANNER);
        intentFilter.addAction(AutoUpdateService.ACTION_APK_UPDATE);
    }

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(SerialPortScannerService.ACTION_SERIAL_PORT_SCANNER)) {
                Logger.d("SuperBaseActivity onReceive:" + intent.getStringExtra(SerialPortScannerService.EXTRA_BARCODE));
                barcodeReceivedFromSerialPort(intent.getStringExtra(SerialPortScannerService.EXTRA_BARCODE));
            } else if (intent.getAction().equals(AutoUpdateService.ACTION_APK_UPDATE)) {
                SyncWaitDialogFragment.hide(SuperBaseActivity.this);
                final String updateUrl = getApp().getUpdateURL();
                boolean approve = getApp().getUpdateApprove();
                final int targetBuildNumber = intent.getIntExtra(AutoUpdateService.ARG_BUILD_NUMBER, 0);
                AlertDialogFragment.hide(SuperBaseActivity.this);
                if (isUpdatePermitted() && approve) {
                    AlertDialogFragment.show(SuperBaseActivity.this, AlertDialogFragment.DialogType.ALERT3,
                            true,
                            R.string.dlg_process_software_update_title,
                            String.format(getString(R.string.apk_update_dialog_title), targetBuildNumber),
                            R.string.btn_yes,
                            R.string.btn_no,
                            R.string.btn_release,
                            new StyledDialogFragment.OnDialogClickListener() {

                                @Override
                                public boolean onClick() {
                                    SyncWaitDialogFragment.show(SuperBaseActivity.this, getString(R.string.str_start_download));
                                    ApkDownloadCommand.start(SuperBaseActivity.this, updateUrl, targetBuildNumber, new ApkDownloadInterface());
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
                                    ReleaseNoteActivity.start(SuperBaseActivity.this, getString(R.string.release_note_link));
                                    return true;
                                }
                            },
                            new StyledDialogFragment.OnDialogClickListener() {
                                @Override
                                public boolean onClick() {
                                    ReleaseNoteActivity.start(SuperBaseActivity.this, getString(R.string.release_note_link));
                                    return true;
                                }
                            }


                    );

                }

            }
        }

    };

    public LoaderManager.LoaderCallbacks<List<PaymentTransactionModel>> openedTransactionsLoader = new LoaderManager.LoaderCallbacks<List<PaymentTransactionModel>>() {

        @Override
        public Loader<List<PaymentTransactionModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(OPENED_TRANSACTIONS_URI)
//                    .projection("count(" + PaymentTransactionTable.GUID + ")")
                    .where(ShopStore.PaymentTransactionTable.STATUS + " = ?", PaymentTransactionModel.PaymentStatus.PRE_AUTHORIZED.ordinal())
                    .wrap(new Function<Cursor, List<PaymentTransactionModel>>() {
                        @Override
                        public List<PaymentTransactionModel> apply(Cursor c) {
                            List<PaymentTransactionModel> list = new ArrayList<PaymentTransactionModel>();
                            while (c.moveToNext()) {
                                PaymentTransactionModel model = new PaymentTransactionModel(
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.GUID)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.PARENT_GUID)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.ORDER_GUID)),
                                        _decimal(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.AMOUNT), BigDecimal.ZERO),
                                        _paymentType(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.TYPE)),
                                        _paymentStatus(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.STATUS)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.OPERATOR_GUID)),
                                        _paymentGateway(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.GATEWAY)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.GATEWAY_PAYMENT_ID)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.GATEWAY_PREAUTH_PAYMENT_ID)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.DECLINE_REASON)),
                                        new Date(c.getLong(c.getColumnIndex(ShopStore.PaymentTransactionTable.CREATE_TIME))),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.SHIFT_GUID)),
                                        c.getString(c.getColumnIndex(ShopStore.PaymentTransactionTable.CARD_NAME)),
                                        _decimal(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.CHANGE_AMOUNT), BigDecimal.ZERO),
                                        _bool(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.IS_PREAUTH)),
                                        _decimal(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.CASH_BACK), BigDecimal.ZERO),
                                        _decimal(c, c.getColumnIndex(ShopStore.PaymentTransactionTable.BALANCE), BigDecimal.ZERO)
                                );

                                list.add(model);
                            }
                            return list;
                        }
                    }).build(SuperBaseActivity.this);
//                    .wrap(new Function<Cursor, Integer>() {
//                        @Override
//                        public Integer apply(Cursor c) {
//                            if (c.moveToFirst()) {
//                                return c.getInt(0);
//                            }
//                            return 0;
//                        }
//                    }).build(DashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<PaymentTransactionModel>> loader, List<PaymentTransactionModel> data) {
            SuperBaseActivity.this.openedTrnsactionsCount = data == null ? 0 : data.size();
            SuperBaseActivity.this.openedTrnsactions = (ArrayList<PaymentTransactionModel>) data;
        }

        @Override
        public void onLoaderReset(Loader<List<PaymentTransactionModel>> loader) {

        }

    };

    private boolean isUpdatePermitted() {
        return (getApp().getOperatorPermissions().contains(Permission.SOFTWARE_UPDATE));
    }

    public class ApkDownloadInterface extends ApkDownloadCommand.BaseApkDownloadCallback {

        @Override
        protected void onhandleSuccess(String apkFileAddress, int buildNumber) {
            getApp().setUpdateFilePath(apkFileAddress);
            SyncWaitDialogFragment.hide(SuperBaseActivity.this);
            install_apk(buildNumber);
        }

        @Override
        protected void onhandleFailure() {
            Toast.makeText(SuperBaseActivity.this, getString(R.string.str_apk_download_fail), Toast.LENGTH_LONG).show();
            SyncWaitDialogFragment.hide(SuperBaseActivity.this);
        }

        @Override
        protected void onhandleProgress(int progress) {
            Intent intent = new Intent(ACTION_APK_DOWNLOAD_PROGRESS);
            intent.putExtra(EXTRA_PROGRESS, progress);
            LocalBroadcastManager.getInstance(SuperBaseActivity.this).sendBroadcast(intent);
        }
    }

    private static String packageName;
    private static int NOTIFICATION_ID = 0xDEADBEEF;
    private static int versionCode = 0;        // as low as it gets
    private static int appIcon = android.R.drawable.ic_popup_reminder;
    private static String appName;
    private static int NOTIFICATION_FLAGS = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;
    private final static String ANDROID_PACKAGE = "application/vnd.android.package-archive";

    private final String STATUSBAR = "statusbar";
    private final String STATUS_BAR_MANAGER = "android.app.StatusBarManager";
    private final String EXPAND_NOTIFICATIONS_PANEL = "expandNotificationsPanel";
    private final String EXPAND = "expand";

    private final String STR_UPDATE = "update";
    private final String STR_UPDATE_AVAILABLE = " update available";
    private final String STR_INSTALL_BUILD = "Select to install build: ";

    protected void install_apk(double targetBuildNumber) {

        String update_file = getApp().getUpdateFilePath();
        File apkfile = new File(update_file);
        if (!apkfile.exists()) {
            Toast.makeText(this, getString(R.string.str_apk_download_fail), Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        stopService(new Intent(this, AutoUpdateService.class));
        startActivity(i);
//        String ns = Context.NOTIFICATION_SERVICE;
//        NotificationManager nm = (NotificationManager) getSystemService(ns);
//        packageName = getApplicationContext().getPackageName();
//        NOTIFICATION_ID = crc32(packageName);
//        ApplicationInfo appinfo = getApplicationContext().getApplicationInfo();
//        if (appinfo.icon != 0) {
//            appIcon = appinfo.icon;
//        } else {
//            Logger.w("unable to find application icon");
//        }
//        if (appinfo.labelRes != 0) {
//            appName = getApplicationContext().getString(appinfo.labelRes);
//        } else {
//            Logger.w("unable to find application label");
//        }
//        String update_file = ((TcrApplication) getApplicationContext()).getUpdateFilePath();
//        if (update_file.length() > 0) {
//
//            // raise notification
//            Notification notification = new Notification(
//                    appIcon, appName + STR_UPDATE, System.currentTimeMillis());
//            notification.flags |= NOTIFICATION_FLAGS;
//
//            CharSequence contentTitle = appName + STR_UPDATE_AVAILABLE;
//            CharSequence contentText = STR_INSTALL_BUILD + targetBuildNumber;
//            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
//            notificationIntent.setDataAndType(
//                    Uri.parse("file://" + update_file),
//                    ANDROID_PACKAGE);
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//            notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
//
//
//            nm.notify(NOTIFICATION_ID, notification);
//        } else {
//            nm.cancel(NOTIFICATION_ID);
//        }
    }

    protected void startCheckUpdateService(boolean force) {
        Logger.d("SuperBaseActivity startCheckUpdateService: " + getUpdateCheckTimer());
        Intent intent = new Intent(SuperBaseActivity.this, AutoUpdateService.class);
        intent.putExtra(AutoUpdateService.ARG_TIMER, getUpdateCheckTimer());
        intent.putExtra(AutoUpdateService.ARG_MANUAL_CHECK, force);
        startService(intent);
    }

    protected long getUpdateCheckTimer() {
        return getApp().getShopInfo().updateCheckTimer;
    }

    private static int crc32(String str) {
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return (int) checksum.getValue();
    }

    public void errorAlarm() {
        if (alarmRingtone == null || alarmRingtone.isPlaying())
            return;
        alarmRingtone.play();
    }

    private Permission[] getPermissionsArray() {
        Set<Permission> permissions = getPermissions();
        if (permissions == null)
            return new Permission[0];
        return permissions.toArray(new Permission[permissions.size()]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        alarmRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

        isDestroyed = false;
        tempLoginActionProvider = new TempLoginActionProvider(this);
        //ViewServer.get(this).addWindow(this);
        Intent intent = new Intent(SuperBaseActivity.this, SerialPortScannerService.class);

        if (shouldSerialPortScanner()) {
            startService(intent);
        } else
            stopService(intent);
    }

    private boolean shouldSerialPortScanner() {
        return (!TextUtils.isEmpty(getApp().getShopPref().scannerAddress().get()) && (getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.SEARIL_PORT_SCANNER_ADDRESS)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        //ViewServer.get(this).removeWindow(this);
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void onResume() {
        super.onResume();
        //ViewServer.get(this).setFocusedWindow(this);
        progressReceiver.register(SuperBaseActivity.this);
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressReceiver.unregister(SuperBaseActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
        invalidateOptionsMenu();
    }

    public ArrayList<PaymentTransactionModel> openedTrnsactions;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem lockItem = menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, getResources().getInteger(R.integer.menu_order_last), R.string.action_lock_label);
        lockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LoginOuterFragment.show(SuperBaseActivity.this, Mode.UNLOCK);
                stopService(new Intent(SuperBaseActivity.this, AutoUpdateService.class));
                return true;
            }
        });

        MenuItem batchOutItem = menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, getResources().getInteger(R.integer.menu_order_default), R.string.action_batchout_label);
        batchOutItem.setVisible(isInSettingPage());

        batchOutItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (openedTrnsactionsCount > 0) {
                    AlertDialogFragment.show(SuperBaseActivity.this, AlertDialogFragment.DialogType.ALERT2, R.string.batch_close_dialog_title, getString(R.string.dashboard_activity_opened_transactions_warning_message), R.string.btn_yes, new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {

                            AlertDialogFragment.show(SuperBaseActivity.this, AlertDialogFragment.DialogType.ALERT2, R.string.batch_close_dialog_title, getString(R.string.batch_close_dialog_msg), R.string.btn_confirm, new StyledDialogFragment.OnDialogClickListener() {
                                @Override
                                public boolean onClick() {
                                    ClosePreauthBatchCommand.start(SuperBaseActivity.this, openedTrnsactions, getApp().getOperatorGuid(), new ClosePreauthBatchCommand.ClosePreauthCommandCallback() {

                                        @Override
                                        public void onCloseSuccess() {
                                            PAXBatchOutFragmentDialog.show(SuperBaseActivity.this, new PAXBatchOutFragmentDialog.IPaxBatchOutListener() {
                                                @Override
                                                public void onComplete(String msg) {
                                                    hide();
                                                }

                                                @Override
                                                public void onCancel() {
                                                    hide();
                                                }

                                                @Override
                                                public void onRetry() {

                                                }

                                                private void hide() {
                                                    PAXBatchOutFragmentDialog.hide(SuperBaseActivity.this);
                                                }
                                            }, PaxModel.get());

                                        }

                                        @Override
                                        public void onCloseFailure() {
//                                WaitDialogFragment.hide(SuperBaseActivity.this);
                                        }
                                    });

                                    return true;
                                }
                            }, new StyledDialogFragment.OnDialogClickListener() {
                                @Override
                                public boolean onClick() {

                                    return true;
                                }
                            }, null);

                            return true;
                        }
                    }, new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
//                        WaitDialogFragment.hide(SuperBaseActivity.this);
                            return true;
                        }
                    }, null);
                } else {
//                    AlertDialogFragment.showAlert(SuperBaseActivity.this, R.string.batch_close_title, getString(R.string.batch_close_msg));
                    PAXBatchOutFragmentDialog.show(SuperBaseActivity.this, new PAXBatchOutFragmentDialog.IPaxBatchOutListener() {
                        @Override
                        public void onComplete(String msg) {
                            hide();
                        }

                        @Override
                        public void onCancel() {
                            hide();
                        }

                        @Override
                        public void onRetry() {

                        }

                        private void hide() {
                            PAXBatchOutFragmentDialog.hide(SuperBaseActivity.this);
                        }
                    }, PaxModel.get());
                }

                return true;
            }
        });

        if (getApp().hasPrevOperator()) {
            MenuItem tempLoginItem = menu.add(Menu.NONE, Menu.NONE, getResources().getInteger(R.integer.menu_order_first), null);
            tempLoginItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            tempLoginItem.setActionProvider(tempLoginActionProvider);
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected boolean isInSettingPage() {
        return false;
    }

    private void checkPermissions() {
        if (!validatePermissions()) {
            PermissionFragment.showRedirecting(this, getOnTempLoginCompleteListener(), getPermissionsArray());
        }
    }

    protected LoginFragment.OnLoginCompleteListener getOnTempLoginCompleteListener() {
        return new LoginFragment.OnLoginCompleteListener() {
            @Override
            public void onLoginComplete() {
                onTempLogin();
                invalidateOptionsMenu();
            }

            @Override
            public boolean onLoginComplete(String lastUncompletedSaleOrderGuid) {
                return false;
            }
        };
    }

    protected void onTempLogin() {
    }

    protected void onTempLogout() {

    }

    private boolean validatePermissions() {
        Set<Permission> permissions = getPermissions();
        if (permissions == null || permissions.isEmpty())
            return true;

        Set<Permission> operatorPermissions = getApp().getOperatorPermissions();
        return operatorPermissions != null && operatorPermissions.containsAll(permissions);
    }

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        errorAlarm();
    }

    private class TempLoginActionProvider extends ActionProvider {

        private Context context;

        public TempLoginActionProvider(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public View onCreateActionView() {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.temp_login_item, null);
            TextView nameLabel = (TextView) view.findViewById(R.id.name_label);
            nameLabel.setText(getApp().getOperator().fullName());
            view.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getApp().hasPrevOperator()) {
                        getApp().restorePrevOperator();
                        onTempLogout();
                        invalidateOptionsMenu();
                        checkPermissions();
                        return;
                    }
                }
            });
            return view;

        }
    }

    public static class BaseTempLoginListener implements LoginFragment.OnLoginCompleteListener {

        private WeakReference<Activity> activityReference;

        public BaseTempLoginListener(Activity activity) {
            activityReference = new WeakReference<Activity>(activity);
        }

        protected Activity getActivity() {
            return activityReference.get();
        }

        @Override
        public void onLoginComplete() {
            Activity activity = getActivity();
            if (activity != null)
                activity.invalidateOptionsMenu();
        }

        @Override
        public boolean onLoginComplete(String lastUncompletedSaleOrderGuid) {
            return false;
        }
    }

    protected SuperBaseActivity self() {
        return this;
    }

}
