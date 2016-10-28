package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.model.EmployeeForReportsModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.List;

/**
 * Created by gdubina on 23.01.14.
 */
public abstract class MangerNamesLoader implements LoaderCallbacks<List<EmployeeForReportsModel>> {

    private static final Uri EMPLOYEE_URI = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    @Override
    public Loader<List<EmployeeForReportsModel>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder
                .forUri(EMPLOYEE_URI)
                .orderBy(ShopStore.EmployeeTable.LOGIN)
                .transformRow(new EmployeesFunction())
                .build(getLoaderContext());
    }

    protected abstract Context getLoaderContext();

    private static class EmployeesFunction extends ListConverterFunction<EmployeeForReportsModel> {

        @Override
        public EmployeeForReportsModel apply(Cursor cursor) {
            super.apply(cursor);
            return new EmployeeForReportsModel(cursor);
        }
    }
}
