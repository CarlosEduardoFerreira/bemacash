package com.kaching123.tcr.model;

import android.text.TextUtils;

import java.math.BigDecimal;

import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by alboyko 02.12.2015
 */
public class ModifierExModel extends ModifierModel {

    protected ItemExModel childItem;
    protected ModifierGroupModel group;

    public ModifierExModel() {
        super();
    }

    public ModifierExModel(String modifierGuid,
                           String itemGuid,
                           ModifierType type,
                           String title,
                           BigDecimal cost,
                           String childItemGuid,
                           BigDecimal childItemQty,
                           String modifierGroupGuid,
                           ModifierGroupModel group,
                           ItemExModel childItem,
                           boolean autoApply,
                           int orderNum) {
        super(modifierGuid, itemGuid, type, title, cost, childItemGuid, childItemQty, modifierGroupGuid, autoApply, orderNum);
        this.group = group;
        this.childItem = childItem;
    }

    public ItemExModel getItem() {
        return childItem;
    }

    public void setItem(ItemExModel item) {
        this.childItem = item;
    }

    public ModifierGroupModel getGroup() {
        return group;
    }

    public BigDecimal getCost(){
        if (type == ModifierType.OPTIONAL)
            return BigDecimal.ZERO;

        if (getItem() != null) {
            return getSubTotal(childItemQty, getItem().price);
        }

        return cost;
    }

   public String getTitle(){
       if (!TextUtils.isEmpty(title))
           return title;

       if (childItem != null)
           return String.format("[%s]", childItem.description);

       return null;
   }

   public boolean isDefaultWithinGroupOrItem(ItemModel hostItem) {
       return false;
   }
}
