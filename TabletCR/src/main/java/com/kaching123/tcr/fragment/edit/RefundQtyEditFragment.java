package com.kaching123.tcr.fragment.edit;

import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.QuantityFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;

import java.math.BigDecimal;

import static com.kaching123.tcr.util.CalculationUtil.negativeQty;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class RefundQtyEditFragment extends DecimalEditFragment{


    private static final String DIALOG_NAME = "RefundQtyEditFragment";

    private HistoryDetailedOrderItemModel item;

    protected OnRefundResultListener listener;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_qty_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_edit_qty;
    }

    @Override
    protected void attachViews() {
        super.attachViews();
        editText.setFilters(new InputFilter[]{new QuantityFormatInputFilter()});
    }

    protected boolean onSubmitForm() {
        BigDecimal value = validateForm();
        if (value == null)
            return false;
        callCommand(saleItemGuid, value);
        if (listener != null) {
            listener.onComplete(item, item.wantedQty);
        }
        return true;
    }

    @Override
    protected BigDecimal getMaxValue() {
        return item.availableQty;
    }

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {
        item.wantedQty = value;
    }

    @Override
    protected BigDecimal getDecimalValue() {
        String text = editText.getText().toString();
        try {
            if (text.endsWith("-")){
                return negativeQty(new BigDecimal(text.substring(0, text.length() - 1)));
            }else {
                return new BigDecimal(text);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public RefundQtyEditFragment setItem(HistoryDetailedOrderItemModel item) {
        this.item = item;
        return this;
    }

    public static void show(FragmentActivity activity, HistoryDetailedOrderItemModel item, boolean isInteger, OnRefundResultListener onResultListener){
        DialogUtil.show(activity, DIALOG_NAME, RefundQtyEditFragment_.builder().isInteger(isInteger).build()).setItem(item).setOnResultListener(onResultListener);
    }



    public void setOnResultListener(OnRefundResultListener onResultListener) {
        this.listener = onResultListener;
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static interface OnRefundResultListener {
        void onComplete(HistoryDetailedOrderItemModel item, BigDecimal qty);
    }
}
