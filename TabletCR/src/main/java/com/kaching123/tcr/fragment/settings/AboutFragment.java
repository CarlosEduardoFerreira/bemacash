package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SettingsActivity;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AskPermissionDialog;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.ApplicationVersion;
import com.kaching123.tcr.model.RegisterStatus;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopOpenHelper;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.DateUtils;
import com.kaching123.tcr.util.ReceiverWrapper;
import com.kaching123.tcr.util.ValueUtil;

import com.kaching123.tcr.service.broadcast.BroadcastInfo;
import com.kaching123.tcr.service.LocalSyncHelper;

import static com.kaching123.tcr.service.OfflineCommandsService.EXTRA_IS_MANUAL;
import static com.kaching123.tcr.service.SyncCommand.ACTION_SYNC_COMPLETED;
import static com.kaching123.tcr.service.SyncCommand.EXTRA_SUCCESS;
import static com.kaching123.tcr.service.SyncCommand.EXTRA_SYNC_LOCKED;
import static com.kaching123.tcr.util.CursorUtil._selectionArgs;

/**
 * Created by pkabakov on 21/05/14.
 */
@EFragment(R.layout.settings_about_fragment)
@OptionsMenu(R.menu.about_fragment)
public class AboutFragment extends SuperBaseFragment {

    private static final Uri URI_REGISTER = ShopProvider.contentUri(ShopStore.RegisterTable.URI_CONTENT);

    private boolean isManualSync;

    String TAG = "BemaCarl12";

    @ViewById
    protected TextView dataValue;

    private MenuItem actionLocalSyncUpload;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ApplicationVersion version = ValueUtil.getApplicationVersion(getActivity());
        dataValue.setText(getString(R.string.about_version_value, version.name, version.code));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(false);

        actionLocalSyncUpload = menu.findItem(R.id.action_local_sync_upload);
        updateTitleLocalSync();
    }

    private void updateTitleLocalSync(){
        if (getApp().getShopPref().enabledLocalSync().get()){
            actionLocalSyncUpload.setTitle(R.string.disable_local_sync);

        } else {
            actionLocalSyncUpload.setTitle(R.string.enable_local_sync);
        }
    }

    @OptionsItem
    protected void actionAddSelected() {
        AlertDialogFragment.showConfirmation(getActivity(),
                R.string.gc_confirmation_dlg_title,
                getString(R.string.gc_confirmation_dlg_msg),
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        /*
                        SyncService.ignoreCommandObserver = true;

                        GCProcessor.incognito(getActivity(), new GCCommand.GCCallback() {
                            @Override
                            protected void handleDataOk(String message) {
                                onDataOk(message);
                            }

                            @Override
                            protected void handleDataMissing(String message) {
                                onDataMissing(message);
                            }
                        });
                        /**/
                        return true;
                    }
                }
        );
    }

    @OptionsItem
    protected void actionUploadBackup() {
        isManualSync = true;
        SyncWaitDialogFragment.show(getActivity(), getString(R.string.pref_sync_wait));
        //SyncService.startUploadSqlHost(getActivity());
    }

    @OptionsItem
    protected void actionDeleteSelected() {
        AlertDialogFragment.showConfirmation(getActivity(),
                R.string.gc_confirmation_dlg_title_seq,
                getString(R.string.gc_confirmation_dlg_msg_seq),
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        //PrintSeqUpCommand.start(getActivity());
                        return true;
                    }
                }
        );
    }

    @OptionsItem
    protected void actionLocalSyncUploadSelected(){
        if (getApp().getShopPref().enabledLocalSync().get()){
            AskPermissionDialog.show(getActivity(), new AskPermissionDialog.OnCallback() {
                @Override
                public void result(boolean success) {
                    if (success) {
                        getApp().getShopPref().enabledLocalSync().put(false);
                        updateTitleLocalSync();
                    }
                }
            });
        }
        else {
            getApp().getShopPref().enabledLocalSync().put(true);
            updateTitleLocalSync();
        }
    }

    @OptionsItem
    protected void actionShowBemacashsSelected(){
        Log.d(TAG,"AboutFragment.actionShowBemacashsSelected ------------------------- Start -------------------------");
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_processing));

        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_status_local_sync, null);
        final TextView tvServerHour = (TextView) view.findViewById(R.id.tv_server_hour);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        //final SQLiteDatabase db = getApp().getSyncOpenHelper().getReadableDatabase();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<DeviceCount> items = new ArrayList<>();
                    /*
                    Cursor c = db.query(ShopStore.RegisterTable.TABLE_NAME, new String[]{ShopStore.RegisterTable.REGISTER_SERIAL},
                            ShopStore.RegisterTable.REGISTER_SERIAL + " != ? AND (" +
                                    ShopStore.RegisterTable.STATUS + " = ? OR " + ShopStore.RegisterTable.STATUS + " = ?)",
                            new String[]{getApp().getRegisterSerial(),
                                    String.valueOf(RegisterStatus.ACTIVE.ordinal()),
                                    String.valueOf(RegisterStatus.INACTIVE.ordinal())},
                            null, null, null);
                    /**/
                    Cursor c = ProviderAction.query(URI_REGISTER)
                        .projection(ShopStore.RegisterTable.REGISTER_SERIAL)
                        .where(ShopStore.RegisterTable.REGISTER_SERIAL + " != ? AND (" +
                                ShopStore.RegisterTable.STATUS + " = ? OR " + ShopStore.RegisterTable.STATUS + " = ?)",
                                new String[]{getApp().getRegisterSerial(),
                                        String.valueOf(RegisterStatus.ACTIVE.ordinal()),
                                        String.valueOf(RegisterStatus.INACTIVE.ordinal())})
                        .perform(getContext());

                    Log.d(TAG,"AboutFragment.actionShowBemacashsSelected.c.getCount(): " + c.getCount());
                    while (c.moveToNext()){
                        String serial = c.getString(0);
                        Log.d(TAG,"AboutFragment.actionShowBemacashsSelected.serial: " + serial);
                        items.add(new DeviceCount(serial, getApp().getLanDevices().contains(new BroadcastInfo(serial))));
                    }

                Log.d(TAG,"AboutFragment.actionShowBemacashsSelected.items1.size(): " + items.size());
                for (DeviceCount info : items){
                    try{
                        Uri uri = ShopProvider.contentUri(ShopStore.SqlCommandHostCountQuery.URI_CONTENT);
                        String[] selectionArgs = _selectionArgs(info.getSerial());

                        Cursor c1 = getApp().getContentResolver().query(
                                uri,
                            null,
                            null,
                            selectionArgs,
                            null
                        );

                        Log.d(TAG,"AboutFragment.actionShowBemacashsSelected.c1.getCount(): " + c1.getCount());
                        if (c1.moveToFirst()){
                            info.setCount(c1.getInt(0));
                        }
                        c1.close();
                    } catch (Exception e){
                        Logger.e("actionShowBemacashsSelected", e);
                        WaitDialogFragment.hide(getActivity());
                        AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.occurred_an_error));
                        break;
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"AboutFragment.actionShowBemacashsSelected.run");
                        if (WaitDialogFragment.isShowing()) {
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(new DeviceCountAdapter(items));
                            tvServerHour.setText(DateUtils.formatFull(new Date(TcrApplication.get().getCurrentServerTimestamp())));
                            Log.d(TAG,"AboutFragment.actionShowBemacashsSelected.items2.size(): " + items.size());
                            WaitDialogFragment.hide(getActivity());
                            new AlertDialog.Builder(getActivity(), R.style.DialogTheme_WithoutMinWidth)
                                    .setTitle(R.string.local_bemacashs)
                                    .setView(view)
                                    .setPositiveButton(R.string.btn_close, null)
                                    /*
                                    .setNeutralButton(R.string.clear_local_sync_data, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            clearLocalSyncDataConfirm();
                                        }
                                    })
                                    /**/
                                    .show();
                        }
                    }
                });
            }
        }).start();
        Log.d(TAG,"AboutFragment.actionShowBemacashsSelected ------------------------- End   -------------------------");
    }

    @OptionsItem
    protected void actionForceSync(){
        isManualSync = true;
        SyncWaitDialogFragment.show(getActivity(), getString(R.string.pref_sync_wait));
        //SyncService.startOldUploadAndOldDownload(getActivity(), true);
        OfflineCommandsService.startUpload(getContext());
        OfflineCommandsService.startDownload(getContext());
    }

    private void clearLocalSyncDataConfirm(){
        AskPermissionDialog.show(getActivity(), new AskPermissionDialog.OnCallback() {
            @Override
            public void result(boolean success) {
                if (success){
                    new AlertDialog.Builder(getActivity(), R.style.DialogTheme_WithoutMinWidth)
                            .setTitle(R.string.warning_dialog_title)
                            .setMessage(R.string.clear_local_sync_data_confirm)
                            .setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clearLocalSyncData();
                                }
                            })
                            .setNegativeButton(R.string.btn_cancel, null)
                            .show();
                }
            }
        });
    }

    private void clearLocalSyncData(){

        final SQLiteOpenHelper dbHelper = new ShopOpenHelper(getContext());
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        WaitDialogFragment.show(getActivity(), getString(R.string.wait_processing));
        LocalSyncHelper.disableLocalSync();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                try {
                    Logger.w("Deleted all from sql_command_client: " + db.delete(ShopStore.SqlCommandClientTable.TABLE_NAME, null, null));
                    Logger.w("Deleted all from sql_command_host: " + db.delete(ShopStore.SqlCommandHostTable.TABLE_NAME, null, null));

                } catch (Exception e){
                    success = false;
                    Logger.e("clearLocalSyncData", e);
                }

                LocalSyncHelper.enableLocalSync();
                final boolean finalSuccess = success;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WaitDialogFragment.hide(getActivity());

                        if (finalSuccess) {
                            actionShowBemacashsSelected();

                        } else {
                            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.occurred_an_error));
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        progressReceiver.unregister(getActivity());
    }

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(
            new IntentFilter(ACTION_SYNC_COMPLETED)) {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SYNC_COMPLETED.equals(intent.getAction())) {
                if (!intent.getBooleanExtra(EXTRA_IS_MANUAL, false)){
                    return;
                }

                SyncWaitDialogFragment.hide(getActivity());

                if (finalStep) {
                    finalStep = false;
                    onDataOk("Procedure completed!");
                    return;
                }

                boolean success = intent.getBooleanExtra(EXTRA_SUCCESS, false);
                boolean isSyncLockedError = intent.getBooleanExtra(EXTRA_SYNC_LOCKED, false);
                //boolean isCanceled = intent.getBooleanExtra(EXTRA_SYNC_CANCELED, false);

                if (success) {
                    if (isManualSync){
                        isManualSync = false;
                        AlertDialogFragment.showComplete(getActivity(), R.string.sync_success_title, getString(R.string.sync_success_message));

                    } else {
                        /*
                        GCProcessor.incognito(getActivity(), new GCCommand.GCCallback() {
                            @Override
                            protected void handleDataOk(String message) {
                                onDataOk(message);
                            }

                            @Override
                            protected void handleDataMissing(String message) {
                                onDataMissing(message);
                            }
                        });
                        /**/
                    }
                } else if (isSyncLockedError) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.sync_error_title, context.getString(R.string.error_message_sync_locked));

                //}  else if (isCanceled) {
                 //   AlertDialogFragment.showAlert(getActivity(), R.string.sync_error_title, getString(R.string.error_message_sync_interrupted));

                } else {
                    AlertDialogFragment.showAlert(getActivity(), R.string.sync_error_title, context.getString(R.string.sync_error_message));
                }
            }
        }
    };

    protected boolean finalStep;
    protected static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //SyncService.startUploadAndDownload(TcrApplication.get(), true);
            OfflineCommandsService.startUpload(TcrApplication.get());
            OfflineCommandsService.startDownload(TcrApplication.get());
        }
    } ;

    protected void onDataMissing(String message) {
        AlertDialogFragment.showConfirmation(getActivity(),
                R.string.gc_confirmation_dlg_title,
                getString(R.string.gc_confirmation_dlg_msg_push) + " " + message,
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        WaitDialogFragment.show(getActivity(), getString(R.string.wait_processing));
                        /*
                        GCProcessor.recover(getActivity(), new GCRCommand.GCCallback() {
                            @Override
                            protected void handleNegative(String message) {
                                WaitDialogFragment.hide(getActivity());

                                finalStep = true;
                                SyncWaitDialogFragment.show(getActivity(), getString(R.string.pref_sync_wait));
                                handler.sendEmptyMessageDelayed(0, 10000);
                        }

                            @Override
                            protected void handlePositive(String message) {
                                WaitDialogFragment.hide(getActivity());

                                AlertDialogFragment.showNotification(getActivity(),
                                        R.string.gc_confirmation_dlg_title,
                                        message);
                            }
                        });
                        /**/
                        return true;
                    }
                }
        );
    }

    protected void onDataOk(String message) {
        AlertDialogFragment.showNotification(getActivity(),
                R.string.gc_confirmation_dlg_title,
                message);
    }

    public static Fragment instance() {
        return AboutFragment_.builder().build();
    }

    @Override
    public void onDestroyView() {
        if (((SettingsActivity) getActivity()).canDestroy()) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(this);
            fragmentTransaction.commit();
        }
        super.onDestroyView();

    }
    private class DeviceCount {

        private String mSerial;
        private int mCount;
        private boolean mIsOnline;

        public DeviceCount(String mSerial, boolean isOnline) {
            this.mSerial = mSerial;
            this.mIsOnline = isOnline;
        }

        public String getSerial() {
            return mSerial;
        }

        public int getCount() {
            return mCount;
        }

        public void setCount(int count){
            mCount = count;
        }

        @Override
        public boolean equals(Object o) {
            return ((DeviceCount) o).getSerial().equals(mSerial);
        }
    }

    class DeviceCountAdapter extends RecyclerView.Adapter<DeviceCountAdapter.ItemHolder>{

        private List<DeviceCount> mItems;

        public DeviceCountAdapter(List<DeviceCount> items){
            this.mItems = items;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_status_local_sync_item, null));
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            DeviceCount item = mItems.get(position);

            if (item.mIsOnline){
                holder.tvStatus.setText(getString(R.string.local_sync_device_status_online));
                holder.tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.green));

            } else {
                holder.tvStatus.setText(getString(R.string.local_sync_device_status_offline));
                holder.tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
            }

            holder.tvSerial.setText(item.getSerial());
            holder.tvCount.setText(String.valueOf(item.getCount()));

            if (item.getCount() > 3000) {
                holder.tvCount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));

            } else if (item.getCount() > 1000){
                holder.tvCount.setTextColor(ContextCompat.getColor(getActivity(), R.color.orange));

            } else {
                holder.tvCount.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class ItemHolder extends RecyclerView.ViewHolder{

            TextView tvStatus;
            TextView tvSerial;
            TextView tvCount;

            ItemHolder(View itemView) {
                super(itemView);

                tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
                tvSerial = (TextView) itemView.findViewById(R.id.tv_serial);
                tvCount = (TextView) itemView.findViewById(R.id.tv_qty);
            }
        }
    }
}