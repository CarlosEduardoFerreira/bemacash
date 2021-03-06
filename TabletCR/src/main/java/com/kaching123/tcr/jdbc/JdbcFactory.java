package com.kaching123.tcr.jdbc;

import com.kaching123.tcr.jdbc.converters.ActivationCarrierJdbcConverter;
import com.kaching123.tcr.jdbc.converters.BillPaymentDescriptionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CashDrawerMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CategoryJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CommissionsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ComposerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CountryJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CreditReceiptJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.DefinedOnHoldJdbcConverter;
import com.kaching123.tcr.jdbc.converters.DepartmentJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeBreaksJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeePermissionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeTimesheetJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemKDSJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemMatrixJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifierGroupsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.KDSAliasJdbcConverter;
import com.kaching123.tcr.jdbc.converters.LoyaltyIncentiveItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.LoyaltyIncentiveJdbcConverter;
import com.kaching123.tcr.jdbc.converters.LoyaltyIncentivePlanJdbcConverter;
import com.kaching123.tcr.jdbc.converters.LoyaltyPlanJdbcConverter;
import com.kaching123.tcr.jdbc.converters.LoyaltyPointsMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.MunicipalityJdbcConverter;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.PrinterAliasJdbcConverter;
import com.kaching123.tcr.jdbc.converters.RegisterJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleIncentiveJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemAddonJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ShiftJdbcConverter;
import com.kaching123.tcr.jdbc.converters.StateJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TBPJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TBPxRegisterJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TaxGroupJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TipsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitLabelJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.VariantItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.VariantSubItemJdbcConverter;
import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.CashDrawerMovementModel;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.model.CommissionsModel;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.CountryModel;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.DefinedOnHoldModel;
import com.kaching123.tcr.model.EmployeeBreakTimesheetModel;
import com.kaching123.tcr.model.ItemKdsModel;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.model.LoyaltyIncentiveItemModel;
import com.kaching123.tcr.model.LoyaltyIncentiveModel;
import com.kaching123.tcr.model.LoyaltyIncentivePlanModel;
import com.kaching123.tcr.model.LoyaltyPlanModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.MultipleDiscountModel;
import com.kaching123.tcr.model.MunicipalityModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.SaleIncentiveModel;
import com.kaching123.tcr.model.SaleModifierModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.model.StateModel;
import com.kaching123.tcr.model.TBPModel;
import com.kaching123.tcr.model.TBPxRegisterModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.jdbc.converters.MultipleDiscountJdbcConverter;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ActivationCarrierTable;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.ComposerTable;
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
import com.kaching123.tcr.store.ShopStore.KDSAliasTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.MultipleDiscountTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.TBPTable;
import com.kaching123.tcr.store.ShopStore.TBPxRegisterTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.ShopStore.DefinedOnHoldTable;
import com.kaching123.tcr.store.ShopStore.EmployeeBreaksTimesheetTable;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.util.HashMap;

public class JdbcFactory {

    public static final String METHOD_ADD = "add_";
    public static final String METHOD_UPDATE = "edit_";
    public static final String METHOD_DELETE = "delete_";

    private static final HashMap<String, JdbcConverter> CONVERTERS = new HashMap<>();
    private static final HashMap<Class, JdbcConverter> CONVERTERS2 = new HashMap<>();

    private static final HashMap<String, String> API_METHOD = new HashMap<>();
    private static final HashMap<Class, String> API_METHOD2 = new HashMap<>();

    static {
        JdbcConverter c;
        CONVERTERS.put(ItemTable.TABLE_NAME, c = new ItemsJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ItemModel.class, c);
        CONVERTERS2.put(ItemExModel.class, c);

        API_METHOD.put(ItemTable.TABLE_NAME, "items");
        API_METHOD2.put(ItemModel.class, "items");
        API_METHOD2.put(ItemExModel.class, "items");

        CONVERTERS.put(ModifierTable.TABLE_NAME, c = new ItemsModifiersJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ModifierModel.class, c);
        CONVERTERS2.put(ModifierExModel.class, c);

        API_METHOD.put(ModifierTable.TABLE_NAME, "modifiers");
        API_METHOD2.put(ModifierModel.class, "modifiers");
        API_METHOD2.put(ModifierExModel.class, "modifiers");

        CONVERTERS.put(ModifierGroupTable.TABLE_NAME, c = new ItemsModifierGroupsJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ModifierGroupModel.class, c);

        API_METHOD.put(ModifierGroupTable.TABLE_NAME, "modifier_groups");
        API_METHOD2.put(ModifierGroupModel.class, "modifier_groups");

        CONVERTERS.put(ItemMovementTable.TABLE_NAME, c = new ItemsMovementJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ItemMovementModel.class, c);

        API_METHOD.put(ItemMovementTable.TABLE_NAME, "item_movements");
        API_METHOD2.put(ItemMovementModel.class, "item_movements");

        CONVERTERS.put(CategoryTable.TABLE_NAME, c = new CategoryJdbcConverter()    );
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(CategoryModel.class, c);

        API_METHOD.put(CategoryTable.TABLE_NAME, "categories");
        API_METHOD2.put(CategoryModel.class, "categories");

        CONVERTERS.put(DefinedOnHoldTable.TABLE_NAME, c = new DefinedOnHoldJdbcConverter());
        CONVERTERS2.put(DefinedOnHoldModel.class, c);

        CONVERTERS.put(DepartmentTable.TABLE_NAME, c = new DepartmentJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(DepartmentModel.class, c);

        API_METHOD.put(DepartmentTable.TABLE_NAME, "departments");
        API_METHOD2.put(DepartmentModel.class, "departments");

        CONVERTERS.put(SaleOrderTable.TABLE_NAME, c = new SaleOrdersJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(SaleOrderModel.class, c);

        API_METHOD.put(SaleOrderTable.TABLE_NAME, "sale_orders");
        API_METHOD2.put(SaleOrderModel.class, "sale_orders");

        CONVERTERS.put(SaleItemTable.TABLE_NAME, c = new SaleOrderItemJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(SaleOrderItemModel.class, c);

        API_METHOD.put(SaleItemTable.TABLE_NAME, "sale_order_items");
        API_METHOD2.put(SaleOrderItemModel.class, "sale_order_items");

        CONVERTERS.put(EmployeeTable.TABLE_NAME, c = new EmployeeJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(EmployeeModel.class, c);

        API_METHOD.put(EmployeeTable.TABLE_NAME, "employees");
        API_METHOD2.put(EmployeeModel.class, "employees");

        CONVERTERS.put(SaleAddonTable.TABLE_NAME, c = new SaleOrderItemAddonJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(SaleModifierModel.class, c);

        API_METHOD.put(SaleAddonTable.TABLE_NAME, "sale_order_item_addons");
        API_METHOD2.put(SaleModifierModel.class, "sale_order_item_addons");

        CONVERTERS.put(PaymentTransactionTable.TABLE_NAME, c = new PaymentTransactionJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(PaymentTransactionModel.class, c);

        API_METHOD.put(PaymentTransactionTable.TABLE_NAME, "payment_transactions");
        API_METHOD2.put(PaymentTransactionModel.class, "payment_transactions");

        CONVERTERS.put(UnitTable.TABLE_NAME, c = new UnitsJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(Unit.class, c);

        API_METHOD.put(UnitTable.TABLE_NAME, "units");
        API_METHOD2.put(Unit.class, "units");

        CONVERTERS.put(ComposerTable.TABLE_NAME, c = new ComposerJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ComposerModel.class, c);

        API_METHOD.put(ComposerTable.TABLE_NAME, "composer");
        API_METHOD2.put(ComposerModel.class, "composer");

        CONVERTERS.put(ShiftTable.TABLE_NAME, c = new ShiftJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ShiftModel.class, c);

        API_METHOD.put(ShiftTable.TABLE_NAME, "shifts");
        API_METHOD2.put(ShiftModel.class, "shifts");

        CONVERTERS.put(CashDrawerMovementTable.TABLE_NAME, c = new CashDrawerMovementJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(CashDrawerMovementModel.class, c);

        API_METHOD.put(CashDrawerMovementTable.TABLE_NAME, "cash_drawer_movements");
        API_METHOD2.put(CashDrawerMovementModel.class, "cash_drawer_movements");

        CONVERTERS.put(EmployeePermissionTable.TABLE_NAME, c = new EmployeePermissionJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(EmployeePermissionModel.class, c);

        API_METHOD.put(EmployeePermissionTable.TABLE_NAME, "employee_permissions");
        API_METHOD2.put(EmployeePermissionModel.class, "employee_permissions");

        CONVERTERS.put(EmployeeTimesheetTable.TABLE_NAME, c = new EmployeeTimesheetJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(EmployeeTimesheetModel.class, c);

        API_METHOD.put(EmployeeTimesheetTable.TABLE_NAME, "employee_timesheets");
        API_METHOD2.put(EmployeeTimesheetModel.class, "employee_timesheets");

        CONVERTERS.put(EmployeeBreaksTimesheetTable.TABLE_NAME, c = new EmployeeBreaksJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(EmployeeBreakTimesheetModel.class, c);

        API_METHOD.put(EmployeeBreaksTimesheetTable.TABLE_NAME, "employee_breaks_timesheet");
        API_METHOD2.put(EmployeeBreakTimesheetModel.class, "employee_breaks_timesheet");

        CONVERTERS.put(EmployeeTipsTable.TABLE_NAME, c = new TipsJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(TipsModel.class, c);

        API_METHOD.put(EmployeeTipsTable.TABLE_NAME, "received_tips");
        API_METHOD2.put(TipsModel.class, "received_tips");

        CONVERTERS.put(TaxGroupTable.TABLE_NAME, c = new TaxGroupJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(TaxGroupModel.class, c);

        API_METHOD.put(TaxGroupTable.TABLE_NAME, "tax_groups");
        API_METHOD2.put(TaxGroupModel.class, "tax_groups");

        CONVERTERS.put(ShopStore.CountryTable.TABLE_NAME, c = new CountryJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(CountryModel.class, c);

        CONVERTERS.put(ShopStore.StateTable.TABLE_NAME, c = new StateJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(StateModel.class, c);

        CONVERTERS.put(ShopStore.MunicipalityTable.TABLE_NAME, c = new MunicipalityJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(MunicipalityModel.class, c);

        CONVERTERS.put(RegisterTable.TABLE_NAME, c = new RegisterJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(RegisterModel.class, c);

        API_METHOD.put(RegisterTable.TABLE_NAME, "registers");
        API_METHOD2.put(RegisterModel.class, "registers");

        CONVERTERS.put(BillPaymentDescriptionTable.TABLE_NAME, c = new BillPaymentDescriptionJdbcConverter());
        CONVERTERS2.put(BillPaymentDescriptionModel.class, c);

        API_METHOD.put(BillPaymentDescriptionTable.TABLE_NAME, "bill_payments_descriptions");
        API_METHOD2.put(BillPaymentDescriptionModel.class, "bill_payments_descriptions");

        CONVERTERS.put(CustomerTable.TABLE_NAME, c = new CustomerJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(CustomerModel.class, c);

        API_METHOD.put(CustomerTable.TABLE_NAME, "customers");
        API_METHOD2.put(CustomerModel.class, "customers");

        CONVERTERS.put(PrinterAliasTable.TABLE_NAME, c = new PrinterAliasJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(PrinterAliasModel.class, c);

        API_METHOD.put(PrinterAliasTable.TABLE_NAME, "printer_aliases");
        API_METHOD2.put(PrinterAliasModel.class, "printer_aliases");

        CONVERTERS.put(KDSAliasTable.TABLE_NAME, c = new KDSAliasJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(KDSAliasModel.class, c);

        CONVERTERS.put(ShopStore.ItemKDSTable.TABLE_NAME, c = new ItemKDSJdbcConverter());
        CONVERTERS2.put(ItemKdsModel.class, c);

        API_METHOD.put(KDSAliasTable.TABLE_NAME, "printer_aliases");
        API_METHOD2.put(KDSAliasModel.class, "printer_aliases");

        CONVERTERS.put(CreditReceiptTable.TABLE_NAME, c = new CreditReceiptJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(CreditReceiptModel.class, c);

        API_METHOD.put(CreditReceiptTable.TABLE_NAME, "credit_receipts");
        API_METHOD2.put(CreditReceiptModel.class, "credit_receipts");

        CONVERTERS.put(ActivationCarrierTable.TABLE_NAME, c = new ActivationCarrierJdbcConverter());
        CONVERTERS2.put(ActivationCarrierModel.class, c);

        CONVERTERS.put(EmployeeCommissionsTable.TABLE_NAME, c = new CommissionsJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(CommissionsModel.class, c);

        API_METHOD.put(EmployeeCommissionsTable.TABLE_NAME, "commissions");
        API_METHOD2.put(CommissionsModel.class, "commissions");

        CONVERTERS.put(ShopStore.UnitLabelTable.TABLE_NAME, c = new UnitLabelJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(UnitLabelModel.class, c);

        API_METHOD.put(ShopStore.UnitLabelTable.TABLE_NAME, "unit_label");
        API_METHOD2.put(UnitLabelModel.class, "unit_label");

        CONVERTERS.put(ShopStore.VariantItemTable.TABLE_NAME, c = new VariantItemJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(VariantItemModel.class, c);

        API_METHOD.put(ShopStore.VariantItemTable.TABLE_NAME, "variants");
        API_METHOD2.put(VariantItemModel.class, "variants");

        CONVERTERS.put(ShopStore.VariantSubItemTable.TABLE_NAME, c = new VariantSubItemJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(VariantSubItemModel.class, c);

        API_METHOD.put(ShopStore.VariantSubItemTable.TABLE_NAME, "sub_variants");
        API_METHOD2.put(VariantSubItemModel.class, "sub_variants");

        CONVERTERS.put(ShopStore.ItemMatrixTable.TABLE_NAME, c = new ItemMatrixJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(ItemMatrixModel.class, c);

        API_METHOD.put(ShopStore.ItemMatrixTable.TABLE_NAME, "item_matrixes");
        API_METHOD2.put(ItemMatrixModel.class, "item_matrixes");

        CONVERTERS.put(ShopStore.LoyaltyIncentiveTable.TABLE_NAME, c = new LoyaltyIncentiveJdbcConverter());
        CONVERTERS2.put(LoyaltyIncentiveModel.class, c);

        API_METHOD.put(ShopStore.LoyaltyIncentiveTable.TABLE_NAME, "loyalty_incentive");
        API_METHOD2.put(LoyaltyIncentiveModel.class, "loyalty_incentive");

        CONVERTERS.put(ShopStore.LoyaltyIncentiveItemTable.TABLE_NAME, c = new LoyaltyIncentiveItemJdbcConverter());
        CONVERTERS2.put(LoyaltyIncentiveItemModel.class, c);

        API_METHOD.put(ShopStore.LoyaltyIncentiveItemTable.TABLE_NAME, "loyalty_incentive_item");
        API_METHOD2.put(LoyaltyIncentiveItemModel.class, "loyalty_incentive_item");

        CONVERTERS.put(ShopStore.LoyaltyPlanTable.TABLE_NAME, c = new LoyaltyPlanJdbcConverter());
        CONVERTERS2.put(LoyaltyPlanModel.class, c);

        API_METHOD.put(ShopStore.LoyaltyPlanTable.TABLE_NAME, "loyalty_plan");
        API_METHOD2.put(LoyaltyPlanModel.class, "loyalty_plan");

        CONVERTERS.put(ShopStore.LoyaltyIncentivePlanTable.TABLE_NAME, c = new LoyaltyIncentivePlanJdbcConverter());
        CONVERTERS2.put(LoyaltyIncentivePlanModel.class, c);

        API_METHOD.put(ShopStore.LoyaltyIncentivePlanTable.TABLE_NAME, "loyalty_incentive_plan");
        API_METHOD2.put(LoyaltyIncentivePlanModel.class, "loyalty_incentive_plan");

        CONVERTERS.put(ShopStore.LoyaltyPointsMovementTable.TABLE_NAME, c = new LoyaltyPointsMovementJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(LoyaltyPointsMovementModel.class, c);

        API_METHOD.put(LoyaltyPointsMovementTable.TABLE_NAME, "loyalty_points_movement");
        API_METHOD2.put(LoyaltyPointsMovementModel.class, "loyalty_points_movement");

        CONVERTERS.put(ShopStore.SaleIncentiveTable.TABLE_NAME, c = new SaleIncentiveJdbcConverter());
        CONVERTERS.put(c.getTableName(), c);
        CONVERTERS2.put(SaleIncentiveModel.class, c);

        API_METHOD.put(SaleIncentiveTable.TABLE_NAME, "sale_incentive");
        API_METHOD2.put(SaleIncentiveModel.class, "sale_incentive");

        CONVERTERS.put(ShopStore.TBPTable.TABLE_NAME, c = new TBPJdbcConverter());
        CONVERTERS2.put(TBPModel.class, c);

        API_METHOD.put(TBPTable.TABLE_NAME, "time_based_pricing");
        API_METHOD2.put(TBPModel.class, "time_based_pricing");

        CONVERTERS.put(ShopStore.TBPxRegisterTable.TABLE_NAME, c = new TBPxRegisterJdbcConverter());
        CONVERTERS2.put(TBPxRegisterModel.class, c);

        API_METHOD.put(TBPxRegisterTable.TABLE_NAME, "time_based_pricing_register");
        API_METHOD2.put(TBPxRegisterModel.class, "time_based_pricing_register");

        CONVERTERS.put(MultipleDiscountTable.TABLE_NAME, c = new MultipleDiscountJdbcConverter());
        CONVERTERS2.put(MultipleDiscountModel.class, c);

        API_METHOD.put(MultipleDiscountTable.TABLE_NAME, "multiple_discount_item");
        API_METHOD2.put(MultipleDiscountModel.class, "multiple_discount_item");

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

    /**
     * Use this method for work with PaymentTransactionModel and childs
     * @param clazz
     * @param model
     * @param appCommandContext
     * @param <T>
     * @return
     */
    public static <T extends IValueModel> ISqlCommand insert(Class<T> clazz, T model, IAppCommandContext appCommandContext) {
        String apiMethod = API_METHOD2.get(clazz);
        if (apiMethod == null) {
            throw new IllegalArgumentException("no api for class = " + model.getClass());
        }
        return getConverter(clazz).insertSQL(model, appCommandContext);
    }

    /**
     * Use insert(Class<T> clazz, T model, IAppCommandContext appCommandContext) instead this method if you need perform paymentTransaction,
     * in other case you can use this method.
     * @param model
     * @param appCommandContext
     * @param <T>
     * @return
     */
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

    public static ISqlCommand deleteReal(String tableName, String guid, IAppCommandContext appCommandContext) {
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
