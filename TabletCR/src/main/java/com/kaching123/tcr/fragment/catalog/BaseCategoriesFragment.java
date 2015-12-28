package com.kaching123.tcr.fragment.catalog;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.inventory.CategoriesFragment;
import com.kaching123.tcr.fragment.itempick.CategoryItemView;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.CategoryView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import static com.kaching123.tcr.model.ContentValuesUtil._castToReal;
import static com.kaching123.tcr.model.ContentValuesUtil._count;

/**
 * Created by vkompaniets on 25.11.13.
 */
@EFragment
public abstract class BaseCategoriesFragment<T extends BaseCategoriesFragment.ICategoryListener> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final Uri URI_CATEGORIES = ShopProvider.getContentUriGroupBy(CategoryView.URI_CONTENT, CategoryTable.GUID);

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
                loadByPosition(v, pos);
            }
        });
        getAdapterView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
                getAdapterView().performItemClick(v, pos, id);
                return true;
            }
        });
        adapter = createAdapter();
        getAdapterView().setAdapter(adapter);
        if (loadImmediately()) {
            Logger.d("[Loader] BaseCategoriesFragment initLoader");
            getLoaderManager().initLoader(0, null, this);
        }
    }

    private void loadByPosition(AdapterView<?> v, int pos){
        selectedPosition = pos;
        if (pos == 0 && isListViewWithHeader(v)) {
            headerItemClicked();
        } else {
            categoryItemClicked((Cursor) v.getItemAtPosition(pos));
        }
    }

    protected boolean loadImmediately() {
        return true;
    }

    protected void headerItemClicked() {
        if (this.listener != null) {
            this.listener.onCategoryChanged(AdapterView.INVALID_POSITION, null, null);
        }
    }

    protected void categoryItemClicked(Cursor c) {
        String depGuid = c.getString(c.getColumnIndex(CategoryTable.DEPARTMENT_GUID));
        String catGuid = c.getString(c.getColumnIndex(CategoryTable.GUID));
        long id = c.getLong(c.getColumnIndex(CategoryTable.ID));
        if (this.listener != null) {
            this.listener.onCategoryChanged(id, depGuid, catGuid);
        }
    }

    public void setUseOnlyNearTheEnd(boolean useOnlyNearTheEnd) {
        this.useOnlyNearTheEnd = useOnlyNearTheEnd;
        getLoaderManager().restartLoader(0, null, this);
    }

    public void setFilter(boolean composer,
                          boolean composition,
                          boolean reference,
                          boolean forSale,
                          boolean hasModifiers,
                          boolean serial) {
        this.composer = composer;
        this.composition = composition;
        this.reference = reference;
        this.forSale = forSale;
        this.hasModifiers = hasModifiers;
        this.serial = serial;

        getLoaderManager().restartLoader(0, Bundle.EMPTY, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        boolean loadEmptyCategories = this instanceof CategoriesFragment;
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(URI_CATEGORIES);
        builder.projection(CategoryTable.ID, CategoryTable.DEPARTMENT_GUID,
                CategoryTable.GUID,
                CategoryTable.TITLE,
                CategoryTable.IMAGE,
                _count(ItemTable.GUID, CategoryView.ITEM_COUNT));
        if (useOnlyNearTheEnd){
            builder.where(ItemTable.STOCK_TRACKING + " = ? ", "1");
            builder.where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castToReal(ItemTable.MINIMUM_QTY));
    }
        if (!loadEmptyCategories){
            builder.where(ItemTable.GUID + " is not null");
        }

        if (forSale) {
            builder.where(ItemTable.SALABLE + " = ? ", "0");
        }

        if (!(this instanceof CategoriesFragment)){
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
        if (header != null){
            header.bind(null, getString(R.string.inventory_categories_all_categories), n, true);
            header.invalidate();
        }
        adapter.changeCursor(cursor);
        checkFirstRow(cursor);
    }

    protected int getItemsCount(Cursor cursor) {
        int n = 0;
        while (cursor.moveToNext()){
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

        int checkedPosition = selectedPosition;
        if (checkedPosition < 0 || checkedPosition >= adapter.getCount())
            checkedPosition = 0;
        if(isListViewWithHeader(getAdapterView()) || cursor.getCount() > 0){
            getAdapterView().setItemChecked(checkedPosition, true);
            loadByPosition(getAdapterView(), checkedPosition);
        }else {
            getAdapterView().setItemChecked(AdapterView.INVALID_POSITION, true);
        }
    }

    private static boolean isListViewWithHeader(AdapterView view) {
        return view instanceof ListView && ((ListView) view).getHeaderViewsCount() > 0;
    }

    public void setListener(T listener) {
        this.listener = listener;
    }

    public interface ICategoryListener {
        void onCategoryChanged(long id, String depGuid, String catGuid);
    }
}
