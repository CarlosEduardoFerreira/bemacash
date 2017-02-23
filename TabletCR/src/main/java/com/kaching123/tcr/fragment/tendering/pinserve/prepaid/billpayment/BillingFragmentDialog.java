package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.BillPaymentCommand;
import com.kaching123.tcr.commands.wireless.WirelessConfirmationCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.websvc.api.prepaid.BillPaymentResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class BillingFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "BillingFragmentDialog";

    protected BillingFragmentDialogCallback callback;

    @FragmentArg
    protected BillPaymentRequest request;
    @FragmentArg
    protected BillPaymentItem chosenBillPaymentItem;
    @FragmentArg
    protected BigDecimal transactionFee;
    @FragmentArg
    protected String orderGuid;

    private String mOrderNum;

    private String mTotal;

    public void setCallback(BillingFragmentDialogCallback callback) {
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
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.base_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
    }

    @AfterViews
    protected void init() {
        WirelessConfirmationCommand.start(getActivity(), orderGuid,new WirelessConfirmationCommand.wirelessConfiramtionCommandCallback() {
            @Override
            protected void handleSuccess(String orderNum, String total) {
                mOrderNum = orderNum;
                mTotal = total;
                BillPaymentCommand.start(getActivity(), billpaymentCommandCallback, request);
            }

            @Override
            protected void handleFailure() {

            }
        });

        WaitDialogFragment.show(getActivity(), "Loading...");
    }

    public interface BillingFragmentDialogCallback {
        public void onError(String message);

        public void onComplete(BillPaymentResponse response, BigDecimal transactionFee, String orderNum, String total, BillPaymentItem chosenBillPaymentItem);
    }

    public static void show(FragmentActivity context,
                            BillPaymentRequest request,
                            BigDecimal transactionFee,
                            String orderGuid,
                            BillPaymentItem chosenBillPaymentItem,
                            BillingFragmentDialogCallback callback
                            ) {
        BillingFragmentDialog dialog = BillingFragmentDialog_.builder()
                .request(request)
                .transactionFee(transactionFee)
                .orderGuid(orderGuid)
                .chosenBillPaymentItem(chosenBillPaymentItem)
                .build();
        dialog.setCallback(callback);
        DialogUtil.show(context, DIALOG_NAME, dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected BillPaymentCommand.BillpaymentCommandCallback billpaymentCommandCallback = new BillPaymentCommand.BillpaymentCommandCallback()
    {

        @Override
        protected void handleSuccess(BillPaymentResponse result) {
            WaitDialogFragment.hide(getActivity());
            callback.onComplete(result, transactionFee, mOrderNum, mTotal, chosenBillPaymentItem);
        }

        @Override
        protected void handleFailure(BillPaymentResponse result) {
            WaitDialogFragment.hide(getActivity());
            callback.onError(result == null ? "Billing failed" : result.resultDescription);
        }
    };

}
