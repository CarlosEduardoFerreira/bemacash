package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.IXReportPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.DepartsSale;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.reports.SalesByItemsReportQuery;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static com.kaching123.tcr.print.processor.BasePrintProcessor.getCityStateZip;

/**
 * Created by vkompaniets on 21.01.14.
 */
public class PrintXReportProcessor {

    private final static String ITEM_STRING_FORMAT = "%s (%s)";

    private XReportInfo report;
    private ReportType xReportType;
    private boolean enableEreportDepartSale;

    private final IAppCommandContext appCommandContext;
    private boolean enableEreportItemSale;
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
    public PrintXReportProcessor(XReportInfo report, ReportType xReportType, IAppCommandContext appCommandContext, boolean enableEreportDepartSale, boolean enableEreportItemSale) {
        this.report = report;
        this.xReportType = xReportType;
        this.appCommandContext = appCommandContext;
        this.enableEreportDepartSale = enableEreportDepartSale;
        this.enableEreportItemSale = enableEreportItemSale;
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

        final ShopInfo shopInfo = app.getShopInfo();

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

        if (appCommandContext.getBlackstoneUser().isValid()) {
            printer.pair(context.getString(R.string.print_order_merchant_id_label), "" + appCommandContext.getBlackstoneUser().getMid());
            printer.drawLine();
        }

        drawSubtitle(printer, context);

        printer.drawLine();

    }

    private void drawSubtitle(IXReportPrinter printer, Context context) {
        if (ReportType.X_REPORT_CURRENT_SHIFT == xReportType) {
            printer.footer(context.getString(R.string.xreport_current_shift_subtitle));
            printer.pair(context.getString(R.string.zreprot_register_id_title_) + ":", registerID + (registerDescription == null || registerDescription.isEmpty() ? "" : " - " + registerDescription));
            printer.titledDate(context.getString(R.string.xreport_print_time), new Date());
        } else if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            printer.footer(context.getString(R.string.xreport_daily_subtitle));
            printer.pair(context.getString(R.string.zreprot_register_id_title_) + ":", registerID + (registerDescription == null || registerDescription.isEmpty() ? "" : " - " + registerDescription));
            printer.titledDate(context.getString(R.string.xreport_print_time), new Date());
        } else {
            printer.footer(context.getString(R.string.xreport_subtitle));
            printer.pair(context.getString(R.string.zreprot_register_id_title_) + ":", registerID + (registerDescription == null || registerDescription.isEmpty() ? "" : " - " + registerDescription));
        }
        SimpleDateFormat format = new SimpleDateFormat("");
        printer.pair(context.getString(R.string.reprot_start_date) + ":", getFromDate());
        printer.pair(context.getString(R.string.reprot_to_date) + ":", getToDate());
    }

    private void printBody(Context context, TcrApplication app, IXReportPrinter printer) {
        if (ReportType.X_REPORT_DAILY_SALES != xReportType) {
            printer.titledDate(context.getString(R.string.xreport_start_time), report.begin);
        }
        if (ReportType.X_REPORT_CURRENT_SHIFT != xReportType && ReportType.X_REPORT_DAILY_SALES != xReportType) {
            printer.titledDate(context.getString(R.string.xreport_end_time), report.end);
        }

        printer.emptyLine();

        printer.pair(context.getString(R.string.xreport_gross_sales), report.grossSales);
        printer.pair(context.getString(R.string.xreport_discounts), report.discounts, true);
        printer.pair(context.getString(R.string.xreport_returns), report.returns, true);
        printer.emptyLine();
        printer.drawLine();

        printer.boldPair(context.getString(R.string.xreport_net_sales), report.netSales, false);
        printer.pair(context.getString(R.string.xreport_gratuity), report.gratuity);
        printer.pair(context.getString(R.string.xreport_tax), report.tax);
        printer.pair(context.getString(R.string.xreport_transaction_fee), report.transactionFee);
        printer.emptyLine();
        printer.drawLine();

        printer.pair(context.getString(R.string.xreport_total_tender), report.totalTender);
        printer.pair(context.getString(R.string.xreport_cogs), report.cogs);
        printer.emptyLine();

        printer.pair(context.getString(R.string.xreport_gross_margin), report.grossMargin);
        printer.percent(report.grossMarginPercent);
        printer.emptyLine();

        if (enableEreportDepartSale) {
            printer.boldPair(context.getString(R.string.xreport_departments_sales), report.totalValue, false);
            Iterator iter = report.departsSales.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry item = (Map.Entry) iter.next();
                DepartsSale temp = (DepartsSale) item.getValue();
                if (temp.sales.compareTo(BigDecimal.ZERO) > 0)
                    printer.pair(temp.departTitle, temp.sales);
            }
            printer.emptyLine();
        }

        if(enableEreportItemSale){
            printer.boldPair(context.getString(R.string.xreport_items_sales), report.totalValue, false);
            for (SalesByItemsReportQuery.ReportItemInfo item: report.itemSales) {
                if (item.revenue.compareTo(BigDecimal.ZERO) > 0)
                    printer.pair(String.format(ITEM_STRING_FORMAT,item.description,item.qty.toString()), item.revenue);
            }
            printer.emptyLine();
        }

        printer.subtitle(context.getString(R.string.xreport_tender_summary), true);
        printer.pair(context.getString(R.string.xreport_credit_card), report.tenderCreditCard);
        printer.pair(context.getString(R.string.xreport_cash), report.tenderCash);
        printer.pair(context.getString(R.string.xreport_credit_receipt), report.tenderCreditReceipt);
        printer.pair(context.getString(R.string.xreport_offline_credit), report.tenderOfflineCredit);
        printer.pair(context.getString(R.string.xreport_check), report.tenderCheck);

        if (app.getShopInfo().acceptEbtCards) {
            printer.pair(context.getString(R.string.xreport_ebt_cash), report.tenderEbtCash);
            printer.pair(context.getString(R.string.xreport_ebt_foodstamp), report.tenderEbtFoodstamp);
        }
        if (app.getShopInfo().acceptDebitCards) {
            printer.pair(context.getString(R.string.xreport_debit), report.tenderDebit);
        }


        printer.emptyLine();

        if (report.cards != null && !report.cards.isEmpty()) {
            printer.subtitle(context.getString(R.string.xreport_credit_card_details), true);
            ArrayList<String> cardNames = new ArrayList<String>(report.cards.keySet());
            Collections.sort(cardNames);
            for (String cardName : cardNames) {
                printer.pair(cardName, report.cards.get(cardName));
            }
        }
        if (xReportType != ReportType.X_REPORT_DAILY_SALES) {

            printer.emptyLine();

            printer.subtitle(context.getString(R.string.xreport_drawer_details), true);
            printer.pair(context.getString(R.string.xreport_drawer_open_amount), report.openAmount);
            printer.pair(context.getString(R.string.xreport_drawer_cash_sale), report.cashSale);
            printer.pair(context.getString(R.string.xreport_drawer_safe_drops), report.safeDrops);
            printer.pair(context.getString(R.string.xreport_drawer_payouts), report.payOuts);
            printer.pair(context.getString(R.string.xreport_drawer_cash_back), report.cashBack);
            printer.pair(context.getString(R.string.xreport_drawer_balance), report.openAmount.add(report.cashSale).add(report.safeDrops).add(report.payOuts).add(report.cashBack));

            printer.emptyLine();

        }
        if (ReportType.X_REPORT_CURRENT_SHIFT != xReportType && ReportType.X_REPORT_DAILY_SALES != xReportType) {
            BigDecimal drawerDiffence = report.drawerDifference;
            if (drawerDiffence != null && drawerDiffence.compareTo(BigDecimal.ZERO) != 0) {
                printer.pair(context.getString(R.string.xreport_drawer_difference), drawerDiffence);
                printer.emptyLine();
            }
        }
//        if (report.end.compareTo(report.begin) > 0)
//            printer.pair(context.getString(R.string.xreport_drawer_difference), report.drawerDifference);
        printer.drawLine();
    }

    private void printFooter(TcrApplication app, IXReportPrinter printer) {
        ShopInfo shopInfo = app.getShopInfo();

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
