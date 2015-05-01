package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand;
import com.kaching123.tcr.commands.print.pos.PrintReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.fragment.reports.sub.ItemManualMovementFragment;
import com.kaching123.tcr.fragment.reports.sub.ReturnedItemsFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesByDepartmentsFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesByDropsAndPayoutsFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesByItemsFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesByItemsReportFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesByTenderTypesFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesItemsTop10QtyFragment;
import com.kaching123.tcr.fragment.reports.sub.SalesItemsTop10RevenuesFragment;
import com.kaching123.tcr.fragment.reports.sub.ShiftsReportFragment;
import com.kaching123.tcr.fragment.reports.sub.SoldOrdersFragment;
import com.kaching123.tcr.model.EmployeeForReportsModel;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.RegisterModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 23.01.14.
 */
@EFragment(R.layout.reports_details_spinner_fragment)
@OptionsMenu(R.menu.sales_items_report)
public class RegisterReportsDetailsFragment extends ReportsDetailsWithSpinnerFragment {

    private static final int LOADER_REGISTERS_ID = 0;

    private static final int LOADER_EMPLOYEE_ID = 1;

    private RegistersAdapter registersAdapter;

    private EmployeesAdapter employeesAdapter;

    private TypeAdapter typeAdapter;

    private IDetailsFragment fragmentInterface;

    private ExportCallback exportCallback = new ExportCallback();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (type != ReportType.DROPS_AND_PAYOUTS)
            getLoaderManager().restartLoader(LOADER_REGISTERS_ID, null, registersLoader);
        else
            getLoaderManager().restartLoader(LOADER_EMPLOYEE_ID, null, employeesLoader);
    }

    @Override
    protected int getSpinnerLabel() {
        return R.string.reports_mode_entity_register;
    }

    @Override
    protected Fragment createBodyFragment() {
        Fragment fragment = null;
        showSpinner();
        switch (type) {
            case SALES_BY_ITEMS: {
                SalesByItemsReportFragment f = SalesByItemsReportFragment.instance(fromDate.getTime(), toDate.getTime(), 0L, OrderType.SALE);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case SALES_BY_DEPS: {
                SalesByDepartmentsFragment f = SalesByDepartmentsFragment.instance(fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case SALES_TOP_10_REVENUES: {
                SalesItemsTop10RevenuesFragment f = SalesItemsTop10RevenuesFragment.instance(fromDate.getTime(), toDate.getTime(), 0L, OrderType.SALE);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case SALES_TOP_10_QTY: {
                SalesItemsTop10QtyFragment f = SalesItemsTop10QtyFragment.instance(fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case SALES_BY_TENDER_TYPES: {
                SalesByTenderTypesFragment f = SalesByTenderTypesFragment.instance(fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case RETURNED_ITEMS: {
                ReturnedItemsFragment f = ReturnedItemsFragment.instance(fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case RETURNED_ORDERS: {
                SoldOrdersFragment f = SoldOrdersFragment.instance(false, fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case SOLD_ORDERS: {
                SoldOrdersFragment f = SoldOrdersFragment.instance(true, fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case INVENTORY_LOG: {
                hideSpinner();
                ItemManualMovementFragment f = ItemManualMovementFragment.instance(fromDate.getTime(), toDate.getTime());
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case SHIFTS_REPORT: {
                ShiftsReportFragment f = ShiftsReportFragment.instance(fromDate.getTime(), toDate.getTime(), 0L);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case PREPAID_SALES_BY_ITEMS: {
                SalesByItemsFragment f = SalesByItemsFragment.instance(fromDate.getTime(), toDate.getTime(), 0L, OrderType.PREPAID);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case PREPAID_SALES_TOP_10_REVENUES: {
                SalesItemsTop10RevenuesFragment f = SalesItemsTop10RevenuesFragment.instance(fromDate.getTime(), toDate.getTime(), 0L, OrderType.PREPAID);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case DROPS_AND_PAYOUTS: {
                SalesByDropsAndPayoutsFragment f = SalesByDropsAndPayoutsFragment.instance(fromDate.getTime(), toDate.getTime(), 0L, getSelectedType());
                fragmentInterface = f;
                fragment = f;
                showCashDrawReport();
                break;
            }
        }
        return fragment;
    }

    @Override
    protected BaseAdapter getRegistersAdapter() {
        if (type != ReportType.DROPS_AND_PAYOUTS) {
            if (registersAdapter == null)
                registersAdapter = new RegistersAdapter(getActivity());
            return registersAdapter;
        } else {
            if (employeesAdapter == null)
                employeesAdapter = new EmployeesAdapter(getActivity());
            return employeesAdapter;
        }
    }

    @Override
    protected TypeAdapter getTypesAdapter() {
        if (typeAdapter == null)
            typeAdapter = new TypeAdapter(getActivity());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getString(R.string.report_type_title_drops));
        list.add(getString(R.string.report_type_title_payouts));
        typeAdapter.changeCursor(list);
        return typeAdapter;
    }

    class TypeAdapter extends ObjectsCursorAdapter<String> {

        public TypeAdapter(Context context) {
            super(context);
        }

        protected View newDropDownView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
            return view;
        }

        @Override
        protected View bindView(View view, int position, String typeName) {
            ((TextView) view).setText(typeName);
            return view;
        }

    }

    @Override
    protected void loadData() {
        long start = fromDate.getTime();
        long end = toDate.getTime();

        long type = typeSpinner.getSelectedItemPosition();
        long resisterId = getSelectedRegisterId();
        String managerGuid = getSelectedManagerGuid();
        fragmentInterface.updateData(start, end, resisterId, type, managerGuid);
    }

    @Override
    protected void print(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        if (type != ReportType.DROPS_AND_PAYOUTS)
            PrintReportsCommand.start(getActivity(), ignorePaperEnd, searchByMac, type, fromDate.getTime(), toDate.getTime(), getSelectedRegisterId(), new PrintCallback());
        else
            PrintReportsCommand.start(getActivity(), ignorePaperEnd, searchByMac, type, fromDate.getTime(), toDate.getTime(), getSelectedRegisterId(), typeSpinner.getSelectedItemPosition(), getSelectedEmployeeName(), new PrintCallback());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem print = menu.findItem(R.id.action_print);
        MenuItem email = menu.findItem(R.id.action_email);
        MenuItem export = menu.findItem(R.id.action_export);
        assert print != null;
        assert email != null;
        assert export != null;

        boolean printable = type != ReportType.SOLD_ORDERS && type != ReportType.RETURNED_ORDERS && type != ReportType.SHIFTS_REPORT;

        print.setVisible(printable);
        email.setVisible(printable);

        export.setVisible(
                type == ReportType.SALES_BY_ITEMS
                        || type == ReportType.SALES_TOP_10_QTY
                        || type == ReportType.SALES_TOP_10_REVENUES
                        || type == ReportType.RETURNED_ITEMS
                        || type == ReportType.RETURNED_ORDERS
                        || type == ReportType.SOLD_ORDERS
        );
    }

    @OptionsItem
    protected void actionPrintSelected() {
        printWithConfirmation();
    }

    @OptionsItem
    protected void actionEmailSelected() {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_email));
        if (type != ReportType.DROPS_AND_PAYOUTS)
            SendDigitalReportsCommand.start(getActivity(), type, fromDate.getTime(), toDate.getTime(), getSelectedRegisterId(), new PrintDigitalSaleByItemsReportCallback());
        else
            SendDigitalReportsCommand.start(getActivity(), type, fromDate.getTime(), toDate.getTime(), getSelectedManagerGuid(), typeSpinner.getSelectedItemPosition(), getSelectedEmployeeName(), new PrintDigitalSaleByItemsReportCallback());

    }

    @OptionsItem
    protected void actionExportSelected() {
        FileChooserFragment.show(getActivity(), Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(getActivity(), getString(R.string.inventory_export_wait_msg));
                ExportReportsCommand.start(getActivity(), file.getAbsolutePath(), type, fromDate.getTime(), toDate.getTime(), getSelectedRegisterId(), null, null, exportCallback);
            }
        });
    }

    private long getSelectedRegisterId() {
        if (type == ReportType.DROPS_AND_PAYOUTS)
            return 0;
        int selectedRegisterPos = modeEntitiesSpinner.getSelectedItemPosition();
        long resisterId = 0;
        if (selectedRegisterPos != -1) {
            resisterId = registersAdapter.getItem(selectedRegisterPos).id;
        }
        return resisterId;
    }

    private String getSelectedManagerGuid() {
        if (type != ReportType.DROPS_AND_PAYOUTS)
            return null;
        int selectedManagerPos = modeEntitiesSpinner.getSelectedItemPosition();
        String managerGuid = null;
        if (selectedManagerPos != -1) {
            managerGuid = employeesAdapter.getItem(selectedManagerPos).guid;
        }
        return managerGuid;
    }

    private String getSelectedEmployeeName() {
        if (type != ReportType.DROPS_AND_PAYOUTS)
            return null;
        int selectedManagerPos = modeEntitiesSpinner.getSelectedItemPosition();
        String employeeName = null;
        if (selectedManagerPos != -1) {
            employeeName = employeesAdapter.getItem(selectedManagerPos).fullName();
        }
        return employeeName;
    }

    private long getSelectedType() {
        int selectedType = typeSpinner.getSelectedItemPosition();
        return selectedType;
    }

    private LoaderManager.LoaderCallbacks<List<RegisterModel>> registersLoader = new RegistersLoader() {

        @Override
        public void onLoadFinished(Loader<List<RegisterModel>> loader, List<RegisterModel> result) {
            ArrayList<RegisterModel> arrayList = new ArrayList<RegisterModel>(result.size() + 1);
            arrayList.add(new RegisterModel(0, null, getString(R.string.register_label_all), null, 0, 0));
            arrayList.addAll(result);
            registersAdapter.changeCursor(arrayList);
        }

        @Override
        public void onLoaderReset(Loader<List<RegisterModel>> loader) {
            registersAdapter.changeCursor(null);
        }

        @Override
        protected Context getLoaderContext() {
            return getActivity();
        }
    };

    private LoaderManager.LoaderCallbacks<List<EmployeeForReportsModel>> employeesLoader = new MangerNamesLoader() {

        @Override
        public void onLoadFinished(Loader<List<EmployeeForReportsModel>> loader, List<EmployeeForReportsModel> data) {
            ArrayList<EmployeeForReportsModel> arrayList = new ArrayList<EmployeeForReportsModel>(data.size() + 1);
            arrayList.add(new EmployeeForReportsModel(null, null, null, getString(R.string.register_label_all)));
            for (EmployeeForReportsModel model : data) {
                if (isEmployeeLogin(model.login))
                    arrayList.add(model);
            }
            employeesAdapter.changeCursor((List) arrayList);
        }

        @Override
        public void onLoaderReset(Loader<List<EmployeeForReportsModel>> loader) {
            employeesAdapter.changeCursor(null);
        }

        @Override
        protected Context getLoaderContext() {
            return getActivity();
        }
    };


    private boolean isEmployeeLogin(String login) {
        try {
            Integer.parseInt(login);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static RegisterReportsDetailsFragment instance(ReportType reportType) {
        return RegisterReportsDetailsFragment_.builder().type(reportType).build();
    }

    public static interface IDetailsFragment {
        void updateData(long startTime, long endTime, long resisterId, long type, String managerGuid);
    }

    public class ExportCallback extends ExportCommandBaseCallback {

        @Override
        protected void handleSuccess(int count) {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showComplete(getActivity(), R.string.inventory_export_success_title, getString(R.string.inventory_export_success_msg, count));
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.inventory_export_error_title, getString(R.string.inventory_export_error_msg));
        }
    }

}
