package com.kaching123.tcr.fragment.tendering.voiding;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.payment.GetIVULotoDataCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintRefundCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.GetIVULotoDataRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerView;
import com.kaching123.tcr.websvc.api.prepaid.IVULotoDataResponse;
import com.kaching123.tcr.websvc.api.prepaid.Receipt;
import com.kaching123.tcr.websvc.api.prepaid.WS_Enums;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class VoidPrintAndFinishFragmentDialog extends PrintAndFinishFragmentDialogBase {

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

    private IVULotoDataResponse response;

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

    private void createRefundIVULoto(){
        WaitDialogFragment.show(getActivity(), getString(R.string.lottery_wait_dialog_title));

        OrderTotalPriceCursorQuery.loadSync(getActivity(), orderGuid, new OrderTotalPriceCursorQuery.LotteryHandler() {
            @Override
            public void handleTotal(BigDecimal totalSubtotal, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal tipsAmount, BigDecimal transactionFee, BigDecimal totalCashBack) {
                BigDecimal mSubTotal = totalSubtotal.subtract(totalDiscount).add(transactionFee).add(totalCashBack);
                BigDecimal totalOrderPrice = totalSubtotal.add(totalTax).subtract(totalDiscount);
                BigDecimal mTotal = totalOrderPrice.add(tipsAmount).add(transactionFee).add(totalCashBack);
                GetIVULotoDataRequest request = getIVULotoDataRequest(mSubTotal, mTotal);
                GetIVULotoDataCommand.start(getActivity(), request, new GetIVULotoDataCommand.IVULotoDataCallBack() {
                    @Override
                    protected void onSuccess(IVULotoDataResponse result) {
                        Logger.d("PayPrintAndFinishFragmentDialog getTicketNumberSuccess");
                        response = result;
                        WaitDialogFragment.hide(getActivity());
                    }

                    @Override
                    protected void onFailure() {
                        Logger.d("PayPrintAndFinishFragmentDialog getLotteryNumberFail");
                        response = new IVULotoDataResponse();
                        WaitDialogFragment.hide(getActivity());
                        Toast.makeText(getActivity(), getString(R.string.lottery_fail_message), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    private final String IVU_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private final String TIME_ZONE_GMT = "GMT";

    private GetIVULotoDataRequest getIVULotoDataRequest(BigDecimal mSubTotal, BigDecimal mTotal) {

        GetIVULotoDataRequest request = new GetIVULotoDataRequest();
        request.MID = String.valueOf(getUser().getMid());
        request.TID = String.valueOf(getUser().getTid());
        request.Password = getUser().getPassword();
        request.transactionId = PrepaidProcessor.generateId();
        Receipt receipt = new Receipt();
        receipt.merchantId = "51774932983";
        receipt.terminalId = "POS00";
        receipt.terminalPassword = "WCeMQVN3";
        receipt.municipalTax = getTax().doubleValue();
        receipt.stateTax = getTax().doubleValue();
        receipt.subTotal = mSubTotal.doubleValue();
        receipt.total = mTotal.doubleValue();
        DateFormat dateFormat = new SimpleDateFormat(IVU_TIME_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
        Date date = new Date();
        receipt.txDate = String.valueOf(dateFormat.format(date));
        receipt.txType = WS_Enums.SoapProtocolVersion.TransactionType.Refund;
        receipt.tenderType = WS_Enums.SoapProtocolVersion.TypeOfTender.Cash;
        request.receipt = receipt;
        return request;
    }


    private PrepaidUser getUser() {
        return ((TcrApplication) getActivity().getApplicationContext()).getPrepaidUser();
    }

    private BigDecimal getTax() {
        return ((TcrApplication) getActivity().getApplicationContext()).getTaxVat();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);

        createRefundIVULoto();
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
