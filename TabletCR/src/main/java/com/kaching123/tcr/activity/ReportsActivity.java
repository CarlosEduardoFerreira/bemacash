package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.XReportChooserAlertDialogFragment.XReportTypeChooseListener;
import com.kaching123.tcr.fragment.dialog.ZReportChooserAlertDialogFragment.ZReportTypeChooserListener;
import com.kaching123.tcr.fragment.reports.CustomersReportsFragment;
import com.kaching123.tcr.fragment.reports.EmployeeReportsDetailsExtFragment;
import com.kaching123.tcr.fragment.reports.EmployeeReportsDetailsFragment;
import com.kaching123.tcr.fragment.reports.InventoryValueFragment;
import com.kaching123.tcr.fragment.reports.RegisterReportsDetailsFragment;
import com.kaching123.tcr.fragment.reports.ReorderReportFragment;
import com.kaching123.tcr.fragment.reports.ReportsChartFragment;
import com.kaching123.tcr.fragment.reports.ReportsListFragment;
import com.kaching123.tcr.fragment.reports.ReportsListFragment.OnReportSelectedListener;
import com.kaching123.tcr.fragment.reports.sub.InventoryStatusReportFragment;
import com.kaching123.tcr.fragment.shift.PrintXReportFragment;
import com.kaching123.tcr.fragment.shift.PrintZReportFragment;
import com.kaching123.tcr.model.Permission;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by pkabakov on 13.01.14.
 */
@EActivity(R.layout.reports_activity)
public class ReportsActivity extends SuperBaseActivity implements OnReportSelectedListener, XReportTypeChooseListener, ZReportTypeChooserListener {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.REPORTS);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }


    public static void start(Context context) {
        ReportsActivity_.intent(context).start();
    }

    @AfterViews
    protected void initViews() {
        ReportsListFragment fragment = ReportsListFragment.instance();
        getSupportFragmentManager().beginTransaction().replace(R.id.report_details, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.report_details);
        if (fragment instanceof ReportsChartFragment) {
            if (((ReportsChartFragment) fragment).onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onReportSelected(ReportType type) {
        updateReportDetails(type);
    }

    @Override
    public void onXReportTypeChosen(ReportType xReportType) {
        if (ReportType.X_REPORT_CURRENT_SHIFT == xReportType) {
            if (!getApp().isShiftOpened()) {
                AlertDialogFragment.showAlert(this, R.string.error_dialog_title, getResources().getString(R.string.reports_error_open_shift));
            } else {
                PrintXReportFragment.show(this, getApp().getShiftGuid(), xReportType);
            }
        } else if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            PrintXReportFragment.show(this, null, xReportType);
        }
    }

    private void updateReportDetails(ReportType type) {
        getActionBar().setTitle(type.getLabelRes());
        Fragment fragment;
        switch (type) {
            case SALES_SUMMARY:
                fragment = ReportsChartFragment.instance();
            break;

            case EMPLOYEE_ATTENDANCE:
            case EMPLOYEE_TIPS:
                fragment = EmployeeReportsDetailsFragment.instance(type);
                break;

            case EMPLOYEE_PAYROLL:
                fragment = EmployeeReportsDetailsExtFragment.instance(type);
                break;
            case REORDER_INVENTORY:
                fragment = ReorderReportFragment.instance();
                break;
            case INVENTORY_VALUE:
                fragment = InventoryValueFragment.instance();
                break;
            case SALES_BY_CUSTOMERS:
                fragment = CustomersReportsFragment.instance();
                break;
            case INVENTORY_STATUS:
                fragment = InventoryStatusReportFragment.instance();
                break;
            default:
                fragment = RegisterReportsDetailsFragment.instance(type);
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.report_details, fragment).addToBackStack(type.name()).commit();
    }

    @Override
    public void onZReportTypeChosen(ReportType zReportType) {
        if (ReportType.Z_REPORT_CURRENT_SHIFT == zReportType) {
            if (!getApp().isShiftOpened()) {
                AlertDialogFragment.showAlert(this, R.string.error_dialog_title, getResources().getString(R.string.reports_error_open_shift));
            } else {
                PrintZReportFragment.show(this, getApp().getShiftGuid(), zReportType);
            }
        } else if (ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            PrintZReportFragment.show(this, null, zReportType);
        }
    }

    public enum ReportType {

        SALES_SUMMARY(R.string.report_type_sales_summary, R.string.report_category_sales),
        SOLD_ORDERS(R.string.report_type_sold_orders, R.string.report_category_sales),
        SALES_BY_ITEMS(R.string.report_type_sales_by_items, R.string.report_category_sales),
        SALES_BY_DEPS(R.string.report_type_sales_by_deps, R.string.report_category_sales),
        SALES_TOP_10_REVENUES(R.string.report_type_sales_top10_revenues, R.string.report_category_sales),
        SALES_TOP_10_QTY(R.string.report_type_sales_top10_qty, R.string.report_category_sales),
        SALES_BY_TENDER_TYPES(R.string.report_type_sales_by_tender_types, R.string.report_category_sales),
        RETURNED_ORDERS(R.string.report_type_returned_orders, R.string.report_category_sales),
        RETURNED_ITEMS(R.string.report_type_returned_items, R.string.report_category_sales),
        SALES_BY_CUSTOMERS(R.string.report_type_by_customers, R.string.report_category_sales),
        PREPAID_SALES_BY_ITEMS(R.string.report_type_prepaid_sales_by_items, R.string.report_category_sales),
        PREPAID_SALES_TOP_10_REVENUES(R.string.report_type_prepaid_sales_top10_revenues, R.string.report_category_sales),
        REORDER_INVENTORY(R.string.report_type_reorder_inventory, R.string.report_category_inventory),
        INVENTORY_VALUE(R.string.report_type_inventory_value, R.string.report_category_inventory),
        INVENTORY_LOG(R.string.report_type_inventory_qty_log, R.string.report_category_inventory),
        INVENTORY_STATUS(R.string.report_type_inventory_status, R.string.report_category_inventory),
        INVENTORY_STATUS_POS(R.string.report_type_inventory_status, R.string.report_category_inventory),
        EMPLOYEE_ATTENDANCE(R.string.report_type_employee_attendance, R.string.report_category_employee),
        EMPLOYEE_PAYROLL(R.string.report_type_employee_payroll, R.string.report_category_employee),
        SHIFTS_REPORT(R.string.report_type_shifts_report, R.string.report_category_shift),
        X_REPORT(R.string.report_type_x_report, R.string.report_category_shift),
        X_REPORT_CURRENT_SHIFT(R.string.xreport_chooser_current_shift_sales, R.string.report_category_shift),
        X_REPORT_DAILY_SALES(R.string.xreport_chooser_sale_for_a_day, R.string.report_category_shift),
        Z_REPORT(R.string.report_type_z_report, R.string.report_category_shift),
        Z_REPORT_CURRENT_SHIFT(R.string.zreport_chooser_current_shift_sales, R.string.report_category_shift),
        Z_REPORT_DAILY_SALES(R.string.zreport_chooser_sale_for_a_day, R.string.report_category_shift),
        EMPLOYEE_TIPS(R.string.report_type_tips_report, R.string.report_category_employee),
        DROPS_AND_PAYOUTS(R.string.report_type_drops_and_payouts, R.string.report_category_sales);

        private final int labelRes;
        private final int categoryRes;

        ReportType(int labelRes, int categoryRes) {
            this.labelRes = labelRes;
            this.categoryRes = categoryRes;
        }

        public int getLabelRes() {
            return labelRes;
        }
    }

}
