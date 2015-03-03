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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.print.ParseHtmlCommand;
import com.kaching123.tcr.commands.print.ParseHtmlCommand.BaseParseHtmlCallback;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.commands.print.pos.PrintWebReceiptCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.util.KeyboardUtils;

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
@OptionsMenu(R.menu.activation_activity)
public class ActivationActivity extends SuperBaseActivity {

    private static FragmentActivity context;
    private PrintWebReceiptCallback printWebReceiptCallback = new PrintWebReceiptCallback();
    private static String currentURL;
    private Menu activationMenu = null;
    private static String[] receiptBuffer;
    private boolean isUrl = true;
    private boolean bSkipPaperWarning = true, bSearchByMac = true;

    {
        receiptBuffer = null;
    }

    static {
        context = null;
        currentURL = null;
    }


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
        try {
            initPrivateKeyAndX509Certificate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.requestFocusFromTouch();
        webView.setWebViewClient(new BasicWebViewClientEx());
        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        isUrl = true;
        String htmlData = null;
        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if ("text/plain".equals(type)) {

                htmlData = handlePrint(intent);
            }

        }
        if (isUrl) {
            webView.loadUrl(url);
        } else if (htmlData != null) {
            webView.loadData(htmlData, "text/html", null);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    @OptionsItem
    protected void actionCompleteSelected() {
        KeyboardUtils.hideKeyboard(this);
        finish();
    }

    @OptionsItem
    protected void printReceiptSelected() {

        if (receiptBuffer == null)
            return;

        WaitDialogFragment.show(ActivationActivity.this, getString(R.string.wait_printing));
        PrintWebReceiptCommand.start(ActivationActivity.this, receiptBuffer, this.bSkipPaperWarning, this.bSearchByMac, printWebReceiptCallback);

    }

    public String[] prepareReceiptLineFeed(String plainText) {

        plainText = plainText.replace("\n", "\n\r").replaceAll("\t", "   ");
        String[] words = plainText.split("\\n+");
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("\n");
        for (String tmp : words) {
            if (tmp == "\r")
                lines.add("\n");
            else
                lines.add(tmp.replaceAll("\\s+$", ""));
        }
        return lines.toArray(new String[lines.size()]);

    }

    private String handlePrint(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            showProgressbar(true);
            if (Patterns.WEB_URL.matcher(sharedText.toLowerCase()).matches()) {
                url = sharedText.toLowerCase();
                ParseHtmlCommand.start(ActivationActivity.this, url, null, null, new ParseHtmCommandCallbackShare());

            } else {
                isUrl = false;
                String htmlData = "<html><body>" + sharedText.replace("\n", " <br>").replace("\t", "&nbsp;&nbsp;&nbsp;") + "</body></html>";
                handlePrintText(intent, sharedText);
                return htmlData;
            }
        }
        return null;
    }

    private void handlePrintText(Intent intent, String text) {

        if (text != null) {

            receiptBuffer = prepareReceiptLineFeed(text);
            WaitDialogFragment.show(ActivationActivity.this, getString(R.string.wait_printing));
            PrintWebReceiptCommand.start(ActivationActivity.this, receiptBuffer, bSkipPaperWarning, bSearchByMac, printWebReceiptCallback);


        }
    }

    private void showProgressbar(boolean show) {
        setProgressBarIndeterminateVisibility(show);
    }

    public static void start(Context context, String url) {
        context = context;
        ActivationActivity_.intent(context).url(url).start();
    }

    public class BasicWebViewClientEx extends WebViewClient {


        private X509Certificate[] certificatesChain;
        private PrivateKey clientCertPrivateKey;

        public BasicWebViewClientEx() {
            certificatesChain = mX509Certificates;
            clientCertPrivateKey = mPrivateKey;


        }

        public void onReceivedClientCertRequest(WebView view, ClientCertRequestHandler handler, String host_and_port) {

            System.out.println("onReceivedClientCertRequest");
            if ((null != clientCertPrivateKey) && ((null != certificatesChain) && (certificatesChain.length != 0))) {
                handler.proceed(this.clientCertPrivateKey, this.certificatesChain);
            } else {
                handler.cancel();
            }
        }


        @Override
        public void onReceivedSslError(final WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }

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
            if (isUrl)
                ParseHtmlCommand.start(ActivationActivity.this, url, null, null, new ParseHtmCommandCallback());
            showProgressbar(false);
        }
    }

    private void initPrivateKeyAndX509Certificate()
            throws Exception {
        KeyStore keyStore;
        keyStore = KeyStore.getInstance(getString(R.string.KEY_STORE_TYPE), getString(R.string.KEY_STORE_PROVIDER));
        keyStore.load(getResources().openRawResource(R.raw.client), getString(R.string.CERTFILE_PASSWORD).toCharArray());
        Enumeration<?> localEnumeration;
        localEnumeration = keyStore.aliases();
        while (localEnumeration.hasMoreElements()) {
            String str3 = (String) localEnumeration.nextElement();
            mPrivateKey = (PrivateKey) keyStore.getKey(str3, getString(R.string.CERTFILE_PASSWORD).toCharArray());
            if (mPrivateKey == null) {
                continue;
            } else {
                Certificate[] arrayOfCertificate = keyStore
                        .getCertificateChain(str3);
                mX509Certificates = new X509Certificate[arrayOfCertificate.length];
                for (int j = 0; j < mX509Certificates.length; j++) {
                    mX509Certificates[j] = ((X509Certificate) arrayOfCertificate[j]);
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        activationMenu = menu;
        return true;
    }

    private class ParseHtmCommandCallback extends BaseParseHtmlCallback {
        @Override
        protected void onHtmlParsed(String[] printLines) {

            receiptBuffer = printLines;
            if (isUrl)
                enablePrint(true);

        }

        @Override
        protected void onHtmlParseFailure() {
            receiptBuffer = null;

            if (isUrl)
                enablePrint(false);

        }
    }

    private class ParseHtmCommandCallbackShare extends BaseParseHtmlCallback {
        @Override
        protected void onHtmlParsed(String[] printLines) {


            showProgressbar(false);
            WaitDialogFragment.show(ActivationActivity.this, getString(R.string.wait_printing));
            PrintWebReceiptCommand.start(ActivationActivity.this, printLines, bSkipPaperWarning, bSearchByMac, printWebReceiptCallback);


        }

        @Override
        protected void onHtmlParseFailure() {
            receiptBuffer = null;
            showProgressbar(false);

        }
    }

    private void enablePrint(boolean enable) {
        if (activationMenu != null) {
            MenuItem item = activationMenu.getItem(0);
            if (item != null) {
                item.setEnabled(enable);
            }
        }
    }

    private class PrintWebReceiptCallback extends BasePrintCommand.BasePrintCallback {

        private PrintCallbackHelper2.IPrintCallback retryListener = new PrintCallbackHelper2.IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                bSearchByMac = searchByMac;
                bSkipPaperWarning = ignorePaperEnd;
                printReceiptSelected();
            }

            @Override
            public void onCancel() {
                onPrintSuccess();

            }
        };


        @Override
        protected void onPrintSuccess() {

            WaitDialogFragment.hide(ActivationActivity.this);

        }

        @Override
        protected void onPrintError(PrinterCommand.PrinterError error) {
            PrintCallbackHelper2.onPrintError(ActivationActivity.this, error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(ActivationActivity.this, retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(ActivationActivity.this, retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(ActivationActivity.this, retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(ActivationActivity.this, retryListener);
        }
    }
}
