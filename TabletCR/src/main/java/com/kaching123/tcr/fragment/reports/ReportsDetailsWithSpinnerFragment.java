package com.kaching123.tcr.fragment.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand.BaseSendDigitalReportsCallback;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;

/**
 * Created by gdubina on 29/01/14.
 */
@EFragment
public abstract class ReportsDetailsWithSpinnerFragment extends DateRangeFragment {

    protected static final int MAX_PERIODS_COUNT = 31;

    @ViewById
    protected TextView modeEntitiesLabel;

    @ViewById
    protected Spinner modeEntitiesSpinner;

    @FragmentArg
    protected ReportType type;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        modeEntitiesLabel.setText(getSpinnerLabel());
        modeEntitiesSpinner.setAdapter(getRegistersAdapter());
        modeEntitiesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Fragment fragment = createBodyFragment();
        if (fragment != null) {
            getChildFragmentManager().beginTransaction().replace(R.id.details_body, fragment).commit();
        }
        loadData();
    }

    protected abstract int getSpinnerLabel();

    protected void showSpinner() {
        modeEntitiesSpinner.setVisibility(View.VISIBLE);
        modeEntitiesLabel.setVisibility(View.VISIBLE);
    }

    protected void hideSpinner() {
        modeEntitiesSpinner.setVisibility(View.GONE);
        modeEntitiesLabel.setVisibility(View.GONE);
    }

    @Override
    protected int getMaxPeriod() {
        return MAX_PERIODS_COUNT;
    }

    protected abstract BaseAdapter getRegistersAdapter();

    protected abstract Fragment createBodyFragment();

    protected abstract void print(boolean ignorePaperEnd, boolean searchByMac);

    protected void printWithConfirmation() {
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

    protected class PrintCallback extends BasePrintCallback {

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

    protected class PrintDigitalSaleByItemsReportCallback extends BaseSendDigitalReportsCallback {

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

}
