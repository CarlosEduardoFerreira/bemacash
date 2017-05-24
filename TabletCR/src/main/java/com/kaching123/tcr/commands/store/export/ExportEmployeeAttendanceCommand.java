package com.kaching123.tcr.commands.store.export;

import com.kaching123.tcr.reports.ClockInOutReportQuery;
import com.kaching123.tcr.reports.ClockInOutReportQuery.EmployeeInfo;
import com.kaching123.tcr.reports.ClockInOutReportQuery.TimeInfo;
import com.kaching123.tcr.util.DateUtils;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvListWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by vkompaniets on 27.05.2014.
 */
public class ExportEmployeeAttendanceCommand extends ExportToFileCommand {

    @Override
    protected int writeBody(ICsvListWriter writer, CellProcessor[] processors) throws IOException {

        long start = getLongArg(ReportArgs.ARG_START_TIME);
        long end = getLongArg(ReportArgs.ARG_END_TIME);
        String employeeGuid = getStringArg(ReportArgs.ARG_EMPLOYEE_GUID);

        int n = 0;
        Collection<EmployeeInfo> items = ClockInOutReportQuery.getItemsClockInOutReport(getContext(), start, end, employeeGuid);
        for (EmployeeInfo info : items){
            for (TimeInfo t : info.times){
                writer.write(readRow(info.name, t.totalBreak, t.clockIn, t.clockOut, t.getDiff()));
                n++;
            }
        }

        return n;
    }

    private List<String> readRow(String name, BigDecimal totalBreaks, Date in, Date out, BigDecimal diff){
        final boolean sameDay = DateUtils.isSameDay(in, out);
        ArrayList<String> row = new ArrayList<String>(5);
        row.add(name);
        row.add(DateUtils.dateOnlyFormat(in));
        row.add(totalBreaks == null ? null : DateUtils.formatMins(totalBreaks));
        row.add(DateUtils.timeOnlyAttendanceFormat(in));
        row.add(sameDay ? DateUtils.timeOnlyAttendanceFormat(out) : DateUtils.formatFullAttendance(out));
        row.add(out == null ? null : DateUtils.formatMins(diff));
        return row;
    }

    @Override
    protected String getFileName() {
        return "Employee_Attendance_Report";
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Employee",
                "Date",
                "Total Break Time",
                "Clock-in",
                "Clock-out",
                "Shift"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return new CellProcessor[]{
                null, //"Employee",
                null, //"Date",
                null, //"Total Break Time",
                null, //"Clock-in",
                null, //"Clock-out",
                null  //"Shift"
        };
    }

    /*public static void start(Context context, long startTime, long endTime, String employeeGuid){
        create(ExportEmployeeAttendanceCommand.class)
                .arg(ReportArgs.ARG_START_TIME, startTime)
                .arg(ReportArgs.ARG_END_TIME, endTime)
                .arg(ReportArgs.ARG_EMPLOYEE_GUID, employeeGuid)
                .queueUsing(context);
    }*/
}
