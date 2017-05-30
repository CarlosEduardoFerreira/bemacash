package com.kaching123.tcr.fragment.saleorder;

import java.math.BigDecimal;

/**
 * Created by mboychenko on 5/29/2017.
 */

public class DetaildeQServiceTotalCostFragment {

    public static interface IOrderPricingListener {

        void onDiscount(BigDecimal itemsSubTotal);

        void onTax();
    }
}
