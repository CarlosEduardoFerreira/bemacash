package com.kaching123.tcr.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gdubina on 28.01.14.
 */
public class SalesTop10QtyQuery extends SalesByItemsReportQuery {

    @Override
    protected SalesReportHandler<ReportItemInfo> createHandler() {
        return new Top10ItemInfoHandler();
    }

    private class Top10ItemInfoHandler extends ReportItemInfoHandler {

        @Override
        public Collection<ReportItemInfo> getResult() {
            ArrayList<ReportItemInfo> result = new ArrayList<ReportItemInfo>(items.values());
            Collections.sort(result, new Comparator<ReportItemInfo>() {
                @Override
                public int compare(ReportItemInfo l, ReportItemInfo r) {
                    return -1 * (l.qty == null ? -1 : r.qty == null ? 1 : l.qty.compareTo(r.qty));
                }
            });
            return result.subList(0, Math.min(10, result.size()));
        }
    }
}
