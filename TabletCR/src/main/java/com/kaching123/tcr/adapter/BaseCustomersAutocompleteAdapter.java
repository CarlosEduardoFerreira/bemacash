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
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;

/**
 * Created by pkabakov on 21.02.14.
 */
public abstract class BaseCustomersAutocompleteAdapter extends ObjectsArrayAdapter<CustomerModel> implements Filterable {

    private static final Uri CUSTOMERS_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    public BaseCustomersAutocompleteAdapter(Context context) {
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
    protected View bindView(View convertView, int position, CustomerModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.nameLabel.setText(item.getFullName());
        holder.emailLabel.setText(item.email);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((CustomerModel)resultValue).getFullName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                FluentIterable<CustomerModel> customerModels = ProviderAction.query(CUSTOMERS_URI)
                        .where(CustomerTable.FISRT_NAME + " LIKE ? OR " + CustomerTable.LAST_NAME + " LIKE ? OR " + CustomerTable.EMAIL + " LIKE ?", constraint + "%", constraint + "%", constraint + "%")
                        .orderBy(CustomerTable.LAST_NAME)
                        .perform(getContext()).toFluentIterable(new ListConverterFunction<CustomerModel>() {
                            @Override
                            public CustomerModel apply(Cursor cursor) {
                                return new CustomerModel(cursor);
                            }
                        });

                results.count = customerModels.size();
                results.values = customerModels;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                FluentIterable<CustomerModel> it = (FluentIterable<CustomerModel>) results.values;

                BaseCustomersAutocompleteAdapter.this.publishResults(it);

                changeCursor(it.toImmutableList());
            }
        };
    }

    protected abstract void publishResults(FluentIterable<CustomerModel> cursor);

    private class ViewHolder {
        TextView nameLabel;
        TextView emailLabel;
    }

}
