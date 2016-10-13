package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.pos.util.IXReportPrinter;
import com.kaching123.tcr.activity.ReportsActivity;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.print.printer.PosXReportTextPrinter;
import com.kaching123.tcr.print.processor.PrintZReportProcessor;
import com.kaching123.tcr.reports.ZReportQuery;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.Date;

import static com.kaching123.tcr.print.printer.BasePosTextPrinter.dateFormat;

/**
 * Created by alboyko on 26.11.2015.
 */
public class PrintZReportCommand extends BasePrintCommand<IXReportPrinter> {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_ZREPORT_TYPE = "ARG_ZREPORT_TYPE";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";

    protected static final Uri URI_REGISTER = ShopProvider.getContentWithLimitUri(ShopStore.RegisterTable.URI_CONTENT, 1);

    @Override
    protected IXReportPrinter createTextPrinter() {
        return new PosXReportTextPrinter();
    }

    @Override
    protected void printBody(IXReportPrinter printerWrapper) {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);

        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);
        ReportType zReportType = (ReportType) getArgs().getSerializable(ARG_ZREPORT_TYPE);

        //final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        ZReportInfo reportInfo;
        if (ReportsActivity.ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            reportInfo = ZReportQuery.loadDailySalesZReport(getContext(), registerID, fromDate, toDate);
        } else {
            reportInfo = ZReportQuery.loadZReport(getContext(), shiftGuid);
        }
        PrintZReportProcessor processor = new PrintZReportProcessor(reportInfo, zReportType, getAppCommandContext());
        setDescriptionInfo(processor, registerID, fromDate, toDate);
        processor.print(getContext(), getApp(), printerWrapper);
    }

    public static void start(Context context, String shiftGuid, ReportsActivity.ReportType zReportType, boolean ignorePaperEnd, boolean searchByMac, BasePrintCallback callback, long registerID, long fromDate, long toDate) {
        create(PrintZReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_ZREPORT_TYPE, zReportType)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }

    private void setDescriptionInfo(PrintZReportProcessor processor, long registerID, long fromDate, long toDate) {
        Cursor c = null;
        Query query = ProviderAction.query(URI_REGISTER)
                .projection(
                        ShopStore.RegisterTable.DESCRIPTION,
                        ShopStore.RegisterTable.TITLE
                );
        if (registerID == 0) {
            c = query.perform(getContext());
        } else {
            c = query.where(ShopStore.RegisterTable.ID + "=?", registerID)
                    .perform(getContext());

        }

        String description = null;
        String title = null;
        if (c.moveToFirst()) {
            description = c.getString(0);
            title = c.getString(1);
        }
        processor.setRegisterDescription(description);
        processor.setRegisterID(registerID == 0 ? "ALL" : title);
        processor.setFromDate(dateFormat.format(new Date(fromDate)));
        processor.setToDate(dateFormat.format(new Date(toDate)));

        c.close();
    }
}
