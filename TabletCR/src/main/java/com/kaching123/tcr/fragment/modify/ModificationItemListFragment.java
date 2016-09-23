package com.kaching123.tcr.fragment.modify;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ModifierExFunction;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ItemGroupTable;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ModifierTable;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.List;


/**
 * Created by alboyko on 01.12.2015.
 */

@EFragment(R.layout.modifier_groups_list_fragment)
@OptionsMenu(R.menu.modifier_group_fragment)
public class ModificationItemListFragment extends ModifierItemListFragment {

    private final static String KEY = "Key_b";

    protected ModifierGroupItemFragment groupFragment;

    @OptionsMenuItem
    protected MenuItem actionEdit;

    @OptionsMenuItem
    protected MenuItem actionDelete;

    @InstanceState
    protected ModifierGroupModel itemModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modifier_groups_list_fragment, container, false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (itemModel == null) {
            actionEdit.setVisible(false);
            actionDelete.setVisible(false);

        } else {
            actionEdit.setVisible(true);
            actionDelete.setVisible(true);
        }
    }

    private Fragment getGroupFragment() {
        List<Fragment> fragmentsList = getFragmentManager().getFragments();
        List<Fragment> childFragment = getChildFragmentManager().getFragments();
        fragmentsList.addAll(childFragment);
        if (!fragmentsList.isEmpty()) {
            for (Fragment fr : fragmentsList) {
                if (fr instanceof ModifierGroupItemFragment) {
                    return fr;
                }
            }
        }
        return null;
    }

    @Override
    protected void attachViews() {
        super.attachViews();
        groupFragment = (ModifierGroupItemFragment) getGroupFragment();
        if (groupFragment != null) {
            groupFragment.setListener(new GroupCallback() {

                @Override
                public void onCategoryChanged(long id, String depGuid, String catGuid) {

                }

                @Override
                public void onItemSelected(ModifierGroupModel item) {
                    itemModel = item;
                    Bundle b = new Bundle();
                    if (itemModel != null) {
                        b.putString(KEY, itemModel.guid);
                    }
                    getLoaderManager().restartLoader(0, b, self());
                    getActivity().invalidateOptionsMenu();
                }
            });
            groupFragment.setItemGuid(model.guid);
        } else {
            Logger.e("ModifierGroupItemFragment isn't in list");
        }
    }

    @OptionsItem
    protected void actionEditSelected() {
        callback.onEdit(itemModel);
    }

    protected ModifierItemListFragment self() {
        return this;
    }

    @OptionsItem
    protected void actionAddSelected() {
        callback.onAdd(modType, itemModel == null ? null : itemModel.guid);
    }

    @OptionsItem
    protected void actionDeleteSelected() {
        AlertDialogWithCancelListener.show(getActivity(),
                R.string.wireless_remove_item_title,
                getString(R.string.wireless_remove_item_body),
                R.string.btn_confirm,
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onDeleteGroup(itemModel);
                        return true;
                    }
                }, null
        );
    }

    public static ModificationItemListFragment build(ModifierType type) {
        return ModificationItemListFragment_.builder().build().setModType(type);
    }

    public ModificationItemListFragment setModType(ModifierType modType) {
        this.modType = modType;
        return this;
    }

    @Override
    public Loader<List<ModifierExModel>> onCreateLoader(int id, Bundle args) {
        getListView().clearChoices();
        if (mode != null) {
            mode.finish();
        }
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_UNITS);
        loader.where(ShopSchema2.ModifierView2.ModifierTable.ITEM_GUID + " = ?", model.guid);
        if (modType != null) {
            loader.where(ShopSchema2.ModifierView2.ModifierTable.TYPE + " = ?", modType.ordinal());
        }
        boolean draggable = false;
        if (args != null && args.containsKey(KEY)) {
            loader.where(ShopSchema2.ModifierView2.ModifierTable.ITEM_GROUP_GUID + " = ?", args.getString(KEY));
            draggable = true;
        }
        loader.orderBy(ItemGroupTable.ORDER_NUM + "," + ModifierTable.ORDER_NUM);

        adapter.setDraggable(draggable);
        getListView().setDragEnabled(draggable);

        return loader.transformRow(new ModifierExFunction()).build(getActivity());
    }
}