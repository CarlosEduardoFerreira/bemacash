package com.kaching123.tcr.fragment.item;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.VariantsActivity;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.VariantSubItemsCountView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.util.CursorUtil._selectionArgs;

/**
 * Created by vkompaniets on 10.08.2016.
 */
@EFragment(R.layout.item_variants_fragment)
public class ItemVariantsFragment extends ItemBaseFragment {

    private static final Uri URI_VARIANT_SUB_ITEMS_COUNT = ShopProvider.contentUri(VariantSubItemsCountView.URI_CONTENT);

    @ViewById protected ImageButton editVariants;
    @ViewById protected View lineSeparator;
    @ViewById protected ListView list;

    private VariantsAdapter variantsAdapter;

    @Override
    protected void setViews() {
        editVariants.setEnabled(!getItemProvider().isCreate());

        variantsAdapter = new VariantsAdapter(getActivity());
        list.setAdapter(variantsAdapter);

        getLoaderManager().restartLoader(0, null, variantsLoaderCallbacks);
    }

    @Override
    protected void setModel() {

    }

    @Override
    public boolean validateData() {
        return true;
    }

    @Override
    public void collectData() {

    }

    @Click
    protected void editVariantsClicked(){
        VariantsActivity.start(getActivity(), getModel());
    }

    private final LoaderManager.LoaderCallbacks<Cursor> variantsLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
            return new CursorLoader(getActivity(),
                    URI_VARIANT_SUB_ITEMS_COUNT,
                    null,
                    null,
                    _selectionArgs(getModel().guid, TcrApplication.get().getShopId()),
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            variantsAdapter.swapCursor(cursor);
            lineSeparator.setVisibility(cursor.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            variantsAdapter.swapCursor(null);
        }
    };

    private static class VariantsAdapter extends ResourceCursorAdapter {
        private int varItemNameIdx, varSubItemCountIdx;

        public VariantsAdapter(Context context) {
            super(context, R.layout.variant_item, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            if (cursor != null) {
                varItemNameIdx = cursor.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_NAME);
                varSubItemCountIdx = cursor.getColumnIndex(ShopStore.VariantsView.VARIANT_SUB_ITEMS_COUNT);
            }
            View view = super.newView(context, cursor, parent);
            view.setTag(new VariantItemHolder(view));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            VariantItemHolder variantItemHolder = (VariantItemHolder) view.getTag();
            variantItemHolder.name.setText(cursor.getString(varItemNameIdx));
            variantItemHolder.count.setText(cursor.getString(varSubItemCountIdx));
        }

        private static class VariantItemHolder {
            TextView name;
            TextView count;

            VariantItemHolder(View v) {
                name = (TextView) v.findViewById(android.R.id.text1);
                count = (TextView) v.findViewById(android.R.id.text2);
            }
        }
    }
}
