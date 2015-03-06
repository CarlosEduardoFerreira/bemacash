package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.reports.CustomersReportsFragment.IDetailsFragment;
import com.kaching123.tcr.model.SalesByCustomerModel;
import com.kaching123.tcr.reports.SalesByCustomersReportQuery;

import java.util.List;

/**
 * Created by pkabakov on 18.02.14.
 */

@EFragment(R.layout.reports_sales_by_customer_list_fragment)
public class SalesByCustomersFragment extends SuperBaseFragment implements IDetailsFragment {

    @ViewById(android.R.id.list)
    protected ListView listView;

    @FragmentArg
    protected long startTime;

    @FragmentArg
    protected long endTime;

    private ItemsAdapter adapter;

    private SalesByCustomerLoader salesByCustomerLoader = new SalesByCustomerLoader();

    public static SalesByCustomersFragment instance(long startTime, long endTime) {
        return SalesByCustomersFragment_.builder().startTime(startTime).endTime(endTime).build();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter = new ItemsAdapter(getActivity()));
        getLoaderManager().initLoader(0, null, salesByCustomerLoader);
    }

    @Override
    public void updateData(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        if (getActivity() != null) {
            getLoaderManager().restartLoader(0, null, salesByCustomerLoader).forceLoad();
        }
    }

    @Override
    public long getStart() {
        return startTime;
    }

    @Override
    public long getEnd() {
        return endTime;
    }

    private class SalesByCustomerLoader implements LoaderCallbacks<List<SalesByCustomerModel>> {

        public Loader<List<SalesByCustomerModel>> onCreateLoader(int i, Bundle bundle) {
            return SalesByCustomersReportQuery.query(getActivity(), startTime, endTime);
        }


        public void onLoadFinished(Loader<List<SalesByCustomerModel>> cursorLoader, List<SalesByCustomerModel> cursor) {
            adapter.changeCursor(cursor);
        }


        public void onLoaderReset(Loader<List<SalesByCustomerModel>> cursorLoader) {
            adapter.changeCursor(null);
        }
    }

    private class ItemsAdapter extends ObjectsCursorAdapter<SalesByCustomerModel> {

        public ItemsAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View v = View.inflate(getContext(), R.layout.reports_sales_by_customers_item_view, null);
            v.setTag(new UiHolder(
                    (TextView) v.findViewById(R.id.name),
                    (TextView) v.findViewById(R.id.phone),
                    (TextView) v.findViewById(R.id.email),
                    (TextView) v.findViewById(R.id.total)
            ));
            return v;
        }

        @Override
        protected View bindView(View view, int position, SalesByCustomerModel item) {
            UiHolder holder = (UiHolder) view.getTag();

            holder.name.setText(item.customerName);
            holder.phone.setText(item.customerPhone);
            holder.email.setText(item.customerEmail);
            UiHelper.showPrice(holder.total, item.totalAmount);

            return view;
        }
    }

    private static class UiHolder {
        TextView name;
        TextView phone;
        TextView email;
        TextView total;

        private UiHolder(TextView name, TextView phone, TextView email, TextView total) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.total = total;
        }
    }
}
