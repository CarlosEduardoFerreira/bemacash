package com.kaching123.tcr.fragment.composer;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.PriceType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by irikhmayer on 05.05.2015.
 */
@EViewGroup(R.layout.composer_list_item_view)
public class ComposerItemView extends FrameLayout {

    @ViewById
    protected CheckBox unitTrack;

    @ViewById
    protected CheckBox unitFree;

    @ViewById
    protected TextView unitName;

    @ViewById
    protected TextView unitQty;

    @ViewById
    protected TextView code;

    @ViewById
    protected TextView cost;

    @ViewById
    protected TextView costItem;

    @ViewById
    protected TextView unitQtyLabel;

    @ViewById
    protected TextView itemQty;

    @ViewById
    protected TextView itemQtyLabel;

    public ComposerItemView(Context context) {
        super(context);
    }

    @AfterViews
    protected void init() {
    }

    public void bind(boolean track,
                     boolean free,
                     String status,
                     String code,
                     BigDecimal qty,
                     BigDecimal itemQtyValue,
                     BigDecimal pricePerItem,
                     String label,
                     BigDecimal totalcost,
                     PriceType type) {
        this.unitTrack.setChecked(track);
        this.unitFree.setChecked(free);
        this.unitName.setText(status);
        if (type == PriceType.UNIT_PRICE) {
            showBrandQty(unitQty, qty);
            showBrandQty(itemQty, itemQtyValue);
        } else {
            showBrandQtyInteger(unitQty, qty);
            showBrandQtyInteger(itemQty, itemQtyValue);
        }
        showPrice(costItem, pricePerItem);
        showPrice(cost, totalcost);
        this.code.setText(code);
        this.unitQtyLabel.setText(label);
        this.itemQtyLabel.setText(label);

    }

}