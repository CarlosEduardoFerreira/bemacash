package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity.IHoldListener;
import com.kaching123.tcr.activity.SettingsActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.device.KDSCommand;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.OnHoldStatus;
import com.kaching123.tcr.model.Permission;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.util.PhoneUtil.onlyDigits;

/**
 * Created by mboychenko on 2/3/2017.
 */
@EFragment
public class AddOnHoldDialogFragment extends BaseOnHoldDialogFragment {

    private static final String DIALOG_NAME = "addOnHoldDialog";

    private IHoldListener listener;

    @ViewById
    protected TextView toGo;
    @ViewById
    protected TextView dineIn;

    @ViewById
    protected EditText orderTitle;
    @ViewById
    protected EditText orderPhone;


    @FragmentArg
    protected String argOrderGuid;

    @FragmentArg
    protected String argOrderTitle;

    @FragmentArg
    protected String argOrderPhone;

    @FragmentArg
    protected OnHoldStatus argOrderHoldStatus;

    @FragmentArg
    protected boolean hasKitchenPrintable;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_heigth));
        initFields();
        initListeners();
    }

    private void initListeners() {
        dineIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dineIn.setActivated(!dineIn.isActivated());
                if(dineIn.isActivated()){
                    toGo.setActivated(false);
                }
            }
        });
        toGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toGo.setActivated(!toGo.isActivated());
                if(toGo.isActivated()){
                    dineIn.setActivated(false);
                }
            }
        });
    }

    private void initFields() {
        orderTitle.setText(argOrderTitle);
        orderTitle.selectAll();

        orderPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        if(!TextUtils.isEmpty(argOrderPhone)) {
            orderPhone.setText(argOrderPhone);
        }

        if(argOrderHoldStatus != null) {
            switch (argOrderHoldStatus) {
                case TO_STAY:
                    dineIn.setActivated(true);
                    break;
                case TO_GO:
                    toGo.setActivated(true);
                    break;
            }
        }
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.add_on_hold_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_add_hold_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_new_order;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                    if (getApp().getShopInfo().onHoldStatusMandatory && getOnHoldStatus() == OnHoldStatus.NONE) {
                        Toast.makeText(getContext(), "On Hold status is mandatory, please choose one. To Stay or To Go", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    printItemsToKitchen(null, false, false, false);
                    printItemToKds();
                    return false;
            }
        };
    }

    protected void onPositiveHandler(){
        if (listener != null) {
            listener.onSwap2Order(orderTitle.getText().toString(), onlyDigits(orderPhone.getText().toString()), getOnHoldStatus(), null, null);
        }
    }

    private OnHoldStatus getOnHoldStatus(){
        OnHoldStatus status = OnHoldStatus.NONE;
        if(dineIn.isActivated()) {
            status = OnHoldStatus.TO_STAY;
        } else if(toGo.isActivated()) {
            status = OnHoldStatus.TO_GO;
        }
        return status;
    }

    protected void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
        printToKitchenFlag = false;
        Logger.e("CEF.HoldFragmentDialog.printItemsToKitchen:printOnholdOrders " + getApp().getShopInfo().printOnholdOrders);
        /*
         *   Added if condition to print only if "Receipt Settings" configuration is seted "Print Kitchen Receipt for On Hold Orders" = enabled
         */
        if(getApp().getShopInfo().printOnholdOrders) {
            WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        }
        PrintItemsForKitchenCommand.itComesFromPay = false;
        PrintItemsForKitchenCommand.start(getActivity(), skipPaperWarning, searchByMac, argOrderGuid, fromPrinter, skip,
                new KitchenKitchenPrintCallback(), false, orderTitle.getText().toString(), getOnHoldStatus(), orderPhone.getText().toString());
    }

    protected void printItemToKds(){
        printToKdsFlag = false;
        PrintOrderToKdsCommand.start(getActivity(), argOrderGuid, false, new KDSPrintCallback());
    }

    public void setListener(IHoldListener listener) {
        this.listener = listener;
    }

    public static void show(FragmentActivity context, String orderGuid, String holdTitle, String holdPhone, OnHoldStatus status, boolean hasKitchenPrintable, IHoldListener listener) {
        DialogUtil.show(context, DIALOG_NAME, AddOnHoldDialogFragment_.builder()
                .argOrderGuid(orderGuid)
                .argOrderTitle(holdTitle)
                .argOrderPhone(holdPhone)
                .argOrderHoldStatus(status)
                .hasKitchenPrintable(hasKitchenPrintable)
                .build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
