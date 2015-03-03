package com.kaching123.tcr.fragment.inventory;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.PriceType;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by vkompaniets on 27.11.13.
 */
@EViewGroup(R.layout.inventory_item_view)
public class ItemView extends FrameLayout {

    @ViewById
    protected TextView description;

    @ViewById
    protected TextView ean;

    @ViewById
    protected TextView cost;

    @ViewById
    protected TextView price;

    @ViewById
    protected TextView qty;

    @ViewById
    protected TextView units;

    @ViewById
    protected TextView totalCost;

    private int position;

    public ItemView(Context context) {
        super(context);
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void showEanOrProductCode(String productCode, String eanCode){
        if (TextUtils.isEmpty(eanCode)) {
            if (TextUtils.isEmpty(productCode)) {
                this.ean.setVisibility(View.GONE);
            } else {
                this.ean.setVisibility(View.VISIBLE);
                this.ean.setText(productCode);
            }
        } else {
            this.ean.setVisibility(View.VISIBLE);
            this.ean.setText(eanCode);
        }
    }

    public void bind(int position, Drawable pencilDrawable, Drawable pencilTransparent, String description, String eanCode, String productCode, BigDecimal price, PriceType priceType, BigDecimal qty, BigDecimal cost, String unitsLabel) {
        this.position = position;
        this.description.setText(description);

        showEanOrProductCode(productCode, eanCode);

        showPrice(this.cost, cost);
        showPrice(this.price, price);

        this.price.setCompoundDrawables(null, null, priceType == PriceType.OPEN ? pencilDrawable : pencilTransparent, null);
        if (unitsLabel == null) {
            showInteger(this.qty, qty);
        } else {
            showQuantity(this.qty, qty);
            this.units.setText(unitsLabel);
        }

        showPrice(this.totalCost, getSubTotal(qty, cost));
    }


}
