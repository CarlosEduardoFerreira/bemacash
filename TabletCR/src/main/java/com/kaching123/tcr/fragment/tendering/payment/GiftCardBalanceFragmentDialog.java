package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorHelloCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintGiftCardBalanceCommand;
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
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;
import com.kaching123.tcr.fragment.tendering.PayChooseCustomerDialog;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;

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
public class GiftCardBalanceFragmentDialog extends PrintAndFinishFragmentDialogBase {

    @FragmentArg
    protected BigDecimal changeAmount;

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

    private static final String DIALOG_NAME = GiftCardBalanceFragmentDialog.class.getSimpleName();

    protected PrintOrderCallback printOrderCallback = new PrintOrderCallback();

    protected boolean ignorePaperEnd = false;

    protected boolean kitchenPrinted;
    protected boolean orderPrinted;
    protected boolean debitOrEBTDetailsPrinted;
    protected boolean signatureOrderPrinted;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        change.setVisibility(View.VISIBLE);
        change.setText(getString(R.string.blackstone_pay_charge_finish,changeAmount == null ? BigDecimal.ZERO : UiHelper.priceFormat(changeAmount)));

    }

    @AfterViews
    protected void initViews() {
        printBox.setChecked(true);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.gift_card_balance_complete;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.print_gift_card_balance_header;
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintGiftCardBalanceCommand.start(getActivity(), changeAmount.toString(), printOrderCallback);
    }

    protected void sendDigitalOrder() {
        PayChooseCustomerDialog.show(getActivity(), true, new ChooseCustomerBaseDialog.emailSenderListener() {
            @Override
            public void onComplete() {
                listener.onConfirmed();
                dismiss();
            }
        }, changeAmount == null ? BigDecimal.ZERO.toString() : changeAmount.toString());
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
        }  else {
            completeProcess();
        }
    }

    @Override
    protected void completeProcess() {
        WaitDialogFragment.hide(getActivity());
        if (emailBox.isChecked()) {
            sendDigitalOrder();
        } else {
            super.completeProcess();
        }
    }

    public static void show(FragmentActivity context,IFinishConfirmListener listener, BigDecimal changeAmount) {
        DialogUtil.show(context, DIALOG_NAME, GiftCardBalanceFragmentDialog_.builder().changeAmount(changeAmount).build()).setListener(listener);
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
        public void onPrintError(PrinterError errorType) {
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

}
