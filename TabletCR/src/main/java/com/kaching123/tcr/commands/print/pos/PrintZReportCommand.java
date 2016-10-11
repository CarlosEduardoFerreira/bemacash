package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IXReportPrinter;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.ReportsActivity;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.print.printer.PosXReportTextPrinter;
import com.kaching123.tcr.print.processor.PrintZReportProcessor;
import com.kaching123.tcr.reports.ZReportQuery;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

/**
 * Created by alboyko on 26.11.2015.
 */
public class PrintZReportCommand extends BasePrintCommand<IXReportPrinter> {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_ZREPORT_TYPE = "ARG_ZREPORT_TYPE";

    protected static final Uri URI_REGISTER = ShopProvider.getContentWithLimitUri(ShopStore.RegisterTable.URI_CONTENT, 1);

    @Override
    protected IXReportPrinter createTextPrinter() {
        return new PosXReportTextPrinter();
    }

    @Override
    protected void printBody(IXReportPrinter printerWrapper) {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);

        ReportType zReportType = (ReportType) getArgs().getSerializable(ARG_ZREPORT_TYPE);

        //final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        ZReportInfo reportInfo;
        if (ReportsActivity.ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            reportInfo = ZReportQuery.loadDailySalesZReport(getContext(), getAppCommandContext().getRegisterId());
        } else {
            reportInfo = ZReportQuery.loadZReport(getContext(), shiftGuid);
        }
        PrintZReportProcessor processor = new PrintZReportProcessor(reportInfo, zReportType, getAppCommandContext());
        processor.setRegisterDescription(getRegisterDescription());
        processor.print(getContext(), getApp(), printerWrapper);
    }

    public static void start(Context context, String shiftGuid, ReportsActivity.ReportType zReportType, boolean ignorePaperEnd, boolean searchByMac, BasePrintCallback callback) {
        create(PrintZReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_ZREPORT_TYPE, zReportType)
                .callback(callback).queueUsing(context);
    }

    private String getRegisterDescription() {
        Cursor c = ProviderAction.query(URI_REGISTER)
                .projection(
                        ShopStore.RegisterTable.DESCRIPTION
                )
                .where(ShopStore.RegisterTable.ID + "=?", ((TcrApplication) getContext().getApplicationContext()).getRegisterId())
                .perform(getContext());
        String description = null;
        if (c.moveToFirst()) {
            description = c.getString(0);
        }
        c.close();
        return description;
    }
}
