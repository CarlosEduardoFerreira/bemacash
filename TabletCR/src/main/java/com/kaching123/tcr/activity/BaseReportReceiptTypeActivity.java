package com.kaching123.tcr.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by mboychenko on 24.08.2016.
 */
@EActivity
public abstract class BaseReportReceiptTypeActivity extends SuperBaseActivity {
    @Extra
    protected Uri attachment;

    @Extra
    protected ReportsActivity.ReportType reportType;

    @ViewById
    protected ViewGroup frame;

    @ViewById
    protected WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(getActionBarTitle());
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

    protected abstract String getActionBarTitle();
}
