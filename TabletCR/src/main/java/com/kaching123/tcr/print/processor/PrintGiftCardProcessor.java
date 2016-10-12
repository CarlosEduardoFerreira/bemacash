package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    protected boolean isGiftCard() {
        return true;
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.subTitle(context.getString(R.string.print_gift_card_balance_header));
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();

        printerWrapper.header(shopInfo.name);

        if (!TextUtils.isEmpty(shopInfo.address1)) {
            printerWrapper.footer(shopInfo.address1);
        }

        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)) {
            printerWrapper.footer(cityStateZip);
        }

        String phone = PhoneUtil.parse(shopInfo.phone);
        if (!TextUtils.isEmpty(phone)) {
            printerWrapper.footer(phone);
        }
        Date date=new Date();

        SimpleDateFormat df=new SimpleDateFormat("MMM dd, yyyy, hh:mm a");

        printerWrapper.header(context.getString(R.string.printer_date),df.format(new Date()).toString());
        printerWrapper.header(context.getString(R.string.tendering_history_total_cashier),app.getOperatorFullName());
        printerWrapper.emptyLine();

//        super.printHeader(context, app, printerWrapper);
    }

    @Override
    protected void printFooter(Context context, TcrApplication app, ITextPrinter printerWrapper) {
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

    @Override
    protected void printLoyalty(Context context, TcrApplication app, ITextPrinter printerWrapper) {
    }
}
