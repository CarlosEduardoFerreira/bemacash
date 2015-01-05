package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;

import java.util.List;

/**
 * Created by teli.yin on 10/31/2014.
 */
public class ProductFlagItemsPageAdapter extends PagerAdapter {
    private int ITEMS_PER_PAGE;

    public List list;
    private int pagesCount;
    private SparseArray<GridView> grids = new SparseArray<GridView>();
    private Context mContext;
    private AdapterView.OnItemClickListener listener;
    private int prepaidMode;

    public ProductFlagItemsPageAdapter(Context context, AdapterView.OnItemClickListener globalItemClickListener, int prepaidMode) {
        if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE || prepaidMode == PrepaidHomeFragment.INTERNATIONAL)
            ITEMS_PER_PAGE = 3 * 5;
        else
            ITEMS_PER_PAGE = 4 * 4;
        listener = globalItemClickListener;
        mContext = context;
        this.prepaidMode = prepaidMode;
    }

    public synchronized void setList(List list) {
        pagesCount = list.size() / ITEMS_PER_PAGE + (list.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
        this.list = list;
        notifyDataSetChanged();

        for (int i = 0; i < grids.size(); i++) {
            GridView gridView = grids.valueAt(i);
            if (gridView != null) {
                if (list.isEmpty()) {
                    gridView.setAdapter(null);
                } else if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
                    gridView.setAdapter(createAdapter3(list, i));
                else {
                    gridView.setAdapter(createAdapter(list, i));
                }
            }
        }
        notifyDataSetChanged();
    }


    private int getLayout() {
        if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE || prepaidMode == PrepaidHomeFragment.INTERNATIONAL)
            return R.layout.productlist_flags_grid;
        else if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
            return R.layout.productlist_items_grid;
        else
            return R.layout.productlist_carriers_grid;
    }

    @Override
    public synchronized Object instantiateItem(ViewGroup container, int position) {
        GridView gridView = grids.get(position);
        if (gridView == null) {
            gridView = (GridView) LayoutInflater.from(mContext).inflate(getLayout(), container, false);
            assert gridView != null;
            gridView.setOnItemClickListener(listener);
            grids.put(position, gridView);
            container.addView(gridView);
        }
        if (gridView.getAdapter() == null) {
            if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE || prepaidMode == PrepaidHomeFragment.INTERNATIONAL)
                gridView.setAdapter(createAdapter(list, position));
            else if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
                gridView.setAdapter(createAdapter3(list, position));
            else
                gridView.setAdapter(createAdapter2(list, position));

        }
        return gridView;
    }

    private synchronized ProductFlagsAdapter createAdapter(List list, int position) {
        int start = position * ITEMS_PER_PAGE;
        int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
        Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
        if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
            return null;
        }
        return new ProductFlagsAdapter(mContext, list.subList(start, end));
    }

    private synchronized ProductCarriersAdapter createAdapter2(List list, int position) {
        int start = position * ITEMS_PER_PAGE;
        int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
        Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
        if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
            return null;
        }
        return new ProductCarriersAdapter(mContext, list.subList(start, end));
    }

    private synchronized ProductItemsBillPaymentCategoriesAdapter createAdapter3(List list, int position) {
        int start = position * ITEMS_PER_PAGE;
        int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
        Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
        if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
            return null;
        }
        return new ProductItemsBillPaymentCategoriesAdapter(mContext, list.subList(start, end));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
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



