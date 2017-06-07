package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.fragment.quickservice.QuickItemsAdapter;
import com.kaching123.tcr.model.ItemExModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by mboychenko on 5/30/2017.
 */
@EFragment(R.layout.detailed_quick_items_fragment)
public class DetailedQuickItemsFragment extends BaseItemsPickFragment {

    @ViewById
    protected ViewPager viewPager;
    @ViewById
    protected Button leftArrow;
    @ViewById
    protected Button rightArrow;

    private IItemListener listener;
    private ItemsPageAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewPager.setAdapter(adapter = new ItemsPageAdapter());

        leftArrow.setEnabled(false);
        if(adapter.getCount() <= 1) {
            rightArrow.setEnabled(false);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int currPage = position + 1;
                int pages = viewPager.getAdapter().getCount();

                if (pages > 1 && currPage > 1) {
                    leftArrow.setEnabled(true);
                } else if (currPage == 1) {
                    leftArrow.setEnabled(false);
                }

                if (currPage == pages) {
                    rightArrow.setEnabled(false);
                } else if (currPage < pages) {
                    rightArrow.setEnabled(true);
                }
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curr = viewPager.getCurrentItem() - 1;
                if (curr >= 0 ) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                }
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pages = viewPager.getAdapter().getCount() - 1;
                int curr = viewPager.getCurrentItem() + 1;
                if(curr <= pages) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
            }
        });
    }

    @Override
    protected ObjectsCursorAdapter<ItemExModel> createAdapter() {
        return null;
    }

    @Override
    public void setCategory(String categoryGuid) {
        viewPager.setCurrentItem(0);
        super.setCategory(categoryGuid);
    }

    @Override
    protected void changeCursor(List<ItemExModel> list) {
        if (adapter != null)
            adapter.setList(list);
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


    @Override
    public void setListener(final IItemListener listener) {
        this.listener = listener;
    }

    public class ItemsPageAdapter extends PagerAdapter {

        private int ITEMS_PER_PAGE;

        public List<ItemExModel> list;
        private int pagesCount;
        private SparseArray<GridView> grids = new SparseArray<GridView>();

        public ItemsPageAdapter() {
            ITEMS_PER_PAGE = 4 * 4;
//            ITEMS_PER_PAGE = getResources().getInteger(R.integer.quick_grid_columns) * getResources().getInteger(R.integer.quick_grid_rows);
        }

        public synchronized void setList(List<ItemExModel> list) {
            pagesCount = list.size() / ITEMS_PER_PAGE + (list.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
            rightArrow.setEnabled(pagesCount > 1);
            this.list = list;
            notifyDataSetChanged();
//            viewPagerIndicator.notifyDataSetChanged();

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
                gridView = (GridView) LayoutInflater.from(getActivity()).inflate(R.layout.detailed_qservice_items_grid, container, false);
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
            return new QuickItemsAdapter(getActivity(), list.subList(start, end), true);
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

    private AdapterView.OnItemClickListener globalItemClickListener = new AdapterView.OnItemClickListener() {
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
