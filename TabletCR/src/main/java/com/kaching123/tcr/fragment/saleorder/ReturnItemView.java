package com.kaching123.tcr.fragment.saleorder;

import android.content.Context;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import com.kaching123.tcr.R;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 08.04.14.
 */
@EViewGroup(R.layout.saleorder_items_item_view)
public class ReturnItemView extends ItemView {

    public ReturnItemView(Context context) {
        super(context);
    }

    @Override
    protected void showPrice(TextView label, BigDecimal value) {
        super.showPrice(label, value.negate());
    }
}
