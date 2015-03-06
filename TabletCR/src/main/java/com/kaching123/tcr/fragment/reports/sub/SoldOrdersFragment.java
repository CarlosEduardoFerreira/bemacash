package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.reports.RegisterReportsDetailsFragment.IDetailsFragment;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.reports.SoldOrdersReportQuery;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by gdubina on 03/02/14.
 */
@EFragment(R.layout.reports_sold_orders_list_fragment)
public class SoldOrdersFragment extends Fragment implements LoaderCallbacks<List<SaleOrderViewModel>>, IDetailsFragment {

    @ViewById(android.R.id.list)
    protected ListView listView;

    @ViewById
    protected TextView subtotal;

    @ViewById
    protected TextView discount;

    @ViewById
    protected TextView tax;

    @ViewById
    protected TextView total;

    @FragmentArg
    protected long startTime;

    @FragmentArg
    protected long endTime;

    @FragmentArg
    protected long resisterId;

    @FragmentArg
    protected boolean isSold;

    private ItemsAdapter adapter;

    protected BigDecimal totalValue = BigDecimal.ZERO;

    private TotalValues totalValues;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter = new ItemsAdapter(getActivity()));
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<List<SaleOrderViewModel>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<SaleOrderViewModel>>(getActivity()) {
            @Override
            public List<SaleOrderViewModel> loadInBackground() {
                List<SaleOrderViewModel> result = SoldOrdersReportQuery.getItemsWithoutTipRefunds(getActivity(), isSold, startTime, endTime, resisterId);
                totalValues = calcTotalValues(result);
                return result;
            }
        };
    }

    protected TotalValues calcTotalValues(List<SaleOrderViewModel> result){
        TotalValues tv = new TotalValues();
        for (SaleOrderViewModel i : result) {
            BigDecimal subtotal = i.tmpTotalPrice.add(i.tmpTotalDiscount).subtract(i.tmpTotalTax);
            tv.subtotal = tv.subtotal.add(subtotal);
            tv.tax = tv.tax.add(i.tmpTotalTax);
            tv.discount = tv.discount.add(i.tmpTotalDiscount);
            tv.total = tv.total.add(i.tmpTotalPrice);
        }
        return tv;
    }

    @Override
    public void onLoadFinished(Loader<List<SaleOrderViewModel>> cursorLoader, List<SaleOrderViewModel> cursor) {
        adapter.changeCursor(cursor);
        showTotal();
    }

    private void showTotal() {
        UiHelper.showPrice(subtotal, totalValues.subtotal);
        UiHelper.showPrice(discount, totalValues.discount);
        UiHelper.showPrice(tax, totalValues.tax);
        UiHelper.showPrice(total, totalValues.total);
    }

    @Override
    public void onLoaderReset(Loader<List<SaleOrderViewModel>> cursorLoader) {
        adapter.changeCursor(null);
    }

    @Override
    public void updateData(long startTime, long endTime, long resisterId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.resisterId = resisterId;
        if (getActivity() != null) {
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    public static SoldOrdersFragment instance(boolean isSold, long startTime, long endTime, long resisterId) {
        return SoldOrdersFragment_.builder().isSold(isSold).startTime(startTime).endTime(endTime).resisterId(resisterId).build();
    }

    private class ItemsAdapter extends ObjectsCursorAdapter<SaleOrderViewModel> {

        public ItemsAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View v = View.inflate(getContext(), R.layout.reports_sold_orders_item_view, null);
            v.setTag(new UiHolder(
                    (TextView) v.findViewById(R.id.date),
                    (TextView) v.findViewById(R.id.register_id),
                    (TextView) v.findViewById(R.id.total_price),
                    (TextView) v.findViewById(R.id.discount),
                    (TextView) v.findViewById(R.id.tax),
                    (TextView) v.findViewById(R.id.total)
            ));
            return v;
        }

        @Override
        protected View bindView(View view, int position, SaleOrderViewModel item) {
            UiHolder holder = (UiHolder) view.getTag();
            holder.date.setText(DateUtils.formatFull(item.createTime));
            holder.registerId.setText(item.registerTitle);

            showPrice(holder.totalPrice, item.tmpTotalPrice.add(item.tmpTotalDiscount).subtract(item.tmpTotalTax));
            showPrice(holder.tax, item.tmpTotalTax);
            showPrice(holder.discount, item.tmpTotalDiscount);
            showPrice(holder.total, item.tmpTotalPrice);
            return view;
        }
    }

    private static class UiHolder {
        private TextView date;
        private TextView registerId;
        private TextView totalPrice;
        private TextView discount;
        private TextView tax;
        private TextView total;

        private UiHolder(TextView date, TextView registerId, TextView totalPrice, TextView discount, TextView tax, TextView total) {
            this.date = date;
            this.registerId = registerId;
            this.totalPrice = totalPrice;
            this.discount = discount;
            this.tax = tax;
            this.total = total;
        }
    }

    private static class TotalValues {
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal tax;
        private BigDecimal total;

        private TotalValues() {
            subtotal = BigDecimal.ZERO;
            discount = BigDecimal.ZERO;
            tax = BigDecimal.ZERO;
            total = BigDecimal.ZERO;
        }
    }
}
