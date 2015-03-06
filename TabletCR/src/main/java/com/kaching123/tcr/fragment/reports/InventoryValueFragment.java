package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
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
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.reports.InventoryValueFragment.Info;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by gdubina on 04.02.14.
 */
@EFragment(R.layout.reports_inventory_value_fragment)
@OptionsMenu(R.menu.sales_items_report)
public class InventoryValueFragment extends Fragment implements LoaderCallbacks<List<Info>> {

    private static boolean EXPORTABLE = false;

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    @ViewById(android.R.id.list)
    protected ListView listView;

    private ItemAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        listView.setAdapter(adapter = new ItemAdapter(getActivity()));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<Info>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(URI_ITEMS)
                .projection(ItemTable.TMP_AVAILABLE_QTY, ItemTable.COST)
                .wrap(new Function<Cursor, List<Info>>() {
                    @Override
                    public List<Info> apply(Cursor c) {

                        int items = 0;
                        BigDecimal qty = BigDecimal.ZERO;
                        BigDecimal cost = BigDecimal.ZERO;
                        while (c.moveToNext()) {
                            BigDecimal q = _decimalQty(c.getString(0));
                            BigDecimal cc = _decimal(c.getString(1));

                            qty = qty.add(q);
                            cost = cost.add(CalculationUtil.getSubTotal(q, cc));
                            items++;
                        }

                        ArrayList<Info> info = new ArrayList<Info>(3);
                        info.add(new Info(R.string.report_inventory_value_count, new BigDecimal(items)));
                        info.add(new Info(R.string.report_inventory_value_qty, qty));
                        info.add(new Info(R.string.report_inventory_value_value, cost));
                        return info;
                    }
                }).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Info>> listLoader, List<Info> infos) {
        adapter.changeCursor(infos);
    }

    @Override
    public void onLoaderReset(Loader<List<Info>> listLoader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem export = menu.findItem(R.id.action_export);
        assert export != null;
        export.setVisible(EXPORTABLE);
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
        SendDigitalReportsCommand.start(getActivity(), ReportType.INVENTORY_VALUE, new PrintDigitalCallback());
    }

    private void print(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        PrintReportsCommand.start(getActivity(), ignorePaperEnd, searchByMac, ReportType.INVENTORY_VALUE, new PrintCallback());
    }

    public static InventoryValueFragment instance() {
        return InventoryValueFragment_.builder().build();
    }

    private class ItemAdapter extends ObjectsCursorAdapter<Info> {

        public ItemAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), R.layout.reports_inventory_value_item_view, null);
            view.setTag(new UiHolder(
                    (TextView) view.findViewById(R.id.description),
                    (TextView) view.findViewById(R.id.price)
            ));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, Info item) {
            UiHolder holder = (UiHolder) convertView.getTag();
            holder.label.setText(item.label);
            UiHelper.showPrice(holder.value, item.value);
            return convertView;
        }

    }

    private static class UiHolder {
        TextView label;
        TextView value;

        private UiHolder(TextView label, TextView value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class Info {
        public int label;
        public BigDecimal value;

        public Info(int label, BigDecimal value) {
            this.label = label;
            this.value = value;
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

}
