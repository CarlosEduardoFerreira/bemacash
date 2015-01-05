package com.kaching123.tcr.component.chart;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 20.01.14.
 */
public abstract class BaseBarChartAdapter<T> extends BaseAdapter {

    protected BarData<T>[] data;

    public void setData(BarChartData<T> barChartData) {
        if (barChartData == null) {
            this.data = null;
        } else {
            this.data = barChartData.data;
            BigDecimal maxAmount = BigDecimal.ZERO;
            for (BarData item: this.data) {
                if (item.amount.compareTo(maxAmount) == 1)
                    maxAmount = item.amount;
            }
            for (BarData item: this.data) {
                item.scale = CalculationUtil.divide(item.amount, maxAmount).floatValue();
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.length;
    }

    @Override
    public Object getItem(int position) {
        return data == null ? null : data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent, position);
        }
        bindView(parent, convertView, position);
        return convertView;
    }

    protected abstract View newView(ViewGroup parent, int position);

    protected abstract void bindView(ViewGroup parent, View view, int position);

}
