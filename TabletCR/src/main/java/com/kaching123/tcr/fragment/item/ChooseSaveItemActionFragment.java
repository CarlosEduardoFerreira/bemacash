package com.kaching123.tcr.fragment.item;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import org.androidannotations.annotations.EFragment;

import static com.kaching123.tcr.fragment.item.ChooseSaveItemActionFragment.SaveItemAction.SAVE;

/**
 * Created by mboychenko on 4/18/2017.
 */

@EFragment
public class ChooseSaveItemActionFragment extends StyledDialogFragment {

    public static final String SAVE_ITEM_CHOOSE_ACTION_DIALOG = "SAVE_ITEM_CHOOSE_ACTION_DIALOG";

    private ChooseSaveItemActionCallback callback;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.choose_save_item_action_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.choose_item_save_action;
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
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onConfirm(SAVE);
                return true;
            }
        };
    }

    public void setCallback(ChooseSaveItemActionCallback callback) {
        this.callback = callback;
    }

    public interface ChooseSaveItemActionCallback {
        void onConfirm(SaveItemAction action);
    }

    public static void show(FragmentActivity activity, ChooseSaveItemActionCallback callback) {
        DialogUtil.show(activity, SAVE_ITEM_CHOOSE_ACTION_DIALOG, ChooseSaveItemActionFragment_.builder().build()).setCallback(callback);
    }

    public enum SaveItemAction {
        ADD_MORE, SAVE, DUPLICATE
    }
}
