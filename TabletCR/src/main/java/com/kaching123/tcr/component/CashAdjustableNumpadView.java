package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BondItemAdapter;
import com.kaching123.tcr.fragment.UiHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EViewGroup(R.layout.component_cash_adjustable_numpad_view)
public class CashAdjustableNumpadView extends KeyboardView {

    private static final List<Integer> BONDS_LIST = new ArrayList<Integer>();

    public BigDecimal exactValue;

    static {
        BONDS_LIST.add(new Integer(1));
        //BONDS_LIST.add(new Integer(2));
        BONDS_LIST.add(new Integer(5));
        BONDS_LIST.add(new Integer(10));
        BONDS_LIST.add(new Integer(20));
        BONDS_LIST.add(new Integer(50));
        BONDS_LIST.add(new Integer(100));
    }

    @ViewById
    protected GridView bonds;

    @ViewById
    protected View btnExact;

    private IExactClickListener exactClickListener;

    public CashAdjustableNumpadView(Context context) {
        super(context);
    }

    public CashAdjustableNumpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CashAdjustableNumpadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GridView getBonds() {
        return bonds;
    }

    @AfterViews
    protected void init() {
        super.init();
        bonds.setAdapter(new BondItemAdapter(getContext(), BONDS_LIST));
        bonds.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addValue(Integer.valueOf((int) id));
            }
        });
        btnExact.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setValue(exactValue);
                if(exactClickListener != null){
                    exactClickListener.onExactClicked();
                }
            }
        });
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
