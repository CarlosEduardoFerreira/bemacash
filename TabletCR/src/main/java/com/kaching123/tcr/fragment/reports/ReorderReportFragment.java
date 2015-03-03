package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalReportsCommand.BaseSendDigitalReportsCallback;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportRestockReportCommand;
import com.kaching123.tcr.commands.store.export.ExportRestockReportCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.reports.ReorderReportQuery;
import com.kaching123.tcr.reports.ReorderReportQuery.ItemQtyInfo;

import java.io.File;
import java.util.List;

/**
 * Created by gdubina on 31.01.14.
 */
@EFragment(R.layout.reports_reorder_inventory_list_fragment)
@OptionsMenu(R.menu.sales_items_report)
public class ReorderReportFragment extends SuperBaseFragment implements LoaderCallbacks<List<ItemQtyInfo>> {

    @ViewById(android.R.id.list)
    protected ListView listView;

    protected ObjectsCursorAdapter<ItemQtyInfo> adapter;

    private ExportCommandCallback exportCommandCallback = new ExportCommandCallback();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        listView.setAdapter(adapter = new ItemAdapter(getActivity()));
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<List<ItemQtyInfo>> onCreateLoader(int i, Bundle bundle) {
        return ReorderReportQuery.getLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ItemQtyInfo>> listLoader, List<ItemQtyInfo> list) {
        adapter.changeCursor(list);
    }

    @Override
    public void onLoaderReset(Loader<List<ItemQtyInfo>> listLoader) {
        adapter.changeCursor(null);
    }

    @OptionsItem
    protected void actionEmailSelected() {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_email));
        SendDigitalReportsCommand.start(getActivity(), ReportType.REORDER_INVENTORY, new DigitalCallback());
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
    protected void actionExportSelected() {
        FileChooserFragment.show(getActivity(), Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(getActivity(), getString(R.string.inventory_export_wait_msg));
                ExportRestockReportCommand.start(getActivity(), file.getAbsolutePath(), exportCommandCallback);
            }
        });
    }

    private void print(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        PrintReportsCommand.start(getActivity(), ignorePaperEnd, searchByMac, ReportType.REORDER_INVENTORY, new PrintCallback());
    }

    private class ExportCommandCallback extends ExportCommandBaseCallback {

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

    private class ItemAdapter extends ObjectsCursorAdapter<ItemQtyInfo> {

        public ItemAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), R.layout.reports_reorder_inventory_item_view, null);
            view.setTag(new UiHolder(
                    (TextView) view.findViewById(R.id.item),
                    (TextView) view.findViewById(R.id.qty),
                    (TextView) view.findViewById(R.id.rec_qty),
                    (TextView) view.findViewById(R.id.to_order_qty)
            ));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, ItemQtyInfo item) {
            UiHolder holder = (UiHolder) convertView.getTag();
            holder.item.setText(item.description);
            UiHelper.showQuantityInteger(holder.qty, item.qty);
            UiHelper.showQuantityInteger(holder.recQty, item.recQty);
            UiHelper.showQuantityInteger(holder.toOrderQty, item.recQty.subtract(item.qty));

            return convertView;
        }


    }

    private static class UiHolder {
        TextView item;
        TextView qty;
        TextView recQty;
        TextView toOrderQty;

        private UiHolder(TextView item, TextView qty, TextView recQty, TextView toOrderQty) {
            this.item = item;
            this.qty = qty;
            this.recQty = recQty;
            this.toOrderQty = toOrderQty;
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

    private class DigitalCallback extends BaseSendDigitalReportsCallback {

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

    public static ReorderReportFragment instance() {
        return ReorderReportFragment_.builder().build();
    }
}
