package com.kaching123.tcr.fragment.modify;

import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.model.ModifierGroupModel;

/**
 * Created by irikhmayer on 17.06.2015.
 */


public interface GroupCallback extends BaseCategoriesFragment.ICategoryListener {
    void onItemSelected(ModifierGroupModel item);
}
