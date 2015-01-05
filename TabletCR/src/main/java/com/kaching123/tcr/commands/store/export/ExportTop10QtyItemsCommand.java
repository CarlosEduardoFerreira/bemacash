package com.kaching123.tcr.commands.store.export;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Created by gdubina on 29.01.14.
 */
class ExportTop10QtyItemsCommand extends ExportBaseTop10ItemsCommand {

    @Override
    protected String getFileName() {
        return "Top_Items_Qty_Export";
    }

    @Override
    protected Comparator<ReportItem> getComparator() {
        return new Comparator<ReportItem>() {
            @Override
            public int compare(ReportItem l, ReportItem r) {
                BigDecimal qtyL = l.qtySold == null ? null : l.qtyReturn == null ? null : l.qtySold.add(l.qtyReturn);
                BigDecimal qtyR = r.qtySold == null ? null : r.qtyReturn == null ? null : r.qtySold.add(r.qtyReturn);
                return -1 * (qtyL == null ? -1 : qtyR == null ? 1 : qtyL.compareTo(qtyR));
            }
        };
    }
}
