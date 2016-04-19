package com.kaching123.tcr.reports;

import com.google.common.collect.ImmutableSortedSet;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery.DepartmentStatistics;
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
public class SalesByDepartmentsReportQuery extends SalesBaseReportQuery<DepartmentStatistics> {
    @Override
    protected SalesReportHandler<DepartmentStatistics> createHandler() {
        return new DepartmentStatisticsHandler();
    }

    private class DepartmentStatisticsHandler implements SalesReportHandler<DepartmentStatistics> {

        final HashMap<String, DepartmentStatistics> items = new HashMap<String, DepartmentStatistics>();

        @Override
        public Collection<DepartmentStatistics> getResult() {
            ArrayList<DepartmentStatistics> result = new ArrayList<DepartmentStatistics>(items.values());
            Collections.sort(result, new Comparator<DepartmentStatistics>() {
                @Override
                public int compare(DepartmentStatistics l, DepartmentStatistics r) {
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
            Logger.d("[SALE_BY_DEPS] [%s] (%s) %s = %s * %s", i.departmentGuid, i.itemGiud, i.description, i.qty, itemFinalPrice);
            BigDecimal revenue = CalculationUtil.getSubTotal(i.qty, itemFinalPrice);

            DepartmentStatistics depInfo = items.get(i.departmentGuid == null ? "Prepaid" : i.departmentGuid);
            if (depInfo == null) {
                items.put(i.departmentGuid == null ? "Prepaid" : i.departmentGuid, depInfo = new DepartmentStatistics(i.departmentTitle == null ? "Prepaid" : i.departmentTitle, BigDecimal.ZERO));
            }

            CategoryStat catStat = depInfo.categories.get(i.categoryGuid == null? "Prepaid" : i.categoryGuid);
            if (catStat == null) {
                depInfo.categories.put(i.categoryGuid == null ? "Prepaid" : i.categoryGuid, catStat = new CategoryStat(i.categoryTitle == null ? "Prepaid" : i.categoryTitle, BigDecimal.ZERO));
            }

            catStat.revenue = catStat.revenue.add(revenue);
            depInfo.revenue = depInfo.revenue.add(revenue);
        }
    }

    public static class DepartmentStatistics extends CategoryStat implements IReportResult {
        private HashMap<String, CategoryStat> categories = new HashMap<String, CategoryStat>();

        public DepartmentStatistics(String description, BigDecimal revenue) {
            super(description, revenue);
        }

        public Collection<CategoryStat> getSortedList() {
            return ImmutableSortedSet.orderedBy(new Comparator<CategoryStat>() {
                @Override
                public int compare(CategoryStat l, CategoryStat r) {
                    return l.description == null ? -1 : r.description == null ? 1 : l.description.compareTo(r.description);
                }
            }).addAll(categories.values().iterator()).build();
        }

        public void reset() {
            categories = null;
        }
    }

    public static class CategoryStat {
        public String description;
        public BigDecimal revenue;

        public CategoryStat(String description, BigDecimal revenue) {
            this.description = description;
            this.revenue = revenue;
        }
    }
}
