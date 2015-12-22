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

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup (R.layout.addon_nooption_container)
public class AddonsContainerView extends BaseAddonContainerView<ModifierExModel> {

    public AddonsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ButtonsAdapter<ModifierExModel> createAdapter() {
        return new ButtonsAdapter<ModifierExModel>(getContext(), false){

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

    public void setSelectedAddonsGuids (ArrayList<String> selectedAddonsGuids){
        setSelectedItems(selectedAddonsGuids);
    }
    public void setColumnNums(int num) {
        buttonGrid.setNumColumns(num);
    }
}
