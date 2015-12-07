package com.kaching123.tcr.fragment.unitlabel;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by alboyko 07.12.2015
 */
@EViewGroup(R.layout.unit_labels_list_item_view)
public class UnitLabelItemView extends FrameLayout {

    @ViewById
    protected TextView unitsLabelDescription;

    @ViewById
    protected TextView unitsLabelShortcut;

    public UnitLabelItemView(Context context) {
        super(context);
    }

    public void bind(String description, String shortCut) {
        this.unitsLabelDescription.setText(description);
        this.unitsLabelShortcut.setText(shortCut);
    }
}