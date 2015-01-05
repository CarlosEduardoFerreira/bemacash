package com.kaching123.tcr.print.processor;

import com.kaching123.tcr.commands.print.ParseHtmlCommand;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IReportsPrinter;
import com.kaching123.pos.util.ISignaturePrinter;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.print.pos.PrintSignatureOrderCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.priceFormat;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.print.processor.BasePrintProcessor.getCityStateZip;
import static com.kaching123.tcr.util.DateUtils.dateOnlyFormat;
import static com.kaching123.tcr.util.DateUtils.formatFull;

/**
 * Created by b1107005 on 12/7/2014.
 */
public class PrintWebReceiptProcessor  {


    public PrintWebReceiptProcessor() {

    }

    public void print(Context context, TcrApplication app, ITextPrinter printer, String[] printBuffer){
        if (context == null || printer == null || printBuffer == null) {
            return;
        }
        if (app.isTrainingMode()) {
            printer.subTitle(context.getString(R.string.training_mode_receipt_title));
            printer.emptyLine();
        }

        if (app.printLogo()) {
            printer.logo();
            printer.emptyLine();
        }
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();
        printer.header(shopInfo.name);
        for (String line: printBuffer) {
            if ( line.indexOf( ParseHtmlCommand.BOLD_PATTERN) >=0 ) {
                line = line.replaceAll(ParseHtmlCommand.BOLD_PATTERN,"");
                printer.add(line,true,false);
            }
            else {
                printer.add(line, false, false);
            }
        }
    }

}
