package com.kaching123.tcr.fragment.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.util.KeyboardUtils;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by gdubina on 11/11/13.
 */
@EFragment
public class WaitDialogFragmentWithCallback extends StyledDialogFragment {

    private static final String DIALOG_NAME = "progressDialog";

    @FragmentArg
    protected String msg;

    @ViewById
    protected TextView progressMsg;

    @ViewById
    protected Button cancelButton;

    @ViewById
    protected EditText usbScannerInput;

    private OnDialogDismissListener mCallback;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
        setCancelable(false);
        cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        progressMsg.setText(msg);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.dialog_wait_dialog_with_callback_fragment;
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
    public Button getNegativeButton(){
        return cancelButton;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public static void show(FragmentActivity activity, String msg) {
        DialogUtil.show(activity, DIALOG_NAME, WaitDialogFragmentWithCallback_.builder().msg(msg).build());
    }

//    public static void showWithCancel(FragmentActivity activity, String msg) {
//        DialogUtil.show(activity, DIALOG_NAME, WaitDialogFragment_.builder().msg(msg).build());
//    }

    public static DialogFragment showWithReturn(FragmentActivity activity, String msg) {
        DialogFragment fragment = WaitDialogFragmentWithCallback_.builder().msg(msg).build();
        DialogUtil.show(activity, DIALOG_NAME, fragment);
        return fragment;
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @AfterTextChange
    protected void usbScannerInputAfterTextChanged(Editable s) {
        String newline = System.getProperty("line.separator");
        boolean hasNewline = s.toString().contains(newline);
        Logger.d("OrderItemListFragment usbScannerInputAfterTextChanged hasNewline: " + s.toString());
        if (hasNewline) {
            final String result = s.toString().replace("\n", "").replace("\r", "");
            mCallback.onDialogDismissed(result);
            s.clear();
            dismiss();
        }
    }

    // Container Activity must implement this interface
    public interface OnDialogDismissListener {
        public void onDialogDismissed(String barcode);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnDialogDismissListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
