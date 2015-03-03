package com.kaching123.tcr.fragment.wireless;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * @author Ivan v. Rikhmayer
 */
@EViewGroup(R.layout.wireless_list_item_view)
public class UnitItemView extends FrameLayout {

    @ViewById
    protected TextView unitSerial;

    @ViewById
    protected TextView unitWarranty;

    @ViewById
    protected TextView unitStatus;

    public UnitItemView(Context context) {
        super(context);
    }

    @AfterViews
    protected void init() {
    }

    public void bind(String guid,
                     String warranty,
                     String status) {
        this.unitSerial.setText(guid);
        this.unitWarranty.setText(warranty);
        this.unitStatus.setText(status);
    }
}