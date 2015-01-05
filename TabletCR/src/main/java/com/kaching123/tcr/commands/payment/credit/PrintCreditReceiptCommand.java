package com.kaching123.tcr.commands.payment.credit;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.PosPrinter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintCreditProcessor;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2.CreditReceiptTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptView;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by gdubina on 25/02/14.
 */
public class PrintCreditReceiptCommand extends BasePrintCommand<PosOrderTextPrinter> {

    private static final Uri REGISTER_URI = ShopProvider.getContentUri(RegisterTable.URI_CONTENT);
    private static final Uri CREDIT_RECEIPT_URI = ShopProvider.getContentUri(CreditReceiptView.URI_CONTENT);

    protected static final String ARG_RECEIPT = "ARG_RECEIPT";
    protected static final String ARG_RECEIPT_GUID = "ARG_RECEIPT_GUID";

    private CreditReceiptModel model;
    private String registerTitle;
    private boolean isCopy;

    @Override
    protected PosOrderTextPrinter createTextPrinter() {
        return new PosOrderTextPrinter();
    }

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {
        String guid = getStringArg(ARG_RECEIPT_GUID);
        if (!TextUtils.isEmpty(guid)) {
            CreditReceiptViewModel view = loadReceipt(getContext(), guid);
            if(view == null){
                return failed();
            }
            isCopy = true;
            model = view.model;
            registerTitle = view.registerName;
        } else {
            model = (CreditReceiptModel) getArgs().getSerializable(ARG_RECEIPT);
            registerTitle = getRegisterTitle(getContext(), model.registerId);
        }

        return super.execute(printer);
    }

    @Override
    protected void printBody(PosOrderTextPrinter printer) {
        PrintCreditProcessor processor = new PrintCreditProcessor(model, registerTitle, isCopy);
        processor.print(getContext(), getApp(), printer);
        Logger.d("[CREDIT_RECEIPT] [%s]-%s %s", registerTitle, model.printNumber, model.amount);
    }

    public static CreditReceiptViewModel loadReceipt(Context context, String guid) {
        Cursor c = ProviderAction
                .query(CREDIT_RECEIPT_URI)
                .where(CreditReceiptTable.GUID + " = ?", guid)
                .perform(context);
        CreditReceiptViewModel model = null;
        if (c.moveToFirst()) {
            model = new CreditReceiptViewModel(
                    new CreditReceiptModel(
                            c.getString(c.getColumnIndex(CreditReceiptTable.GUID)),
                            c.getString(c.getColumnIndex(CreditReceiptTable.CASHIER_GUID)),
                            c.getLong(c.getColumnIndex(CreditReceiptTable.REGISTER_ID)),
                            c.getString(c.getColumnIndex(CreditReceiptTable.SHIFT_ID)),
                            new Date(c.getLong(c.getColumnIndex(CreditReceiptTable.CREATE_TIME))),
                            _decimal(c, c.getColumnIndex(CreditReceiptTable.AMOUNT)),
                            c.getLong(c.getColumnIndex(CreditReceiptTable.PRINT_NUMBER)),
                            c.getInt(c.getColumnIndex(CreditReceiptTable.EXPIRE_TIME))),
                    c.getString(c.getColumnIndex(CreditReceiptView2.RegisterTable.TITLE))
            );
        }
        c.close();
        return model;
    }

    public static class CreditReceiptViewModel {
        public CreditReceiptModel model;
        public String registerName;

        public CreditReceiptViewModel(CreditReceiptModel model, String registerName) {
            this.model = model;
            this.registerName = registerName;
        }
    }

    public static String getRegisterTitle(Context context, long registerId) {
        Cursor c = ProviderAction.query(REGISTER_URI)
                .projection(RegisterTable.TITLE)
                .where(RegisterTable.ID + " = ?", registerId)
                .perform(context);

        String registerTitle = null;
        if (c.moveToFirst()) {
            registerTitle = c.getString(0);
        }
        c.close();
        return registerTitle;
    }

    public static void start(Context context, boolean ignorePaperEnd, boolean searchByMac, CreditReceiptModel receipt, BasePrintCallback callback) {
        create(PrintCreditReceiptCommand.class)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_RECEIPT, receipt)
                .callback(callback)
                .queueUsing(context);
    }

    public static void start(Context context, boolean ignorePaperEnd, boolean searchByMac, String guid, BasePrintCallback callback) {
        create(PrintCreditReceiptCommand.class)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_RECEIPT_GUID, guid)
                .callback(callback)
                .queueUsing(context);
    }
}
