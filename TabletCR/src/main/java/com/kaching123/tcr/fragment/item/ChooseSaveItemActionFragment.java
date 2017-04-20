package com.kaching123.tcr.fragment.item;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.kaching123.tcr.fragment.item.ChooseSaveItemActionFragment.SaveItemAction.ADD_MORE;
import static com.kaching123.tcr.fragment.item.ChooseSaveItemActionFragment.SaveItemAction.DUPLICATE;
import static com.kaching123.tcr.fragment.item.ChooseSaveItemActionFragment.SaveItemAction.NO;

/**
 * Created by mboychenko on 4/18/2017.
 */

@EFragment
public class ChooseSaveItemActionFragment extends StyledDialogFragment {

    public static final String SAVE_ITEM_CHOOSE_ACTION_DIALOG = "SAVE_ITEM_CHOOSE_ACTION_DIALOG";

    @ViewById
    protected CheckBox btnYes;

    @ViewById
    protected CheckBox btnNo;

    @ViewById
    protected CheckBox btnDup;

    private ChooseSaveItemActionCallback callback;
    private SaveItemAction action = NO;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.choose_save_item_action_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.item_save_action;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_continue;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onConfirm(action);
                return true;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.base_dlg_width), WRAP_CONTENT);
    }

    @AfterViews
    protected void init(){
        btnDup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    action = DUPLICATE;
                    btnNo.setChecked(false);
                    btnYes.setChecked(false);
                } else if(!btnNo.isChecked() && !btnYes.isChecked()) {
                    btnDup.setChecked(true);
                }
            }
        });
        btnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    action = NO;
                    btnDup.setChecked(false);
                    btnYes.setChecked(false);
                } else if(!btnDup.isChecked() && !btnYes.isChecked()) {
                    btnNo.setChecked(true);
                }
            }
        });
        btnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    action = ADD_MORE;
                    btnNo.setChecked(false);
                    btnDup.setChecked(false);
                } else if(!btnNo.isChecked() && !btnDup.isChecked()) {
                    btnYes.setChecked(true);
                }
            }
        });

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
        ADD_MORE, NO, DUPLICATE
    }
}
