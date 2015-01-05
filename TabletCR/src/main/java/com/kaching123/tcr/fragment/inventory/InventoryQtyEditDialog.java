package com.kaching123.tcr.fragment.inventory;

import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.SignedQuantityFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.QtyEditFragment;

import java.math.BigDecimal;

import static com.kaching123.tcr.util.CalculationUtil.negativeQty;

/**
 * Created by gdubina on 04/02/14.
 */
@EFragment
public class InventoryQtyEditDialog extends QtyEditFragment {

    private static final String DIALOG_NAME = "inventoryQtyEditFragment";

    @Override
    protected void attachViews() {
        super.attachViews();
        editText.setFilters(new InputFilter[]{new SignedQuantityFormatInputFilter()});
        keyboard.setMinusVisible(true);
        editText.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                callInternalListener(adjust);
                return false;
            }
        });
    }

    protected BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        BigDecimal maxValue = getMaxValue();
        BigDecimal minValue = negativeQty(maxValue);
        return value != null && value.compareTo(minValue) != -1 && value.compareTo(maxValue) != 1 ? value : null;
    }

    @Override
    protected void enablePositiveButtons(boolean enable) {
        getNeutralButton().setEnabled(enable);
        getPositiveButton().setEnabled(enable);
        keyboard.setEnterEnabled(enable);

        getPositiveButton().setTextColor(enable ? normalBtnColor : disabledBtnColor);
        getNeutralButton().setTextColor(enable ? normalBtnColor : disabledBtnColor);
    }

    @Override
    protected boolean hasSkipButton() {
        return true;
    }

    @Override
    protected int getSkipButtonTitle() {
        return R.string.btn_replace;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_adjust;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return adjust;
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return replace;
    }

    private OnEditQtyListener listener;

    public void setListener(OnEditQtyListener listener) {
        this.listener = listener;
    }

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {

    }


    private OnDialogClickListener adjust = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            BigDecimal value = validateForm();
            if (value == null)
                return false;
            if(listener != null){
                listener.onAdjust(value);
            }
            return true;
        }
    };

    private OnDialogClickListener replace = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            BigDecimal value = validateForm();
            if (value == null)
                return false;
            if(listener != null){
                listener.onReplace(value);
            }
            return true;
        }
    };

    public static void show(FragmentActivity activity, BigDecimal qty, boolean isInteger, OnEditQtyListener onResultListener){
        DialogUtil.show(activity, DIALOG_NAME, InventoryQtyEditDialog_.builder().isInteger(isInteger).decimalValue(qty).build())
                .setListener(onResultListener);
    }

    public static interface OnEditQtyListener{
        void onReplace(BigDecimal value);
        void onAdjust(BigDecimal value);
    }

}
