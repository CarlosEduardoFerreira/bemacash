package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.kaching123.tcr.commands.store.export.ExportReportsCommand;
import com.kaching123.tcr.commands.store.export.ExportReportsCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.reports.InventoryStatusReportQuery;
import com.kaching123.tcr.reports.InventoryStatusReportQuery.DepInfo;
import com.kaching123.tcr.reports.InventoryStatusReportQuery.DepInfoWrapFunction;
import com.kaching123.tcr.reports.InventoryStatusReportQuery.ItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.showQuantityInteger;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by vkompaniets on 04.03.14.
 */
@EFragment(R.layout.reports_inventory_status_fragment)
@OptionsMenu(R.menu.inventory_status_report_fragment)
public class InventoryStatusReportFragment extends SuperBaseFragment {

    @ViewById
    protected Spinner departmentSpinner;

    @ViewById(android.R.id.list)
    protected ListView listView;

    private ItemsAdapter itemsAdapter;

    private DepartmentAdapter departmentAdapter;

    private String depGuid;
    private ExportCommandBaseCallback exportCallback = new ExportCallback();

    public static InventoryStatusReportFragment instance() {
        return InventoryStatusReportFragment_.builder().build();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        departmentAdapter = new DepartmentAdapter(getActivity());
        departmentSpinner.setAdapter(departmentAdapter);
        departmentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setDepartment(departmentAdapter.getItem(i)[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        getLoaderManager().initLoader(0, null, new DepartmentsLoader());

        itemsAdapter = new ItemsAdapter(getActivity());
        listView.setAdapter(itemsAdapter);
    }

    private void setDepartment(String depGuid) {
        this.depGuid = depGuid;
        getLoaderManager().restartLoader(0, null, new ItemsLoader()).forceLoad();
    }

    private static class HeaderRow {
        String name;

        private HeaderRow(String name) {
            this.name = name;
        }
    }

    private static class TotalRow {
        BigDecimal total;

        private TotalRow(BigDecimal total) {
            this.total = total;
        }
    }

    @OptionsItem
    protected void actionExportSelected() {
        FileChooserFragment.show(getActivity(), Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(getActivity(), getString(R.string.inventory_export_wait_msg));
                ExportReportsCommand.start(getActivity(), file.getAbsolutePath(), ReportType.INVENTORY_STATUS, 0L, 0L, 0L, null, depGuid, exportCallback);
            }
        });
    }

    @OptionsItem
    protected void actionPrintSelected(){
        AlertDialogFragment.showConfirmationNoImage(getActivity(), R.string.warning_dialog_title, getApp().getString(R.string.inventory_status_report_print_warning_msg), new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                printReport(false, false);
                return true;
            }
        });
    }

    @OptionsItem
    protected void actionEmailSelected() {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_email));
        SendDigitalReportsCommand.start(getActivity(), ReportType.INVENTORY_STATUS, 0L, 0L, depGuid, new PrintDigitalCallback());
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

    private void printReport(boolean ingorePaperWanring, boolean searchByMac){
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        PrintReportsCommand.start(getActivity(), ingorePaperWanring, searchByMac, ReportType.INVENTORY_STATUS_POS, 0L, 0L, depGuid, new PrintCallback());
    }

    protected class PrintCallback extends BasePrintCallback {

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printReport(ignorePaperEnd, searchByMac);
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

    private class ItemsLoader implements LoaderCallbacks<List<Object>> {

        @Override
        public Loader<List<Object>> onCreateLoader(int i, Bundle bundle) {
            return new AsyncTaskLoader<List<Object>>(getActivity()) {
                @Override
                public List<Object> loadInBackground() {
                    List<DepInfo> depInfos = _wrap(InventoryStatusReportQuery
                            .syncQuery(depGuid)
                            .perform(getContext()),
                            new DepInfoWrapFunction());

                    ArrayList<Object> rows = new ArrayList<Object>();
                    for (DepInfo depInfo : depInfos) {
                        rows.add(new HeaderRow(depInfo.title));
                        rows.addAll(depInfo.items);
                        rows.add(new TotalRow(depInfo.totalCost));
                    }
                    return rows;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Object>> listLoader, List<Object> objects) {
            itemsAdapter.changeCursor(objects);
        }

        @Override
        public void onLoaderReset(Loader<List<Object>> listLoader) {
            itemsAdapter.changeCursor(null);
        }
    }

    private class ItemsAdapter extends ObjectsCursorAdapter<Object> {

        public ItemsAdapter(Context context) {
            super(context);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            Class clazz = getItem(position).getClass();
            if (clazz == HeaderRow.class) {
                return 0;
            } else if (clazz == TotalRow.class) {
                return 2;
            }
            return 1;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            int type = getItemViewType(position);
            View view;
            if (type == 0) {
                view = View.inflate(getContext(), R.layout.reports_inventory_status_item0_view, null);
                UIHolderHeader header = new UIHolderHeader();
                header.title = (TextView) view.findViewById(R.id.name);
                view.setTag(header);
            } else if (type == 2) {
                view = View.inflate(getContext(), R.layout.reports_inventory_status_item2_view, null);
                UIHolderTotal total = new UIHolderTotal();
                total.total = (TextView) view.findViewById(R.id.total);
                view.setTag(total);
            } else {
                view = View.inflate(getContext(), R.layout.reports_inventory_status_item1_view, null);
                UIHolderItem item = new UIHolderItem();
                item.title = (TextView) view.findViewById(R.id.title);
                item.ean = (TextView) view.findViewById(R.id.ean_code);
                item.productCode = (TextView) view.findViewById(R.id.product_code);
                item.onHand = (TextView) view.findViewById(R.id.on_hand);
                item.unitCost = (TextView) view.findViewById(R.id.unit_cost);
                item.totalCost = (TextView) view.findViewById(R.id.total_cost);
                item.active = (ImageView) view.findViewById(R.id.active);
                view.setTag(item);
            }
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, Object item) {
            int type = getItemViewType(position);
            if (type == 0) {
                UIHolderHeader holder = (UIHolderHeader) convertView.getTag();
                HeaderRow header = (HeaderRow) item;
                holder.title.setText(header.name);
            } else if (type == 2) {
                UIHolderTotal holder = (UIHolderTotal) convertView.getTag();
                TotalRow total = (TotalRow) item;
                showPrice(holder.total, total.total);
            } else {
                UIHolderItem holder = (UIHolderItem) convertView.getTag();
                ItemInfo itemInfo = (ItemInfo) item;
                holder.title.setText(itemInfo.title);
                holder.ean.setText(itemInfo.ean);
                holder.productCode.setText(itemInfo.productCode);
                showQuantityInteger(holder.onHand, itemInfo.onHand);
                showPrice(holder.unitCost, itemInfo.unitCost);
                showPrice(holder.totalCost, itemInfo.totalCost);
                holder.active.setVisibility(itemInfo.active ? View.VISIBLE : View.INVISIBLE);
            }

            return convertView;
        }
    }

    private static class UIHolderHeader {
        TextView title;
    }

    private static class UIHolderTotal {
        TextView total;
    }

    private static class UIHolderItem {
        TextView title;
        TextView ean;
        TextView productCode;
        TextView onHand;
        TextView unitCost;
        TextView totalCost;
        ImageView active;
    }

    private class DepartmentsLoader implements LoaderCallbacks<List<String[]>> {

        private final Uri URI_DEPARTMENT = ShopProvider.getContentUri(DepartmentTable.URI_CONTENT);

        @Override
        public Loader<List<String[]>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(URI_DEPARTMENT)
                    .projection(DepartmentTable.GUID, DepartmentTable.TITLE)
                    .transform(new Function<Cursor, String[]>() {

                        @Override
                        public String[] apply(Cursor cursor) {
                            return new String[]{cursor.getString(0), cursor.getString(1)};
                        }
                    })
                    .wrap(new Function<List<String[]>, List<String[]>>() {

                        @Override
                        public List<String[]> apply(List<String[]> strings) {
                            ArrayList<String[]> result = new ArrayList<String[]>(strings.size() + 1);
                            result.add(new String[]{null, getString(R.string.register_label_all)});
                            result.addAll(strings);
                            return result;
                        }
                    })
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<String[]>> listLoader, List<String[]> strings) {
            departmentAdapter.changeCursor(strings);
        }

        @Override
        public void onLoaderReset(Loader<List<String[]>> listLoader) {
            departmentAdapter.changeCursor(null);
        }
    }

    private static class DepartmentAdapter extends ObjectsCursorAdapter<String[]> {

        public DepartmentAdapter(Context context) {
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
        protected View bindView(View view, int position, String[] item) {
            ((TextView) view).setText(item[1]);
            return view;
        }
    }
}
