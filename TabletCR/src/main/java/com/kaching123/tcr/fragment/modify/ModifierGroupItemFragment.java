package com.kaching123.tcr.fragment.modify;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.itempick.CategoryItemView_;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ModifierGroupView2.GroupTable;
import com.kaching123.tcr.store.ShopStore.ModifierGroupView;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.model.ContentValuesUtil._count;

/**
 * Created by  alboyko on 02.12.2015.
 */

@EFragment(R.layout.itempick_categories_fragment)
public class ModifierGroupItemFragment extends BaseCategoriesFragment<GroupCallback> {

    private static final Uri URI_GROUPS = ShopProvider.contentUriGroupBy(ModifierGroupView.URI_CONTENT,
            ShopSchema2.ModifierGroupView2.GroupTable.GUID);

    private final static String KEY = "Key_b";
    private final static String KEY_INI = "Key_c";

    @ViewById
    protected ListView list;

    protected boolean initial; // bound to activity lifecycle

    @Override
    protected CursorAdapter createAdapter() {
        return new ModifierGroupAdapter(getActivity());
    }

    @Override
    protected AbsListView getAdapterView() {
        return list;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        header = CategoryItemView_.build(getActivity());
        list.addHeaderView(header);
        super.onActivityCreated(savedInstanceState);
    }

    public ModifierGroupItemFragment setItemGuid(String guid) {
        Bundle b = new Bundle();
        b.putString(KEY, guid);
        b.putBoolean(KEY_INI, true);
        getLoaderManager().initLoader(0, b, this);
                getLoaderManager().restartLoader(1, b, itemCountLoader);
        return this;
    }

    @Override
    protected boolean loadImmediately() {
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        initial = bundle.containsKey(KEY_INI);
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(URI_GROUPS);
        builder.projection(ShopSchema2.ModifierGroupView2.GroupTable.ID,
                ShopSchema2.ModifierGroupView2.GroupTable.GUID,
                ShopSchema2.ModifierGroupView2.GroupTable.TITLE,
                ShopSchema2.ModifierGroupView2.GroupTable.ITEM_GUID,
                _count(ShopSchema2.ModifierGroupView2.ItemTable.MODIFIER_GUID, ModifierGroupView.ITEM_COUNT));
        builder.where(ShopSchema2.ModifierGroupView2.GroupTable.ITEM_GUID + " = ? ", bundle.getString(KEY));
        builder.orderBy(getOrderBy());
        return builder.build(getActivity());
    }

    protected void allCount(int count) {
        header.bind(null, getString(R.string.inventory_categories_all_categories), count, true);
        header.invalidate();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.changeCursor(cursor);
        if (initial) {
            checkFirstRow(cursor);
        }
        initial = false;
    }

    @Override
    protected String getOrderBy() {
        return GroupTable.ORDER_NUM;
    }

    @Override
    protected int getItemsCount(Cursor cursor) {
        int n = 0;
        while (cursor.moveToNext()){
            n += cursor.getInt(cursor.getColumnIndex(ModifierGroupView.ITEM_COUNT));
        }
        return n;
    }

    @Override
    protected void listItemClicked(int position) {
        if (listener == null)
            return;

        Cursor c = (Cursor) getAdapterView().getItemAtPosition(position);
        if (c == null){
            listener.onItemSelected(null);
        }else{
            String catGuid = c.getString(c.getColumnIndex(ShopSchema2.ModifierGroupView2.GroupTable.GUID));
            String title = c.getString(c.getColumnIndex(ShopSchema2.ModifierGroupView2.GroupTable.TITLE));
            String itemGuid = c.getString(c.getColumnIndex(ShopSchema2.ModifierGroupView2.GroupTable.ITEM_GUID));
            ModifierGroupModel itemModel = new ModifierGroupModel();
            itemModel.itemGuid = itemGuid;
            itemModel.title = title;
            itemModel.guid = catGuid;
            listener.onItemSelected(itemModel);
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> itemCountLoader = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final Uri UNI_ITEM = ShopProvider.contentUri(ModifierTable.URI_CONTENT);

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String itemGuid = bundle.getString(KEY);
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(UNI_ITEM)
                    .projection(ModifierTable.ITEM_GUID, ModifierTable.TYPE)
                    .where(ModifierTable.TYPE + " = ?", ModifierType.MODIFIER.ordinal())
                    .where(ModifierTable.ITEM_GUID + " = ?", itemGuid);
            return builder.build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> listLoader, Cursor groups) {
            allCount(groups.getCount());
        }

        @Override
        public void onLoaderReset(Loader<Cursor> listLoader) {
        }
    };
}
