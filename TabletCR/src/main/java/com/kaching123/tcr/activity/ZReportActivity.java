package com.kaching123.tcr.activity;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;

import org.androidannotations.annotations.EActivity;


/**
 * Created by alboyko on 26.11.2015.
 */

@EActivity(R.layout.xreport_activity)
public class ZReportActivity extends BaseReportReceiptTypeActivity {

    @Override
    protected String getActionBarTitle() {
        int actionBarTitleRes = R.string.xreport_activity_label;
        if (ReportsActivity.ReportType.Z_REPORT_DAILY_SALES == reportType) {
            actionBarTitleRes = R.string.zreport_daily_subtitle;
        } else if (ReportsActivity.ReportType.Z_REPORT_CURRENT_SHIFT == reportType) {
            actionBarTitleRes = R.string.zreport_current_shift_subtitle;
        }
        return getResources().getString(actionBarTitleRes);
    }

    public static void start(Context context, Uri attachment, ReportType reportType) {
        ZReportActivity_.intent(context).attachment(attachment).reportType(reportType).start();
    }
}
