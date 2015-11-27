package com.kaching123.tcr.activity;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by alboyko on 26.11.2015.
 */

@EActivity(R.layout.xreport_activity)
public class ZReportActivity extends XReportActivity {

    @Extra
    protected ReportsActivity.ReportType zReportType;

    @Override
    protected String getActionBarTitle() {
        int actionBarTitleRes = R.string.xreport_activity_label;
        if (ReportsActivity.ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            actionBarTitleRes = R.string.zreport_daily_subtitle;
        } else if (ReportsActivity.ReportType.Z_REPORT_CURRENT_SHIFT == zReportType) {
            actionBarTitleRes = R.string.zreport_current_shift_subtitle;
        }
        return getResources().getString(actionBarTitleRes);
    }

    public static void start(Context context, Uri attachment, ReportsActivity.ReportType zReportType) {
        ZReportActivity_.intent(context).attachment(attachment).zReportType(zReportType).start();
    }
}
