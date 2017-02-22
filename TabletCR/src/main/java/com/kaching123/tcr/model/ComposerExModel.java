package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by idyuzheva on 04.12.2015.
 */
public class ComposerExModel extends ComposerModel {

    protected final ItemExModel childItem;
    protected final ItemExModel hostItem;

    public ComposerExModel() {
        super();
        this.childItem = null;
        this.hostItem = null;
    }

    public ItemExModel getChildItem() {
        return childItem;
    }

    public ItemExModel getHostItem() {
        return hostItem;
    }

    public ComposerExModel(String guid,
                           String itemHostId,
                           String itemChildId,
                           BigDecimal qty,
                           boolean storeTrackingEnabled,
                           boolean freeOfChargeComposer,
                           ItemExModel childItem,
                           ItemExModel hostItem) {
        super(guid, itemHostId, itemChildId, qty, storeTrackingEnabled, freeOfChargeComposer, null);
        this.hostItem = hostItem;
        this.childItem = childItem;
    }
}