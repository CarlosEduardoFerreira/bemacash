package com.kaching123.tcr.fragment;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

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
 * Created by hamsterksu on 28.01.14.
 */
public final class PrintCallbackHelper {

    private PrintCallbackHelper() {
    }

    public static void onPrintError(FragmentActivity activity, PrinterCommand.PrinterError errorType, final IRetryCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(getPrinterErrorMessage(errorType)),
                R.string.btn_try_again,
                new OnDialogClickListener() {

                    @Override
                    public boolean onClick() {
                        callback.onRetry(false);
                        return true;
                    }
                }
        );
    }

    public static void onPrintError(FragmentActivity activity, PrinterCommand.PrinterError errorType, final IRetryWithCancelCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(getPrinterErrorMessage(errorType)),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(false);
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

    public static void onPrintError(FragmentActivity activity, PrinterError errorType, final String fromPrinter, String aliasTitle, final IRetryWithSkipCallback callback) {
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(TextUtils.isEmpty(aliasTitle) ? getPrinterErrorMessage(errorType) : getPrinterErrorMessageWithName(errorType), aliasTitle),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter);
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter);
                        return true;
                    }
                }
        );
    }

    public static void onPrintError(FragmentActivity activity, PrinterError errorType, final IRetryFullCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(getPrinterErrorMessage(errorType)),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry();
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip();
                        return true;
                    }
                }
        );
    }

    public static void onPrintErrorOnlySkip(FragmentActivity activity, PrinterError errorType, final String fromPrinter, String aliasTitle, final IRetryWithSkipCallback callback) {
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(TextUtils.isEmpty(aliasTitle) ? getPrinterErrorMessage(errorType) : getPrinterErrorMessageWithName(errorType), aliasTitle),
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterNotConfigured(final FragmentActivity activity) {
        WaitDialogFragment.hide(activity);
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
                                    onPrinterNotConfigured(activity);
                                }
                            }, Permission.ADMIN);
                            return true;
                        }
                        SettingsActivity.start(activity);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterNotConfigured(final FragmentActivity activity, final IRetryWithCancelCallback callback) {
        WaitDialogFragment.hide(activity);
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
                                    onPrinterNotConfigured(activity);
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

    public static void onPrinterNotConfigured(final FragmentActivity activity, final String fromPrinter, String aliasTitle, final IRetryWithSkipCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(TextUtils.isEmpty(aliasTitle) ? R.string.printer_not_configured : R.string.printer_not_configured_with_name, aliasTitle),
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
                                    onPrinterNotConfigured(activity);
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
                        callback.onSkip(fromPrinter);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterNotConfigured(final FragmentActivity activity, final IRetryFullCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlertWithSkip(
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
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip();
                        return true;
                    }
                }
        );
    }

    public static void onPrinterDisconnected(FragmentActivity activity, final IRetryCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_disconnected),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(false);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterDisconnected(FragmentActivity activity, final IRetryWithCancelCallback callback) {
        AlertDialogFragment.showAlert(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_disconnected),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(false);
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

    public static void onPrinterDisconnected(FragmentActivity activity, final String fromPrinter, String aliasTitle, final IRetryWithSkipCallback callback) {
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_disconnected_with_name, aliasTitle),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterDisconnected(FragmentActivity activity, final IRetryFullCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.showAlertWithSkip(
                activity,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_disconnected),
                R.string.btn_try_again,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry();
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onCancel();
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip();
                        return true;
                    }
                }
        );
    }

    public static void onPrinterPaperNearTheEnd(final FragmentActivity activity, final IRetryCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.show(
                activity,
                DialogType.INFO2,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_paper_is_near_end),
                R.string.btn_continue,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(true);
                        return true;
                    }
                }
        );
    }

    public static void onPrinterPaperNearTheEnd(final FragmentActivity activity, final IRetryWithCancelCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.show(
                activity,
                DialogType.INFO2,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_paper_is_near_end),
                R.string.btn_continue,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(true);
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

    public static void onPrinterPaperNearTheEnd(final FragmentActivity activity, final String fromPrinter, String aliasTitle, final IRetryWithSkipCallback callback) {
        WaitDialogFragment.hide(activity);
        AlertDialogFragment.show(
                activity,
                DialogType.INFO2,
                R.string.error_dialog_title,
                activity.getString(R.string.error_message_printer_paper_is_near_end_with_name, aliasTitle),
                R.string.btn_continue,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onRetry(fromPrinter);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        callback.onSkip(fromPrinter);
                        return true;
                    }
                }, null
        );
    }

    public static int getPrinterErrorMessage(PrinterCommand.PrinterError errorType) {
        if (errorType == null)
            return R.string.error_message_printer_unknown;

        switch (errorType) {
            case NOT_CONFIGURED:
                return R.string.printer_not_configured;
            case DISCONNECTED:
                return R.string.error_message_printer_disconnected;
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

    public static int getPrinterErrorMessageWithName(PrinterCommand.PrinterError errorType){
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

    public static interface IRetryCallback {
        void onRetry(boolean ignorePaperEnd);
    }

    public static interface IRetryWithCancelCallback {
        void onRetry(boolean ignorePaperEnd);
        void onCancel();
    }

    public static interface IRetryWithSkipCallback {
        void onRetry(String fromPrinter);
        void onSkip(String fromPrinter);
    }

    public static interface IRetryFullCallback {
        void onRetry();
        void onSkip();
        void onCancel();
    }
}
