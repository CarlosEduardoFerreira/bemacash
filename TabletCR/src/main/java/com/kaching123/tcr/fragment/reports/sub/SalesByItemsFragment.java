package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.print.SaleReportsProcessor;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.reports.SalesByItemsReportQuery;
import com.kaching123.tcr.reports.SalesByItemsReportQuery.ReportItemInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 27.01.14.
 */
@EFragment(R.layout.reports_sales_by_item_list_fragment)
public class SalesByItemsFragment extends SalesBaseFragment<ReportItemInfo> {

    @FragmentArg
    protected OrderType orderType;

    @Override
    protected ObjectsCursorAdapter<ReportItemInfo> createAdapter() {
        return new ItemAdapter(getActivity());
    }

    @Override
    public Loader<List<ReportItemInfo>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<ReportItemInfo>>(getActivity()) {
            @Override
            public List<ReportItemInfo> loadInBackground() {
                ArrayList<ReportItemInfo> result = new ArrayList<ReportItemInfo>(createQuery().getItems(getContext(), startTime, endTime, resisterId, orderType));
                final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult = SaleReportsProcessor.getGroupedResult(result, orderType);
                return getResultList(groupedResult);
            }
        };
    }

    protected List<SalesByItemsReportQuery.ReportItemInfo> getResultList(ArrayList<SalesByItemsReportQuery.ReportItemInfo> result) {
        totalValue = calcTotalValue(result);
        return result;
    }

    protected BigDecimal calcTotalValue(ArrayList<ReportItemInfo> result) {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (ReportItemInfo i : result) {
            totalValue = totalValue.add(i.revenue);
        }
        return totalValue;
    }

    protected SalesByItemsReportQuery createQuery() {
        return new SalesByItemsReportQuery(isSale());
    }

    protected boolean isSale() {
        return true;
    }

    private class ItemAdapter extends ObjectsCursorAdapter<ReportItemInfo> {

        public ItemAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), R.layout.reports_sales_by_item_item_view, null);
            view.setTag(new UiHolder(
                    (TextView) view.findViewById(R.id.description),
                    (TextView) view.findViewById(R.id.qty),
                    (TextView) view.findViewById(R.id.price)
            ));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, ReportItemInfo item) {
            UiHolder holder = (UiHolder) convertView.getTag();
            holder.description.setText(item.description);
            UiHelper.showQuantityInteger(holder.qty, item.qty);
            UiHelper.showPrice(holder.price, item.revenue);
            return convertView;
        }


    }

    private static class UiHolder {
        TextView description;
        TextView qty;
        TextView price;

        private UiHolder(TextView description, TextView qty, TextView price) {
            this.description = description;
            this.qty = qty;
            this.price = price;
        }
    }

    public static SalesByItemsFragment instance(long startTime, long endTime, long resisterId, OrderType orderType) {
        return SalesByItemsFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).orderType(orderType).build();
    }
}
