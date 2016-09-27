package com.kaching123.tcr.fragment.editmodifiers;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemExtView;
import com.kaching123.tcr.store.ShopStore.ModifiersCountView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.kaching123.tcr.util.CursorUtil._selectionArgs;

/**
 * Created by vkompaniets on 11.12.13.
 */
@EFragment(R.layout.editmodifiers_copymodifiers_search_fragment)
public class SearchFragment extends Fragment {

    private static final Uri URI_ITEMS = ShopProvider.contentUri(ModifiersCountView.URI_CONTENT);

    @ViewById(android.R.id.list)
    protected StickyListHeadersListView stickyListHeadersListView;

    private String itemGuid;
    private String searchText = "";
    IItemListener listener;

    private ModifiersCopyListAdapter adapter;

    public void setListener(IItemListener listener) {
        this.listener = listener;
    }

    public void setItemGuid(String itemGuid) {
        this.itemGuid = itemGuid;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        getLoaderManager().restartLoader(0, null, loader);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ModifiersCopyListAdapter(getActivity());

        stickyListHeadersListView.setAdapter(adapter);
        stickyListHeadersListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (listener != null) {
                    ModifierCountItemModel model = (ModifierCountItemModel) adapterView.getItemAtPosition(position);
                    listener.onItemSelected(id, model.guid);
                }
            }
        });
    }

    private LoaderCallbacks<Cursor> loader = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    URI_ITEMS,
                    null,
                    null,
                    _selectionArgs("%" + searchText + "%", itemGuid == null ? "" : itemGuid),
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ItemConverter func = new ItemConverter();
            ArrayList<ModifierCountItemModel> models = new ArrayList<>(data.getCount());
            while (data.moveToNext()){
                models.add(func.apply(data));
            }
            adapter.changeCursor(models);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.changeCursor(null);
        }
    };

    private static class ItemConverter extends ListConverterFunction<ModifierCountItemModel> {
        @Override
        public ModifierCountItemModel apply(Cursor cursor) {
            super.apply(cursor);
            return new ModifierCountItemModel(
                    cursor.getString(indexHolder.get(ModifiersCountView.ITEM_GUID)),
                    cursor.getString(indexHolder.get(ModifiersCountView.ITEM_DESCRIPTION)),
                    cursor.getString(indexHolder.get(ModifiersCountView.CATEGORY_GUID)),
                    cursor.getString(indexHolder.get(ModifiersCountView.CATEGORY_TITLE)),
                    cursor.getInt(indexHolder.get(ItemExtView.MODIFIERS_COUNT)),
                    cursor.getInt(indexHolder.get(ItemExtView.ADDONS_COUNT)),
                    cursor.getInt(indexHolder.get(ItemExtView.OPTIONAL_COUNT))
            );
        }
    }

    public static class ModifierCountItemModel {
        final String guid;
        final String description;
        final String categoryId;
        final String categoryTitle;
        final int numModifiers;
        final int numAddons;
        final int numOptionals;

        public ModifierCountItemModel(String guid, String description, String categoryId, String categoryTitle, int numModifiers, int numAddons, int numOptionals) {
            this.guid = guid;
            this.description = description;
            this.categoryId = categoryId;
            this.categoryTitle = categoryTitle;
            this.numModifiers = numModifiers;
            this.numAddons = numAddons;
            this.numOptionals = numOptionals;
        }
    }

    public interface IItemListener {
        void onItemSelected(long id, String fromItem);
    }

}
