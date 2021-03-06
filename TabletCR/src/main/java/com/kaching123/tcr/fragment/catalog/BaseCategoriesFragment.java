package com.kaching123.tcr.fragment.catalog;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseQuickServiceActiviry;
import com.kaching123.tcr.fragment.inventory.CategoriesFragment;
import com.kaching123.tcr.fragment.itempick.CategoryItemView;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.CategoryView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import static com.kaching123.tcr.model.ContentValuesUtil._castAsReal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._countDistinct;

/**
 * Created by vkompaniets on 25.11.13.
 */
@EFragment
public abstract class BaseCategoriesFragment<T extends BaseCategoriesFragment.ICategoryListener> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        BaseQuickServiceActiviry.ICategoryFragmentBaseActions<T> {

    private static final Uri URI_CATEGORIES = ShopProvider.getContentUriGroupBy(CategoryView.URI_CONTENT, CategoryTable.GUID);
    private static final int CATEGORY_LOADER_ID = 0;

    protected T listener;

    protected CursorAdapter adapter;

    protected abstract CursorAdapter createAdapter();

    protected abstract AbsListView getAdapterView();

    protected CategoryItemView header;

    private boolean useOnlyNearTheEnd;
    public boolean composer;
    public boolean composition;
    public boolean reference;
    public boolean forSale;
    public boolean hasModifiers;
    public boolean serial;
    public boolean child;

    @InstanceState
    protected int selectedPosition;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAdapterView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getAdapterView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> v, View view, int pos, long id) {
                listItemClicked(pos);
            }
        });
        adapter = createAdapter();
        getAdapterView().setAdapter(adapter);
        if (loadImmediately()) {
            Logger.d("[Loader] BaseCategoriesFragment initLoader");
            getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
        }
    }

    protected void listItemClicked(int position){
        Cursor c = (Cursor) getAdapterView().getItemAtPosition(position);
        long id = -1;
        String departmentId = null;
        String categoryId = null;
        if (c != null){
            departmentId = c.getString(c.getColumnIndex(CategoryTable.DEPARTMENT_GUID));
            categoryId = c.getString(c.getColumnIndex(CategoryTable.GUID));
            id = c.getLong(c.getColumnIndex(CategoryTable.ID));
        }
        if (this.listener != null) {
            this.listener.onCategoryChanged(id, departmentId, categoryId);
        }
    }

    protected boolean loadImmediately() {
        return true;
    }


    public void setUseOnlyNearTheEnd(boolean useOnlyNearTheEnd) {
        this.useOnlyNearTheEnd = useOnlyNearTheEnd;
    }

    public void setFilter(boolean composer,
                          boolean composition,
                          boolean reference,
                          boolean forSale,
                          boolean hasModifiers,
                          boolean serial,
                          boolean child) {
        this.composer = composer;
        this.composition = composition;
        this.reference = reference;
        this.forSale = forSale;
        this.hasModifiers = hasModifiers;
        this.serial = serial;
        this.child = child;

        getLoaderManager().restartLoader(0, Bundle.EMPTY, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        boolean loadEmptyCategories = this instanceof CategoriesFragment;

        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(URI_CATEGORIES);
        builder.projection(CategoryTable.ID,
                CategoryTable.DEPARTMENT_GUID,
                CategoryTable.GUID,
                CategoryTable.TITLE,
                CategoryTable.IMAGE,
                _countDistinct(ItemTable.GUID, CategoryView.ITEM_COUNT));//_count(ItemTable.GUID, CategoryView.ITEM_COUNT));
        if (useOnlyNearTheEnd) {
            builder.where(ItemTable.STOCK_TRACKING + " = ? ", "1");
            builder.where(_castAsReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castAsReal(ItemTable.MINIMUM_QTY));
        }
        builder.where(ItemTable.IS_DELETED + " = ? ", "0");
        if (serial) {
            builder.where(ItemTable.SERIALIZABLE + " = ? ", "1");
        } else if (composer) {
            builder.where(ShopSchema2.CategoryView2.HostComposerTable.ID + " IS NOT NULL");
        } else if (composition) {
            builder.where(ShopSchema2.CategoryView2.ChildComposerTable.ID + " IS NOT NULL");
        } else if (reference) {
            builder.where(ItemTable.ITEM_REF_TYPE + " = ? ", ItemRefType.Reference.ordinal());
        } else if (child) {
            builder.where(ItemTable.REFERENCE_ITEM_ID + " IS NOT NULL OR "
                    + ShopSchema2.CategoryView2.ItemMatrixTable.PARENT_GUID + " IS NOT NULL");
        } else if (forSale) {
            builder.where(ItemTable.SALABLE + " = ? ", "0");
        }
        if (!loadEmptyCategories) {
            builder.where(ItemTable.GUID + " is not null");
        }
        if (!(this instanceof CategoriesFragment)) {
            builder.where(ItemTable.SALABLE + " = ? ", "1");
        }

        builder.orderBy(getOrderBy());
        return builder.build(getActivity());
    }

    protected String getOrderBy() {
        return CategoryTable.TITLE;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        int n = getItemsCount(cursor);
        if (header != null) {
            header.bind(null, getString(R.string.inventory_categories_all_categories), n, true);
            header.invalidate();
        }
        adapter.changeCursor(cursor);
        checkFirstRow(cursor);
    }

    protected int getItemsCount(Cursor cursor) {
        int n = 0;
        while (cursor.moveToNext()) {
            n += cursor.getInt(cursor.getColumnIndex(CategoryView.ITEM_COUNT));
        }
        return n;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }

    protected void checkFirstRow(Cursor cursor) {
        if (cursor == null || this.listener == null)
            return;

        if (getAdapterView().getCheckedItemPosition() == -1 && getAdapterView().getCount() > 0){
            AbsListView list = getAdapterView();
            ListAdapter adapter = getAdapterView().getAdapter();
            list.performItemClick(adapter.getView(0, null, null), 0, adapter.getItemId(0));
        }
    }

    public void setListener(T listener) {
        this.listener = listener;
    }

    public interface ICategoryListener {
        void onCategoryChanged(long id, String depGuid, String catGuid);
    }
}
