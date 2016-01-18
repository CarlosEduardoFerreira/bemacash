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

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.XReportChooserAlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.ZReportChooserAlertDialogFragment;
import com.kaching123.tcr.model.PlanOptions;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

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
                ReportType.X_REPORT,
                ReportType.Z_REPORT
        }));

        final OnItemClickListener onItemClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReportsAdapter adapter = (ReportsAdapter) parent.getAdapter();
                ReportType type = adapter.getItem(position);
                if (isOptionAllowed(type)) {
                    listener.onReportSelected(adapter.getItem(position));
                }
            }
        };

        OnItemClickListener onShiftReportItemClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReportsAdapter adapter = (ReportsAdapter) parent.getAdapter();
                final ReportType reportType = adapter.getItem(position);
                if (isOptionAllowed(reportType)) {
                    switch (reportType) {
                        case SHIFTS_REPORT:
                            listener.onReportSelected(adapter.getItem(position));
                            break;
                        case X_REPORT:
                            XReportChooserAlertDialogFragment.show(getActivity());
                            break;
                        case Z_REPORT:
                            ZReportChooserAlertDialogFragment.show(getActivity());
                            break;
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

    private boolean isOptionAllowed(ReportType type) {

        if (!TcrApplication.get().isFreemium()) {
            return true;
        }

        switch (type) {
            case SALES_SUMMARY:
                if (!PlanOptions.isSalesSummaryReportReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SOLD_ORDERS:
                if (!PlanOptions.isSalesReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SALES_BY_ITEMS:
                if (!PlanOptions.isSalesByItemReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SALES_BY_DEPS:
                if (!PlanOptions.isSalesByDepartmentReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SALES_TOP_10_REVENUES:
                if (!PlanOptions.isToptenSoldRevenuesReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SALES_TOP_10_QTY:
                if (!PlanOptions.isToptenSoldItemsReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
            case SALES_BY_TENDER_TYPES:
                if (!PlanOptions.isSalesByTenderTypeReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case RETURNED_ORDERS:
                if (!PlanOptions.isSalesReturnsReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case RETURNED_ITEMS:
                if (!PlanOptions.isReturnedItemsReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SALES_BY_CUSTOMERS:
                if (!PlanOptions.isSalesByCustomersReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case DROPS_AND_PAYOUTS:
                if (!PlanOptions.isDropsAndPayoutsReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case REORDER_INVENTORY:
                if (!PlanOptions.isRestockReportReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case INVENTORY_VALUE:
                if (!PlanOptions.isInventoryValueReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case INVENTORY_LOG:
                if (!PlanOptions.isItemQuantityLogReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case INVENTORY_STATUS:
                if (!PlanOptions.isInventoryStatusReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case EMPLOYEE_ATTENDANCE:
                if (!PlanOptions.isEmployeeAttendanceReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case EMPLOYEE_PAYROLL:
                if (!PlanOptions.isEmployeePayrollReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            //// FIXME: 04.01.2016 consider to change value's name
            case EMPLOYEE_TIPS:
                if (!PlanOptions.isShiftGratuityReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case SHIFTS_REPORT:
                if (PlanOptions.isJustZReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
            case X_REPORT:
                if (PlanOptions.isJustZReportAllowed() || !PlanOptions.isXReportAllowed()) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                    return false;
                }
                break;
        }
        return true;
    }

    public interface OnReportSelectedListener {
        void onReportSelected(ReportType type);
    }
}
