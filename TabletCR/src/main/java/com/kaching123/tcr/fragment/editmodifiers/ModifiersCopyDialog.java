package com.kaching123.tcr.fragment.editmodifiers;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.CopyModifiersCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ModifierType;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by vkompaniets on 12.12.13.
 */

@EFragment(R.layout.editmodifiers_copymodifiers_dialog)
public class ModifiersCopyDialog extends StyledDialogFragment implements InnerCopyFragment.IItemClickListener {

    public static String DIALOG_NAME = "modifier_copy_dialog";

    @FragmentArg
    protected String fromItem;

    @FragmentArg
    protected String toItem;

    private InnerCopyFragment modifiers;

    private InnerCopyFragment addons;

    private InnerCopyFragment options;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        enablePositiveButtons(false);

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                .replace(R.id.modifiers, modifiers = InnerCopyFragment_.builder()
                        .itemGuid(fromItem)
                        .type(ModifierType.MODIFIER)
                        .build().setListener(this))
                .replace(R.id.addons, addons = InnerCopyFragment_.builder()
                        .itemGuid(fromItem)
                        .type(ModifierType.ADDON)
                        .build().setListener(this))
                .replace(R.id.options, options = InnerCopyFragment_.builder()
                        .itemGuid(fromItem)
                        .type(ModifierType.OPTIONAL)
                        .build().setListener(this))
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

    public static void show(FragmentActivity activity, String fromItem, String toItem) {
        DialogUtil.show(activity, DIALOG_NAME, ModifiersCopyDialog_.builder().fromItem(fromItem).toItem(toItem).build());
    }

    private OnDialogClickListener clickListener = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            HashSet<String> all = collectSelectedItems();
            if (!all.isEmpty()) {
                CopyModifiersCommand.start(getActivity(), toItem, new ArrayList<String>(all));
            }
            getActivity().finish();
            return true;
        }
    };

    private HashSet<String> collectSelectedItems() {
        HashSet<String> all = new HashSet<String>();
        all.addAll(modifiers.getSelectedItems());
        all.addAll(addons.getSelectedItems());
        all.addAll(options.getSelectedItems());
        return all;
    }

    @Override
    public void onClick() {
        HashSet<String> all = collectSelectedItems();
        enablePositiveButtons(!all.isEmpty());
    }
}
