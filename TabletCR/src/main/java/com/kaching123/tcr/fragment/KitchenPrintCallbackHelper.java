package com.kaching123.tcr.fragment;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SettingsActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.Permission;

/**
 * Created by vkompaniets on 01.08.2014.
 */
public class KitchenPrintCallbackHelper {

    private KitchenPrintCallbackHelper(){
    }

    public static void onPrintError(FragmentActivity activity, PrinterError errorType, final String fromPrinter, String aliasTitle, final IKitchenPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(getPrinterErrorMessage(errorType), aliasTitle),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter, false, false);
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter, false, false);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterNotConfigured(final FragmentActivity activity, final String fromPrinter, final String aliasTitle, final IKitchenPrintCallback callback){
        hideWaitDialog(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.printer_not_configured_with_name, aliasTitle),
                R.string.btn_configure,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        boolean adminPermitted = ((TcrApplication) activity.getApplicationContext()).hasPermission(Permission.ADMIN);
                        if (!adminPermitted) {
                            PermissionFragment.showCancelable(activity, new SuperBaseActivity.BaseTempLoginListener(activity) {
                                @Override
                                public void onLoginComplete() {
                                    super.onLoginComplete();
                                    onPrinterNotConfigured(activity, fromPrinter, aliasTitle, callback);
                                }
                            }, Permission.ADMIN);
                            return true;
                        }
                        SettingsActivity.start(activity);
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter, false, false);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterDisconnected(FragmentActivity activity, final String fromPrinter, String aliasTitle, final IKitchenPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_disconnected_with_name, aliasTitle),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter, true, true);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter, false, false);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterIPnotfound(FragmentActivity activity, final String fromPrinter, String aliasTitle, final IKitchenPrintCallback callback){
        hideWaitDialog(activity);
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_ip_not_found_with_name, aliasTitle),
                R.string.btn_ok,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter, true, true);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter, false, false);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterPaperNearTheEnd(final FragmentActivity activity, final String fromPrinter, String aliasTitle, final IKitchenPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.show(
                activity,
                DialogType.INFO2,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_paper_is_near_end_with_name, aliasTitle),
                R.string.btn_continue,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter, true, false);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter, false, false);
                        return true;
                    }
                }, null
        );
    }

    private static void hideWaitDialog(FragmentActivity activity){
        WaitDialogFragment.hide(activity);
    }

    private static int getPrinterErrorMessage(PrinterCommand.PrinterError errorType){
        if (errorType == null)
            return R.string.error_message_printer_unknown_with_name;

        switch (errorType) {
            case NOT_CONFIGURED:
                return R.string.printer_not_configured_with_name;
            case DISCONNECTED:
                return R.string.error_message_printer_disconnected_with_name;
            case NO_PAPER:
                return R.string.error_message_printer_no_paper_with_name;
            case PAPER_IS_NEAR_END:
                return R.string.error_message_printer_paper_is_near_end_with_name;
            case HEAD_OVERHEATED:
                return R.string.error_message_printer_head_overheated_with_name;
            case CUTTER_ERROR:
                return R.string.error_message_printer_cutter_error_with_name;
            case COVER_IS_OPENED:
                return R.string.error_message_printer_cover_is_opened_with_name;
            case BUSY:
                return R.string.error_message_printer_busy_with_name;
            case OFFLINE:
                return R.string.error_message_printer_offline_with_name;
            default:
                return R.string.error_message_printer_busy_with_name;
        }
    }

    public static interface IKitchenPrintCallback{
        void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac);
        void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac);
    }

}
