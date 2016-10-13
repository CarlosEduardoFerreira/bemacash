package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.IXReportPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.kaching123.tcr.print.processor.BasePrintProcessor.getCityStateZip;

/**
 * Created by alboyko on 26.11.2015
 */
public class PrintZReportProcessor {

    private ZReportInfo report;
    private ReportType zReportType;
    private String registerDescription;
    private String registerID;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    private String fromDate;
    private String toDate;

    private final PublicGroundyTask.IAppCommandContext appCommandContext;

    public PrintZReportProcessor(ZReportInfo report, ReportType zReportType, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.report = report;
        this.zReportType = zReportType;
        this.appCommandContext = appCommandContext;
    }

    public void print(Context context, TcrApplication app, IXReportPrinter printer) {
        printHeader(context, app, printer);
        printBody(context, app, printer);
        printFooter(app, printer);
    }

    private void printHeader(Context context, TcrApplication app, IXReportPrinter printer) {
        if (app.isTrainingMode()) {
            printer.center(context.getString(R.string.training_mode_receipt_title));
            printer.emptyLine();
        }

        if (app.printLogo()) {
            printer.logo();
            printer.emptyLine();
        }

        final ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();

        printer.header(shopInfo.name);
        if (!TextUtils.isEmpty(shopInfo.address1)) {
            printer.footer(shopInfo.address1);
        }

        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)) {
            printer.footer(cityStateZip);
        }

        String phone = PhoneUtil.parse(shopInfo.phone);
        if (!TextUtils.isEmpty(phone)) {
            printer.footer(phone);
        }

        printer.drawLine();

        drawSubtitle(printer, context);

        printer.drawLine();
    }

    private void drawSubtitle(IXReportPrinter printer, Context context) {
        if (ReportType.Z_REPORT_CURRENT_SHIFT == zReportType) {
            printer.footer(context.getString(R.string.zreport_current_shift_subtitle));
            printer.pair(context.getString(R.string.zreprot_register_id_title_) + ":", registerID + (registerDescription == null || registerDescription.isEmpty() ? "" : " - " + registerDescription));
            printer.titledDate(context.getString(R.string.zreport_print_time), new Date());
        } else if (ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            printer.footer(context.getString(R.string.zreport_daily_subtitle));
            printer.pair(context.getString(R.string.zreprot_register_id_title_) + ":", registerID + (registerDescription == null || registerDescription.isEmpty() ? "" : " - " + registerDescription));
            printer.titledDate(context.getString(R.string.zreport_print_time), new Date());
        } else {
            printer.footer(context.getString(R.string.zreport_subtitle));
            printer.pair(context.getString(R.string.zreprot_register_id_title_) + ":", registerID + (registerDescription == null || registerDescription.isEmpty() ? "" : " - " + registerDescription));
        }
        printer.pair(context.getString(R.string.reprot_start_date) + ":", getFromDate());
        printer.pair(context.getString(R.string.reprot_to_date) + ":", getToDate());

    }

    private void printBody(Context context, TcrApplication app, IXReportPrinter printer) {
        if (ReportType.Z_REPORT_DAILY_SALES != zReportType) {
            printer.titledDate(context.getString(R.string.zreport_start_time), report.begin);
        }
        if (ReportType.Z_REPORT_CURRENT_SHIFT != zReportType && ReportType.Z_REPORT_DAILY_SALES != zReportType) {
            printer.titledDate(context.getString(R.string.xreport_end_time), report.end);
        }

        printer.emptyLine();

        printer.pair(context.getString(R.string.zreport_gross_sales), report.grossSales);
        printer.pair(context.getString(R.string.zreport_discounts), report.discounts, true);
        printer.pair(context.getString(R.string.zreport_returns), report.returns, true);
        printer.emptyLine();
        printer.drawLine();

        printer.boldPair(context.getString(R.string.zreport_net_sales), report.netSales, false);
        printer.pair(context.getString(R.string.zreport_gratuity), report.gratuity);
        printer.pair(context.getString(R.string.xreport_tax), report.tax);
        printer.pair(context.getString(R.string.xreport_transaction_fee), report.transactionFee);
        printer.emptyLine();
        printer.drawLine();

        printer.pair(context.getString(R.string.zreport_total_tender), report.totalTender);
        printer.pair(context.getString(R.string.zreport_cogs), report.cogs);
        printer.emptyLine();

        printer.pair(context.getString(R.string.zreport_gross_margin), report.grossMargin);
        printer.percent(report.grossMarginPercent);
        printer.emptyLine();

        printer.subtitle(context.getString(R.string.zreport_tender_summary), true);

        if (app.getShopInfo().acceptCreditCards) {
            if (report.cards != null && !report.cards.isEmpty()) {
                printer.subtitle(context.getString(R.string.zreport_credit_cards), false);
                ArrayList<String> cardNames = new ArrayList<String>(report.cards.keySet());
                Collections.sort(cardNames);
                for (String cardName : cardNames) {
                    printer.subPair(cardName, report.cards.get(cardName), 1, false);
                }
            }
        }

        if (app.getShopInfo().acceptDebitCards) {
            printer.pair(context.getString(R.string.zreport_debit), report.tenderDebit);
        }
        /*if (app.getShopInfo().acceptVoucherCards) {
            printer.pair(context.getString(R.string.zreport_voucher), report.tenderVoucher);
        }*/

        printer.pair(context.getString(R.string.zreport_cash), report.tenderCash);
        printer.pair(context.getString(R.string.zreport_credit_receipt), report.tenderCreditReceipt);
        printer.pair(context.getString(R.string.zreport_offline_credit), report.tenderOfflineCredit);
        //  printer.pair(context.getString(R.string.zreport_offline_debit), report.tenderOfflineDebit);
        // printer.pair(context.getString(R.string.zreport_offline_voucher), report.tenderOfflineVoucher);

        printer.pair(context.getString(R.string.xreport_check), report.tenderCheck);

        printer.drawLine();
        printer.emptyLine();

        printer.subtitle(context.getString(R.string.zreport_transaction_count), true);

        printer.pair(context.getString(R.string.zreport_sales), String.valueOf(report.salesCounter));
        printer.pair(context.getString(R.string.zreport_voids), String.valueOf(report.voidsCounter));
        printer.pair(context.getString(R.string.zreport_refunds), String.valueOf(report.refundsCounter));

        printer.drawLine();
        printer.emptyLine();

        printer.subtitle(context.getString(R.string.zreport_drops_payouts), true);
        printer.pair(context.getString(R.string.zreport_drops), String.valueOf(report.safeDrops));
        printer.pair(context.getString(R.string.zreport_payouts), String.valueOf(report.payOuts));

        printer.drawLine();

    }

    private void printFooter(TcrApplication app, IXReportPrinter printer) {
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();

        if (!TextUtils.isEmpty(shopInfo.email)) {
            printer.footer(shopInfo.email);
        }
        if (!TextUtils.isEmpty(shopInfo.site)) {
            printer.footer(shopInfo.site);
        }

    }

    public void setRegisterDescription(String registerDescription) {
        this.registerDescription = registerDescription;
    }

    public void setRegisterID(String registerID) {
        this.registerID = registerID;
    }
}
