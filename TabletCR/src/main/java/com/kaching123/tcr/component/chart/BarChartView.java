package com.kaching123.tcr.component.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * Created by pkabakov on 10.01.14.
 */
@EViewGroup(R.layout.bar_chart_view)
public class BarChartView extends FrameLayout {

    @ViewById
    protected View barView;

    private int barBackgroundRes;

    private float barHeightPercent;

    public BarChartView(Context context) {
        super(context);
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.BarChartView);
        try {
            barBackgroundRes = a.getResourceId(R.styleable.BarChartView_bar_background, 0);
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

    public void setBarHeight(float heightPercent) {
        if (barHeightPercent == heightPercent)
            return;

        barHeightPercent = heightPercent;
        requestLayout();
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
        child.getLayoutParams().height = Math.round(parentHeight * barHeightPercent);

        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

}
