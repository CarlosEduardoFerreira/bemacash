package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.AddonsContainerView;
import com.kaching123.tcr.component.ModifiersContainerView;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by gdubina on 26/11/13.
 */
@EFragment
public class BaseItemModifiersFragment extends Fragment {

    private static final Uri MODIFIER_URI = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);
    private static final Uri ADDON_URI = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    private static final int LOADER_MODIFIERS = 1;
    private static final int LOADER_ADDONS = 2;
    private static final int LOADER_OPTIONALS = 3;

    @ViewById
    protected ModifiersContainerView modifiers;

    @ViewById
    protected AddonsContainerView addons;

    @ViewById
    protected AddonsContainerView optionals;

    @InstanceState
    protected String itemGuid;

    @InstanceState
    protected int numModifiers;

    @InstanceState
    protected int numAddons;

    @InstanceState
    protected int numOptionals;

    @InstanceState
    protected String selectedModifierGuid;

    @InstanceState
    protected ArrayList<String> selectedAddonsGuids;

    @InstanceState
    protected ArrayList<String> selectedOptionalsGuids;

    private OnAddonsChangedListener onAddonsChangedListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setupParams(String itemGuid, int numModifiers, int numAddons, int numOptionals, String selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {
        this.itemGuid = itemGuid;
        this.numModifiers = numModifiers;
        this.numAddons = numAddons;
        this.numOptionals = numOptionals;

        this.selectedModifierGuid = selectedModifierGuid;
        this.selectedAddonsGuids = selectedAddonsGuids;
        this.selectedOptionalsGuids = selectedOptionalsGuids;

        calcWindowWidth(numModifiers, numAddons, numOptionals);
        reinitFragment(numModifiers, numAddons, numOptionals, selectedModifierGuid, selectedAddonsGuids, selectedOptionalsGuids);
    }

    public void reinitFragment(int numModifiers, int numAddons, int numOptionals, String selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {
        if (!TextUtils.isEmpty(selectedModifierGuid)) {
            modifiers.setSelectedModifier(selectedModifierGuid);
        } else {
            modifiers.cleanSelection();
        }

        if (selectedAddonsGuids != null) {
            addons.setSelectedAddonsGuids(selectedAddonsGuids);
        } else {
            addons.cleanSelection();
        }

        if (selectedOptionalsGuids != null) {
            optionals.setSelectedAddonsGuids(selectedOptionalsGuids);
        } else {
            optionals.cleanSelection();
        }

        getLoaderManager().restartLoader(LOADER_MODIFIERS, null, new ModifierModelLoader());
        getLoaderManager().restartLoader(LOADER_ADDONS, null, new AddonModelLoader(ModifierType.ADDON, addons));
        getLoaderManager().restartLoader(LOADER_OPTIONALS, null, new AddonModelLoader(ModifierType.OPTIONAL, optionals));
    }

    private static int addonColumnCount;
    private static int otherColumnCount;

    @AfterViews
    protected void init() {
        modifiers.setContainerTitle(getString(R.string.dlg_section_modifier));
        addons.setContainerTitle(getString(R.string.dlg_section_addon));
        optionals.setContainerTitle(getString(R.string.dlg_section_optional));
        addonColumnCount = 3;
        otherColumnCount = getActivity().getResources().getInteger(R.integer.addon_nooption_column);
    }

    public void setOnAddonsChangedListener(OnAddonsChangedListener onAddonsChangedListener) {
        this.onAddonsChangedListener = onAddonsChangedListener;
    }

    public void onConfirm() {
        String modifierGuid = modifiers.getSelectedModifier();
        Set<String> addonsGuids = addons.getSelectedItems();
        Set<String> optionalsGuids = optionals.getSelectedItems();
        if (onAddonsChangedListener != null) {
            onAddonsChangedListener.onAddonsChanged(modifierGuid, new ArrayList<String>(addonsGuids), new ArrayList<String>(optionalsGuids));
        }
    }

    private class ModifierModelLoader implements LoaderCallbacks<List<ModifierModel>> {

        @Override
        public Loader<List<ModifierModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(MODIFIER_URI)
                    .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                    .where(ModifierTable.TYPE + "= ?", ModifierType.MODIFIER.ordinal())
                    .orderBy(ModifierTable.TITLE)
                    .transform(new ModifierFunction()).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierModel>> listLoader, List<ModifierModel> modifierModels) {
            assert modifierModels != null;
            if (modifierModels.isEmpty()) {
                modifiers.setVisibility(View.GONE);
            } else {
                modifiers.setVisibility(View.VISIBLE);
            }
            modifiers.setList(modifierModels);
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierModel>> listLoader) {
            modifiers.setList(null);
        }

    }

    private class AddonModelLoader implements LoaderCallbacks<List<ModifierModel>> {

        final AddonsContainerView containerView;
        final ModifierType type;

        public AddonModelLoader(ModifierType type, AddonsContainerView containerView) {
            this.type = type;
            this.containerView = containerView;
        }

        @Override
        public Loader<List<ModifierModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ADDON_URI)
                    .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                    .where(ModifierTable.TYPE + "= ?", type.ordinal())
                    .orderBy(ModifierTable.TITLE)
                    .transform(new ModifierFunction()).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierModel>> listLoader, List<ModifierModel> addonsModels) {
            assert addonsModels != null;
            if (addonsModels.isEmpty()) {
                containerView.setVisibility(View.GONE);
            } else {
                containerView.setVisibility(View.VISIBLE);
            }
            containerView.setList(addonsModels);
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierModel>> listLoader) {
            containerView.setList(null);
        }

    }

    public static interface OnAddonsChangedListener {
        void onAddonsChanged(String modifierGuid, ArrayList<String> addonsGuid, ArrayList<String> optionalsGuid);
    }

    private int calcWindowWidth(int numModifiers, int numAddons, int numOptionals) {
        if (numModifiers == 0 && numAddons == 0 && numOptionals == 0) {
            return 0;
        }

        int rowCount = getResources().getInteger(R.integer.modify_container_row_count);

        HashMap<ColumnInfo.Type, ColumnInfo> columnsInfo = calculateAvailableColumns(numModifiers, numAddons, numOptionals, rowCount);

        int width = 0;
        int margin = getResources().getDimensionPixelOffset(R.dimen.modify_container_margin_left) * 2;

        ViewGroup[] viewGroups = new ViewGroup[]{modifiers, addons, optionals};

        for (int i = 0; i < viewGroups.length; i++) {
            ColumnInfo c = i == 0 ? columnsInfo.get(ColumnInfo.Type.M) : i == 1 ? columnsInfo.get(ColumnInfo.Type.A) : columnsInfo.get(ColumnInfo.Type.O);
            int containerWidth = calcContainerWidth(getActivity(), c.itemsCount, c.displayColumn);
            setContainerWidth(viewGroups[i], containerWidth);
            width += containerWidth + (c.itemsCount == 0 ? 0 : margin);
        }

        width += margin * 2;
        int minWidth = getResources().getDimensionPixelOffset(R.dimen.modify_dialog_min_width);

        return (width > minWidth ? width : minWidth);
    }

    private void setContainerWidth(ViewGroup container, int width) {
        ViewGroup.LayoutParams params = container.getLayoutParams();
        params.width = width;
    }

    public static int calcContainerWidth(Context context, int addonCnt, int availableColumns) {
        if (addonCnt == 0) {
            return 0;
        }
        int columnCnt = (addonCnt + 2) / context.getResources().getInteger(R.integer.modify_container_row_count);
        columnCnt = Math.min(columnCnt, availableColumns);
        int btnWidth = context.getResources().getDimensionPixelOffset(R.dimen.modify_button_width);
        int padding = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_padding);
        int space = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_space);
        int size = columnCnt * btnWidth + (columnCnt - 1) * space + 2 * padding;
        return size;
    }

    public static HashMap<ColumnInfo.Type, ColumnInfo> calculateAvailableColumns(int numModifiers, int numAddons, int numOptionals, int rowCount) {
        HashMap<ColumnInfo.Type, ColumnInfo> columnsInfo = new HashMap<ColumnInfo.Type, ColumnInfo>();

        ColumnInfo info;
        ArrayList<ColumnInfo> items = new ArrayList<ColumnInfo>(3);
        items.add(info = new ColumnInfo(ColumnInfo.Type.M, numModifiers, rowCount));
        columnsInfo.put(info.type, info);
        items.add(info = new ColumnInfo(ColumnInfo.Type.A, numAddons, rowCount));
        columnsInfo.put(info.type, info);
        items.add(info = new ColumnInfo(ColumnInfo.Type.O, numOptionals, rowCount));
        columnsInfo.put(info.type, info);
        Collections.sort(items);

        int dif = 0;
        int total = 0;

        int sections = items.size();

            for (int k = 0; k < sections; k++) {
                ColumnInfo i = items.get(k);

                if(i.type == ColumnInfo.Type.A)
                {
                    i.displayColumn = Math.min(i.columns, addonColumnCount);
                }
                else
                {
                    i.displayColumn = Math.min(i.columns, otherColumnCount);
                }
            }

        return columnsInfo;
    }

    public static class ColumnInfo implements Comparable<ColumnInfo> {

        public static enum Type {M, A, O}

        Type type;
        int columns;
        int displayColumn;
        int itemsCount;

        ColumnInfo(Type type, int items, int rowCount) {
            this.type = type;
            this.itemsCount = items;
            this.columns = (items + 2) / rowCount;
        }

        @Override
        public int compareTo(ColumnInfo o) {
            return o.columns == columns ? 0 : columns > o.columns ? 1 : -1;
        }
    }
}

