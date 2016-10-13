package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.processor.PrintXReportProcessor;
import com.kaching123.tcr.reports.XReportQuery;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.Date;

import static com.kaching123.tcr.print.printer.BasePosTextPrinter.dateFormat;

/**
 * Created by gdubina on 14.03.14.
 */
public class SendDigitalXReportCommand extends BaseSendEmailCommand {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_XREPORT_TYPE = "ARG_XREPORT_TYPE";

    private static final String ARG_XREPORT_ENABLE = "ARG_XREPORT_ENABLE";

    private static final String ARG_ITEM_XREPORT_ENABLE = "ARG_ITEM_XREPORT_ENABLE";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";
    protected static final Uri URI_REGISTER = ShopProvider.getContentWithLimitUri(ShopStore.RegisterTable.URI_CONTENT, 1);

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String email = getApp().getShopInfo().ownerEmail;
        if (TextUtils.isEmpty(email))
            return Response.responseFailed();

        String shiftGuid = getStringArg(ARG_SHIFT_GUID);
        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);
        final ReportType xReportType = (ReportType) getArgs().getSerializable(ARG_XREPORT_TYPE);

        final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        XReportInfo reportInfo;
        if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            reportInfo = XReportQuery.loadDailySalesXReport(getContext(),registerID, fromDate, toDate);
        } else {
            reportInfo = XReportQuery.loadXReport(getContext(), shiftGuid);
        }
        PrintXReportProcessor processor = new PrintXReportProcessor(reportInfo, xReportType, getAppCommandContext(), getBooleanArg(ARG_XREPORT_ENABLE), getBooleanArg(ARG_ITEM_XREPORT_ENABLE));
        setDescriptionInfo(processor, registerID, fromDate, toDate);
        processor.print(getContext(), getApp(), builder);

        String html = builder.build();

        String subject = getSubject(xReportType);

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    private String getSubject(ReportType xReportType) {
        int subjId = (ReportType.X_REPORT_DAILY_SALES == xReportType || ReportType.X_REPORT_CURRENT_SHIFT == xReportType) ?
                R.string.report_type_x_report : R.string.report_type_xreport ;
        return getContext().getString(subjId);
    }


    public static void start(Context context, String shiftGuid, ReportType xReportType, boolean enableEreportDepartSale, boolean itemXreportSaleEnabled, SendPrintDigitalXReportCallback callback, long registerID, long fromDate, long toDate) {
        create(SendDigitalXReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_XREPORT_TYPE, xReportType)
                .arg(ARG_XREPORT_ENABLE, enableEreportDepartSale)
                .arg(ARG_ITEM_XREPORT_ENABLE,itemXreportSaleEnabled)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }

    public static abstract class SendPrintDigitalXReportCallback {

        @OnSuccess(SendDigitalXReportCommand.class)
        public final void onPrintSuccess() {
            handleSuccess();
        }

        @OnFailure(SendDigitalXReportCommand.class)
        public final void onPrintError() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();
    }

    private void setDescriptionInfo(PrintXReportProcessor processor, long registerID, long fromDate, long toDate) {
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
