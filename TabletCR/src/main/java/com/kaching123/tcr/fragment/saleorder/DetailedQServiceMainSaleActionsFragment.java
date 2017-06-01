package com.kaching123.tcr.fragment.saleorder;

import android.support.v4.app.Fragment;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EFragment;

/**
 * Created by mboychenko on 5/29/2017.
 */
@EFragment(R.layout.detailed_qservice_main_sale_actions_fragment)
public class DetailedQServiceMainSaleActionsFragment  extends Fragment {

    public static interface IOrderRegisterActionListener {
        void onPay();

        void onHold();

        void onVoid();

        void onCustomer();
    }
}
