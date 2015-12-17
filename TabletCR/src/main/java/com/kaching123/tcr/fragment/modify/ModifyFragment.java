package com.kaching123.tcr.fragment.modify;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment.ColumnInfo;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment.OnAddonsChangedListener;
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
    protected String saleItemGuid;

    private OnAddonsChangedListener onAddonsChangedListener;

    private ItemModifiersFragment innerFragment;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.modify_dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.modify_dialog_min_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.modify_dialog_height);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.modify_container,
                        innerFragment = ItemModifiersFragment_.builder()
                                .itemGuid(itemGuid)
                                .saleItemGuid(saleItemGuid)
                                .build()
                ).commit();
        innerFragment.setOnAddonsChangedListener(onAddonsChangedListener);
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

    public static void show(FragmentActivity activity,
                            String itemGuid,
                            OnAddonsChangedListener listener) {
        show(activity, itemGuid, null, listener);
    }

    public static void show(FragmentActivity activity, String itemGuid, String saleItemGuid, OnAddonsChangedListener listener) {
        DialogUtil.show(activity, DIALOG_NAME,
                ModifyFragment_.builder()
                        .itemGuid(itemGuid)
                        .saleItemGuid(saleItemGuid)
                        .build())
                .setOnAddonsChangedListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
