package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;

import org.androidannotations.annotations.EViewGroup;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ModifierModel;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup (R.layout.modify_container)
public class ModifyContainerView extends BaseAddonContainerView<ModifierModel> {

    public ModifyContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ButtonsAdapter<ModifierModel> createAdapter() {
        return new ButtonsAdapter<ModifierModel>(getContext(), true){

            @Override
            protected String getTitle(ModifierModel item) {
                return item.title;
            }

            @Override
            protected BigDecimal getCost(ModifierModel item) {
                return item.cost;
            }

            @Override
            protected String getGuid(ModifierModel item) {
                return item.modifierGuid;
            }
        };
    }

    public String getSelectedModifier(){
        Set<String> selected = getSelectedItems();
        if(selected.isEmpty())
            return null;
        return selected.iterator().next();
    }
}
