package com.kaching123.tcr.component;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jess.ui.TwoWayGridView;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand.SelectedModifierExModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup(R.layout.modify_container_bra)
public class ModifierContainerView extends FrameLayout {

    @ViewById
    protected TwoWayGridView buttonGrid;

    @ViewById
    protected TextView containerTitle;

    private ButtonsAdapter adapter;

    private ModifierGroupModel modifierGroup;

    public ModifierContainerView(Context context) {
        super(context);
    }

    @AfterViews
    protected void init() {
        Logger.e("BaseAddonContainerView: init()");
        adapter = new ButtonsAdapter(getContext());
        buttonGrid.setAdapter(adapter);
    }

    public void setGroup(ModifierGroupModel group){
        this.modifierGroup = group;
    }

    public ModifierGroupModel getGroup() {
        return modifierGroup;
    }

    public void setList(List<SelectedModifierExModel> modifiers) {
        cleanSelection();
        ModifierExModel firstItem = modifiers.get(0);
        setContainerTitle(firstItem);
        for (SelectedModifierExModel modifier : modifiers){
            if (modifier.isSelected){
                adapter.selectedItems.add(modifier.getGuid());
            }
        }
        adapter.changeCursor(modifiers);
    }

    public ModifierType getModifierType(){
        if (adapter.getCount() == 0){
            return null;
        }

        return adapter.getItem(0).type;
    }

    public Set<String> getSelectedItems() {
        return adapter.selectedItems;
    }

    private void setContainerTitle(ModifierExModel firstItem) {
        if (firstItem.getGroup() != null){
            this.containerTitle.setText(firstItem.getGroup().title);
            return;
        }

        int title = 0;
        switch (firstItem.type){
            case MODIFIER:
                title = R.string.dlg_section_modifier;
                break;
            case ADDON:
                title = R.string.dlg_section_addon;
                break;
            case OPTIONAL:
                title = R.string.dlg_section_optional;
                break;
        }
        this.containerTitle.setText(title);
    }

    public void cleanSelection() {
        adapter.setSelectedItems(null);
    }

    public class ButtonsAdapter extends ObjectsCursorAdapter<SelectedModifierExModel> {

        private HashSet<String> selectedItems = new HashSet<>();

        public ButtonsAdapter(Context context) {
            super(context);
        }

        @Override
        public void changeCursor(List<SelectedModifierExModel> list) {
            super.changeCursor(list);
            onChangeSelections();
        }

        private void onChangeSelections() {
            if (onChangeListener != null) {
                onChangeListener.onChanged();
            }
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            ModifyButton v = ModifyButton_.build(getContext());
            v.setOnClickListener(changeSelectedListener);
            return v;
        }

        @Override
        protected View bindView(View convertView, int position, SelectedModifierExModel item) {
            ModifyButton view = (ModifyButton) convertView;
            view.bind(item.getGuid(), item.getTitle(), item.getCost(), selectedItems.contains(item.getGuid()));
            return view;
        }

        private void setSelectedItems(List<String> selectedItemGuids) {
            selectedItems.clear();
            if (selectedItemGuids != null) {
                selectedItems.addAll(selectedItemGuids);
            }
            notifyDataSetChanged();
        }

        private OnClickListener changeSelectedListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                ModifyButton v = (ModifyButton) view;
                String guid = v.getGuid();
                handleMultipleMode(guid);
                notifyDataSetChanged();
                onChangeSelections();
            }

            private void handleMultipleMode(String guid) {
                if (!selectedItems.remove(guid)) {
                    if (modifierGroup != null && modifierGroup.conditionValue != 0 && selectedItems.size() >= modifierGroup.conditionValue){
                        Toast.makeText(getContext(), getContext().getString(R.string.modifiers_limit_exceed_msg, modifierGroup.conditionValue), Toast.LENGTH_SHORT).show();
                    }else{
                        selectedItems.add(guid);
                    }
                }
            }
        };
    }

    private OnChangeListener onChangeListener;

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        void onChanged();
    }

}
