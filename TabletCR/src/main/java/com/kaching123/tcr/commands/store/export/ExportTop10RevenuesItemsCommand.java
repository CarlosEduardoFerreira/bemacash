package com.kaching123.tcr.commands.store.export;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Created by gdubina on 29.01.14.
 */
class ExportTop10RevenuesItemsCommand extends ExportBaseTop10ItemsCommand {

    @Override
    protected String getFileName() {
        return orderType == null ? "Top_Items_Revenues_Export" : "Prepaid_Top_Items_Revenues_Export";
    }

    @Override
    protected Comparator<ReportItem> getComparator() {
        return new Comparator<ReportItem>() {
            @Override
            public int compare(ReportItem l, ReportItem r) {
                BigDecimal revenueL = null;
                if (l.grossSold != null && l.discountSold != null && l.taxSold != null && l.grossReturn != null && l.discountReturn != null && l.taxReturn != null) {
                    revenueL = l.grossSold.subtract(l.discountSold).add(l.taxSold)
                            .add(l.grossReturn).subtract(l.discountReturn).add(l.taxReturn);
                }

                BigDecimal revenueR = null;
                if (r.grossSold != null && r.discountSold != null && r.taxSold != null && r.grossReturn != null && r.discountReturn != null && r.taxReturn != null) {
                    revenueR = r.grossSold.subtract(r.discountSold).add(r.taxSold)
                            .add(r.grossReturn).subtract(r.discountReturn).add(r.taxReturn);
                }


                return -1 * (revenueL == null ? -1 : revenueR == null ? 1 : revenueL.compareTo(revenueR));
            }
        };
    }
}
