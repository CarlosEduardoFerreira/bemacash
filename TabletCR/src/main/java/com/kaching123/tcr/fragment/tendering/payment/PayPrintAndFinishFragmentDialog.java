package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
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
    protected boolean longSignatureReceiptPrinted;

    public boolean print_digital_signature = false;

    boolean tips                = false;    // tips
    boolean digital_signature   = false;    // pax signature bitmap
    String  signature_receipt   = "SHORT";  // manual signature
    boolean signaturePrintLimit = false;    // Require Signature on Transactions Higher Than:

    FragmentActivity activity;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();

        longSignatureReceiptPrinted = false;

        tips                = getApp().isTipsEnabled();
        digital_signature   = getApp().getDigitalSignature();
        signature_receipt   = getApp().getSignatureReceipt();

        Log.i("BemaCarl","doubleCheckBeforePrint tips: " + tips);
        Log.i("BemaCarl","doubleCheckBeforePrint digital_signature: " + digital_signature);
        Log.i("BemaCarl","doubleCheckBeforePrint signature_receipt: " + signature_receipt);

        signaturePrintLimit = getApp().getShopInfo().signaturePrintLimit != null && getApp().getShopInfo().signaturePrintLimit.compareTo(calcTotal()) <= 0;

        signatureBox.setFocusable(false);

        if ((!enableSignatureCheckbox() || getApp().PAX_SIGNATURE_EMULATOR) || (digital_signature && signaturePrintLimit) ) {
            signatureBox.setEnabled(false);
            signatureBox.setChecked(false);
        } else if (!signaturePrintLimit){
            signatureBox.setEnabled(true);
            signatureBox.setChecked(false);
        } else if (signaturePrintLimit) {
            signatureBox.setEnabled(false);
            signatureBox.setChecked(true);
        }



        if (changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) == 1) {
            change.setVisibility(View.VISIBLE);

            change.setText(getString(R.string.blackstone_change_charge_finish, UiHelper.priceFormat(changeAmount)));
        }

        if (kitchenPrintStatus != KitchenPrintStatus.PRINTED) {
            printItemsToKitchen(null, false, false, false);
        }

        printBox.setChecked(getApp().getPrintReceiptDefault());
        emailBox.setChecked(getApp().getEmailReceiptDefault());


        /** Condition 1
         *  If “Tips” are “Disabled” and “Digital Signature” is set to “Yes” and “Signature Receipt” is set to “Short”
         *  "Signature Receipt" should be disabled. If the check box "Print Receipt"
         *  it should print only 1 complete receipt with digital signature.
         */
        if(!tips && digital_signature && signature_receipt.equals("SHORT")) {        // condition 1
            //print_digital_signature = true;
            Log.i("BemaCarl","doubleCheckBeforePrint Condition 1");

        /** Condition 2
         *  If “Tips” are “Disabled” and “Digital Signature” is set to “No” and “Signature Receipt” is set to “Short”
         *  and user only selected "Print Receipt", print only 1 simple value receipt.
         *
         *  Condition 3
         *  If user checks signatureBox, it should print the manual signature receipt too. Only "Merchant Copy".
         */
        }else if(!tips && !digital_signature && signature_receipt.equals("SHORT")) {   // condition 2
            //signatureBox.setEnabled(true);
            Log.i("BemaCarl","doubleCheckBeforePrint Condition 2 e 3");

        /** Condition 4
         *  If “Tips” are “Disabled” “Digital Signature” is set to “No” and “Signature Receipt” is set to “Long”
         *  and user only selected "Print Receipt", print only 1 simple value receipt.
         *
         *  Condition 5
         *  If user checks signatureBox, it should print the manual signature receipt too. Both "Merchant Copy" and "Customer Copy".
         */
        }else if(!tips && !digital_signature && signature_receipt.equals("LONG")) {   // condition 4
            //signatureBox.setEnabled(true);
            Log.i("BemaCarl","doubleCheckBeforePrint Condition 4 e 5");

        /** Condition 6
         *  If “Tips” are “Enabled” “Digital Signature” is set to “No” and “Signature Receipt” is set to “Short”
         *  and user only selected "Print Receipt", print only 1 simple value receipt.
         *
         *  Condition 7
         *  If user checks signatureBox, it should print the manual signature receipt too with "TIP". Only "Merchant Copy".
         */
        }else if(tips && !digital_signature && signature_receipt.equals("SHORT")) {   // condition 6
            //signatureBox.setEnabled(true);
            Log.i("BemaCarl","doubleCheckBeforePrint Condition 6 e 7");

        /** Condition 8
         *  If “Tips” are “Enabled” “Digital Signature” is set to “No” and “Signature Receipt” is set to “Long”
         *  and user only selected "Print Receipt", print only 1 simple value receipt.
         *
         *  Condition 9
         *  If user checks signatureBox, it should print the manual signature receipt too with "TIP". Both "Merchant Copy" and "Customer Copy".
         */
        }else if(tips && !digital_signature && signature_receipt.equals("LONG")) {   // condition 8
            //signatureBox.setEnabled(true);
            Log.i("BemaCarl","doubleCheckBeforePrint Condition 8 e 9");
        }

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

    protected void chooseCustomer() {
        PayChooseCustomerDialog.show(getActivity(), orderGuid, transactions, new emailSenderListener() {
            @Override
            public void onComplete() {
                listener.onConfirmed();
                dismiss();
            }
        }, releaseResultList);
    }


    @Override
    protected boolean onConfirm() {
        printReceipts();
        if (!getApp().isBlackstonePax() && getApp().isPaxConfigured())
            PaxProcessorHelloCommand.start(getActivity(), PaxModel.get(), helloCallBack);
        return false;
    }

    protected void printReceipts() {

        getApp().forceSignaturePrint = signatureBox.isChecked() ? true : false;

        if (printBox.isChecked() && !orderPrinted) {
            printOrder(false, false);
        } else if (signatureBox.isChecked() && !signatureOrderPrinted) {
            printSignatureOrder(false, false, ReceiptType.MERCHANT, printSignatureCallback);
        } else if (printBox.isChecked() && (gateWay == ReceiptType.DEBIT || gateWay == ReceiptType.EBT || gateWay == ReceiptType.EBT_CASH) && isPrinterTwoCopiesReceipt) {
            printOrder(false, false);
            isPrinterTwoCopiesReceipt = false;
        } else if (printBox.isChecked() && (gateWay == ReceiptType.DEBIT || gateWay == ReceiptType.EBT || gateWay == ReceiptType.EBT_CASH) && !debitOrEBTDetailsPrinted) {
            printDebitorEBTDetails(false, false);
        }
        completeProcess();
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        //WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintOrderCommand.start(getActivity(), skipPaperWarning, searchByMac, orderGuid, transactions, releaseResultList, giftCardResults, printOrderCallback);
    }

    private void printDebitorEBTDetails(boolean skipPaperWarning, boolean searchByMac) {
        printSignatureOrder(skipPaperWarning, searchByMac, gateWay, printDebitorEBTCallback);
    }

    private void printSignatureOrder(boolean skipPaperWarning, boolean searchByMac, ReceiptType receiptType, PrintSignatureOrderCallback printSignatureCallback) {
        //WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        //if (receiptType != ReceiptType.DEBIT && receiptType != ReceiptType.EBT_CASH && receiptType != ReceiptType.EBT) {
            PrintSignatureOrderCommand.start(getActivity(), skipPaperWarning || this.ignorePaperEnd, searchByMac, orderGuid, transactions, receiptType, printSignatureCallback);
       //}
    }

    boolean printed = false;

    @Override
    protected void completeProcess() {
        if (emailBox.isChecked()) {
            if (customer == null) {
                chooseCustomer();
            } else {
                sendDigitalOrder();
            }
        } else if(printed) {
            dismiss();
            super.completeProcess();
        } else {
            if(printBox.isChecked() || signatureBox.isChecked()) {
                final WaitDialogFragment waitDialog = (WaitDialogFragment) activity.getSupportFragmentManager().findFragmentByTag("progressDialog");
                waitDialog.show(getActivity(), getString(R.string.wait_printing));
                /* should to fix to get printer status */
                printBox.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitDialog.hide(activity);
                        printed = true;
                        completeProcess();
                    }
                }, 3000);
            }else{
                printed = true;
                completeProcess();
            }
        }
    }

    protected void sendDigitalOrder() {
        SendDigitalOrderCommand.start(getActivity(), orderGuid, customer.email, null, transactions, releaseResultList);
        listener.onConfirmed();
        dismiss();
    }

    private void onSignaturePrintSuccess() {
        signatureOrderPrinted = true;
        //printReceipts();
    }

    private void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintItemsForKitchenCommand.itComesFromPay = true;
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
            //if (printBox.isChecked()) {
                //printSignatureOrder(false, false, ReceiptType.MERCHANT, printSignatureCallback2);
            //} else {
                PayPrintAndFinishFragmentDialog.this.onSignaturePrintSuccess();
            //}
            WaitDialogFragment.hide(getActivity());
            if(signature_receipt.equals("LONG") && !longSignatureReceiptPrinted) {
                longSignatureReceiptPrinted = true;
                printSignatureOrder(false, false, ReceiptType.CUSTOMER, printSignatureCallback);
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
