package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.payment.credit.CreateCreditReceiptCommand;
import com.kaching123.tcr.commands.payment.credit.CreateCreditReceiptCommand.CreateCreditReceiptBaseCallback;
import com.kaching123.tcr.commands.payment.credit.CreditRefundCommand;
import com.kaching123.tcr.commands.payment.credit.CreditRefundCommand.CreditRefundCommandBaseCallback;
import com.kaching123.tcr.commands.payment.credit.PrintCreditReceiptCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author gdubina
 */

@EFragment
public class RefundCreditReceiptFragmentDialog extends RefundBaseFragmentDialog {

    private static final String DIALOG_NAME = "RefundCreditReceiptFragmentDialog";

    @ViewById
    protected TextView msg;

    @ViewById
    protected ImageView icon;

    @ViewById
    protected View progress;

    @ViewById
    protected View mainBlock;

    @ViewById
    protected View printingBlock;

    @FragmentArg
    protected boolean needToCancel;

    @FragmentArg
    protected boolean isManualReturn;

    private CreditReceiptModel receipt;

    private Mode mode = Mode.PROGRESS;

    private enum Mode {
        PROGRESS, ERROR, PRINT, COMPLETE
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.refund_credit_receipt_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.credit_receipt_dlg_title;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_finish;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    private IRefundCreditProgressListener getRefundListener(){
        return (IRefundCreditProgressListener)refundListener;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                switch (mode) {
                    case ERROR:
                        getRefundListener().onFailed();
                        return true;
                    case PRINT:
                        printReceipt(false, false);
                        return false;
                    case COMPLETE:
                        return true;
                }
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                setCompleteMode();
                return false;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.base_dlg_width),
                getDialog().getWindow().getAttributes().height
        );

        initViews();

        createReceipt();
    }

    private void initViews() {
        enablePositiveButtons(false);
        getNegativeButton().setVisibility(View.GONE);
    }

    @Override
    protected void completeRefund() {
        super.completeRefund();
        dismiss();
    }

    @Override
    protected void updateUI(int currentStep, int steps) {

    }

    @Override
    protected void callRefundCommand(PaymentTransactionModel transaction, BigDecimal amount, SaleOrderModel returnOrder) {
        CreditRefundCommand.start(getActivity(),
                new CreditRefundCommandCallback(),
                transaction,
                amount,
                receipt,
                returnOrder,
                needToCancel,
                isManualReturn);
    }

    private void printReceipt(boolean ignorePaperEnd, boolean searchByMac) {
        setProgressMode();
        PrintCreditReceiptCommand.start(getActivity(), ignorePaperEnd, false, receipt, new PrintCreditReceiptCallback());
    }

    private void createReceipt(){
        CreateCreditReceiptCommand.start(getActivity(), refundAmount, new CreateCreditReceiptCallback());
    }

    private void setProgressMode() {
        mode = Mode.PROGRESS;

        printingBlock.setVisibility(View.GONE);
        mainBlock.setVisibility(View.VISIBLE);

        msg.setText(R.string.credit_receipt_printing);
        progress.setVisibility(View.VISIBLE);
        icon.setVisibility(View.GONE);

        getPositiveButton().setText(R.string.btn_ok);
        enablePositiveButtons(false);
        getNegativeButton().setVisibility(View.GONE);
    }

    private void setCompleteMode(){
        mode = Mode.COMPLETE;

        printingBlock.setVisibility(View.GONE);
        mainBlock.setVisibility(View.VISIBLE);

        msg.setText(R.string.credit_receipt_print_completed);
        progress.setVisibility(View.GONE);
        showImage(DialogType.COMPLETE);

        getPositiveButton().setText(R.string.btn_ok);
        enablePositiveButtons(false);
        getNegativeButton().setVisibility(View.GONE);

        newRefundIteration();
    }

    private void setPrintMode() {
        mode = Mode.PRINT;

        printingBlock.setVisibility(View.VISIBLE);
        mainBlock.setVisibility(View.GONE);

        getPositiveButton().setText(R.string.btn_print);
        enablePositiveButtons(true);
        getNegativeButton().setVisibility(View.VISIBLE);
    }

    private void setErrorMode() {
        setErrorMode(R.string.credit_receipt_proceed_finish_error);
    }

    private void setErrorMode(int error) {
        mode = Mode.ERROR;

        printingBlock.setVisibility(View.GONE);
        mainBlock.setVisibility(View.VISIBLE);

        msg.setText(error);
        progress.setVisibility(View.GONE);
        showImage(DialogType.ALERT);

        getPositiveButton().setText(R.string.btn_abort);
        enablePositiveButtons(true);
        getNegativeButton().setVisibility(View.GONE);
    }

    private void showImage(DialogType type) {
        icon.setVisibility(View.VISIBLE);
        icon.setImageLevel(type.level);
    }

    public class CreateCreditReceiptCallback extends CreateCreditReceiptBaseCallback {

        @Override
        protected void handleSuccess(CreditReceiptModel receipt) {
            RefundCreditReceiptFragmentDialog.this.receipt = receipt;
            printReceipt(false, false);
        }

        @Override
        protected void handleOnFailure() {
            setErrorMode();
        }
    }

    public class PrintCreditReceiptCallback extends BasePrintCallback {
        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printReceipt(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };


        @Override
        protected void onPrintSuccess() {
            setCompleteMode();
        }

        @Override
        protected void onPrintError(PrinterError error) {
            PrintCallbackHelper2.onPrintError(getActivity(), error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(getActivity(), retryListener);
        }
    }

    public class CreditRefundCommandCallback extends CreditRefundCommandBaseCallback {

        @Override
        protected void handleOnSuccess(PaymentTransactionModel childTransaction, BigDecimal refundedAmount, SaleOrderModel returnOrder) {
            onRefundCommandSuccess(childTransaction, refundedAmount, returnOrder);
        }

        @Override
        protected void handleOnFailure() {
            setErrorMode();
        }
    }

    public static void show(FragmentActivity context,
                            SaleOrderModel childOrder,
                            List<PaymentTransactionModel> transactions,
                            BigDecimal refundAmount,
                            boolean needToCancel,
                            boolean isManualReturn,
                            IRefundCreditProgressListener listener) {

        DialogUtil.show(context, DIALOG_NAME,
                RefundCreditReceiptFragmentDialog_.builder().needToCancel(needToCancel).isManualReturn(isManualReturn).build())
                .setReturnOrder(childOrder)
                .setRefundListener(listener)
                .setTransactions(transactions)
                .setRefundAmount(refundAmount);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static interface IRefundCreditProgressListener extends IRefundProgressListener{
        void onFailed();
    }
}
