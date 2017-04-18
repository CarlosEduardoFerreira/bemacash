package com.kaching123.tcr.fragment.item;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import org.androidannotations.annotations.EFragment;

/**
 * Created by mboychenko on 4/18/2017.
 */

@EFragment
public class ChooseSaveItemActionFragment extends StyledDialogFragment {

    public static final String SAVE_ITEM_CHOOSE_ACTION_DIALOG = "SAVE_ITEM_CHOOSE_ACTION_DIALOG";

    private ChooseSaveItemActionCallback callback;

    @Override
    protected int getDialogContentLayout() {
        return 0;
    }

    @Override
    protected int getDialogTitle() {
        return 0;
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
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public void setCallback(ChooseSaveItemActionCallback callback) {
        this.callback = callback;
    }

    public interface ChooseSaveItemActionCallback {

    }

    public static void show(FragmentActivity activity, ChooseSaveItemActionCallback callback) {
        DialogUtil.show(activity, SAVE_ITEM_CHOOSE_ACTION_DIALOG, ChooseSaveItemActionFragment_.builder().build()).setCallback(callback);
    }
}
