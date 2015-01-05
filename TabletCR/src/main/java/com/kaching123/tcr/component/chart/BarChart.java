package com.kaching123.tcr.component.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by pkabakov on 10.01.14.
 */
public class BarChart extends GridView {

    public enum PeriodUnit {
        HOUR, DAY
    }

    public enum ValueUnit {
        AMOUNT, COUNT
    }

    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("h:mm");
    private static final SimpleDateFormat hourUnitFormat = new SimpleDateFormat("a");
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
    private static final SimpleDateFormat dayUnitFormat = new SimpleDateFormat("MMM");

    private int measuredHeight;

    public BarChart(Context context) {
        super(context);
        init();
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setAdapter(new PeriodBarChartAdapter());
    }

    public void setData(PeriodBarChartData data) {
        ((PeriodBarChartAdapter)getAdapter()).setData(data);
    }

    public void setValueUnit(ValueUnit valueUnit) {
        ((PeriodBarChartAdapter)getAdapter()).setValueUnit(valueUnit);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private class PeriodBarChartAdapter extends BarChartAdapter<Long> {

        private PeriodUnit periodUnit;
        private ValueUnit valueUnit = ValueUnit.COUNT;
        private BigDecimal averageAmount;
        private int averageCount;

        @Override
        public void setData(BarChartData<Long> barChartData) {
            if (barChartData != null) {
                PeriodBarChartData periodBarChartData = (PeriodBarChartData)barChartData;
                this.periodUnit = periodBarChartData.periodUnit;
                if (periodBarChartData.valueUnit != null)
                    this.valueUnit = periodBarChartData.valueUnit;

                BigDecimal maxAmount = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                int maxCount = 0;
                int totalCount = 0;
                for (BarData<Long> item: periodBarChartData.data) {
                    totalAmount = totalAmount.add(item.amount);
                    totalCount += ((PeriodBarData)item).count;
                    if (item.amount.compareTo(maxAmount) == 1)
                        maxAmount = item.amount;
                    if (((PeriodBarData)item).count > maxCount)
                        maxCount = ((PeriodBarData)item).count;
                }
                for (BarData<Long> item: periodBarChartData.data) {
                    item.scale = CalculationUtil.divide(item.amount, maxAmount).floatValue();
                    ((PeriodBarData)item).countScale = (float)((PeriodBarData)item).count / (float)maxCount;
                }
                this.averageAmount = CalculationUtil.divide(totalAmount, new BigDecimal(periodBarChartData.data.length)) ;
                this.averageCount = Math.round((float)totalCount / (float)periodBarChartData.data.length);
            }
            super.setData(barChartData);
        }

        public void setValueUnit(ValueUnit valueUnit) {
            this.valueUnit = valueUnit;
            notifyDataSetChanged();
        }

        @Override
        protected int getItemLayoutRes() {
            return R.layout.bar_chart_item;
        }

        protected void bindView(ViewGroup parent, View view, int position) {
            ViewHolder holder = (ViewHolder) view.getTag();
            PeriodBarData item = (PeriodBarData) getItem(position);

            switch (valueUnit) {
                case AMOUNT:
                    UiHelper.showInteger(holder.headerLabel, item.amount.setScale(0, RoundingMode.HALF_UP));
                    break;
                case COUNT:
                    holder.headerLabel.setText(String.valueOf(item.count));
                    break;
            }

            holder.footerValueLabel.setText(getPeriodFormat().format(item.description));
            holder.footerUnitLabel.setText(getPeriodUnitFormat().format(item.description).toUpperCase());

            holder.barChartView.setBarHeight(valueUnit == ValueUnit.AMOUNT ? item.scale : item.countScale);
            holder.barChartView.setBarBackground(getBarBackground(item));
        }

        private int getBarBackground(PeriodBarData item) {
            switch (valueUnit) {
                case AMOUNT:
                    if (item.amount.compareTo(averageAmount.multiply(new BigDecimal("1.5"))) == 1) {
                        return R.drawable.bar_chart_bar_green_bg;
                    } else if (item.amount.compareTo(averageAmount) >= 0) {
                        return R.drawable.bar_chart_bar_green_light_bg;
                    }else if (item.amount.compareTo(averageAmount.multiply(new BigDecimal("0.5"))) == -1) {
                        return R.drawable.bar_chart_bar_red_bg;
                    } else {
                        return R.drawable.bar_chart_bar_red_light_bg;
                    }
                case COUNT:
                    if (item.count > averageCount * 1.5f) {
                        return R.drawable.bar_chart_bar_green_bg;
                    } else if (item.count >= averageCount) {
                        return R.drawable.bar_chart_bar_green_light_bg;
                    }else if (item.count < averageCount * 0.5f) {
                        return R.drawable.bar_chart_bar_red_bg;
                    } else {
                        return R.drawable.bar_chart_bar_red_light_bg;
                    }
            }
            throw new IllegalArgumentException("unknown valueUnit: " + valueUnit);
        }

        private DateFormat getPeriodFormat() {
            switch (periodUnit) {
                case DAY:
                    return dayFormat;
                case HOUR:
                    return hourFormat;
                default:
                    return hourFormat;
            }
        }

        private DateFormat getPeriodUnitFormat() {
            switch (periodUnit) {
                case DAY:
                    return dayUnitFormat;
                case HOUR:
                    return hourUnitFormat;
                default:
                    return hourUnitFormat;
            }
        }

    }

    private abstract class BarChartAdapter<T> extends BaseBarChartAdapter<T> {

        public void setData(BarChartData<T> barChartData) {
            if (barChartData != null) {
                final int count = barChartData.data.length;
                BarChart.this.setNumColumns(count);
            }

            if (barChartData == null) {
                this.data = null;
            } else {
                this.data = barChartData.data;
            }
            notifyDataSetChanged();
        }

        protected View newView(ViewGroup parent, int position) {
            View view = LayoutInflater.from(getContext()).inflate(getItemLayoutRes(), parent, false);

            view.getLayoutParams().height = getParentHeight();

            ViewHolder holder = new ViewHolder();
            holder.headerLabel = (TextView) view.findViewById(R.id.header_label);
            holder.footerValueLabel = (TextView) view.findViewById(R.id.footer_value_label);
            holder.footerUnitLabel = (TextView) view.findViewById(R.id.footer_unit_label);
            holder.barChartView = (BarChartView) view.findViewById(R.id.bar_chart_view);
            view.setTag(holder);

            return view;
        }

        protected abstract int getItemLayoutRes();

        private int getParentHeight() {
            int parentHeight = getHeight();
            if (parentHeight == 0)
                parentHeight = measuredHeight;
            return parentHeight;
        }

        protected class ViewHolder {
            TextView headerLabel;
            TextView footerValueLabel;
            TextView footerUnitLabel;
            BarChartView barChartView;
        }

    }

    public static class PeriodBarChartData extends BarChartData<Long>{
        final PeriodUnit periodUnit;
        final ValueUnit valueUnit;

        public PeriodBarChartData(PeriodBarData[] data, PeriodUnit periodUnit) {
            this(data, periodUnit, null);
        }

        public PeriodBarChartData(PeriodBarData[] data, PeriodUnit periodUnit, ValueUnit valueUnit) {
            super(data);
            this.periodUnit = periodUnit;
            this.valueUnit = valueUnit;
        }
    }

    public static class PeriodBarData extends BarData<Long> {

        final int count;
        float countScale;

        public PeriodBarData(Long period, BigDecimal amount, int count) {
            super(period, amount);
            this.count = count;
        }

    }

}
