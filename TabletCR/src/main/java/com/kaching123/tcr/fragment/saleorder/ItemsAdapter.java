package com.kaching123.tcr.fragment.saleorder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.googlecode.androidannotations.annotations.EBean;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.util.DrawableUtil;

import java.math.BigDecimal;

@EBean
public class ItemsAdapter extends ObjectsCursorAdapter<SaleOrderItemViewModel> {

    public static final int DEFAULT_ANIMATION_TIME = 1000;

    private ItemView.OnItemRemoveClick itemRemoveListener;

    private Drawable pencilDrawable;
    private Drawable pencilTransparent;
    private HighlightedColumn highlightedColumn;

    public ItemsAdapter(Context context) {
        super(context);
        pencilDrawable = context.getResources().getDrawable(R.drawable.pencil);
        DrawableUtil.boundDrawable(pencilDrawable);
        pencilTransparent = context.getResources().getDrawable(R.drawable.square_opacity);
        DrawableUtil.boundDrawable(pencilTransparent);
    }

    public void setItemRemoveListener(ItemView.OnItemRemoveClick itemRemoveListener) {
        this.itemRemoveListener = itemRemoveListener;
    }

    public boolean isPcsUnit(int pos) {
        return getItem(pos).isPcsUnit;
    }

    public String getSaleItemGuid(int pos) {
        return getItem(pos).itemModel.saleItemGuid;
    }

    public BigDecimal getItemQty(int pos) {
        return getItem(pos).itemModel.qty;
    }

    public BigDecimal getItemPrice(int pos) {
        return getItem(pos).fullPrice;
    }

    public BigDecimal getItemDiscount(int pos) {
        return getItem(pos).itemModel.discount;
    }

    public DiscountType getItemDiscountType(int pos) {
        return getItem(pos).itemModel.discountType;

    }

    public String getSaleItemNotes(int pos) {
        return getItem(pos).itemModel.notes;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return instantiateView().setListener(itemRemoveListener);
    }

    protected ItemView instantiateView() {
        return ItemView_.build(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(position, parent);
        }
        ((SwipeListView) parent).recycle(convertView, position);
        return bindView(convertView, position, getItem(position));
    }

    private static void showSerialCodes(String codes, ItemView itemView){
        if (!TextUtils.isEmpty(codes)) {
            itemView.itemSerialCodes.setVisibility(View.VISIBLE);
            itemView.itemSerialCodesTitle.setVisibility(View.VISIBLE);
            itemView.itemSerialCodes.setText(codes);
        } else {
            itemView.itemSerialCodes.setVisibility(View.GONE);
            itemView.itemSerialCodesTitle.setVisibility(View.GONE);
        }
    }

    @Override
    protected View bindView(View convertView, int position, SaleOrderItemViewModel item) {
        ItemView itemView = (ItemView) convertView;

        itemView.bind(
                position,
                pencilDrawable,
                pencilTransparent,
                item.description,
                item.eanCode,
                item.productCode,
                item.getSerialCodesString(),
                item.itemModel.qty,
                item.isPcsUnit ? null : item.unitsLabel,
                item.fullPrice,
                item.itemModel.priceType,
                item.itemModel.discountable,
                item.itemModel.discount,
                item.itemModel.discountType,
                item.subTitle,
                item.itemModel.notes,
                item.itemModel.hasNotes
        );
        itemView.itemQtyBlock.setActivated(false);
        itemView.itemUnitPrice.setActivated(false);
        itemView.discountBlock.setActivated(false);

        showSerialCodes(item.getSerialCodesString(), itemView);

        if (highlightedColumn != null && highlightedColumn.saleItemGuid.equals(item.itemModel.saleItemGuid)) {
            if (highlightedColumn.startTime == 0) {
                highlightedColumn.startTime = System.currentTimeMillis();
                //Logger.d("Animation: start animation %d", highlightedColumn.startTime);
            }
            long dif = (System.currentTimeMillis() - highlightedColumn.startTime);
            //Logger.d("Animation: dif: %d", dif);
            if (dif < DEFAULT_ANIMATION_TIME) {
                //  Logger.d("Animation: set bold");
                if (highlightedColumn.type == HighlightedColumn.Type.QTY) {
                    itemView.itemQtyBlock.setActivated(true);
                } else if (highlightedColumn.type == HighlightedColumn.Type.PRICE) {
                    itemView.itemUnitPrice.setActivated(true);
                } else {
                    itemView.discountBlock.setActivated(true);
                }
            } else {
                //Logger.d("Animation: set null");
                highlightedColumn = null;
            }
        }
        Logger.d("hello " + item.tmpUnit.size());
        return convertView;
    }

    public void highlightedColumn(String saleItemGuid, HighlightedColumn.Type type) {
        highlightedColumn = new HighlightedColumn(saleItemGuid, type);
        notifyDataSetChanged();
    }

    public static class HighlightedColumn {
        public static enum Type {QTY, PRICE, DISCOUNT}

        public String saleItemGuid;
        public long startTime = 0;
        public Type type;

        private HighlightedColumn(String saleItemGuid, Type type) {
            this.saleItemGuid = saleItemGuid;
            this.type = type;
        }

        boolean isStarted() {
            return startTime != 0;
        }

    }

}
