package com.kaching123.tcr.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by gdubina on 22/11/13.
 */
@EFragment
public class AlertDialogFragment extends StyledDialogFragment {

    protected static final String DIALOG_NAME = "errorDialogFragment";

    public enum DialogType {
        CONFIRM(3, true), INFO(2, false), ALERT(4, false), COMPLETE(1, false), ALERT2(4, true), INFO2(2, true), CONFIRM_NONE(3, true), ALERT3(4, true), ALERT4(4, false);

        public final int level;
        final boolean hasNegativeBtn;

        DialogType(int level, boolean hasNegativeBtn) {
            this.level = level;
            this.hasNegativeBtn = hasNegativeBtn;
        }

        public static DialogType valueOf(int temp) {
            return temp >= 0 && temp < DialogType.values().length ? DialogType.values()[temp] : CONFIRM_NONE;
        }
    }

    @ViewById
    protected TextView textView;

    @ViewById
    protected ImageView icon;

    @FragmentArg
    protected int titleId;

    @FragmentArg
    protected int positiveButtonTitleId;

    @FragmentArg
    protected int negativeButtonTitleId;

    @FragmentArg
    protected int skipTitleId;

    @FragmentArg
    protected String errorMsg;

    @FragmentArg
    protected DialogType dialogType;

    @FragmentArg
    protected boolean hideImage;

    @FragmentArg
    protected boolean listenMessage;

    private OnDialogClickListener onPositiveListener;
    private OnDialogClickListener onNegativeListener;
    private OnDialogClickListener onSkipListener;
    private OnDialogClickListener onMessageListener;

    @AfterViews
    protected void bind() {
        setCancelable(false);
        textView = (TextView) getView().findViewById(R.id.text_view);

        textView.setGravity(dialogType == DialogType.CONFIRM || dialogType == DialogType.CONFIRM_NONE ? Gravity.LEFT : Gravity.CENTER);
        textView.setText(errorMsg);
//        if(listenMessage)
//            textView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onMessageListener.onClick();
//                }
//            });
        if (dialogType == DialogType.CONFIRM_NONE || hideImage) {
            icon.setVisibility(View.GONE);
        } else {
            icon.setVisibility(View.VISIBLE);
            icon.setImageLevel(dialogType.level);
        }
        textView.setVisibility(TextUtils.isEmpty(errorMsg) ? View.GONE : View.VISIBLE);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.dialog_alert_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return titleId;
    }

    @Override
    protected int getSkipButtonTitle() {
        return skipTitleId == 0 ?  R.string.btn_skip :skipTitleId;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return negativeButtonTitleId == 0 ? R.string.btn_cancel : negativeButtonTitleId;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return positiveButtonTitleId;
    }

    @Override
    protected boolean hasNegativeButton() {
        return dialogType.hasNegativeBtn;
    }

    @Override
    protected boolean hasSkipButton() {
        return dialogType == DialogType.ALERT3 || dialogType == DialogType.ALERT4;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return onPositiveListener;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return this.onNegativeListener;
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return onSkipListener;
    }

    protected OnDialogClickListener getMessageListener() {
        return onMessageListener;
    }

    public void setOnSkipListener(OnDialogClickListener onSkipListener) {
        this.onSkipListener = onSkipListener;
    }

    public AlertDialogFragment setOnMessageListener(OnDialogClickListener onMessageListener) {
        this.onMessageListener = onMessageListener;
        return this;
    }

    public AlertDialogFragment setOnPositiveListener(OnDialogClickListener onPositiveListener) {
        this.onPositiveListener = onPositiveListener;
        return this;
    }

    public AlertDialogFragment setOnNegativeListener(OnDialogClickListener onNegativeListener) {
        this.onNegativeListener = onNegativeListener;
        return this;
    }

    public static void hide(FragmentActivity activity) {

        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static void show(FragmentActivity activity, DialogType type, boolean listenMessage, int titleId, String msg, int positiveTitleId, int negativeTitleId, int skipTitleId, OnDialogClickListener positiveListener, OnDialogClickListener negativeListener, OnDialogClickListener skipListener, OnDialogClickListener messageLisener) {
        DialogUtil.show(activity, DIALOG_NAME, AlertDialogFragment_.builder().listenMessage(listenMessage).titleId(titleId).errorMsg(msg).hideImage(true).negativeButtonTitleId(negativeTitleId).skipTitleId(skipTitleId).positiveButtonTitleId(positiveTitleId).dialogType(type).build()).setOnPositiveListener(positiveListener).setOnMessageListener(messageLisener).setOnNegativeListener(negativeListener).setOnSkipListener(skipListener);
    }

    public static void show(FragmentActivity activity, DialogType type, int titleId, String msg, int positiveTitleId, OnDialogClickListener positiveListener, OnDialogClickListener negativeListener, OnDialogClickListener skipListener) {
        DialogUtil.show(activity, DIALOG_NAME, AlertDialogFragment_.builder().titleId(titleId).errorMsg(msg).positiveButtonTitleId(positiveTitleId).dialogType(type).build()).setOnPositiveListener(positiveListener).setOnNegativeListener(negativeListener).setOnSkipListener(skipListener);
    }

    public static void show(FragmentActivity activity, DialogType type, int titleId, String msg, int positiveTitleId, int negativeTitleId, boolean hideImage,OnDialogClickListener positiveListener, OnDialogClickListener negativeListener, OnDialogClickListener skipListener) {
        DialogUtil.show(activity, DIALOG_NAME, AlertDialogFragment_.builder().titleId(titleId).errorMsg(msg).positiveButtonTitleId(positiveTitleId).negativeButtonTitleId(negativeTitleId).hideImage(hideImage).dialogType(type).build()).setOnPositiveListener(positiveListener).setOnNegativeListener(negativeListener).setOnSkipListener(skipListener);
    }

    public static void show(FragmentActivity activity, DialogType type, int titleId, String msg, int positiveTitleId, OnDialogClickListener positiveListener) {
        show(activity, type, titleId, msg, positiveTitleId, positiveListener, null, null);
    }

    public static void showNotification(FragmentActivity activity, int titleId, String msg){
        showNotification(activity, titleId, msg, null);
    }

    public static void showNotification(FragmentActivity activity, int titleId, String msg, OnDialogClickListener positiveListener) {
        show(activity, DialogType.INFO, titleId, msg, R.string.btn_ok, positiveListener);
    }

    public static void showComplete(FragmentActivity activity, int titleId, String msg) {
        show(activity, DialogType.COMPLETE, titleId, msg, R.string.btn_ok, null);
    }

    public static void showConfirmation(FragmentActivity activity, int titleId, String msg, OnDialogClickListener listener) {
        show(activity, DialogType.CONFIRM_NONE, titleId, msg, R.string.btn_confirm, listener);
    }

    public static void showConfirmationNoImage(FragmentActivity activity, int titleId, String msg, OnDialogClickListener listener) {
        show(activity, DialogType.CONFIRM_NONE, titleId, msg, R.string.btn_confirm, listener);
    }

    public static void showAlert(FragmentActivity activity, int titleId, String msg) {
        show(activity, DialogType.ALERT, titleId, msg, R.string.btn_ok, null);
    }

    public static void showAlert(FragmentActivity activity, int titleId, String msg, OnDialogClickListener cancelListener) {
        show(activity, DialogType.ALERT, titleId, msg, R.string.btn_cancel, cancelListener);
    }

    public static void showAlert(FragmentActivity activity, int titleId, String msg, int btnTitleId, OnDialogClickListener listener) {
        show(activity, DialogType.ALERT2, titleId, msg, btnTitleId, listener);
    }

    public static void showAlert(FragmentActivity activity, int titleId, String msg, int btnTitleId, OnDialogClickListener okListener, OnDialogClickListener cancelListener) {
        show(activity, DialogType.ALERT2, titleId, msg, btnTitleId, okListener, cancelListener, null);
    }

    public static void showAlertWithSkip(FragmentActivity activity, int titleId, String msg, int btnTitleId, OnDialogClickListener okListener, OnDialogClickListener cancelListener, OnDialogClickListener skipListener) {
        show(activity, DialogType.ALERT3, titleId, msg, btnTitleId, okListener, cancelListener, skipListener);
    }

    public static void showAlertWithSkip(FragmentActivity activity, int titleId, String msg, OnDialogClickListener okListener, OnDialogClickListener onSkipListener) {
        show(activity, DialogType.ALERT3, titleId, msg, R.string.btn_try_again, okListener, null, onSkipListener);
    }

    public static void showAlertWithSkip(FragmentActivity activity, int titleId, String msg, int okTitleId, OnDialogClickListener okListener, OnDialogClickListener skipListener) {
        show(activity, DialogType.ALERT4, titleId, msg, okTitleId, okListener, null, skipListener);
    }
}
