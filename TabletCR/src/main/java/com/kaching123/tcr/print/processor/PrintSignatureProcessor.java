package com.kaching123.tcr.print.processor;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ISignaturePrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.print.pos.PrintSignatureOrderCommand.ReceiptType;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.fragment.UiHelper.priceFormat;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 13.01.14.
 */
public class PrintSignatureProcessor extends BasePrintProcessor<ISignaturePrinter> {

    private ArrayList<PaymentTransactionModel> transactions;
    private ReceiptType type;

    public PrintSignatureProcessor(String orderGuid, ArrayList<PaymentTransactionModel> transactions, ReceiptType receiptType, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.transactions = transactions;
        this.type = receiptType;
    }

    @Override
    public void print(final Context context, final TcrApplication app, final ISignaturePrinter printerWrapper) {
        prePrintHeader(context, app, printerWrapper);
        printHeader(context, app, printerWrapper);
        printBody(context, app, printerWrapper);
        printFooter(app, printerWrapper);
    }

    protected void printBody(Context context, TcrApplication app, ISignaturePrinter printerWrapper) {

        printerWrapper.subTitle(context.getString(R.string.printer_signature_subtitle));

        boolean printGratuityLine = app.isTipsEnabled();
        String tippedTransactionGuid = null;
        BigDecimal tipAmount = null;
        boolean tippedOrder = false;
        Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(EmployeeTipsTable.URI_CONTENT, 1))
                .projection(EmployeeTipsTable.PAYMENT_TRANSACTION_ID, EmployeeTipsTable.AMOUNT)
                .where(EmployeeTipsTable.ORDER_ID + " = ?", orderGuid)
                .perform(context);
        if (c.moveToFirst()){
            tippedTransactionGuid = c.getString(0);
            tipAmount = _decimal(c, 1);
            tippedOrder = true;
        }
        c.close();

        for (PaymentTransactionModel payment : transactions) {
            printerWrapper.drawLine();
            printerWrapper.date(payment.createTime);
            printerWrapper.cardName(payment.cardName);
            String shifted = UiHelper.formatLastFour(payment.lastFour);
            if (!TextUtils.isEmpty(shifted)) {
                printerWrapper.shiftedNumber(shifted);
            }
            if (!TextUtils.isEmpty(payment.authorizationNumber))
                printerWrapper.authNumber(payment.authorizationNumber);
            printerWrapper.amount(payment.amount);
            if (app.isTipsEnabled()){
                if (tippedOrder && tippedTransactionGuid.equals(payment.guid)){
                    printerWrapper.addWithTab(context.getString(R.string.printer_gratuity_colon), priceFormat(tipAmount));
                    printerWrapper.addWithTab(context.getString(R.string.printer_total_colon), priceFormat(payment.amount.add(tipAmount)));
                } else if (!tippedOrder){
                    printerWrapper.emptyLine();
                    printerWrapper.addWithTab(context.getString(R.string.printer_gratuity_colon), context.getString(R.string.printer_manual_input_line));
                    printerWrapper.emptyLine();
                    printerWrapper.addWithTab(context.getString(R.string.printer_total_colon), context.getString(R.string.printer_manual_input_line));
                }
            }
            printerWrapper.emptyLine();
            printerWrapper.cropLine(context.getString(R.string.printer_signature_line));
            printerWrapper.emptyLine();
        }

        switch (type){
            case CUSTOMER:
                printerWrapper.footer(context.getString(R.string.printer_signature_customer_copy));
                break;
            case MERCHANT:
                printerWrapper.footer(context.getString(R.string.printer_signature_merchant_copy_phrase1));
                printerWrapper.footer(context.getString(R.string.printer_signature_merchant_copy_phrase2));
                printerWrapper.emptyLine();
                printerWrapper.footer(context.getString(R.string.printer_signature_merchant_copy));
                break;
        }

        printerWrapper.drawLine();
    }

    @Override
    protected void printFooter(TcrApplication app, ISignaturePrinter printerWrapper) {
        ShopInfo shopInfo = app.getShopInfo();
        if (!TextUtils.isEmpty(shopInfo.footerMsg1)){
            printerWrapper.subTitle(shopInfo.footerMsg1);
        }
        if (!TextUtils.isEmpty(shopInfo.footerMsg2)){
            printerWrapper.subTitle(shopInfo.footerMsg2);
        }
        if (!TextUtils.isEmpty(shopInfo.email)) {
            printerWrapper.footer(shopInfo.email, true);
        }
        if (!TextUtils.isEmpty(shopInfo.site)) {
            printerWrapper.footer(shopInfo.site, true);
        }
        if (!TextUtils.isEmpty(shopInfo.thanksPhrase)) {
            printerWrapper.footer(shopInfo.thanksPhrase);
        }
    }

    @Override
    protected void printMidTid(ISignaturePrinter printer, String label, String value, boolean bold) {
        printer.addWithTab(label, value);
    }
}