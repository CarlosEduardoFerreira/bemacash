package com.kaching123.tcr.fragment.saleorder;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SettingsActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.device.KDSCommand;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.Permission;


/**
 * Created by mboychenko on 2/10/2017.
 */

public abstract class BaseOnHoldDialogFragment extends StyledDialogFragment {

    protected boolean printToKitchenFlag, printToKdsFlag;

    protected class KitchenKitchenPrintCallback extends PrintItemsForKitchenCommand.BaseKitchenPrintCallback {

        private KitchenPrintCallbackHelper.IKitchenPrintCallback skipListener = new KitchenPrintCallbackHelper.IKitchenPrintCallback() {

            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                onPrintSuccess();
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

    protected class KDSPrintCallback extends PrintOrderToKdsCommand.BasePrintOrderToKdsCallback {

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

    protected abstract void onPositiveHandler();
    protected abstract void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac);
    protected abstract void printItemToKds();
}
