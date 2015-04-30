package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.reports.DateRangeFragment;
import com.kaching123.tcr.reports.SalesByDropsAndPayoutsReportQuery;

import org.androidannotations.annotations.EFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by hamsterksu on 28.01.14.
 */
@EFragment(R.layout.reports_sales_by_drop_and_payouts_list_fragment)
public class SalesByDropsAndPayoutsFragment extends SalesBaseFragment<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> {

    @Override
    protected ObjectsCursorAdapter<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> createAdapter() {
        return new ItemAdapter(getActivity());
    }

    @Override
    public Loader<List<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState>>(getActivity()) {
            @Override
            public List<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> loadInBackground() {
                Collection<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> deps = new SalesByDropsAndPayoutsReportQuery().getItems(resisterId, getContext(), type, startTime, endTime);
                ArrayList<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> result = new ArrayList<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState>();
                totalValue = BigDecimal.ZERO;
                for (SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState d : deps) {
                    result.add(d);
                    totalValue.add(d.amount);
                }
                return (List) result;
            }
        };
    }

    private class ItemAdapter extends ObjectsCursorAdapter<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> {

        public ItemAdapter(Context context) {
            super(context);
        }


        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), R.layout.reports_sales_by_drops_and_payouts_view, null);
            view.setTag(new UiHolder(
                    (TextView) view.findViewById(R.id.date),
                    (TextView) view.findViewById(R.id.type),
                    (TextView) view.findViewById(R.id.amount),
                    (TextView) view.findViewById(R.id.comment)
            ));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState item) {
            UiHolder holder = (UiHolder) convertView.getTag();
            holder.date.setText(DateRangeFragment.periodDateFormat.format(new Date(Long.parseLong(item.date))).toString());
            if (item.type == 0)
                holder.type.setText(R.string.report_type_title_drops);
            else
                holder.type.setText(R.string.report_type_title_payouts);
            holder.comment.setText(item.comment);
            UiHelper.showPrice(holder.amount, item.amount);
            return convertView;
        }


    }

    private static class UiHolder {
        TextView date;
        TextView type;
        TextView amount;
        TextView comment;

        private UiHolder(TextView date, TextView type, TextView amount, TextView comment) {
            this.date = date;
            this.type = type;
            this.amount = amount;
            this.comment = comment;
        }
    }

    public static SalesByDropsAndPayoutsFragment instance(long startTime, long endTime, long resisterId, long type) {
        return SalesByDropsAndPayoutsFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).type(type).build();
    }
}
