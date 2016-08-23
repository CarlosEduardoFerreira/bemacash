package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ModifierModel;

import org.androidannotations.annotations.EViewGroup;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup (R.layout.addon_nooption_container)
public class AddonsContainerView extends BaseAddonContainerView<ModifierModel> {

    public AddonsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ButtonsAdapter<ModifierModel> createAdapter() {
        return new ButtonsAdapter<ModifierModel>(getContext(), false){

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

    public void setSelectedAddonsGuids (ArrayList<String> selectedAddonsGuids){
        setSelectedItems(selectedAddonsGuids);
    }
    public void setColumnNums(int num) {
        buttonGrid.setNumColumns(num);
    }
}
