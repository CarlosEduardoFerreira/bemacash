package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_2to6_3 implements IUpdateContainer {

    private static final String SQL_CREATE_SALE_ITEM_DEPT_VIEW = "CREATE VIEW sale_item_dept_view AS SELECT sale_order_table.guid as sale_order_table_guid, sale_order_table.shift_guid as sale_order_table_shift_guid,sale_order_table.discount as sale_order_table_discount, sale_order_table.discount_type as sale_order_table_discount_type, payment_transaction_table.create_time as payment_transaction_table_create_time, payment_transaction_table.status as payment_transaction_table_status, sale_item_table.final_discount as sale_item_table_final_discount, sale_item_table.final_gross_price as sale_item_table_final_gross_price, sale_item_table.final_tax as sale_item_table_final_tax,sale_item_table.taxable as sale_item_table_taxable, sale_item_table.item_id as sale_item_table_item_id, sale_item_table.order_id as sale_item_table_order_id, sale_item_table.price as sale_item_table_price , sale_item_table.tax as sale_item_table_tax, sale_item_table.quantity as sale_item_table_quantity, sale_item_table.discount as sale_item_table_discount, sale_item_table.discount_type as sale_item_table_discount_type, sale_item_table.discountable as sale_item_table_discountable ,modifier_table.extra_cost as modifier_table_extra_cost, item_table.category_id as item_table_category_id, item_table.guid as item_table_guid, category_table.guid as category_table_guid, category_table.title as category_table_title, department_table.guid as department_table_guid, department_table.title as department_table_title FROM sale_order as sale_order_table JOIN sale_order_item as sale_item_table ON sale_order_table.guid = sale_item_table.order_id JOIN payment_transaction as payment_transaction_table ON payment_transaction_table.order_guid = sale_order_table.guid JOIN items_modifier as modifier_table ON sale_item_table.item_id = modifier_table.item_guid JOIN item as item_table ON sale_item_table.item_id = item_table.guid JOIN category as category_table ON category_table.guid = item_table.category_id JOIN department as department_table ON department_table.guid = category_table.department_guid";

    static void update6_2to6_3(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SALE_ITEM_DEPT_VIEW);
    }

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_2to6_3(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION6_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_3;
    }
}
