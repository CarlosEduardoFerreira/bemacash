package com.kaching123.tcr.fragment.dialog;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by mayer
 */
@EFragment
public class AlertDialogWithCancelFragment extends AlertDialogFragment {

    protected static final String DIALOG_NAME = "AlertDialogWithCancelDualListener";

    protected OnDialogListener listener;
    protected boolean showNegativeBtn;

    public AlertDialogWithCancelFragment setListener(OnDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public AlertDialogWithCancelFragment setShowNegativeBtn(boolean showNegativeBtn) {
        this.showNegativeBtn = showNegativeBtn;
        return this;
    }

    @AfterViews
    protected void init() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone alarmRingtone = RingtoneManager.getRingtone(getApp(), notification);
        if (!alarmRingtone.isPlaying()) {
            alarmRingtone.play();
        }
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener(){
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    listener.onCancel();
                }
                return true;
            }
        };
    }

    @Override
    protected boolean hasNegativeButton() {
        return showNegativeBtn;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    listener.onClick();
                }
                return true;
            }
        };
    }

    public static interface OnDialogListener {
        boolean onClick();
        boolean onCancel();
    }

    public static void show(FragmentActivity activity, int titleId, String msg, int btnTitleId, OnDialogListener listener){
        DialogUtil.show(activity, DIALOG_NAME, AlertDialogWithCancelFragment_.builder().titleId(titleId)
                .positiveButtonTitleId(btnTitleId).errorMsg(msg).dialogType(DialogType.ALERT2).build()).setListener(listener);
    }

    public static void showWithTwo(FragmentActivity activity, int titleId, String msg, int btnTitleId, OnDialogListener listener){
        DialogUtil.show(activity, DIALOG_NAME, AlertDialogWithCancelFragment_.builder().titleId(titleId)
                .positiveButtonTitleId(btnTitleId).errorMsg(msg).dialogType(DialogType.ALERT2).build()).setListener(listener).setShowNegativeBtn(true);
    }

}
