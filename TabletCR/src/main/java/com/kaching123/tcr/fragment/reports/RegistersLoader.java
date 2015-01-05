package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.RegisterTable;

import java.util.List;

/**
 * Created by gdubina on 23.01.14.
 */
public abstract class RegistersLoader implements LoaderCallbacks<List<RegisterModel>> {

    private static final Uri REGISTERS_URI = ShopProvider.getContentUri(RegisterTable.URI_CONTENT);

    @Override
    public Loader<List<RegisterModel>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder
                .forUri(REGISTERS_URI)
                .orderBy(RegisterTable.TITLE)
                .transform(new RegistersFunction())
                .build(getLoaderContext());
    }

    protected abstract Context getLoaderContext();

    private static class RegistersFunction extends ListConverterFunction<RegisterModel> {

        @Override
        public RegisterModel apply(Cursor cursor) {
            super.apply(cursor);
            return new RegisterModel(cursor);
        }
    }
}
