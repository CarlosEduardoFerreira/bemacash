package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by vkompaniets on 20.11.13.
 */
public class AddonViewModel {

    public final String addonGuid;
    public final String itemGuid;
    public final String title;
    public final BigDecimal cost;

    public AddonViewModel(String addonGuid, String itemGuid, String title, BigDecimal cost) {
        this.itemGuid = itemGuid;
        this.addonGuid = addonGuid;
        this.title = title;
        this.cost = cost;
    }
}
