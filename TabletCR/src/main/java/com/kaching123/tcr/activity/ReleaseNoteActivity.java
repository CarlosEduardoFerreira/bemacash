package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ClientCertRequestHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.print.ParseHtmlCommand;
import com.kaching123.tcr.commands.print.ParseHtmlCommand.BaseParseHtmlCallback;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.commands.print.pos.PrintWebReceiptCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.util.KeyboardUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by vkompaniets on 07.07.2014.
 */
@EActivity(R.layout.activation_activity)
public class ReleaseNoteActivity extends SuperBaseActivity {

    @Extra
    protected String url;

    @ViewById
    protected WebView webView;

    private X509Certificate[] mX509Certificates;
    private PrivateKey mPrivateKey;

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
        webView.getSettings().setBuiltInZoomControls(true);
        webView.requestFocusFromTouch();
        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.loadUrl(url);
        // Get intent, action and MIME type
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
        else
            finish();
    }

    private void showProgressbar(boolean show) {
        setProgressBarIndeterminateVisibility(show);
    }

    public static void start(Context context, String url) {
        context = context;
        ReleaseNoteActivity_.intent(context).url(url).start();
    }

}
