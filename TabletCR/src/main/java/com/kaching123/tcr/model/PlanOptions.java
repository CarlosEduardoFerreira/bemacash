package com.kaching123.tcr.model;

import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.pref.ShopPref_;

/**
 * Created by idyuzheva on 30.11.2015.
 */
public class PlanOptions {

    @SerializedName("items_modifiers")
    public PlanOption itemModifierSettings;

    @SerializedName("store_customer")
    public PlanOption customerSettings;

    @SerializedName("store_employees")
    public PlanOption employeeLimit;

    @SerializedName("employee_permissions_access")
    public PlanOption employeeCustomPermission;

    @SerializedName("export_inventory_allowed")
    public PlanOption exportInventory;

    @SerializedName("scales_connection_allowed")
    public PlanOption scalesConnection;

    @SerializedName("merchant_store_serializable_items")
    public PlanOption serializableSettings; //web parameter is already synced through shop settings

    @SerializedName("allow_z_report")
    public PlanOption zReportSettings; //ignore, web only

    @SerializedName("inventory_limited")
    public PlanOption isInventoryLimited;

    @SerializedName("inventory_limited_amount")
    public PlanOption inventoryLimit;

    @SerializedName("inventory_tracking_allowed")
    public PlanOption stockTracking;

    @SerializedName("allow_z_report_filters")
    public PlanOption zReportFilters;

    @SerializedName("purchase_orders_access")
    public PlanOption purchaseOrdersAccess;

    @SerializedName("just_z_report_allowed")
    public PlanOption justZReport;

    @SerializedName("x_report")
    public PlanOption xReport;

    @SerializedName("sales")
    public PlanOption sales;

    @SerializedName("sales_by_item")
    public PlanOption salesByItem;

    @SerializedName("prepaid_items_sales")
    public PlanOption prepaidItemsSales;

    @SerializedName("topten_sold_revenues")
    public PlanOption toptenSoldRevenues;

    @SerializedName("topten_prepaid_revenues")
    public PlanOption toptenPrepaidRevenues;

    @SerializedName("topten_sold_items")
    public PlanOption toptenSoldItems;

    @SerializedName("sales_by_department")
    public PlanOption salesByDepartment;

    @SerializedName("salesbytendertype")
    public PlanOption salesByTenderType;

    @SerializedName("sales_by_customers")
    public PlanOption salesByCustomers;

    @SerializedName("sales_returns")
    public PlanOption salesReturns;

    @SerializedName("returned_items")
    public PlanOption returnedItems;

    @SerializedName("restock_report")
    public PlanOption restockReport;

    @SerializedName("inventory_value")
    public PlanOption inventoryValue;

    @SerializedName("item_quantity_log")
    public PlanOption itemQuantityLog;

    @SerializedName("inventory_status")
    public PlanOption inventoryStatus;

    @SerializedName("employee_attendance")
    public PlanOption employeeAttendance;

    @SerializedName("employee_payroll")
    public PlanOption employeePayroll;

    @SerializedName("employee_shift")
    public PlanOption employeeShift;

    @SerializedName("employee_commissions")
    public PlanOption employeeCommissions;

    @SerializedName("purchase_orders")
    public PlanOption purchaseOrders;

    @SerializedName("items_purchase_history")
    public PlanOption itemsPurchaseHistory;

    @SerializedName("re_stock")
    public PlanOption reStock;

    @SerializedName("shift_gratuity")
    public PlanOption shiftGratuity;

    @SerializedName("summary_gratuity")
    public PlanOption summaryGratuity;

    @SerializedName("drops_and_payouts")
    public PlanOption dropsAndPayouts;

    @SerializedName("sales_summary_report")
    public PlanOption salesSummaryReport;

    public PlanOptions() {
    }

    public void save() {
        ShopPref_ prefs = TcrApplication.get().getShopPref();
        prefs.edit()
                .modifiersAvailable().put(itemModifierSettings.isAvailable())
                .customerEditingAvailable().put(customerSettings.isAvailable())
                .employeeLimit().put(employeeLimit.getValue())
                .exportInventory().put(exportInventory.isAvailable())
                .employeeCustomPermitissions().put(employeeCustomPermission.isAvailable())
                .scalesConnection().put(scalesConnection.isAvailable())
                .isInventoryLimited().put(isInventoryLimited.isAvailable())
                .inventoryLimit().put(inventoryLimit.getValue())
                .stockTracking().put(stockTracking.isAvailable())
                .isZReportFiltersAllowed().put(zReportFilters.isAvailable())
                .isPurchaseOrdersAviable().put(purchaseOrdersAccess.isAvailable())
                .isJustZReportAllowed().put(justZReport.isAvailable())
                .isXReportAllowed().put(xReport.isAvailable())
                .isSalesReportAllowed().put(sales.isAvailable())
                .isSalesByItemReportAllowed().put(salesByItem.isAvailable())
                .isPrepaidItemsSalesReportAllowed().put(prepaidItemsSales.isAvailable())
                .isToptenSoldRevenuesReportAllowed().put(toptenSoldRevenues.isAvailable())
                .isToptenPrepaidRevenuesReportAllowed().put(toptenPrepaidRevenues.isAvailable())
                .isToptenSoldItemsReportAllowed().put(toptenSoldItems.isAvailable())
                .isSalesByDepartmentReportAllowed().put(salesByDepartment.isAvailable())
                .isSalesByTenderTypeReportAllowed().put(salesByTenderType.isAvailable())
                .isSalesByCustomersReportAllowed().put(salesByCustomers.isAvailable())
                .isSalesReturnsReportAllowed().put(salesReturns.isAvailable())
                .isReturnedItemsReportAllowed().put(returnedItems.isAvailable())
                .isRestockReportReportAllowed().put(restockReport.isAvailable())
                .isInventoryValueReportAllowed().put(inventoryValue.isAvailable())
                .isItemQuantityLogReportAllowed().put(itemQuantityLog.isAvailable())
                .isInventoryStatusReportAllowed().put(inventoryStatus.isAvailable())
                .isEmployeeAttendanceReportAllowed().put(employeeAttendance.isAvailable())
                .isEmployeePayrollReportAllowed().put(employeePayroll.isAvailable())
                .isEmployeeShiftReportAllowed().put(employeeShift.isAvailable())
                .isEmployeeCommissionsReportAllowed().put(employeeCommissions.isAvailable())
                .isPurchaseOrdersReportAllowed().put(purchaseOrders.isAvailable())
                .isItemsPurchaseHistoryReportAllowed().put(itemsPurchaseHistory.isAvailable())
                .isReStockReportAllowed().put(reStock.isAvailable())
                .isShiftGratuityReportAllowed().put(shiftGratuity.isAvailable())
                .isSummaryGratuityReportAllowed().put(summaryGratuity.isAvailable())

                .isDropsAndPayoutsReportAllowed().put(dropsAndPayouts.isAvailable())
                .isSalesSummaryReportReportAllowed().put(salesSummaryReport.isAvailable())
                .apply();
    }

    private static boolean isAllowed(RestrictionType restrictionType) {
        TcrApplication app = TcrApplication.get();
        if (app.isFreemium()) {
            switch (restrictionType) {
                case MODIFIERS:
                    return app.getShopPref().modifiersAvailable().get();
                case CUSTOMER:
                    return app.getShopPref().customerEditingAvailable().get();
                case EXPORT_INVENTORY:
                    return app.getShopPref().exportInventory().get();
                case EMPLOYEE_CUSTOM_PERMISSIONS:
                    return app.getShopPref().employeeCustomPermitissions().get();
                case SCALE_CONNECTION:
                    return app.getShopPref().scalesConnection().get();
                case STOCK_TRACKING:
                    return app.getShopPref().stockTracking().get();
                case Z_REPORT_FILTERS:
                    return app.getShopPref().isZReportFiltersAllowed().get();
                case PURCHASE_ORDERS_ACCESS:
                    return app.getShopPref().isPurchaseOrdersAviable().get();
                case JUST_Z_REPORT:
                    return app.getShopPref().isJustZReportAllowed().get();
                case X_REPORT:
                    return app.getShopPref().isXReportAllowed().get();
                case SALES:
                    return app.getShopPref().isSalesReportAllowed().get();
                case SALES_BY_ITEM:
                    return app.getShopPref().isSalesByItemReportAllowed().get();
                case PREPAID_ITEMS_SALES:
                    return app.getShopPref().isPrepaidItemsSalesReportAllowed().get();
                case TOPTEN_SOLD_REVENUES:
                    return app.getShopPref().isToptenSoldRevenuesReportAllowed().get();
                case TOPTEN_PREPAID_REVENUES:
                    return app.getShopPref().isToptenPrepaidRevenuesReportAllowed().get();
                case TOPTEN_SOLD_ITEMS:
                    return app.getShopPref().isToptenSoldItemsReportAllowed().get();
                case SALES_BY_DEPARTMENT:
                    return app.getShopPref().isSalesByDepartmentReportAllowed().get();
                case SALES_BY_TENDER_TYPE:
                    return app.getShopPref().isSalesByTenderTypeReportAllowed().get();
                case SALES_BY_CUSTOMERS:
                    return app.getShopPref().isSalesByCustomersReportAllowed().get();
                case SALES_RETURNS:
                    return app.getShopPref().isSalesReturnsReportAllowed().get();
                case RETURNED_ITEMS:
                    return app.getShopPref().isReturnedItemsReportAllowed().get();
                case RESTOCK_REPORT:
                    return app.getShopPref().isRestockReportReportAllowed().get();
                case INVENTORY_VALUE:
                    return app.getShopPref().isInventoryValueReportAllowed().get();
                case ITEM_QUANTITY_LOG:
                    return app.getShopPref().isItemQuantityLogReportAllowed().get();
                case INVENTORY_STATUS:
                    return app.getShopPref().isInventoryStatusReportAllowed().get();
                case EMPLOYEE_ATTENDANCE:
                    return app.getShopPref().isEmployeeAttendanceReportAllowed().get();
                case EMPLOYEE_PAYROLL:
                    return app.getShopPref().isEmployeePayrollReportAllowed().get();
                case EMPLOYEE_SHIFT:
                    return app.getShopPref().isEmployeeShiftReportAllowed().get();
                case EMPLOYEE_COMMISSIONS:
                    return app.getShopPref().isEmployeeCommissionsReportAllowed().get();
                case PURCHASE_ORDERS:
                    return app.getShopPref().isPurchaseOrdersReportAllowed().get();
                case ITEMS_PURCHASE_HISTORY:
                    return app.getShopPref().isItemsPurchaseHistoryReportAllowed().get();
                case RE_STOCK:
                    return app.getShopPref().isReStockReportAllowed().get();
                case SHIFT_GRATUITY:
                    return app.getShopPref().isShiftGratuityReportAllowed().get();
                case SUMMARY_GRATUITY:
                    return app.getShopPref().isSummaryGratuityReportAllowed().get();
                case DROPS_AND_PAYOUTS:
                    return app.getShopPref().isDropsAndPayoutsReportAllowed().get();
                case SALES_SUMMARY_REPORT:
                    return app.getShopPref().isSalesSummaryReportReportAllowed().get();
            }
        }
        return true;
    }

    private static boolean isLimited(RestrictionType restrictionType) {
        TcrApplication app = TcrApplication.get();
        if (app.isFreemium()) {
            switch (restrictionType) {
                case INVENTORY_LIMIT:
                    return app.getShopPref().isInventoryLimited().get();
                case EMPLOYEE_LIMIT:
                    return true;
            }
        }
        return false;
    }

    public static boolean isExportInventoryAllowed() {
        return isAllowed(RestrictionType.EXPORT_INVENTORY);
    }

    public static boolean isModifiersAllowed() {
        return isAllowed(RestrictionType.MODIFIERS);
    }

    public static boolean isEditingCustomersAllowed() {
        return isAllowed(RestrictionType.CUSTOMER);
    }

    public static boolean isScaleConnectionAllowed() {
        return isAllowed(RestrictionType.SCALE_CONNECTION);
    }

    public static boolean isCustomPermissionAllowed() {
        return isAllowed(RestrictionType.EMPLOYEE_CUSTOM_PERMISSIONS);
    }

    public static boolean isStockTrackingAllowed() {
        return isAllowed(RestrictionType.STOCK_TRACKING);
    }

    public static boolean isSerializableAllowed() {
        return TcrApplication.get().getShopPref().acceptSerializableItems().get();
    }

    public static boolean isEmployeeLimited() {
        return isLimited(RestrictionType.EMPLOYEE_LIMIT);
    }

    public static boolean isInventoryLimited() {
        return isLimited(RestrictionType.INVENTORY_LIMIT);
    }

    public static int getEmployeeLimit() {
        return TcrApplication.get().getShopPref().employeeLimit().get();
    }

    public static int getInventoryLimit() {
        return TcrApplication.get().getShopPref().inventoryLimit().get();
    }

    public static boolean isZReportFiltersAllowed() {
        return isAllowed(RestrictionType.Z_REPORT_FILTERS);
    }

    public static boolean isPurchaseOrdersAllowed() {
        return isAllowed(RestrictionType.PURCHASE_ORDERS_ACCESS);
    }

    public static boolean isJustZReportAllowed() {
        return isAllowed(RestrictionType.JUST_Z_REPORT);
    }

    public static boolean isXReportAllowed() {
        return isAllowed(RestrictionType.X_REPORT);
    }

    public static boolean isSalesReportAllowed() {
        return isAllowed(RestrictionType.SALES);
    }

    public static boolean isSalesByItemReportAllowed() {
        return isAllowed(RestrictionType.SALES_BY_ITEM);
    }

    public static boolean isPrepaidItemsSalesReportAllowed() {
        return isAllowed(RestrictionType.PREPAID_ITEMS_SALES);
    }

    public static boolean isToptenSoldRevenuesReportAllowed() {
        return isAllowed(RestrictionType.TOPTEN_SOLD_REVENUES);
    }

    public static boolean isToptenPrepaidRevenuesReportAllowed() {
        return isAllowed(RestrictionType.TOPTEN_PREPAID_REVENUES);
    }

    public static boolean isToptenSoldItemsReportAllowed() {
        return isAllowed(RestrictionType.TOPTEN_SOLD_ITEMS);
    }

    public static boolean isSalesByDepartmentReportAllowed() {
        return isAllowed(RestrictionType.SALES_BY_DEPARTMENT);
    }

    public static boolean isSalesByTenderTypeReportAllowed() {
        return isAllowed(RestrictionType.SALES_BY_TENDER_TYPE);
    }

    public static boolean isSalesByCustomersReportAllowed() {
        return isAllowed(RestrictionType.SALES_BY_CUSTOMERS);
    }

    public static boolean isSalesReturnsReportAllowed() {
        return isAllowed(RestrictionType.SALES_RETURNS);
    }

    public static boolean isReturnedItemsReportAllowed() {
        return isAllowed(RestrictionType.RETURNED_ITEMS);
    }

    public static boolean isRestockReportReportAllowed() {
        return isAllowed(RestrictionType.RESTOCK_REPORT);
    }

    public static boolean isInventoryValueReportAllowed() {
        return isAllowed(RestrictionType.INVENTORY_VALUE);
    }

    public static boolean isItemQuantityLogReportAllowed() {
        return isAllowed(RestrictionType.ITEM_QUANTITY_LOG);
    }

    public static boolean isInventoryStatusReportAllowed() {
        return isAllowed(RestrictionType.INVENTORY_STATUS);
    }

    public static boolean isEmployeeAttendanceReportAllowed() {
        return isAllowed(RestrictionType.EMPLOYEE_ATTENDANCE);
    }

    public static boolean isEmployeePayrollReportAllowed() {
        return isAllowed(RestrictionType.EMPLOYEE_PAYROLL);
    }

    public static boolean isEmployeeShiftReportAllowed() {
        return isAllowed(RestrictionType.EMPLOYEE_SHIFT);
    }

    public static boolean isEmployeeCommissionsReportAllowed() {
        return isAllowed(RestrictionType.EMPLOYEE_COMMISSIONS);
    }

    public static boolean isPurchaseOrdersReportAllowed() {
        return isAllowed(RestrictionType.PURCHASE_ORDERS);
    }

    public static boolean isItemsPurchaseHistoryReportAllowed() {
        return isAllowed(RestrictionType.ITEMS_PURCHASE_HISTORY);
    }

    public static boolean isReStockReportAllowed() {
        return isAllowed(RestrictionType.RE_STOCK);
    }

    public static boolean isShiftGratuityReportAllowed() {
        return isAllowed(RestrictionType.SHIFT_GRATUITY);
    }

    public static boolean isSummaryGratuityReportAllowed() {
        return isAllowed(RestrictionType.SUMMARY_GRATUITY);
    }

    public static boolean isDropsAndPayoutsReportAllowed() {
        return isAllowed(RestrictionType.DROPS_AND_PAYOUTS);
    }
    public static boolean isSalesSummaryReportReportAllowed() {
        return isAllowed(RestrictionType.SALES_SUMMARY_REPORT);
    }

    public class PlanOption {

        @SerializedName("name")
        private String name;

        @SerializedName("value")
        private String value;

        @SerializedName("descr")
        private String description;

        @SerializedName("type")
        private String type;

        public PlanOption() {
        }

        public boolean isAvailable() {
            return OptionType.isFlag(type) && Integer.valueOf(value) == 1;
        }

        public int getValue() {
            return OptionType.isValue(type) ? Integer.valueOf(value) : 0;
        }
    }

    public enum OptionType {
        VALUE("val"),
        FLAG("flag");

        private String type;

        OptionType(String type) {
            this.type = type;
        }

        public static boolean isFlag(String type) {
            return FLAG.type.equalsIgnoreCase(type);
        }

        public static boolean isValue(String type) {
            return VALUE.type.equalsIgnoreCase(type);
        }
    }

    private enum RestrictionType {
        MODIFIERS,
        CUSTOMER,
        EMPLOYEE_LIMIT,
        INVENTORY_LIMIT,
        EXPORT_INVENTORY,
        EMPLOYEE_CUSTOM_PERMISSIONS,
        STOCK_TRACKING,
        SCALE_CONNECTION,
        Z_REPORT_FILTERS,
        PURCHASE_ORDERS_ACCESS,
        JUST_Z_REPORT,
        JUST_Z_REPORT_ALLOWED,
        X_REPORT,
        SALES,
        SALES_BY_ITEM,
        PREPAID_ITEMS_SALES,
        TOPTEN_SOLD_REVENUES,
        TOPTEN_PREPAID_REVENUES,
        TOPTEN_SOLD_ITEMS,
        SALES_BY_DEPARTMENT,
        SALES_BY_TENDER_TYPE,
        SALES_BY_CUSTOMERS,
        SALES_RETURNS,
        RETURNED_ITEMS,
        RESTOCK_REPORT,
        INVENTORY_VALUE,
        ITEM_QUANTITY_LOG,
        INVENTORY_STATUS,
        EMPLOYEE_ATTENDANCE,
        EMPLOYEE_PAYROLL,
        EMPLOYEE_SHIFT,
        EMPLOYEE_COMMISSIONS,
        PURCHASE_ORDERS,
        ITEMS_PURCHASE_HISTORY,
        RE_STOCK,
        SHIFT_GRATUITY,
        SUMMARY_GRATUITY,
        DROPS_AND_PAYOUTS,
        SALES_SUMMARY_REPORT
        }
}