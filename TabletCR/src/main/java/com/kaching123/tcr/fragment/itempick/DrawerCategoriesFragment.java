package com.kaching123.tcr.fragment.itempick;

import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;

@EFragment (R.layout.itempick_categories_fragment)
public class DrawerCategoriesFragment extends BaseCategoriesFragment{

    @ViewById
    protected ListView list;

    @Override
    protected CursorAdapter createAdapter() {
        return new DrawerCategoriesAdapter(getActivity());
    }

    @Override
    protected AbsListView getAdapterView() {
        return list;
    }

    @Override
    protected String getOrderBy() {
        return CategoryTable.ORDER_NUM;
    }
}
