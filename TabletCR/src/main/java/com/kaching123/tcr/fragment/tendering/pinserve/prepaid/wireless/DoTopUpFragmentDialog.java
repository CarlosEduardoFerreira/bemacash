package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.DoTopUpCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.PINCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.PINCommand.PinCommandCallback;
import com.kaching123.tcr.commands.wireless.WirelessConfirmationCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoTopUpRequest;
import com.kaching123.tcr.websvc.api.prepaid.PIN;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class DoTopUpFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "DoTopUpFragmentDialog";

    protected DoTopUpFragmentDialogCallback callback;
    @FragmentArg
    protected DoTopUpRequest request;
    @FragmentArg
    protected String orderGuid;
    private String mOrderNum;
    private String mTotal;


    public void setCallback(DoTopUpFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_cc_in_progress_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_progress_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
    }

    @AfterViews
    protected void init() {
        WirelessConfirmationCommand.start(getActivity(), orderGuid, new WirelessConfirmationCommand.wirelessConfiramtionCommandCallback() {
            @Override
            protected void handleSuccess(String orderNum, String total) {
                mOrderNum = orderNum;
                mTotal = total;
                if (request.type.isPin()) {
                    PINCommand.start(getActivity(), pinCallback, request);
                } else {
                    DoTopUpCommand.start(getActivity(), doCallback, request);
                }
            }

            @Override
            protected void handleFailure() {

            }
        });
    }

    private PinCommandCallback pinCallback = new PinCommandCallback() {

        @Override
        protected void handleSuccess(PIN response) {
            callback.onPrintPin(response, mOrderNum, mTotal);
        }

        @Override
        protected void handleFailure(PIN response) {
            callback.onError(response == null ? "Billing failed" : response.errorMessage);
        }
    };
    private DoTopUpCommand.doCommandCallback doCallback = new DoTopUpCommand.doCommandCallback() {

        @Override
        protected void handleSuccess(PIN response) {
            callback.onComplete(response, mOrderNum, mTotal);
        }

        @Override
        protected void handleFailure(PIN response) {
            callback.onError(response == null ? "Billing failed" : response.errorMessage);
        }
    };

    public interface DoTopUpFragmentDialogCallback {

        public abstract void onError(String message);

        public abstract void onPrintPin(PIN data2print, String orderNum, String total);

        public abstract void onComplete(PIN result, String orderNum, String total);
    }


    public static void show(FragmentActivity context,
                            DoTopUpRequest request,
                            String orderGuid,
                            DoTopUpFragmentDialogCallback callback) {
        DoTopUpFragmentDialog dialog = DoTopUpFragmentDialog_.builder()
                .request(request)
                .orderGuid(orderGuid)
                .build();
        dialog.setCallback(callback);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
