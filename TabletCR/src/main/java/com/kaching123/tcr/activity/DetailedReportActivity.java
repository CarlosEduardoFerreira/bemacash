package com.kaching123.tcr.activity;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EActivity;

/**
 * Created by mboychenko on 24.08.2016.
 */
@EActivity(R.layout.detailed_sales_activity)
public class DetailedReportActivity extends BaseReportReceiptTypeActivity {

    @Override
    protected String getActionBarTitle() {
        int actionBarTitleRes = R.string.detailed_report_activity_label;
        return getResources().getString(actionBarTitleRes);
    }

    public static void start(Context context, Uri attachment) {
        DetailedReportActivity_.intent(context).attachment(attachment).start();
    }
}
