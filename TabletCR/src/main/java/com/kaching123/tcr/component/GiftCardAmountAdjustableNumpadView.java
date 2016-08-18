package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BondItemAdapter;
import com.kaching123.tcr.fragment.UiHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EViewGroup(R.layout.gift_card_amount_adjustable_numpad_view)
public class GiftCardAmountAdjustableNumpadView extends KeyboardView {


    public BigDecimal exactValue;

    private IExactClickListener exactClickListener;

    public GiftCardAmountAdjustableNumpadView(Context context) {
        super(context);
    }

    public GiftCardAmountAdjustableNumpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GiftCardAmountAdjustableNumpadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @AfterViews
    protected void init() {
        super.init();
    }

    public void setExactClickListener(IExactClickListener exactClickListener) {
        this.exactClickListener = exactClickListener;
    }

    private void addValue(Integer value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        BigDecimal current = getDecimalValue();
        setValue(current == null ? bigDecimal : current.add(bigDecimal));
    }

    private void setValue(BigDecimal value) {
        currentTextView.setText(UiHelper.valueOf(value));
    }

    protected BigDecimal getDecimalValue() {
        try {
            return new BigDecimal(currentTextView.getText().toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static interface IExactClickListener{
        void onExactClicked();
    }
}
