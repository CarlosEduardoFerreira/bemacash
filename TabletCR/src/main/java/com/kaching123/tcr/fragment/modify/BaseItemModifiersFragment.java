package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.AddonsContainerView;
import com.kaching123.tcr.component.ModifiersContainerView;
import com.kaching123.tcr.component.ModifiersContainerView_;
import com.kaching123.tcr.component.NoOptionsContainerView;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ModifierExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2;
import com.kaching123.tcr.store.ShopStore.ModifierView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gdubina on 26/11/13.
 */
@EFragment
public class BaseItemModifiersFragment extends Fragment {

    private static final Uri MODIFIER_URI = ShopProvider.contentUri(ModifierView.URI_CONTENT);

    private static final int LOADER_MODIFIERS = 1;
    private static final int LOADER_ADDONS = 2;
    private static final int LOADER_OPTIONALS = 3;

    private static String MODIFIERS_GROUP_ID = "modifiers";
    private static String ADDONS_GROUP_ID = "addons";
    private static String OPTIONAL_GROUP_ID = "options";

    @ViewById
    protected ModifiersContainerView modifiers;

    @ViewById
    protected LinearLayout modifiersGroupHolder;

    @ViewById
    protected AddonsContainerView addons;

    @ViewById
    protected NoOptionsContainerView optionals;

    @InstanceState
    protected String itemGuid;

    @InstanceState
    protected int numModifiers;

    @InstanceState
    protected int numAddons;

    @InstanceState
    protected int numOptionals;

    @InstanceState
    protected ArrayList<String> selectedModifierGuid;

    @InstanceState
    protected ArrayList<String> selectedAddonsGuids;

    @InstanceState
    protected ArrayList<String> selectedOptionalsGuids;

    private OnAddonsChangedListener onAddonsChangedListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setupParams(String itemGuid, int numModifiers, int numAddons, int numOptionals,
                            ArrayList<String> selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {
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

    public void reinitFragment(int numModifiers, int numAddons, int numOptionals, ArrayList<String> selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids) {

      /*  if (modifiersGroupHolder.getChildCount() > 0) {
            int n = modifiersGroupHolder.getChildCount();
            for (int i = 0; i < n; i++) {
                ModifiersContainerView view = (ModifiersContainerView) modifiersGroupHolder.getChildAt(i);
                if (!TextUtils.isEmpty(selectedModifierGuid.get(i))) {
                    view.setSelectedModifier(selectedModifierGuid.get(i));
                } else {
                    view.cleanSelection();
                }
            }
        } */


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
    }

    public void setOnAddonsChangedListener(OnAddonsChangedListener onAddonsChangedListener) {
        this.onAddonsChangedListener = onAddonsChangedListener;
    }

    public void onConfirm() {
        //Set<String> modifierGuids = modifiers.getSelectedItems();
        Set<String> modifierGropsGuids = new HashSet<>();
        Set<String> addonsGuids = addons.getSelectedItems();
        Set<String> optionalsGuids = optionals.getSelectedItems();

        int n = modifiersGroupHolder.getChildCount();
        for (int i = 0; i < n; i++){
            ModifiersContainerView view = (ModifiersContainerView) modifiersGroupHolder.getChildAt(i);
            Set<String> selectedItems = view.getSelectedItems();
            modifierGropsGuids.addAll(selectedItems);
        }


        if (onAddonsChangedListener != null) {
            onAddonsChangedListener.onAddonsChanged(new ArrayList<>(modifierGropsGuids), new ArrayList<>(addonsGuids), new ArrayList<>(optionalsGuids));
        }
    }

    private class ModifierModelLoader implements LoaderCallbacks<List<ModifierExModel>> {

        @Override
        public Loader<List<ModifierExModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(MODIFIER_URI)
                    .where(ModifierView2.ModifierTable.ITEM_GUID + " = ?", itemGuid)
                    .where(ModifierView2.ModifierTable.TYPE + "= ?", ModifierType.MODIFIER.ordinal())
                    .orderBy(ModifierView2.ModifierTable.TITLE)
                    .transform(new ModifierExFunction()).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierExModel>> listLoader, List<ModifierExModel> modifierModels) {
            assert modifierModels != null;
            if (modifierModels.isEmpty()) {
                modifiers.setVisibility(View.GONE);
            } else {
                modifiers.setVisibility(View.VISIBLE);
            }

            int modifyMaxColumn = 2;
            modifiers.setList(modifierModels);
            modifiers.setColumnNums(modifierModels.size() < modifyMaxColumn ? modifierModels.size() : modifyMaxColumn);


            Map<String, List<ModifierExModel>> sortedModifiers = sortModifiers(modifierModels);
            fillViewWithContainers(sortedModifiers);
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierExModel>> listLoader) {
            modifiers.setList(null);
        }

        protected void fillViewWithContainers(Map<String, List<ModifierExModel>> groupedItems){

            if (modifiersGroupHolder.getChildCount() > 0){
                modifiersGroupHolder.removeAllViews();
            }
            int totalWidth = 0;
            for (List<ModifierExModel> itemList : groupedItems.values()){
                ModifiersContainerView child = ModifiersContainerView_.build(getActivity());
                int width = calcContainerWidth(getActivity(), itemList.size());
                totalWidth += width;
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
                child.setLayoutParams(layoutParams);

                int modifyMaxColumn = 2;
                child.setList(itemList);
                child.setColumnNums(itemList.size() < modifyMaxColumn ? itemList.size() : modifyMaxColumn);

                String groupTitle;
                if(itemList.get(0).getGroup()!=null) {
                    groupTitle = itemList.get(0).getGroup().title;
                } else {
                    groupTitle = getString(R.string.dlg_section_modifier);
                }

                child.setContainerTitle(groupTitle);
                modifiersGroupHolder.addView(child);
            }

       }

        private  int calcContainerWidth(Context context, int addonCnt) {
            if (addonCnt == 0) {
                return 0;
            }
            int columnCnt = (addonCnt + 2) / context.getResources().getInteger(R.integer.modify_container_row_count);
            int btnWidth = context.getResources().getDimensionPixelOffset(R.dimen.modify_button_width);
            int padding = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_padding);
            int margin = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_margin);
            int space = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_space);

            return columnCnt * btnWidth + (columnCnt - 1) * space + 2 * padding + 2 * margin;
        }

        private Map<String, List<ModifierExModel>> sortModifiers(List<ModifierExModel> modifiers){
            Comparator<String> comparator = new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    int lhsNum = intRepresentation(lhs);
                    int rhsNum = intRepresentation(rhs);

                    return lhsNum - rhsNum;
                }

                private int intRepresentation(String s){
                /* free_modifiers, mod_group1, mod_group2, ..., addons, oprionals */
                    int i;
                    if (s.equals(MODIFIERS_GROUP_ID)){
                        i = 1;
                    }else if (s.equals(ADDONS_GROUP_ID)){
                        i = 3;
                    }else if (s.equals(OPTIONAL_GROUP_ID)){
                        i = 4;
                    }else{
                        i = 2;
                    }
                    return i;
                }
            };

            HashMap<String, List<ModifierExModel>> groupedItems = new HashMap<>();
            for (ModifierExModel item : modifiers){
                String key;
                switch (item.type){
                    case ADDON:
                        key = ADDONS_GROUP_ID;
                        break;
                    case OPTIONAL:
                        key = OPTIONAL_GROUP_ID;
                        break;
                    default:
                        key = item.modifierGroupGuid != null ? item.modifierGroupGuid : MODIFIERS_GROUP_ID;
                        break;
                }
                if (!groupedItems.containsKey(key)) {
                    groupedItems.put(key, new ArrayList<ModifierExModel>());
                }
                groupedItems.get(key).add(item);
            }

            ArrayList<String> keys = new ArrayList<>(groupedItems.keySet());
            Collections.sort(keys, comparator);
            LinkedHashMap<String, List<ModifierExModel>> groupedSortedItems = new LinkedHashMap<>(groupedItems.size());
            for (String key : keys){
                groupedSortedItems.put(key, groupedItems.get(key));
            }

            return groupedSortedItems;
        }


    }

    private class AddonModelLoader implements LoaderCallbacks<List<ModifierExModel>> {

        final AddonsContainerView containerView;
        final ModifierType type;

        public AddonModelLoader(ModifierType type, AddonsContainerView containerView) {
            this.type = type;
            this.containerView = containerView;
        }

        @Override
        public Loader<List<ModifierExModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(MODIFIER_URI)
                    .where(ModifierView2.ModifierTable.ITEM_GUID + " = ?", itemGuid)
                    .where(ModifierView2.ModifierTable.TYPE + "= ?", type.ordinal())
                    .orderBy(ModifierView2.ModifierTable.TITLE)
                    .transform(new ModifierExFunction()).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierExModel>> listLoader, List<ModifierExModel> addonsModels) {
            assert addonsModels != null;
            if (addonsModels.isEmpty()) {
                containerView.setVisibility(View.GONE);
            } else {
                containerView.setVisibility(View.VISIBLE);
            }
            containerView.setList(addonsModels);
            int addsOnMax = 3;
            int onOptionsMax = 2;
            if (type == ModifierType.ADDON)
                containerView.setColumnNums(addonsModels.size() < addsOnMax ? addonsModels.size() : addsOnMax);
            else
                containerView.setColumnNums(addonsModels.size() < onOptionsMax ? addonsModels.size() : onOptionsMax);
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierExModel>> listLoader) {
            containerView.setList(null);
        }

    }

    public interface OnAddonsChangedListener {
        void onAddonsChanged(ArrayList<String> modifierGuid,
                             ArrayList<String> addonsGuid,
                             ArrayList<String> optionalsGuid);
    }

    private int calcWindowWidth(int numModifiers, int numAddons, int numOptionals) {
        if (numModifiers == 0 && numAddons == 0 && numOptionals == 0) {
            return 0;
        }

        int modifyRowCount = getResources().getInteger(R.integer.modify_container_row_count);

        int addOnRowCount = getResources().getInteger(R.integer.modify_container_row_count);

        HashMap<ColumnInfo.Type, ColumnInfo> columnsInfo = calculateAvailableColumns(numModifiers, numAddons, numOptionals, modifyRowCount, addOnRowCount);

        int width = 0;
        int margin = getResources().getDimensionPixelOffset(R.dimen.modify_container_margin_left) * 2;

        ViewGroup[] viewGroups = new ViewGroup[]{modifiers, addons, optionals};

        for (int i = 0; i < viewGroups.length; i++) {
            ColumnInfo c = i == 0 ? columnsInfo.get(ColumnInfo.Type.M) : i == 1 ? columnsInfo.get(ColumnInfo.Type.A) : columnsInfo.get(ColumnInfo.Type.O);
            int containerWidth = calcContainerWidth(getActivity(), c.itemsCount, c.displayColumn, c);
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

    public static int calcContainerWidth(Context context, int addonCnt, int availableColumns, ColumnInfo info) {
        if (addonCnt == 0) {
            return 0;
        }
        int columnCnt = (addonCnt + (info.type == ColumnInfo.Type.A && info.itemsCount >= 3 ? 3 : 2)) / context.getResources().getInteger(R.integer.modify_container_row_count);
        columnCnt = Math.min(columnCnt, availableColumns);
        int btnWidth = context.getResources().getDimensionPixelOffset(R.dimen.modify_button_width);
        int padding = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_padding);
        int space = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_space);
        int size = columnCnt * btnWidth + (columnCnt - 1) * space + 2 * padding;
        return size;
    }

    public static HashMap<ColumnInfo.Type, ColumnInfo> calculateAvailableColumns(int numModifiers, int numAddons, int numOptionals, int modifyRowCount, int addOnRowCount) {
        HashMap<ColumnInfo.Type, ColumnInfo> columnsInfo = new HashMap<ColumnInfo.Type, ColumnInfo>();

        ColumnInfo info;
        ArrayList<ColumnInfo> items = new ArrayList<>(3);
        items.add(info = new ColumnInfo(ColumnInfo.Type.M, numModifiers, modifyRowCount));
        columnsInfo.put(info.type, info);
        items.add(info = new ColumnInfo(ColumnInfo.Type.A, numAddons, addOnRowCount));
        columnsInfo.put(info.type, info);
        items.add(info = new ColumnInfo(ColumnInfo.Type.O, numOptionals, modifyRowCount));
        columnsInfo.put(info.type, info);
        Collections.sort(items);

        int dif = 0;
        int total = 0;
        addonColumnCount = 3;
        otherColumnCount = 2;
        int sections = items.size();

        for (int k = 0; k < sections; k++) {
            ColumnInfo i = items.get(k);

            if (i.type == ColumnInfo.Type.A) {
                i.displayColumn = Math.min(i.columns, addonColumnCount);
            } else {
                i.displayColumn = Math.min(i.columns, otherColumnCount);
            }
        }

        return columnsInfo;
    }

    public static class ColumnInfo implements Comparable<ColumnInfo> {

        public enum Type {M, A, O}

        Type type;
        int columns;
        int displayColumn;
        int itemsCount;

        ColumnInfo(Type type, int items, int rowCount) {
            this.type = type;
            this.itemsCount = items;
            this.columns = (items + (this.type == Type.A ? 3 : 2)) / rowCount;
        }

        @Override
        public int compareTo(ColumnInfo o) {
            return o.columns == columns ? 0 : columns > o.columns ? 1 : -1;
        }
    }
}

