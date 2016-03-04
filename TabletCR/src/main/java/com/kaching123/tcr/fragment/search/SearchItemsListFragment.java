package com.kaching123.tcr.fragment.search;

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
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ModifierTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.ItemExtView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._caseCount;
import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;

@EFragment(R.layout.search_items_list_fragment)
public class SearchItemsListFragment extends Fragment implements LoaderCallbacks<List<CategoryItemViewModel>> {

    private static final Uri URI_ITEMS = ShopProvider.getContentUriGroupBy(ItemExtView.URI_CONTENT, ItemTable.GUID);

    @ViewById(android.R.id.list)
    protected StickyListHeadersListView stickyListHeadersListView;

    private String searchText;

    private IItemListener listener;

    private StickyItemsAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("ItemsListFragment onActivityCreated");
        stickyListHeadersListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> v, View arg1, int pos, long id) {
                if (SearchItemsListFragment.this.listener != null) {
                    ItemExModel model = (ItemExModel) v.getItemAtPosition(pos);
                    SearchItemsListFragment.this.listener.onItemSelected(id, model);
                    //hideKeyboard(getActivity());
                }
            }
        });
        stickyListHeadersListView.setAdapter(adapter = new StickyItemsAdapter(getActivity()));
    }

    public void setSearchText(String searchText) {
        adapter.changeCursor(null);
        this.searchText = searchText;
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<List<CategoryItemViewModel>> onCreateLoader(int loaderId, Bundle args) {
        Logger.d("ItemsListFragment onCreateLoader");
        return CursorLoaderBuilder.forUri(URI_ITEMS)
                .projection(ItemConverter.PROJECTION)
                .where(ItemTable.ACTIVE_STATUS + " = ? AND " + ItemTable.SALABLE + " = ? AND ( " + ItemTable.DESCRIPTION
                                + " like ?" + " OR " + ItemTable.PRODUCT_CODE + " like ? )", 1, 1,
                        "%" + searchText + "%", "%" + searchText + "%")
                .orderBy(ItemTable.CATEGORY_ID + ", " + ItemTable.ORDER_NUM)
                .transform(new ItemConverter()).build(getActivity());

    }

    @Override
    public void onLoadFinished(Loader<List<CategoryItemViewModel>> loader, List<CategoryItemViewModel> list) {
        adapter.changeCursor(list);
    }

    @Override
    public void onLoaderReset(Loader<List<CategoryItemViewModel>> loader) {
        adapter.changeCursor(null);
    }

    public void setListener(IItemListener listener) {
        this.listener = listener;
    }

    public static interface IItemListener {
        void onItemSelected(long id, ItemExModel model);
    }

    private static class ItemConverter extends ListConverterFunction<CategoryItemViewModel> {

        public static String[] PROJECTION = new String[]{
                ItemTable.GUID,
                ItemTable.CATEGORY_ID,
                ItemTable.DESCRIPTION,
                ItemTable.CODE,
                ItemTable.EAN_CODE,
                ItemTable.PRODUCT_CODE,
                ItemTable.PRICE_TYPE,
                ItemTable.SALE_PRICE,
                ItemTable.TMP_AVAILABLE_QTY,
                ItemTable.UNITS_LABEL,
                ItemTable.UNIT_LABEL_ID,
                ItemTable.STOCK_TRACKING,
                ItemTable.ACTIVE_STATUS,
                ItemTable.DISCOUNTABLE,
                ItemTable.SALABLE,
                ItemTable.DISCOUNT,
                ItemTable.DISCOUNT_TYPE,
                ItemTable.TAXABLE,
                ItemTable.TAX_GROUP_GUID,
                _caseCount(ModifierTable.TYPE, ModifierType.MODIFIER, ItemExtView.MODIFIERS_COUNT),
                _caseCount(ModifierTable.TYPE, ModifierType.ADDON, ItemExtView.ADDONS_COUNT),
                _caseCount(ModifierTable.TYPE, ModifierType.OPTIONAL, ItemExtView.OPTIONAL_COUNT),
                CategoryTable.DEPARTMENT_GUID,
                CategoryTable.TITLE,
                TaxGroupTable.TAX,
                ItemTable.IS_DELETED,
                ItemTable.DEFAULT_MODIFIER_GUID,
                ItemTable.ORDER_NUM,
                ItemTable.PRINTER_ALIAS_GUID,
                ItemTable.BUTTON_VIEW,
                ItemTable.HAS_NOTES,
                ItemTable.SERIALIZABLE,
                ItemTable.CODE_TYPE,
                ItemTable.ELIGIBLE_FOR_COMMISSION,
                ItemTable.COMMISSION,
                ItemTable.REFERENCE_ITEM_ID,
                ItemTable.ITEM_REF_TYPE
        };

        @Override
        public CategoryItemViewModel apply(Cursor c) {
            super.apply(c);
            return new CategoryItemViewModel(
                    c.getString(indexHolder.get(ItemTable.GUID)),
                    c.getString(indexHolder.get(ItemTable.CATEGORY_ID)),
                    c.getString(indexHolder.get(ItemTable.DESCRIPTION)),
                    c.getString(indexHolder.get(ItemTable.CODE)),
                    c.getString(indexHolder.get(ItemTable.EAN_CODE)),
                    c.getString(indexHolder.get(ItemTable.PRODUCT_CODE)),
                    PriceType.valueOf(c.getInt(indexHolder.get(ItemTable.PRICE_TYPE))),
                    _decimal(c.getString(indexHolder.get(ItemTable.SALE_PRICE))),
                    _decimalQty(c.getString(indexHolder.get(ItemTable.TMP_AVAILABLE_QTY))),
                    c.getString(indexHolder.get(ItemTable.UNITS_LABEL)),
                    c.getString(indexHolder.get(ItemTable.UNIT_LABEL_ID)),
                    c.getInt(indexHolder.get(ItemTable.STOCK_TRACKING)) == 1,
                    c.getInt(indexHolder.get(ItemTable.ACTIVE_STATUS)) == 1,
                    c.getInt(indexHolder.get(ItemTable.DISCOUNTABLE)) == 1,
                    c.getInt(indexHolder.get(ItemTable.SALABLE)) == 1,
                    _decimal(c.getString(indexHolder.get(ItemTable.DISCOUNT))),
                    _discountType(c, indexHolder.get(ItemTable.DISCOUNT_TYPE)),
                    c.getInt(indexHolder.get(ItemTable.TAXABLE)) == 1,
                    c.getString(indexHolder.get(ItemTable.TAX_GROUP_GUID)),
                    c.getString(indexHolder.get(ItemTable.TAX_GROUP_GUID2)),
                    c.getInt(indexHolder.get(ItemExtView.MODIFIERS_COUNT)),
                    c.getInt(indexHolder.get(ItemExtView.ADDONS_COUNT)),
                    c.getInt(indexHolder.get(ItemExtView.OPTIONAL_COUNT)),
                    c.getString(indexHolder.get(CategoryTable.DEPARTMENT_GUID)),
                    c.getString(indexHolder.get(CategoryTable.TITLE)),
                    _decimal(c.getString(indexHolder.get(TaxGroupTable.TAX))),
                    c.getString(c.getColumnIndex(ItemTable.DEFAULT_MODIFIER_GUID)),
                    c.getInt(indexHolder.get(ItemTable.ORDER_NUM)),
                    c.getString(c.getColumnIndex(ItemTable.PRINTER_ALIAS_GUID)),
                    c.getInt(indexHolder.get(ItemTable.BUTTON_VIEW)),
                    c.getInt(indexHolder.get(ItemTable.HAS_NOTES)) == 1,
                    c.getInt(indexHolder.get(ItemTable.SERIALIZABLE)) == 1,
                    _codeType(c, indexHolder.get(ItemTable.CODE_TYPE)),
                    _bool(c, c.getColumnIndex(ItemTable.ELIGIBLE_FOR_COMMISSION)),
                    _decimal(c, c.getColumnIndex(ItemTable.COMMISSION)),
                    c.getString(c.getColumnIndex(ItemTable.REFERENCE_ITEM_ID)),
                    ItemRefType.valueOf(c.getInt(indexHolder.get(ItemTable.ITEM_REF_TYPE))));
        }
    }
}
