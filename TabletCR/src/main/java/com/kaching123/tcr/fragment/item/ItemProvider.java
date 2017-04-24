package com.kaching123.tcr.fragment.item;

import com.kaching123.tcr.activity.BaseItemActivity2.ItemQtyInfo;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;

/**
 * Created by vkompaniets on 21.07.2016.
 */
public interface ItemProvider {
    boolean isCreate();
    boolean isDuplicate();
    String getPrdeDuplicateModelGuid();
    ItemExModel getModel();
    ItemQtyInfo getQtyInfo();
    void setParentItem(ItemExModel parent);
    void setParentMatrixItem(ItemMatrixModel parentMatrixItem);


    void onStockTypeChanged();
    void onPriceTypeChanged();
}
