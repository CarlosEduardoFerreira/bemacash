package com.kaching123.tcr.fragment.catalog;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.activity.BaseCashierActivity.IPriceLevelListener;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.ItemExtView;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._lower;

/**
 * Created by gdubina on 25/11/13.
 */
@EFragment
public abstract class BaseItemsPickFragment extends Fragment implements IPriceLevelListener, LoaderCallbacks<List<ItemExModel>> {

    protected static final Uri URI_ITEMS = ShopProvider.contentUriGroupBy(ItemExtView.URI_CONTENT, ItemTable.GUID);

    private static final int BASE_ITEMS_LOADER_ID = 0;

    protected String categoryGuid;

    protected ObjectsCursorAdapter<ItemExModel> adapter;

    protected abstract ObjectsCursorAdapter<ItemExModel> createAdapter();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = createAdapter();
    }

    public void setCategory(String categoryGuid) {
        this.categoryGuid = categoryGuid;
        Logger.d("restartLoader from setCategory");
        restartItemsLoader();
    }

    @Override
    public void onPriceLevelChanged(List<Integer> priceLevels) {
        setPriceLevels(priceLevels);
    }

    protected void restartItemsLoader() {
        getLoaderManager().restartLoader(BASE_ITEMS_LOADER_ID, Bundle.EMPTY, this);
    }

    @Override
    public Loader<List<ItemExModel>> onCreateLoader(int loaderId, Bundle args) {
        Logger.d("[Loader] BaseItemsPickFragment onCreateLoader");
        String sortOrder = ((SuperBaseActivity) getActivity()).getApp().isEnableABCOrder() ? _lower(ItemTable.DESCRIPTION) : ItemTable.ORDER_NUM;
        return CursorLoaderBuilder.forUri(URI_ITEMS)
                .where(ItemTable.SALABLE + " = ?", 1)
                .where(ItemTable.ACTIVE_STATUS + " = ?", 1)
                .where(ItemTable.IS_DELETED + " = ?", 0)
                .where(ItemTable.CATEGORY_ID + " = ? ", categoryGuid == null ? "" : categoryGuid)
                .projection(ItemExFunction.PROJECTION)
                .orderBy(sortOrder)
                .transform(new ItemExFunction())
                .build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ItemExModel>> loader, List<ItemExModel> list) {
        changeCursor(new ArrayList<>(list));
        if (getActivity() instanceof BaseCashierActivity){
            setPriceLevels(((BaseCashierActivity) getActivity()).getPriceLevels());
        }
    }

    protected void changeCursor(List<ItemExModel> list){
        if (adapter != null)
            adapter.changeCursor(list);
    }

    protected void setPriceLevels(List<Integer> priceLevels){
        if (adapter == null)
            return;

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            adapter.getItem(i).setCurrentPriceLevel(priceLevels);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<ItemExModel>> loader) {
        if (getActivity() == null || getView() == null)
            return;
        if (adapter != null) {
            adapter.changeCursor(null);
        }
    }

    public abstract void setListener(IItemListener listener);

    public interface IItemListener {
        void onItemSelected(long id, ItemExModel model);
    }

}
