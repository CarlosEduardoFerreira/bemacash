package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectCursorDragAdapter;
import com.kaching123.tcr.commands.store.inventory.BatchUpdateModifierOrderCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.util.UnitUtil;

/**
 * Created by alboyko on 01.12.2015.
 */
public class ModifierItemAdapter extends ObjectCursorDragAdapter<ModifierExModel> {

    private boolean draggable;

    public ModifierItemAdapter(Context context) {
        super(context);
    }


    @Override
    protected View newView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.modifier_list_item_view, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    protected View bindView(View convertView, int position, final ModifierExModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
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
            title = itemModel.description;
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

        holder.bind(fromInventory,
                title,
                productCode,
                qty,
                price,
                priceLabel,
                totalPrice,
                draggable
        );
        return convertView;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    @Override
    public void drop(int from, int to) {
        super.drop(from, to);
        if (from == to) {
            return;
        }

        int count = getCount();
        String[] guids = new String[count];
        for (int i = 0; i < count; i++) {
            guids[i] = getItem(i).getGuid();
        }
        BatchUpdateModifierOrderCommand.start(getContext(), guids);
    }

    private class ViewHolder {
        protected CheckBox unitTrack;
        protected TextView unitName;
        protected TextView unitQty;
        protected TextView unitQtyLabel;
        protected TextView code;
        protected TextView cost;
        protected TextView costItem;
        protected ImageView drag;

        public ViewHolder(View v) {
            unitTrack = (CheckBox) v.findViewById(R.id.unit_track);
            unitName = (TextView) v.findViewById(R.id.unit_name);
            unitQty = (TextView) v.findViewById(R.id.unit_qty);
            unitQtyLabel = (TextView) v.findViewById(R.id.unit_qty_label);
            code = (TextView) v.findViewById(R.id.code);
            cost = (TextView) v.findViewById(R.id.cost);
            costItem = (TextView) v.findViewById(R.id.cost_item);
            drag = (ImageView) v.findViewById(R.id.drag);
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
}