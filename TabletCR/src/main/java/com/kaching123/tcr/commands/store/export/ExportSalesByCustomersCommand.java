package com.kaching123.tcr.commands.store.export;

import com.kaching123.tcr.function.SalesByCustomersWrapFunction;
import com.kaching123.tcr.model.SalesByCustomerModel;
import com.kaching123.tcr.reports.SalesByCustomersReportQuery;
import com.telly.groundy.TaskResult;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvListWriter;

import java.io.IOException;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by pkabakov on 18.02.14.
 */
public class ExportSalesByCustomersCommand extends ExportToFileCommand {

    private long startTime;
    private long endTime;

    @Override
    protected TaskResult doInBackground() {
        startTime = getLongArg(ReportArgs.ARG_START_TIME);
        endTime = getLongArg(ReportArgs.ARG_END_TIME);
        return super.doInBackground();
    }

    @Override
    protected int writeBody(ICsvListWriter writer, CellProcessor[] processors) throws IOException {
        List<SalesByCustomerModel> salesByCustomerList = _wrap(
                SalesByCustomersReportQuery.syncQuery(startTime, endTime).perform(getContext()),
                new SalesByCustomersWrapFunction()
        );
        int count = salesByCustomerList.size();
        for (SalesByCustomerModel model : salesByCustomerList) {
            writer.write(model.customerName, model.customerPhone, model.customerEmail, _decimal(model.totalAmount));
        }

        return count;
    }

    @Override
    protected String getFileName() {
        return "Sales_by_Customers_Export";
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Customer Name",
                "Customer Phone",
                "Customer Email",
                "Total Amount"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return null;
    }

}
