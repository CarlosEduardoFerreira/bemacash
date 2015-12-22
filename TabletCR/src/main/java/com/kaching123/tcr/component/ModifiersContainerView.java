package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.jess.ui.TwoWayGridView;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup(R.layout.modify_container)
public class ModifiersContainerView extends BaseAddonContainerView<ModifierExModel> {

    public ModifiersContainerView(Context context) {
        super(context);
    }
    public ModifiersContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ButtonsAdapter<ModifierExModel> createAdapter() {
        return new ButtonsAdapter<ModifierExModel>(getContext(), true) {

            @Override
            protected String getTitle(ModifierExModel item) {
                return item.getTitle();
            }

            @Override
            protected BigDecimal getCost(ModifierExModel item) {
                return item.getCost();
            }

            @Override
            protected String getGuid(ModifierExModel item) {
                return item.modifierGuid;
            }
        };
    }

    public String getSelectedModifier() {
        Set<String> selected = getSelectedItems();
        if (selected.isEmpty())
            return null;
        return selected.iterator().next();
    }

    public void setSelectedModifier(String selectedModifierGuid) {
        setSelectedItems(Arrays.asList(selectedModifierGuid));
    }

    public void setSelectedModifiersGuids (ArrayList<String> selectedModifiersGuids){
        setSelectedItems(selectedModifiersGuids);
    }

    public void setColumnNums(int num) {
        buttonGrid.setNumColumns(num);
    }
}
