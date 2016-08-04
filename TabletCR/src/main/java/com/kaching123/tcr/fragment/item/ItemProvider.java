package com.kaching123.tcr.fragment.item;

import com.kaching123.tcr.model.ItemExModel;

/**
 * Created by vkompaniets on 21.07.2016.
 */
public interface ItemProvider {
    ItemExModel getModel();
    ItemExModel getModel2();
    boolean isCreate();
}
