package com.kaching123.tcr.fragment.editmodifiers;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.CopyModifiersCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierType;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by vkompaniets on 12.12.13.
 */

@EFragment(R.layout.editmodifiers_copymodifiers_dialog)
public class ModifiersCopyDialog extends StyledDialogFragment {

    public static String DIALOG_NAME = "modifier_copy_dialog";

    @FragmentArg
    protected ItemExModel itemModel;

    @FragmentArg
    protected String copyToItemGuid;

    private InnerCopyFragment modifiers;

    private InnerCopyFragment addons;

    private InnerCopyFragment options;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                .replace(R.id.modifiers, modifiers = InnerCopyFragment_.builder()
                        .itemGuid(itemModel.guid)
                        .type(ModifierType.MODIFIER)
                        .build())
                .replace(R.id.addons, addons = InnerCopyFragment_.builder()
                        .itemGuid(itemModel.guid)
                        .type(ModifierType.ADDON)
                        .build())
                .replace(R.id.options, options = InnerCopyFragment_.builder()
                        .itemGuid(itemModel.guid)
                        .type(ModifierType.OPTIONAL)
                        .build())
                .commit();

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.copy_modify_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.copy_modify_dialog_height);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.editmodifiers_copymodifiers_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.modify_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_copy;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return clickListener;
    }

    public void setOnClosedListener(OnClosedListener onClosedListener) {
        this.onClosedListener = onClosedListener;
    }

    public static void show(FragmentActivity activity, String copyToItemGuid, ItemExModel srcItemModel, OnClosedListener onClosedListener) {
        DialogUtil.show(activity, DIALOG_NAME, ModifiersCopyDialog_.builder().itemModel(srcItemModel).copyToItemGuid(copyToItemGuid).build()).setOnClosedListener(onClosedListener);
    }

    private OnDialogClickListener clickListener = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            HashSet<String> all = new HashSet<String>();
            all.addAll(modifiers.getSelectedItems());
            all.addAll(addons.getSelectedItems());
            all.addAll(options.getSelectedItems());
            if (!all.isEmpty()) {
                CopyModifiersCommand.start(getActivity(), copyToItemGuid, new ArrayList<String>(all));
            }
            if(onClosedListener != null){
                onClosedListener.onDialogSuccessClosed();
            }
            return true;
        }
    };

    private OnClosedListener onClosedListener;

    public static interface OnClosedListener{
        void onDialogSuccessClosed();
    }
}
