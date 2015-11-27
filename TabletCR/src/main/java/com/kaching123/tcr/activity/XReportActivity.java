package com.kaching123.tcr.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;

/**
 * Created by vkompaniets on 23.01.14.
 */
@EActivity(R.layout.xreport_activity)
public class XReportActivity extends SuperBaseActivity {

    @Extra
    protected Uri attachment;

    @Extra
    protected ReportType xReportType;

    @ViewById
    protected ViewGroup frame;

    @ViewById
    protected WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(getActionBarTitle());
    }

    protected String getActionBarTitle() {
        int actionBarTitleRes = R.string.xreport_activity_label;
        if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            actionBarTitleRes = R.string.xreport_daily_subtitle;
        } else if (ReportType.X_REPORT_CURRENT_SHIFT == xReportType) {
            actionBarTitleRes = R.string.xreport_current_shift_subtitle;
        }
        return getResources().getString(actionBarTitleRes);
    }

    @Override
    public void onResume() {
        super.onResume();

        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        setWebViewSize(mWindowManager.getDefaultDisplay().getRotation());
    }

    private void setWebViewSize(int rotation) {
        Logger.d("[rotation]" + rotation);
        ViewGroup.LayoutParams params = frame.getLayoutParams();
        int width = getResources().getDimensionPixelOffset(R.dimen.xreport_webview_width);
        int height = 0;
        if (rotation == 0 || rotation == 2) {
            height = getResources().getDimensionPixelOffset(R.dimen.xreport_webview_height_land);
        } else {
            height = getResources().getDimensionPixelOffset(R.dimen.xreport_webview_height_portrait);
        }
        params.width = width;
        params.height = height;
        webView.invalidate();
    }

    @AfterViews
    protected void init() {
        webView.loadUrl(attachment.toString());
    }

    public static void start(Context context, Uri attachment, ReportType xReportType) {
        XReportActivity_.intent(context).attachment(attachment).xReportType(xReportType).start();
    }
}
