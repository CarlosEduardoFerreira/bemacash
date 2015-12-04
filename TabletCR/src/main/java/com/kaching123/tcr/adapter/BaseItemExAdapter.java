package com.kaching123.tcr.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
/**
 * Created by irikhmayer on 14.05.2015.
 */
public abstract class BaseItemExAdapter extends ObjectsArrayAdapter<ItemExModel> implements Filterable {

    //    private static final Uri ITEMS_URI = ShopProvider.contentUri(ItemExtView.URI_CONTENT);
    private static final Uri ITEMS_URI = ShopProvider.contentUriGroupBy(ShopStore.ItemExtView.URI_CONTENT, ShopSchema2.ItemExtView2.ItemTable.GUID);

    public BaseItemExAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.customer_dropdown_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.nameLabel = (TextView) view.findViewById(R.id.name_label);
        holder.emailLabel = (TextView) view.findViewById(R.id.email_label);
        view.setTag(holder);
        return view;
    }

    @Override
    protected View bindView(View convertView, int position, ItemExModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.nameLabel.setText(item.description);
        holder.emailLabel.setText(item.eanCode);
        return convertView;
    }

    protected String whereInColumn() {
        return null;
    }

    protected Collection<String> whereInCollection() {
        return null;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((ItemExModel) resultValue).description;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    Query base = ProviderAction.query(ITEMS_URI)
                            .where(getSelection(), getSelectionArgs(constraint))
                            .projection(ItemExFunction.PROJECTION)
                            .orderBy(ShopSchema2.ItemExtView2.ItemTable.DESCRIPTION);
                    if (!TextUtils.isEmpty(whereInColumn())) {
                        base.where(whereInColumn() + " NOT IN (" + Joiner.on(",").join(Collections.nCopies(whereInCollection().size(), "?")) + ")", whereInCollection().toArray());
                    }
                    FluentIterable<ItemExModel> customerModels = base.perform(getContext()).toFluentIterable(new ListConverterFunction<ItemExModel>() {
                                @Override
                                public ItemExModel apply(Cursor cursor) {
                                    return new ItemExFunction().apply(cursor);
                                }
                            });
                    results.count = customerModels.size();
                    results.values = customerModels;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {

                    FluentIterable<ItemExModel> it = (FluentIterable<ItemExModel>) results.values;

                    self().publishResults(it);
                    changeCursor(it.toImmutableList());
                } else {
                    self().publishResults(null);
                }
            }
        };
    }

    private String getSelection() {
        String selection = "("+ ShopSchema2.ItemExtView2.ItemTable.DESCRIPTION + " LIKE ? OR "
                + ShopSchema2.ItemExtView2.ItemTable.PRODUCT_CODE + " LIKE ?)"
                + " AND " + ShopSchema2.ItemExtView2.ItemTable.IS_DELETED + " = 0";
        String customSelection = getCustomSelection();
        if (customSelection != null && customSelection.length() > 0) {
            selection = selection + " AND " + customSelection;
        }
        return selection;
    }

    private String[] getSelectionArgs(CharSequence constraint) {
        String[] selectionArgs = {constraint + "%", constraint + "%"};
        String[] customSelectionArgs = getCustomSelectionArgs();
        if (customSelectionArgs != null && customSelectionArgs.length > 0) {
            String[] result = Arrays.copyOf(selectionArgs, selectionArgs.length + customSelectionArgs.length);
            System.arraycopy(customSelectionArgs, 0, result, selectionArgs.length, customSelectionArgs.length);
            return result;
        }
        return selectionArgs;

    }

    protected String getCustomSelection() {
        return null;
    }

    protected String[] getCustomSelectionArgs() {
        return null;
    }

    protected BaseItemExAdapter self() {
        return this;
    }

    protected abstract void publishResults(FluentIterable<ItemExModel> cursor);

    private class ViewHolder {
        TextView nameLabel;
        TextView emailLabel;
    }

}