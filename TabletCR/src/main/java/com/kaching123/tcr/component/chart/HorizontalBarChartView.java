package com.kaching123.tcr.component.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * Created by pkabakov on 17.01.14.
 */
@EViewGroup(R.layout.horizontal_bar_chart_view)
public class HorizontalBarChartView extends FrameLayout {

    @ViewById
    protected View barView;

    private int barBackgroundRes;

    private float barWidthPercent;

    public HorizontalBarChartView(Context context) {
        super(context);
    }

    public HorizontalBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public HorizontalBarChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.HorizontalBarChartView);
        try {
            barBackgroundRes = a.getResourceId(R.styleable.HorizontalBarChartView_horizontal_bar_background, 0);
        } catch (NullPointerException ignore) {}
        a.recycle();
    }

    @AfterViews
    protected  void initViews() {
        setBarBackground(barBackgroundRes);
    }

    public void setBarBackground(int backgroundRes) {
        barView.setBackgroundResource(backgroundRes);
    }

    public void setBarWidth(float widthPercent) {
        if (barWidthPercent == widthPercent)
            return;

        barWidthPercent = widthPercent;
        requestLayout();
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec);
        child.getLayoutParams().width = Math.round(parentWidth * barWidthPercent);

        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

}
