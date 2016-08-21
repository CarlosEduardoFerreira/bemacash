package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery.PrintHandler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by gdubina on 23.12.13.
 */
public class PrintGiftCardProcessor extends BasePrintProcessor<ITextPrinter> {

    private boolean reprint;

    private final String TRANSACTION_FEE = "Transaction Fee";

    private BigDecimal amount;

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PrintGiftCardProcessor(IAppCommandContext appCommandContext) {
        super(null, appCommandContext);
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.subTitle(context.getString(R.string.print_gift_card_balance_header));
        printerWrapper.emptyLine();

//        super.printHeader(context, app, printerWrapper);
    }

    @Override
    protected void printFooter(TcrApplication app, ITextPrinter printerWrapper) {
//        super.printFooter(app, printerWrapper);
    }

    @Override
    protected void prePrintHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
//        super.prePrintHeader(context, app, printerWrapper);
    }

    @Override
    protected void printMidTid(Context context, TcrApplication app, ITextPrinter printerWrapper, OrderType type) {
//        super.printMidTid(context, app, printerWrapper, type);
    }


    @Override
    protected void printMidTid(ITextPrinter printer, String label, String value, boolean bold) {

    }

    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {

        printerWrapper.orderFooter(context.getString(R.string.printer_gift_card_balance_title), amount);


    }
}
