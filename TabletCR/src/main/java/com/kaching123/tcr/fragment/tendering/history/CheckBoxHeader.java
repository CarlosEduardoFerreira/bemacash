package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
@EViewGroup(R.layout.tendering_history_items_list_header)
public class CheckBoxHeader extends FrameLayout {

    @ViewById
    public CheckBox checkbox;

    public CheckBoxHeader(Context context) {
        super(context);
    }

    public CheckBoxHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBoxHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface ICheckBoxListener {

        void onCheckChange(HistoryDetailedOrderItemModel item, BigDecimal qty, boolean check);
    }

}
