package com.kaching123.tcr.fragment.settings;

import android.net.TrafficStats;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseFragment;

/**
 * Created by gdubina on 28/02/14.
 */
@EFragment(R.layout.settings_datausage_stat_fragment)
public class DataUsageStatFragment extends SuperBaseFragment{

    @ViewById
    protected TextView dataValue;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int uid = getApp().getApplicationInfo().uid;
        long bytes = TrafficStats.getUidTxBytes(uid) + TrafficStats.getUidRxBytes(uid);

        long kb = bytes / 1024;
        bytes = bytes % 1024;
        long mb = kb / 1024;
        kb = kb % 1024;

        StringBuilder text = new StringBuilder();
        if(mb > 0){
            text.append(mb).append(" MB").append(" ");
        }
        if(kb > 0){
            text.append(kb).append(" kB").append(" ");
        }
        text.append(bytes).append(" B");
        dataValue.setText(text.toString());
    }

    public static Fragment instance() {
        return DataUsageStatFragment_.builder().build();
    }
}
