package com.kaching123.tcr.model;

import com.kaching123.tcr.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by vkompaniets on 24.12.13.
 */
public enum PermissionPreset implements LabaledEnum{

    CASHIER(1,
            R.string.permission_preset_cashier,
            Permission.SALES_TRANSACTION,
            Permission.SALES_RETURN,
            Permission.CUSTOMER_MANAGEMENT),

    MANAGER(2,
            R.string.permission_preset_manager,
            Permission.SALES_TRANSACTION,
            Permission.SALES_DISCOUNTS,
            Permission.SALES_TAX,
            Permission.SALES_RETURN,
            Permission.VOID_SALES,
            Permission.REPORTS,
            /*Permission.TABLE_ORDERING,
            Permission.JOIN_TABLE,
            Permission.GUEST_TABLE_ORDERING,*/
            Permission.INVENTORY_MODULE,
            Permission.OPEN_CLOSE_SHIFT,
            Permission.NO_SALE,
            Permission.DROPS_AND_PAYOUTS,
            Permission.EMPLOYEE_MANAGEMENT,
            Permission.CUSTOMER_MANAGEMENT,
            Permission.ON_HOLD_GLOBAL,
            Permission.SALES_HISTORY/*,
            Permission.BACKUPS*/),

    /*HOST(3,
            R.string.permission_preset_host
            *//*,Permission.TABLE_ORDERING,
            Permission.TABLE_ORDERING,
            Permission.JOIN_TABLE*//*),

    WAITER(4,
            R.string.permission_preset_waiter
            *//*,Permission.TABLE_ORDERING,
            Permission.TABLE_ORDERING,
            Permission.JOIN_TABLE*//*),
    COOK(5,
            R.string.permission_preset_cook),*/

    CUSTOM(0, R.string.permission_custom);

    private final long id;
    private final int labelId;

    private LinkedHashSet<Permission> permissions = new LinkedHashSet<Permission>();

    PermissionPreset(long id, int labelId, Permission... permissions) {
        this.id = id;
        this.labelId = labelId;
        this.permissions.addAll(Arrays.asList(permissions));
    }

    public long getId() {
        return id;
    }

    public int getLabelId() {
        return labelId;
    }

    public Collection<Permission> getPermissions(){
        return permissions;
    }


    public boolean isPreset(Collection<Permission> input) {
        if (permissions.size() == 0 && input.size() == 0)
            return false;

        if (input == null || input.size() != permissions.size())
            return false;

        for(Permission i : input){
            if(!permissions.contains(i))
                return false;
        }
        return true;
    }

    public static PermissionPreset valueOf(long id) {
        for (PermissionPreset permissionValue : PermissionPreset.values()) {
            if (id == permissionValue.id)
                return permissionValue;
        }

        throw new IllegalArgumentException(id + " is not a constant in "
                + PermissionPreset.class.getSimpleName());
    }

}
