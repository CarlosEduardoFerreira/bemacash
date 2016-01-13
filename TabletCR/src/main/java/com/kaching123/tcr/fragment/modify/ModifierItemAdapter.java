package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.util.UnitUtil;

/**
 * Created by alboyko on 01.12.2015.
 */
public class ModifierItemAdapter extends ObjectsCursorAdapter<ModifierExModel> {

    private ItemModel hostItem;

    public ModifierItemAdapter(Context context) {
        super(context);
    }

    public void setHostItem(ItemModel hostItem) {
        this.hostItem = hostItem;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return ModifierItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, final ModifierExModel item) {
        ModifierItemView itemView = (ModifierItemView_) convertView;
        ItemExModel itemModel = item.getItem();
        String title;
        String price;
        String qty;
        String productCode;
        String totalPrice;
        String priceLabel;

        boolean fromInventory = item.childItemGuid != null;
        if (itemModel == null) {
            title = item.title;
            totalPrice = UiHelper.valueOf(item.cost);
            String NA = getContext().getString(R.string.order_transactions_status_na);
            qty = NA;
            productCode = NA;
            price = NA;
            priceLabel = NA;
        } else {
            title = item.title + " [" + itemModel.description + "]";
            price = UiHelper.valueOf(itemModel.price);

            if (UnitUtil.isNotUnitPriceType(itemModel.priceType)) {
                qty = UiHelper.brandQtyIntFormat(item.childItemQty);
            } else {
                qty = UiHelper.brandQtyFormat(item.childItemQty);
            }

            productCode = itemModel.productCode;
            totalPrice = UiHelper.valueOf(item.getCost());
            priceLabel = itemModel.shortCut;
        }

        itemView.bind(fromInventory,
                title,
                productCode,
                qty,
                price,
                priceLabel,
                totalPrice,
                item.isDefaultItem());
        return convertView;
    }
}