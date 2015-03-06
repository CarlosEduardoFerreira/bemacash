package com.kaching123.tcr.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.util.KeyboardUtils;

/**
 * Created by vkompaniets on 07.07.2014.
 */
@EActivity(R.layout.activation_activity)
@OptionsMenu(R.menu.activation_activity)
public class SignupActivity extends SuperBaseActivity {

    @Extra
    protected String url;

    @ViewById
    protected WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @AfterViews
    protected void init() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(client);
        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @OptionsItem
    protected void actionCompleteSelected() {
        KeyboardUtils.hideKeyboard(this);
        finish();
    }

    private void showProgressbar(boolean show) {
        setProgressBarIndeterminateVisibility(show);
    }

    private WebViewClient client = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgressbar(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            showProgressbar(false);
        }

    };

    public static void start(Context context, String url) {
        SignupActivity_.intent(context).url(url).start();
    }
}
