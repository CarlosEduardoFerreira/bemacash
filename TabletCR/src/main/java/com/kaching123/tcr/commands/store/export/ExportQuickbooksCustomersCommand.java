package com.kaching123.tcr.commands.store.export;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkabakov on 06.03.14.
 */
public class ExportQuickbooksCustomersCommand extends ExportCursorToFileCommand {

    private static final Uri CUSTOMERS_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    @Override
    protected String getFileName() {
        return "Customers_Quickbooks_Export";
    }

    @Override
    protected Cursor query() {
        return createQuery()
                .perform(getContext());
    }

    protected Query createQuery() {
        return ProviderAction
                .query(CUSTOMERS_URI)
                .projection(
                        CustomerTable.FISRT_NAME,
                        CustomerTable.LAST_NAME,
                        CustomerTable.EMAIL,
                        CustomerTable.PHONE,
                        CustomerTable.STREET,
                        CustomerTable.CITY,
                        CustomerTable.STATE,
                        CustomerTable.ZIP,
                        CustomerTable.COUNTRY
                );
    }

    @Override
    protected List<Object> readRow(Cursor c) {
        ArrayList<Object> columns = new ArrayList<Object>(8);

        String fullName = c.getString(c.getColumnIndex(CustomerTable.FISRT_NAME)) + " " + c.getString(c.getColumnIndex(CustomerTable.FISRT_NAME));
        columns.add(fullName);
        columns.add(c.getString(c.getColumnIndex(CustomerTable.EMAIL)));
        columns.add(c.getString(c.getColumnIndex(CustomerTable.PHONE)));
        columns.add(c.getString(c.getColumnIndex(CustomerTable.STREET)));
        columns.add(c.getString(c.getColumnIndex(CustomerTable.CITY)));
        columns.add(c.getString(c.getColumnIndex(CustomerTable.STATE)));
        columns.add(c.getString(c.getColumnIndex(CustomerTable.ZIP)));
        columns.add(c.getString(c.getColumnIndex(CustomerTable.COUNTRY)));

        return columns;
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Name",
                "Email",
                "Phone",
                "Street",
                "City",
                "State",
                "ZIP",
                "Country"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return new CellProcessor[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        };
    }

    public static void start(Context context, String fileName, ExportCommandBaseCallback callback) {
        create(ExportQuickbooksCustomersCommand.class).arg(ARG_FILENAME, fileName).callback(callback).queueUsing(context);
    }

    public static abstract class ExportCommandBaseCallback {
        @OnSuccess(ExportQuickbooksCustomersCommand.class)
        public void onSuccess(@Param(RESULT_COUNT) int count) {
            handleSuccess(count);
        }

        protected abstract void handleSuccess(int count);

        @OnFailure(ExportQuickbooksCustomersCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleFailure();
    }

}
