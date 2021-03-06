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

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.reports.RegisterReportsDetailsFragment;
import com.kaching123.tcr.fragment.reports.RegisterReportsDetailsFragment.IDetailsFragment;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.reports.SoldOrdersReportQuery;
import com.kaching123.tcr.util.DateUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

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
    protected TextView gratuity;

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

    @FragmentArg
    protected String managerGuid;

    private ItemsAdapter adapter;

    protected BigDecimal totalValue = BigDecimal.ZERO;

    private TotalValues totalValues;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter = new ItemsAdapter(getActivity()));
        ((RegisterReportsDetailsFragment)getParentFragment()).showCashierSpinner();
        getLoaderManager().initLoader(2, null, this).forceLoad();
    }

    @Override
    public void onDetach() {
        ((RegisterReportsDetailsFragment)getParentFragment()).hideCashierSpinner();
        super.onDetach();
    }

    @Override
    public Loader<List<SaleOrderViewModel>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<SaleOrderViewModel>>(getActivity()) {
            @Override
            public List<SaleOrderViewModel> loadInBackground() {
                List<SaleOrderViewModel> result = SoldOrdersReportQuery.getItemsWithoutRefunds(getActivity(), isSold, startTime, endTime, resisterId, managerGuid);
                totalValues = calcTotalValues(result);
                return result;
            }
        };
    }

    protected TotalValues calcTotalValues(List<SaleOrderViewModel> result) {
        TotalValues tv = new TotalValues();
        for (SaleOrderViewModel i : result) {
            BigDecimal subtotal = i.tmpTotalPrice.add(i.tmpTotalDiscount).subtract(i.tmpTotalTax);
            tv.subtotal = tv.subtotal.add(subtotal);
            tv.tax = tv.tax.add(i.tmpTotalTax);
            tv.discount = tv.discount.add(i.tmpTotalDiscount);
            tv.gratuity = tv.gratuity.add(i.tipsAmount);
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
        UiHelper.showPrice(gratuity, totalValues.gratuity);
        UiHelper.showPrice(tax, totalValues.tax);
        UiHelper.showPrice(total, totalValues.total);
    }

    @Override
    public void onLoaderReset(Loader<List<SaleOrderViewModel>> cursorLoader) {
        adapter.changeCursor(null);
    }

    @Override
    public void updateData(long startTime, long endTime, long resisterId, int type, String managerGuid) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.resisterId = resisterId;
        this.managerGuid = managerGuid;
        if (getActivity() != null) {
            getLoaderManager().restartLoader(2, null, this).forceLoad();
        }
    }

    public static SoldOrdersFragment instance(boolean isSold, long startTime, long endTime, long resisterId) {
        return SoldOrdersFragment.instance(isSold, startTime, endTime, resisterId, null);
    }

    public static SoldOrdersFragment instance(boolean isSold, long startTime, long endTime, long resisterId, String managerGuid) {
        return SoldOrdersFragment_.builder().isSold(isSold).startTime(startTime).endTime(endTime).resisterId(resisterId).managerGuid(managerGuid).build();
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
                    (TextView) v.findViewById(R.id.cashier),
                    (TextView) v.findViewById(R.id.total_price),
                    (TextView) v.findViewById(R.id.discount),
                    (TextView) v.findViewById(R.id.gratuity),
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
            holder.cashier.setText(item.operatorName);

            showPrice(holder.totalPrice, item.tmpTotalPrice.add(item.tmpTotalDiscount).subtract(item.tmpTotalTax));
            showPrice(holder.tax, item.tmpTotalTax);
            showPrice(holder.discount, item.tmpTotalDiscount);
            showPrice(holder.gratuity, item.tipsAmount);
            showPrice(holder.total, item.tmpTotalPrice);
            return view;
        }
    }

    private static class UiHolder {
        private TextView date;
        private TextView registerId;
        private TextView cashier;
        private TextView totalPrice;
        private TextView discount;
        private TextView gratuity;
        private TextView tax;
        private TextView total;

        private UiHolder(TextView date, TextView registerId, TextView cashier, TextView totalPrice, TextView discount, TextView gratuity, TextView tax, TextView total) {
            this.date = date;
            this.registerId = registerId;
            this.cashier = cashier;
            this.totalPrice = totalPrice;
            this.discount = discount;
            this.gratuity = gratuity;
            this.tax = tax;
            this.total = total;
        }
    }

    private static class TotalValues {
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal tax;
        private BigDecimal total;
        private BigDecimal gratuity;

        private TotalValues() {
            subtotal = BigDecimal.ZERO;
            discount = BigDecimal.ZERO;
            tax = BigDecimal.ZERO;
            total = BigDecimal.ZERO;
            gratuity = BigDecimal.ZERO;
        }
    }
}
