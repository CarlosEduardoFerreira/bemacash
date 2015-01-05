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
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;

import java.util.List;

/**
 * Created by teli.yin on 10/31/2014.
 */
public class ProductItemsPageAdapter extends PagerAdapter {
    private int ITEMS_PER_PAGE;

    public List<WirelessItem> list;
    public List<BillPaymentItem> list_BillPayment;
    private int pagesCount;
    private SparseArray<GridView> grids = new SparseArray<GridView>();
    private Context mContext;
    private AdapterView.OnItemClickListener listener;
    private int prepaidMode;

    public ProductItemsPageAdapter(Context context, AdapterView.OnItemClickListener globalItemClickListener, int prepaidMode) {
        ITEMS_PER_PAGE = context.getResources().getInteger(R.integer.product_grid_columns) * context.getResources().getInteger(R.integer.product_grid_rows);
        listener = globalItemClickListener;
        mContext = context;
        this.prepaidMode = prepaidMode;
    }

    public synchronized void setList(List<WirelessItem> list) {
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

    public synchronized void setList2(List<BillPaymentItem> list) {
        pagesCount = list.size() / ITEMS_PER_PAGE + (list.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
        this.list_BillPayment = list;
        notifyDataSetChanged();

        for (int i = 0; i < grids.size(); i++) {
            GridView gridView = grids.valueAt(i);
            if (gridView != null) {
                if (list.isEmpty()) {
                    gridView.setAdapter(null);
                } else {
                    gridView.setAdapter(createAdapter2(list_BillPayment, i));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public synchronized Object instantiateItem(ViewGroup container, int position) {
        GridView gridView = grids.get(position);
        if (gridView == null) {
            gridView = (GridView) LayoutInflater.from(mContext).inflate(R.layout.productlist_items_grid, container, false);
            assert gridView != null;
            gridView.setOnItemClickListener(listener);
            grids.put(position, gridView);
            container.addView(gridView);
        }
        if (gridView.getAdapter() == null) {
            if (prepaidMode != PrepaidHomeFragment.BILLPAYMENT)
                gridView.setAdapter(createAdapter(list, position));
            else
                gridView.setAdapter(createAdapter2(list_BillPayment, position));
        }
        return gridView;
    }

    private synchronized ProductItemsAdapter createAdapter(List<WirelessItem> list, int position) {
        int start = position * ITEMS_PER_PAGE;
        int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
        Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
        if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
            return null;
        }
        return new ProductItemsAdapter(mContext, list.subList(start, end));
    }

    private synchronized ProductItemsMasterBillerAdapter createAdapter2(List<BillPaymentItem> list, int position) {
        int start = position * ITEMS_PER_PAGE;
        int end = start + Math.min(ITEMS_PER_PAGE, list.size() - start);
        Logger.d("createAdapter: %d [ %d, %d] len = %d, %b", position, start, end, list.size(), end > list.size());
        if (position >= pagesCount || start > end || start >= list.size() || end > list.size()) {
            return null;
        }
        return new ProductItemsMasterBillerAdapter(mContext, list.subList(start, end));
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



