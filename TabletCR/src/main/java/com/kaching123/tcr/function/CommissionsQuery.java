package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by pkabakov on 09.07.2014.
 */
public class CommissionsQuery {

    private static final Uri SALE_ITEM_COMMISSIONS_URI = ShopProvider.getContentUri(ShopStore.SaleItemCommissionsView.URI_CONTENT);
    private static final Uri EMPLOYEE_URI = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    public static Map<String, BigDecimal> loadSync(Context context, String[] employeeGuids, ArrayList<SaleOrderItemModel> saleItems, BigDecimal defaultStoreCommission) {
        List<SaleItemCommissionsModel> saleItemCommissions = loadItemCommissions(context, saleItems);
        if (saleItemCommissions == null || saleItemCommissions.isEmpty())
            return new HashMap<String, BigDecimal>();

        List<EmployeeCommissionsModel> employeeCommissions = loadEmployeeCommissions(context, employeeGuids);

        return calculateCommission(saleItemCommissions, employeeCommissions, defaultStoreCommission);
    }

    private static Map<String, BigDecimal> calculateCommission(List<SaleItemCommissionsModel> saleItemCommissions, List<EmployeeCommissionsModel> employeeCommissions, BigDecimal storeDefaultCommission) {
        Map<String, BigDecimal> employeeCommissionAmounts = new HashMap<String, BigDecimal>();
        int employeeCount = employeeCommissions.size();
        for (SaleItemCommissionsModel item: saleItemCommissions) {
            BigDecimal itemCommissionPrice = item.saleItem.finalGrossPrice.subtract(item.saleItem.finalDiscount);
            BigDecimal itemSubCommissionPrice = CalculationUtil.getSubTotal(item.saleItem.qty, itemCommissionPrice);
            BigDecimal baseItemCommissionAmount = CalculationUtil.splitAmount(itemSubCommissionPrice, employeeCount);

            for (EmployeeCommissionsModel employee: employeeCommissions) {
                BigDecimal employeeCommissionAmount = employeeCommissionAmounts.get(employee.guid);
                if (employeeCommissionAmount == null) {
                    employeeCommissionAmount = BigDecimal.ZERO;
                    employeeCommissionAmounts.put(employee.guid, employeeCommissionAmount);
                }

                BigDecimal itemCommission = item.commission;
                if (item.commission == null || item.commission.compareTo(BigDecimal.ZERO) == 0) {
                    itemCommission = (employee.commission == null || employee.commission.compareTo(BigDecimal.ZERO) == 0) ? storeDefaultCommission : employee.commission;
                } else if (employee.commission != null && employee.commission.compareTo(BigDecimal.ZERO) != 0) {
                    itemCommission = (item.commission.compareTo(employee.commission) < 0) ? item.commission : employee.commission;
                }
                if (itemCommission == null || itemCommission.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                BigDecimal itemCommissionAmount = CalculationUtil.getItemDiscountValue(baseItemCommissionAmount, itemCommission, DiscountType.PERCENT);

                employeeCommissionAmount = employeeCommissionAmount.add(itemCommissionAmount);
                employeeCommissionAmounts.put(employee.guid, employeeCommissionAmount);
            }
        }
        return employeeCommissionAmounts;
    }

    private static List<EmployeeCommissionsModel> loadEmployeeCommissions(Context context, String[] employeeGuids) {
        return _wrap(ProviderAction
                        .query(EMPLOYEE_URI)
                        .projection(ShopStore.EmployeeTable.GUID, ShopStore.EmployeeTable.COMMISSION)
                        .where(ShopStore.EmployeeTable.GUID + " IN (" + getSelectionPlaceholders(employeeGuids.length) + ")", employeeGuids)
                        .perform(context),
                new ListConverterFunction<List<EmployeeCommissionsModel>>() {
                    @Override
                    public List<EmployeeCommissionsModel> apply(Cursor cursor) {
                        super.apply(cursor);

                        List<EmployeeCommissionsModel> models = new ArrayList<EmployeeCommissionsModel>();
                        while (cursor.moveToNext()) {
                            String guid = cursor.getString(indexHolder.get(ShopStore.EmployeeTable.GUID));
                            BigDecimal commission = _decimal(cursor, indexHolder.get(ShopStore.EmployeeTable.COMMISSION));
                            models.add(new EmployeeCommissionsModel(guid, commission));
                        }
                        return models;
                    }
                }
        );
    }

    private static String getSelectionPlaceholders(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (builder.length() > 0)
                builder.append(',');
            builder.append('?');
        }
        return builder.toString();
    }

    //TODO: improve
    private static List<SaleItemCommissionsModel> loadItemCommissions(Context context, ArrayList<SaleOrderItemModel> saleItems) {
        assert context != null;
        assert saleItems != null;

        HashSet<String> itemGuids = new HashSet<String>();
        for (SaleOrderItemModel saleItem: saleItems) {
            itemGuids.add(saleItem.itemGuid);
        }

        List<ItemCommissionsModel> itemCommissions = _wrap(ProviderAction
                        .query(SALE_ITEM_COMMISSIONS_URI)
                        .whereIn(ShopSchema2.SaleItemCommissionsView2.ItemTable.GUID, itemGuids)
                        .where(ShopSchema2.SaleItemCommissionsView2.ItemTable.ELIGIBLE_FOR_COMMISSION + " = 1 AND " + ShopSchema2.SaleItemCommissionsView2.CategoryTable.ELIGIBLE_FOR_COMMISSION + " = 1")
                        .perform(context),
                new SaleItemCommissionsFunction());

        ArrayList<SaleItemCommissionsModel> saleItemCommissions = new ArrayList<SaleItemCommissionsModel>();
        for (SaleOrderItemModel saleItem: saleItems) {
            for (ItemCommissionsModel itemCommission: itemCommissions) {
                if (saleItem.itemGuid.equals(itemCommission.itemGuid)) {
                    saleItemCommissions.add(new SaleItemCommissionsModel(saleItem, itemCommission.commission));
                    break;
                }
            }
        }

        return saleItemCommissions;
    }

    public static class SaleItemCommissionsFunction extends ListConverterFunction<List<ItemCommissionsModel>> {

        @Override
        public List<ItemCommissionsModel> apply(Cursor cursor) {
            super.apply(cursor);

            List<ItemCommissionsModel> itemCommissionsList = new ArrayList<ItemCommissionsModel>();
            while (cursor.moveToNext()) {
                BigDecimal itemCommission = _decimal(cursor, indexHolder.get(ShopSchema2.SaleItemCommissionsView2.ItemTable.COMMISSION));
                BigDecimal categoryCommission = _decimal(cursor, indexHolder.get(ShopSchema2.SaleItemCommissionsView2.CategoryTable.COMMISSION));

                BigDecimal commission = (itemCommission == null || itemCommission.compareTo(BigDecimal.ZERO) == 0) ? categoryCommission : itemCommission;

                ItemCommissionsModel itemCommissionsModel = new ItemCommissionsModel(
                        cursor.getString(indexHolder.get(ShopSchema2.SaleItemCommissionsView2.ItemTable.GUID)),
                        commission
                );

                itemCommissionsList.add(itemCommissionsModel);
            }

            return itemCommissionsList;
        }

    }

    public static class ItemCommissionsModel {

        public final String itemGuid;
        public BigDecimal commission;

        public ItemCommissionsModel(String itemGuid, BigDecimal commission) {
            this.itemGuid = itemGuid;
            this.commission = commission;
        }
    }

    public static class SaleItemCommissionsModel {

        public final SaleOrderItemModel saleItem;
        public BigDecimal commission;

        public SaleItemCommissionsModel(SaleOrderItemModel saleItem, BigDecimal commission) {
            this.saleItem = saleItem;
            this.commission = commission;
        }
    }

    public static class EmployeeCommissionsModel {

        public String guid;
        public BigDecimal commission;

        public EmployeeCommissionsModel(String guid, BigDecimal commission) {
            this.guid = guid;
            this.commission = commission;
        }
    }

}
