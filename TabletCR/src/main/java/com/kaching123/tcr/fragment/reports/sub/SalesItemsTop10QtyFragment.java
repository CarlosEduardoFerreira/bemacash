package com.kaching123.tcr.fragment.reports.sub;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.reports.SalesByItemsReportQuery;
import com.kaching123.tcr.reports.SalesByItemsReportQuery.ReportItemInfo;
import com.kaching123.tcr.reports.SalesTop10QtyQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gdubina on 28.01.14.
 */
@EFragment(R.layout.reports_sales_by_item_list_qty_fragment)
public class SalesItemsTop10QtyFragment extends SalesByItemsFragment {

    @Override
    protected SalesByItemsReportQuery createQuery() {
        return new SalesTop10QtyQuery();
    }

    @Override
    protected BigDecimal calcTotalValue(ArrayList<ReportItemInfo> result) {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (ReportItemInfo i : result) {
            totalValue = totalValue.add(i.qty);
        }
        return totalValue;
    }

    @Override
    protected List<ReportItemInfo> getResultList(ArrayList<ReportItemInfo> result) {
        totalValue = calcTotalValue(result);
        Collections.sort(result, new Comparator<ReportItemInfo>() {
            @Override
            public int compare(ReportItemInfo l, ReportItemInfo r) {
                return -1 * (l.qty == null ? -1 : r.qty == null ? 1 : l.qty.compareTo(r.qty));
            }
        });
        return result;
    }

    public static SalesItemsTop10QtyFragment instance(long startTime, long endTime, long resisterId) {
        return SalesItemsTop10QtyFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).orderType(OrderType.SALE).build();
    }
}
