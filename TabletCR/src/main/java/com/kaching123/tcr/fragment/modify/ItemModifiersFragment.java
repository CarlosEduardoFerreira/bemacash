package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand.SelectedModifierExModel;
import com.kaching123.tcr.component.ModifierContainerView;
import com.kaching123.tcr.component.ModifierContainerView_;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.payment.ModifierGroupCondition;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by vkompaniets on 6/23/2015.
 */
@EFragment(R.layout.item_modifiers_inner_fragment_bra)
public class ItemModifiersFragment extends Fragment{

    private static String MODIFIERS_GROUP_ID = "modifiers";
    private static String ADDONS_GROUP_ID = "addons";
    private static String OPTIONAL_GROUP_ID = "options";

    @ViewById
    protected LinearLayout holder;

    @FragmentArg
    protected String itemGuid;

    @FragmentArg
    protected String saleItemGuid;

    private OnAddonsChangedListener onAddonsChangedListener;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (itemGuid == null){
            return;
        }

        collectModifiers();
    }

    protected void collectModifiers(){
        CollectModifiersCommand.start(getActivity(), itemGuid, saleItemGuid, collectionCallback);
    }

    public CollectModifiersCommand.BaseCollectModifiersCallback collectionCallback = new CollectModifiersCommand.BaseCollectModifiersCallback() {
        @Override
        public void onCollected(ArrayList<SelectedModifierExModel> modifiers, ItemExModel model, BigDecimal price, BigDecimal quantity, Unit unit, boolean hasAutoApply) {
            Map<String, List<SelectedModifierExModel>> sortedModifiers = sortModifiers(modifiers);
            fillViewWithContainers(sortedModifiers);
        }
    };

    public void setOnAddonsChangedListener(OnAddonsChangedListener onAddonsChangedListener) {
        this.onAddonsChangedListener = onAddonsChangedListener;
    }

    public void onConfirm() {
        Set<String> modifierGuids = new HashSet<>();
        Set<String> addonsGuids = new HashSet<>();
        Set<String> optionalsGuids = new HashSet<>();

        int n = holder.getChildCount();
        for (int i = 0; i < n; i++){
            ModifierContainerView view = (ModifierContainerView) holder.getChildAt(i);
            Set<String> selectedItems = view.getSelectedItems();
            ModifierGroupModel group = view.getGroup();
            if (group != null && group.conditionValue > 0){
                if (ModifierGroupCondition.EQUAL == group.condition && selectedItems.size() != group.conditionValue){
                    onAddonsChangedListener.onModifiersCountInsufficient(group);
                    return;
                }
                if (ModifierGroupCondition.LESS_EQUAL == group.condition && selectedItems.size() > group.conditionValue){
                    onAddonsChangedListener.onModifiersCountInsufficient(group);
                    return;
                }
            }

            switch (view.getModifierType()){
                case MODIFIER:
                    modifierGuids.addAll(selectedItems);
                    break;
                case ADDON:
                    addonsGuids = selectedItems;
                    break;
                case OPTIONAL:
                    optionalsGuids = selectedItems;
                    break;
            }
        }

        if (onAddonsChangedListener != null) {
            onAddonsChangedListener.onAddonsChanged(new ArrayList<>(modifierGuids),
                    new ArrayList<>(addonsGuids), new ArrayList<>(optionalsGuids));
        }
    }

    private static Map<String, List<SelectedModifierExModel>> sortModifiers(List<SelectedModifierExModel> modifiers){

        HashMap<String, List<SelectedModifierExModel>> groupedItems = new HashMap<>();
        for (SelectedModifierExModel item : modifiers){
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
                groupedItems.put(key, new ArrayList<SelectedModifierExModel>());
            }
            groupedItems.get(key).add(item);
        }

        return sortByComparator(groupedItems);
    }

    private static Map<String, List<SelectedModifierExModel>> sortByComparator(Map<String, List<SelectedModifierExModel>> unsortMap) {

        // Convert Map to List
        List<Map.Entry<String, List<SelectedModifierExModel>>> list =
                new LinkedList<>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, List<SelectedModifierExModel>>>() {
            public int compare(Map.Entry<String, List<SelectedModifierExModel>> o1,
                               Map.Entry<String, List<SelectedModifierExModel>> o2) {
                SelectedModifierExModel m1 = o1.getValue().get(0);
                SelectedModifierExModel m2 = o2.getValue().get(0);
                int dif = m1.type.compareTo(m2.type);
                if(dif != 0) return dif;
                if(m1.getGroup() == null || m2.getGroup() == null)
                    return 0;
                return m1.getGroup().orderNum - m2.getGroup().orderNum;
            }
        });

        // Convert sorted map back to a Map
        Map<String, List<SelectedModifierExModel>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<SelectedModifierExModel>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }



    protected void fillViewWithContainers(Map<String, List<SelectedModifierExModel>> groupedItems){
        if (holder.getChildCount() > 0){
            holder.removeAllViews();
        }

        int totalWidth = 0;
        for (List<SelectedModifierExModel> itemList : groupedItems.values()){
            ModifierContainerView child = ModifierContainerView_.build(getActivity());
            int width = calcContainerWidth(getActivity(), itemList.size());
            totalWidth += width;
            ViewGroup.LayoutParams layoutParams = new LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
            child.setLayoutParams(layoutParams);

            child.setList(itemList);
            if (!itemList.isEmpty()){
                child.setGroup(itemList.get(0).getGroup());
            }

            holder.addView(child);
        }

        resizePerentFragment(totalWidth + 20);
    }

    private void resizePerentFragment(int width) {
        DialogFragment parent;
        if (getParentFragment() instanceof DialogFragment){
            parent = (DialogFragment) getParentFragment();
        }else {
            return;
        }

        int min = getParentFragment().getResources().getDimensionPixelOffset(R.dimen.modify_dialog_min_width);
        int max = getParentFragment().getResources().getDimensionPixelOffset(R.dimen.modify_dialog_max_width);

        width = Math.max(min, width);
        width = Math.min(max, width);

        Window window = parent.getDialog().getWindow();
        if(window!=null) {
            window.setLayout(
                    width,
                    window.getAttributes().height);
        }
    }

    private static int calcContainerWidth(Context context, int addonCnt) {
        if (addonCnt == 0) {
            return 0;
        }
        int columnCnt = (addonCnt + 2) / context.getResources().getInteger(R.integer.modify_container_row_count);
        int btnWidth = context.getResources().getDimensionPixelOffset(R.dimen.modify_button_width);
        int padding = columnCnt <= 1 ? 4 : context.getResources().getDimensionPixelOffset(R.dimen.modify_container_padding);
        int margin = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_margin);
        int space = context.getResources().getDimensionPixelOffset(R.dimen.modify_container_space);
        return columnCnt * btnWidth + (columnCnt - 1) * space + 2 * padding + 2 * margin;
    }

    public interface OnAddonsChangedListener {
        void onAddonsChanged(ArrayList<String> modifierGuid,
                             ArrayList<String> addonsGuid,
                             ArrayList<String> optionalsGuid);

        void onModifiersCountInsufficient(ModifierGroupModel group);
    }
}
