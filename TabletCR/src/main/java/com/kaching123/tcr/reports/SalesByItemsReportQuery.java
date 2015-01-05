package com.kaching123.tcr.reports;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.reports.SalesByItemsReportQuery.ReportItemInfo;
import com.kaching123.tcr.reports.XReportQuery.SaleItemInfo2;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by gdubina on 23.01.14.
 */
public class SalesByItemsReportQuery extends SalesBaseReportQuery<ReportItemInfo> {


    public SalesByItemsReportQuery() {
        super(true);
    }

    public SalesByItemsReportQuery(boolean sale) {
        super(sale);
    }

    @Override
    protected SalesReportHandler<ReportItemInfo> createHandler() {
        return new ReportItemInfoHandler();
    }

    protected static class ReportItemInfoHandler implements SalesReportHandler<ReportItemInfo> {

        protected final HashMap<String, ReportItemInfo> items = new HashMap<String, ReportItemInfo>();

        @Override
        public Collection<ReportItemInfo> getResult() {
            ArrayList<ReportItemInfo> result = new ArrayList<ReportItemInfo>(items.values());
            Collections.sort(result, new Comparator<ReportItemInfo>() {
                @Override
                public int compare(ReportItemInfo l, ReportItemInfo r) {
                    return l.description == null ? -1 : r.description == null ? 1 : l.description.compareTo(r.description);
                }
            });
            return result;
        }

        @Override
        public void splitItem(SaleItemInfo item) {

        }

        @Override
        public void handleItem(SaleItemInfo ii, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
            SaleItemInfo2 i = (SaleItemInfo2) ii;
            Logger.d("[SALE_BY_ITEMS] (%s) %s = %s * %s", i.itemGiud, i.description, i.qty, itemFinalPrice);
            BigDecimal revenue = CalculationUtil.getSubTotal(i.qty, itemFinalPrice);

            ReportItemInfo itemInfo = items.get(i.itemGiud);
            if (itemInfo == null) {
                items.put(i.itemGiud, itemInfo = new ReportItemInfo(i.itemGiud, i.description, i.ean, i.productCode, i.qty, revenue));
            } else {
                itemInfo.qty = itemInfo.qty.add(i.qty);
                itemInfo.revenue = itemInfo.revenue.add(revenue);
            }
        }
    }

    public static class ReportItemInfo implements IReportResult {
        public String itemGuid;
        public String description;
        public String ean;
        public String productCode;
        public BigDecimal revenue;
        public BigDecimal qty;

        public ReportItemInfo(String itemGuid, String description, String ean, String productCode, BigDecimal qty, BigDecimal revenue) {
            this.itemGuid = itemGuid;
            this.description = description;
            this.ean = ean;
            this.productCode = productCode;
            this.qty = qty;
            this.revenue = revenue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ReportItemInfo itemInfo = (ReportItemInfo) o;

            if (itemGuid != null ? !itemGuid.equals(itemInfo.itemGuid) : itemInfo.itemGuid != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return itemGuid != null ? itemGuid.hashCode() : 0;
        }
    }
}
