package com.kaching123.tcr.fragment.saleorder;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity;
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

/**
 * Created by mboychenko on 2/3/2017.
 */
@EFragment
public class AddOnHoldDialogFragment extends StyledDialogFragment {

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

    private boolean printToKitchenFlag, printToKdsFlag;

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
        if (TextUtils.isEmpty(argOrderGuid)) {
            orderTitle.setVisibility(View.GONE);
        } else {
            orderTitle.setText(argOrderTitle);
        }

        if(!TextUtils.isEmpty(argOrderPhone)) {
            orderPhone.setText(argOrderPhone);
        }

        if(argOrderHoldStatus != null) {
            switch (argOrderHoldStatus) {
                case DINE_IN:
                    dineIn.setActivated(true);
                    break;
                case TO_GO:
                    toGo.setActivated(true);
                    break;
            }
        }
        orderTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_DONE == actionId){
//                    if (printBox.isChecked()) {
                        printItemsToKitchen(null, false, false, false);
                        printItemToKds();
                        return false;
//                    }
//                    onPositiveHandler();
//                    return true;
                }
                return false;
            }
        });
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
//                if (printBox.isChecked()){
                    printItemsToKitchen(null, false, false, false);
                    printItemToKds();
                    return false;
//                }
//                onPositiveHandler();
//                return true;
            }
        };
    }

    private void onPositiveHandler(){
        if (listener != null) {

            OnHoldStatus status = OnHoldStatus.NONE;
            if(dineIn.isActivated()) {
                status = OnHoldStatus.DINE_IN;
            } else if(toGo.isActivated()) {
                status = OnHoldStatus.TO_GO;
            }
            listener.onSwap2Order(orderTitle.getText().toString(), orderPhone.getText().toString(), status, null, null);
        }
    }

    private void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
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
                new KitchenKitchenPrintCallback(), false, orderTitle.getText().toString());
    }

    private void printItemToKds(){
        printToKdsFlag = false;
        PrintOrderToKdsCommand.start(getActivity(), argOrderGuid, false, new KDSPrintCallback());
    }

    private class KitchenKitchenPrintCallback extends PrintItemsForKitchenCommand.BaseKitchenPrintCallback {

        private KitchenPrintCallbackHelper.IKitchenPrintCallback skipListener = new KitchenPrintCallbackHelper.IKitchenPrintCallback() {

            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
//                printItemsToKitchen(fromPrinter, true, ignorePaperEnd, searchByMac);
            }
        };

        @Override
        protected void onPrintSuccess() {
            printToKitchenFlag = true;
            if(printToKdsFlag){
                WaitDialogFragment.hide(getActivity());
                onPositiveHandler();
                dismiss();
            }
        }

        @Override
        protected void onPrintError(PrinterCommand.PrinterError error, String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrintError(getActivity(), error, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterNotConfigured(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterNotConfigured(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterDisconnected(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterDisconnected(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterIPnotFound(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterIPnotfound(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(getActivity(), fromPrinter, aliasTitle, skipListener);
        }
    }

    private class KDSPrintCallback extends PrintOrderToKdsCommand.BasePrintOrderToKdsCallback {

        @Override
        protected void onDigitalPrintSuccess() {
            printToKdsFlag = true;
            if(printToKitchenFlag){
                WaitDialogFragment.hide(getActivity());
                onSkip();
            }
        }

        @Override
        protected void onDigitalPrintError(KDSCommand.KDSError error) {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlertWithSkip(
                    getActivity(),
                    R.string.error_dialog_title,
                    "Cannot connect to the host " + getApp().getShopPref().kdsRouterIp().getOr(""),
                    R.string.btn_try_again,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            printItemToKds();
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            onSkip();
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onKdsNotConfigured() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.error_dialog_title,
                    getActivity().getString(R.string.kds_station_not_configured),
                    R.string.btn_configure,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            boolean adminPermitted = ((TcrApplication) getActivity().getApplicationContext()).hasPermission(Permission.ADMIN);
                            if (!adminPermitted) {
                                PermissionFragment.showCancelable(getActivity(), new SuperBaseActivity.BaseTempLoginListener(getActivity()) {
                                    @Override
                                    public void onLoginComplete() {
                                        super.onLoginComplete();
                                        onKdsNotConfigured();
                                    }
                                }, Permission.ADMIN);
                                return true;
                            }
                            SettingsActivity.start(getActivity());
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onRouterNotConfigured() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.error_dialog_title,
                    getActivity().getString(R.string.kds_router_not_configured),
                    R.string.btn_configure,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            boolean adminPermitted = ((TcrApplication) getActivity().getApplicationContext()).hasPermission(Permission.ADMIN);
                            if (!adminPermitted) {
                                PermissionFragment.showCancelable(getActivity(), new SuperBaseActivity.BaseTempLoginListener(getActivity()) {
                                    @Override
                                    public void onLoginComplete() {
                                        super.onLoginComplete();
                                        onRouterNotConfigured();
                                    }
                                }, Permission.ADMIN);
                                return true;
                            }
                            SettingsActivity.start(getActivity());
                            return true;
                        }
                    }
            );
        }

        private void onSkip(){
            onPositiveHandler();
            dismiss();
        }
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
