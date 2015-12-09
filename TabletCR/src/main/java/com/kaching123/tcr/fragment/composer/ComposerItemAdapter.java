package com.kaching123.tcr.fragment.composer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ItemExModel;

import java.math.BigDecimal;

/**
 * Created by idyuzheva on 04.12.2015.
 */
public class ComposerItemAdapter extends ObjectsCursorAdapter<ComposerExModel> {

    public ComposerItemAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return ComposerItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, final ComposerExModel item) {
        ComposerItemView itemView = (ComposerItemView_) convertView;
        ItemExModel model = item.getChildItem();
        itemView.bind(item.tracked,
                item.restricted,
                model == null ? item.guid : model.description,
                model == null ? item.guid : model.productCode,
                item.qty,
                model == null ? BigDecimal.ZERO : model.availableQty,
                item.getChildItem().cost,
                model == null ? item.guid : model.shortCut,
                item.getChildItem().cost.multiply(item.qty),
                model.priceType);
        return convertView;
    }
}
