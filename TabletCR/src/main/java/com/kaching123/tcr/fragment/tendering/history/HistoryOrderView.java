package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TenderType;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TransactionsState;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Ivan v. Rikhmayer
 */
@EViewGroup(R.layout.tendering_history_orders_list_item_view)
public class HistoryOrderView extends FrameLayout {

    @ViewById
    protected TextView orderNum;

    @ViewById
    protected TextView cashier;

    @ViewById
    protected TextView date;

    @ViewById
    protected TextView amount;

    @ViewById
    protected TextView tenderType;

    @ViewById
    protected TextView tipsAmount;

    @ViewById
    protected TextView transactionsState;

    private final boolean isTipsEnabled;

    public HistoryOrderView(Context context, boolean isTipsEnabled) {
        super(context);
        this.isTipsEnabled = isTipsEnabled;
    }

    @AfterViews
    protected void init() {
        tipsAmount.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
        transactionsState.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
    }

    public void bind(String orderNum,
                     Date date,
                     BigDecimal total,
                     String operatorName,
                     TenderType tenderType,
                     OrderType orderType,
                     BigDecimal tipsAmount,
                     TransactionsState transactionsState) {
        this.orderNum.setText(orderNum);
        this.date.setText(DateUtils.formatFull(date));
        this.amount.setText(UiHelper.valueOf(total));
        this.cashier.setText(operatorName);
        this.tenderType.setText(tenderType != null ? tenderType.label : R.string.edit_printer_unknown);

        if (!isTipsEnabled)
            return;
        UiHelper.showPrice(this.tipsAmount, tipsAmount);
        this.transactionsState.setText(getTransactionStateLabel(transactionsState));
    }

    private static int getTransactionStateLabel(TransactionsState transactionsState) {
        switch (transactionsState) {
            case OPEN:
                return R.string.order_transactions_status_open;
            case CLOSED:
                return R.string.order_transactions_status_closed;
            default:
                return R.string.order_transactions_status_na;
        }
    }
}