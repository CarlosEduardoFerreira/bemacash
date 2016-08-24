package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by alboyko on 23.08.2016.
 */
@EViewGroup(R.layout.component_keyboard_tender_united_view)
public class PayTenderUnitedKeyboardView extends KeyboardView {

    @ViewById
    protected ImageButton numClear;

    public PayTenderUnitedKeyboardView(Context context) {
        super(context);
    }

    public PayTenderUnitedKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PayTenderUnitedKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @AfterViews
    protected void init() {
        super.init();
        numMinus.setVisibility(GONE);
        numEnter.setVisibility(GONE);
        numDelete.setVisibility(GONE);
        numClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTextView.clear();
            }
        });
    }

}
