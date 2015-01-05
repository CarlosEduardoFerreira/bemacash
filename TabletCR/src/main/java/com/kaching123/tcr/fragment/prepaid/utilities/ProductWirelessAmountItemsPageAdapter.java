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
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidWirelessProductAmountFragment;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

import java.util.List;

/**
 * Created by teli.yin on 10/31/2014.
 */
public class ProductWirelessAmountItemsPageAdapter extends PagerAdapter {
    private int ITEMS_PER_PAGE;

    public List list;
    private int pagesCount;
    private SparseArray<GridView> grids = new SparseArray<GridView>();
    private Context mContext;
    private PrepaidWirelessProductAmountFragment.AmountSelectedListener listener;

    public ProductWirelessAmountItemsPageAdapter(Context context, PrepaidWirelessProductAmountFragment.AmountSelectedListener globalItemClickListener) {
            ITEMS_PER_PAGE = 3 * 2;
        listener = globalItemClickListener;
        mContext = context;
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
                } else {
                    gridView.setAdapter(createAdapter(list, i));
                }
            }
        }
        notifyDataSetChanged();
    }

    private int getLayout() {
            return R.layout.product_wireless_amount_grid;
    }

    @Override
    public synchronized Object instantiateItem(ViewGroup container, int position) {
        GridView gridView = grids.get(position);
        if (gridView == null) {
            gridView = (GridView) LayoutInflater.from(mContext).inflate(getLayout(), container, false);
            assert gridView != null;
            grids.put(position, gridView);
            container.addView(gridView);
        }
        if (gridView.getAdapter() == null) {
                gridView.setAdapter(createAdapter(list, position));

        }
        return gridView;
    }

    private synchronized ProductAmountAdapter createAdapter(List list, int position) {
        int start = position * ITEMS_PER_PAGE;
        int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
        Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
        if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
            return null;
        }
        return new ProductAmountAdapter(mContext, list.subList(start, end), listener);
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



