package com.kaching123.tcr.fragment.edit;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomSwitch;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.util.KeyboardUtils;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 17.12.13.
 */
@EFragment
public class CashDrawerMovementEditFragment extends DecimalEditFragment {

    private static final String DIALOG_NAME = CashDrawerMovementEditFragment.class.getSimpleName();

    private static final BigDecimal MAX_VALUE = new BigDecimal("9999999.99");

    @FragmentArg
    protected BigDecimal cashInDrawer;

    @ViewById
    protected CustomSwitch movementTypeSwitch;
    @ViewById
    protected EditText commentBox;

    private MovementType movementType = MovementType.DROP;

    private OnEditAmountListener editAmountListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @AfterViews
    protected void initViews() {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    KeyboardUtils.hideKeyboard(v.getContext(), v);
            }
        });
        movementTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                movementType = !isChecked ? MovementType.DROP : MovementType.PAYOUT;
                checkPositiveButtonCondition();
            }
        });
    }

    @Override
    protected BigDecimal getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_cash_drawer_movement_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_cash_drawer_movements;
    }

    @Override
    protected BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        BigDecimal maxValue = getMaxValue();
        return value != null && (maxValue == null || value.compareTo(maxValue) != 1)
                && (cashInDrawer == null || value.compareTo(cashInDrawer) != 1)  ? value : null;
    }

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {
        if (editAmountListener != null) {
            editAmountListener.onConfirm(value.negate(), commentBox.getText().toString(), movementType);
        }
    }

    public void setEditAmountListener(OnEditAmountListener editAmountListener) {
        this.editAmountListener = editAmountListener;
    }

    public static void show(FragmentActivity activity, BigDecimal cashInDrawer, OnEditAmountListener listener) {
        CashDrawerMovementEditFragment fragment = CashDrawerMovementEditFragment_.builder().cashInDrawer(cashInDrawer).build();
        fragment.setEditAmountListener(listener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static interface OnEditAmountListener {
        void onConfirm(BigDecimal value, String comment, MovementType movementType);
    }

}
