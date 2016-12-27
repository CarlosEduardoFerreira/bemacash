package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBaseCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxSignature;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.util.PhoneUtil;

import java.io.IOException;
import java.util.Calendar;

import static com.kaching123.tcr.print.FormatterUtil.priceFormat;
import static com.kaching123.tcr.print.processor.BasePrintProcessor.getCityStateZip;
import static com.kaching123.tcr.util.DateUtils.dateOnlyFormat;
import static com.kaching123.tcr.util.DateUtils.formatFull;

/**
 * Created by vkompaniets on 26.02.14.
 */
public class PrintCreditProcessor {

    private final CreditReceiptModel model;
    private final String registerTitle;
    private boolean isCopy;

    private final String orderNumber;

    public PrintCreditProcessor(CreditReceiptModel model, String registerTitle, boolean isCopy) {
        this.model = model;
        this.registerTitle = registerTitle;
        this.isCopy = isCopy;
        orderNumber = registerTitle + "-" + model.printNumber;
    }

    public void print(Context context, TcrApplication app, ITextPrinter printer){
        printHeader(context, app, printer);
        printBody(context, app, printer);
        printFooter(context, app, printer);
    }

    private void printHeader(Context context, TcrApplication app, ITextPrinter printer) {
        if (app.isTrainingMode()) {
            printer.subTitle(context.getString(R.string.training_mode_receipt_title));
            printer.emptyLine();
        }

        if (app.printLogo()) {
            printer.logo();
            printer.emptyLine();
        }
        ShopInfo shopInfo = app.getShopInfo();
        printer.header(shopInfo.name);

        if (!TextUtils.isEmpty(shopInfo.address1)) {
            printer.footer(shopInfo.address1);
        }

        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)){
            printer.footer(cityStateZip);
        }

        String phone = PhoneUtil.parse(shopInfo.phone);
        if (!TextUtils.isEmpty(phone)) {
            printer.footer(phone);
        }

        printer.emptyLine();
    }

    private void printBody(Context context, TcrApplication app, ITextPrinter printer) {

        /*printer.addWithTab(context.getString(R.string.printer_merchant_id), String.valueOf(app.getShopInfo().prepaidMid), true, false);
        printer.addWithTab(context.getString(R.string.printer_terminal_id), String.valueOf(app.getPrepaidUser().getTid()), true, false);*/
        printer.addWithTab(context.getString(R.string.credit_receipt_print_date), formatFull(model.createTime), true, false);
        printer.drawLine();
        printer.subTitle(context.getString(R.string.credit_receipt_print_subtitle));
        if (isCopy){
            printer.footer(context.getString(R.string.print_order_copy_header));
        }
        printer.addWithTab(orderNumber, "", true, false);
        printer.drawLine();
        printer.addWithTab(context.getString(R.string.credit_receipt_print_amount), priceFormat(model.amount), true, true);
        printer.emptyLine();

        Calendar c = Calendar.getInstance();
        c.setTime(model.createTime);
        c.add(Calendar.DATE, model.expireTime);

        printer.addWithTab(context.getString(R.string.credit_receipt_print_valid_through) + " " + dateOnlyFormat(c.getTime()), "", true, false);
        printer.emptyLine();
        printer.add(context.getString(R.string.credit_receipt_print_signature), false, true);
        printer.emptyLine();
    }

    private void printFooter(Context context, TcrApplication app, ITextPrinter printer) {
        printer.barcode(orderNumber);

        ShopInfo shopInfo = app.getShopInfo();
        if (!TextUtils.isEmpty(shopInfo.footerMsg1)){
            printer.subTitle(shopInfo.footerMsg1);
        }
        if (!TextUtils.isEmpty(shopInfo.footerMsg2)){
            printer.subTitle(shopInfo.footerMsg2);
        }
        if (!TextUtils.isEmpty(shopInfo.email)) {
            printer.footer(shopInfo.email, true);
        }
        if (!TextUtils.isEmpty(shopInfo.site)) {
            printer.footer(shopInfo.site, true);
        }
        if (!TextUtils.isEmpty(shopInfo.thanksPhrase)) {
            printer.footer(shopInfo.thanksPhrase);
        }

    }

}
