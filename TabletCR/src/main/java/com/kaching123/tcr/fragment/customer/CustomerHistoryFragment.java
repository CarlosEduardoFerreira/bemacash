package com.kaching123.tcr.fragment.customer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SaleItemsHistoryActivity;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.SaleOrderTipsViewModel;
import com.kaching123.tcr.model.converter.SaleOrderTipsViewFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTipsQuery;
import com.kaching123.tcr.util.DateUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 27.06.2016.
 */
@EFragment(R.layout.customer_history_fragment)
public class CustomerHistoryFragment extends CustomerBaseFragment {

    private static final Uri URI_ORDERS = ShopProvider.getContentWithLimitUri(SaleOrderTipsQuery.URI_CONTENT, 20);

    @ViewById protected ListView list;

    private OrderAdapter adapter;

    @Override
    @AfterViews
    protected void init(){
        super.init();
    }

    @Override
    protected void setViews() {
        super.setViews();
        adapter = new OrderAdapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String orderGuid = adapter.getItem(position).guid;
                SaleItemsHistoryActivity.start(getActivity(), orderGuid);
            }
        });
    }

    @Override
    protected void setCustomer() {
        super.setCustomer();
        getLoaderManager().restartLoader(0, null, new OrderLoader());
    }

    private class OrderLoader implements LoaderCallbacks<List<SaleOrderTipsViewModel>>{

        @Override
        public Loader<List<SaleOrderTipsViewModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(URI_ORDERS)
                    .where(CustomerTable.GUID + " = ?", getCustomer().guid)
                    .orderBy(SaleOrderTable.CREATE_TIME + " DESC")
                    .transform(new SaleOrderTipsViewFunction())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<SaleOrderTipsViewModel>> loader, List<SaleOrderTipsViewModel> data) {
            adapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<List<SaleOrderTipsViewModel>> loader) {
            adapter.changeCursor(null);
        }
    }

    private class OrderAdapter extends ObjectsCursorAdapter<SaleOrderTipsViewModel> {

        public OrderAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.sale_order_list_item, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, SaleOrderTipsViewModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            BigDecimal total = item.tmpTotalPrice == null ? BigDecimal.ZERO : item.tmpTotalPrice;
            BigDecimal discount = item.tmpTotalDiscount == null ? BigDecimal.ZERO : item.tmpTotalDiscount;
            BigDecimal tax = item.tmpTotalTax == null ? BigDecimal.ZERO : item.tmpTotalTax;
            //price - discount + tax = total
            BigDecimal price = total.add(discount).subtract(tax);

            holder.date.setText(DateUtils.formatFull(item.createTime));
            holder.register.setText(item.registerTitle);
            showPrice(holder.price, total);
            showPrice(holder.discount, discount);
            showPrice(holder.tax, tax);
            showPrice(holder.total, total.add(tax).subtract(discount));

            return convertView;
        }

        class ViewHolder {
            TextView date;
            TextView register;
            TextView price;
            TextView discount;
            TextView tax;
            TextView total;

            public ViewHolder(View view) {
                date = (TextView) view.findViewById(R.id.date);
                register = (TextView) view.findViewById(R.id.register);
                price = (TextView) view.findViewById(R.id.price);
                discount = (TextView) view.findViewById(R.id.discount);
                tax = (TextView) view.findViewById(R.id.tax);
                total = (TextView) view.findViewById(R.id.total);
            }
        }
    }

}