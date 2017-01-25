package com.kaching123.tcr.store.migration.version.to5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

/**
 * Created by pkabakov on 06.08.2014.
 */
public class Update4to5 implements IUpdateContainer {

    static final String SQL_DROP_TABLE_UNIT = "drop table if exists unit";
    static final String SQL_DROP_TABLE_DEPARTMENT = "drop table if exists department";
    static final String SQL_DROP_TABLE_CATEGORY = "drop table if exists category";
    static final String SQL_DROP_TABLE_ITEM = "drop table if exists item";
    static final String SQL_DROP_TABLE_ITEM_MOVEMENT = "drop table if exists item_movement";
    static final String SQL_DROP_TABLE_ITEMS_MODIFIER = "drop table if exists items_modifier";
    static final String SQL_DROP_TABLE_SALE_ORDER = "drop table if exists sale_order";
    static final String SQL_DROP_TABLE_SALE_ORDER_ITEM = "drop table if exists sale_order_item";
    static final String SQL_DROP_TABLE_SALE_ORDER_ITEM_ADDON = "drop table if exists sale_order_item_addon";
    static final String SQL_DROP_TABLE_EMPLOYEE = "drop table if exists employee";
    static final String SQL_DROP_TABLE_EMPLOYEE_PERMISSION = "drop table if exists employee_permission";
    static final String SQL_DROP_TABLE_SHIFT = "drop table if exists shift";
    static final String SQL_DROP_TABLE_CASHDRAWER_TR = "drop table if exists cashdrawer_tr";
    static final String SQL_DROP_TABLE_PAYMENT_TRANSACTION = "drop table if exists payment_transaction";
    static final String SQL_DROP_TABLE_EMPLOYEE_TIMESHEET = "drop table if exists employee_timesheet";
    static final String SQL_DROP_TABLE_TAX_GROUP = "drop table if exists tax_group";
    static final String SQL_DROP_TABLE_REGISTER = "drop table if exists register";
    static final String SQL_DROP_TABLE_BP_DESCRIPTION = "drop table if exists bp_description";
    static final String SQL_DROP_TABLE_CUSTOMER = "drop table if exists customer";
    static final String SQL_DROP_TABLE_PRINTER_ALIAS_TABLE = "drop table if exists printer_alias_table";
    static final String SQL_DROP_TABLE_CREDIT_RECEIPT_TABLE = "drop table if exists credit_receipt_table";
    static final String SQL_DROP_TABLE_EMPLOYEE_TIPS = "drop table if exists employee_tips";
    static final String SQL_DROP_TABLE_ACTIVATION_CARRIER = "drop table if exists activation_carrier";
    static final String SQL_DROP_TABLE_EMPLOYEE_COMMISSIONS = "drop table if exists employee_commissions";
    static final String SQL_DROP_TABLE_TOPUP = "drop table if exists wireless_top_up_item;";
    static final String SQL_DROP_TABLE_PIN = "drop table if exists wireless_pin_item;";



    static final String SQL_CREATE_UNIT = "CREATE TABLE unit( _id TEXT PRIMARY KEY NOT NULL, item_id TEXT NOT NULL, serial_code TEXT, code_type INTEGER NOT NULL DEFAULT (0), status INTEGER, warranty_period INTEGER, sale_order_item_id TEXT, child_order_item_id TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(item_id) REFERENCES item(guid),FOREIGN KEY(sale_order_item_id) REFERENCES sale_order(guid),FOREIGN KEY(child_order_item_id) REFERENCES sale_order(guid))";

    static final String SQL_CREATE_DEPARTMENT = "create table department( _id INTEGER PRIMARY KEY AUTOINCREMENT, guid TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, title TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_CATEGORY = "CREATE TABLE category( _id INTEGER PRIMARY KEY AUTOINCREMENT, guid TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, department_guid TEXT NOT NULL, title TEXT NOT NULL, image TEXT, order_num INTEGER, eligible_for_commission INTEGER NOT NULL DEFAULT (1), commission TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(department_guid) REFERENCES department(guid))";

    static final String SQL_CREATE_ITEM = "create table item( guid TEXT PRIMARY KEY NOT NULL, category_id TEXT NOT NULL, description TEXT, code TEXT, ean_code TEXT, product_code TEXT, price_type INTEGER, serializable INTEGER NOT NULL DEFAULT (0), sale_price TEXT, units_label TEXT NOT NULL, stock_tracking INTEGER, active_status INTEGER, discountable INTEGER, discount TEXT, discount_type INTEGER, taxable INTEGER, cost TEXT, minimum_qty TEXT, recommended_qty TEXT, UPDATE_QTY_FLAG TEXT NOT NULL, tax_group_guid TEXT, tmp_available_qty TEXT, order_num INTEGER NOT NULL, default_modifier_guid TEXT, printer_alias_guid TEXT, button_view INTEGER, has_notes INTEGER, code_type INTEGER, eligible_for_commission INTEGER NOT NULL DEFAULT (1), commission TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(category_id) REFERENCES category(guid),FOREIGN KEY(tax_group_guid) REFERENCES tax_group(guid),FOREIGN KEY(printer_alias_guid) REFERENCES printer_alias_table(guid))";

    static final String SQL_CREATE_ITEM_MOVEMENT = "CREATE TABLE item_movement( guid TEXT NOT NULL, item_guid TEXT NOT NULL, qty TEXT NOT NULL, ITEM_UPDATE_QTY_FLAG INTEGER NOT NULL, manual INTEGER, create_time INTEGER NOT NULL, tmp_available_qty TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0), PRIMARY KEY( guid, item_guid),FOREIGN KEY(item_guid) REFERENCES item(guid))";

    static final String SQL_CREATE_ITEMS_MODIFIER = "CREATE TABLE items_modifier( modifier_guid TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, unitsLabel INTEGER NOT NULL, item_guid TEXT NOT NULL, extra_cost TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(item_guid) REFERENCES item(guid))";

    static final String SQL_CREATE_SALE_ORDER = "CREATE TABLE sale_order( guid TEXT PRIMARY KEY NOT NULL, create_time INTEGER NOT NULL, operator_guid TEXT NOT NULL, shift_guid TEXT NOT NULL, customer_guid TEXT, discount TEXT, discount_type INTEGER, status INTEGER NOT NULL, hold_name TEXT, taxable INTEGER, print_seq_num INTEGER, register_id INTEGER NOT NULL, parent_id TEXT, order_type INTEGER, tml_total_price TEXT, tml_total_tax TEXT, tml_total_discount TEXT, is_tipped INTEGER NOT NULL DEFAULT (0), kitchen_print_status INTEGER, transaction_fee TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(operator_guid) REFERENCES employee(guid),FOREIGN KEY(shift_guid) REFERENCES shift(guid),FOREIGN KEY(customer_guid) REFERENCES customer(guid),FOREIGN KEY(register_id) REFERENCES register(_id),FOREIGN KEY(parent_id) REFERENCES sale_order(guid))";

    static final String SQL_CREATE_SALE_ORDER_ITEM = "CREATE TABLE sale_order_item( sale_order_item_id TEXT PRIMARY KEY NOT NULL, order_id TEXT NOT NULL, item_id TEXT NOT NULL, quantity TEXT, kitchen_printed_qty TEXT DEFAULT ('0.000'), price TEXT NOT NULL, price_type INTEGER, discountable INTEGER, discount TEXT, discount_type INTEGER, taxable INTEGER, tax TEXT, sequence INTEGER, parent_guid TEXT, final_gross_price TEXT, final_tax TEXT, final_discount TEXT, tmp_refund_quantity TEXT, notes TEXT, has_notes INTEGER, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0), FOREIGN KEY(order_id) REFERENCES sale_order(guid),FOREIGN KEY(parent_guid) REFERENCES sale_order_item(sale_order_item_id))";

    static final String SQL_CREATE_SALE_ORDER_ITEM_ADDON = "CREATE TABLE sale_order_item_addon( guid TEXT PRIMARY KEY NOT NULL, addon_id TEXT NOT NULL, item_guid TEXT NOT NULL, extra_cost TEXT NOT NULL, addon_type INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(addon_id) REFERENCES items_modifier(modifier_guid),FOREIGN KEY(item_guid) REFERENCES sale_order_item(sale_order_item_id))";

    static final String SQL_CREATE_EMPLOYEE = "create table employee( _id INTEGER PRIMARY KEY AUTOINCREMENT, guid TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, first_name TEXT NOT NULL, last_name TEXT, login TEXT NOT NULL, password TEXT NOT NULL, street TEXT, complementary TEXT, city TEXT, state TEXT, country TEXT, zip TEXT, phone TEXT, email TEXT, sex INTEGER NOT NULL, hire_date INTEGER NOT NULL, fire_date INTEGER, status INTEGER NOT NULL, hourly_rate TEXT, shop_id INTEGER NOT NULL, tips_eligible INTEGER NOT NULL DEFAULT (0), eligible_for_commission INTEGER NOT NULL DEFAULT (1), commission TEXT, is_merchant INTEGER DEFAULT (0), is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_EMPLOYEE_PERMISSION = "create table employee_permission( user_guid TEXT NOT NULL, permission_id INTEGER NOT NULL, enabled INTEGER NOT NULL DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0), PRIMARY KEY( user_guid, permission_id),FOREIGN KEY(user_guid) REFERENCES employee(guid))";

    static final String SQL_CREATE_SHIFT = "CREATE TABLE shift( guid TEXT PRIMARY KEY NOT NULL, start_time INTEGER NOT NULL, end_time INTEGER, open_manager_id TEXT NOT NULL, close_manager_id TEXT, register_id INTEGER NOT NULL, open_amount TEXT, close_amount TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(open_manager_id) REFERENCES employee(guid),FOREIGN KEY(close_manager_id) REFERENCES employee(guid),FOREIGN KEY(register_id) REFERENCES register(_id))";

    static final String SQL_CREATE_CASHDRAWER_TR = "CREATE TABLE cashdrawer_tr( guid TEXT PRIMARY KEY NOT NULL, shift_guid TEXT NOT NULL, manager_guid TEXT NOT NULL, type INTEGER NOT NULL, amount TEXT NOT NULL, MOVEMENT_TIME INTEGER, comment TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(shift_guid) REFERENCES shift(guid),FOREIGN KEY(manager_guid) REFERENCES employee(guid))";

    static final String SQL_CREATE_PAYMENT_TRANSACTION = "CREATE TABLE payment_transaction( guid TEXT PRIMARY KEY NOT NULL, order_guid TEXT NOT NULL, parent_guid TEXT, amount TEXT NOT NULL, status INTEGER NOT NULL, unitsLabel INTEGER NOT NULL, operator_id TEXT NOT NULL, gtw_id INTEGER, gtwp_id TEXT, gtw_preauth_payment_id TEXT, gateway_closed_perauth_guid TEXT, d_reason TEXT, create_time INTEGER NOT NULL, shift_guid TEXT NOT NULL, card_name TEXT, change_amount TEXT, is_preauth INTEGER, balance TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0), last_four TEXT, entry_method INTEGER, application_identifier TEXT, application_cryptogram_type TEXT, authorization_number TEXT, signature_bytes TEXT,FOREIGN KEY(order_guid) REFERENCES sale_order(guid),FOREIGN KEY(parent_guid) REFERENCES payment_transaction(guid),FOREIGN KEY(operator_id) REFERENCES employee(guid),FOREIGN KEY(shift_guid) REFERENCES shift(guid))";

    static final String SQL_CREATE_EMPLOYEE_TIMESHEET = "CREATE TABLE employee_timesheet( guid TEXT PRIMARY KEY NOT NULL, employee_guid TEXT NOT NULL, clock_in INTEGER NOT NULL, clock_out INTEGER, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(employee_guid) REFERENCES employee(guid))";

    static final String SQL_CREATE_TAX_GROUP = "create table tax_group( _id INTEGER PRIMARY KEY AUTOINCREMENT, guid TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, title TEXT NOT NULL, tax TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_REGISTER = "create table register( _id INTEGER PRIMARY KEY, register_serial TEXT NOT NULL, title TEXT, status INTEGER NOT NULL, prepaid_tid INTEGER, blackstone_payment_cid INTEGER, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_BP_DESCRIPTION = "create table bp_description( guid TEXT PRIMARY KEY NOT NULL, description TEXT NOT NULL, type INTEGER, is_voided INTEGER NOT NULL DEFAULT (0), is_failed INTEGER NOT NULL DEFAULT (0), order_id INTEGER, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_CUSTOMER = "create table customer( guid TEXT PRIMARY KEY NOT NULL, first_name TEXT NOT NULL, last_name TEXT, street TEXT, complementary TEXT, city TEXT, state TEXT, country TEXT, zip TEXT, email TEXT, phone TEXT, sex INTEGER NOT NULL, create_time INTEGER NOT NULL, consent_promotions INTEGER NOT NULL DEFAULT (0), is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_PRINTER_ALIAS_TABLE = "create table printer_alias_table( guid TEXT PRIMARY KEY NOT NULL, alias TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0))";

    static final String SQL_CREATE_CREDIT_RECEIPT_TABLE = "CREATE TABLE credit_receipt_table( guid TEXT PRIMARY KEY NOT NULL, cashier_guid TEXT NOT NULL, register_id INTEGER NOT NULL, shift_id TEXT NOT NULL, create_time INTEGER NOT NULL, amount TEXT NOT NULL, print_number INTEGER NOT NULL, expire_time INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(cashier_guid) REFERENCES employee(guid),FOREIGN KEY(register_id) REFERENCES register(_id),FOREIGN KEY(shift_id) REFERENCES shift(guid))";

    static final String SQL_CREATE_EMPLOYEE_TIPS = "CREATE TABLE employee_tips( guid TEXT PRIMARY KEY NOT NULL, parent_guid TEXT, employee_id TEXT, shift_id TEXT NOT NULL, order_id TEXT, payment_transaction_id TEXT, create_time INTEGER NOT NULL, amount TEXT NOT NULL, comment TEXT, payment_type INTEGER NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(parent_guid) REFERENCES employee_tips(guid),FOREIGN KEY(employee_id) REFERENCES employee(guid),FOREIGN KEY(shift_id) REFERENCES shift(guid),FOREIGN KEY(order_id) REFERENCES sale_order(guid),FOREIGN KEY(payment_transaction_id) REFERENCES payment_transaction(guid))";

    static final String SQL_CREATE_ACTIVATION_CARRIER = "create table activation_carrier( ID INTEGER PRIMARY KEY, NAME TEXT NOT NULL, URL TEXT NOT NULL, IS_ACTIVE INTEGER NOT NULL DEFAULT (0))";

    static final String SQL_CREATE_EMPLOYEE_COMMISSIONS = "CREATE TABLE employee_commissions( guid TEXT PRIMARY KEY NOT NULL, employee_id TEXT NOT NULL, shift_id TEXT NOT NULL, order_id TEXT NOT NULL, create_time INTEGER NOT NULL, amount TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(employee_id) REFERENCES employee(guid),FOREIGN KEY(shift_id) REFERENCES shift(guid),FOREIGN KEY(order_id) REFERENCES sale_order(guid))";

    private static final String SQL_REMOVE_INVALID_PAXES = "DELETE FROM pax_table WHERE ip IS NULL OR port IS NULL OR is_deleted = 1;";
    private static final String SQL_RENAME_PAX_TABLE = "ALTER TABLE pax_table RENAME TO pax_table_tmp;";
    private static final String SQL_CREATE_PAX_TABLE = "create table pax_table( guid TEXT PRIMARY KEY NOT NULL, ip TEXT NOT NULL, port INTEGER NOT NULL, mac TEXT, subnet TEXT, gateway TEXT, dhcp TEXT)";
    private static final String SQL_COPY_PAX_TABLE = "INSERT INTO pax_table(guid, ip, port, mac, subnet, gateway, dhcp) SELECT guid, ip, port, mac, subnet, gateway, dhcp FROM pax_table_tmp;";
    private static final String SQL_DROP_TEMP_PAX_TABLE = "DROP TABLE pax_table_tmp;";

    private static final String SQL_REMOVE_INVALID_PRINTERS = "DELETE FROM printer_able WHERE ip IS NULL OR port IS NULL OR is_deleted = 1;";
    private static final String SQL_RENAME_PRINTER_TABLE = "ALTER TABLE printer_able RENAME TO printer_able_tmp;";
    private static final String SQL_CREATE_PRINTER_TABLE = "create table printer_able( guid TEXT PRIMARY KEY NOT NULL, ip TEXT NOT NULL, port INTEGER NOT NULL, alias_guid TEXT, mac TEXT, subnet TEXT, gateway TEXT, dhcp TEXT)";
    private static final String SQL_COPY_PRINTER_TABLE = "INSERT INTO printer_able(guid, ip, port, alias_guid, mac, subnet, gateway, dhcp) SELECT guid, ip, port, alias_guid, mac, subnet, gateway, dhcp FROM printer_able_tmp;";
    private static final String SQL_DROP_TEMP_PRINTER_TABLE = "DROP TABLE printer_able_tmp;";

    private static final String SQL_RENAME_WIRELESS_ITEM = "ALTER TABLE wireless_item RENAME TO wireless_item_tmp;";
    private static final String SQL_CREATE_WIRELESS_ITEM = "create table wireless_item( _id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT, name TEXT, carrierName TEXT, countryCode TEXT, countryName TEXT, type TEXT, url TEXT, useFixedDenominations INTEGER, denominations TEXT, minDenomination TEXT, maxDenomination TEXT, dialCountryCode TEXT)";
    private static final String SQL_COPY_WIRELESS_ITEM = "INSERT INTO wireless_item(_id, code, name, carrierName, countryCode, countryName, type, url, useFixedDenominations, denominations, minDenomination, maxDenomination, dialCountryCode) SELECT _id, code, name, carrierName, countryCode, countryName, type, url, useFixedDenominations, denominations, minDenomination, maxDenomination, dialCountryCode FROM wireless_item_tmp;";
    private static final String SQL_DROP_TEMP_WIRELESS_ITEM = "DROP TABLE wireless_item_tmp;";


    private static final String SQL_ALTER_SQL_COMMAND = "ALTER TABLE sql_command ADD COLUMN api_v INTEGER DEFAULT (2);";
    private static final String SQL_UPDATE_SQL_COMMAND = "UPDATE sql_command SET api_v = 1;";


    static final String SQL_CREATE_UNIT_ITEM = "create index idx_unit_item on unit( item_id)";

    static final String SQL_CREATE_UNIT_SALE_ITEM = "create index idx_unit_sale_item on unit( sale_order_item_id)";

    static final String SQL_CREATE_UNIT_CHILD_SALE_ITEM = "create index idx_unit_child_sale_item on unit( child_order_item_id)";

    static final String SQL_CREATE_CATEGORY_DEPS = "create index idx_category_deps on category( department_guid)";

    static final String SQL_CREATE_ITEM_CATS = "create index idx_item_cats on item( category_id)";

    static final String SQL_CREATE_ITEM_EAN = "create index idx_item_ean on item( ean_code)";

    static final String SQL_CREATE_ITEM_PRODUCT = "create index idx_item_product on item( product_code)";

    static final String SQL_CREATE_ITEM_TAX_GROUP = "create index idx_item_tax_group on item( tax_group_guid)";

    static final String SQL_CREATE_ITEM_PRINTER_ALIAS = "create index idx_item_printer_alias on item( printer_alias_guid)";

    static final String SQL_CREATE_ITEM_MOVEMENT_ITEM = "create index idx_item_movement_item on item_movement( item_guid)";

    static final String SQL_CREATE_ITEMS_MODIFIER_ITEM = "create index idx_items_modifier_item on items_modifier( item_guid)";

    static final String SQL_CREATE_SALE_ORDER_CREATE_TIME = "create index idx_sale_order_create_time on sale_order( create_time)";

    static final String SQL_CREATE_SALE_ORDER_OPERATOR = "create index idx_sale_order_operator on sale_order( operator_guid)";

    static final String SQL_CREATE_SALE_ORDER_CUSTOMER = "create index idx_sale_order_customer on sale_order( customer_guid)";

    static final String SQL_CREATE_SALE_ORDER_REGISTER = "create index idx_sale_order_register on sale_order( register_id)";

    static final String SQL_CREATE_SALE_ORDER_SHIFT = "create index idx_sale_order_shift on sale_order( shift_guid)";

    static final String SQL_CREATE_SALE_ORDER_STATUS = "create index idx_sale_order_status on sale_order( status)";

    static final String SQL_CREATE_SALE_ORDER_PARENT = "create index idx_sale_order_parent on sale_order( parent_id)";

    static final String SQL_CREATE_SALE_ORDER_ITEM_ORDER = "create index idx_sale_order_item_order on sale_order_item( order_id)";

    static final String SQL_CREATE_SALE_ORDER_ITEM_ITEM_GUID = "create index idx_sale_order_item_item_guid on sale_order_item( item_id)";

    static final String SQL_CREATE_SALE_ORDER_ITEM_PARENT = "create index idx_sale_order_item_parent on sale_order_item( parent_guid)";

    static final String SQL_CREATE_SALE_ORDER_ITEM_ADDON_ITEM_GUID = "create index idx_sale_order_item_addon_item_guid on sale_order_item_addon( item_guid)";

    static final String SQL_CREATE_SALE_ORDER_ITEM_ADDON_ADDON_GUID = "create index idx_sale_order_item_addon_addon_guid on sale_order_item_addon( addon_id)";

    static final String SQL_CREATE_EMPLOYEE_FIRST_NAME = "create index idx_employee_first_name on employee( first_name)";

    static final String SQL_CREATE_EMPLOYEE_EMAIL = "create index idx_employee_email on employee( email)";

    static final String SQL_CREATE_EMPLOYEE_PERMISSION_USER = "create index idx_employee_permission_user on employee_permission( user_guid)";

    static final String SQL_CREATE_SHIFT_START_TIME = "create index idx_shift_start_time on shift( start_time)";

    static final String SQL_CREATE_SHIFT_OPEN_MANAGER = "create index idx_shift_open_manager on shift( open_manager_id)";

    static final String SQL_CREATE_SHIFT_CLOSE_MANAGER = "create index idx_shift_close_manager on shift( close_manager_id)";

    static final String SQL_CREATE_SHIFT_REGISTER = "create index idx_shift_register on shift( register_id)";

    static final String SQL_CREATE_CASHDRAWER_TR_SHIFT = "create index idx_cashdrawer_tr_shift on cashdrawer_tr( shift_guid)";

    static final String SQL_CREATE_CASHDRAWER_TR_MANAGER = "create index idx_cashdrawer_tr_manager on cashdrawer_tr( manager_guid)";

    static final String SQL_CREATE_PAYMENT_TRANSACTION_ORDER_GUID = "create index idx_payment_transaction_order_guid on payment_transaction( order_guid)";

    static final String SQL_CREATE_PAYMENT_TRANSACTION_PARENT = "create index idx_payment_transaction_parent on payment_transaction( parent_guid)";

    static final String SQL_CREATE_PAYMENT_TRANSACTION_STATUS = "create index idx_payment_transaction_status on payment_transaction( status)";

    static final String SQL_CREATE_PAYMENT_TRANSACTION_TYPE = "create index idx_payment_transaction_type on payment_transaction( unitsLabel)";

    static final String SQL_CREATE_PAYMENT_TRANSACTION_OPERATOR = "create index idx_payment_transaction_operator on payment_transaction( operator_id)";

    static final String SQL_CREATE_PAYMENT_TRANSACTION_SHIFT = "create index idx_payment_transaction_shift on payment_transaction( shift_guid)";

    static final String SQL_CREATE_EMPLOYEE_TIMESHEET_EMPLOYEE = "create index idx_employee_timesheet_employee on employee_timesheet( employee_guid)";

    static final String SQL_CREATE_EMPLOYEE_TIMESHEET_CLOCK_IN = "create index idx_employee_timesheet_clock_in on employee_timesheet( clock_in)";

    static final String SQL_CREATE_CUSTOMER_LAST_NAME = "create index idx_customer_last_name on customer( last_name)";

    static final String SQL_CREATE_CUSTOMER_EMAIL = "create index idx_customer_email on customer( email)";

    static final String SQL_CREATE_CREDIT_RECEIPT_TABLE_REGISTER = "create index idx_credit_receipt_table_register on credit_receipt_table( register_id)";

    static final String SQL_CREATE_CREDIT_RECEIPT_TABLE_CASHIER = "create index idx_credit_receipt_table_cashier on credit_receipt_table( cashier_guid)";

    static final String SQL_CREATE_CREDIT_RECEIPT_TABLE_SHIFT = "create index idx_credit_receipt_table_shift on credit_receipt_table( shift_id)";

    static final String SQL_CREATE_EMPLOYEE_TIPS_SHIFT = "create index idx_employee_tips_shift on employee_tips( shift_id)";

    static final String SQL_CREATE_EMPLOYEE_TIPS_CASHIER = "create index idx_employee_tips_cashier on employee_tips( employee_id)";

    static final String SQL_CREATE_EMPLOYEE_TIPS_ORDER = "create index idx_employee_tips_order on employee_tips( order_id)";

    static final String SQL_CREATE_EMPLOYEE_TIPS_PAYMENT = "create index idx_employee_tips_payment on employee_tips( payment_transaction_id)";

    static final String SQL_CREATE_EMPLOYEE_TIPS_PARENT = "create index idx_employee_tips_parent on employee_tips( parent_guid)";

    static final String SQL_CREATE_EMPLOYEE_COMMISSIONS_SHIFT = "create index idx_employee_commissions_shift on employee_commissions( shift_id)";

    static final String SQL_CREATE_EMPLOYEE_COMMISSIONS_CASHIER = "create index idx_employee_commissions_cashier on employee_commissions( employee_id)";

    static final String SQL_CREATE_EMPLOYEE_COMMISSIONS_ORDER = "create index idx_employee_commissions_order on employee_commissions( order_id)";

    static final String SQL_CREATE_ITEM_MOVEMENTS = "create index idx_item_movements on item( UPDATE_QTY_FLAG)";

    static final String SQL_CREATE_ITEM_MOVEMENT_MVN_FLAG = "create index idx_item_movement_mvn_flag on item_movement( ITEM_UPDATE_QTY_FLAG)";


    static final String SQL_DROP_SO_WITH_DELETED_ITEM_VIEW = "DROP VIEW IF EXISTS so_with_deleted_item_view";
    static final String SQL_DROP_SALE_ITEMS_COMMISSIONS_VIEW = "drop view if exists sale_items_commissions_view";
    static final String SQL_DROP_CREDIT_RECEIPT_EX_VIEW = "drop view if exists credit_receipt_ex_view";
    static final String SQL_DROP_SO_VIEW = "drop view if exists so_view";
    static final String SQL_DROP_EMPLOYEE_TIMESHEET_VIEW = "drop view if exists employee_timesheet_view";
    static final String SQL_DROP_SHIFT_VIEW = "drop view if exists shift_view";
    static final String SQL_DROP_TIPS_REPORT_VIEW = "drop view if exists tips_report_view";
    static final String SQL_DROP_EXPORT_SOLD_ITEMS = "drop view if exists export_sold_items";
    static final String SQL_DROP_EXPORT_TOP_ITEMS_VIEW = "drop view if exists export_top_items_view";
    static final String SQL_DROP_SO_ITEMS_VIEW = "drop view if exists so_items_view";
    static final String SQL_DROP_SO_ITEMS_FAST_VIEW = "drop view if exists so_items_fast_view";
    static final String SQL_DROP_SO_SALE_REPORTS_ITEMS_VIEW = "drop view if exists so_sale_reports_items_view";
    static final String SQL_DROP_XREPORT_VIEW = "drop view if exists xreport_view";
    static final String SQL_DROP_SO_IM_VIEW = "drop view if exists so_im_view";
    static final String SQL_DROP_CUSTOMER_VIEW = "drop view if exists customer_view";
    static final String SQL_DROP_PRINTER_VIEW = "drop view if exists printer_view";
    static final String SQL_DROP_ITEMS_EXT_VIEW = "drop view if exists items_ext_view";
    static final String SQL_DROP_PAYMENT_TRANSACTION_VIEW = "drop view if exists payment_transaction_view";
    static final String SQL_DROP_EXPORT_ITEMS_VIEW = "drop view if exists export_items_view";
    static final String SQL_DROP_INVENTORY_LOG_VIEW = "drop view if exists inventory_log_view";
    static final String SQL_DROP_KITCHEN_PRINT = "drop view if exists kitchen_print";
    static final String SQL_DROP_CREDIT_RECEIPT_VIEW = "drop view if exists credit_receipt_view";
    static final String SQL_DROP_SALE_ADDON_VIEW = "drop view if exists sale_addon_view";
    static final String SQL_DROP_PREPAID_ORDER_VIEW = "drop view if exists prepaid_order_view";
    static final String SQL_DROP_TIPS_VIEW = "drop view if exists tips_view";
    static final String SQL_DROP_CATEGORY_VIEW = "drop view if exists category_view";


    static final String SQL_CREATE_SO_WITH_DELETED_ITEM_VIEW = "CREATE VIEW so_with_deleted_item_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.notes as sale_item_table_notes, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.stock_tracking as item_table_stock_tracking, item_table.description as item_table_description, item_table.printer_alias_guid as item_table_printer_alias_guid FROM sale_order_item AS sale_item_table JOIN item AS item_table ON item_table.guid = sale_item_table.item_id where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_SALE_ITEMS_COMMISSIONS_VIEW = "CREATE VIEW sale_items_commissions_view AS SELECT  item_table.guid as item_table_guid, item_table.eligible_for_commission as item_table_eligible_for_commission, item_table.commission as item_table_commission, category_table.eligible_for_commission as category_table_eligible_for_commission, category_table.commission as category_table_commission FROM item AS item_table JOIN category AS category_table ON category_table.guid = item_table.category_id";
    static final String SQL_CREATE_CREDIT_RECEIPT_EX_VIEW = "CREATE VIEW credit_receipt_ex_view AS SELECT  credit_receipt_table.guid as credit_receipt_table_guid, credit_receipt_table.cashier_guid as credit_receipt_table_cashier_guid, credit_receipt_table.register_id as credit_receipt_table_register_id, credit_receipt_table.shift_id as credit_receipt_table_shift_id, credit_receipt_table.create_time as credit_receipt_table_create_time, credit_receipt_table.amount as credit_receipt_table_amount, credit_receipt_table.print_number as credit_receipt_table_print_number, credit_receipt_table.expire_time as credit_receipt_table_expire_time, credit_receipt_table.is_deleted as credit_receipt_table_is_deleted, credit_receipt_table.update_time as credit_receipt_table_update_time, credit_receipt_table.is_draft as credit_receipt_table_is_draft, register_table.title as register_table_title, cashier_table.first_name as cashier_table_first_name, cashier_table.last_name as cashier_table_last_name, payment_table.create_time as payment_table_create_time, payment_table.amount as payment_table_amount FROM credit_receipt_table AS credit_receipt_table JOIN register AS register_table ON register_table._id = credit_receipt_table.register_id and register_table.is_deleted = 0 JOIN employee AS cashier_table ON cashier_table.guid = credit_receipt_table.cashier_guid and cashier_table.is_deleted = 0 LEFT OUTER JOIN payment_transaction AS payment_table ON payment_table.gtwp_id = credit_receipt_table.guid and payment_table.unitsLabel = 0 and payment_table.is_deleted = 0 where credit_receipt_table.is_deleted = 0";
    static final String SQL_CREATE_SO_VIEW = "CREATE VIEW so_view AS SELECT  sale_order_table.guid as sale_order_table_guid, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.operator_guid as sale_order_table_operator_guid, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.customer_guid as sale_order_table_customer_guid, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.status as sale_order_table_status, sale_order_table.hold_name as sale_order_table_hold_name, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.print_seq_num as sale_order_table_print_seq_num, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.parent_id as sale_order_table_parent_id, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.tml_total_price as sale_order_table_tml_total_price, sale_order_table.tml_total_tax as sale_order_table_tml_total_tax, sale_order_table.tml_total_discount as sale_order_table_tml_total_discount, sale_order_table.is_tipped as sale_order_table_is_tipped, sale_order_table.kitchen_print_status as sale_order_table_kitchen_print_status, sale_order_table.transaction_fee as sale_order_table_transaction_fee, sale_order_table.is_deleted as sale_order_table_is_deleted, sale_order_table.update_time as sale_order_table_update_time, sale_order_table.is_draft as sale_order_table_is_draft, operator_table.first_name as operator_table_first_name, operator_table.last_name as operator_table_last_name, customer_table.guid as customer_table_guid, customer_table.first_name as customer_table_first_name, customer_table.last_name as customer_table_last_name, customer_table.phone as customer_table_phone, customer_table.email as customer_table_email, register_table.title as register_table_title, tips_table.amount as tips_table_amount FROM sale_order AS sale_order_table LEFT OUTER JOIN employee AS operator_table ON operator_table.guid = sale_order_table.operator_guid LEFT OUTER JOIN customer AS customer_table ON customer_table.guid = sale_order_table.customer_guid JOIN register AS register_table ON register_table._id = sale_order_table.register_id and register_table.is_deleted = 0 LEFT OUTER JOIN employee_tips AS tips_table ON tips_table.order_id = sale_order_table.guid and tips_table.is_deleted = 0 where sale_order_table.is_deleted = 0";
    static final String SQL_CREATE_EMPLOYEE_TIMESHEET_VIEW = "CREATE VIEW employee_timesheet_view AS SELECT  time_table.guid as time_table_guid, time_table.employee_guid as time_table_employee_guid, time_table.clock_in as time_table_clock_in, time_table.clock_out as time_table_clock_out, time_table.is_deleted as time_table_is_deleted, time_table.update_time as time_table_update_time, time_table.is_draft as time_table_is_draft, employee_table.guid as employee_table_guid, employee_table.first_name as employee_table_first_name, employee_table.last_name as employee_table_last_name, employee_table.hourly_rate as employee_table_hourly_rate FROM employee_timesheet AS time_table JOIN employee AS employee_table ON employee_table.guid = time_table.employee_guid where time_table.is_deleted = 0";
    static final String SQL_CREATE_EMPLOYEE_COMISSION_VIEW = "CREATE VIEW employee_comission_view AS SELECT  comission_table.guid as comission_table_guid, comission_table.employee_id as comission_table_employee_id, comission_table.shift_id as comission_table_shift_id, comission_table.order_id as comission_table_order_id, comission_table.create_time as comission_table_create_time, comission_table.amount as comission_table_amount, comission_table.is_deleted as comission_table_is_deleted, comission_table.update_time as comission_table_update_time, comission_table.is_draft as comission_table_is_draft FROM employee_commissions AS comission_table";
    static final String SQL_CREATE_SHIFT_VIEW = "CREATE VIEW shift_view AS SELECT  shift_table.guid as shift_table_guid, shift_table.start_time as shift_table_start_time, shift_table.end_time as shift_table_end_time, shift_table.open_manager_id as shift_table_open_manager_id, shift_table.close_manager_id as shift_table_close_manager_id, shift_table.register_id as shift_table_register_id, shift_table.open_amount as shift_table_open_amount, shift_table.close_amount as shift_table_close_amount, shift_table.is_deleted as shift_table_is_deleted, shift_table.update_time as shift_table_update_time, shift_table.is_draft as shift_table_is_draft, open_manager_table.first_name as open_manager_table_first_name, open_manager_table.last_name as open_manager_table_last_name, close_manager_table.first_name as close_manager_table_first_name, close_manager_table.last_name as close_manager_table_last_name, register_table.title as register_table_title FROM shift AS shift_table JOIN employee AS open_manager_table ON open_manager_table.guid = shift_table.open_manager_id LEFT OUTER JOIN employee AS close_manager_table ON close_manager_table.guid = shift_table.close_manager_id JOIN register AS register_table ON register_table._id = shift_table.register_id and register_table.is_deleted = 0 where shift_table.is_deleted = 0";
    static final String SQL_CREATE_TIPS_REPORT_VIEW = "CREATE VIEW tips_report_view AS SELECT  tips_table.guid as tips_table_guid, tips_table.parent_guid as tips_table_parent_guid, tips_table.employee_id as tips_table_employee_id, tips_table.shift_id as tips_table_shift_id, tips_table.order_id as tips_table_order_id, tips_table.payment_transaction_id as tips_table_payment_transaction_id, tips_table.create_time as tips_table_create_time, tips_table.amount as tips_table_amount, tips_table.comment as tips_table_comment, tips_table.payment_type as tips_table_payment_type, tips_table.is_deleted as tips_table_is_deleted, tips_table.update_time as tips_table_update_time, tips_table.is_draft as tips_table_is_draft, shift_table.start_time as shift_table_start_time, shift_table.end_time as shift_table_end_time, employee_table.first_name as employee_table_first_name, employee_table.last_name as employee_table_last_name FROM employee_tips AS tips_table JOIN shift AS shift_table ON shift_table.guid = tips_table.shift_id and shift_table.is_deleted = 0 LEFT OUTER JOIN employee AS employee_table ON employee_table.guid = tips_table.employee_id where tips_table.is_deleted = 0";
    static final String SQL_CREATE_EXPORT_SOLD_ITEMS = "CREATE VIEW export_sold_items AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.status as sale_order_table_status, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.customer_guid as sale_order_table_customer_guid, sale_order_table.order_type as sale_order_table_order_type, item_table.description as item_table_description, item_table.category_id as item_table_category_id, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.guid as item_table_guid, item_table.cost as item_table_cost, category_table.title as category_table_title, category_table.department_guid as category_table_department_guid, department_table.title as department_table_title FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 JOIN item AS item_table ON item_table.guid = sale_item_table.item_id JOIN category AS category_table ON category_table.guid = item_table.category_id and category_table.is_deleted = 0 JOIN department AS department_table ON department_table.guid = category_table.department_guid and department_table.is_deleted = 0 where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_EXPORT_TOP_ITEMS_VIEW = "CREATE VIEW export_top_items_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.status as sale_order_table_status, sale_order_table.order_type as sale_order_table_order_type, item_table.guid as item_table_guid, item_table.category_id as item_table_category_id, item_table.description as item_table_description, item_table.code as item_table_code, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.price_type as item_table_price_type, item_table.serializable as item_table_serializable, item_table.sale_price as item_table_sale_price, item_table.units_label as item_table_units_label, item_table.stock_tracking as item_table_stock_tracking, item_table.active_status as item_table_active_status, item_table.discountable as item_table_discountable, item_table.discount as item_table_discount, item_table.discount_type as item_table_discount_type, item_table.taxable as item_table_taxable, item_table.cost as item_table_cost, item_table.minimum_qty as item_table_minimum_qty, item_table.recommended_qty as item_table_recommended_qty, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.tax_group_guid as item_table_tax_group_guid, item_table.tmp_available_qty as item_table_tmp_available_qty, item_table.order_num as item_table_order_num, item_table.default_modifier_guid as item_table_default_modifier_guid, item_table.printer_alias_guid as item_table_printer_alias_guid, item_table.button_view as item_table_button_view, item_table.has_notes as item_table_has_notes, item_table.code_type as item_table_code_type, item_table.eligible_for_commission as item_table_eligible_for_commission, item_table.commission as item_table_commission, item_table.is_deleted as item_table_is_deleted, item_table.update_time as item_table_update_time, item_table.is_draft as item_table_is_draft FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 JOIN item AS item_table ON item_table.guid = sale_item_table.item_id where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_SO_ITEMS_VIEW = "CREATE VIEW so_items_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.transaction_fee as sale_order_table_transaction_fee, item_table.guid as item_table_guid, item_table.category_id as item_table_category_id, item_table.description as item_table_description, item_table.code as item_table_code, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.price_type as item_table_price_type, item_table.serializable as item_table_serializable, item_table.sale_price as item_table_sale_price, item_table.units_label as item_table_units_label, item_table.stock_tracking as item_table_stock_tracking, item_table.active_status as item_table_active_status, item_table.discountable as item_table_discountable, item_table.discount as item_table_discount, item_table.discount_type as item_table_discount_type, item_table.taxable as item_table_taxable, item_table.cost as item_table_cost, item_table.minimum_qty as item_table_minimum_qty, item_table.recommended_qty as item_table_recommended_qty, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.tax_group_guid as item_table_tax_group_guid, item_table.tmp_available_qty as item_table_tmp_available_qty, item_table.order_num as item_table_order_num, item_table.default_modifier_guid as item_table_default_modifier_guid, item_table.printer_alias_guid as item_table_printer_alias_guid, item_table.button_view as item_table_button_view, item_table.has_notes as item_table_has_notes, item_table.code_type as item_table_code_type, item_table.eligible_for_commission as item_table_eligible_for_commission, item_table.commission as item_table_commission, item_table.is_deleted as item_table_is_deleted, item_table.update_time as item_table_update_time, item_table.is_draft as item_table_is_draft, bill_payment_description_table.guid as bill_payment_description_table_guid, bill_payment_description_table.description as bill_payment_description_table_description, bill_payment_description_table.type as bill_payment_description_table_type, bill_payment_description_table.is_voided as bill_payment_description_table_is_voided, bill_payment_description_table.is_failed as bill_payment_description_table_is_failed, bill_payment_description_table.order_id as bill_payment_description_table_order_id, bill_payment_description_table.is_deleted as bill_payment_description_table_is_deleted, bill_payment_description_table.update_time as bill_payment_description_table_update_time, bill_payment_description_table.is_draft as bill_payment_description_table_is_draft, sale_addon_table.guid as sale_addon_table_guid, sale_addon_table.addon_id as sale_addon_table_addon_id, sale_addon_table.item_guid as sale_addon_table_item_guid, sale_addon_table.extra_cost as sale_addon_table_extra_cost, sale_addon_table.addon_type as sale_addon_table_addon_type, sale_addon_table.is_deleted as sale_addon_table_is_deleted, sale_addon_table.update_time as sale_addon_table_update_time, sale_addon_table.is_draft as sale_addon_table_is_draft, modifier_table.title as modifier_table_title FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 LEFT OUTER JOIN item AS item_table ON item_table.guid = sale_item_table.item_id LEFT OUTER JOIN bp_description AS bill_payment_description_table ON bill_payment_description_table.guid = sale_item_table.item_id LEFT OUTER JOIN sale_order_item_addon AS sale_addon_table ON sale_addon_table.item_guid = sale_item_table.sale_order_item_id and sale_addon_table.is_deleted = 0 LEFT OUTER JOIN items_modifier AS modifier_table ON modifier_table.modifier_guid = sale_addon_table.addon_id and modifier_table.is_deleted = 0 where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_SO_ITEMS_FAST_VIEW = "CREATE VIEW so_items_fast_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.is_draft as sale_order_table_is_draft FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_SO_SALE_REPORTS_ITEMS_VIEW = "CREATE VIEW so_sale_reports_items_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.status as sale_order_table_status, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.transaction_fee as sale_order_table_transaction_fee, item_table.description as item_table_description, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.cost as item_table_cost, item_table.category_id as item_table_category_id, category_table.title as category_table_title, category_table.department_guid as category_table_department_guid, department_table.title as department_table_title, bill_payment_description_table.guid as bill_payment_description_table_guid, bill_payment_description_table.description as bill_payment_description_table_description, bill_payment_description_table.type as bill_payment_description_table_type, bill_payment_description_table.is_voided as bill_payment_description_table_is_voided, bill_payment_description_table.is_failed as bill_payment_description_table_is_failed, bill_payment_description_table.order_id as bill_payment_description_table_order_id, bill_payment_description_table.is_deleted as bill_payment_description_table_is_deleted, bill_payment_description_table.update_time as bill_payment_description_table_update_time, bill_payment_description_table.is_draft as bill_payment_description_table_is_draft FROM sale_order_item AS sale_item_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 LEFT OUTER JOIN item AS item_table ON item_table.guid = sale_item_table.item_id LEFT OUTER JOIN category AS category_table ON category_table.guid = item_table.category_id LEFT OUTER JOIN department AS department_table ON department_table.guid = category_table.department_guid LEFT OUTER JOIN bp_description AS bill_payment_description_table ON bill_payment_description_table.guid = sale_item_table.item_id where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_XREPORT_VIEW = "CREATE VIEW xreport_view AS SELECT  sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.kitchen_printed_qty as sale_item_table_kitchen_printed_qty, sale_item_table.price as sale_item_table_price, sale_item_table.price_type as sale_item_table_price_type, sale_item_table.discountable as sale_item_table_discountable, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.taxable as sale_item_table_taxable, sale_item_table.tax as sale_item_table_tax, sale_item_table.sequence as sale_item_table_sequence, sale_item_table.parent_guid as sale_item_table_parent_guid, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.tmp_refund_quantity as sale_item_table_tmp_refund_quantity, sale_item_table.notes as sale_item_table_notes, sale_item_table.has_notes as sale_item_table_has_notes, sale_item_table.is_deleted as sale_item_table_is_deleted, sale_item_table.update_time as sale_item_table_update_time, sale_item_table.is_draft as sale_item_table_is_draft, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.status as sale_order_table_status, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.transaction_fee as sale_order_table_transaction_fee, item_table.guid as item_table_guid, item_table.description as item_table_description, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.cost as item_table_cost, item_table.category_id as item_table_category_id FROM sale_order_item AS sale_item_table LEFT OUTER JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 LEFT OUTER JOIN item AS item_table ON item_table.guid = sale_item_table.item_id where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_SO_IM_VIEW = "CREATE VIEW so_im_view AS SELECT  movement_table.guid as movement_table_guid, movement_table.item_guid as movement_table_item_guid, movement_table.qty as movement_table_qty, movement_table.ITEM_UPDATE_QTY_FLAG as movement_table_ITEM_UPDATE_QTY_FLAG, movement_table.manual as movement_table_manual, movement_table.create_time as movement_table_create_time, movement_table.tmp_available_qty as movement_table_tmp_available_qty, movement_table.is_deleted as movement_table_is_deleted, movement_table.update_time as movement_table_update_time, movement_table.is_draft as movement_table_is_draft, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.is_draft as item_table_is_draft FROM item_movement AS movement_table JOIN item AS item_table ON movement_table.item_guid = item_table.guid and item_table.UPDATE_QTY_FLAG is NOT NULL and item_table.UPDATE_QTY_FLAG = movement_table.ITEM_UPDATE_QTY_FLAG where movement_table.is_deleted = 0";
    static final String SQL_CREATE_CUSTOMER_VIEW = "CREATE VIEW customer_view AS SELECT  sale_order_table.guid as sale_order_table_guid, sale_order_table.create_time as sale_order_table_create_time, sale_order_table.operator_guid as sale_order_table_operator_guid, sale_order_table.shift_guid as sale_order_table_shift_guid, sale_order_table.customer_guid as sale_order_table_customer_guid, sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, sale_order_table.status as sale_order_table_status, sale_order_table.hold_name as sale_order_table_hold_name, sale_order_table.taxable as sale_order_table_taxable, sale_order_table.print_seq_num as sale_order_table_print_seq_num, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.parent_id as sale_order_table_parent_id, sale_order_table.order_type as sale_order_table_order_type, sale_order_table.tml_total_price as sale_order_table_tml_total_price, sale_order_table.tml_total_tax as sale_order_table_tml_total_tax, sale_order_table.tml_total_discount as sale_order_table_tml_total_discount, sale_order_table.is_tipped as sale_order_table_is_tipped, sale_order_table.kitchen_print_status as sale_order_table_kitchen_print_status, sale_order_table.transaction_fee as sale_order_table_transaction_fee, sale_order_table.is_deleted as sale_order_table_is_deleted, sale_order_table.update_time as sale_order_table_update_time, sale_order_table.is_draft as sale_order_table_is_draft, customer_table.guid as customer_table_guid, customer_table.first_name as customer_table_first_name, customer_table.last_name as customer_table_last_name, customer_table.street as customer_table_street, customer_table.complementary as customer_table_complementary, customer_table.city as customer_table_city, customer_table.state as customer_table_state, customer_table.country as customer_table_country, customer_table.zip as customer_table_zip, customer_table.email as customer_table_email, customer_table.phone as customer_table_phone, customer_table.sex as customer_table_sex, customer_table.create_time as customer_table_create_time, customer_table.consent_promotions as customer_table_consent_promotions, customer_table.is_deleted as customer_table_is_deleted, customer_table.update_time as customer_table_update_time, customer_table.is_draft as customer_table_is_draft FROM sale_order AS sale_order_table LEFT OUTER JOIN customer AS customer_table ON customer_table.guid = sale_order_table.customer_guid and customer_table.is_deleted = 0 where sale_order_table.is_deleted = 0";
    static final String SQL_CREATE_PRINTER_VIEW = "CREATE VIEW printer_view AS SELECT  printer_table.guid as printer_table_guid, printer_table.ip as printer_table_ip, printer_table.port as printer_table_port, printer_table.alias_guid as printer_table_alias_guid, printer_table.mac as printer_table_mac, printer_table.subnet as printer_table_subnet, printer_table.gateway as printer_table_gateway, printer_table.dhcp as printer_table_dhcp, alias_table.alias as alias_table_alias FROM printer_able AS printer_table LEFT OUTER JOIN printer_alias_table AS alias_table ON alias_table.guid = printer_table.alias_guid and alias_table.is_deleted = 0";
    static final String SQL_CREATE_ITEMS_EXT_VIEW = "CREATE VIEW items_ext_view AS SELECT  item_table.guid as item_table_guid, item_table.category_id as item_table_category_id, item_table.description as item_table_description, item_table.code as item_table_code, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.price_type as item_table_price_type, item_table.serializable as item_table_serializable, item_table.sale_price as item_table_sale_price, item_table.units_label as item_table_units_label, item_table.stock_tracking as item_table_stock_tracking, item_table.active_status as item_table_active_status, item_table.discountable as item_table_discountable, item_table.discount as item_table_discount, item_table.discount_type as item_table_discount_type, item_table.taxable as item_table_taxable, item_table.cost as item_table_cost, item_table.minimum_qty as item_table_minimum_qty, item_table.recommended_qty as item_table_recommended_qty, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.tax_group_guid as item_table_tax_group_guid, item_table.tmp_available_qty as item_table_tmp_available_qty, item_table.order_num as item_table_order_num, item_table.default_modifier_guid as item_table_default_modifier_guid, item_table.printer_alias_guid as item_table_printer_alias_guid, item_table.button_view as item_table_button_view, item_table.has_notes as item_table_has_notes, item_table.code_type as item_table_code_type, item_table.eligible_for_commission as item_table_eligible_for_commission, item_table.commission as item_table_commission, item_table.is_deleted as item_table_is_deleted, item_table.update_time as item_table_update_time, item_table.is_draft as item_table_is_draft, category_table.title as category_table_title, category_table.department_guid as category_table_department_guid, modifier_table.unitsLabel as modifier_table_unitsLabel, modifier_table.modifier_guid as modifier_table_modifier_guid, modifier_table.extra_cost as modifier_table_extra_cost, tax_group_table.tax as tax_group_table_tax FROM item AS item_table JOIN category AS category_table ON category_table.guid = item_table.category_id and category_table.is_deleted = 0 JOIN department AS department_table ON department_table.guid = category_table.department_guid and department_table.is_deleted = 0 LEFT OUTER JOIN items_modifier AS modifier_table ON modifier_table.item_guid = item_table.guid and modifier_table.is_deleted = 0 LEFT OUTER JOIN tax_group AS tax_group_table ON tax_group_table.guid = item_table.tax_group_guid and tax_group_table.is_deleted = 0 where item_table.is_deleted = 0";
    static final String SQL_CREATE_PAYMENT_TRANSACTION_VIEW = "CREATE VIEW payment_transaction_view AS SELECT  payment_transaction_table.guid as payment_transaction_table_guid, payment_transaction_table.order_guid as payment_transaction_table_order_guid, payment_transaction_table.parent_guid as payment_transaction_table_parent_guid, payment_transaction_table.amount as payment_transaction_table_amount, payment_transaction_table.status as payment_transaction_table_status, payment_transaction_table.unitsLabel as payment_transaction_table_unitsLabel, payment_transaction_table.operator_id as payment_transaction_table_operator_id, payment_transaction_table.gtw_id as payment_transaction_table_gtw_id, payment_transaction_table.gtwp_id as payment_transaction_table_gtwp_id, payment_transaction_table.gtw_preauth_payment_id as payment_transaction_table_gtw_preauth_payment_id, payment_transaction_table.gateway_closed_perauth_guid as payment_transaction_table_gateway_closed_perauth_guid, payment_transaction_table.d_reason as payment_transaction_table_d_reason, payment_transaction_table.create_time as payment_transaction_table_create_time, payment_transaction_table.shift_guid as payment_transaction_table_shift_guid, payment_transaction_table.card_name as payment_transaction_table_card_name, payment_transaction_table.change_amount as payment_transaction_table_change_amount, payment_transaction_table.is_preauth as payment_transaction_table_is_preauth, payment_transaction_table.balance as payment_transaction_table_balance, payment_transaction_table.is_deleted as payment_transaction_table_is_deleted, payment_transaction_table.update_time as payment_transaction_table_update_time, payment_transaction_table.is_draft as payment_transaction_table_is_draft, sale_order_table.register_id as sale_order_table_register_id, sale_order_table.guid as sale_order_table_guid, sale_order_table.parent_id as sale_order_table_parent_id, sale_order_table.create_time as sale_order_table_create_time, employee_tips_table.guid as employee_tips_table_guid, employee_tips_table.parent_guid as employee_tips_table_parent_guid, employee_tips_table.employee_id as employee_tips_table_employee_id, employee_tips_table.shift_id as employee_tips_table_shift_id, employee_tips_table.order_id as employee_tips_table_order_id, employee_tips_table.payment_transaction_id as employee_tips_table_payment_transaction_id, employee_tips_table.create_time as employee_tips_table_create_time, employee_tips_table.amount as employee_tips_table_amount, employee_tips_table.comment as employee_tips_table_comment, employee_tips_table.payment_type as employee_tips_table_payment_type, employee_tips_table.is_deleted as employee_tips_table_is_deleted, employee_tips_table.update_time as employee_tips_table_update_time, employee_tips_table.is_draft as employee_tips_table_is_draft FROM payment_transaction AS payment_transaction_table JOIN sale_order AS sale_order_table ON sale_order_table.guid = payment_transaction_table.order_guid and sale_order_table.is_deleted = 0 LEFT OUTER JOIN employee_tips AS employee_tips_table ON employee_tips_table.payment_transaction_id = payment_transaction_table.guid and employee_tips_table.is_deleted = 0 where payment_transaction_table.is_deleted = 0";
    static final String SQL_CREATE_EXPORT_ITEMS_VIEW = "CREATE VIEW export_items_view AS SELECT  item_table.guid as item_table_guid, item_table.category_id as item_table_category_id, item_table.description as item_table_description, item_table.code as item_table_code, item_table.ean_code as item_table_ean_code, item_table.product_code as item_table_product_code, item_table.price_type as item_table_price_type, item_table.serializable as item_table_serializable, item_table.sale_price as item_table_sale_price, item_table.units_label as item_table_units_label, item_table.stock_tracking as item_table_stock_tracking, item_table.active_status as item_table_active_status, item_table.discountable as item_table_discountable, item_table.discount as item_table_discount, item_table.discount_type as item_table_discount_type, item_table.taxable as item_table_taxable, item_table.cost as item_table_cost, item_table.minimum_qty as item_table_minimum_qty, item_table.recommended_qty as item_table_recommended_qty, item_table.UPDATE_QTY_FLAG as item_table_UPDATE_QTY_FLAG, item_table.tax_group_guid as item_table_tax_group_guid, item_table.tmp_available_qty as item_table_tmp_available_qty, item_table.order_num as item_table_order_num, item_table.default_modifier_guid as item_table_default_modifier_guid, item_table.printer_alias_guid as item_table_printer_alias_guid, item_table.button_view as item_table_button_view, item_table.has_notes as item_table_has_notes, item_table.code_type as item_table_code_type, item_table.eligible_for_commission as item_table_eligible_for_commission, item_table.commission as item_table_commission, item_table.is_deleted as item_table_is_deleted, item_table.update_time as item_table_update_time, item_table.is_draft as item_table_is_draft, category_table.title as category_table_title, category_table.department_guid as category_table_department_guid, department_table.title as department_table_title, sale_order_table.create_time as sale_order_table_create_time FROM item AS item_table JOIN category AS category_table ON category_table.guid = item_table.category_id and category_table.is_deleted = 0 JOIN department AS department_table ON department_table.guid = category_table.department_guid and department_table.is_deleted = 0 LEFT OUTER JOIN sale_order_item AS sale_item_table ON sale_item_table.item_id = item_table.guid and sale_item_table.is_deleted = 0 LEFT OUTER JOIN sale_order AS sale_order_table ON sale_order_table.guid = sale_item_table.order_id and sale_order_table.is_deleted = 0 where item_table.is_deleted = 0";
    static final String SQL_CREATE_INVENTORY_LOG_VIEW = "CREATE VIEW inventory_log_view AS SELECT  item_movement_table.guid as item_movement_table_guid, item_movement_table.item_guid as item_movement_table_item_guid, item_movement_table.qty as item_movement_table_qty, item_movement_table.ITEM_UPDATE_QTY_FLAG as item_movement_table_ITEM_UPDATE_QTY_FLAG, item_movement_table.manual as item_movement_table_manual, item_movement_table.create_time as item_movement_table_create_time, item_movement_table.tmp_available_qty as item_movement_table_tmp_available_qty, item_movement_table.is_deleted as item_movement_table_is_deleted, item_movement_table.update_time as item_movement_table_update_time, item_movement_table.is_draft as item_movement_table_is_draft, item_table.description as item_table_description FROM item_movement AS item_movement_table JOIN item AS item_table ON item_table.guid = item_movement_table.item_guid and item_table.is_deleted = 0 where item_movement_table.is_deleted = 0";
    static final String SQL_CREATE_KITCHEN_PRINT = "CREATE VIEW kitchen_print AS SELECT  sale_item_table.order_id as sale_item_table_order_id, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.quantity as sale_item_table_quantity, item_table.description as item_table_description, item_table.printer_alias_guid as item_table_printer_alias_guid, printer_alias_table.guid as printer_alias_table_guid, printer_alias_table.alias as printer_alias_table_alias, printer_alias_table.is_deleted as printer_alias_table_is_deleted, printer_alias_table.update_time as printer_alias_table_update_time, printer_alias_table.is_draft as printer_alias_table_is_draft, printer_table.guid as printer_table_guid, printer_table.ip as printer_table_ip, printer_table.port as printer_table_port, printer_table.alias_guid as printer_table_alias_guid, printer_table.mac as printer_table_mac, printer_table.subnet as printer_table_subnet, printer_table.gateway as printer_table_gateway, printer_table.dhcp as printer_table_dhcp, order_table.operator_guid as order_table_operator_guid, order_table.print_seq_num as order_table_print_seq_num, order_table.register_id as order_table_register_id, employee_table.first_name as employee_table_first_name, employee_table.last_name as employee_table_last_name FROM sale_order_item AS sale_item_table JOIN item AS item_table ON item_table.guid = sale_item_table.item_id and item_table.is_deleted = 0 JOIN printer_alias_table AS printer_alias_table ON printer_alias_table.guid = item_table.printer_alias_guid and printer_alias_table.is_deleted = 0 JOIN printer_able AS printer_table ON printer_table.alias_guid = printer_alias_table.guid JOIN sale_order AS order_table ON order_table.guid = sale_item_table.order_id and order_table.is_deleted = 0 JOIN employee AS employee_table ON employee_table.guid = order_table.operator_guid and employee_table.is_deleted = 0 where sale_item_table.is_deleted = 0";
    static final String SQL_CREATE_CREDIT_RECEIPT_VIEW = "CREATE VIEW credit_receipt_view AS SELECT  credit_receipt_table.guid as credit_receipt_table_guid, credit_receipt_table.cashier_guid as credit_receipt_table_cashier_guid, credit_receipt_table.register_id as credit_receipt_table_register_id, credit_receipt_table.shift_id as credit_receipt_table_shift_id, credit_receipt_table.create_time as credit_receipt_table_create_time, credit_receipt_table.amount as credit_receipt_table_amount, credit_receipt_table.print_number as credit_receipt_table_print_number, credit_receipt_table.expire_time as credit_receipt_table_expire_time, credit_receipt_table.is_deleted as credit_receipt_table_is_deleted, credit_receipt_table.update_time as credit_receipt_table_update_time, credit_receipt_table.is_draft as credit_receipt_table_is_draft, register_table.title as register_table_title FROM credit_receipt_table AS credit_receipt_table JOIN register AS register_table ON register_table._id = credit_receipt_table.register_id and register_table.is_deleted = 0 where credit_receipt_table.is_deleted = 0";
    static final String SQL_CREATE_SALE_ADDON_VIEW = "CREATE VIEW sale_addon_view AS SELECT  sale_addon_table.guid as sale_addon_table_guid, sale_addon_table.addon_id as sale_addon_table_addon_id, sale_addon_table.item_guid as sale_addon_table_item_guid, sale_addon_table.extra_cost as sale_addon_table_extra_cost, sale_addon_table.addon_type as sale_addon_table_addon_type, sale_addon_table.is_deleted as sale_addon_table_is_deleted, sale_addon_table.update_time as sale_addon_table_update_time, sale_addon_table.is_draft as sale_addon_table_is_draft, modifier_table.title as modifier_table_title FROM sale_order_item_addon AS sale_addon_table JOIN items_modifier AS modifier_table ON modifier_table.modifier_guid = sale_addon_table.addon_id and modifier_table.is_deleted = 0 where sale_addon_table.is_deleted = 0";
    static final String SQL_CREATE_PREPAID_ORDER_VIEW = "CREATE VIEW prepaid_order_view AS SELECT  sale_order_table.guid as sale_order_table_guid, sale_order_table.order_type as sale_order_table_order_type, sale_item_table.sale_order_item_id as sale_item_table_sale_order_item_id, bill_payment_description_table.guid as bill_payment_description_table_guid, bill_payment_description_table.description as bill_payment_description_table_description, bill_payment_description_table.type as bill_payment_description_table_type, bill_payment_description_table.is_voided as bill_payment_description_table_is_voided, bill_payment_description_table.is_failed as bill_payment_description_table_is_failed, bill_payment_description_table.order_id as bill_payment_description_table_order_id, bill_payment_description_table.is_deleted as bill_payment_description_table_is_deleted, bill_payment_description_table.update_time as bill_payment_description_table_update_time, bill_payment_description_table.is_draft as bill_payment_description_table_is_draft FROM sale_order AS sale_order_table LEFT OUTER JOIN sale_order_item AS sale_item_table ON sale_item_table.order_id = sale_order_table.guid and sale_item_table.is_deleted = 0 LEFT OUTER JOIN bp_description AS bill_payment_description_table ON bill_payment_description_table.guid = sale_item_table.item_id and bill_payment_description_table.is_deleted = 0 where sale_order_table.is_deleted = 0";
    static final String SQL_CREATE_TIPS_VIEW = "CREATE VIEW tips_view AS SELECT  sale_order_table.guid as sale_order_table_guid, sale_order_table.parent_id as sale_order_table_parent_id, tips_table.guid as tips_table_guid, tips_table.parent_guid as tips_table_parent_guid, tips_table.employee_id as tips_table_employee_id, tips_table.shift_id as tips_table_shift_id, tips_table.order_id as tips_table_order_id, tips_table.payment_transaction_id as tips_table_payment_transaction_id, tips_table.create_time as tips_table_create_time, tips_table.amount as tips_table_amount, tips_table.comment as tips_table_comment, tips_table.payment_type as tips_table_payment_type, tips_table.is_deleted as tips_table_is_deleted, tips_table.update_time as tips_table_update_time, tips_table.is_draft as tips_table_is_draft FROM sale_order AS sale_order_table JOIN employee_tips AS tips_table ON tips_table.order_id = sale_order_table.guid and tips_table.is_deleted = 0 where sale_order_table.is_deleted = 0";
    static final String SQL_CREATE_CATEGORY_VIEW = "CREATE VIEW category_view AS SELECT  category_table._id as _id, category_table.guid as category_table_guid, category_table.department_guid as category_table_department_guid, category_table.title as category_table_title, category_table.image as category_table_image, category_table.order_num as category_table_order_num, category_table.eligible_for_commission as category_table_eligible_for_commission, category_table.commission as category_table_commission, category_table.is_deleted as category_table_is_deleted, category_table.update_time as category_table_update_time, category_table.is_draft as category_table_is_draft, item_table.guid as item_table_guid, item_table.stock_tracking as item_table_stock_tracking, item_table.tmp_available_qty as item_table_tmp_available_qty, item_table.minimum_qty as item_table_minimum_qty FROM category AS category_table JOIN department AS department_table ON department_table.guid = category_table.department_guid and department_table.is_deleted = 0 LEFT OUTER JOIN item AS item_table ON item_table.category_id = category_table.guid and item_table.is_deleted = 0 where category_table.is_deleted = 0";
    static final String SQL_CREATE_CATEGORY_SIMPLE_VIEW = "CREATE VIEW category_simple_view AS SELECT  category_table._id as _id, category_table.guid as category_table_guid, category_table.department_guid as category_table_department_guid, category_table.title as category_table_title, category_table.image as category_table_image, category_table.order_num as category_table_order_num, category_table.eligible_for_commission as category_table_eligible_for_commission, category_table.commission as category_table_commission, category_table.is_deleted as category_table_is_deleted, category_table.update_time as category_table_update_time, category_table.is_draft as category_table_is_draft FROM category AS category_table JOIN department AS department_table ON department_table.guid = category_table.department_guid and department_table.is_deleted = 0 where category_table.is_deleted = 0";

    public static void update4To5(SQLiteDatabase db) {
        db.execSQL(SQL_RENAME_WIRELESS_ITEM);
        db.execSQL(SQL_CREATE_WIRELESS_ITEM);
        db.execSQL(SQL_COPY_WIRELESS_ITEM);
        db.execSQL(SQL_DROP_TEMP_WIRELESS_ITEM);

        db.execSQL(SQL_DROP_TABLE_UNIT);
        db.execSQL(SQL_DROP_TABLE_DEPARTMENT);
        db.execSQL(SQL_DROP_TABLE_CATEGORY);
        db.execSQL(SQL_DROP_TABLE_ITEM);
        db.execSQL(SQL_DROP_TABLE_ITEM_MOVEMENT);
        db.execSQL(SQL_DROP_TABLE_ITEMS_MODIFIER);
        db.execSQL(SQL_DROP_TABLE_SALE_ORDER);
        db.execSQL(SQL_DROP_TABLE_SALE_ORDER_ITEM);
        db.execSQL(SQL_DROP_TABLE_SALE_ORDER_ITEM_ADDON);
        db.execSQL(SQL_DROP_TABLE_EMPLOYEE);
        db.execSQL(SQL_DROP_TABLE_EMPLOYEE_PERMISSION);
        db.execSQL(SQL_DROP_TABLE_SHIFT);
        db.execSQL(SQL_DROP_TABLE_CASHDRAWER_TR);
        db.execSQL(SQL_DROP_TABLE_PAYMENT_TRANSACTION);
        db.execSQL(SQL_DROP_TABLE_EMPLOYEE_TIMESHEET);
        db.execSQL(SQL_DROP_TABLE_TAX_GROUP);
        db.execSQL(SQL_DROP_TABLE_REGISTER);
        db.execSQL(SQL_DROP_TABLE_BP_DESCRIPTION);
        db.execSQL(SQL_DROP_TABLE_CUSTOMER);
        db.execSQL(SQL_DROP_TABLE_PRINTER_ALIAS_TABLE);
        db.execSQL(SQL_DROP_TABLE_CREDIT_RECEIPT_TABLE);
        db.execSQL(SQL_DROP_TABLE_EMPLOYEE_TIPS);
        db.execSQL(SQL_DROP_TABLE_ACTIVATION_CARRIER);
        db.execSQL(SQL_DROP_TABLE_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_DROP_TABLE_TOPUP);
        db.execSQL(SQL_DROP_TABLE_PIN);


        //NOTE: order important
        db.execSQL(SQL_CREATE_ACTIVATION_CARRIER);

        db.execSQL(SQL_REMOVE_INVALID_PAXES);
        db.execSQL(SQL_RENAME_PAX_TABLE);
        db.execSQL(SQL_CREATE_PAX_TABLE);
        db.execSQL(SQL_COPY_PAX_TABLE);
        db.execSQL(SQL_DROP_TEMP_PAX_TABLE);

        db.execSQL(SQL_REMOVE_INVALID_PRINTERS);
        db.execSQL(SQL_RENAME_PRINTER_TABLE);
        db.execSQL(SQL_CREATE_PRINTER_TABLE);
        db.execSQL(SQL_COPY_PRINTER_TABLE);
        db.execSQL(SQL_DROP_TEMP_PRINTER_TABLE);

        db.execSQL(SQL_CREATE_DEPARTMENT);
        db.execSQL(SQL_CREATE_EMPLOYEE);
        db.execSQL(SQL_CREATE_TAX_GROUP);
        db.execSQL(SQL_CREATE_REGISTER);
        db.execSQL(SQL_CREATE_BP_DESCRIPTION);
        db.execSQL(SQL_CREATE_CUSTOMER);
        db.execSQL(SQL_CREATE_PRINTER_ALIAS_TABLE);

        db.execSQL(SQL_CREATE_CATEGORY);
        db.execSQL(SQL_CREATE_ITEM);
        db.execSQL(SQL_CREATE_ITEM_MOVEMENT);
        db.execSQL(SQL_CREATE_ITEMS_MODIFIER);

        db.execSQL(SQL_CREATE_EMPLOYEE_PERMISSION);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIMESHEET);
        db.execSQL(SQL_CREATE_SHIFT);

        db.execSQL(SQL_CREATE_CASHDRAWER_TR);
        db.execSQL(SQL_CREATE_CREDIT_RECEIPT_TABLE);

        db.execSQL(SQL_CREATE_SALE_ORDER);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM);
        db.execSQL(SQL_CREATE_SALE_ORDER_ITEM_ADDON);

        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION);

        db.execSQL(SQL_CREATE_UNIT);

        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS);


        db.execSQL(SQL_ALTER_SQL_COMMAND);
        db.execSQL(SQL_UPDATE_SQL_COMMAND);


        db.execSQL(SQL_CREATE_UNIT_ITEM);
        db.execSQL(SQL_CREATE_UNIT_SALE_ITEM);
        db.execSQL(SQL_CREATE_UNIT_CHILD_SALE_ITEM);
        db.execSQL(SQL_CREATE_CATEGORY_DEPS);
        db.execSQL(SQL_CREATE_ITEM_CATS);
        db.execSQL(SQL_CREATE_ITEM_EAN);
        db.execSQL(SQL_CREATE_ITEM_PRODUCT);
        db.execSQL(SQL_CREATE_ITEM_TAX_GROUP);
        db.execSQL(SQL_CREATE_ITEM_PRINTER_ALIAS);
        db.execSQL(SQL_CREATE_ITEM_MOVEMENT_ITEM);
        db.execSQL(SQL_CREATE_ITEMS_MODIFIER_ITEM);
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
        db.execSQL(SQL_CREATE_EMPLOYEE_FIRST_NAME);
        db.execSQL(SQL_CREATE_EMPLOYEE_EMAIL);
        db.execSQL(SQL_CREATE_EMPLOYEE_PERMISSION_USER);
        db.execSQL(SQL_CREATE_SHIFT_START_TIME);
        db.execSQL(SQL_CREATE_SHIFT_OPEN_MANAGER);
        db.execSQL(SQL_CREATE_SHIFT_CLOSE_MANAGER);
        db.execSQL(SQL_CREATE_SHIFT_REGISTER);
        db.execSQL(SQL_CREATE_CASHDRAWER_TR_SHIFT);
        db.execSQL(SQL_CREATE_CASHDRAWER_TR_MANAGER);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_ORDER_GUID);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_PARENT);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_STATUS);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_TYPE);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_OPERATOR);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIMESHEET_EMPLOYEE);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIMESHEET_CLOCK_IN);
        db.execSQL(SQL_CREATE_CUSTOMER_LAST_NAME);
        db.execSQL(SQL_CREATE_CUSTOMER_EMAIL);
        db.execSQL(SQL_CREATE_CREDIT_RECEIPT_TABLE_REGISTER);
        db.execSQL(SQL_CREATE_CREDIT_RECEIPT_TABLE_CASHIER);
        db.execSQL(SQL_CREATE_CREDIT_RECEIPT_TABLE_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_CASHIER);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_ORDER);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_PAYMENT);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIPS_PARENT);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS_SHIFT);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS_CASHIER);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMMISSIONS_ORDER);
        db.execSQL(SQL_CREATE_ITEM_MOVEMENTS);
        db.execSQL(SQL_CREATE_ITEM_MOVEMENT_MVN_FLAG);


        db.execSQL(SQL_DROP_SO_WITH_DELETED_ITEM_VIEW);
        db.execSQL(SQL_CREATE_SO_WITH_DELETED_ITEM_VIEW);

        db.execSQL(SQL_DROP_SALE_ITEMS_COMMISSIONS_VIEW);
        db.execSQL(SQL_DROP_CREDIT_RECEIPT_EX_VIEW);
        db.execSQL(SQL_DROP_SO_VIEW);

        db.execSQL(SQL_CREATE_SALE_ITEMS_COMMISSIONS_VIEW);
        db.execSQL(SQL_CREATE_CREDIT_RECEIPT_EX_VIEW);
        db.execSQL(SQL_CREATE_SO_VIEW);

        db.execSQL(SQL_DROP_EMPLOYEE_TIMESHEET_VIEW);
        db.execSQL(SQL_CREATE_EMPLOYEE_TIMESHEET_VIEW);
        db.execSQL(SQL_CREATE_EMPLOYEE_COMISSION_VIEW);
        db.execSQL(SQL_DROP_SHIFT_VIEW);
        db.execSQL(SQL_CREATE_SHIFT_VIEW);
        db.execSQL(SQL_DROP_TIPS_REPORT_VIEW);
        db.execSQL(SQL_CREATE_TIPS_REPORT_VIEW);

        db.execSQL(SQL_DROP_EXPORT_SOLD_ITEMS);
        db.execSQL(SQL_DROP_EXPORT_TOP_ITEMS_VIEW);
        db.execSQL(SQL_DROP_SO_ITEMS_VIEW);
        db.execSQL(SQL_DROP_SO_ITEMS_FAST_VIEW);
        db.execSQL(SQL_DROP_SO_SALE_REPORTS_ITEMS_VIEW);
        db.execSQL(SQL_DROP_XREPORT_VIEW);
        db.execSQL(SQL_CREATE_EXPORT_SOLD_ITEMS);
        db.execSQL(SQL_CREATE_EXPORT_TOP_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_SO_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_SO_ITEMS_FAST_VIEW);
        db.execSQL(SQL_CREATE_SO_SALE_REPORTS_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_XREPORT_VIEW);
        db.execSQL(SQL_DROP_SO_IM_VIEW);
        db.execSQL(SQL_CREATE_SO_IM_VIEW);

        db.execSQL(SQL_DROP_CUSTOMER_VIEW);
        db.execSQL(SQL_DROP_PRINTER_VIEW);
        db.execSQL(SQL_DROP_ITEMS_EXT_VIEW);
        db.execSQL(SQL_DROP_PAYMENT_TRANSACTION_VIEW);
        db.execSQL(SQL_DROP_EXPORT_ITEMS_VIEW);
        db.execSQL(SQL_DROP_INVENTORY_LOG_VIEW);
        db.execSQL(SQL_DROP_KITCHEN_PRINT);
        db.execSQL(SQL_DROP_CREDIT_RECEIPT_VIEW);
        db.execSQL(SQL_DROP_SALE_ADDON_VIEW);
        db.execSQL(SQL_DROP_PREPAID_ORDER_VIEW);
        db.execSQL(SQL_DROP_TIPS_VIEW);
        db.execSQL(SQL_DROP_CATEGORY_VIEW);
        db.execSQL(SQL_CREATE_CUSTOMER_VIEW);
        db.execSQL(SQL_CREATE_PRINTER_VIEW);
        db.execSQL(SQL_CREATE_ITEMS_EXT_VIEW);
        db.execSQL(SQL_CREATE_PAYMENT_TRANSACTION_VIEW);
        db.execSQL(SQL_CREATE_EXPORT_ITEMS_VIEW);
        db.execSQL(SQL_CREATE_INVENTORY_LOG_VIEW);
        db.execSQL(SQL_CREATE_KITCHEN_PRINT);
        db.execSQL(SQL_CREATE_CREDIT_RECEIPT_VIEW);
        db.execSQL(SQL_CREATE_SALE_ADDON_VIEW);
        db.execSQL(SQL_CREATE_PREPAID_ORDER_VIEW);
        db.execSQL(SQL_CREATE_TIPS_VIEW);
        db.execSQL(SQL_CREATE_CATEGORY_VIEW);
        db.execSQL(SQL_CREATE_CATEGORY_SIMPLE_VIEW);
    }
    
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update4To5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION4;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5;
    }
}
