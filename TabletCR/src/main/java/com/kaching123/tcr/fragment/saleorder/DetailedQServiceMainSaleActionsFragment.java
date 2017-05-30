package com.kaching123.tcr.fragment.saleorder;

/**
 * Created by mboychenko on 5/29/2017.
 */

public class DetailedQServiceMainSaleActionsFragment {

    public static interface IOrderRegisterActionListener {
        void onPay();

        void onHold();

        void onVoid();

        void onCustomer();
    }
}
