package com.kaching123.tcr.fragment.tendering.payment;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.getbase.android.db.provider.ProviderAction;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.credit.CreditSaleCommand.CreditSaleCommandBaseCallback;
import com.kaching123.tcr.commands.payment.credit.PrintCreditReceiptCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.OrderNumberFormatInputFilter;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.BarcodeListenerHolder;
import com.kaching123.tcr.model.BarcodeListenerHolder.BarcodeListener;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.payment.credit.CreditReceiptData;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2.CreditReceiptTable;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2.RegisterTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptView;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.DateUtils.cutTime;
import static com.kaching123.tcr.util.Util.toInt;

/**
 * Created by gdubina on 24/02/14.
 */
@EFragment
public class PayCreditReceiptFragmentDialog extends StyledDialogFragment implements CustomEditBox.IKeyboardSupport {

    private static final Uri URI_CREDIT_RECEIPT = ShopProvider.getContentUri(CreditReceiptView.URI_CONTENT);

    private static final String DIALOG_NAME = "PayCreditReceiptFragmentDialog";

    @ViewById
    protected CustomEditBox creditReceiptEdit;

    @ViewById
    protected View inputBlock;

    @ViewById
    protected View infoBlock;

    @ViewById
    protected View printingBlock;

    @ViewById
    protected TextView total;

    @ViewById
    protected TextView charge;

    @ViewById
    protected TextView pending;

    @ViewById
    protected TextView receiptDate;

    @ViewById
    protected TextView receiptExpire;

    @ViewById
    protected TextView infoReceiptNum;

    private boolean secondStep = false;

    @FragmentArg
    protected Transaction transaction;

    private ICreditReceiptPaymentListener listener;

    @ViewById
    protected KeyboardView keyboard;

    @AfterViews
    protected void attachViews() {
        Logger.d("PayCreditReceiptFragmentDialog: attachViews()");
        creditReceiptEdit.setFilters(new InputFilter[]{new OrderNumberFormatInputFilter()});
        creditReceiptEdit.setKeyboardSupportConteiner(this);
        creditReceiptEdit.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                return onOkPressed();
            }
        });
        creditReceiptEdit.requestFocus();

        keyboard.setDotEnabled(false);
        keyboard.setMinusVisible(true);

        Logger.d("PayCreditReceiptFragmentDialog: attachViews(): set barcode listener");
        if (getBarcodeListenerHolder() != null)
            getBarcodeListenerHolder().setBarcodeListener(barcodeListener);
        else
            Logger.d("PayCreditReceiptFragmentDialog: attachViews(): set barcode listener: failed - can not get listener holder!");
    }

    @Override
    public void onDetach() {
        Logger.d("PayCreditReceiptFragmentDialog: onDetach()");
        Logger.d("PayCreditReceiptFragmentDialog: onDetach(): restore default barcode listener");
        if (getBarcodeListenerHolder() != null)
            getBarcodeListenerHolder().setDefaultBarcodeListener();
        else
            Logger.d("PayCreditReceiptFragmentDialog: onDetach(): restore default barcode listener: failed - can not get listener holder!");

        super.onDetach();
    }

    private BarcodeListenerHolder getBarcodeListenerHolder() {
        Logger.d("PayCreditReceiptFragmentDialog: getBarcodeListenerHolder()");
        if (getActivity() instanceof BarcodeListenerHolder)
            return (BarcodeListenerHolder) getActivity();
        Logger.d("PayCreditReceiptFragmentDialog: getBarcodeListenerHolder(): failed - can not get listener holder!");
        return null;
    }

    @AfterTextChange
    protected void creditReceiptEditAfterTextChanged(Editable s) {
        enablePositiveButtons(validate());
    }

    @Override
    protected void enablePositiveButtons(boolean enable) {
        super.enablePositiveButtons(enable);
        keyboard.setEnterEnabled(enable);
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_credit_receipt_container_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.credit_receipt_dlg_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                return onOkPressed();
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if(changeReceipt != null){
                    listener.onPaymentCompleted(transaction.getAmount());
                }else{
                    listener.onCancel();
                }
                return true;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.keyboard_popup_width),
                getResources().getDimensionPixelOffset(R.dimen.pay_credit_receipt_dialog_height));
        enablePositiveButtons(false);
        showPrice(total, transaction.amount);
        setCancelable(false);
    }

    private boolean onOkPressed() {
        if(changeReceipt != null){
            printChangeReceipt(false, false);
            return false;
        }
        if (!secondStep) {
            goToSecondStep();
            return false;
        }
        return validateReceipt();
    }

    private void goToSecondStep() {

        String creditReceipt = creditReceiptEdit.getText().toString();
        String register = null;
        String receiptNum = null;
        if (!TextUtils.isEmpty(creditReceipt)) {
            String[] ar = creditReceipt.split("-");
            if (ar.length > 0) {
                register = ar[0];
            }
            if (ar.length > 1) {
                receiptNum = ar[1];
            }
        }

        new FindCreditReceipt(register, toInt(receiptNum, 0)).execute();
    }

    private void goToSecondStep(CreditReceiptModel receipt) {
        Logger.d("PayCreditReceiptFragmentDialog: goToSecondStep()");
        //WaitDialogFragment.hide(getActivity());
        if (receipt == null) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.credit_receipt_not_found));
            return;
        }

        //update receipt info
        receiptDate.setText(DateUtils.dateOnlyFormat(receipt.createTime));
        Date expireTime = getExpireTime(receipt.createTime, receipt.expireTime);
        receiptExpire.setText(DateUtils.dateOnlyFormat(expireTime));
        long now = cutTime(Calendar.getInstance()).getTimeInMillis();
        if(now > expireTime.getTime()){
            receiptExpire.setTextColor(getResources().getColor(R.color.dlg_text_red));
            enablePositiveButtons(false);
        }
        infoReceiptNum.setText(String.valueOf(receipt.printNumber));

        BigDecimal pendingAmount = receipt.amount.subtract(transaction.amount);
        //update amount
        showPrice(charge, receipt.amount);
        showPrice(pending, pendingAmount);

        showInfoPanel();
        secondStep = true;

        if (getDisplayBinder() != null) {
            getDisplayBinder().startCommand(new DisplayTenderCommand(receipt.amount, pendingAmount));
        }

        Logger.d("PayCreditReceiptFragmentDialog: goToSecondStep(): restore default barcode listener");
        if (getBarcodeListenerHolder() != null)
            getBarcodeListenerHolder().setDefaultBarcodeListener();
        else
            Logger.d("PayCreditReceiptFragmentDialog: goToSecondStep(): restore default barcode listener: failed - listener holder is not set!");
    }

    private IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof IDisplayBinder) {
            return (IDisplayBinder) getActivity();
        }
        return null;
    }

    private Date getExpireTime(Date createTime, int expireTime) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(createTime);
        cutTime(calendar);
        calendar.add(Calendar.DATE, expireTime);
        return calendar.getTime();
    }

    private void showInfoPanel() {
        int width = inputBlock.getWidth();

        ObjectAnimator inputAnimator = ObjectAnimator.ofFloat(inputBlock, "x", 0f, -100f);
        inputAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                inputBlock.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        ObjectAnimator infoAnimator = ObjectAnimator.ofFloat(infoBlock, "x", width, 0f);
        infoAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                infoBlock.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        AnimatorSet transition = new AnimatorSet();
        transition.playTogether(
                inputAnimator,
                ObjectAnimator.ofFloat(inputBlock, "alpha", 1f, 0f),
                infoAnimator);
        transition.setDuration(200);
        transition.start();
    }

    private boolean validate() {
        String creditReceipt = creditReceiptEdit.getText().toString();
        String register = null;
        String receiptNum = null;
        if (!TextUtils.isEmpty(creditReceipt)) {
            String[] ar = creditReceipt.split("-");
            if (ar.length > 0) {
                register = ar[0];
            }
            if (ar.length > 1) {
                receiptNum = ar[1];
            }
        }

        return !TextUtils.isEmpty(register) && !TextUtils.isEmpty(receiptNum);
    }

    private boolean validateReceipt() {

        String creditReceipt = creditReceiptEdit.getText().toString();
        String register = null;
        String receiptNum = null;
        if (!TextUtils.isEmpty(creditReceipt)) {
            String[] ar = creditReceipt.split("-");
            if (ar.length > 0) {
                register = ar[0];
            }
            if (ar.length > 1) {
                receiptNum = ar[1];
            }
        }
        if (TextUtils.isEmpty(register) || TextUtils.isEmpty(receiptNum)) {
            return false;
        }
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_dialog_title));
        PaymentGateway.CREDIT.gateway().sale(
                getActivity(),
                new CreditSaleCallback(),
                null,
                new CreditReceiptData(register, receiptNum), transaction);
        return false;
    }

    public void setListener(ICreditReceiptPaymentListener listener) {
        this.listener = listener;
    }

    private BarcodeListener barcodeListener = new BarcodeListener() {
        @Override
        public void onBarcodeReceived(String barcode) {
            Logger.d("PayCreditReceiptFragmentDialog: barcodeListener: onBarcodeReceived(): barcode = " + barcode);
            if (creditReceiptEdit != null) {
                Logger.d("PayCreditReceiptFragmentDialog: barcodeListener: onBarcodeReceived(): set barcode to edit text");
                creditReceiptEdit.setText(barcode);
            } else {
                Logger.d("PayCreditReceiptFragmentDialog: barcodeListener: onBarcodeReceived(): set barcode to edit text: failed - no edit text!");
            }
        }
    };

    public static interface ICreditReceiptPaymentListener {

        void onPaymentCompleted(BigDecimal amount);

        void onCancel();

    }

    private void finishWithSuccess() {
        WaitDialogFragment.hide(getActivity());
        listener.onPaymentCompleted(transaction.getAmount());
        dismiss();
    }

    private CreditReceiptModel changeReceipt;

    private void printChangeReceipt(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintCreditReceiptCommand.start(getActivity(), ignorePaperEnd, searchByMac, changeReceipt, new PrintCreditReceiptCallback());
    }

    public class CreditSaleCallback extends CreditSaleCommandBaseCallback {

        @Override
        protected void handleOnSuccess(CreditReceiptModel changeReceipt) {
            if (changeReceipt != null) {
                showPrintBlock();
                PayCreditReceiptFragmentDialog.this.changeReceipt = changeReceipt;
                printChangeReceipt(false, false);
                return;
            }
            finishWithSuccess();
        }

        @Override
        protected void handleOnFailure() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.show(getActivity(), DialogType.ALERT, R.string.error_dialog_title, getString(R.string.credit_receipt_proceed_error), R.string.btn_ok, new OnDialogClickListener() {
                @Override
                public boolean onClick() {
                    listener.onCancel();
                    dismiss();
                    return true;
                }
            });
        }
    }

    private void showPrintBlock() {
        enablePositiveButtons(true);
        getPositiveButton().setText(R.string.btn_print);
        getNegativeButton().setText(R.string.btn_finish);
        infoBlock.setVisibility(View.GONE);
        printingBlock.setVisibility(View.VISIBLE);
    }

    public class PrintCreditReceiptCallback extends BasePrintCallback {

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printChangeReceipt(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                finishWithSuccess();
            }
        };


        @Override
        protected void onPrintSuccess() {
            finishWithSuccess();
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


    private class FindCreditReceipt implements LoaderCallbacks<CreditReceiptModel> {

        final String register;
        final int receiptNum;

        private FindCreditReceipt(String register, int receiptNum) {
            this.register = register;
            this.receiptNum = receiptNum;
        }

        @Override
        public Loader<CreditReceiptModel> onCreateLoader(int i, Bundle bundle) {
            return new AsyncTaskLoader<CreditReceiptModel>(getActivity()) {
                @Override
                public CreditReceiptModel loadInBackground() {
                    Cursor c = ProviderAction
                            .query(URI_CREDIT_RECEIPT)
                            .projection(
                                    CreditReceiptTable.GUID,
                                    CreditReceiptTable.CREATE_TIME,
                                    CreditReceiptTable.AMOUNT,
                                    CreditReceiptTable.EXPIRE_TIME)
                            .where(RegisterTable.TITLE + " = ?", register)
                            .where(CreditReceiptTable.PRINT_NUMBER + " = ?", receiptNum)
                            .perform(getActivity());
                    CreditReceiptModel model = null;
                    if (c.moveToFirst()) {
                        model = new CreditReceiptModel(
                                c.getString(0),
                                null, 0, null,
                                new Date(c.getLong(1)),
                                _decimal(c, 2, BigDecimal.ZERO),
                                receiptNum,
                                c.getInt(3));
                    }
                    c.close();
                    return model;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<CreditReceiptModel> loader, CreditReceiptModel model) {
            handleResult(model);
        }

        @Override
        public void onLoaderReset(Loader<CreditReceiptModel> loader) {
            //handleResult(null);
        }

        public void handleResult(final CreditReceiptModel model) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    getLoaderManager().destroyLoader(0);
                    goToSecondStep(model);
                }
            });
        }

        public void execute() {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        }
    }

    public static void show(FragmentActivity activity, Transaction transaction, ICreditReceiptPaymentListener onResultListener) {
        DialogUtil.show(activity, DIALOG_NAME,
                PayCreditReceiptFragmentDialog_.builder().transaction(transaction).build())
                .setListener(onResultListener);
    }
}
