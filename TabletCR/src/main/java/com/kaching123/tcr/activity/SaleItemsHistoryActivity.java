package com.kaching123.tcr.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.converter.HistoryOrderItemViewModelWrapFunction;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;

/**
 * Created by vkompaniets on 11.07.2016.
 */
@EActivity(R.layout.sale_items_history_activity)
public class SaleItemsHistoryActivity extends SuperBaseActivity {
    
    @ViewById
    ListView list;
    
    @Extra
    protected String orderGuid;

    private ItemAdapter adapter;
    
    @AfterViews
    protected void init(){
        adapter = new ItemAdapter(self());
        list.setAdapter(adapter);
        getSupportLoaderManager().restartLoader(0, null, new ItemsLoader());
    }
    
    private class ItemsLoader implements LoaderCallbacks<List<SaleOrderItemViewModel>> {

        @Override
        public Loader<List<SaleOrderItemViewModel>> onCreateLoader(int id, Bundle args) {
            return HistoryOrderItemViewModelWrapFunction.createHistorySimpleLoader(self(), orderGuid);
        }

        @Override
        public void onLoadFinished(Loader<List<SaleOrderItemViewModel>> loader, List<SaleOrderItemViewModel> data) {
            adapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<List<SaleOrderItemViewModel>> loader) {
            adapter.changeCursor(null);
        }
    }

    private class ItemAdapter extends ObjectsCursorAdapter<SaleOrderItemViewModel> {

        public ItemAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(self()).inflate(R.layout.sale_item_history_list_item, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, SaleOrderItemViewModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.descrition.setText(item.description);
            showPrice(holder.price, item.finalPrice);
            showQuantity(holder.qty, item.getQty(), item.isPcsUnit);
            showPrice(holder.total, CalculationUtil.getSubTotal(item.getQty(), item.finalPrice));
            return convertView;
        }

        class ViewHolder{
            TextView descrition;
            TextView price;
            TextView qty;
            TextView total;

            public ViewHolder(View view) {
                descrition = (TextView) view.findViewById(R.id.description);
                price = (TextView) view.findViewById(R.id.price);
                qty = (TextView) view.findViewById(R.id.qty);
                total = (TextView) view.findViewById(R.id.total);
            }
        }
    }

    public static void start(Context context, String orderGuid){
        SaleItemsHistoryActivity_.intent(context).orderGuid(orderGuid).start();
    }

}
