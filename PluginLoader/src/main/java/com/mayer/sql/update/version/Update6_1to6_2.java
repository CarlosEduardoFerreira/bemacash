package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

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
    private static final String SQL_CREATE_PAYMENT_TRANSACTION = "CREATE TABLE payment_transaction( guid TEXT PRIMARY KEY NOT NULL, order_guid TEXT NOT NULL, parent_guid TEXT, amount TEXT NOT NULL, status INTEGER NOT NULL, unitsLabel INTEGER NOT NULL, operator_id TEXT NOT NULL, gtw_id INTEGER, gtwp_id TEXT, gtw_preauth_payment_id TEXT, gateway_closed_perauth_guid TEXT, d_reason TEXT, create_time INTEGER NOT NULL, shift_guid TEXT NOT NULL, card_name TEXT, change_amount TEXT, is_preauth INTEGER, balance TEXT, cash_back TEXT, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(order_guid) REFERENCES sale_order(guid) ON DELETE CASCADE,FOREIGN KEY(parent_guid) REFERENCES payment_transaction(guid),FOREIGN KEY(operator_id) REFERENCES employee(guid),FOREIGN KEY(shift_guid) REFERENCES shift(guid))";
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
    private static final String SQL_SET_COLUMN_BP_DESCRIPTION = "update bp_description set sale_order_id = (select sale_order_item.order_id from sale_order_item where sale_order_item.item_id = bp_description.guid)";
    private static final String SQL_DELETE_INVALID_ROWS_BP_DESCRIPTION = "DELETE FROM bp_description WHERE sale_order_id IS NULL";

    private static final String SQL_RENAME_BP_DESCRIPTION = "ALTER TABLE bp_description RENAME TO bp_description_tmp;";
    private static final String SQL_CREATE_BP_DESCRIPTION = "CREATE TABLE bp_description( guid TEXT PRIMARY KEY NOT NULL, description TEXT NOT NULL, type INTEGER, is_voided INTEGER NOT NULL DEFAULT (0), is_failed INTEGER NOT NULL DEFAULT (0), order_id INTEGER, sale_order_id TEXT NOT NULL, is_deleted INTEGER DEFAULT (0), update_time INTEGER, is_draft INTEGER DEFAULT (0),FOREIGN KEY(sale_order_id) REFERENCES sale_order(guid) ON DELETE CASCADE)";
    private static final String SQL_CREATE_BP_DESCRIPTION_ORDER = "create index idx_bp_description_order on bp_description( sale_order_id)";
    private static final String SQL_COPY_BP_DESCRIPTION = "INSERT INTO bp_description(guid, description, type, is_voided, is_failed, order_id, sale_order_id, is_deleted, update_time, is_draft) SELECT guid, description, type, is_voided, is_failed, order_id, sale_order_id, is_deleted, update_time, is_draft FROM bp_description_tmp;";
    private static final String SQL_DROP_TEMP_BP_DESCRIPTION = "DROP TABLE bp_description_tmp;";

    static void update6_1to6_2(SQLiteDatabase db){

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

        db.execSQL(SQL_ADD_COLUMN_BP_DESCRIPTION);
        db.execSQL(SQL_SET_COLUMN_BP_DESCRIPTION);
        db.execSQL(SQL_DELETE_INVALID_ROWS_BP_DESCRIPTION);

        db.execSQL(SQL_RENAME_BP_DESCRIPTION);
        db.execSQL(SQL_CREATE_BP_DESCRIPTION);
        db.execSQL(SQL_CREATE_BP_DESCRIPTION_ORDER);
        db.execSQL(SQL_COPY_BP_DESCRIPTION);
        db.execSQL(SQL_DROP_TEMP_BP_DESCRIPTION);
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
