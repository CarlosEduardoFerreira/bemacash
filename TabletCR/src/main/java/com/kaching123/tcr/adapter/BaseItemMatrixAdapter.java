package com.kaching123.tcr.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;


/**
 * Created by aakimov on 20.05.2015.
 */
public abstract class BaseItemMatrixAdapter extends ObjectsArrayAdapter<ItemMatrixModel> implements Filterable {

    private static final Uri ITEM_MATRIX_URI = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);

    public BaseItemMatrixAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.prepaid_spinner_dropdown_carrier_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.nameLabel = (TextView) view.findViewById(android.R.id.text1);
        view.setTag(holder);
        return view;
    }

    @Override
    protected View bindView(View convertView, int position, ItemMatrixModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.nameLabel.setText(item.name);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((ItemMatrixModel) resultValue).name;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    FluentIterable<ItemMatrixModel> models = ProviderAction.query(ITEM_MATRIX_URI)
                            .where(getSelection(), getSelectionArgs(constraint))
                            .perform(getContext()).toFluentIterable(new ListConverterFunction<ItemMatrixModel>() {
                                @Override
                                public ItemMatrixModel apply(Cursor cursor) {
                                    return new ItemMatrixFunction().apply(cursor);
                                }
                            });
                    results.count = models.size();
                    results.values = models;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    FluentIterable<ItemMatrixModel> it = (FluentIterable<ItemMatrixModel>) results.values;
                    changeCursor(it.toImmutableList());
                    BaseItemMatrixAdapter.this.publishResults(it);
                } else {
                    BaseItemMatrixAdapter.this.publishResults(null);
                }
            }
        };
    }

    protected abstract void publishResults(FluentIterable<ItemMatrixModel> cursor);

    protected abstract String getSelection();

    protected abstract String[] getSelectionArgs(CharSequence constraint);


    private class ViewHolder {
        TextView nameLabel;
    }

}