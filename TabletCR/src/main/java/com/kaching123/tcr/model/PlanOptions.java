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
        SCALE_CONNECTION
    }
}