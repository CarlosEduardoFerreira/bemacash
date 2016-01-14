package com.kaching123.tcr.fragment.edit;

import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.QuantityFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.saleorder.OrderItemListFragment;

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

    @FragmentArg
    protected boolean isEnable;
    private OrderItemListFragment orderItemListFragment;

    public QtyEditFragment() {
    }

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
        editText.addTextChangedListener(new BrandTextWatcher(editText));
       // editText.setFilters(new InputFilter[]{new QuantityFormatInputFilter()});
    /*    if(!isEnable) {
            getNegativeButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderItemListFragment.doRemoceClickLine(saleItemGuid);
                    dismiss();
                }
            });
        }*/
        editText.setKeyboardSupportConteiner(this);
        keyboard.attachEditView(editText);
        keyboard.setDotEnabled(!isInteger);
        editText.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                callInternalListener(submitListener);
                return false;
            }
        });
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
        String text = editText.getText().toString().replaceAll(",", "");;
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
        QtyEditFragment fragment = QtyEditFragment_.builder().saleItemGuid(saleItemGuid).isInteger(isInteger).decimalValue(qty).isEnable(true).build();
        DialogUtil.show(activity, DIALOG_NAME, fragment).setOnEditQtyListener(onEditQtyListener);
    }
    public static void showCancelable(FragmentActivity activity, String saleItemGuid, BigDecimal qty, boolean isInteger, OnEditQtyListener onEditQtyListener, OrderItemListFragment orderItemListFragment) {
        QtyEditFragment fragment = QtyEditFragment_.builder().saleItemGuid(saleItemGuid).isInteger(isInteger).decimalValue(qty).isEnable(false).build();
        fragment.orderItemListFragment = orderItemListFragment;
        DialogUtil.show(activity, DIALOG_NAME, fragment).setOnEditQtyListener(onEditQtyListener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public void setOnEditQtyListener(OnEditQtyListener onEditQtyListener) {
        this.onEditQtyListener = onEditQtyListener;
    }

    public interface OnEditQtyListener {

         void onConfirm(BigDecimal qty);

    }
}
