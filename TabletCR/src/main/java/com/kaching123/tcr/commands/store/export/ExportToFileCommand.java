package com.kaching123.tcr.commands.store.export;

import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.kaching123.tcr.util.Util.toInt;

/**
 * Created by gdubina on 18/02/14.
 */
public abstract class ExportToFileCommand extends PublicGroundyTask {

    private static final DateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("MM_dd_yyyy");

    protected static final String ARG_FILENAME = "ARG_FILENAME";
    protected static final String RESULT_COUNT = "RESULT_COUNT";
    public static final String CSV = ".csv";

    @Override
    protected TaskResult doInBackground() {
        String folderName = getStringArg(ARG_FILENAME);

        String reportFileName = getFileName() + "_" + getAdditionalTitle();
        File file = new File(folderName, reportFileName + CSV);
        if(file.exists()){
            Logger.d("[EXPORT] file with name %s already exist", reportFileName);
            final String reportFileNameTemplate = reportFileName + "-";
            File dir = new File(folderName);
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(reportFileNameTemplate) && filename.endsWith(CSV);
                }
            });
            int maxNumber = 0;
            for(File f : files){
                String num = f.getName().substring(reportFileNameTemplate.length());

                int pos = num.lastIndexOf(".");
                num = pos > 0 ? num.substring(0, pos) : num;
                if(TextUtils.isDigitsOnly(num)){
                    maxNumber = Math.max(maxNumber, toInt(num, 0));
                }
            }
            maxNumber++;
            file = new File(folderName, reportFileName + "-" + maxNumber + CSV);
            Logger.d("[EXPORT] next name will be %s", file.getName());
        }

        final String[] header = getHeader();
        final CellProcessor[] processors = getColumns();

        ICsvListWriter writer = null;
        try {
            writer = new CsvListWriter(new FileWriter(file), CsvPreference.STANDARD_PREFERENCE);
            if(header != null){
                writer.writeHeader(header);
            }
            int count = writeBody(writer, processors);
            return succeeded().add(RESULT_COUNT, count);
        } catch (Exception e) {
            Logger.e("[EXPORT] exception", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Logger.e("[EXPORT] close file exception", e);
                }
            }
        }
        return failed();
    }

    protected String getAdditionalTitle(){
        long startTime = getLongArg(ReportArgs.ARG_START_TIME);
        long endTime = getLongArg(ReportArgs.ARG_END_TIME);
        if(startTime != 0 && endTime != 0){
            return "from_" +  FILE_NAME_DATE_FORMAT.format(new Date(startTime)) + "_to_" +  FILE_NAME_DATE_FORMAT.format(new Date(endTime));
        }
        return FILE_NAME_DATE_FORMAT.format(new Date());
    }

    protected abstract int writeBody(ICsvListWriter writer, CellProcessor[] processors) throws IOException;

    protected abstract String getFileName();
    protected abstract String[] getHeader();
    protected abstract CellProcessor[] getColumns();
}
