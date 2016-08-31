package com.kaching123.tcr.fragment.tendering.voiding;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.device.WaitForCashInDrawerCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackVoidCommand;
import com.kaching123.tcr.commands.payment.cash.CashVoidCommand;
import com.kaching123.tcr.commands.payment.other.CheckVoidCommand;
import com.kaching123.tcr.commands.payment.other.OfflineCreditVoidCommand;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneRefundCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorRefundCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener.IDrawerFriend;
import com.kaching123.tcr.fragment.tendering.refund.RefundCreditReceiptFragmentDialog;
import com.kaching123.tcr.fragment.tendering.refund.RefundCreditReceiptFragmentDialog.IRefundCreditProgressListener;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoFullRefundResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.util.AnimationUtils;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class VoidProcessingFragmentDialog extends StyledDialogFragment implements IDrawerFriend {

    private static final String DIALOG_NAME = "VoidProcessingFragmentDialog";

    @ViewById
    protected ImageView sign;

    @ViewById
    protected ProgressBar progressBar;

    @ViewById
    protected RelativeLayout amountHolder;

    @ViewById
    protected TextView total;

    @ViewById
    protected TextView message;

    @ViewById
    protected TextView description;

    @ViewById
    protected ViewFlipper flipper;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;

    @FragmentArg
    protected boolean needToCancel;

    private TaskHandler waitCashTask;
    private SaleOrderModel childOrderModel;

    private LinkedBlockingQueue<PaymentTransactionModel> creditCardTransactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    private LinkedBlockingQueue<PaymentTransactionModel> paxTransactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    private LinkedBlockingQueue<PaymentTransactionModel> cashTransactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    private ArrayList<PaymentTransactionModel> creditRecdeiptTransactions = new ArrayList<PaymentTransactionModel>();
    private LinkedBlockingQueue<PaymentTransactionModel> offlineCreditTransactions = new LinkedBlockingQueue<PaymentTransactionModel>();
    private LinkedBlockingQueue<PaymentTransactionModel> checkTransactions = new LinkedBlockingQueue<PaymentTransactionModel>();

    //private Map<PaymentTransactionModel, Pair<DoVoidResponse, ErrorReason>> responses;
    private IVoidProgressListener listener;
    private List<PaymentTransactionModel> voidedTransactions = new ArrayList<PaymentTransactionModel>();
    private User user;
    private int max;
    private int current;
    private boolean failedCreditCardTransactions;
    private boolean failedPaxTransactions;
    private boolean failedOfflineCreditTransactions;
    private boolean failedCheckTransactions;
    private boolean skipCashTransactions;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.voiding_processor;
    }

    public VoidProcessingFragmentDialog setChildOrderModel(SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        enableButton(false);
        AnimationUtils.applyFlippingEffect(getActivity(), flipper);
        max = paxTransactions.size() + creditCardTransactions.size() + (int) Math.signum(cashTransactions.size());
        Logger.d("setting maximum of %d", max);
        message.setText(String.format("voided %d of %d", 0, max));
        next();
    }

    private boolean complete() {
        if (listener == null) {
            return false;
        }
        enableButton(true);
        flipper.setDisplayedChild(2);
        if (voidedTransactions.isEmpty()) { // no transactions were voided
            description.setText(R.string.void_result_fail);
            amountHolder.setVisibility(View.INVISIBLE);
            sign.setImageResource(R.drawable.error_payment);
        } else if (skipCashTransactions
                || failedCreditCardTransactions
                || failedOfflineCreditTransactions
                || failedCheckTransactions
                || failedPaxTransactions) { // some was with error
            description.setText(R.string.void_result_partially);
            amountHolder.setVisibility(View.INVISIBLE);
            sign.setImageResource(R.drawable.error_payment);
        } else {                                                            // all ok
            description.setText(R.string.void_result_ok);
        }
        getNegativeButton().setVisibility(View.VISIBLE);
        return true;
    }
    private Object returnPaxCallBack () {
        if (!TcrApplication.get().isBlackstonePax()) {
            return new PaxProcessorRefundCommand.PaxREFUNDCommandBaseCallback() {

                @Override
                protected void handleSuccess(SaleOrderModel childOrderModel,
                                             PaymentTransactionModel childTransactionModel,
                                             Transaction transaction,
                                             String errorMessage) {
                    if (childOrderModel != null) {
                        VoidProcessingFragmentDialog.this.childOrderModel = childOrderModel;
                        message.setText(String.format("voided %d of %d", ++current, max));
                        voidedTransactions.add(childTransactionModel);
                    } else {
                        failedPaxTransactions = true;
                    }
                    next();
                }

                @Override
                protected void handleError() {
                    message.setText(String.format("voided %d of %d", ++current, max));
                    failedPaxTransactions = true;
                    next();
                }
            };
        }

        return new PaxBlackstoneRefundCommand.PaxREFUNDCommandBaseCallback() {

            @Override
            protected void handleSuccess(SaleOrderModel childOrderModel,
                                         PaymentTransactionModel childTransactionModel,
                                         Transaction transaction,
                                         String errorMessage) {
                if (childOrderModel != null) {
                    VoidProcessingFragmentDialog.this.childOrderModel = childOrderModel;
                    message.setText(String.format("voided %d of %d", ++current, max));
                    voidedTransactions.add(childTransactionModel);
                } else {
                    failedPaxTransactions = true;
                }
                next();
            }

            @Override
            protected void handleError() {
                message.setText(String.format("voided %d of %d", ++current, max));
                failedPaxTransactions = true;
                next();
            }
        };

    }

    private boolean voidNextPAXTransaction() {

        PaymentTransactionModel transaction = paxTransactions.poll();
        if (transaction == null) {
            return false;
        }
        if (!PaymentType.SALE.equals(transaction.paymentType)) {
            Logger.d("wrong transaction PType, ignoring");
            voidNextPAXTransaction();
            return false;
        } else if (!transaction.status.isSuccessful()) {
            Logger.d("wrong transaction PaymentStatus, ignoring");
            voidNextPAXTransaction();
            return false;
        } else if (   !PaymentGateway.PAX.equals(transaction.gateway)
                   && !PaymentGateway.PAX_EBT_CASH.equals(transaction.gateway)
                   && !PaymentGateway.PAX_EBT_FOODSTAMP.equals(transaction.gateway)
                   && !PaymentGateway.PAX_DEBIT.equals(transaction.gateway)
                ) {
            Logger.d("wrong transaction TransactionType, ignoring");
            voidNextPAXTransaction();
            return false;
        } else {
            message.setText(String.format("Current PAX transaction number %s. Overall voided %d of %d", transaction.getGuid(), ++current, max));
            transaction.gateway.gateway().voidMe(getActivity(), returnPaxCallBack() , user, transaction, childOrderModel, needToCancel);
            return true;
        }
    }

    private boolean voidNextCreditTransaction() {

        PaymentTransactionModel transaction = creditCardTransactions.poll();
        if (transaction == null) {
            return false;
        }
        if (!PaymentType.SALE.equals(transaction.paymentType)) {
            Logger.d("wrong transaction PType, ignoring");
            voidNextCreditTransaction();
            return false;
        } else if (!transaction.status.isSuccessful()) {
            Logger.d("wrong transaction PaymentStatus, ignoring");
            voidNextCreditTransaction();
            return false;
        } else if (!PaymentGateway.BLACKSTONE.equals(transaction.gateway)) {
            Logger.d("wrong transaction TransactionType, ignoring");
            voidNextCreditTransaction();
            return false;
        } else {
            PaymentGateway.BLACKSTONE.gateway().voidMe(getActivity(), this, user, transaction, childOrderModel, needToCancel);
            return true;
        }
    }

    private boolean voidNextCashTransaction() {
        Logger.d("voiding th cash");
        message.setText(String.format("voided %d of %d", current++, max));
        PaymentTransactionModel transaction = cashTransactions.poll();
        if (transaction == null || skipCashTransactions) {
            next();
            return false;
        }
        if (!PaymentType.SALE.equals(transaction.paymentType)) {
            Logger.d("wrong transaction PType, ignoring");
            voidNextCashTransaction();
            return false;
        } else if (!PaymentStatus.SUCCESS.equals(transaction.status)) {
            Logger.d("wrong transaction PaymentStatus, ignoring");
            voidNextCashTransaction();
            return false;
        } else if (!PaymentGateway.CASH.equals(transaction.gateway)) {
            Logger.d("wrong transaction TransactionType, ignoring");
            voidNextCashTransaction();
            return false;
        } else {
            PaymentGateway.CASH.gateway().voidMe(getActivity(), this, user, transaction, childOrderModel, needToCancel);
            return true;
        }
    }

    private BigDecimal getCashAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentTransactionModel transaction : cashTransactions) {
            total = total.add(transaction.availableAmount);
        }
        return total;
    }

    private boolean processCashTransactions() {
        if (cashTransactions.isEmpty() || skipCashTransactions) {
            getNegativeButton().setTextColor(getResources().getColor(R.color.light_gray));
            Logger.d("processCashTransactions cashTransactions.isEmpty()");
            return false;
        } else {
            amountHolder.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);
            total.setText(UiHelper.valueOf(getCashAmount()));
            enableButton(false);
            try2GetCash(false);
            return true;
        }
    }

    private boolean processCreditReceiptTransactions() {
        if (creditRecdeiptTransactions.isEmpty()) {
            getNegativeButton().setTextColor(getResources().getColor(R.color.light_gray));
            Logger.d("processCashTransactions cashTransactions.isEmpty()");
            return false;
        } else {
            BigDecimal total = BigDecimal.ZERO;
            for(PaymentTransactionModel p : creditRecdeiptTransactions){
                total = total.add(p.amount);
            }
            RefundCreditReceiptFragmentDialog.show(getActivity(), childOrderModel, creditRecdeiptTransactions, total, true, false, new IRefundCreditProgressListener() {
                @Override
                public void onComplete(BigDecimal amountAfterRefund, ArrayList<PaymentTransactionModel> refundChildTransactions, SaleOrderModel childOrderModel) {
                    VoidProcessingFragmentDialog.this.childOrderModel = childOrderModel;
                    current += creditRecdeiptTransactions.size();
                    message.setText(String.format("voided %d of %d", current, max));
                    if(BigDecimal.ZERO.compareTo(amountAfterRefund) == 0){
                        voidedTransactions.addAll(refundChildTransactions);
                    }
                    complete();
                }

                @Override
                public void onFailed() {
                    current += creditRecdeiptTransactions.size();
                    message.setText(String.format("voided %d of %d", current, max));
                    complete();
                }
            });
            return true;
        }
    }

    private boolean voidNextOfflineCreditTransaction() {
        Logger.d("voiding offline credit");
        message.setText(String.format("voided %d of %d", current++, max));
        PaymentTransactionModel transaction = offlineCreditTransactions.poll();
        if (transaction == null) {
            return false;
        }
        if (!PaymentType.SALE.equals(transaction.paymentType)) {
            Logger.d("wrong transaction PType, ignoring");
            voidNextOfflineCreditTransaction();
            return false;
        } else if (!PaymentStatus.SUCCESS.equals(transaction.status)) {
            Logger.d("wrong transaction PaymentStatus, ignoring");
            voidNextOfflineCreditTransaction();
            return false;
        } else if (!PaymentGateway.OFFLINE_CREDIT.equals(transaction.gateway)) {
            Logger.d("wrong transaction TransactionType, ignoring");
            voidNextOfflineCreditTransaction();
            return false;
        } else {
            PaymentGateway.OFFLINE_CREDIT.gateway().voidMe(getActivity(), this, user, transaction, childOrderModel, needToCancel);
            return true;
        }
    }

    private boolean voidNextCheckTransaction() {
        Logger.d("voiding check");
        message.setText(String.format("voided %d of %d", current++, max));
        PaymentTransactionModel transaction = checkTransactions.poll();
        if (transaction == null) {
            return false;
        }
        if (!PaymentType.SALE.equals(transaction.paymentType)) {
            Logger.d("wrong transaction PType, ignoring");
            voidNextCheckTransaction();
            return false;
        } else if (!PaymentStatus.SUCCESS.equals(transaction.status)) {
            Logger.d("wrong transaction PaymentStatus, ignoring");
            voidNextCheckTransaction();
            return false;
        } else if (!PaymentGateway.CHECK.equals(transaction.gateway)) {
            Logger.d("wrong transaction TransactionType, ignoring");
            voidNextCheckTransaction();
            return false;
        } else {
            PaymentGateway.CHECK.gateway().voidMe(getActivity(), this, user, transaction, childOrderModel, needToCancel);
            return true;
        }
    }

    protected void next() {
        if (       !voidNextPAXTransaction()
                && !voidNextCreditTransaction()
                && !processCashTransactions()
                && !voidNextOfflineCreditTransaction()
                && !voidNextCheckTransaction()
                && !processCreditReceiptTransactions())
        {
            complete();
        }
    }

    @OnSuccess(BlackVoidCommand.class)
    public void onVoidSuccess(@Param(WebCommand.RESULT_DATA) DoFullRefundResponse result,
                              @Param(WebCommand.RESULT_REASON) WebCommand.ErrorReason reason,
                              @Param(BlackVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                              @Param(BlackVoidCommand.ARG_CHILD_TRANSACTION_MODEL) PaymentTransactionModel childTransaction,
                              @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        Logger.d("BlackVoidCommand.onVoidSuccess(): result: %s", result.toDebugString());
        this.childOrderModel = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        voidedTransactions.add(childTransaction);
        next();
    }

    @OnFailure(BlackVoidCommand.class)
    public void onVoidFail(@Param(RESTWebCommand.RESULT_DATA) DoFullRefundResponse result,
                           @Param(RESTWebCommand.RESULT_REASON) WebCommand.ErrorReason reason,
                           @Param(BlackVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                           @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        Logger.e("BlackVoidCommand.onVoidFail(): result: " + (result == null ? null : result.toDebugString()) + ", error reason: " + reason);
        this.childOrderModel = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        failedCreditCardTransactions = true;
        next();
    }

    @OnSuccess(CashVoidCommand.class)
    public void onVoidCashSuccess(@Param(WebCommand.RESULT_REASON) WebCommand.ErrorReason reason,
                                  @Param(CashVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                                  @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                                  @Param(CashVoidCommand.ARG_CHILD_TRANSACTION_MODEL) PaymentTransactionModel childTransaction) {

        message.setText(String.format("voided %d of %d", ++current, max));
        this.childOrderModel = childOrderModel;
        voidedTransactions.add(childTransaction);
        voidNextCashTransaction();
    }

    @OnFailure(CashVoidCommand.class)
    public void onVoidCashFail(@Param(RESTWebCommand.RESULT_REASON) WebCommand.ErrorReason reason,
                               @Param(CashVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                               @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        voidNextCashTransaction();
    }

    @OnSuccess(OfflineCreditVoidCommand.class)
    public void onVoidOfflineCredirSuccess(@Param(OfflineCreditVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                                           @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                                           @Param(OfflineCreditVoidCommand.ARG_CHILD_TRANSACTION_MODEL) PaymentTransactionModel childTransaction) {
        message.setText(String.format("voided %d of %d", ++current, max));
        this.childOrderModel = childOrderModel;
        voidedTransactions.add(childTransaction);
        next();
    }

    @OnFailure(OfflineCreditVoidCommand.class)
    public void onVoidOfflineCreditFail( @Param(OfflineCreditVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                                         @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        failedOfflineCreditTransactions = true;
        next();
    }

    @OnSuccess(CheckVoidCommand.class)
    public void onVoidCheckSuccess(@Param(CheckVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                                   @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                                   @Param(CheckVoidCommand.ARG_CHILD_TRANSACTION_MODEL) PaymentTransactionModel childTransaction) {
        message.setText(String.format("voided %d of %d", ++current, max));
        this.childOrderModel = childOrderModel;
        voidedTransactions.add(childTransaction);
        next();
    }

    @OnFailure(CheckVoidCommand.class)
    public void onVoidCheckCreditFail( @Param(CheckVoidCommand.ARG_TRANSACTION) PaymentTransactionModel transaction,
                                       @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        message.setText(String.format("voided %d of %d", ++current, max));
        failedCheckTransactions = true;
        next();
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    if (cashTransactions.isEmpty()) {
                        listener.onComplete(voidedTransactions, childOrderModel);
                    } else {
                        listener.onCancel();
                        getNegativeButton().clearAnimation();
                    }
                }
                return false;
            }
        };
    }

    private void enableButton(boolean enabled) {
        getNegativeButton().setEnabled(enabled);
        getNegativeButton().setTextColor(enabled ? colorPaymentOk : colorPaymentDisabled);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.void_process_title;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public VoidProcessingFragmentDialog setTransactions(List<PaymentTransactionModel> transactions) {
        for (PaymentTransactionModel transaction : transactions) {
            if (!PaymentType.SALE.equals(transaction.paymentType)) {
                Logger.d("We skip this unwanted transaction - it is no SALE one");
                continue;
            }
            if (PaymentGateway.BLACKSTONE == transaction.gateway) {
                creditCardTransactions.add(transaction);
            } else if (PaymentGateway.CASH == transaction.gateway) {
                cashTransactions.add(transaction);
            } else if(PaymentGateway.CREDIT == transaction.gateway){
                creditRecdeiptTransactions.add(transaction);
            } else if(PaymentGateway.OFFLINE_CREDIT == transaction.gateway){
                offlineCreditTransactions.add(transaction);
            } else if(PaymentGateway.CHECK == transaction.gateway){
                checkTransactions.add(transaction);
            } else if(PaymentGateway.PAX == transaction.gateway || PaymentGateway.PAX_DEBIT == transaction.gateway
                    || PaymentGateway.PAX_EBT_CASH == transaction.gateway || PaymentGateway.PAX_EBT_FOODSTAMP == transaction.gateway){
                paxTransactions.add(transaction);
            }
        }
        return this;
    }

    public VoidProcessingFragmentDialog setListener(IVoidProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public VoidProcessingFragmentDialog setUser(User user) {
        this.user = user;
        return this;
    }

    @Override
    public void onDrawerOpened() {
        Logger.d("VoidProcessingFragmentDialog: onDrawerOpened()");
        WaitDialogFragment.hide(getActivity());
        flipper.setDisplayedChild(1);
    }

    @Override
    public void onFailure/*cash drawer*/() {
        Logger.d("VoidProcessingFragmentDialog: onFailure()");
        skipCashTransactions = true;
        WaitDialogFragment.hide(getActivity());
    }

    @Override
    public void onPopupCancelled() {
        listener.onComplete(voidedTransactions, childOrderModel);
    }

    @Override
    public void onCashReceived() {
        Logger.d("VoidProcessingFragmentDialog: onCashReceived()");
        skipCashTransactions = false;
        WaitDialogFragment.hide(getActivity());
        flipper.setDisplayedChild(0);
        amountHolder.setVisibility(View.INVISIBLE);
        voidNextCashTransaction();
    }

    @Override
    public void cancelWaitCashTask() {
        if (waitCashTask != null){
            waitCashTask.cancel(getActivity(), 0, null);
            waitCashTask = null;
        }
        /*if (listener != null){
            listener.onCancel();
        }*/
    }

    @Override
    public boolean try2GetCash(boolean searchByMac) {
        Logger.d("VoidProcessingFragmentDialog: try2GetCash()");
        if (getCashAmount().compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(getActivity(), R.string.pay_toast_zero, Toast.LENGTH_LONG).show();
            return false;
        }
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_open_drawer));
        waitCashTask = WaitForCashInDrawerCommand.start(getActivity(), searchByMac, new OpenDrawerListener(this));

        return false;
    }

    @Override
    public TaskHandler getHandler() {
        return waitCashTask;
    }

    @Override
    public void setHandler(TaskHandler handler) {
        waitCashTask = handler;
    }

    public static interface IVoidProgressListener {

        void onComplete(List<PaymentTransactionModel> reopened, SaleOrderModel childOrderModel);

        void onCancel();

    }

    public static void show(FragmentActivity context,
                            final List<PaymentTransactionModel> transactions,
                            User user,
                            IVoidProgressListener listener,
                            SaleOrderModel childOrderModel,
                            boolean needToCancel) {
        DialogUtil.show(context, DIALOG_NAME, VoidProcessingFragmentDialog_.builder().needToCancel(needToCancel).build())
                .setChildOrderModel(childOrderModel)
                .setListener(listener)
                .setTransactions(transactions)
                .setUser(user);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
