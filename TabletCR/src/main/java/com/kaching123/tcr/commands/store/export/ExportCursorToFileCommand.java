package com.kaching123.tcr.commands.store.export;

import android.database.Cursor;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvListWriter;

import java.io.IOException;
import java.util.List;

/**
 * Created by gdubina on 29.01.14.
 */
public abstract class ExportCursorToFileCommand extends ExportToFileCommand {

    @Override
    protected int writeBody(ICsvListWriter writer, CellProcessor[] processors) throws IOException {
        Cursor cursor = query();
        int count = cursor.getCount();
        try {
            while (cursor.moveToNext()) {
                List<Object> row = readRow(cursor);
                writer.write(row, processors);
            }
        } finally {
            cursor.close();
        }
        return count;
    }

    protected abstract Cursor query();

    protected abstract List<Object> readRow(Cursor cursor);

}
