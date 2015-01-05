package com.kaching123.tcr.fragment.filter;

/**
 * Created by gdubina on 27/02/14.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.SpinnerAdapter;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;

public class CashierFilterSpinnerAdapter extends SpinnerAdapter implements LoaderCallbacks<Cursor> {

    private static final Uri CASHIER_URI = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

    private MatrixCursor defaultItemCursor = new MatrixCursor(new String[]{EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME, EmployeeTable.GUID});
    public static final String DEFAULT_ITEM_GUID = "-1";

    private Context context;

    public CashierFilterSpinnerAdapter(Context context) {
        super(context,
                R.layout.spinner_item_filter,
                new String[]{EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME},
                new int[]{android.R.id.text1},
                R.layout.spinner_dropdown_item);

        this.context = context;
        defaultItemCursor.addRow(new String[]{context.getString(R.string.orders_history_default_item_description), "", DEFAULT_ITEM_GUID});
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView fullName = (TextView) view;
        fullName.setText(concatFullname(cursor.getString(0), cursor.getString(1)));
    }

    @Override
    protected String getIdColumnName() {
        return EmployeeTable.GUID;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        Cursor mergeCursor = (cursor == null ? null : new MergeCursor(new Cursor[]{defaultItemCursor, cursor}));
        super.changeCursor(mergeCursor);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(CASHIER_URI)
                .projection(new String[]{EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME, EmployeeTable.GUID})
                .build(context);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        this.changeCursor(null);
    }
}