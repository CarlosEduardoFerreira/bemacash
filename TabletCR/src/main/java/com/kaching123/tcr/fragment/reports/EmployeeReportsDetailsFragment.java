package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand;
import com.kaching123.tcr.commands.print.pos.PrintReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.fragment.reports.sub.EmployeeClockInOutFragment;
import com.kaching123.tcr.fragment.reports.sub.EmployeeTipsReportFragment;
import com.kaching123.tcr.fragment.reports.sub.PayrollReportFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamsterksu on 23.01.14.
 */
@EFragment(R.layout.reports_details_spinner_fragment)
@OptionsMenu(R.menu.sales_items_report)
public class EmployeeReportsDetailsFragment extends ReportsDetailsWithSpinnerFragment {

    private static final int LOADER_EMPLOYEE_ID = 0;

    private EmployeeAdapter employeeAdapter;

    private IDetailsFragment fragmentInterface;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().restartLoader(LOADER_EMPLOYEE_ID, null, employeeLoader);
    }

    @Override
    protected boolean supportMinFromDate() {
        return type != ReportType.EMPLOYEE_ATTENDANCE;
    }

    @Override
    protected int getSpinnerLabel() {
        return R.string.reports_mode_entity_employee;
    }

    @Override
    protected Fragment createBodyFragment() {
        Fragment fragment = null;
        switch (type) {
            case EMPLOYEE_ATTENDANCE: {
                EmployeeClockInOutFragment f = EmployeeClockInOutFragment.instance(fromDate.getTime(), toDate.getTime(), 0);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case EMPLOYEE_PAYROLL: {
                PayrollReportFragment f = PayrollReportFragment.instance(fromDate.getTime(), toDate.getTime(), 0);
                fragmentInterface = f;
                fragment = f;
                break;
            }
            case EMPLOYEE_TIPS: {
                EmployeeTipsReportFragment f = EmployeeTipsReportFragment.instance(fromDate.getTime(), toDate.getTime());
                fragmentInterface = f;
                fragment = f;
                break;
            }
        }
        return fragment;
    }

    @Override
    protected EmployeeAdapter getRegistersAdapter() {
        if (employeeAdapter == null)
            employeeAdapter = new EmployeeAdapter(getActivity());
        return employeeAdapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_export);
        if (item != null) {
            item.setVisible(type == ReportType.EMPLOYEE_ATTENDANCE);
        }
    }

    @OptionsItem
    protected void actionPrintSelected() {
        printWithConfirmation();
    }

    @Override
    protected void print(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        PrintReportsCommand.start(getActivity(), ignorePaperEnd, searchByMac, type, fromDate.getTime(), toDate.getTime(), getSelectedEmployeeGuid(), new PrintCallback());
    }

    @OptionsItem
    protected void actionEmailSelected() {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_email));
        SendDigitalReportsCommand.start(getActivity(), type, fromDate.getTime(), toDate.getTime(), getSelectedEmployeeGuid(), new PrintDigitalSaleByItemsReportCallback());
    }

    @OptionsItem
    protected void actionExportSelected() {
        FileChooserFragment.show(getActivity(), Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(getActivity(), getString(R.string.employee_export_wait_msg));
                ExportReportsCommand.start(getActivity(), file.getAbsolutePath(), type, fromDate.getTime(), toDate.getTime(), 0L, getSelectedEmployeeGuid(), null, exportCallback);
            }
        });
    }

    @Override
    protected void loadData() {
        long start = fromDate.getTime();
        long end = toDate.getTime();

        fragmentInterface.updateData(start, end, getSelectedEmployeeGuid());
    }

    private String getSelectedEmployeeGuid() {
        int selectedRegisterPos = modeEntitiesSpinner.getSelectedItemPosition();
        String employeeGuid = null;
        if (selectedRegisterPos != -1) {
            employeeGuid = employeeAdapter.getItem(selectedRegisterPos).guid;
        }
        return employeeGuid;
    }

    private LoaderManager.LoaderCallbacks<List<EmployeeModel>> employeeLoader = new LoaderCallbacks<List<EmployeeModel>>() {

        private final Uri URI_EMPLOYEE = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

        @Override
        public Loader<List<EmployeeModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(URI_EMPLOYEE)
                    .projection(EmployeeTable.GUID, EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME)
                    .where(EmployeeTable.IS_MERCHANT + " = ?", 0)
                    .orderBy(EmployeeTable.LAST_NAME)
                    .transform(new Function<Cursor, EmployeeModel>() {
                        @Override
                        public EmployeeModel apply(Cursor c) {
                            return new EmployeeModel(
                                    c.getString(0),
                                    c.getString(1),
                                    c.getString(2),
                                    null);
                        }
                    })
                    .wrap(new Function<List<EmployeeModel>, List<EmployeeModel>>() {
                        @Override
                        public List<EmployeeModel> apply(List<EmployeeModel> result) {
                            ArrayList<EmployeeModel> arrayList = new ArrayList<EmployeeModel>(result.size() + 1);
                            arrayList.add(new EmployeeModel(null, getString(R.string.register_label_all), "", null));
                            arrayList.addAll(result);
                            return arrayList;
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<EmployeeModel>> listLoader, List<EmployeeModel> employeeModels) {
            employeeAdapter.changeCursor(employeeModels);
        }

        @Override
        public void onLoaderReset(Loader<List<EmployeeModel>> listLoader) {
            employeeAdapter.changeCursor(null);
        }
    };

    public static EmployeeReportsDetailsFragment instance(ReportType reportType) {
        return EmployeeReportsDetailsFragment_.builder().type(reportType).build();
    }

    public static interface IDetailsFragment {
        void updateData(long startTime, long endTime, String employeeGuid);
    }

    private ExportCommandBaseCallback exportCallback = new ExportCommandBaseCallback() {
        @Override
        protected void handleSuccess(int count) {
            WaitDialogFragment.hide(getActivity());
            Toast.makeText(getActivity(), String.format("Exported %d rows", count), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(getActivity());
            Toast.makeText(getActivity(), "Export failed", Toast.LENGTH_SHORT).show();
        }
    };

    private static class EmployeeAdapter extends ObjectsCursorAdapter<EmployeeModel> {

        public EmployeeAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
        }

        @Override
        protected View bindView(View view, int position, EmployeeModel item) {
            ((TextView) view).setText(item.fullName());
            return view;
        }
    }

}
