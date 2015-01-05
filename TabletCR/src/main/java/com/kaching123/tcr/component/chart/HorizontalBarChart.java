package com.kaching123.tcr.component.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by pkabakov on 17.01.14.
 */
public class HorizontalBarChart extends ListView {

    public HorizontalBarChart(Context context) {
        super(context);
        init();
    }

    public HorizontalBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setAdapter(new BarChartAdapter());
    }

    public void setData(DescriptionBarChartData data) {
        ((BarChartAdapter)getAdapter()).setData(data);
    }

    private class BarChartAdapter extends BaseBarChartAdapter<String> {

        protected View newView(ViewGroup parent, int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.horizontal_bar_chart_item, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.headerLabel = (TextView) view.findViewById(R.id.header_label);
            holder.footerValueLabel = (TextView) view.findViewById(R.id.footer_value_label);
            holder.barChartView = (HorizontalBarChartView) view.findViewById(R.id.bar_chart_view);
            view.setTag(holder);

            return view;
        }

        protected void bindView(ViewGroup parent, View view, int position) {
            ViewHolder holder = (ViewHolder) view.getTag();
            BarData<String> item = (BarData<String>) getItem(position);

            UiHelper.showInteger(holder.headerLabel, item.amount.setScale(0, RoundingMode.HALF_UP));
            holder.footerValueLabel.setText(item.description);
            holder.barChartView.setBarWidth(item.scale);
        }

        private class ViewHolder {
            TextView headerLabel;
            TextView footerValueLabel;
            HorizontalBarChartView barChartView;
        }

    }

    public static class DescriptionBarChartData extends BarChartData<String> {

        public DescriptionBarChartData(DescriptionBarData[] data) {
            super(data);
        }
    }

    public static class DescriptionBarData extends BarData<String> {

        public DescriptionBarData(String description, BigDecimal amount) {
            super(description, amount);
        }
    }

}
