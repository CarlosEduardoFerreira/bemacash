package com.kaching123.tcr.fragment.barcode;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by gdubina on 11/11/13.
 */
@EFragment
public class WaitScannerFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "waitScannerDialog";

    @ViewById
    protected EditText scannerFakeInput;

    private OnBarcodeScannedListener onBarcodeScanned;

    @AfterViews
    protected void inti(){
        scannerFakeInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Logger.d("Scanner: onKey - %s", scannerFakeInput.getText());
                return false;
            }
        });

        scannerFakeInput.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Logger.d("Scanner: onEditorAction");
                tryToSearchBarCode();
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
        setCancelable(false);
    }

    private void tryToSearchBarCode() {
        if(onBarcodeScanned != null){
            onBarcodeScanned.onBarcodeScanned(scannerFakeInput.getText().toString());
        }
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.wait_scanner_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.wait_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public void setOnBarcodeScanned(OnBarcodeScannedListener onBarcodeScanned) {
        this.onBarcodeScanned = onBarcodeScanned;
    }

    public static void show(FragmentActivity activity, OnBarcodeScannedListener listener) {
       DialogUtil.show(activity, DIALOG_NAME, WaitScannerFragment_.builder().build()).setOnBarcodeScanned(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static interface OnBarcodeScannedListener {
        void onBarcodeScanned(String barcode);
    }
}
