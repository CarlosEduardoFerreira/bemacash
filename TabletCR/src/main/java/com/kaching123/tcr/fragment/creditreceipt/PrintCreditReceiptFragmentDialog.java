package com.kaching123.tcr.fragment.creditreceipt;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.getbase.android.db.provider.ProviderAction;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.payment.credit.PrintCreditReceiptCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2.CreditReceiptTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptView;
import com.kaching123.tcr.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.DateUtils.cutTime;

/**
 * Created by gdubina on 14/03/14.
 */
@EFragment
public class PrintCreditReceiptFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "PrintCreditReceiptFragmentDialog";

    private static final Uri URI_CREDIT_RECEIPT = ShopProvider.getContentUri(CreditReceiptView.URI_CONTENT);

    @FragmentArg
    protected String receiptGuid;

    @ViewById
    protected TextView charge;

    @ViewById
    protected TextView receiptDate;

    @ViewById
    protected TextView receiptExpire;

    @ViewById
    protected TextView infoReceiptNum;


    @Override
    protected int getDialogContentLayout() {
        return R.layout.credeit_receipt_print_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.credit_receipt_print_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_print;
    }

    @Override
    protected boolean hasSkipButton() {
        return true;
    }

    @Override
    protected int getSkipButtonTitle() {
        return R.string.btn_email;
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                EmailCreditReceiptFragmentDialog.show(getActivity(), receiptGuid);
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                printChangeReceipt(false, false);
                return false;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.keyboard_popup_width),
                getResources().getDimensionPixelOffset(R.dimen.credit_receipt_print_dialog_height));

        new FindCreditReceipt().execute();
    }

    private void setCreditReceiptInfo(CreditReceiptModel receipt) {
        receiptDate.setText(DateUtils.dateOnlyFormat(receipt.createTime));
        Date expireTime = getExpireTime(receipt.createTime, receipt.expireTime);
        receiptExpire.setText(DateUtils.dateOnlyFormat(expireTime));
        long now = cutTime(Calendar.getInstance()).getTimeInMillis();
        if(now > expireTime.getTime()){
            receiptExpire.setTextColor(getResources().getColor(R.color.dlg_text_red));
        }
        infoReceiptNum.setText(String.valueOf(receipt.printNumber));
        showPrice(charge, receipt.amount);
    }

    private Date getExpireTime(Date createTime, int expireTime) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(createTime);
        cutTime(calendar);
        calendar.add(Calendar.DATE, expireTime);
        return calendar.getTime();
    }

    private void printChangeReceipt(boolean ignorePaperEnd, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintCreditReceiptCommand.start(getActivity(), ignorePaperEnd, searchByMac, receiptGuid, new PrintCreditReceiptCallback());
    }

    public class PrintCreditReceiptCallback extends BasePrintCallback {

        private IPrintCallback retryListener = new IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                printChangeReceipt(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };


        @Override
        protected void onPrintSuccess() {
            WaitDialogFragment.hide(getActivity());
            dismiss();
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
                                    CreditReceiptTable.EXPIRE_TIME,
                                    CreditReceiptTable.PRINT_NUMBER)
                            .where(CreditReceiptTable.GUID + " = ?", receiptGuid)
                            .perform(getActivity());
                    CreditReceiptModel model = null;
                    if (c.moveToFirst()) {
                        model = new CreditReceiptModel(
                                c.getString(0),
                                null,
                                0,
                                null,
                                new Date(c.getLong(1)),
                                _decimal(c, 2),
                                c.getLong(4),
                                c.getInt(3));
                    }
                    c.close();
                    return model;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<CreditReceiptModel> loader, CreditReceiptModel model) {
            getLoaderManager().destroyLoader(0);
            setCreditReceiptInfo(model);
        }

        @Override
        public void onLoaderReset(Loader<CreditReceiptModel> loader) {

        }

        public void execute() {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        }
    }

    public static void show(FragmentActivity activity, String receiptGuid){
        DialogUtil.show(activity,
                DIALOG_NAME,
                PrintCreditReceiptFragmentDialog_.builder().receiptGuid(receiptGuid).build());
    }
}
