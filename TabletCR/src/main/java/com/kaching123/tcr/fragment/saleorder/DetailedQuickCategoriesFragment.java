package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.catalog.BaseCategoriesFragment;
import com.kaching123.tcr.fragment.detailedpick.CategoriesAdapter;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by mboychenko on 5/30/2017.
 */

@EFragment(R.layout.detailed_ui_categories_fragment)
public class DetailedQuickCategoriesFragment extends BaseCategoriesFragment {

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
    protected String getOrderBy() {
        return CategoryTable.ORDER_NUM;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAdapterView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> v, View view, int pos, long id) {
                if (getAdapterView().getChildCount() > 0) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        getAdapterView().getChildAt(0).setActivated(false);
                    }
                }
                view.setActivated(true);
                listItemClicked(pos);
            }
        });

    }
}
