package com.kaching123.tcr.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TaxHelper;
import com.kaching123.tcr.fragment.taxgroup.TaxGroupDialog;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.util.List;

@EActivity(R.layout.tax_groups_activity)
public class TaxGroupsEcuadorActivity extends TaxGroupsActivity {

    private static final Uri URI_TAX_GROUPS = ShopProvider.getContentUri(ShopStore.TaxGroupTable.URI_CONTENT);

    private DefaultTaxGroupsLoader defaultTaxGroupsLoader = new DefaultTaxGroupsLoader();

    private int defaultGroupCount = 0;

    protected TaxGroupsAdapter getAdapter() {
        return new TaxGroupsEcuadorAdapter(this);
    }

    @AfterViews
    protected void init() {
        super.init();
        getSupportLoaderManager().restartLoader(1, null, defaultTaxGroupsLoader);
    }

    @Override
    protected void showTaxGroupDialog(TaxGroupModel model) {
        TaxGroupDialog.show(this, model, defaultGroupCount < TaxHelper.MAX_TAX_GROUP_COUNT, changeListener);
    }

    @Override
    protected void onGroupDeleted() {
        super.onGroupDeleted();
        getSupportLoaderManager().restartLoader(1, null, defaultTaxGroupsLoader);
    }

    protected void onGroupUpdated() {
        super.onGroupUpdated();
        getSupportLoaderManager().restartLoader(1, null, defaultTaxGroupsLoader);
    }

    private class TaxGroupsEcuadorAdapter extends TaxGroupsAdapter {

        public TaxGroupsEcuadorAdapter(Context context) {
            super(context);
        }

        @Override
        protected void display(ViewHolder holder, TaxGroupModel item) {
            super.display(holder, item);
            holder.title.setText(item.isDefault ? item.title + " (Default)" : item.title);
        }
    }

    private class DefaultTaxGroupsLoader implements LoaderCallbacks<List<TaxGroupModel>> {

        @Override
        public Loader<List<TaxGroupModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_TAX_GROUPS)
                    .where(ShopStore.TaxGroupTable.IS_DEFAULT + " = ? ", 1)
                    .transform(new TaxGroupConverter())
                    .build(TaxGroupsEcuadorActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<TaxGroupModel>> listLoader, List<TaxGroupModel> list) {
            defaultGroupCount = list != null ? list.size() : 0;
        }

        @Override
        public void onLoaderReset(Loader<List<TaxGroupModel>> listLoader) {
            defaultGroupCount = 0;
        }
    }


    public static void start(Context context) {
        TaxGroupsEcuadorActivity_.intent(context).start();
    }

}
