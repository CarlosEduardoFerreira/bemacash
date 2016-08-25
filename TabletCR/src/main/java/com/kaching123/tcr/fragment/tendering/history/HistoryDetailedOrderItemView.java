package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.tendering.history.CheckBoxHeader.ICheckBoxListener;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.showQuantityInteger;

/**
 * @author Ivan v. Rikhmayer
 */

@EViewGroup(R.layout.tendering_history_items_list_item_view)
public class HistoryDetailedOrderItemView extends FrameLayout implements ICheckBoxListener {


    private IQtyListener callback;

    @ViewById
    protected CheckBox checkbox;

    @ViewById
    protected TextView name;

    @ViewById
    protected TextView maxRefundQty;

    @ViewById
    protected TextView ean;

    @ViewById
    protected TextView notes;

    @ViewById
    protected TextView qtyRefund;

    @ViewById
    protected TextView price;

    @ViewById
    protected TextView qty;

    @ViewById
    protected TextView total;

    @ViewById
    protected View refundQtyBlock;

    private HistoryDetailedOrderItemModel historyItem;

    private ICheckBoxListener listener;

    public HistoryDetailedOrderItemView(Context context) {
        super(context);
    }

    @AfterViews
    public void onCreate() {
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                historyItem.wanted = b;
                if (listener == null || compoundButton != checkbox)
                    return;
                if (historyItem.saleItemModel.isSerializable) {
                    if (b) {
                        enableCheckboxPolitics();
                    } else {
                        disableCheckboxPolitics();
                    }
                }
                listener.onCheckChange(historyItem, new BigDecimal(String.valueOf(qtyRefund.getText())), b);
            }
        });
    }

    public HistoryDetailedOrderItemView setCallback(ICheckBoxListener listener) {
        this.listener = listener;
        return this;
    }

    public HistoryDetailedOrderItemView bind(HistoryDetailedOrderItemModel historyItem) {
        this.historyItem = historyItem;

        SaleOrderItemViewModel saleItemModel = historyItem.saleItemModel;
        SaleOrderItemModel itemModel = saleItemModel.itemModel;

        name.setText(saleItemModel.description);
        if (TextUtils.isEmpty(saleItemModel.subTitle)) {
            ean.setVisibility(View.GONE);
        } else {
            ean.setText(saleItemModel.subTitle);
            ean.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(saleItemModel.itemModel.notes)) {
            notes.setVisibility(View.GONE);
        } else {
            notes.setText(saleItemModel.itemModel.notes);
            notes.setVisibility(View.VISIBLE);
        }

        BigDecimal price = saleItemModel.finalPrice;//saleItemModel.fullPrice
        showPrice(this.price, price);
        showPrice(this.total, CalculationUtil.getSubTotal(itemModel.qty, price));//CalculationUtil.getSubTotal(itemModel.qty, saleItemModel.fullPrice, itemModel.discount, itemModel.discountType));


        BigDecimal availableQty = historyItem.availableQty;//itemModel.qty.subtract(itemModel.tmpRefundQty);


        if (saleItemModel.isPcsUnit) {
            showInteger(this.qty, itemModel.qty);
            showInteger(this.maxRefundQty, itemModel.isPrepaidItem || itemModel.isGiftCard ? BigDecimal.ONE : availableQty);
            showInteger(this.qtyRefund, itemModel.isPrepaidItem || itemModel.isGiftCard ? new BigDecimal(0) : historyItem.wantedQty);

        } else {
            showQuantityInteger(this.qty, itemModel.qty);
            showQuantityInteger(this.maxRefundQty, itemModel.isPrepaidItem || itemModel.isGiftCard ? new BigDecimal(0) : availableQty);
            showQuantityInteger(this.qtyRefund, itemModel.isPrepaidItem || itemModel.isGiftCard ? new BigDecimal(0) : historyItem.wantedQty);
        }

        checkbox.setChecked(historyItem.wanted);
        if (historyItem.isFinished()) {
            checkbox.setEnabled(false);
            refundQtyBlock.setEnabled(false);
            setAlpha(0.3f);
        } else {
            checkbox.setEnabled(true);
            refundQtyBlock.setEnabled(true);
            setAlpha(1f);
        }
        if (historyItem.saleItemModel.isSerializable && availableQty.intValue() > 0) {
            if (historyItem.wantedQty != null && historyItem.scannedQty != null && historyItem.wantedQty.compareTo(historyItem.scannedQty) < 0) {
                onCheckChange(historyItem, historyItem.wantedQty, false);
            }
            if (historyItem.scannedQty != null && historyItem.scannedQty.intValue() > 0) {
                checkbox.setClickable(true);
                checkbox.setChecked(true);
            } else {
                checkbox.setClickable(false);
                checkbox.setChecked(false);
            }
        } else {
            checkbox.setClickable(true);
            /*checkbox.setChecked(true);*/
        }

        //disenable refund for prepaid items
        if (itemModel.isPrepaidItem || itemModel.isGiftCard)
            checkbox.setEnabled(false);

        return this;
    }

    public void enableCheckboxPolitics() {
        checkbox.setClickable(true);
        if (!checkbox.isChecked()) {
            checkbox.toggle();
        }
    }

    public void disableCheckboxPolitics() {
        Logger.d(historyItem.saleItemModel.description + "disabling 3");
        checkbox.setClickable(false);
        if (checkbox.isChecked()) {
            checkbox.toggle();
        }
    }

    @Override
    public void onCheckChange(HistoryDetailedOrderItemModel item, BigDecimal qty, boolean check) {
        if (checkbox == null) {
            return;
        }
        if (checkbox.isChecked() == check || !checkbox.isClickable()) {
            return;
        }
        if ((item != null && item.saleItemModel != null && item.saleItemModel.isSerializable )
        || (historyItem != null && historyItem.saleItemModel != null&& historyItem.saleItemModel.isSerializable )) {
            return;
        }
        checkbox.setChecked(check);
    }

    @Click
    protected void refundQtyBlockClicked() {
        if (callback != null) {
            callback.onRefundQtyClicked(this, historyItem);
        }
    }

    public HistoryDetailedOrderItemView setListener(IQtyListener listener) {
        this.callback = listener;
        return this;
    }

    public static interface IQtyListener {
        void onRefundQtyClicked(View v, HistoryDetailedOrderItemModel position);
    }
}
