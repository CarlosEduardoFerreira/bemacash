package com.kaching123.tcr.fragment.item;

import com.kaching123.tcr.activity.BaseItemActivity2.ItemQtyInfo;
import com.kaching123.tcr.model.ItemExModel;

/**
 * Created by vkompaniets on 21.07.2016.
 */
public interface ItemProvider {
    boolean isCreate();
    ItemExModel getModel();
    ItemQtyInfo getQtyInfo();

    void onStockTypeChanged();
    void onPriceTypeChanged();
}
