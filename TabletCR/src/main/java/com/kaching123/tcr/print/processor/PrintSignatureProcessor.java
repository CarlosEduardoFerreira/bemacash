package com.kaching123.tcr.print.processor;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Printer;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.printer.BitmapCarl;
import com.kaching123.pos.printer.BitmapPrintedCarl;
import com.kaching123.pos.util.ISignaturePrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBaseCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxSignature;
import com.kaching123.tcr.commands.print.pos.PrintSignatureOrderCommand.ReceiptType;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.io.IOException;
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

    protected void printBody(Context context, TcrApplication app, ISignaturePrinter printerWrapper) {

        printerWrapper.subTitle(context.getString(R.string.print_order_body_sale_subtitle));

        if (type == ReceiptType.DEBIT)
            printerWrapper.subTitle(context.getString(R.string.printer_debit_subtitle));
        else if (type == ReceiptType.EBT_CASH)
            printerWrapper.subTitle(context.getString(R.string.printer_ebt_cash_subtitle));
        else
            printerWrapper.subTitle(context.getString(R.string.printer_signature_subtitle));

        boolean printGratuityLine = app.isTipsEnabled();
        String tippedTransactionGuid = null;
        BigDecimal tipAmount = null;
        boolean tippedOrder = false;
        BigDecimal cashBackTotal = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(EmployeeTipsTable.URI_CONTENT, 1))
                .projection(EmployeeTipsTable.PAYMENT_TRANSACTION_ID, EmployeeTipsTable.AMOUNT)
                .where(EmployeeTipsTable.ORDER_ID + " = ?", orderGuid)
                .perform(context);
        if (c.moveToFirst()) {
            tippedTransactionGuid = c.getString(0);
            tipAmount = _decimal(c, 1, BigDecimal.ZERO);
            tippedOrder = true;
        }
        c.close();

        for (PaymentTransactionModel payment : transactions) {
            printerWrapper.drawLine();
            printerWrapper.date(payment.createTime);
            printerWrapper.cardName(payment.cardName);
            String shifted = UiHelper.formatLastFour(payment.lastFour);
            String entry = getEntryMethod(payment.entryMethod);
            String AID = payment.applicationIdentifier;
            String ARQC = payment.applicationCryptogramType;
            String approvalNumber = payment.resultCode;
            if (!TextUtils.isEmpty(shifted)) {
                printerWrapper.shiftedNumber(shifted);
            }
            if (!TextUtils.isEmpty(entry)) {
                printerWrapper.entryMethod(entry);
            }
            if (!TextUtils.isEmpty(AID)) {
                printerWrapper.aidNumber(AID);
            }
            if (!TextUtils.isEmpty(ARQC)) {
                printerWrapper.arqcNumber(ARQC);
            }
            if (!TextUtils.isEmpty(payment.authorizationNumber)) {
                printerWrapper.approvalNumber(payment.authorizationNumber);
            }
//            if (!TextUtils.isEmpty(payment.authorizationNumber))
//                printerWrapper.authNumber(payment.authorizationNumber);
            printerWrapper.amount(payment.amount);
            cashBackTotal = cashBackTotal.add(payment.cashBack.negate());
            amount = amount.add(payment.amount);


            if (type != ReceiptType.DEBIT && type != ReceiptType.EBT_CASH) {
                if (app.isTipsEnabled()) {
                    if (tippedOrder && tippedTransactionGuid.equals(payment.guid)) {
                        printerWrapper.addWithTab(context.getString(R.string.printer_gratuity_colon), priceFormat(tipAmount));
                        printerWrapper.addWithTab(context.getString(R.string.printer_total_colon), priceFormat(payment.amount.add(tipAmount)));
                    } else if (!tippedOrder) {
                        printerWrapper.emptyLine();
                        printerWrapper.addWithTab(context.getString(R.string.printer_gratuity_colon), context.getString(R.string.printer_manual_input_line));
                        printerWrapper.emptyLine();
                        printerWrapper.addWithTab(context.getString(R.string.printer_total_colon), context.getString(R.string.printer_manual_input_line));
                    }
                }
                printerWrapper.emptyLine();

                // Signature Line to customer to sign
                //printerWrapper.cropLine(context.getString(R.string.printer_signature_line));

                /** Pax Signature Bitmap Object ***********************************/
                PaxSignature paxSignature = PaxProcessorBaseCommand.paxSignature;
                Bitmap bmp = paxSignature.SignatureBitmapObject;
                BitmapCarl bitmapCarl = new BitmapCarl();
                /* Convert the Bitmap Object to be printed
                    133x90  (original)
                    166x113
                    199x120
                    266x180
                 */
                /**/
                BitmapPrintedCarl printedBitmapCarl = bitmapCarl.toPrint(bmp);
                printerWrapper.printPaxSignature(printedBitmapCarl.toPrint());
                /*********************************** Pax Signature Bitmap Object **/


                printerWrapper.subTitle(getCustomerName(payment.customerName));
                printerWrapper.emptyLine();


            }
        }
        if ((type == ReceiptType.DEBIT || type == ReceiptType.EBT_CASH) && cashBackTotal.compareTo(BigDecimal.ZERO) > 0) {
            printerWrapper.cashBack(cashBackTotal);
        }
        if ((type == ReceiptType.DEBIT || type == ReceiptType.EBT_CASH)) {
            printerWrapper.total(amount.add(cashBackTotal));
        }

        switch (type) {
            case EBT_CASH:
            case DEBIT:
            case CUSTOMER:
                printerWrapper.footer(context.getString(R.string.printer_signature_customer_copy));
                break;
            case MERCHANT:
                printerWrapper.footer(context.getString(R.string.printer_signature_merchant_copy_phrase1));
                printerWrapper.footer(context.getString(R.string.printer_signature_merchant_copy_phrase2));
                printerWrapper.emptyLine();
                printerWrapper.footer(context.getString(R.string.printer_signature_merchant_copy));
                break;
//            case DEBIT:
//                printerWrapper.footer(context.getString(R.string.printer_customer_debit_copy));
//                break;
//            case EBT_CASH:
//                printerWrapper.footer(context.getString(R.string.printer_customer_ebt_cash_copy));
//                break;
        }

        printerWrapper.drawLine();
    }

    private String getCustomerName(String customerName) {
        String[] names = customerName.split("/");
        if (names.length == 1)
            return customerName;
        else
            return names[1].trim() + " " + names[0].trim();
    }

    private String getEntryMethod(String entryMethod) {
        int method = Integer.parseInt(entryMethod.equalsIgnoreCase("") ? "6" : entryMethod);
        switch (method) {
            case 0:
                return "Manual";
            case 1:
                return "Swipe";
            case 2:
                return "Contactless";
            case 3:
                return "Scanner";
            case 4:
                return "Chip";
            case 5:
                return "Chip Fall Back Swipe";
            default:
                return "";
        }
    }

    @Override
    protected void printLoyalty(Context context, TcrApplication app, ISignaturePrinter printerWrapper) {

    }

    @Override
    protected void printFooter(Context context, TcrApplication app, ISignaturePrinter printerWrapper) {
        ShopInfo shopInfo = app.getShopInfo();
        if (!TextUtils.isEmpty(shopInfo.footerMsg1)) {
            printerWrapper.subTitle(shopInfo.footerMsg1);
        }
        if (!TextUtils.isEmpty(shopInfo.footerMsg2)) {
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
