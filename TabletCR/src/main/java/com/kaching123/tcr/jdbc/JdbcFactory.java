package com.kaching123.tcr.jdbc;

import com.kaching123.tcr.jdbc.converters.ActivationCarrierJdbcConverter;
import com.kaching123.tcr.jdbc.converters.BillPaymentDescriptionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CashDrawerMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CategoryJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CommissionsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ComposerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CreditReceiptJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.DepartmentJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeePermissionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeTimesheetJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifierGroupsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.PrinterAliasJdbcConverter;
import com.kaching123.tcr.jdbc.converters.RegisterJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemAddonJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ShiftJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TaxGroupJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TipsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitLabelJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.CashDrawerMovementModel;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.model.CommissionsModel;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.kaching123.tcr.store.ShopStore.ActivationCarrierTable;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.EmployeeCommissionsTable;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.ShopStore.ComposerTable;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.util.HashMap;

public class JdbcFactory {

    public static final String METHOD_ADD = "add_";
    public static final String METHOD_UPDATE = "edit_";
    public static final String METHOD_DELETE = "delete_";

    private static final HashMap<String, JdbcConverter> CONVERTERS = new HashMap<String, JdbcConverter>();
    private static final HashMap<Class, JdbcConverter> CONVERTERS2 = new HashMap<Class, JdbcConverter>();

    private static final HashMap<String, String> API_METHOD = new HashMap<String, String>();
    private static final HashMap<Class, String> API_METHOD2 = new HashMap<Class, String>();

    static {
        JdbcConverter c;
        CONVERTERS.put(ItemTable.TABLE_NAME, c = new ItemsJdbcConverter());
        CONVERTERS2.put(ItemModel.class, c);
        CONVERTERS2.put(ItemExModel.class, c);

        API_METHOD.put(ItemTable.TABLE_NAME, "items");
        API_METHOD2.put(ItemModel.class, "items");
        API_METHOD2.put(ItemExModel.class, "items");

        CONVERTERS.put(ModifierTable.TABLE_NAME, c = new ItemsModifiersJdbcConverter());
        CONVERTERS2.put(ModifierModel.class, c);
        CONVERTERS2.put(ModifierExModel.class, c);

        API_METHOD.put(ModifierTable.TABLE_NAME, "modifiers");
        API_METHOD2.put(ModifierModel.class, "modifiers");
        API_METHOD2.put(ModifierExModel.class, "modifiers");

        CONVERTERS.put(ModifierGroupTable.TABLE_NAME, c = new ItemsModifierGroupsJdbcConverter());
        CONVERTERS2.put(ModifierGroupModel.class, c);

        API_METHOD.put(ModifierGroupTable.TABLE_NAME, "modifier_groups");
        API_METHOD2.put(ModifierGroupModel.class, "modifier_groups");

        CONVERTERS.put(ItemMovementTable.TABLE_NAME, c = new ItemsMovementJdbcConverter());
        CONVERTERS2.put(ItemMovementModel.class, c);

        API_METHOD.put(ItemMovementTable.TABLE_NAME, "item_movements");
        API_METHOD2.put(ItemMovementModel.class, "item_movements");

        CONVERTERS.put(CategoryTable.TABLE_NAME, c = new CategoryJdbcConverter());
        CONVERTERS2.put(CategoryModel.class, c);

        API_METHOD.put(CategoryTable.TABLE_NAME, "categories");
        API_METHOD2.put(CategoryModel.class, "categories");

        CONVERTERS.put(DepartmentTable.TABLE_NAME, c = new DepartmentJdbcConverter());
        CONVERTERS2.put(DepartmentModel.class, c);

        API_METHOD.put(DepartmentTable.TABLE_NAME, "departments");
        API_METHOD2.put(DepartmentModel.class, "departments");

        CONVERTERS.put(SaleOrderTable.TABLE_NAME, c = new SaleOrdersJdbcConverter());
        CONVERTERS2.put(SaleOrderModel.class, c);

        API_METHOD.put(SaleOrderTable.TABLE_NAME, "sale_orders");
        API_METHOD2.put(SaleOrderModel.class, "sale_orders");

        CONVERTERS.put(SaleItemTable.TABLE_NAME, c = new SaleOrderItemJdbcConverter());
        CONVERTERS2.put(SaleOrderItemModel.class, c);

        API_METHOD.put(SaleItemTable.TABLE_NAME, "sale_order_items");
        API_METHOD2.put(SaleOrderItemModel.class, "sale_order_items");

        CONVERTERS.put(EmployeeTable.TABLE_NAME, c = new EmployeeJdbcConverter());
        CONVERTERS2.put(EmployeeModel.class, c);

        API_METHOD.put(EmployeeTable.TABLE_NAME, "employees");
        API_METHOD2.put(EmployeeModel.class, "employees");

        CONVERTERS.put(SaleAddonTable.TABLE_NAME, c = new SaleOrderItemAddonJdbcConverter());
        CONVERTERS2.put(SaleOrderItemAddonModel.class, c);

        API_METHOD.put(SaleAddonTable.TABLE_NAME, "sale_order_item_addons");
        API_METHOD2.put(SaleOrderItemAddonModel.class, "sale_order_item_addons");

        CONVERTERS.put(PaymentTransactionTable.TABLE_NAME, c = new PaymentTransactionJdbcConverter());
        CONVERTERS2.put(PaymentTransactionModel.class, c);

        API_METHOD.put(PaymentTransactionTable.TABLE_NAME, "payment_transactions");
        API_METHOD2.put(PaymentTransactionModel.class, "payment_transactions");

        CONVERTERS.put(UnitTable.TABLE_NAME, c = new UnitsJdbcConverter());
        CONVERTERS2.put(Unit.class, c);

        API_METHOD.put(UnitTable.TABLE_NAME, "units");
        API_METHOD2.put(Unit.class, "units");

        CONVERTERS.put(ComposerTable.TABLE_NAME, c = new ComposerJdbcConverter());
        CONVERTERS2.put(ComposerModel.class, c);

        API_METHOD.put(ComposerTable.TABLE_NAME, "composer");
        API_METHOD2.put(ComposerModel.class, "composer");

        CONVERTERS.put(ShiftTable.TABLE_NAME, c = new ShiftJdbcConverter());
        CONVERTERS2.put(ShiftModel.class, c);

        API_METHOD.put(ShiftTable.TABLE_NAME, "shifts");
        API_METHOD2.put(ShiftModel.class, "shifts");

        CONVERTERS.put(CashDrawerMovementTable.TABLE_NAME, c = new CashDrawerMovementJdbcConverter());
        CONVERTERS2.put(CashDrawerMovementModel.class, c);

        API_METHOD.put(CashDrawerMovementTable.TABLE_NAME, "cash_drawer_movements");
        API_METHOD2.put(CashDrawerMovementModel.class, "cash_drawer_movements");

        CONVERTERS.put(EmployeePermissionTable.TABLE_NAME, c = new EmployeePermissionJdbcConverter());
        CONVERTERS2.put(EmployeePermissionModel.class, c);

        API_METHOD.put(EmployeePermissionTable.TABLE_NAME, "employee_permissions");
        API_METHOD2.put(EmployeePermissionModel.class, "employee_permissions");

        CONVERTERS.put(EmployeeTimesheetTable.TABLE_NAME, c = new EmployeeTimesheetJdbcConverter());
        CONVERTERS2.put(EmployeeTimesheetModel.class, c);

        API_METHOD.put(EmployeeTimesheetTable.TABLE_NAME, "employee_timesheets");
        API_METHOD2.put(EmployeeTimesheetModel.class, "employee_timesheets");

        CONVERTERS.put(EmployeeTipsTable.TABLE_NAME, c = new TipsJdbcConverter());
        CONVERTERS2.put(TipsModel.class, c);

        API_METHOD.put(EmployeeTipsTable.TABLE_NAME, "received_tips");
        API_METHOD2.put(TipsModel.class, "received_tips");

        CONVERTERS.put(TaxGroupTable.TABLE_NAME, c = new TaxGroupJdbcConverter());
        CONVERTERS2.put(TaxGroupModel.class, c);

        API_METHOD.put(TaxGroupTable.TABLE_NAME, "tax_groups");
        API_METHOD2.put(TaxGroupModel.class, "tax_groups");

        CONVERTERS.put(RegisterTable.TABLE_NAME, c = new RegisterJdbcConverter());
        CONVERTERS2.put(RegisterModel.class, c);

        API_METHOD.put(RegisterTable.TABLE_NAME, "registers");
        API_METHOD2.put(RegisterModel.class, "registers");

        CONVERTERS.put(BillPaymentDescriptionTable.TABLE_NAME, c = new BillPaymentDescriptionJdbcConverter());
        CONVERTERS2.put(BillPaymentDescriptionModel.class, c);

        API_METHOD.put(BillPaymentDescriptionTable.TABLE_NAME, "bill_payments_descriptions");
        API_METHOD2.put(BillPaymentDescriptionModel.class, "bill_payments_descriptions");

        CONVERTERS.put(CustomerTable.TABLE_NAME, c = new CustomerJdbcConverter());
        CONVERTERS2.put(CustomerModel.class, c);

        API_METHOD.put(CustomerTable.TABLE_NAME, "customers");
        API_METHOD2.put(CustomerModel.class, "customers");

        CONVERTERS.put(PrinterAliasTable.TABLE_NAME, c = new PrinterAliasJdbcConverter());
        CONVERTERS2.put(PrinterAliasModel.class, c);

        API_METHOD.put(PrinterAliasTable.TABLE_NAME, "printer_aliases");
        API_METHOD2.put(PrinterAliasModel.class, "printer_aliases");

        CONVERTERS.put(CreditReceiptTable.TABLE_NAME, c = new CreditReceiptJdbcConverter());
        CONVERTERS2.put(CreditReceiptModel.class, c);

        API_METHOD.put(CreditReceiptTable.TABLE_NAME, "credit_receipts");
        API_METHOD2.put(CreditReceiptModel.class, "credit_receipts");

        CONVERTERS.put(ActivationCarrierTable.TABLE_NAME, c = new ActivationCarrierJdbcConverter());
        CONVERTERS2.put(ActivationCarrierModel.class, c);

        CONVERTERS.put(EmployeeCommissionsTable.TABLE_NAME, c = new CommissionsJdbcConverter());
        CONVERTERS2.put(CommissionsModel.class, c);

        API_METHOD.put(EmployeeCommissionsTable.TABLE_NAME, "commissions");
        API_METHOD2.put(CommissionsModel.class, "commissions");

        CONVERTERS.put(ShopStore.UnitLabelTable.TABLE_NAME, c = new UnitLabelJdbcConverter());
        CONVERTERS2.put(UnitLabelModel.class, c);

        API_METHOD.put(ShopStore.UnitLabelTable.TABLE_NAME, "unit_label");
        API_METHOD2.put(UnitLabelModel.class, "unit_label");

    }

    public static JdbcConverter getConverter(String tableName) {
        return CONVERTERS.get(tableName);
    }

    public static <T extends IValueModel> JdbcConverter<T> getConverter(T model) {
        return CONVERTERS2.get(model.getClass());
    }

    public static <T extends IValueModel> JdbcConverter<T> getConverter(Class<T> clazz) {
        return CONVERTERS2.get(clazz);
    }

    /*public static <T extends IValueModel> ISqlCommand insert(String tableName, T model) {
        String apiMethod = API_METHOD.get(tableName);
        if(apiMethod == null){
            throw new IllegalArgumentException("no api for table = " + tableName);
        }
        return CONVERTERS.get(tableName).insertSQL("add_" + apiMethod, model);
    }*/

    public static <T extends IValueModel> ISqlCommand insert(T model, IAppCommandContext appCommandContext) {
        String apiMethod = API_METHOD2.get(model.getClass());
        if(apiMethod == null){
            throw new IllegalArgumentException("no api for class = " + model.getClass());
        }
        return getConverter(model).insertSQL(model, appCommandContext);
    }

    public static <T extends IValueModel> ISqlCommand update(T model, IAppCommandContext appCommandContext) {
        String apiMethod = API_METHOD2.get(model.getClass());
        if(apiMethod == null){
            throw new IllegalArgumentException("no api for class = " + model.getClass());
        }
        return getConverter(model).updateSQL(model, appCommandContext);
    }

    public static <T extends IValueModel> ISqlCommand delete(T model, IAppCommandContext appCommandContext) {
        String apiMethod = API_METHOD2.get(model.getClass());
        if(apiMethod == null){
            throw new IllegalArgumentException("no api for class = " + model.getClass());
        }
        return getConverter(model).deleteSQL(model, appCommandContext);
    }

    public static ISqlCommand delete(String tableName, String guid, IAppCommandContext appCommandContext) {
        String apiMethod = API_METHOD.get(tableName);
        if(apiMethod == null){
            throw new IllegalArgumentException("no api for table = " + tableName);
        }
        return getConverter(tableName).deleteSQL(apiMethod, guid, appCommandContext);
    }

    public static <T extends IValueModel> String getApiMethod(T model) {
        return API_METHOD2.get(model.getClass());
    }

    public static <T extends IValueModel> String getApiMethod(Class<T> clazz) {
        return API_METHOD2.get(clazz);
    }

    public static String getApiMethod(String tableName) {
        return API_METHOD.get(tableName);
    }

}
