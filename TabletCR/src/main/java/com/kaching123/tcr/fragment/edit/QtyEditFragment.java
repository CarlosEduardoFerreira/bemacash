package com.kaching123.tcr.fragment.edit;

import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.QuantityFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;

import java.math.BigDecimal;

import static com.kaching123.tcr.util.CalculationUtil.negativeQty;

/**
 * Created by gdubina on 14/11/13.
 */
@EFragment
public class QtyEditFragment extends DecimalEditFragment{

    private static final BigDecimal MAX_VALUE = new BigDecimal("9999.999");

    private static final String DIALOG_NAME = "qtyEditFragment";

    private OnEditQtyListener onEditQtyListener;

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

    @Override
    protected BigDecimal getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {
        if (onEditQtyListener != null)
            onEditQtyListener.onConfirm(value);
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

    public static void show(FragmentActivity activity, String saleItemGuid, BigDecimal qty, boolean isInteger, OnEditQtyListener onEditQtyListener) {
        DialogUtil.show(activity, DIALOG_NAME, QtyEditFragment_.builder().saleItemGuid(saleItemGuid).isInteger(isInteger).decimalValue(qty).build()).setOnEditQtyListener(onEditQtyListener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public void setOnEditQtyListener(OnEditQtyListener onEditQtyListener) {
        this.onEditQtyListener = onEditQtyListener;
    }

    public interface OnEditQtyListener {

        public void onConfirm(BigDecimal qty);

    }
}
