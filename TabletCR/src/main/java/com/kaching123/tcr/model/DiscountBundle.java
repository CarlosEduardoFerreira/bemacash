package com.kaching123.tcr.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vkompaniets on 24.08.2016.
 */
public class DiscountBundle implements Serializable {

    public String id;
    public List<MultipleDiscountModel> bundleItems;

    public DiscountBundle(String id, List<MultipleDiscountModel> bundleItems) {
        this.id = id;
        this.bundleItems = bundleItems;
    }
}
