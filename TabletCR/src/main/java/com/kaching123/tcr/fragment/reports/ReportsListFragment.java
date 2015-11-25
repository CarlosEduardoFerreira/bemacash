package com.kaching123.tcr.fragment.reports;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.XReportChooserAlertDialogFragment;

import java.util.ArrayList;

/**
 * Created by gdubina on 20/02/14.
 */
@EFragment(R.layout.reports_list_fragment)
public class ReportsListFragment extends SuperBaseFragment {

    @ViewById
    ListView inventoryList;

    @ViewById
    ListView salesList;

    @ViewById
    ListView employeeList;

    @ViewById
    ListView shiftList;

    private OnReportSelectedListener listener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.reports_activity_label);

        listener = (OnReportSelectedListener) getActivity();
        salesList.setAdapter(new ReportsAdapter(getActivity(), new ReportType[]{
                ReportType.SALES_SUMMARY,
                ReportType.SOLD_ORDERS,
                ReportType.SALES_BY_ITEMS,
                ReportType.SALES_BY_DEPS,
                ReportType.SALES_TOP_10_REVENUES,
                ReportType.SALES_TOP_10_QTY,
                ReportType.SALES_BY_TENDER_TYPES,
                ReportType.RETURNED_ORDERS,
                ReportType.RETURNED_ITEMS,
                ReportType.SALES_BY_CUSTOMERS,
//                ReportType.PREPAID_SALES_BY_ITEMS,
//                ReportType.PREPAID_SALES_TOP_10_REVENUES,
                ReportType.DROPS_AND_PAYOUTS
        }));

        inventoryList.setAdapter(new ReportsAdapter(getActivity(), new ReportType[]{
                ReportType.REORDER_INVENTORY,
                ReportType.INVENTORY_VALUE,
                ReportType.INVENTORY_LOG,
                ReportType.INVENTORY_STATUS
        }));

        ArrayList<ReportType> employeeReports = new ArrayList<ReportType>();
        employeeReports.add(ReportType.EMPLOYEE_ATTENDANCE);
        employeeReports.add(ReportType.EMPLOYEE_PAYROLL);
        if (getApp().isTipsEnabled()) {
            employeeReports.add(ReportType.EMPLOYEE_TIPS);
        }
        employeeList.setAdapter(new ReportsAdapter(getActivity(), employeeReports.toArray(new ReportType[employeeReports.size()])));

        shiftList.setAdapter(new ReportsAdapter(getActivity(), new ReportType[]{
                ReportType.SHIFTS_REPORT,
                ReportType.X_REPORT
        }));

        final OnItemClickListener onItemClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReportsAdapter adapter = (ReportsAdapter) parent.getAdapter();
                listener.onReportSelected(adapter.getItem(position));
            }
        };

        OnItemClickListener onShiftReportItemClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReportsAdapter adapter = (ReportsAdapter) parent.getAdapter();
                final ReportType reportType = adapter.getItem(position);
                if (reportType == ReportType.SHIFTS_REPORT) {
                    listener.onReportSelected(adapter.getItem(position));
                } else if (reportType == ReportType.X_REPORT) {
                    if (getApp().isFreemium()) {
                        AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    } else {
                        XReportChooserAlertDialogFragment.show(getActivity());
                    }
                }
            }
        };

        salesList.setOnItemClickListener(onItemClickListener);
        inventoryList.setOnItemClickListener(onItemClickListener);
        employeeList.setOnItemClickListener(onItemClickListener);
        shiftList.setOnItemClickListener(onShiftReportItemClickListener);
    }

    public static ReportsListFragment instance() {
        return ReportsListFragment_.builder().build();
    }

    private class ReportsAdapter extends ArrayAdapter<ReportType> {

        private LayoutInflater inflater;

        public ReportsAdapter(Context context, ReportType[] items) {
            super(context, 0, items);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.reports_list_item_view, parent, false);
            }

            ReportType type = getItem(position);

            TextView text = (TextView) convertView;
            text.setText(type.getLabelRes());
            return convertView;
        }

    }

    public static interface OnReportSelectedListener {
        void onReportSelected(ReportType type);
    }
}
