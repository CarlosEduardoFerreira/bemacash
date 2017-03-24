package com.kaching123.tcr.fragment.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import com.kaching123.tcr.AutoUpdateService;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by gdubina on 07/11/13.
 */
@EFragment
@OptionsMenu(R.menu.settings_sync_fragment)
public class SyncSettingsFragment extends PreferenceFragment {
    private static final IntentFilter intentFilter = new IntentFilter();
    protected static final Uri URI_EMPLOYEE_SYNCED = ShopProvider.getNoNotifyContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    private ManualCheckUpdateListener listener;

    static {
        intentFilter.addAction(UploadTask.ACTION_UPLOAD_COMPLETED);
        intentFilter.addAction(SyncCommand.ACTION_SYNC_COMPLETED);
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED);
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED);
        intentFilter.addAction(AutoUpdateService.ACTION_NO_UPDATE);
    }

    @App
    protected TcrApplication app;

    private boolean isUploadSuccess;

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || intent == null)
                return;

            if (UploadTask.ACTION_UPLOAD_COMPLETED.equals(intent.getAction())) {
                isUploadSuccess = intent.getBooleanExtra(UploadTask.EXTRA_SUCCESS, false);
                return;
            }
            if (SyncCommand.ACTION_SYNC_COMPLETED.equals(intent.getAction())) {
                SyncWaitDialogFragment.hide(getActivity());

                boolean success = intent.getBooleanExtra(SyncCommand.EXTRA_SUCCESS, false);
                boolean isSyncLockedError = intent.getBooleanExtra(SyncCommand.EXTRA_SYNC_LOCKED, false);
                if (success && isUploadSuccess) {
                    AlertDialogFragment.showComplete(getActivity(), R.string.sync_success_title, getString(R.string.sync_success_message));

                    if (app.isTipsEnabledWasChanged()) {
                        app.setTipsEnabledWasChanged(false);
                        Toast.makeText(getActivity(), app.isTipsEnabled() ? R.string.warning_message_tips_enabled : R.string.warning_message_tips_disabled, Toast.LENGTH_LONG).show();
                    }
                } else if (isSyncLockedError) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.sync_error_title, getString(R.string.error_message_sync_locked));
                } else {
                    AlertDialogFragment.showAlert(getActivity(), R.string.sync_error_title, getString(R.string.sync_error_message));
                }
                Intent ii = new Intent(getActivity(), AutoUpdateService.class);
                ii.putExtra(AutoUpdateService.ARG_TIMER, getUpdateCheckTimer());
                ii.putExtra(AutoUpdateService.ARG_MANUAL_CHECK, false);
                getActivity().stopService(ii);
                getActivity().startService(ii);
            }
            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED.equals(intent.getAction())) {
                if (intent.getBooleanExtra(UploadTaskV2.EXTRA_SUCCESS, false))
                    updateEmployeeSyncStatus();
                if (intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE) != null && intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE).equalsIgnoreCase("400"))
                    Toast.makeText(getActivity(), R.string.warning_main_upload_fail, Toast.LENGTH_LONG).show();
            }
            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED.equals(intent.getAction())) {

            }
            if (AutoUpdateService.ACTION_NO_UPDATE.equals(intent.getAction())) {
                SyncWaitDialogFragment.hide(getActivity());

                int targetBuildNumber = intent.getIntExtra(AutoUpdateService.ARG_BUILD_NUMBER, 0) == 0 ? getCurrentBuildNumber() : intent.getIntExtra(AutoUpdateService.ARG_BUILD_NUMBER, 0);
                Toast.makeText(getActivity(), String.format(getString(R.string.str_no_app_update), targetBuildNumber), Toast.LENGTH_LONG).show();
            }
        }
    };

    private int getCurrentBuildNumber() {
        try {
            return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateEmployeeSyncStatus() {
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues v = new ContentValues(1);
        v.put(ShopStore.EmployeeTable.IS_SYNC, "1");
        cr.update(URI_EMPLOYEE_SYNCED, v, ShopStore.EmployeeTable.IS_SYNC + " = ?", new String[]{"0"});
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_sync_pref_fragment);
        ListPreference timePref = (ListPreference)findPreference(getString(R.string.pref_sync_time_key));

        if(OfflineCommandsService.localSync){
            timePref.setDefaultValue(R.integer.local_sync_time_def_val);
            timePref.setShouldDisableView(true);
            timePref.setValueIndex(0);
        }else{
            timePref.setDefaultValue(R.integer.sync_time_entries_def_val);
            timePref.setShouldDisableView(false);
            timePref.setValueIndex(3);
        }

        assert timePref != null;

        timePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(OfflineCommandsService.localSync) {
                    return false;
                }else {
                    int mins = Integer.parseInt(newValue.toString());
                    app.getShopPref().syncPeriod().put(mins);
                    OfflineCommandsService.scheduleSyncAction(getActivity());
                }
                return true;
            }
        });

        Preference button = (Preference) findPreference(getString(R.string.pref_update_manual));
        button.setEnabled(isUpdatePermitted());
        if (!isUpdatePermitted())
            getPreferenceScreen().removePreference(button);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                listener.onCheckClick();
                return true;
            }
        });
    }

    protected long getUpdateCheckTimer() {
        return ((TcrApplication) getActivity().getApplicationContext()).getShopInfo().updateCheckTimer;
    }

    protected boolean isUpdatePermitted() {
        return ((TcrApplication) getActivity().getApplicationContext()).getOperatorPermissions().contains(Permission.SOFTWARE_UPDATE);
    }

    @OptionsItem
    protected void actionSyncSelected() {
        SyncWaitDialogFragment.show(getActivity(), getString(R.string.pref_sync_wait));
        isUploadSuccess = false;
        OfflineCommandsService.startUpload(getActivity(), true);
        OfflineCommandsService.startDownload(getActivity(), true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (ManualCheckUpdateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public static Fragment instance() {
        return SyncSettingsFragment_.builder().build();
    }

    public interface ManualCheckUpdateListener {
        void onCheckClick();
    }
}
