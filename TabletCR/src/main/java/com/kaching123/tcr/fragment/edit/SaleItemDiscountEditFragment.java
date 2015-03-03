package com.kaching123.tcr.fragment.edit;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.DiscountType;

import java.math.BigDecimal;

/**
 * Created by gdubina on 14/11/13.
 */
@EFragment
public class SaleItemDiscountEditFragment extends BaseDiscountEditFragment {

    private static final String DIALOG_NAME = "siDiscountEditFragment";

    private OnEditSaleItemDiscountListener onEditSaleItemDiscountListener;

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {
        DiscountType type = discountPercentType.isChecked() ? DiscountType.PERCENT : DiscountType.VALUE;
        onEditSaleItemDiscountListener.onConfirm(value, type);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_si_discount_qty;
    }

    public static void show(FragmentActivity activity, String saleItemGuid, BigDecimal price, BigDecimal discount, DiscountType discountType, OnEditSaleItemDiscountListener listener) {
        DialogUtil.show(activity, DIALOG_NAME, SaleItemDiscountEditFragment_.builder().saleItemGuid(saleItemGuid).maxValue(price).decimalValue(discount).discountType(discountType).build()).setOnEditSaleItemDiscountListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public void setOnEditSaleItemDiscountListener(OnEditSaleItemDiscountListener onEditSaleItemDiscountListener) {
        this.onEditSaleItemDiscountListener = onEditSaleItemDiscountListener;
    }

    public static interface OnEditSaleItemDiscountListener {
        void onConfirm(BigDecimal value, DiscountType type);
    }
}
