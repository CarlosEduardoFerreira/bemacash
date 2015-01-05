package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

@EViewGroup(R.layout.settings_printer_status_item_view)
public class PrinterStatusListItem extends RelativeLayout {

    @ViewById
    protected TextView label;

    @ViewById
    protected TextView value;

    public PrinterStatusListItem(Context context) {
        super(context);
        setMinimumHeight(getContext().getResources().getDimensionPixelOffset(R.dimen.listPreferredSmallItemHeight));
    }

    public void bind(int labelResId, int valueResId, int colorResId) {
        this.label.setText(labelResId);
        this.value.setText(valueResId);
        this.value.setTextColor(getResources().getColor(colorResId));
    }

}
