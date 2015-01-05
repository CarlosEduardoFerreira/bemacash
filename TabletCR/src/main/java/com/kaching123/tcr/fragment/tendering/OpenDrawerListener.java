package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.device.WaitForCashInDrawerCommand.BaseWaitForCashInDrawerCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.telly.groundy.TaskHandler;

/**
 * @author Ivan v. Rikhmayer
 */
public class OpenDrawerListener extends BaseWaitForCashInDrawerCallback {

    private IDrawerFriend parent;

    public OpenDrawerListener(IDrawerFriend parent) {
        this.parent = parent;
    }

    @Override
    protected void onDrawerIPnotFound() {
        parent.setHandler(null);
        parent.onFailure();
        AlertDialogFragment.showAlert(
                parent.getActivity(),
                R.string.open_drawer_error_title,
                parent.getActivity().getString(R.string.error_message_drawer_ip_not_found),
                R.string.btn_ok,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        parent.try2GetCash(true);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        parent.onPopupCancelled();
                        return true;
                    }
                }
        );
    }

    @Override
    protected void onDrawerOpened() {
        Logger.d("OpenDrawerListener: onDrawerOpened()");
        parent.setHandler(null);
        parent.onDrawerOpened();
    }

    @Override
    protected void onDrawerClosed() {
        Logger.d("OpenDrawerListener: onDrawerClosed()");
        parent.setHandler(null);
        parent.onCashReceived();
    }

    @Override
    protected void onDrawerTimeoutError() {
        Logger.d("OpenDrawerListener: onDrawerTimeoutError()");
        if (parent.getActivity() == null)
            return;
        handleError(R.string.close_drawer_error_title, parent.getActivity().getString(R.string.close_drawer_error_msg_close_id));
    }

    @Override
    protected void onDrawerCloseError(PrinterError error) {
        Logger.d("OpenDrawerListener: onDrawerCloseError()");
        if (parent.getActivity() == null)
            return;
        handleError(R.string.open_drawer_error_title, parent.getActivity().getString(PrintCallbackHelper.getPrinterErrorMessage(error)));

    }

    private void handleError(int title, String message){
        parent.setHandler(null);
        parent.onFailure();
        AlertDialogFragment.showAlertWithSkip(parent.getActivity(), title, message,
                R.string.btn_try_again, new OnDialogClickListener() {

                    @Override
                    public boolean onClick() {
                        parent.try2GetCash(false);
                        return true;
                    }
                },
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        parent.onPopupCancelled();
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        onDrawerClosed();
                        return true;
                    }
                }
        );
    }

    /**
     * @author Ivan v. Rikhmayer
     *         This class is intended to
     */
    public interface IDrawerFriend {

        void onDrawerOpened();

        void onFailure();

        void onPopupCancelled();

        void onCashReceived();

        void cancelWaitCashTask();

        boolean try2GetCash(boolean searchByMac);

        TaskHandler getHandler();

        void setHandler(TaskHandler handler);

        FragmentActivity getActivity();
    }
}