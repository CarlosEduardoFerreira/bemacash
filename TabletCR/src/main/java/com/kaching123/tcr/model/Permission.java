package com.kaching123.tcr.model;

import com.kaching123.tcr.R;

/**
 * Created by pkabakov on 17.12.13.
 */
public enum Permission implements LabaledEnum {

    //Note: order remains on UI, insert new items carefully!

    TIPS(21, R.string.permission_tips, Group.SALES_MODULE),
    VOID_SALES(3, R.string.permission_void_sales, Group.SALES_MODULE),
    CHANGE_QTY(59, R.string.permission_change_quantity, Group.SALES_MODULE),
    SALES_TRANSACTION(1, R.string.permission_sales_transaction, Group.SALES_MODULE),
    SALES_TAX(20, R.string.permission_sales_tax, Group.SALES_MODULE),
    SALES_DISCOUNTS(2, R.string.permission_sales_discount, Group.SALES_MODULE),
    SALES_RETURN(4, R.string.permission_sales_return, Group.SALES_MODULE),
    WARRANTY_EXPIRATION_OVERRIDE(23, R.string.permission_warranty_expiration_override, Group.SALES_MODULE),
    ON_HOLD_GLOBAL(102, R.string.permission_on_hold_global, Group.SALES_MODULE),

    DROPS_AND_PAYOUTS(17, R.string.permission_drops_and_payout, Group.DASHBOARD),
    OPEN_CLOSE_SHIFT(19, R.string.permission_open_close_shift, Group.DASHBOARD),
    EMPLOYEE_MANAGEMENT(13, R.string.permission_employee_management, Group.DASHBOARD),
    CUSTOMER_MANAGEMENT(14, R.string.permission_customer_management, Group.DASHBOARD),
    CUSTOMER_LOYALTY_BONUS_POINTS(64, R.string.permission_customer_loyalty_bonus_points, Group.DASHBOARD),
    CASH_DRAWER_MONEY(22, R.string.permission_cash_drawer_money, Group.DASHBOARD),
    NO_SALE(16, R.string.permission_no_sales, Group.DASHBOARD),
    INVENTORY_MODULE(12, R.string.permission_inventory_module, Group.DASHBOARD),
    REPORTS(10, R.string.permission_reports, Group.DASHBOARD),

    TRAINING_MODE(24, R.string.permission_training_mode, Group.SYSTEM_CONFIGURATION),
    ADMIN(18, R.string.permission_admin, Group.SYSTEM_CONFIGURATION),
    SOFTWARE_UPDATE(25, R.string.permission_software_update, Group.SYSTEM_CONFIGURATION);

    private final long id;
    private final int labelId;
    private final Group group;

    Permission(long id, int labelId, Group group) {
        this.id = id;
        this.labelId = labelId;
        this.group = group;
    }

    public static Permission valueOf(long id) {
        for (Permission permissionValue : Permission.values()) {
            if (id == permissionValue.id)
                return permissionValue;
        }

        throw new IllegalArgumentException(id + " is not a constant in "
                + Permission.class.getSimpleName());
    }

    public static Permission valueOfOrNull(long id) {
        for (Permission permissionValue : Permission.values()) {
            if (id == permissionValue.id)
                return permissionValue;
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public int getLabelId() {
        return labelId;
    }

    public Group getGroup() {
        return group;
    }

    public static enum Group implements LabaledEnum {
        SALES_MODULE(R.string.permission_group_sales_module),
        //TABLE_MANAGEMENT(R.string.permission_group_table_management),
        DASHBOARD(R.string.permission_group_dashboard),
        SYSTEM_CONFIGURATION(R.string.permission_group_system_configuration);

        private final int labelId;

        Group(int labelId) {
            this.labelId = labelId;
        }

        public int getLabelId() {
            return labelId;
        }
    }
}
