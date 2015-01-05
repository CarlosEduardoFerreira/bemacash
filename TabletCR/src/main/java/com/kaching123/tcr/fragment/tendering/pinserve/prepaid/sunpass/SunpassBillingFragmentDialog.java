package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass.DoSunPassDocumentPaymentCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass.DoSunPassReplenishmentCommand;
import com.kaching123.tcr.commands.wireless.WirelessConfirmationCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentPaymentResponse;
import com.kaching123.tcr.websvc.api.prepaid.ReplenishmentResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class SunpassBillingFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "SunpassBillingFragmentDialog";

    @FragmentArg
    protected SunPassDocumentPaymentRequest dRequest;
    protected SunpassBillingFragmentDialogCallback callback;
    @FragmentArg
    protected SunReplenishmentRequest request;
    @FragmentArg
    protected BalanceResponse response;
    @FragmentArg
    protected DocumentInquiryResponse dResponse;
    @FragmentArg
    protected SunpassType type;
    @FragmentArg
    protected BigDecimal transactionFee;
    @FragmentArg
    protected String orderGuid;

    public void setCallback(SunpassBillingFragmentDialogCallback callback) {
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

    private String mOrderNum;
    private String mTotal;

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

                switch (type) {
                    case SUNPASS_PAY_YOUR_DOCUMENT:
                        DoSunPassDocumentPaymentCommand.start(getActivity(), doSunPassDocumentPaymentCommandCallback, dRequest);
                        break;
                    case SUNPASS_TRANSPONDER:
                        DoSunPassReplenishmentCommand.start(getActivity(), doSunPassREplenishmentCommandCallback, request);
                        break;
                }
            }

            @Override
            protected void handleFailure() {

            }
        });

    }

    public interface SunpassBillingFragmentDialogCallback {

        public abstract void onError(String message);

        public abstract void onComplete(ReplenishmentResponse result, BalanceResponse balanceResponse, BigDecimal transactionFee, String orderNum, String total);

        public abstract void onComplete(DocumentPaymentResponse result, DocumentInquiryResponse balanceResponse, BigDecimal transactionFee, String orderNum, String total);
    }

    public static void show(FragmentActivity context,
                            SunReplenishmentRequest request,
                            SunpassType type,
                            String orderGuid,
                            SunpassBillingFragmentDialogCallback callback,
                            BalanceResponse response,
                            BigDecimal transactionFee,
                            SunPassDocumentPaymentRequest dRequest,
                            DocumentInquiryResponse dResponse) {
        SunpassBillingFragmentDialog dialog = SunpassBillingFragmentDialog_.builder()
                .request(request)
                .type(type)
                .response(response)
                .transactionFee(transactionFee)
                .orderGuid(orderGuid)
                .dRequest(dRequest)
                .dResponse(dResponse)
                .build();
        dialog.setCallback(callback);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected DoSunPassReplenishmentCommand.DoSunPassREplenishmentCommandCallback doSunPassREplenishmentCommandCallback = new DoSunPassReplenishmentCommand.DoSunPassREplenishmentCommandCallback() {

        @Override
        protected void handleSuccess(ReplenishmentResponse result) {
            callback.onComplete(result, response, transactionFee, mOrderNum, mTotal);
        }

        @Override
        protected void handleFailure(ReplenishmentResponse result) {
            callback.onError(result == null ? "Billing failed" : result.responseDescription);
        }
    };

    protected DoSunPassDocumentPaymentCommand.DoSunPassDocumentPaymentCommandCallback doSunPassDocumentPaymentCommandCallback = new DoSunPassDocumentPaymentCommand.DoSunPassDocumentPaymentCommandCallback()

    {

        @Override
        protected void handleSuccess(DocumentPaymentResponse result) {
            callback.onComplete(result, dResponse, transactionFee, mOrderNum, mTotal);
        }

        @Override
        protected void handleFailure(DocumentPaymentResponse result) {
            callback.onError(result == null ? "Billing failed" : result.responseDescription);
        }
    };

}
