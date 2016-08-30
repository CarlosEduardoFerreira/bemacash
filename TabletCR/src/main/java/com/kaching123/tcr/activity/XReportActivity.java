package com.kaching123.tcr.activity;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;

import org.androidannotations.annotations.EActivity;

/**
 * Created by vkompaniets on 23.01.14.
 */
@EActivity(R.layout.xreport_activity)
public class XReportActivity extends BaseReportReceiptTypeActivity {

    protected String getActionBarTitle() {
        int actionBarTitleRes = R.string.xreport_activity_label;
        if (ReportType.X_REPORT_DAILY_SALES == reportType) {
            actionBarTitleRes = R.string.xreport_daily_subtitle;
        } else if (ReportType.X_REPORT_CURRENT_SHIFT == reportType) {
            actionBarTitleRes = R.string.xreport_current_shift_subtitle;
        }
        return getResources().getString(actionBarTitleRes);
    }

    public static void start(Context context, Uri attachment, ReportType reportType) {
        XReportActivity_.intent(context).attachment(attachment).reportType(reportType).start();
    }
}
