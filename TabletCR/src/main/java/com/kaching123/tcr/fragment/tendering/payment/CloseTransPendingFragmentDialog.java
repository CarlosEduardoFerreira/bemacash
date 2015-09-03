package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackClosePreauthCommand.BaseClosePreauthCallback;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneAddTipsCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorAddTipsCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

@EFragment
public class CloseTransPendingFragmentDialog extends TransactionPendingFragmentDialogBase<CloseTransPendingFragmentDialog, PreauthResponse> {

    private static final String DIALOG_NAME = CloseTransPendingFragmentDialog.class.getSimpleName();

    @ViewById
    protected TextView message;

    @FragmentArg
    protected PaymentTransactionModel transactionModel;
    @FragmentArg
    protected TipsModel tips;
    @FragmentArg
    SaleActionResponse reloadResponse;

    private ICloseProgressListener closeListener;

    public CloseTransPendingFragmentDialog setCloseListener(ICloseProgressListener listener) {
        this.closeListener = listener;
        return this;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @AfterViews
    protected void initViews() {
        if (!getApp().isPaxConfigured())
            return;

        message.setSingleLine(false);
        if (tips != null)
            message.setText(getString(R.string.pax_add_tips_instructions, transactionModel == null ? "" : transactionModel.getGuid(), UiHelper.priceFormat(tips.amount)));
        else
            message.setText(getString(R.string.pax_close_instructions, transactionModel == null ? "" : transactionModel.getGuid()));
    }

    private Object returnPaxCallBack() {
        if (!TcrApplication.get().isBlackstonePax()) {
            return closePreauthPaxProcessorCallback;
        }

        return closePreauthPaxBlackstoneCallback;

    }

    @Override
    protected void doCommand() {
        if (getApp().isPaxConfigured()) {
            if (transactionModel == null || PaymentGateway.PAX != transactionModel.gateway) {
                failed(getString(R.string.blackstone_pax_failure_reason_wrong_gateway));
                return;
            }
            PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();

            paxGateway.closePreauth(getActivity(), returnPaxCallBack(), transactionModel,
                    tips == null ? CalculationUtil.value(BigDecimal.ZERO) : tips.amount, tips == null ? null : tips.comment, tips == null ? null : tips.employeeId,
                    reloadResponse);
            return;
        }

        if (transactionModel == null || PaymentGateway.BLACKSTONE != transactionModel.gateway) {
            failed(getString(R.string.blackstone_failure_reason_wrong_gateway));
            return;
        }
        BlackGateway gateway = (BlackGateway) PaymentGateway.BLACKSTONE.gateway();
        gateway.closePreauth(getActivity(), closePreauthCallback, TcrApplication.get().getBlackStoneUser(), transactionModel,
                tips == null ? CalculationUtil.value(BigDecimal.ZERO) : tips.amount, tips == null ? null : tips.comment, tips == null ? null : tips.employeeId);
    }

    private void complete(TransactionStatusCode responseCode, ErrorReason errorReason) {
        if (closeListener != null) {
            closeListener.onComplete(responseCode, errorReason);
        }
    }

    private void complete(TransactionStatusCode responseCode, PaxGateway.Error error) {
        if (closeListener != null) {
            closeListener.onComplete(responseCode, error);
        }
    }

    private void failed(String errorMessage) {
        if (closeListener != null) {
            closeListener.onFailure(errorMessage);
        }
    }

    private BaseClosePreauthCallback closePreauthCallback = new BaseClosePreauthCallback() {

        @Override
        protected void handleSuccess(TransactionStatusCode responseCode) {
            Logger.d("BaseClosePreauthCallback.handleSuccess(): responseCode: ", responseCode);
            complete(responseCode, (ErrorReason) null);
        }

        @Override
        protected void handleFailure(TransactionStatusCode responseCode, ErrorReason errorReason) {
            Logger.e("BaseClosePreauthCallback.handleFailure(): responseCode: " + responseCode + ", errorReason: " + errorReason);
            complete(responseCode, errorReason);
        }
    };

    private PaxBlackstoneAddTipsCommand.PaxTipsCommandBaseCallback closePreauthPaxBlackstoneCallback = new PaxBlackstoneAddTipsCommand.PaxTipsCommandBaseCallback() {

        @Override
        protected void handleSuccess(TransactionStatusCode responseCode) {
            Logger.d("PaxTipsBlackstoneCommandBaseCallback.handleSuccess(): responseCode: ", responseCode);
            complete(responseCode, (PaxGateway.Error) null);
        }

        @Override
        protected void handleError(PaxGateway.Error error, TransactionStatusCode errorCode) {
            Logger.e("PaxTipsBlackstoneCommandBaseCallback.handleFailure(): errorCode: " + errorCode + ", error: " + error);
            complete(errorCode, error);
        }

    };
    private PaxProcessorAddTipsCommand.PaxTipsCommandBaseCallback closePreauthPaxProcessorCallback = new PaxProcessorAddTipsCommand.PaxTipsCommandBaseCallback() {

        @Override
        protected void handleSuccess(TransactionStatusCode responseCode) {
            Logger.d("PaxTipsProcessorCommandBaseCallback.handleSuccess(): responseCode: ", responseCode);
            complete(responseCode, (PaxGateway.Error) null);
        }

        @Override
        protected void handleError(PaxGateway.Error error, TransactionStatusCode errorCode) {
            Logger.e("PaxTipsProcessorCommandBaseCallback.handleFailure(): errorCode: " + errorCode + ", error: " + error);
            complete(errorCode, error);
        }

    };

    public interface ICloseProgressListener {

        void onComplete(TransactionStatusCode responseCode, ErrorReason errorReason);

        void onComplete(TransactionStatusCode responseCode, PaxGateway.Error error);

        void onFailure(String errorMessage);
    }

    public static void show(FragmentActivity context,
                            PaymentTransactionModel transactionModel,
                            TipsModel tips,
                            SaleActionResponse reloadResponse,
                            ICloseProgressListener listener) {
        DialogUtil.show(context, DIALOG_NAME, CloseTransPendingFragmentDialog_.builder().transactionModel(transactionModel).tips(tips).reloadResponse(reloadResponse).build())
                .setCloseListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
