package com.kaching123.tcr.fragment.edit;

import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;

import java.math.BigDecimal;

/**
 * Created by gdubina on 14/11/13.
 */
@EFragment
public class PriceEditFragment extends DecimalEditFragment {

    private static final String DIALOG_NAME = "priceEditFragment";

    private OnEditPriceListener editPriceListener;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_price_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_edit_price;
    }

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {
        if (editPriceListener != null) {
            editPriceListener.onConfirm(value);
        }
    }

    public static void show(FragmentActivity activity, String saleItemGuid, BigDecimal price, OnEditPriceListener listener) {
        DialogUtil.show(activity, DIALOG_NAME, PriceEditFragment_.builder().saleItemGuid(saleItemGuid).decimalValue(price).build()).setEditPriceListener(listener);
    }

    public static void show(FragmentActivity activity, BigDecimal price, OnEditPriceListener listener) {
        PriceEditFragment frm = PriceEditFragment_.builder().decimalValue(price).build();
        frm.setEditPriceListener(listener);
        frm.show(activity.getSupportFragmentManager(), DIALOG_NAME);
    }

    public void setEditPriceListener(OnEditPriceListener editPriceListener) {
        this.editPriceListener = editPriceListener;
    }

    public static interface OnEditPriceListener {
        void onConfirm(BigDecimal value);
    }
}
