package com.kaching123.tcr.commands;

import android.content.Context;
import android.util.Log;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.service.OfflineCommandsService;

/**
 * Created by Carlos on 31/03/2017.
 */

public class BackOfficeSyncCommand {

    Context mContext;

    public BackOfficeSyncCommand() {
        this.mContext = TcrApplication.get().getApplicationContext();
    }

    public void adjustSyncTime(){

        Integer syncTimeDefault     = mContext.getResources().getInteger(R.integer.sync_time_entries_def_val);
        Integer syncTimeActual      = TcrApplication.get().getShopPref().syncPeriod().get();
        Integer syncTimeLocalSync   = mContext.getResources().getInteger(R.integer.local_sync_time_def_val);

        Integer newTime = syncTimeActual.equals(syncTimeLocalSync) ? syncTimeDefault : syncTimeActual ;

        boolean gotIt = new AtomicUpload().hasInternetConnection();
        if(gotIt && OfflineCommandsService.localSync) {
            newTime = mContext.getResources().getInteger(R.integer.local_sync_time_def_val);
        }
        int mins = Integer.parseInt(newTime.toString());
        Log.d("BemaCarl8", "BackOfficeSyncCommand.adjustSyncTime.mins: " + mins);
        TcrApplication.get().getShopPref().syncPeriod().put(mins);
        OfflineCommandsService.scheduleSyncAction(mContext);
    }
}
