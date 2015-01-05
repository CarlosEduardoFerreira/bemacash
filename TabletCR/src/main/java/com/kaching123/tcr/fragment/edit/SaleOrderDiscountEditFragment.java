package com.kaching123.tcr.fragment.edit;

import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderDiscountCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.DiscountType;

import java.math.BigDecimal;

/**
 * Created by gdubina on 14/11/13.
 */
@EFragment
public class SaleOrderDiscountEditFragment extends BaseDiscountEditFragment {

    private static final String DIALOG_NAME = "soDiscountEditFragment";

    @Override
    protected void callCommand(String saleItemGuid, BigDecimal value) {
        DiscountType type = discountPercentType.isChecked() ? DiscountType.PERCENT : DiscountType.VALUE;
        UpdateSaleOrderDiscountCommand.start(getActivity(), saleItemGuid, value, type);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_so_discount_qty;
    }

    public static void show(FragmentActivity activity, String orderGuid, BigDecimal orderPrice, BigDecimal discount, DiscountType discountType) {
        DialogUtil.show(activity, DIALOG_NAME, SaleOrderDiscountEditFragment_.builder().saleItemGuid(orderGuid).maxValue(orderPrice).decimalValue(discount).discountType(discountType).build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
