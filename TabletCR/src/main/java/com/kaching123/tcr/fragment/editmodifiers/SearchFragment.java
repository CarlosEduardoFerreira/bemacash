package com.kaching123.tcr.fragment.editmodifiers;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.search.CategoryItemViewModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemExtView;
import com.kaching123.tcr.store.ShopStore.SearchItemWithModifierView;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;

/**
 * Created by vkompaniets on 11.12.13.
 */
@EFragment(R.layout.editmodifiers_copymodifiers_search_fragment)
public class SearchFragment extends Fragment implements LoaderCallbacks<List<CategoryItemViewModel>> {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SearchItemWithModifierView.URI_CONTENT);

    @ViewById(android.R.id.list)
    protected StickyListHeadersListView stickyListHeadersListView;

    IItemListener listener;

    private String searchText = "";

    public void setListener(IItemListener listener) {
        this.listener = listener;
    }

    public void setSearchText(String searchText) {
        setListAdapter(null);
        this.searchText = searchText;
        stickyListHeadersListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (listener != null) {
                    listener.onItemSelected(id, (CategoryItemViewModel) adapterView.getItemAtPosition(position));
                }
            }
        });
        getLoaderManager().restartLoader(0, null, this);
    }

    private void setListAdapter(StickyListHeadersAdapter adapter) {
        stickyListHeadersListView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<List<CategoryItemViewModel>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(URI_ITEMS)
                .where("", "%" + searchText + "%")
                .transform(new ItemConverter()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<CategoryItemViewModel>> listLoader, List<CategoryItemViewModel> categoryItemViewModels) {
        setListAdapter(new ModifiersCopyListAdapter(getActivity(), categoryItemViewModels));
    }

    @Override
    public void onLoaderReset(Loader<List<CategoryItemViewModel>> listLoader) {
        setListAdapter(null);
    }

    private static class ItemConverter extends ListConverterFunction<CategoryItemViewModel> {

        @Override
        public CategoryItemViewModel apply(Cursor c) {
            super.apply(c);
            return new CategoryItemViewModel(
                    c.getString(indexHolder.get(SearchItemWithModifierView.GUID)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.CATEGORY_ID)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.DESCRIPTION)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.CODE)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.EAN_CODE)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.PRODUCT_CODE)),
                    PriceType.valueOf(c.getInt(indexHolder.get(SearchItemWithModifierView.PRICE_TYPE))),
                    _decimal(c.getString(indexHolder.get(SearchItemWithModifierView.SALE_PRICE))),
                    _decimalQty(c.getString(indexHolder.get(SearchItemWithModifierView.QUANTITY))),
                    c.getString(indexHolder.get(SearchItemWithModifierView.UNITS_LABEL)),
                    c.getInt(indexHolder.get(SearchItemWithModifierView.STOCK_TRACKING)) == 1,
                    c.getInt(indexHolder.get(SearchItemWithModifierView.ACTIVE_STATUS)) == 1,
                    c.getInt(indexHolder.get(SearchItemWithModifierView.DISCOUNTABLE)) == 1,
                    _decimal(c.getString(indexHolder.get(SearchItemWithModifierView.DISCOUNT))),
                    _discountType(c, indexHolder.get(SearchItemWithModifierView.DISCOUNT_TYPE)),
                    c.getInt(indexHolder.get(SearchItemWithModifierView.TAXABLE)) == 1,
                    c.getString(indexHolder.get(SearchItemWithModifierView.TAX_GROUP_GUID)),
                    c.getInt(indexHolder.get(ItemExtView.MODIFIERS_COUNT)),
                    c.getInt(indexHolder.get(ItemExtView.ADDONS_COUNT)),
                    c.getInt(indexHolder.get(ItemExtView.OPTIONAL_COUNT)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.DEPARTMENT_ID)),
                    c.getString(indexHolder.get(SearchItemWithModifierView.CATEGORY_TITLE)),
                    _decimal(c.getString(indexHolder.get(SearchItemWithModifierView.TAX))),
                    c.getString(c.getColumnIndex(SearchItemWithModifierView.DEFAULT_MODIFIER_GUID)),
                    c.getInt(indexHolder.get(SearchItemWithModifierView.ORDER_NUM)),
                    c.getString(c.getColumnIndex(SearchItemWithModifierView.PRINTER_ALIAS_GUID)),
                    c.getInt(indexHolder.get(SearchItemWithModifierView.BUTTON_VIEW)),
                    c.getInt(indexHolder.get(SearchItemWithModifierView.HASNOTES)) == 1,
                    c.getInt(indexHolder.get(SearchItemWithModifierView.SERIALIZABLE)) == 1,
                    _codeType(c, indexHolder.get(SearchItemWithModifierView.CODE_TYPE)),
                    _bool(c, c.getColumnIndex(SearchItemWithModifierView.ELIGIBLE_FOR_COMMISSION)),
                    _decimal(c, c.getColumnIndex(SearchItemWithModifierView.COMMISSION))
            );
        }
    }

    public static interface IItemListener {
        void onItemSelected(long id, ItemExModel model);
    }

}
