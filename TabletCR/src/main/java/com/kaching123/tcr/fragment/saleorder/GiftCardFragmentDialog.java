package com.kaching123.tcr.fragment.saleorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorGiftCardReloadCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorSaleCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.BaseKitchenPrintCallback;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper.IKitchenPrintCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.SaleOrderFunction;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by gdubina on 13/11/13.
 */
@EFragment
public class GiftCardFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "GiftCardFragmentDialog";

    private IGiftCardListener listener;

    private Calendar calendar = Calendar.getInstance();

    @ViewById
    protected Button btnReload, btnBalance;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_gift_card_width),
                getResources().getDimensionPixelOffset(R.dimen.holdon_gift_card_heigth));

        iniTitle();
    }

    private void iniTitle() {

    }


    @Override
    protected int getDialogContentLayout() {
        return R.layout.gift_card_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_gift_card_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_gift_card_cancel;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected boolean hasSkipButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Click
    protected void btnReload()
    {
        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
        listener.Reload();
        this.dismiss();
//        paxGateway.reload(getActivity(), reloadGiftCardCallBack(),  null, null, transaction, reloadResponse);
    }

    @Click
    protected void btnBalance()
    {
        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
        paxGateway.doBalance(getActivity(),balanceGiftCardCallBack());
    }

    private Object reloadGiftCardCallBack(){
        return new PaxProcessorGiftCardReloadCommand.PaxSaleCommandBaseCallback(){

            @Override
            protected void handleSuccess(Transaction result, String errorReason) {

            }

            @Override
            protected void handleError() {

            }
        };
    }
    private Object balanceGiftCardCallBack () {
        return new PaxBlackstoneBalanceCommand.PaxBalanceCommandBaseCallback() {

            @Override
            protected void handleSuccess(BigDecimal result, String last4, String errorReason) {
                listener.Balance(result, last4, errorReason);
            }

            @Override
            protected void handleError() {
                listener.Balance(BigDecimal.ZERO, "", "Error");
            }
        };
    }
//    private void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
//        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
//        PrintItemsForKitchenCommand.start(getActivity(), skipPaperWarning, searchByMac, argOrderGuid, fromPrinter, skip, new KitchenKitchenPrintCallback(), false, orderTitle.getText().toString());
//    }

//    private class KitchenKitchenPrintCallback extends BaseKitchenPrintCallback {
//
//        private IKitchenPrintCallback skipListener = new IKitchenPrintCallback() {
//
//            @Override
//            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
//                printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac);
//            }
//
//            @Override
//            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
//                printItemsToKitchen(fromPrinter, true, ignorePaperEnd, searchByMac);
//            }
//        };
//
//        @Override
//        protected void onPrintSuccess() {
//            WaitDialogFragment.hide(getActivity());
//
//            dismiss();
//        }
//
//        @Override
//        protected void onPrintError(PrinterError error, String fromPrinter, String aliasTitle) {
//            KitchenPrintCallbackHelper.onPrintError(getActivity(), error, fromPrinter, aliasTitle, skipListener);
//        }
//
//        @Override
//        protected void onPrinterNotConfigured(String fromPrinter, String aliasTitle) {
//            KitchenPrintCallbackHelper.onPrinterNotConfigured(getActivity(), fromPrinter, aliasTitle, skipListener);
//        }
//
//        @Override
//        protected void onPrinterDisconnected(String fromPrinter, String aliasTitle) {
//            KitchenPrintCallbackHelper.onPrinterDisconnected(getActivity(), fromPrinter, aliasTitle, skipListener);
//        }
//
//        @Override
//        protected void onPrinterIPnotFound(String fromPrinter, String aliasTitle) {
//            KitchenPrintCallbackHelper.onPrinterIPnotfound(getActivity(), fromPrinter, aliasTitle, skipListener);
//        }
//
//        @Override
//        protected void onPrinterPaperNearTheEnd(String fromPrinter, String aliasTitle) {
//            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(getActivity(), fromPrinter, aliasTitle, skipListener);
//        }
//    }

    public void setListener(IGiftCardListener listener) {
        this.listener = listener;
    }

    public static interface IGiftCardListener {
        void Reload();
        void Balance(BigDecimal result, String last4, String errorReason);
    }

    public static void show(FragmentActivity context, IGiftCardListener listener) {
        DialogUtil.show(context, DIALOG_NAME, GiftCardFragmentDialog_.builder().build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
