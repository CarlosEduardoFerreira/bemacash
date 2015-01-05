package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 17.01.14.
*/
public class TopItemModel implements Comparable<TopItemModel> {
    public final String guid;
    public final String description;
    public BigDecimal quantity = BigDecimal.ZERO;

    public TopItemModel(String guid, String description) {
        this.guid = guid;
        this.description = description;
    }

    @Override
    public int compareTo(TopItemModel another) {
        return this.quantity.compareTo(another.quantity) * -1;
    }
}
