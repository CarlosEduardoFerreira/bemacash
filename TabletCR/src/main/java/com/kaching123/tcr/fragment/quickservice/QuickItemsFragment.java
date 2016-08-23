package com.kaching123.tcr.fragment.quickservice;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.viewpagerindicator.LinePageIndicator;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by gdubina on 22.11.13.
 */
@EFragment(R.layout.quickservice_items_list_fragment)
public class QuickItemsFragment extends BaseItemsPickFragment {

    @ViewById
    protected ViewPager viewPager;

    @ViewById
    protected LinePageIndicator viewPagerIndicator;

    private ItemsPageAdapter adapter;

    private IItemListener listener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewPager.setAdapter(adapter = new ItemsPageAdapter());
        viewPagerIndicator.setViewPager(viewPager);
    }

    @Override
    public void setCategory(String categoryGuid) {
        viewPager.setCurrentItem(0);
        super.setCategory(categoryGuid);
    }

    @Override
    public void setListener(final IItemListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLoadFinished(Loader<List<ItemExModel>> loader, List<ItemExModel> list) {
        adapter.setList(list);
        setPriceLevels(((BaseCashierActivity) getActivity()).getPriceLevels());
    }

    @Override
    public void onLoaderReset(Loader<List<ItemExModel>> loader) {

    }

    @Override
    protected ObjectsCursorAdapter<ItemExModel> createAdapter() {
        return null;
    }

    @Override
    protected void setPriceLevels(List<Integer> priceLevels) {
        if (adapter == null || adapter.list == null)
            return;

        for (ItemExModel model : adapter.list){
            model.setCurrentPriceLevel(priceLevels);
        }
        adapter.notifyDataSetChanged();
    }

    public class ItemsPageAdapter extends PagerAdapter {

        private int ITEMS_PER_PAGE;

        public List<ItemExModel> list;
        private int pagesCount;
        private SparseArray<GridView> grids = new SparseArray<GridView>();

        public ItemsPageAdapter() {
            ITEMS_PER_PAGE = getResources().getInteger(R.integer.quick_grid_columns) * getResources().getInteger(R.integer.quick_grid_rows);
        }

        public synchronized void setList(List<ItemExModel> list) {
            pagesCount = list.size() / ITEMS_PER_PAGE + (list.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
            this.list = list;
            notifyDataSetChanged();
            viewPagerIndicator.notifyDataSetChanged();

            for (int i = 0; i < grids.size(); i++) {
                GridView gridView = grids.valueAt(i);
                if (gridView != null) {
                    if (list.isEmpty()) {
                        gridView.setAdapter(null);
                    } else {
                        gridView.setAdapter(createAdapter(list, i));
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public synchronized Object instantiateItem(ViewGroup container, int position) {
            GridView gridView = grids.get(position);
            if (gridView == null) {
                gridView = (GridView) LayoutInflater.from(getActivity()).inflate(R.layout.quickservice_items_grid, container, false);
                assert gridView != null;
                gridView.setOnItemClickListener(globalItemClickListener);
                grids.put(position, gridView);
                container.addView(gridView);
            }
            if (gridView.getAdapter() == null) {
                gridView.setAdapter(createAdapter(list, position));
            }
            return gridView;
        }

        private synchronized QuickItemsAdapter createAdapter(List<ItemExModel> list, int position) {
            int start = position * ITEMS_PER_PAGE;
            int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
            Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
            if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
                return null;
            }
            return new QuickItemsAdapter(getActivity(), list.subList(start, end));
        }

        @Override
        public void destroyItem(android.view.ViewGroup container, int position, java.lang.Object object) {
            container.removeView((View) object);
            grids.remove(position);
        }

        @Override
        public synchronized int getCount() {
            return pagesCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }
    }

    private OnItemClickListener globalItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listener == null) {
                return;
            }
            ItemExModel model = (ItemExModel) parent.getItemAtPosition(position);
            assert model != null;
            listener.onItemSelected(id, model);
        }
    };

}
