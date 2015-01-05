package com.kaching123.tcr.fragment.reports.sub;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.print.SaleReportsProcessor;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.reports.SalesByItemsReportQuery;
import com.kaching123.tcr.reports.SalesTop10RevenuesQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 28.01.14.
 */
@EFragment(R.layout.reports_sales_by_item_list_fragment)
public class SalesItemsTop10RevenuesFragment extends SalesByItemsFragment {

    @Override
    protected SalesByItemsReportQuery createQuery() {
        return new SalesTop10RevenuesQuery();
    }

    public static SalesItemsTop10RevenuesFragment instance(long startTime, long endTime, long resisterId, OrderType orderType) {
        return SalesItemsTop10RevenuesFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).orderType(orderType).build();
    }

    @Override
    protected List<SalesByItemsReportQuery.ReportItemInfo> getResultList(ArrayList<SalesByItemsReportQuery.ReportItemInfo> result) {
        final ArrayList<SalesByItemsReportQuery.ReportItemInfo> top10List = SaleReportsProcessor.getTop10SortedByRevenue(result);
        totalValue = calcTotalValue(top10List);
        return top10List;
    }
}
