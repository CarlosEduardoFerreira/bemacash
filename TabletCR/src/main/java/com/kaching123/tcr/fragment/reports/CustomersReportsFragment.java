package com.kaching123.tcr.fragment.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand.BaseSendDigitalReportsCallback;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.fragment.reports.sub.SalesByCustomersFragment;

import java.io.File;

/**
 * Created by pkabakov on 18.02.14.
 */

@EFragment(R.layout.reports_customers_fragment)
@OptionsMenu(R.menu.customers_reports_fragment)
public class CustomersReportsFragment extends DateRangeFragment {

    private static final int MAX_PERIODS_COUNT = 31;

    private IDetailsFragment fragmentInterface;

    private ExportCallback exportCallback = new ExportCallback();

    public static CustomersReportsFragment instance() {
        return CustomersReportsFragment_.builder().build();
    }

    @Override
    protected int getMaxPeriod() {
        return MAX_PERIODS_COUNT;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        Fragment fragment = createBodyFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.details_body, fragment).commit();

        loadData();
    }

    private Fragment createBodyFragment() {
        Fragment fragment = SalesByCustomersFragment.instance(fromDate.getTime(), toDate.getTime());
        fragmentInterface = (IDetailsFragment) fragment;
        return fragment;
    }

    @Override
    protected void loadData() {
        long start = fromDate.getTime();
        long end = toDate.getTime();

        fragmentInterface.updateData(start, end);
    }

    @OptionsItem
    protected void actionExportSelected() {
        FileChooserFragment.show(getActivity(), Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(getActivity(), getString(R.string.inventory_export_wait_msg));
                ExportReportsCommand.start(getActivity(), file.getAbsolutePath(), ReportType.SALES_BY_CUSTOMERS, fromDate.getTime(), toDate.getTime(), 0L, null, null, exportCallback);
            }
        });
    }

    @OptionsItem
    protected void actionPrintSelected() {
        AlertDialogFragment.showConfirmation(getActivity(), R.string.report_printing_confirmation_dlg_title,
                getString(R.string.report_printing_confirmation_dlg_msg),
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        print(false, false);
                        return true;
                    }
                }
        );
    }

    @OptionsItem
    protected void actionEmailSelected() {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_email));
        SendDigitalReportsCommand.start(getActivity(), ReportType.SALES_BY_CUSTOMERS, fragmentInterface.getStart(), fragmentInterface.getEnd(), 0, new PrintDigitalCallback());
    }

    private void print(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        PrintReportsCommand.start(getActivity(), ignorePaperEnd, searchByMac, ReportType.SALES_BY_CUSTOMERS, fragmentInterface.getStart(), fragmentInterface.getEnd(), 0, new PrintCallback());
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

    private class PrintCallback extends BasePrintCallback {

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                print(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                //do nothing
            }
        };

        @Override
        protected void onPrintSuccess() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showComplete(getActivity(), R.string.report_details_printing_completed, null);
        }

        @Override
        protected void onPrintError(PrinterError error) {
            PrintCallbackHelper2.onPrintError(getActivity(), error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(getActivity(), retryListener);
        }
    }

    private class PrintDigitalCallback extends BaseSendDigitalReportsCallback {

        @Override
        protected void handleSuccess() {
            WaitDialogFragment.hide(getActivity());
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.report_details_error_generate_report));
        }
    }


    public static interface IDetailsFragment {
        void updateData(long startTime, long endTime);

        long getStart();

        long getEnd();
    }
}
