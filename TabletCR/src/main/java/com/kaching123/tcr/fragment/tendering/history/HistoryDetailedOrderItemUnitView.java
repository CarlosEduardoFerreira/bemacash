package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.util.DateUtils;

import java.util.Date;

/**
 * @author Ivan v. Rikhmayer
 */

@EViewGroup(R.layout.tendering_history_items_list_item_unit_view)
public class HistoryDetailedOrderItemUnitView extends FrameLayout {


    @ViewById
    protected TextView status;

    @ViewById
    protected TextView serial;

    @ViewById
    protected TextView warranty;

    @ViewById
    protected TextView userStatus;

    private Unit historyItem;

    public HistoryDetailedOrderItemUnitView(Context context) {
        super(context);
    }

    public HistoryDetailedOrderItemUnitView bind(Unit historyItem, Date orderSaleDate, String orderId) {
        this.historyItem = historyItem;
        warranty.setText(DateUtils.formatFull(orderSaleDate));
        status.setText(historyItem.status.toString());
        serial.setText(historyItem.serialCode);
        userStatus.setText((historyItem.status.equals(Unit.Status.SOLD) && historyItem.orderId.equals(orderId)
                ? USER_STATUS.AVAILABLE : USER_STATUS.UNAVAILABLE).toString());
        return this;
    }

    public enum USER_STATUS {
        SCANNED,
        AVAILABLE,
        UNAVAILABLE
    }
}
