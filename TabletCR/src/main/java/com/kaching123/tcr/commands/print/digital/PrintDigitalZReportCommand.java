package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.processor.PrintZReportProcessor;
import com.kaching123.tcr.reports.ZReportQuery;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import static com.kaching123.tcr.print.printer.BasePosTextPrinter.dateFormat;

/**
 * Created by alboyko on 26.11.2015.
 */
public class PrintDigitalZReportCommand extends PublicGroundyTask {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_ZREPORT_TYPE = "ARG_ZREPORT_TYPE";

    private static final String EXTRA_ZREPORT = "EXTRA_ZREPORT";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";
    private ReportType zReportType;

    protected static final Uri URI_REGISTER = ShopProvider.getContentWithLimitUri(ShopStore.RegisterTable.URI_CONTENT, 1);

    @Override
    protected TaskResult doInBackground() {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);

        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);

        zReportType = (ReportType) getArgs().getSerializable(ARG_ZREPORT_TYPE);
        final DigitalXReportBuilder builder = new DigitalXReportBuilder();

        final ZReportInfo reportInfo;
        if (ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            reportInfo = ZReportQuery.loadDailySalesZReport(getContext(), registerID, fromDate, toDate);
        } else {
            reportInfo = ZReportQuery.loadZReport(getContext(), shiftGuid);
        }

        PrintZReportProcessor processor = new PrintZReportProcessor(reportInfo, zReportType, getAppCommandContext(), getApp().getShopPref().enableZReportTaxGroups().get());
        setDescriptionInfo(processor, registerID, fromDate, toDate);
        processor.print(getContext(), getApp(), builder);

        File file = new File(getContext().getExternalCacheDir(), getContext().getString(R.string.report_type_zreport) + ".html");
        try {
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.append(builder.build());
            bufferedWriter.close();
        } catch (IOException e) {
            return failed();
        }

        return succeeded().add(EXTRA_ZREPORT, Uri.fromFile(file));
    }

    public static void start(Context context, String shiftGuid, ReportType zReportType, BasePrintDigitalZReportCallback callback, long registerID, long fromDate, long toDate) {
        create(PrintDigitalZReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_ZREPORT_TYPE, zReportType)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BasePrintDigitalZReportCallback {

        @OnSuccess(PrintDigitalZReportCommand.class)
        public void onPrintSuccess(@Param(EXTRA_ZREPORT) Uri attachment) {
            onDigitalPrintSuccess(attachment);
        }

        @OnFailure(PrintDigitalZReportCommand.class)
        public void onPrintError() {
            onDigitalPrintError();
        }

        protected abstract void onDigitalPrintSuccess(Uri attachment);

        protected abstract void onDigitalPrintError();
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
