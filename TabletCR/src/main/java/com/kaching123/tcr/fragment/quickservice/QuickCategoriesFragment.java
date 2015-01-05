package com.kaching123.tcr.fragment.quickservice;

import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.GridView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;

/**
 * Created by vkompaniets on 22.11.13.
 */
@EFragment (R.layout.quickservice_categories_fragment)
public class QuickCategoriesFragment extends BaseCategoriesFragment {

    @ViewById
    protected GridView gridView;

    @Override
    protected CursorAdapter createAdapter() {
        return new CategoriesAdapter(getActivity());
    }

    @Override
    protected AbsListView getAdapterView() {
        return gridView;
    }

    @Override
    protected String getOrderBy() {
        return CategoryTable.ORDER_NUM;
    }
}
