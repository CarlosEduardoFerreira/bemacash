package com.kaching123.tcr.fragment.edit;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Switch;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderTaxStatusCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.Permission;

/**
 * Created by gdubina on 20/11/13.
 */
@EFragment
public class TaxEditFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "taxEditFragment";

    @ViewById
    protected Switch taxSwitch;

    @FragmentArg
    protected String orderGuid;

    @FragmentArg
    protected boolean oldStatus;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.keyboard_popup_width),
                getDialog().getWindow().getAttributes().height);
        taxSwitch.setChecked(oldStatus);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_tax_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_edit_tax;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                boolean editTaxtPermitted = getApp().hasPermission(Permission.SALES_TAX);
                if (!editTaxtPermitted) {
                    PermissionFragment.showCancelable(getActivity(), new SuperBaseActivity.BaseTempLoginListener(getActivity()), Permission.SALES_TAX);
                    return false;
                }
                UpdateSaleOrderTaxStatusCommand.start(getActivity(), orderGuid, taxSwitch.isChecked());
                return true;
            }
        };
    }

    public static void show(FragmentActivity activity, String orderGuid, boolean state){
        DialogUtil.show(activity, DIALOG_NAME, TaxEditFragment_.builder().orderGuid(orderGuid).oldStatus(state).build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
