package com.kaching123.tcr.fragment.inventory;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.fragment.itempick.CategoriesAdapter;
import com.kaching123.tcr.fragment.itempick.CategoryItemView_;

/**
 * Created by vkompaniets on 27.11.13.
 */

@EFragment (R.layout.itempick_categories_fragment)
public class CategoriesFragment extends BaseCategoriesFragment {

    @ViewById
    protected ListView list;

    @Override
    protected CursorAdapter createAdapter() {
        return new CategoriesAdapter(getActivity());
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
}
