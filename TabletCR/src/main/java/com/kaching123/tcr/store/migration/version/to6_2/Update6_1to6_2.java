package com.kaching123.tcr.store.migration.version.to6_2;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_1to6_2 implements IUpdateContainer {

    private static final String SQL_RENAME_SALE_ORDER = "ALTER TABLE sale_order RENAME TO sale_order_tmp;";
    private static final String SQL_CREATE_SALE_ORDER = "CREATE TABLE sale_order(guid TEXT PRIMARY KEY NOT NULL, create_time INTEGER NOT NULL, operator_guid TEXT NOT NULL, shift_guid TEXT NOT NULL, customer_guid TEXT, discount TEXT, discount_type INTEGER, status INTEGER NOT NULL, hold_name TEXT, taxable INTEGER, print_seq_num INTEGER, register_id INTEGER NOT NULL, parent_id TEXT, order_type INTEGER, tml_total_price TEXT, tml_total_tax TEXT, tml_total_discount TEXT, is_tipped INTEGER NOT NULL DEFAULT (0), kitchen_print_status INTEGER, transaction_fee TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(operator_guid) REFERENCES employee(guid),FOREIGN KEY(shift_guid) REFERENCES shift(guid),FOREIGN KEY(customer_guid) REFERENCES customer(guid),FOREIGN KEY(register_id) REFERENCES register(_id),FOREIGN KEY(parent_id) REFERENCES sale_order(guid) ON DELETE CASCADE)";
    private static final String SQL_COPY_SALE_ORDER = "INSERT INTO sale_order SELECT * FROM sale_order_tmp;";
    private static final String SQL_DROP_TEMP_SALE_ORDER = "DROP TABLE sale_order_tmp;";

    private static final String SQL_CREATE_NO_FK_SALE_ORDER_ITEM = "CREATE TABLE sale_order_item( sale_order_item_id TEXT PRIMARY KEY NOT NULL, order_id TEXT NOT NULL, item_id TEXT NOT NULL, quantity TEXT, kitchen_printed_qty TEXT DEFAULT (0.000), price TEXT NOT NULL, price_type INTEGER, discountable INTEGER, discount TEXT, discount_type INTEGER, taxable INTEGER, tax TEXT, sequence INTEGER, parent_guid TEXT, final_gross_price TEXT, final_tax TEXT, final_discount TEXT, tmp_refund_quantity TEXT, notes TEXT, has_notes INTEGER, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";
    private static final String SQL_RENAME_SALE_ORDER_ITEM = "ALTER TABLE sale_order_item RENAME TO sale_order_item_tmp;";
    private static final String SQL_CREATE_SALE_ORDER_ITEM = "CREATE TABLE sale_order_item( sale_order_item_id TEXT PRIMARY KEY NOT NULL, order_id TEXT NOT NULL, item_id TEXT NOT NULL, quantity TEXT, kitchen_printed_qty TEXT DEFAULT (0.000), price TEXT NOT NULL, price_type INTEGER, discountable INTEGER, discount TEXT, discount_type INTEGER, taxable INTEGER, tax TEXT, sequence INTEGER, parent_guid TEXT, final_gross_price TEXT, final_tax TEXT, final_discount TEXT, tmp_refund_quantity TEXT, notes TEXT, has_notes INTEGER, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(order_id) REFERENCES sale_order(guid) ON DELETE CASCADE,FOREIGN KEY(parent_guid) REFERENCES sale_order_item(sale_order_item_id))";
    private static final String SQL_COPY_SALE_ORDER_ITEM = "INSERT INTO sale_order_item SELECT * FROM sale_order_item_tmp;";
    private static final String SQL_DROP_TEMP_SALE_ORDER_ITEM = "DROP TABLE sale_order_item_tmp;";

    private static final String SQL_CREATE_NO_FK_SALE_ORDER_ITEM_ADDON = "CREATE TABLE sale_order_item_addon( guid TEXT PRIMARY KEY NOT NULL, addon_id TEXT NOT NULL, item_guid TEXT NOT NULL, extra_cost TEXT NOT NULL, addon_type INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";
    private static final String SQL_RENAME_SALE_ORDER_ITEM_ADDON = "ALTER TABLE sale_order_item_addon RENAME TO sale_order_item_addon_tmp;";
    private static final String SQL_CREATE_SALE_ORDER_ITEM_ADDON = "CREATE TABLE sale_order_item_addon( guid TEXT PRIMARY KEY NOT NULL, addon_id TEXT NOT NULL, item_guid TEXT NOT NULL, extra_cost TEXT NOT NULL, addon_type INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(addon_id) REFERENCES items_modifier(modifier_guid),FOREIGN KEY(item_guid) REFERENCES sale_order_item(sale_order_item_id) ON DELETE CASCADE)";
    private static final String SQL_COPY_SALE_ORDER_ITEM_ADDON = "INSERT INTO sale_order_item_addon SELECT * FROM sale_order_item_addon_tmp;";
    private static final String SQL_DROP_TEMP_SALE_ORDER_ITEM_ADDON = "DROP TABLE sale_order_item_addon_tmp;";

    private static final String SQL_CREATE_NO_FK_PAYMENT_TRANSACTION = "CREATE TABLE payment_transaction( guid TEXT PRIMARY KEY NOT NULL, order_guid TEXT NOT NULL, parent_guid TEXT, amount TEXT NOT NULL, status INTEGER NOT NULL, unitsLabel INTEGER NOT NULL, operator_id TEXT NOT NULL, gtw_id INTEGER, gtwp_id TEXT, gtw_preauth_payment_id TEXT, gateway_closed_perauth_guid TEXT, d_reason TEXT, create_time INTEGER NOT NULL, shift_guid TEXT NOT NULL, card_name TEXT, change_amount TEXT, is_preauth INTEGER, balance TEXT, cash_back TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";
    private static final String SQL_RENAME_PAYMENT_TRANSACTION = "ALTER TABLE payment_transaction RENAME TO payment_transaction_tmp;";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION = "CREATE TABLE payment_transaction( guid TEXT PRIMARY KEY NOT NULL, order_guid TEXT NOT NULL, parent_guid TEXT, amount TEXT NOT NULL, status INTEGER NOT NULL, unitsLabel INTEGER NOT NULL, operator_id TEXT NOT NULL, gtw_id INTEGER, gtwp_id TEXT, gtw_preauth_payment_id TEXT, gateway_closed_perauth_guid TEXT, d_reason TEXT, create_time INTEGER NOT NULL, shift_guid TEXT NOT NULL, card_name TEXT, change_amount TEXT, is_preauth INTEGER, balance TEXT, cash_back TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0), FOREIGN KEY(order_guid) REFERENCES sale_order(guid) ON DELETE CASCADE,FOREIGN KEY(parent_guid) REFERENCES payment_transaction(guid),FOREIGN KEY(operator_id) REFERENCES employee(guid),FOREIGN KEY(shift_guid) REFERENCES shift(guid))";
    private static final String SQL_COPY_PAYMENT_TRANSACTION = "INSERT INTO payment_transaction SELECT * FROM payment_transaction_tmp;";
    private static final String SQL_DROP_TEMP_PAYMENT_TRANSACTION = "DROP TABLE payment_transaction_tmp;";

    private static final String SQL_CREATE_NO_FK_EMPLOYEE_TIPS = "CREATE TABLE employee_tips( guid TEXT PRIMARY KEY NOT NULL, parent_guid TEXT, employee_id TEXT, shift_id TEXT NOT NULL, order_id TEXT, payment_transaction_id TEXT, create_time INTEGER NOT NULL, amount TEXT NOT NULL, comment TEXT, payment_type INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";
    private static final String SQL_RENAME_EMPLOYEE_TIPS = "ALTER TABLE employee_tips RENAME TO employee_tips_tmp;";
    private static final String SQL_CREATE_EMPLOYEE_TIPS = "CREATE TABLE employee_tips( guid TEXT PRIMARY KEY NOT NULL, parent_guid TEXT, employee_id TEXT, shift_id TEXT NOT NULL, order_id TEXT, payment_transaction_id TEXT, create_time INTEGER NOT NULL, amount TEXT NOT NULL, comment TEXT, payment_type INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(parent_guid) REFERENCES employee_tips(guid),FOREIGN KEY(employee_id) REFERENCES employee(guid),FOREIGN KEY(shift_id) REFERENCES shift(guid),FOREIGN KEY(order_id) REFERENCES sale_order(guid) ON DELETE CASCADE,FOREIGN KEY(payment_transaction_id) REFERENCES payment_transaction(guid))";
    private static final String SQL_COPY_EMPLOYEE_TIPS = "INSERT INTO employee_tips SELECT * FROM employee_tips_tmp;";
    private static final String SQL_DROP_TEMP_EMPLOYEE_TIPS = "DROP TABLE employee_tips_tmp;";

    private static final String SQL_CREATE_NO_FK_EMPLOYEE_COMMISSIONS = "CREATE TABLE employee_commissions( guid TEXT PRIMARY KEY NOT NULL, employee_id TEXT NOT NULL, shift_id TEXT NOT NULL, order_id TEXT NOT NULL, create_time INTEGER NOT NULL, amount TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";
    private static final String SQL_RENAME_EMPLOYEE_COMMISSIONS = "ALTER TABLE employee_commissions RENAME TO employee_commissions_tmp;";
    private static final String SQL_CREATE_EMPLOYEE_COMMISSIONS = "CREATE TABLE employee_commissions( guid TEXT PRIMARY KEY NOT NULL, employee_id TEXT NOT NULL, shift_id TEXT NOT NULL, order_id TEXT NOT NULL, create_time INTEGER NOT NULL, amount TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(employee_id) REFERENCES employee(guid),FOREIGN KEY(shift_id) REFERENCES shift(guid),FOREIGN KEY(order_id) REFERENCES sale_order(guid) ON DELETE CASCADE)";
    private static final String SQL_COPY_EMPLOYEE_COMMISSIONS = "INSERT INTO employee_commissions SELECT * FROM employee_commissions_tmp;";
    private static final String SQL_DROP_TEMP_EMPLOYEE_COMMISSIONS = "DROP TABLE employee_commissions_tmp;";

    private static final String SQL_CREATE_NO_FK_UNIT = "CREATE TABLE unit( _id TEXT PRIMARY KEY NOT NULL, item_id TEXT NOT NULL, serial_code TEXT, code_type INTEGER NOT NULL DEFAULT (0), status INTEGER, warranty_period INTEGER, sale_order_item_id TEXT, child_order_item_id TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";
    private static final String SQL_RENAME_UNIT = "ALTER TABLE unit RENAME TO unit_tmp;";
    private static final String SQL_CREATE_UNIT = "CREATE TABLE unit( _id TEXT PRIMARY KEY NOT NULL, item_id TEXT NOT NULL, serial_code TEXT, code_type INTEGER NOT NULL DEFAULT (0), status INTEGER, warranty_period INTEGER, sale_order_item_id TEXT, child_order_item_id TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(item_id) REFERENCES item(guid),FOREIGN KEY(sale_order_item_id) REFERENCES sale_order(guid) ON DELETE CASCADE,FOREIGN KEY(child_order_item_id) REFERENCES sale_order(guid))";
    private static final String SQL_COPY_UNIT = "INSERT INTO unit SELECT * FROM unit_tmp;";
    private static final String SQL_DROP_TEMP_UNIT = "DROP TABLE unit_tmp;";

    private static final String SQL_ADD_COLUMN_BP_DESCRIPTION = "ALTER TABLE bp_description ADD COLUMN sale_order_id TEXT";
    private static final String SQL_SET_COLUMN_BP_DESCRIPTION = "update bp_description set sale_order_id = (select sale_order_item.order_id from sale_order_item where sale_order_item.item_id = bp_description.guid and sale_order_item.parent_guid is null)";

    private static final String SQL_RENAME_BP_DESCRIPTION = "ALTER TABLE bp_description RENAME TO bp_description_tmp;";
    private static final String SQL_CREATE_BP_DESCRIPTION = "CREATE TABLE bp_description( guid TEXT PRIMARY KEY NOT NULL, description TEXT NOT NULL, type INTEGER, is_voided INTEGER NOT NULL DEFAULT (0), is_failed INTEGER NOT NULL DEFAULT (0), order_id INTEGER, sale_order_id TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(sale_order_id) REFERENCES sale_order(guid) ON DELETE CASCADE)";
    private static final String SQL_CREATE_BP_DESCRIPTION_ORDER = "create index idx_bp_description_order on bp_description( sale_order_id)";
    private static final String SQL_COPY_BP_DESCRIPTION = "INSERT INTO bp_description(guid, description, type, is_voided, is_failed, order_id, sale_order_id, is_deleted, update_time, is_draft) SELECT guid, description, type, is_voided, is_failed, order_id, sale_order_id, is_deleted, update_time, is_draft FROM bp_description_tmp;";
    private static final String SQL_DROP_TEMP_BP_DESCRIPTION = "DROP TABLE bp_description_tmp;";

    private static final String SQL_CREATE_EMPLOYEE_TIPS_SHIFT = "create index idx_employee_tips_shift on employee_tips( shift_id)";
    private static final String SQL_CREATE_EMPLOYEE_TIPS_CASHIER = "create index idx_employee_tips_cashier on employee_tips( employee_id)";
    private static final String SQL_CREATE_EMPLOYEE_TIPS_ORDER = "create index idx_employee_tips_order on employee_tips( order_id)";
    private static final String SQL_CREATE_EMPLOYEE_TIPS_PAYMENT = "create index idx_employee_tips_payment on employee_tips( payment_transaction_id)";
    private static final String SQL_CREATE_EMPLOYEE_TIPS_PARENT = "create index idx_employee_tips_parent on employee_tips( parent_guid)";
    private static final String SQL_CREATE_EMPLOYEE_COMMISSIONS_SHIFT = "create index idx_employee_commissions_shift on employee_commissions( shift_id)";
    private static final String SQL_CREATE_EMPLOYEE_COMMISSIONS_CASHIER = "create index idx_employee_commissions_cashier on employee_commissions( employee_id)";
    private static final String SQL_CREATE_EMPLOYEE_COMMISSIONS_ORDER = "create index idx_employee_commissions_order on employee_commissions( order_id)";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION_ORDER_GUID = "create index idx_payment_transaction_order_guid on payment_transaction( order_guid)";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION_PARENT = "create index idx_payment_transaction_parent on payment_transaction( parent_guid)";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION_STATUS = "create index idx_payment_transaction_status on payment_transaction( status)";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION_TYPE = "create index idx_payment_transaction_type on payment_transaction( unitsLabel)";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION_OPERATOR = "create index idx_payment_transaction_operator on payment_transaction( operator_id)";
    private static final String SQL_CREATE_PAYMENT_TRANSACTION_SHIFT = "create index idx_payment_transaction_shift on payment_transaction( shift_guid)";
    private static final String SQL_CREATE_SALE_ORDER_CREATE_TIME = "create index idx_sale_order_create_time on sale_order( create_time)";
    private static final String SQL_CREATE_SALE_ORDER_OPERATOR = "create index idx_sale_order_operator on sale_order( operator_guid)";
    private static final String SQL_CREATE_SALE_ORDER_CUSTOMER = "create index idx_sale_order_customer on sale_order( customer_guid)";
    private static final String SQL_CREATE_SALE_ORDER_REGISTER = "create index idx_sale_order_register on sale_order( register_id)";
    private static final String SQL_CREATE_SALE_ORDER_SHIFT = "create index idx_sale_order_shift on sale_order( shift_guid)";
    private static final String SQL_CREATE_SALE_ORDER_STATUS = "create index idx_sale_order_status on sale_order( status)";
    private static final String SQL_CREATE_SALE_ORDER_PARENT = "create index idx_sale_order_parent on sale_order( parent_id)";
    private static final String SQL_CREATE_SALE_ORDER_ITEM_ORDER = "create index idx_sale_order_item_order on sale_order_item( order_id)";
    private static final String SQL_CREATE_SALE_ORDER_ITEM_ITEM_GUID = "create index idx_sale_order_item_item_guid on sale_order_item( item_id)";
    private static final String SQL_CREATE_SALE_ORDER_ITEM_PARENT = "create index idx_sale_order_item_parent on sale_order_item( parent_guid)";
    private static final String SQL_CREATE_SALE_ORDER_ITEM_ADDON_ITEM_GUID = "create index idx_sale_order_item_addon_item_guid on sale_order_item_addon( item_guid)";
    private static final String SQL_CREATE_SALE_ORDER_ITEM_ADDON_ADDON_GUID = "create index idx_sale_order_item_addon_addon_guid on sale_order_item_addon( addon_id)";
    private static final String SQL_CREATE_UNIT_ITEM = "create index idx_unit_item on unit( item_id)";
    private static final String SQL_CREATE_UNIT_SALE_ITEM = "create index idx_unit_sale_item on unit( sale_order_item_id)";
    private static final String SQL_CREATE_UNIT_CHILD_SALE_ITEM = "create index idx_unit_child_sale_item on unit( child_order_item_id)";


    private static final String SQL_DROP_PREPAID_ORDER_VIEW = "drop view if exists prepaid_order_view";
    private static final String SQL_DROP_SO_ITEMS_VIEW = "drop view if exists so_items_view";
    private static final String SQL_DROP_SO_SALE_REPORTS_ITEMS_VIEW = "drop view if exists so_sale_reports_items_view";
    private static final String SQL_CREATE_PREPAID_ORDER_VIEW = "CREATE VIEW prepaid_order_view AS SELECT  sale_order_table.guid as sale_order_table_guid, sale_order_table.order_type as sale_order_table_order_type, sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, bill_payment_description_table.guid as bill_payment_description_table_guid, bill_payment_description_table.description as bill_payment_description_table_description, bill_payment_description_table.type as bill_payment_description_table_type, bill_payment_description_table.is_voided as bill_payment_description_table_is_voided, bill_payment_description_table.is_failed as bill_payment_description_table_is_failed, bill_payment_description_table.order_id as bill_payment_description_table_order_id, bill_payment_description_table.sale_order_id as bill_payment_description_table_sale_order_id, bill_payment_description_table.is_deleted as bill_payment_description_table_is_deleted, bill_payment_description_table.update_time as bill_payment_description_table_update_time, bill_payment_description_table.is_draft as bill_payment_description_table_is_draft FROM sale_order AS sale_order_table LEFT OUTER JOIN sale_order_item AS sale_item_table ON sale_item_table.order_id = sale_order_table.guid and sale_item_table.is_deleted = 0 LEFT OUTER JOIN bp_description AS bill_payment_description_table ON bill_payment_description_table.guid = sale_item_table.item_id and bill_payment_description_table.is_deleted = 0 where sale_order_table.is_deleted = 0";
    private static final String SQL_CREATE_SO_ITEMS_VIEW = "CREATE VIEW so_items_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.transaction_fee as sale_order_table_transaction_fee, item_table.guid as item_table_guid, item_table.category_id as item_table_category_id, item_table.description as item_table_description, item_table.code as item_table_code, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.price_type as item_table_price_type, item_table.serializable as item_table_serializable, item_table.sale_price as item_table_sale_price, item_table.units_label as item_table_units_label, item_table.stock_tracking as item_table_stock_tracking, item_table.active_status as item_table_active_status, item_table.discountable as item_table_discountable, item_table.discount as item_table_discount, item_table.discount_type as item_table_discount_type, item_table.taxable as item_table_taxable, item_table.cost as item_table_cost, item_table.minimum_qty as item_table_minimum_qty, item_table.recommended_qty as item_table_recommended_qty, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.tax_group_guid as item_table_tax_group_guid, item_table.tmp_available_qty as item_table_tmp_available_qty, item_table.order_num as item_table_order_num, item_table.default_modifier_guid as item_table_default_modifier_guid, item_table.printer_alias_guid as item_table_printer_alias_guid, item_table.button_view as item_table_button_view, item_table.has_notes as item_table_has_notes, item_table.code_type as item_table_code_type, item_table.eligible_for_commission as item_table_eligible_for_commission, item_table.commission as item_table_commission, item_table.is_deleted as item_table_is_deleted, item_table.update_time as item_table_update_time, item_table.is_draft as item_table_is_draft, bill_payment_description_table.guid as bill_payment_description_table_guid, bill_payment_description_table.description as bill_payment_description_table_description, bill_payment_description_table.type as bill_payment_description_table_type, bill_payment_description_table.is_voided as bill_payment_description_table_is_voided, bill_payment_description_table.is_failed as bill_payment_description_table_is_failed, bill_payment_description_table.order_id as bill_payment_description_table_order_id, bill_payment_description_table.sale_order_id as bill_payment_description_table_sale_order_id, bill_payment_description_table.is_deleted as bill_payment_description_table_is_deleted, bill_payment_description_table.update_time as bill_payment_description_table_update_time, bill_payment_description_table.is_draft as bill_payment_description_table_is_draft, sale_addon_table.guid as sale_addon_table_guid, sale_addon_table.addon_id as sale_addon_table_addon_id, sale_addon_table.item_guid as sale_addon_table_item_guid, sale_addon_table.extra_cost as sale_addon_table_extra_cost, sale_addon_table.addon_type as sale_addon_table_addon_type, sale_addon_table.is_deleted as sale_addon_table_is_deleted, sale_addon_table.update_time as sale_addon_table_update_time, sale_addon_table.is_draft as sale_addon_table_is_draft, modifier_table.title as modifier_table_title FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 LEFT OUTER JOIN item AS item_table ON item_table.guid = sale_item_table.item_id LEFT OUTER JOIN bp_description AS bill_payment_description_table ON bill_payment_description_table.guid = sale_item_table.item_id LEFT OUTER JOIN sale_order_item_addon AS sale_addon_table ON sale_addon_table.item_guid = sale_item_table.sale_order_item_id and sale_addon_table.is_deleted = 0 LEFT OUTER JOIN items_modifier AS modifier_table ON modifier_table.modifier_guid = sale_addon_table.addon_id and modifier_table.is_deleted = 0 where sale_item_table.is_deleted = 0";
    private static final String SQL_CREATE_SO_SALE_REPORTS_ITEMS_VIEW = "CREATE VIEW so_sale_reports_items_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.status as sale_order_table_status, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.transaction_fee as sale_order_table_transaction_fee, item_table.description as item_table_description, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.cost as item_table_cost, item_table.category_id as item_table_category_id, category_table.title as category_table_title, category_table.department_guid as category_table_department_guid, department_table.title as department_table_title, bill_payment_description_table.guid as bill_payment_description_table_guid, bill_payment_description_table.description as bill_payment_description_table_description, bill_payment_description_table.type as bill_payment_description_table_type, bill_payment_description_table.is_voided as bill_payment_description_table_is_voided, bill_payment_description_table.is_failed as bill_payment_description_table_is_failed, bill_payment_description_table.order_id as bill_payment_description_table_order_id, bill_payment_description_table.sale_order_id as bill_payment_description_table_sale_order_id, bill_payment_description_table.is_deleted as bill_payment_description_table_is_deleted, bill_payment_description_table.update_time as bill_payment_description_table_update_time, bill_payment_description_table.is_draft as bill_payment_description_table_is_draft FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 LEFT OUTER JOIN item AS item_table ON item_table.guid = sale_item_table.item_id LEFT OUTER JOIN category AS category_table ON category_table.guid = item_table.category_id LEFT OUTER JOIN department AS department_table ON department_table.guid = category_table.department_guid LEFT OUTER JOIN bp_description AS bill_payment_description_table ON bill_payment_description_table.guid = sale_item_table.item_id where sale_item_table.is_deleted = 0";
    private static final String SQL_DROP_SO_VIEW = "drop view if exists so_view";
    private static final String SQL_CREATE_SO_VIEW = "CREATE VIEW so_view AS SELECT  sale_order_table.guid as sale_order_table_guid, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.operator_guid as sale_order_table_operator_guid, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.customer_guid as sale_order_table_customer_guid, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.status as sale_order_table_status, sale_order_table.hold_name as sale_order_table_hold_name, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.print_seq_num as sale_order_table_print_seq_num, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.parent_id as sale_order_table_parent_id, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.tml_total_price as sale_order_table_tml_total_price, sale_order_table.tml_total_tax as sale_order_table_tml_total_tax, sale_order_table.tml_total_discount as sale_order_table_tml_total_discount, sale_order_table.is_tipped as sale_order_table_is_tipped, sale_order_table.kitchen_print_status as sale_order_table_kitchen_print_status, sale_order_table.transaction_fee as sale_order_table_transaction_fee, sale_order_table.is_deleted as sale_order_table_is_deleted, sale_order_table.update_time as sale_order_table_update_time, sale_order_table.is_draft as sale_order_table_is_draft, operator_table.first_name as operator_table_first_name, operator_table.last_name as operator_table_last_name, customer_table.guid as customer_table_guid, customer_table.first_name as customer_table_first_name, customer_table.last_name as customer_table_last_name, customer_table.phone as customer_table_phone, customer_table.email as customer_table_email, register_table.title as register_table_title, tips_table.amount as tips_table_amount, tips_table.create_time as tips_table_create_time FROM sale_order AS sale_order_table LEFT OUTER JOIN employee AS operator_table ON operator_table.guid = sale_order_table.operator_guid LEFT OUTER JOIN customer AS customer_table ON customer_table.guid = sale_order_table.customer_guid JOIN register AS register_table ON register_table._id = sale_order_table.register_id and register_table.is_deleted = 0 LEFT OUTER JOIN employee_tips AS tips_table ON tips_table.order_id = sale_order_table.guid and tips_table.is_deleted = 0 where sale_order_table.is_deleted = 0";
    private static final String SQL_CREATE_UNITS_VIEW = "CREATE VIEW units_view AS SELECT  unit_table._id as _id, unit_table.item_id as unit_table_item_id, unit_table.serial_code as unit_table_serial_code, unit_table.code_type as unit_table_code_type, unit_table.status as unit_table_status, unit_table.warranty_period as unit_table_warranty_period, unit_table.sale_order_item_id as unit_table_sale_order_item_id, unit_table.child_order_item_id as unit_table_child_order_item_id, unit_table.is_deleted as unit_table_is_deleted, unit_table.update_time as unit_table_update_time, unit_table.is_draft as unit_table_is_draft, sale_order_table.create_time as sale_order_table_create_time FROM unit AS unit_table LEFT OUTER JOIN sale_order AS sale_order_table ON sale_order_table.guid = unit_table.sale_order_item_id and sale_order_table.is_deleted = 0 where unit_table.is_deleted = 0";

    private static final String SQL_CREATE_ITEM_MOVEMENT_CREATE_TIME = "create index idx_item_movement_create_time on item_movement(create_time)";

    private static final String SQL_CREATE_TRIGGER_UNLINK_OLD_REFUND_UNITS = "CREATE TRIGGER IF NOT EXISTS trigger_unlink_old_refund_units BEFORE DELETE ON sale_order FOR EACH ROW " +
            " WHEN OLD.parent_id IS NOT NULL " +
            " BEGIN  UPDATE unit SET child_order_item_id =  NULL  WHERE child_order_item_id = OLD.guid;END";

    private static final String SQL_CREATE_TRIGGER_FIX_SALE_ORDER_UNITS = "CREATE TRIGGER IF NOT EXISTS trigger_fix_sale_order_units BEFORE DELETE ON sale_order FOR EACH ROW" +
            " WHEN OLD.parent_id IS NULL AND OLD.status = 1 " +
            "BEGIN  UPDATE unit SET sale_order_item_id =  NULL  WHERE sale_order_item_id = OLD.guid AND status != 3;END";

    private static final String SQL_CREATE_UPDATE_TIME = "create table update_time( table_id INTEGER PRIMARY KEY, guid TEXT NOT NULL, update_time INTEGER NOT NULL)";

    private static final String SQL_INSERT_SELECT_UPDATE_TIME_REGISTER = "insert into update_time(table_id, update_time, guid) select 0,  update_time, _id from register where update_time is not null order by update_time DESC, _id DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_PRINTER_ALIAS = "insert into update_time(table_id, update_time, guid) select 1,  update_time, guid from printer_alias_table where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_CUSTOMER = "insert into update_time(table_id, update_time, guid) select 3,  update_time, guid from customer where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE = "insert into update_time(table_id, update_time, guid) select 4,  update_time, guid from employee where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_PERMISSION = "insert into update_time(table_id, update_time, guid) select 5,  update_time, permission_id from employee_permission where update_time is not null order by update_time DESC, permission_id DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_TIMESHEET = "insert into update_time(table_id, update_time, guid) select 6,  update_time, guid from employee_timesheet where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_SHIFT = "insert into update_time(table_id, update_time, guid) select 7,  update_time, guid from shift where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_CASH_DRAWER_MOVEMENT = "insert into update_time(table_id, update_time, guid) select 2,  update_time, guid from cashdrawer_tr where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_TAX_GROUP = "insert into update_time(table_id, update_time, guid) select 8,  update_time, guid from tax_group where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_DEPARTMENT = "insert into update_time(table_id, update_time, guid) select 9,  update_time, guid from department where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_CATEGORY = "insert into update_time(table_id, update_time, guid) select 10,  update_time, guid from category where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_ITEM = "insert into update_time(table_id, update_time, guid) select 11,  update_time, guid from item where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_MODIFIER = "insert into update_time(table_id, update_time, guid) select 12,  update_time, modifier_guid from items_modifier where update_time is not null order by update_time DESC, modifier_guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_ITEM_MOVEMENT = "insert into update_time(table_id, update_time, guid) select 13,  update_time, guid from item_movement where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_SALE_ORDER = "insert into update_time(table_id, update_time, guid) select 15,  update_time, guid from sale_order where update_time is not null and parent_id is null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_REFUND_ORDER = "insert into update_time(table_id, update_time, guid) select 16,  update_time, guid from sale_order where update_time is not null and parent_id is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_BILL_PAYMENT_DESCRIPTION = "insert into update_time(table_id, update_time, guid) select 22,  update_time, guid from bp_description where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_SALE_ITEM = "insert into update_time(table_id, update_time, guid) select 17,  update_time, sale_order_item_id from sale_order_item where update_time is not null and parent_guid is null order by update_time DESC, sale_order_item_id DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_REFUND_ITEM = "insert into update_time(table_id, update_time, guid) select 18,  update_time, sale_order_item_id from sale_order_item where update_time is not null and parent_guid is not null order by update_time DESC, sale_order_item_id DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_SALE_ADDON = "insert into update_time(table_id, update_time, guid) select 19,  update_time, guid from sale_order_item_addon where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_PAYMENT_TRANSACTION = "insert into update_time(table_id, update_time, guid) select 20,  update_time, guid from payment_transaction where update_time is not null and parent_guid is null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_REFUND_PAYMENT_TRANSACTION = "insert into update_time(table_id, update_time, guid) select 21,  update_time, guid from payment_transaction where update_time is not null and parent_guid is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_CREDIT_RECEIPT = "insert into update_time(table_id, update_time, guid) select 23,  update_time, guid from credit_receipt_table where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_TIPS = "insert into update_time(table_id, update_time, guid) select 24,  update_time, guid from employee_tips where update_time is not null and parent_guid is null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_REFUND_EMPLOYEE_TIPS = "insert into update_time(table_id, update_time, guid) select 25,  update_time, guid from employee_tips where update_time is not null and parent_guid is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_COMMISSIONS = "insert into update_time(table_id, update_time, guid) select 26,  update_time, guid from employee_commissions where update_time is not null order by update_time DESC, guid DESC limit 1";
    private static final String SQL_INSERT_SELECT_UPDATE_TIME_UNIT = "insert into update_time(table_id, update_time, guid) select 14,  update_time, _id from unit where update_time is not null order by update_time DESC, _id DESC limit 1";

    public static void update6_1to6_2(SQLiteDatabase db){

        db.execSQL(SQL_RENAME_UNIT);
        db.execSQL(SQL_CREATE_NO_FK_UNIT);
        db.execSQL(SQL_COPY_UNIT);
        db.execSQL(SQL_DROP_TEMP_UNIT);

        db.execSQL(SQL_RENAME_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_CREATE_NO_FK_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_COPY_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_DROP_TEMP_EMPLOYEE_COMMISSIONS);

        db.execSQL(SQL_RENAME_EMPLOYEE_TIPS);
        db.execSQL(SQL_CREATE_NO_FK_EMPLOYEE_TIPS);
        db.execSQL(SQL_COPY_EMPLOYEE_TIPS);
        db.execSQL(SQL_DROP_TEMP_EMPLOYEE_TIPS);

        db.execSQL(SQL_RENAME_PAYMENT_TRANSACTION);
        db.execSQL(SQL_CREATE_NO_FK_PAYMENT_TRANSACTION);
        db.execSQL(SQL_COPY_PAYMENT_TRANSACTION);
        db.execSQL(SQL_DROP_TEMP_PAYMENT_TRANSACTION);

        db.execSQL(SQL_RENAME_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_CREATE_NO_FK_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_COPY_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_DROP_TEMP_SALE_ORDER_ITEM_ADDON);

        db.execSQL(SQL_RENAME_SALE_ORDER_ITEM);
        db.execSQL(SQL_CREATE_NO_FK_SALE_ORDER_ITEM);
        db.execSQL(SQL_COPY_SALE_ORDER_ITEM);
        db.execSQL(SQL_DROP_TEMP_SALE_ORDER_ITEM);


        db.execSQL(SQL_RENAME_SALE_ORDER);
        db.execSQL(SQL_CREATE_SALE_ORDER);
        db.execSQL(SQL_COPY_SALE_ORDER);
        db.execSQL(SQL_DROP_TEMP_SALE_ORDER);

        db.execSQL(SQL_RENAME_SALE_ORDER_ITEM);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM);
        db.execSQL(SQL_COPY_SALE_ORDER_ITEM);
        db.execSQL(SQL_DROP_TEMP_SALE_ORDER_ITEM);

        db.execSQL(SQL_RENAME_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_COPY_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_DROP_TEMP_SALE_ORDER_ITEM_ADDON);

        db.execSQL(SQL_RENAME_PAYMENT_TRANSACTION);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION);
        db.execSQL(SQL_COPY_PAYMENT_TRANSACTION);
        db.execSQL(SQL_DROP_TEMP_PAYMENT_TRANSACTION);

        db.execSQL(SQL_RENAME_EMPLOYEE_TIPS);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS);
        db.execSQL(SQL_COPY_EMPLOYEE_TIPS);
        db.execSQL(SQL_DROP_TEMP_EMPLOYEE_TIPS);

        db.execSQL(SQL_RENAME_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_COPY_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_DROP_TEMP_EMPLOYEE_COMMISSIONS);

        db.execSQL(SQL_RENAME_UNIT);
        db.execSQL(SQL_CREATE_UNIT);
        db.execSQL(SQL_COPY_UNIT);
        db.execSQL(SQL_DROP_TEMP_UNIT);

        db.execSQL(SQL_CREATE_UNIT_ITEM);
        db.execSQL(SQL_CREATE_UNIT_SALE_ITEM);
        db.execSQL(SQL_CREATE_UNIT_CHILD_SALE_ITEM);
        db.execSQL(SQL_CREATE_SALE_ORDER_CREATE_TIME);
        db.execSQL(SQL_CREATE_SALE_ORDER_OPERATOR);
        db.execSQL(SQL_CREATE_SALE_ORDER_CUSTOMER);
        db.execSQL(SQL_CREATE_SALE_ORDER_REGISTER);
        db.execSQL(SQL_CREATE_SALE_ORDER_SHIFT);
        db.execSQL(SQL_CREATE_SALE_ORDER_STATUS);
        db.execSQL(SQL_CREATE_SALE_ORDER_PARENT);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_ORDER);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_ITEM_GUID);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_PARENT);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_ADDON_ITEM_GUID);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_ADDON_ADDON_GUID);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_ORDER_GUID);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_PARENT);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_STATUS);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_TYPE);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_OPERATOR);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_CASHIER);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_ORDER);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_PAYMENT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_PARENT);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS_CASHIER);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS_ORDER);

        db.execSQL(SQL_ADD_COLUMN_BP_DESCRIPTION);
        db.execSQL(SQL_SET_COLUMN_BP_DESCRIPTION);

        db.execSQL(SQL_RENAME_BP_DESCRIPTION);
        db.execSQL(SQL_CREATE_BP_DESCRIPTION);
        db.execSQL(SQL_CREATE_BP_DESCRIPTION_ORDER);
        db.execSQL(SQL_COPY_BP_DESCRIPTION);
        db.execSQL(SQL_DROP_TEMP_BP_DESCRIPTION);

        db.execSQL(SQL_CREATE_ITEM_MOVEMENT_CREATE_TIME);

        db.execSQL(SQL_CREATE_TRIGGER_UNLINK_OLD_REFUND_UNITS);
        db.execSQL(SQL_CREATE_TRIGGER_FIX_SALE_ORDER_UNITS);
        db.execSQL(SQL_DROP_SO_VIEW);
        db.execSQL(SQL_DROP_PREPAID_ORDER_VIEW);
        db.execSQL(SQL_DROP_SO_ITEMS_VIEW);
        db.execSQL(SQL_DROP_SO_SALE_REPORTS_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_PREPAID_ORDER_VIEW);
        db.execSQL(SQL_CREATE_SO_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_SO_SALE_REPORTS_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_SO_VIEW);
        db.execSQL(SQL_CREATE_UNITS_VIEW);

        db.execSQL(SQL_CREATE_UPDATE_TIME);

        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_REGISTER);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_PRINTER_ALIAS);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_CUSTOMER);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_PERMISSION);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_TIMESHEET);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_SHIFT);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_CASH_DRAWER_MOVEMENT);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_TAX_GROUP);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_DEPARTMENT);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_CATEGORY);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_ITEM);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_MODIFIER);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_ITEM_MOVEMENT);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_SALE_ORDER);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_REFUND_ORDER);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_BILL_PAYMENT_DESCRIPTION);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_SALE_ITEM);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_REFUND_ITEM);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_SALE_ADDON);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_PAYMENT_TRANSACTION);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_REFUND_PAYMENT_TRANSACTION);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_CREDIT_RECEIPT);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_TIPS);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_REFUND_EMPLOYEE_TIPS);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_INSERT_SELECT_UPDATE_TIME_UNIT);
    }

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_1to6_2(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION6_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_2;
    }
}
