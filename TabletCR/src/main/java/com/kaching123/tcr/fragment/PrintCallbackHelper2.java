package com.kaching123.tcr.fragment;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SettingsActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.Permission;

/**
 * Created by vkompaniets on 31.07.2014.
 */
public class PrintCallbackHelper2 {

    private PrintCallbackHelper2() {
    }

    public static void onPrintError(FragmentActivity activity, PrinterCommand.PrinterError errorType, final IPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(getPrinterErrorMessage(errorType)),
                R.string.btn_try_again,
                new OnDialogClickListener() {

                    @Override
                    public boolean onClick() {
                        callback.onRetry(false, false);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                }
        );
    }

    public static void onPrinterNotConfigured(final FragmentActivity activity, final IPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.printer_not_configured),
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
                                    onPrinterNotConfigured(activity, callback);
                                }
                            }, Permission.ADMIN);
                            return true;
                        }
                        SettingsActivity.start(activity);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                }
        );
    }

    public static void onPrinterDisconnected(FragmentActivity activity, final IPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_disconnected),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(false, false);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                }
        );
    }

    public static void onPrinterPaperNearTheEnd(final FragmentActivity activity, final IPrintCallback callback) {
        hideWaitDialog(activity);
        AlertDialogFragment.show(
                activity,
                DialogType.INFO2,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_paper_is_near_end),
                R.string.btn_continue,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(false, true);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                }, null
        );
    }

    public static void onPrinterIPnotFound(final FragmentActivity activity, final IPrintCallback callback){
        hideWaitDialog(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_ip_not_found),
                R.string.btn_ok,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(true, true);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                }
        );
    }

    private static void hideWaitDialog(FragmentActivity activity){
        WaitDialogFragment.hide(activity);
    }

    private static int getPrinterErrorMessage(PrinterCommand.PrinterError errorType) {
        if (errorType == null)
            return R.string.error_message_printer_unknown;

        switch (errorType) {
            case NOT_CONFIGURED:
                return R.string.printer_not_configured;
            case DISCONNECTED:
                return R.string.error_message_printer_disconnected;
            case IP_NOT_FOUND:
                return R.string.error_message_printer_ip_not_found;
            case NO_PAPER:
                return R.string.error_message_printer_no_paper;
            case PAPER_IS_NEAR_END:
                return R.string.error_message_printer_paper_is_near_end;
            case HEAD_OVERHEATED:
                return R.string.error_message_printer_head_overheated;
            case CUTTER_ERROR:
                return R.string.error_message_printer_cutter_error;
            case COVER_IS_OPENED:
                return R.string.error_message_printer_cover_is_opened;
            case BUSY:
                return R.string.error_message_printer_busy;
            case OFFLINE:
                return R.string.error_message_printer_offline;
            default:
                return R.string.error_message_printer_busy;
        }
    }

    public static interface IPrintCallback {
        void onRetry(boolean searchByMac, boolean ignorePaperEnd);
        void onCancel();
    }
}
