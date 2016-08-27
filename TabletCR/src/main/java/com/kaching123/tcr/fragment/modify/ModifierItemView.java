package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


/**
 * Created by alboyko on 01.12.2015.
 */

@EViewGroup(R.layout.modifier_list_item_view)
public class ModifierItemView extends FrameLayout {

    @ViewById
    protected CheckBox unitTrack;

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
    protected ImageView drag;

    public ModifierItemView(Context context) {
        super(context);
    }

    public void bind(boolean track,
                     String status,
                     String code,
                     String qty,
                     String pricePerItem,
                     String label,
                     String totalCost,
                     boolean draggable) {
        this.unitTrack.setChecked(track);
        this.unitName.setText(status);
        this.unitQty.setText(qty);
        this.code.setText(code);
        this.costItem.setText(pricePerItem);
        this.unitQtyLabel.setText(label);
        this.cost.setText(totalCost);
        this.drag.setVisibility(draggable ? View.VISIBLE : View.INVISIBLE);
    }
}