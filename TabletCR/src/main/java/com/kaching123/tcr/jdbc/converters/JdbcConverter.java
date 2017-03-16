package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

public abstract class JdbcConverter<T extends IValueModel> {

    public abstract T toValues(JdbcJSONObject rs) throws JSONException;

    public abstract String getTableName();

    public abstract String getGuidColumn();

    public abstract String getLocalGuidColumn();

    public abstract JSONObject getJSONObject(T model);

    public String getParentGuidColumn() {
        return null;
    }

    public abstract SingleSqlCommand insertSQL(T model, IAppCommandContext appCommandContext);

    public abstract SingleSqlCommand updateSQL(T model, IAppCommandContext appCommandContext);

    public boolean supportUpdateTimeLocalFlag() {
        return false;
    }

    public boolean supportUpdateTimeFlag() {
        return true;
    }

    public boolean supportDeleteFlag() {
        return true;
    }

    public boolean supportDraftFlag() {
        return true;
    }

    protected long getShopId(){
        return TcrApplication.get().getShopPref().shopId().get();
    }

	public SingleSqlCommand deleteSQL(T model, IAppCommandContext appCommandContext) {
        return deleteSQL(JdbcFactory.getApiMethod(model), model.getGuid(), appCommandContext);
    }

    public final SingleSqlCommand deleteSQL(String method, String guid, IAppCommandContext appCommandContext) {
        return _update(getTableName(), appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(getGuidColumn(), guid)
                .build(method);
    }


    public static boolean compareTable(String table, String... tables) {
        if (table == null || tables == null) return false;
        for (String t : tables) if (table.equalsIgnoreCase(t)) return true;
        return false;
    }


    public static String getURIFromTable(String table){
        if (compareTable(table, ShopStore.CashDrawerMovementTable.TABLE_NAME, CashDrawerMovementJdbcConverter.TABLE_NAME)) return ShopStore.CashDrawerMovementTable.URI_CONTENT;
        if (compareTable(table, ShopStore.CategoryTable.TABLE_NAME, CategoryJdbcConverter.CATEGORY_TABLE_NAME)) return ShopStore.CategoryTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ComposerTable.TABLE_NAME, ComposerJdbcConverter.TABLE_NAME)) return ShopStore.ComposerTable.URI_CONTENT;
        if (compareTable(table, ShopStore.CountryTable.TABLE_NAME, CountryJdbcConverter.TABLE_NAME)) return ShopStore.CountryTable.URI_CONTENT;
        if (compareTable(table, ShopStore.CreditReceiptTable.TABLE_NAME, CreditReceiptJdbcConverter.TABLE_NAME)) return ShopStore.CreditReceiptTable.URI_CONTENT;
        if (compareTable(table, ShopStore.CustomerTable.TABLE_NAME, CustomerJdbcConverter.TABLE_NAME)) return ShopStore.CustomerTable.URI_CONTENT;
        if (compareTable(table, ShopStore.DepartmentTable.TABLE_NAME, DepartmentJdbcConverter.DEPARTMENT_TABLE_NAME)) return ShopStore.DepartmentTable.URI_CONTENT;
        if (compareTable(table, ShopStore.EmployeeCommissionsTable.TABLE_NAME, CommissionsJdbcConverter.TABLE_NAME)) return ShopStore.EmployeeCommissionsTable.URI_CONTENT;
        if (compareTable(table, ShopStore.EmployeePermissionTable.TABLE_NAME, EmployeePermissionJdbcConverter.TABLE_NAME)) return ShopStore.EmployeePermissionTable.URI_CONTENT;
        if (compareTable(table, ShopStore.EmployeeTable.TABLE_NAME, EmployeeJdbcConverter.TABLE_NAME)) return ShopStore.EmployeeTable.URI_CONTENT;
        if (compareTable(table, ShopStore.EmployeeTimesheetTable.TABLE_NAME, EmployeeTimesheetJdbcConverter.TABLE_NAME)) return ShopStore.EmployeeTimesheetTable.URI_CONTENT;
        if (compareTable(table, ShopStore.EmployeeTipsTable.TABLE_NAME, TipsJdbcConverter.TABLE_NAME)) return ShopStore.EmployeeTipsTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ItemMatrixTable.TABLE_NAME, ItemMatrixJdbcConverter.TABLE_NAME)) return ShopStore.ItemMatrixTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ItemMovementTable.TABLE_NAME, ItemsMovementJdbcConverter.TABLE_NAME)) return ShopStore.ItemMovementTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ItemTable.TABLE_NAME, ItemsJdbcConverter.ITEM_TABLE_NAME)) return ShopStore.ItemTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ModifierGroupTable.TABLE_NAME, ItemsModifierGroupsJdbcConverter.TABLE_NAME)) return ShopStore.ModifierGroupTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ModifierTable.TABLE_NAME, ItemsModifiersJdbcConverter.TABLE_NAME)) return ShopStore.ModifierTable.URI_CONTENT;
        if (compareTable(table, ShopStore.MunicipalityTable.TABLE_NAME, MunicipalityJdbcConverter.TABLE_NAME)) return ShopStore.MunicipalityTable.URI_CONTENT;
        if (compareTable(table, ShopStore.PaymentTransactionTable.TABLE_NAME, PaymentTransactionJdbcConverter.TABLE_NAME)) return ShopStore.PaymentTransactionTable.URI_CONTENT;
        if (compareTable(table, ShopStore.PrinterAliasTable.TABLE_NAME, PrinterAliasJdbcConverter.TABLE_NAME)) return ShopStore.PrinterAliasTable.URI_CONTENT;
        if (compareTable(table, ShopStore.RegisterTable.TABLE_NAME, RegisterJdbcConverter.TABLE_NAME)) return ShopStore.RegisterTable.URI_CONTENT;
        if (compareTable(table, ShopStore.SaleAddonTable.TABLE_NAME, SaleOrderItemAddonJdbcConverter.TABLE_NAME)) return ShopStore.SaleAddonTable.URI_CONTENT;
        if (compareTable(table, ShopStore.SaleItemTable.TABLE_NAME, SaleOrderItemJdbcConverter.SALE_ORDER_ITEMS_TABLE_NAME)) return ShopStore.SaleItemTable.URI_CONTENT;
        if (compareTable(table, ShopStore.SaleOrderTable.TABLE_NAME, SaleOrdersJdbcConverter.SALE_ORDER_TABLE_NAME)) return ShopStore.SaleOrderTable.URI_CONTENT;
        if (compareTable(table, ShopStore.ShiftTable.TABLE_NAME, ShiftJdbcConverter.TABLE_NAME)) return ShopStore.ShiftTable.URI_CONTENT;
        if (compareTable(table, ShopStore.StateTable.TABLE_NAME, StateJdbcConverter.TABLE_NAME)) return ShopStore.StateTable.URI_CONTENT;
        if (compareTable(table, ShopStore.TaxGroupTable.TABLE_NAME, TaxGroupJdbcConverter.TABLE_NAME)) return ShopStore.TaxGroupTable.URI_CONTENT;
        if (compareTable(table, ShopStore.UnitLabelTable.TABLE_NAME, UnitLabelJdbcConverter.TABLE_NAME)) return ShopStore.UnitLabelTable.URI_CONTENT;
        if (compareTable(table, ShopStore.UnitTable.TABLE_NAME, UnitsJdbcConverter.TABLE_NAME)) return ShopStore.UnitTable.URI_CONTENT;
        if (compareTable(table, ShopStore.VariantItemTable.TABLE_NAME, VariantItemJdbcConverter.TABLE_NAME)) return ShopStore.VariantItemTable.URI_CONTENT;
        if (compareTable(table, ShopStore.VariantSubItemTable.TABLE_NAME, VariantSubItemJdbcConverter.TABLE_NAME)) return ShopStore.VariantSubItemTable.URI_CONTENT;
        return null;
    }

}
