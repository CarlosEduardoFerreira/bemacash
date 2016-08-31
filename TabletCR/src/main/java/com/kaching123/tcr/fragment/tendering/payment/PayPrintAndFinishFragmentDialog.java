package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorHelloCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalOrderCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintOrderCommand;
import com.kaching123.tcr.commands.print.pos.PrintSignatureOrderCommand;
import com.kaching123.tcr.commands.print.pos.PrintSignatureOrderCommand.ReceiptType;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.BaseKitchenPrintCallback;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper.IKitchenPrintCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog.emailSenderListener;
import com.kaching123.tcr.fragment.tendering.PayChooseCustomerDialog;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.print.processor.GiftCardBillingResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PayPrintAndFinishFragmentDialog extends PrintAndFinishFragmentDialogBase {

    @FragmentArg
    protected BigDecimal changeAmount;
    @FragmentArg
    protected ReceiptType gateWay;
    @FragmentArg
    protected boolean isPrinterTwoCopiesReceipt;
    @FragmentArg
    protected ArrayList<PrepaidReleaseResult> releaseResultList;
    @FragmentArg
    protected ArrayList<GiftCardBillingResult> giftCardResults;

    @ViewById
    protected CheckBox signatureBox;

    @ViewById
    protected CheckBox emailBox;

    @ViewById
    protected TextView change;

    @Override
    protected BigDecimal calcTotal() {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (PaymentTransactionModel i : transactions) {
            totalValue = totalValue.add(i.amount);
        }
        return totalValue;
    }

    @Override
    protected boolean enableSignatureCheckbox() {
        for (PaymentTransactionModel i : transactions) {
            PaymentGateway gateway = i.gateway;
            if (gateway != null && gateway.isTrueCreditCard()) {
                return true;
            }
        }
        return false;
    }

    @FragmentArg
    protected ArrayList<PaymentTransactionModel> transactions;

    @FragmentArg
    protected KitchenPrintStatus kitchenPrintStatus;

    private static final String DIALOG_NAME = PayPrintAndFinishFragmentDialog.class.getSimpleName();

    protected PrintOrderCallback printOrderCallback = new PrintOrderCallback();

    protected PrintDebitorEBTCallback printDebitorEBTCallback = new PrintDebitorEBTCallback();
    protected PrintSignatureOrderCallback printSignatureCallback = new PrintSignatureOrderCallback();
    protected PrintSignatureOrderCallback2 printSignatureCallback2 = new PrintSignatureOrderCallback2();

    protected boolean ignorePaperEnd = false;

    protected boolean kitchenPrinted;
    protected boolean orderPrinted;
    protected boolean debitOrEBTDetailsPrinted;
    protected boolean signatureOrderPrinted;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!enableSignatureCheckbox()) {
            signatureBox.setEnabled(false);
            signatureBox.setChecked(false);
            signatureBox.setFocusable(false);
        } else if (getApp().getShopInfo().signaturePrintLimit != null && getApp().getShopInfo().signaturePrintLimit.compareTo(calcTotal()) <= 0) {
            signatureBox.setEnabled(false);
            signatureBox.setChecked(true);
            signatureBox.setFocusable(false);
        }

        if (changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) == 1) {
            change.setVisibility(View.VISIBLE);

            change.setText(getString(R.string.blackstone_change_charge_finish, UiHelper.priceFormat(changeAmount)));
        }

        if (kitchenPrintStatus != KitchenPrintStatus.PRINTED) {
            printItemsToKitchen(null, false, false, false);
        }

        printBox.setChecked(getApp().getPrintReceiptDefault() );
        emailBox.setChecked(getApp().getEmailReceiptDefault());
    }

    @Override
    protected void onOrderDataLoaded() {
        super.onOrderDataLoaded();
        if (customer != null && TextUtils.isEmpty(customer.email)) {
            emailBox.setChecked(false);
            emailBox.setEnabled(false);
        }
    }

    @AfterViews
    protected void initViews() {
        printBox.setChecked(true);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_complete;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_confirm_title;
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintOrderCommand.start(getActivity(), skipPaperWarning, searchByMac, orderGuid, transactions, releaseResultList, giftCardResults, printOrderCallback);
    }

    protected void chooseCustomer() {
        PayChooseCustomerDialog.show(getActivity(), orderGuid, transactions, new emailSenderListener() {
            @Override
            public void onComplete() {
                listener.onConfirmed();
                dismiss();
            }
        }, releaseResultList);
    }

    protected void sendDigitalOrder() {
        SendDigitalOrderCommand.start(getActivity(), orderGuid, customer.email, null, transactions, releaseResultList);
        listener.onConfirmed();
        dismiss();
    }

    //@Override
    private void printSignatureOrder(boolean skipPaperWarning, boolean searchByMac) {
        PayPrintAndFinishFragmentDialog.this.ignorePaperEnd = false;
        printSignatureOrder(skipPaperWarning, searchByMac, ReceiptType.CUSTOMER, printSignatureCallback);
    }

    private void printDebitorEBTDetails(boolean skipPaperWarning, boolean searchByMac) {
        printSignatureOrder(skipPaperWarning, searchByMac, gateWay, printDebitorEBTCallback);
    }

    private void printSignatureOrder(boolean skipPaperWarning, boolean searchByMac, ReceiptType receiptType, PrintSignatureOrderCallback printSignatureCallback) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        if (receiptType != ReceiptType.DEBIT && receiptType != ReceiptType.EBT_CASH && receiptType != ReceiptType.EBT)
            if (!printBox.isChecked()) {
                receiptType = ReceiptType.MERCHANT;
            }
        PrintSignatureOrderCommand.start(getActivity(), skipPaperWarning || this.ignorePaperEnd, searchByMac, orderGuid, transactions, receiptType, printSignatureCallback);
    }

    @Override
    protected boolean onConfirm() {
        printReceipts();
        if (!getApp().isBlackstonePax() && getApp().isPaxConfigured())
            PaxProcessorHelloCommand.start(getActivity(), PaxModel.get(), helloCallBack);
        return false;
    }

    protected void printReceipts() {
        WaitDialogFragment.hide(getActivity());
        if (printBox.isChecked() && !orderPrinted) {
            printOrder(false, false);
        } else if (signatureBox.isChecked() && !signatureOrderPrinted) {
            printSignatureOrder(false, false);
        } else if (printBox.isChecked() && (gateWay == ReceiptType.DEBIT || gateWay == ReceiptType.EBT || gateWay == ReceiptType.EBT_CASH) && isPrinterTwoCopiesReceipt) {
            printOrder(false, false);
            isPrinterTwoCopiesReceipt = false;
        } else if (printBox.isChecked() && (gateWay == ReceiptType.DEBIT || gateWay == ReceiptType.EBT || gateWay == ReceiptType.EBT_CASH) && !debitOrEBTDetailsPrinted) {
            printDebitorEBTDetails(false, false);
        } else {
            completeProcess();
        }
    }

    @Override
    protected void completeProcess() {
        WaitDialogFragment.hide(getActivity());
        if (emailBox.isChecked()) {
            if (customer == null) {
                chooseCustomer();
            } else {
                sendDigitalOrder();
            }
        } else {
            super.completeProcess();
        }
    }

    private void onSignaturePrintSuccess() {
        signatureOrderPrinted = true;
        printReceipts();
    }

    private void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintItemsForKitchenCommand.start(getActivity(), skipPaperWarning, searchByMac, orderGuid, fromPrinter, skip, new KitchenKitchenPrintCallback(), false, null);
    }

    public static void show(FragmentActivity context, String orderGuid, IFinishConfirmListener listener, ArrayList<PaymentTransactionModel> transactions, KitchenPrintStatus kitchenPrintStatus, BigDecimal changeAmount, ReceiptType debitGateway, boolean isPrinterTwoCopiesReceipt, ArrayList<PrepaidReleaseResult> releaseResultList, ArrayList<GiftCardBillingResult> giftCardResults) {
        DialogUtil.show(context, DIALOG_NAME, PayPrintAndFinishFragmentDialog_.builder().transactions(transactions).orderGuid(orderGuid).kitchenPrintStatus(kitchenPrintStatus).changeAmount(changeAmount).releaseResultList(releaseResultList).gateWay(debitGateway).giftCardResults(giftCardResults).isPrinterTwoCopiesReceipt(isPrinterTwoCopiesReceipt).build()).setListener(listener);
    }

    public static void show(FragmentActivity context, String orderGuid, IFinishConfirmListener listener, ArrayList<PaymentTransactionModel> transactions, KitchenPrintStatus kitchenPrintStatus, BigDecimal changeAmount, ReceiptType debitGateway, boolean isPrinterTwoCopiesReceipt) {
        DialogUtil.show(context, DIALOG_NAME, PayPrintAndFinishFragmentDialog_.builder().transactions(transactions).orderGuid(orderGuid).kitchenPrintStatus(kitchenPrintStatus).changeAmount(changeAmount).gateWay(debitGateway).isPrinterTwoCopiesReceipt(isPrinterTwoCopiesReceipt).build()).setListener(listener);
    }


    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public class PrintOrderCallback extends BasePrintCallback {

        IPrintCallback callback = new IPrintCallback() {
            @Override
            public void onRetry(boolean searchByMac, boolean ignorePaperEnd) {
                printOrder(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };

        @Override
        public void onPrintSuccess() {
            orderPrinted = true;
            printReceipts();
        }

        @Override
        public void onPrintError(PrinterCommand.PrinterError errorType) {
            PrintCallbackHelper2.onPrintError(getActivity(), errorType, callback);
        }

        @Override
        public void onPrinterNotConfigured() {
            PrintCallbackHelper2.onPrinterNotConfigured(getActivity(), callback);
        }

        @Override
        public void onPrinterDisconnected() {
            PrintCallbackHelper2.onPrinterDisconnected(getActivity(), callback);
        }

        @Override
        protected void onPrinterIPnotFound() {
            PrintCallbackHelper2.onPrinterIPnotFound(getActivity(), callback);
        }

        @Override
        public void onPrinterPaperNearTheEnd() {
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(getActivity(), callback);
        }

    }

    public class PrintDebitorEBTCallback extends PrintSignatureOrderCallback {

        @Override
        protected ReceiptType getType() {
            return gateWay;
        }

        @Override
        public void onPrintSuccess() {
            debitOrEBTDetailsPrinted = true;
            PayPrintAndFinishFragmentDialog.this.onSignaturePrintSuccess();
        }

    }

    public class PrintSignatureOrderCallback2 extends PrintSignatureOrderCallback {

        @Override
        protected ReceiptType getType() {
            return ReceiptType.MERCHANT;
        }

        @Override
        public void onPrintSuccess() {
            PayPrintAndFinishFragmentDialog.this.onSignaturePrintSuccess();
        }

    }

    public class PrintSignatureOrderCallback extends BasePrintCallback {

        protected ReceiptType getType() {
            return ReceiptType.CUSTOMER;
        }

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printSignatureOrder(ignorePaperEnd, searchByMac, getType(), PrintSignatureOrderCallback.this);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };


        @Override
        protected void onPrintSuccess() {
            if (printBox.isChecked()) {
                printSignatureOrder(false, false, ReceiptType.MERCHANT, printSignatureCallback2);
            } else {
                PayPrintAndFinishFragmentDialog.this.onSignaturePrintSuccess();
            }
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

    public class KitchenKitchenPrintCallback extends BaseKitchenPrintCallback {

        private IKitchenPrintCallback skipListener = new IKitchenPrintCallback() {

            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, true, ignorePaperEnd, searchByMac);
            }
        };

        @Override
        protected void onPrintSuccess() {
            kitchenPrinted = true;
            WaitDialogFragment.hide(getActivity());
        }

        @Override
        protected void onPrintError(PrinterError error, String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrintError(getActivity(), error, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterNotConfigured(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterNotConfigured(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterDisconnected(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterDisconnected(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterIPnotFound(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterIPnotfound(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(getActivity(), fromPrinter, aliasTitle, skipListener);
        }
    }

}
