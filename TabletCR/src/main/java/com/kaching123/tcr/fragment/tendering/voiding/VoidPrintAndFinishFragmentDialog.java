package com.kaching123.tcr.fragment.tendering.voiding;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintRefundCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerView;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class VoidPrintAndFinishFragmentDialog extends PrintAndFinishFragmentDialogBase {

    private static final Uri CUSTOMERS_URI = ShopProvider.getContentUri(CustomerView.URI_CONTENT);

    private static final String DIALOG_NAME = VoidPrintAndFinishFragmentDialog.class.getSimpleName();

    @FragmentArg
    protected ArrayList<RefundSaleItemInfo> refundItemsInfo;

    @FragmentArg
    protected ArrayList<PaymentTransactionModel> refundTransactions;

    @FragmentArg
    protected SaleOrderModel childOrderModel;

    @FragmentArg
    protected boolean isRefund;

    private PrintRefundCallback printRefundCallback = new PrintRefundCallback();

    @Override
    protected int getDialogContentLayout() {
        return R.layout.void_complete;
    }

    @Override
    protected int getDialogTitle() {
        return isRefund ? R.string.refund_confirm_title : R.string.void_confirm_title;
    }

    public VoidPrintAndFinishFragmentDialog setRefundItemsInfo(ArrayList<RefundSaleItemInfo> refundItemsInfo) {
        this.refundItemsInfo = refundItemsInfo;
        return this;
    }

    public VoidPrintAndFinishFragmentDialog setRefundTransactions(ArrayList<PaymentTransactionModel> refundTransactions) {
        this.refundTransactions = refundTransactions;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));

        PrintRefundCommand.start(getActivity(), skipPaperWarning, searchByMac, orderGuid, childOrderModel.guid, refundItemsInfo, printRefundCallback);
    }

    private void onPrintSuccess() {
        WaitDialogFragment.hide(getActivity());
        completeProcess();
    }

    private ArrayList<String> getTransactionsGuids() {
        ArrayList<String> transactionsGuids = new ArrayList<String>(refundTransactions == null ? 0 : refundTransactions.size());
        if (refundTransactions != null) {
            for (PaymentTransactionModel p : refundTransactions) {
                transactionsGuids.add(p.guid);
            }
        }
        return transactionsGuids;
    }

    private boolean ensureSingleShot;

    public static void show(FragmentActivity context, String orderGuid,
                            final ArrayList<RefundSaleItemInfo> refundItemsInfo,
                            final ArrayList<PaymentTransactionModel> refundTransactions,
                            IFinishConfirmListener listener,
                            final SaleOrderModel childOrderModel,
                            boolean isRefund) {
        VoidPrintAndFinishFragmentDialog dialog = VoidPrintAndFinishFragmentDialog_.builder().childOrderModel(childOrderModel).orderGuid(orderGuid).isRefund(isRefund).build();
        dialog.setRefundItemsInfo(refundItemsInfo).setRefundTransactions(refundTransactions).setListener(listener);
        DialogUtil.show(context, DIALOG_NAME, dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    protected boolean enableSignatureCheckbox() {

        return false;
    }

    @Override
    protected BigDecimal calcTotal() {
        return BigDecimal.ZERO;
    }

    public class PrintRefundCallback extends BasePrintCallback{

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printOrder(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };

        @Override
        protected void onPrintSuccess() {
            VoidPrintAndFinishFragmentDialog.this.onPrintSuccess();;
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
}