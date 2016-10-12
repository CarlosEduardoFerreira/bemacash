package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IXReportPrinter;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.printer.PosXReportTextMatrixPrinter;
import com.kaching123.tcr.print.printer.PosXReportTextPrinter;
import com.kaching123.tcr.print.processor.PrintXReportProcessor;
import com.kaching123.tcr.reports.XReportQuery;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

/**
 * Created by gdubina on 06/12/13.
 */
public class PrintXReportCommand extends BasePrintCommand<IXReportPrinter> {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_XREPORT_TYPE = "ARG_XREPORT_TYPE";
    private static final String ARG_XREPORT_ENABLE = "ARG_XREPORT_ENABLE";
    private static final String ARG_ITEM_XREPORT_ENABLE = "ARG_ITEM_XREPORT_ENABLE";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";
    protected static final Uri URI_REGISTER = ShopProvider.getContentWithLimitUri(ShopStore.RegisterTable.URI_CONTENT, 1);

    @Override
    protected IXReportPrinter createTextPrinter() {
        return getPrinter().printerType.equalsIgnoreCase("Thermal") ? new PosXReportTextPrinter(): new PosXReportTextMatrixPrinter();
    }

    @Override
    protected void printBody(IXReportPrinter printer) {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);

        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);

        ReportType xReportType = (ReportType) getArgs().getSerializable(ARG_XREPORT_TYPE);

        final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        XReportInfo reportInfo;
        if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            reportInfo = XReportQuery.loadDailySalesXReport(getContext(),registerID, fromDate, toDate);
        } else {
            reportInfo = XReportQuery.loadXReport(getContext(), shiftGuid);
        }
        PrintXReportProcessor processor = new PrintXReportProcessor(reportInfo, xReportType, getAppCommandContext(), getBooleanArg(ARG_XREPORT_ENABLE) ,getBooleanArg(ARG_ITEM_XREPORT_ENABLE));
        setDescriptionInfo(processor);
        processor.print(getContext(), getApp(), printer);
    }

    public static void start(Context context, String shiftGuid, ReportType xReportType, boolean ignorePaperEnd, boolean searchByMac, BasePrintCallback callback, boolean enableEreportDepartSale, boolean itemXreportSaleEnabled, long registerID, long fromDate, long toDate) {
        create(PrintXReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_XREPORT_TYPE, xReportType)
                .arg(ARG_XREPORT_ENABLE,enableEreportDepartSale)
                .arg(ARG_ITEM_XREPORT_ENABLE,itemXreportSaleEnabled)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }

    private void setDescriptionInfo(PrintXReportProcessor processor) {
        Cursor c = ProviderAction.query(URI_REGISTER)
                .projection(
                        ShopStore.RegisterTable.DESCRIPTION,
                        ShopStore.RegisterTable.TITLE
                )
                .where(ShopStore.RegisterTable.REGISTER_SERIAL + "=?", ((TcrApplication) getContext().getApplicationContext()).getRegisterSerial())
                .perform(getContext());
        String description = null;
        String title = null;
        if (c.moveToFirst()) {
            description = c.getString(0);
            title = c.getString(1);
        }
        processor.setRegisterDescription(description);
        processor.setRegisterID(title.equalsIgnoreCase("0") ? "ALL" : title);

        c.close();
    }
}
