package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery.CategoryStat;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery.DepartmentStatistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by hamsterksu on 28.01.14.
 */
@EFragment(R.layout.reports_sales_by_deps_list_fragment)
public class SalesByDepartmentsFragment extends SalesBaseFragment<CategoryStat> {

    @Override
    protected ObjectsCursorAdapter<CategoryStat> createAdapter() {
        return new ItemAdapter(getActivity());
    }

    @Override
    public Loader<List<CategoryStat>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<CategoryStat>>(getActivity()) {
            @Override
            public List<CategoryStat> loadInBackground() {
                Collection<DepartmentStatistics> deps = new SalesByDepartmentsReportQuery().getItems(getContext(), startTime, endTime, resisterId);
                ArrayList<CategoryStat> result = new ArrayList<CategoryStat>();
                totalValue = BigDecimal.ZERO;
                for (DepartmentStatistics d : deps) {
                    result.add(d);
                    Collection<CategoryStat> cats = d.getSortedList();
                    result.addAll(cats);
                    d.reset();
                    totalValue = totalValue.add(d.revenue);
                }
                return result;
            }
        };
    }

    private class ItemAdapter extends ObjectsCursorAdapter<CategoryStat> {

        public ItemAdapter(Context context) {
            super(context);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position).getClass() == DepartmentStatistics.class) {
                return 0;
            }
            return 1;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), getItemViewType(position) == 0 ? R.layout.reports_sales_by_deps_item0_view : R.layout.reports_sales_by_deps_item1_view, null);
            view.setTag(new UiHolder(
                    (TextView) view.findViewById(R.id.description),
                    (TextView) view.findViewById(R.id.price)
            ));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, CategoryStat item) {
            UiHolder holder = (UiHolder) convertView.getTag();
            holder.description.setText(item.description);
            UiHelper.showPrice(holder.price, item.revenue);
            return convertView;
        }


    }

    private static class UiHolder {
        TextView description;
        TextView price;

        private UiHolder(TextView description, TextView price) {
            this.description = description;
            this.price = price;
        }
    }

    public static SalesByDepartmentsFragment instance(long startTime, long endTime, long resisterId) {
        return SalesByDepartmentsFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).build();
    }
}
