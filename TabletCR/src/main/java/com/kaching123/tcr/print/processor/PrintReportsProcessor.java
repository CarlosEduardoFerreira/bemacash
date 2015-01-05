package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.IReportsPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import static com.kaching123.tcr.print.processor.BasePrintProcessor.getCityStateZip;

/**
 * Created by vkompaniets on 24.01.14.
 */
public abstract class PrintReportsProcessor<T> {

    protected Collection<T> report;
    private Date start;
    private Date end;
    protected String reportName;

    private final IAppCommandContext appCommandContext;

    public PrintReportsProcessor(String reportName, Collection<T> report, Date startDate, Date endDate, IAppCommandContext appCommandContext) {
        this.report = report;
        this.start = startDate;
        this.end = endDate;
        this.reportName = reportName;
        this.appCommandContext = appCommandContext;
    }

    public void print(Context context, TcrApplication app, IReportsPrinter printer) {
        printHeader(context, app, printer);
        printBody(context, app, printer);
        printFooter(context, app, printer);
    }

    private void printHeader(Context context, TcrApplication app, IReportsPrinter printer) {
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

        printer.drawLine();

        if (appCommandContext.getBlackstoneUser().isValid()){
            printer.startBody();
            printer.addWithTab(context.getString(R.string.print_order_merchant_id_label), "" + appCommandContext.getBlackstoneUser().getMid(), false);
            printer.endBody();
            printer.drawLine();
        }
    }

    protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
        printer.subTitle(reportName);
        printer.drawLine();

        printer.dateRange(context.getString(R.string.report_date_range_label), start, end);
        printer.time(context.getString(R.string.report_print_time_label), new Date());
        printer.drawLine();

        printTableHeader(printer);

        printer.emptyLine();

        BigDecimal total = BigDecimal.ZERO;
        printer.startBody();
        for (T item : report){
            BigDecimal r = printItem(printer, item);
            if(r != null){
                total = total.add(r);
            }
        }
        printer.endBody();
        printer.emptyLine();

        printTotal(printer, context.getString(R.string.report_sale_by_items_total), total);
        printer.drawLine();
    }

    protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total){
        printer.total(totalLabel, total);
    }

    protected abstract void printTableHeader(IReportsPrinter printer);
    protected abstract BigDecimal printItem(IReportsPrinter printer, T item);

    private void printFooter(Context context, TcrApplication app, IReportsPrinter printer) {
        ShopInfo shopInfo = app.getShopInfo();

        if (!TextUtils.isEmpty(shopInfo.email)) {
            printer.footer(shopInfo.email, true);
        }
        if (!TextUtils.isEmpty(shopInfo.site)) {
            printer.footer(shopInfo.site, true);
        }
    }
}
