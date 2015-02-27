package com.kaching123.tcr.fragment.modify;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment.ColumnInfo;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment.OnAddonsChangedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vkompaniets on 18.11.13.
 */

@EFragment
public class ModifyFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "modifyFragment";

    @FragmentArg
    protected String itemGuid;

    @FragmentArg
    protected int numModifiers;

    @FragmentArg
    protected int numAddons;

    @FragmentArg
    protected int numOptionals;

    @FragmentArg
    protected String selectedModifierGuid;

    @FragmentArg
    protected ArrayList<String> selectedAddonsGuids;

    @FragmentArg
    protected ArrayList<String> selectedOptionalsGuids;

    private OnAddonsChangedListener onAddonsChangedListener;

    private ItemModifiersInnerFragment innerFragment;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.modify_dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.modify_container,
                        innerFragment = ItemModifiersInnerFragment_.builder()
                                .argItemGuid(itemGuid)
                                .argNumModifiers(numModifiers)
                                .argNumAddons(numAddons)
                                .argNumOptionals(numOptionals)
                                .argSelectedModifierGuid(selectedModifierGuid)
                                .argSelectedAddonsGuids(selectedAddonsGuids)
                                .argSelectedOptionalsGuids(selectedOptionalsGuids)
                                .build()
                ).commit();
        innerFragment.setOnAddonsChangedListener(onAddonsChangedListener);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = calcWindowWidth(numModifiers, numAddons, numOptionals);
        params.height = getResources().getDimensionPixelOffset(R.dimen.modify_dialog_height);
    }

    private int calcWindowWidth(int numModifiers, int numAddons, int numOptionals) {
        if (numModifiers == 0 && numAddons == 0 && numOptionals == 0) {
            return 0;
        }

        int width = 0;
        int margin = getResources().getDimensionPixelOffset(R.dimen.modify_container_margin_left) * 2;
        int modifyRowCount = getResources().getInteger(R.integer.modify_container_row_count);
        int addOnRowCount = getResources().getInteger(R.integer.modify_container_row_count);

        HashMap<ColumnInfo.Type, ColumnInfo> columnsInfo = ItemModifiersInnerFragment.calculateAvailableColumns(numModifiers, numAddons, numOptionals, modifyRowCount, addOnRowCount);

        for (int i = 0; i < columnsInfo.size(); i++) {
            ColumnInfo c = i == 0 ? columnsInfo.get(ColumnInfo.Type.M) : i == 1 ? columnsInfo.get(ColumnInfo.Type.A) : columnsInfo.get(ColumnInfo.Type.O);
            int containerWidth = ItemModifiersInnerFragment.calcContainerWidth(getActivity(), c.itemsCount, c.displayColumn);
            width += containerWidth + (c.itemsCount == 0 ? 0 : margin);

        }

        width += margin * 2;
        int minWidth = getResources().getDimensionPixelOffset(R.dimen.modify_dialog_min_width);

        return (width > minWidth ? width : minWidth);
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                onConfirm();
                return true;
            }
        };

    }

    private void onConfirm() {
        innerFragment.onConfirm();
        /*String modifierGuid = modifiers.getSelectedModifier();
        Set<String> addonsGuids = addons.getSelectedItems();
        Set<String> optionalsGuids = optionals.getSelectedItems();
        if (onAddonsChangedListener != null) {
            onAddonsChangedListener.onAddonsChanged(modifierGuid, new ArrayList<String>(addonsGuids), new ArrayList<String>(optionalsGuids));
        }*/
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
        return R.string.btn_confirm;
    }

    public void setOnAddonsChangedListener(OnAddonsChangedListener onAddonsChangedListener) {
        this.onAddonsChangedListener = onAddonsChangedListener;
    }

    public static void show(FragmentActivity activity, String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, String defaultModifierGuid, OnAddonsChangedListener listener) {
        DialogUtil.show(activity, DIALOG_NAME, ModifyFragment_.builder().numModifiers(modifiersCount).numAddons(addonsCount).numOptionals(optionalsCount).selectedModifierGuid(defaultModifierGuid).itemGuid(itemGuid).build()).setOnAddonsChangedListener(listener);
    }

    public static void show(FragmentActivity activity, String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, String selectedModifierGuid, ArrayList<String> selectedAddonsGuid, ArrayList<String> selectedOptionalsGuid, OnAddonsChangedListener listener) {
        DialogUtil.show(activity, DIALOG_NAME, ModifyFragment_.builder().numModifiers(modifiersCount).numAddons(addonsCount).numOptionals(optionalsCount).itemGuid(itemGuid).selectedModifierGuid(selectedModifierGuid).selectedAddonsGuids(selectedAddonsGuid).selectedOptionalsGuids(selectedOptionalsGuid).build()).setOnAddonsChangedListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
