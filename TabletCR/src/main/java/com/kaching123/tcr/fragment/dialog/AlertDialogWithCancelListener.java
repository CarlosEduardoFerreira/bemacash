package com.kaching123.tcr.fragment.dialog;

import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;

import org.androidannotations.annotations.EFragment;

/**
 * Created by vkompaniets on 10.01.14.
 */
@EFragment
public class AlertDialogWithCancelListener extends AlertDialogFragment{

    private ArrayAdapter adapter;

    public AlertDialogWithCancelListener setAdapter(ArrayAdapter adapter){
        this.adapter = adapter;
        return this;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener(){
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        };
    };

    public static void show(FragmentActivity activity, int titleId, String msg, int btnTitleId, OnDialogClickListener listener, ArrayAdapter adapter){
        DialogUtil.show(activity, DIALOG_NAME, AlertDialogWithCancelListener_.builder().titleId(titleId).positiveButtonTitleId(btnTitleId).errorMsg(msg).dialogType(DialogType.CONFIRM_NONE).build()).setAdapter(adapter).setOnPositiveListener(listener);
    }

}
