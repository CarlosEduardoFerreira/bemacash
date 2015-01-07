package com.kaching123.tcr.store;

import android.content.ContentValues;

import com.annotatedsql.annotation.provider.Provider;
import com.annotatedsql.annotation.provider.URI;
import com.annotatedsql.annotation.sql.Autoincrement;
import com.annotatedsql.annotation.sql.Column;
import com.annotatedsql.annotation.sql.Column.Type;
import com.annotatedsql.annotation.sql.Columns;
import com.annotatedsql.annotation.sql.ExcludeStaticWhere;
import com.annotatedsql.annotation.sql.From;
import com.annotatedsql.annotation.sql.IgnoreColumns;
import com.annotatedsql.annotation.sql.Index;
import com.annotatedsql.annotation.sql.Indexes;
import com.annotatedsql.annotation.sql.Join;
import com.annotatedsql.annotation.sql.NotNull;
import com.annotatedsql.annotation.sql.PrimaryKey;
import com.annotatedsql.annotation.sql.RawJoin;
import com.annotatedsql.annotation.sql.RawQuery;
import com.annotatedsql.annotation.sql.Schema;
import com.annotatedsql.annotation.sql.SimpleView;
import com.annotatedsql.annotation.sql.SqlQuery;
import com.annotatedsql.annotation.sql.StaticWhere;
import com.annotatedsql.annotation.sql.Table;
import com.annotatedsql.annotation.sql.Unique;

import static com.kaching123.tcr.store.ShopSchemaEx.ForeignKey.foreignKey;
import static com.kaching123.tcr.store.ShopSchemaEx.applyForeignKeys;
import static com.kaching123.tcr.store.ShopSchemaEx.applyTmpFields;

@Schema(className = "ShopSchema", dbName = "shop.db", dbVersion = 295)
@Provider(name = "ShopProvider", authority = "com.kaching123.tcr.store.AUTHORITY", schemaClass = "ShopSchema", openHelperClass = "ShopOpenHelper")
public abstract class ShopStore {

    /*force static initialization of class*/
    public static void init() {
    }

    public static final String DEFAULT_UPDATE_TIME = "update_time";
    public static final String DEFAULT_IS_DELETED = "is_deleted";
    public static final String DEFAULT_IS_DRAFT = "is_draft";

    public static final ContentValues DELETE_VALUES = new ContentValues(1);

    static {
        DELETE_VALUES.put(ShopStore.DEFAULT_IS_DELETED, 1);
    }

    //@StaticWhere(column = ISupportDraftTable.UPDATE_IS_DRAFT, value = "0")
    public static interface ISupportDraftTable {
        @Column(type = Column.Type.INTEGER, defVal = "0")
        String UPDATE_IS_DRAFT = DEFAULT_IS_DRAFT;
    }

    @StaticWhere(column = IBemaSyncTable.IS_DELETED, value = "0")
    public static interface IBemaSyncTable extends ISupportDraftTable {

        @Column(type = Type.INTEGER, defVal = "0")
        String IS_DELETED = DEFAULT_IS_DELETED;

        @Column(type = Column.Type.INTEGER)
        String UPDATE_TIME = DEFAULT_UPDATE_TIME;

    }

    @Table(BillPayment.TABLE_NAME)
    public static interface BillPayment {
        @URI
        String URI_CONTENT = "BillPayment_item";
        String TABLE_NAME = "BillPayment_item";
        @PrimaryKey
        @Autoincrement
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Column(type = Type.TEXT)
        String CATEGORYID = "categoryId";

        @Column(type = Type.TEXT)
        String CATEGORYDESCRIPTION = "categoryDescription";

        @Column(type = Type.TEXT)
        String MASTERBILLERID = "masterBillerId";

        @Column(type = Type.TEXT)
        String MASTERBILLERDESCRIPTION = "masterBillerDescription";
    }

    @Table(WirelessTable.TABLE_NAME)
    public static interface WirelessTable {

        @URI
        String URI_CONTENT = "wireless_item";

        String TABLE_NAME = "wireless_item";

        @PrimaryKey
        @Autoincrement
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Column(type = Type.TEXT)
        String CODE = "code";

        @Column(type = Type.TEXT)
        String NAME = "name";

        @Column(type = Type.TEXT)
        String CARRIERNAME = "carrierName";

        @Column(type = Type.TEXT)
        String COUNTRYCODE = "countryCode";

        @Column(type = Type.TEXT)
        String COUNTRYNAME = "countryName";

        @Column(type = Type.TEXT)
        String TYPE = "type";

        @Column(type = Type.TEXT)
        String URL = "url";

        @Column(type = Type.INTEGER)
        String USEFIXEDDENOMINATIONS = "useFixedDenominations";

        @Column(type = Column.Type.TEXT)
        String DENOMINATIONS = "denominations";

        @Column(type = Column.Type.TEXT)

        String MINDENOMINATION = "minDenomination";

        @Column(type = Column.Type.TEXT)
        String MAXDENOMINATION = "maxDenomination";

        @Column(type = Type.TEXT)
        String DIALCOUNTRYCODE = "dialCountryCode";


        @Column(type = Type.TEXT)
        String TERMSANDCONDITIONS = "TermsAndConditions";

        @Column(type = Type.TEXT)
        String PRODUCTACCESSPHONES = "productAccessPhones";

        @Column(type = Type.INTEGER)
        String MERCHANGTBUYINGFREQUENCY = "merchantBuyingFrequency";

        @Column(type = Type.INTEGER)
        String ZIPCODEBUYFREQUENCY = "zipCodeBuyingFrequency";
    }

    @Table(UnitTable.TABLE_NAME)
    @Indexes({
            @Index(name = "item", columns = UnitTable.ITEM_ID),
            @Index(name = "sale_item", columns = UnitTable.SALE_ORDER_ID),
            @Index(name = "child_sale_item", columns = UnitTable.CHILD_ORDER_ID)
    })
    public static interface UnitTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "unit";

        String TABLE_NAME = "unit";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String ID = "_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_ID = "item_id";

        @Column(type = Column.Type.TEXT)
        String SERIAL_CODE = "serial_code";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String CODE_TYPE = "code_type";

        @Column(type = Type.INTEGER)
        String STATUS = "status";

        @Column(type = Type.INTEGER)
        String WARRANTY_PERIOD = "warranty_period";

        @Column(type = Type.TEXT)
        String SALE_ORDER_ID = "sale_order_item_id";

        @Column(type = Type.TEXT)
        String CHILD_ORDER_ID = "child_order_item_id";
    }

    static {
        applyForeignKeys(UnitTable.TABLE_NAME,
                foreignKey(UnitTable.ITEM_ID, ItemTable.TABLE_NAME, ItemTable.GUID),
                foreignKey(UnitTable.SALE_ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID),
                foreignKey(UnitTable.CHILD_ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID)
        );
    }

    @Table(DepartmentTable.TABLE_NAME)
    public static interface DepartmentTable extends IBemaSyncTable {

        @URI(altNotify = {CategorySimpleView.URI_CONTENT})
        String URI_CONTENT = "department";

        String TABLE_NAME = "department";

        @PrimaryKey
        @Autoincrement
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @Unique
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String TITLE = "title";
    }

    @Table(CategoryTable.TABLE_NAME)
    @Index(name = "deps", columns = CategoryTable.DEPARTMENT_GUID)
    public static interface CategoryTable extends IBemaSyncTable {

        @URI(altNotify = {CategoryView.URI_CONTENT, CategorySimpleView.URI_CONTENT})
        String URI_CONTENT = "category";

        String TABLE_NAME = "category";

        @PrimaryKey
        @Autoincrement
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @Unique
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String DEPARTMENT_GUID = "department_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String TITLE = "title";

        @Column(type = Column.Type.TEXT)
        String IMAGE = "image";

        @Column(type = Type.INTEGER)
        String ORDER_NUM = "order_num";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "1")
        String ELIGIBLE_FOR_COMMISSION = "eligible_for_commission";

        @Column(type = Column.Type.TEXT)
        String COMMISSION = "commission";

    }

    static {
        applyForeignKeys(CategoryTable.TABLE_NAME,
                foreignKey(CategoryTable.DEPARTMENT_GUID, DepartmentTable.TABLE_NAME, DepartmentTable.GUID));
    }

    @Table(ItemTable.TABLE_NAME)
    @Indexes({
            @Index(name = "cats", columns = ItemTable.CATEGORY_ID),
            @Index(name = "ean", columns = ItemTable.EAN_CODE),
            @Index(name = "product", columns = ItemTable.PRODUCT_CODE),
            @Index(name = "tax_group", columns = ItemTable.TAX_GROUP_GUID),
            @Index(name = "printer_alias", columns = ItemTable.PRINTER_ALIAS_GUID),
            @Index(name = "movements", columns = ItemTable.UPDATE_QTY_FLAG)
    })
    public static interface ItemTable extends IBemaSyncTable {

        @URI(altNotify = {ItemExtView.URI_CONTENT, CategoryView.URI_CONTENT})
        String URI_CONTENT = "item";

        String TABLE_NAME = "item";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String CATEGORY_ID = "category_id";

        @Column(type = Column.Type.TEXT)
        String DESCRIPTION = "description";

        @Column(type = Column.Type.TEXT)
        String CODE = "code";

        @Column(type = Column.Type.TEXT)
        String EAN_CODE = "ean_code";

        @Column(type = Column.Type.TEXT)
        String PRODUCT_CODE = "product_code";

        @Column(type = Column.Type.INTEGER)
        String PRICE_TYPE = "price_type";

        @NotNull
        @Column(type = Column.Type.INTEGER, defVal = "0")
        String SERIALIZABLE = "serializable";

        @Column(type = Column.Type.TEXT)
        String SALE_PRICE = "sale_price";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String UNITS_LABEL = "units_label";

        @Column(type = Column.Type.INTEGER)
        String STOCK_TRACKING = "stock_tracking";

        @Column(type = Column.Type.INTEGER)
        String ACTIVE_STATUS = "active_status";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNTABLE = "discountable";

        @Column(type = Column.Type.TEXT)
        String DISCOUNT = "discount";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNT_TYPE = "discount_type";

        @Column(type = Column.Type.INTEGER)
        String TAXABLE = "taxable";

        @Column(type = Column.Type.TEXT)
        String COST = "cost";

        @Column(type = Column.Type.TEXT)
        String MINIMUM_QTY = "minimum_qty";

        @Column(type = Column.Type.TEXT)
        String RECOMMENDED_QTY = "recommended_qty";

        @NotNull
        @Column(type = Type.TEXT)
        String UPDATE_QTY_FLAG = "UPDATE_QTY_FLAG";

        @Column(type = Type.TEXT)
        String TAX_GROUP_GUID = "tax_group_guid";

        @Column(type = Type.TEXT)
        String TMP_AVAILABLE_QTY = "tmp_available_qty";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String ORDER_NUM = "order_num";

        @Column(type = Type.TEXT)
        String DEFAULT_MODIFIER_GUID = "default_modifier_guid";

        @Column(type = Column.Type.TEXT)
        String PRINTER_ALIAS_GUID = "printer_alias_guid";

        @Column(type = Column.Type.INTEGER)
        String BUTTON_VIEW = "button_view";

        @Column(type = Column.Type.INTEGER)
        String HAS_NOTES = "has_notes";

        @Column(type = Column.Type.INTEGER)
        String CODE_TYPE = "code_type";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "1")
        String ELIGIBLE_FOR_COMMISSION = "eligible_for_commission";

        @Column(type = Column.Type.TEXT)
        String COMMISSION = "commission";
    }

    static {
        applyForeignKeys(ItemTable.TABLE_NAME,
                foreignKey(ItemTable.CATEGORY_ID, CategoryTable.TABLE_NAME, CategoryTable.GUID),
                foreignKey(ItemTable.TAX_GROUP_GUID, TaxGroupTable.TABLE_NAME, TaxGroupTable.GUID),
                foreignKey(ItemTable.PRINTER_ALIAS_GUID, PrinterAliasTable.TABLE_NAME, PrinterAliasTable.GUID)
        );

        applyTmpFields(ItemTable.TABLE_NAME, ItemTable.TMP_AVAILABLE_QTY);
    }

    @Table(ItemMovementTable.TABLE_NAME)
    @PrimaryKey(columns = {ItemMovementTable.GUID, ItemMovementTable.ITEM_GUID})
    @Indexes({
            @Index(name = "item", columns = ItemMovementTable.ITEM_GUID),
            @Index(name = "mvn_flag", columns = ItemMovementTable.ITEM_UPDATE_QTY_FLAG)
    })
    public static interface ItemMovementTable extends IBemaSyncTable {

        @URI(altNotify = SaleOrderItemsView.URI_CONTENT)
        String URI_CONTENT = "item_movement";

        String TABLE_NAME = "item_movement";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_GUID = "item_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String QTY = "qty";

        @NotNull
        @Column(type = Type.INTEGER)
        String ITEM_UPDATE_QTY_FLAG = "ITEM_UPDATE_QTY_FLAG";

        @Column(type = Type.INTEGER)
        String MANUAL = "manual";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @Column(type = Type.TEXT)
        String TMP_AVAILABLE_QTY = "tmp_available_qty";
    }

    static {
        applyForeignKeys(ItemMovementTable.TABLE_NAME,
                foreignKey(ItemMovementTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));

        applyTmpFields(ItemMovementTable.TABLE_NAME, ItemMovementTable.TMP_AVAILABLE_QTY);
    }

    @Table(ModifierTable.TABLE_NAME)
    @Index(name = "item", columns = ModifierTable.ITEM_GUID)
    public static interface ModifierTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "items_modifier";

        String TABLE_NAME = "items_modifier";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String MODIFIER_GUID = "modifier_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String TITLE = "title";

        @NotNull
        @Column(type = Type.INTEGER)
        String TYPE = "unitsLabel";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_GUID = "item_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String EXTRA_COST = "extra_cost";
    }

    static {
        applyForeignKeys(ModifierTable.TABLE_NAME,
                foreignKey(ModifierTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));
    }

    @Table(SaleOrderTable.TABLE_NAME)
    @Indexes({
            @Index(name = "create_time", columns = SaleOrderTable.CREATE_TIME),
            @Index(name = "operator", columns = SaleOrderTable.OPERATOR_GUID),
            @Index(name = "customer", columns = SaleOrderTable.CUSTOMER_GUID),
            @Index(name = "register", columns = SaleOrderTable.REGISTER_ID),
            @Index(name = "shift", columns = SaleOrderTable.SHIFT_GUID),
            @Index(name = "status", columns = SaleOrderTable.STATUS),
            @Index(name = "parent", columns = SaleOrderTable.PARENT_ID)
    })
    public static interface SaleOrderTable extends IBemaSyncTable {

        @URI(altNotify = {SaleOrderItemsView.URI_CONTENT, SaleOrderTipsQuery.URI_CONTENT})
        String URI_CONTENT = "sale_order";

        String TABLE_NAME = "sale_order";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String OPERATOR_GUID = "operator_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String SHIFT_GUID = "shift_guid";

        @Column(type = Column.Type.TEXT)
        String CUSTOMER_GUID = "customer_guid";

        @Column(type = Column.Type.TEXT)
        String DISCOUNT = "discount";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNT_TYPE = "discount_type";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";

        @Column(type = Column.Type.TEXT)
        String HOLD_NAME = "hold_name";

        @Column(type = Type.INTEGER)
        String TAXABLE = "taxable";

        @Column(type = Type.INTEGER)
        String PRINT_SEQ_NUM = "print_seq_num";

        @NotNull
        @Column(type = Type.INTEGER)
        String REGISTER_ID = "register_id";

        @Column(type = Type.TEXT)
        String PARENT_ID = "parent_id";

        @Column(type = Type.INTEGER)
        String ORDER_TYPE = "order_type";

        @Column(type = Column.Type.TEXT)
        String TML_TOTAL_PRICE = "tml_total_price";

        @Column(type = Column.Type.TEXT)
        String TML_TOTAL_TAX = "tml_total_tax";

        @Column(type = Column.Type.TEXT)
        String TML_TOTAL_DISCOUNT = "tml_total_discount";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String IS_TIPPED = "is_tipped";

        @Column(type = Type.INTEGER)
        String KITCHEN_PRINT_STATUS = "kitchen_print_status";

        @Column(type = Column.Type.TEXT)
        String TRANSACTION_FEE = "transaction_fee";
    }

    static {
        applyForeignKeys(SaleOrderTable.TABLE_NAME,
                foreignKey(SaleOrderTable.OPERATOR_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(SaleOrderTable.SHIFT_GUID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(SaleOrderTable.CUSTOMER_GUID, CustomerTable.TABLE_NAME, CustomerTable.GUID),
                foreignKey(SaleOrderTable.REGISTER_ID, RegisterTable.TABLE_NAME, RegisterTable.ID),
                foreignKey(SaleOrderTable.PARENT_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID)
        );

        applyTmpFields(SaleOrderTable.TABLE_NAME,
                SaleOrderTable.TML_TOTAL_PRICE,
                SaleOrderTable.TML_TOTAL_DISCOUNT,
                SaleOrderTable.TML_TOTAL_TAX);
    }

    @Table(SaleItemTable.TABLE_NAME)
    @Indexes({
            @Index(name = "order", columns = SaleItemTable.ORDER_GUID),
            @Index(name = "item_guid", columns = SaleItemTable.ITEM_GUID),
            @Index(name = "parent", columns = SaleItemTable.PARENT_GUID)
    })
    public static interface SaleItemTable extends IBemaSyncTable {

        //@Trigger(when = When.AFTER, title = "updateSoTpSi")
        @URI(altNotify = SaleOrderItemsView.URI_CONTENT)
        String URI_CONTENT = "sale_order_item";

        String TABLE_NAME = "sale_order_item";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String SALE_ITEM_GUID = "sale_order_item_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ORDER_GUID = "order_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_GUID = "item_id";

        @Column(type = Column.Type.TEXT)
        String QUANTITY = "quantity";

        @Column(type = Column.Type.TEXT, defVal = "0.000")
        String KITCHEN_PRINTED_QTY = "kitchen_printed_qty";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String PRICE = "price";

        @Column(type = Column.Type.INTEGER)
        String PRICE_TYPE = "price_type";

        @Column(type = Type.INTEGER)
        String DISCOUNTABLE = "discountable";

        @Column(type = Column.Type.TEXT)
        String DISCOUNT = "discount";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNT_TYPE = "discount_type";

        @Column(type = Column.Type.INTEGER)
        String TAXABLE = "taxable";

        @Column(type = Type.TEXT)
        String TAX = "tax";

        @Column(type = Column.Type.INTEGER)
        String SEQUENCE = "sequence";

        @Column(type = Column.Type.TEXT)
        String PARENT_GUID = "parent_guid";

        @Column(type = Column.Type.TEXT)
        String FINAL_GROSS_PRICE = "final_gross_price";//with addons

        @Column(type = Column.Type.TEXT)
        String FINAL_TAX = "final_tax";

        @Column(type = Column.Type.TEXT)
        String FINAL_DISCOUNT = "final_discount";

        @Column(type = Column.Type.TEXT)
        String TMP_REFUND_QUANTITY = "tmp_refund_quantity";

        @Column(type = Column.Type.TEXT)
        String NOTES = "notes";

        @Column(type = Type.INTEGER)
        String HAS_NOTES = "has_notes";
    }

    static {
        applyForeignKeys(SaleItemTable.TABLE_NAME,
                foreignKey(SaleItemTable.ORDER_GUID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID),
                foreignKey(SaleItemTable.PARENT_GUID, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID)
        );

        applyTmpFields(SaleItemTable.TABLE_NAME, SaleItemTable.TMP_REFUND_QUANTITY);
    }

    @Table(SaleAddonTable.TABLE_NAME)
    @Indexes({
            @Index(name = "item_guid", columns = SaleAddonTable.ITEM_GUID),
            @Index(name = "addon_guid", columns = SaleAddonTable.ADDON_GUID)
    })
    public static interface SaleAddonTable extends IBemaSyncTable {

        //@Trigger(when = When.AFTER, title = "updateSoTpSa")
        @URI(altNotify = SaleOrderItemsView.URI_CONTENT)
        String URI_CONTENT = "sale_order_item_addon";

        String TABLE_NAME = "sale_order_item_addon";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ADDON_GUID = "addon_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_GUID = "item_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String EXTRA_COST = "extra_cost";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String TYPE = "addon_type";
    }

    static {
        applyForeignKeys(SaleAddonTable.TABLE_NAME,
                foreignKey(SaleAddonTable.ADDON_GUID, ModifierTable.TABLE_NAME, ModifierTable.MODIFIER_GUID),
                foreignKey(SaleAddonTable.ITEM_GUID, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID)
        );
    }

    /*@Table(PurchaseOrderTable.TABLE_NAME)
    public static interface PurchaseOrderTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "purchase_order";

        String TABLE_NAME = "purchase_order";

        @PrimaryKey
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @Column(type = Column.Type.INTEGER)
        String CREATE_TIME = "create_time";

        @Column(type = Column.Type.INTEGER)
        String OPERATOR_ID = "operator_id";

        @Column(type = Column.Type.INTEGER)
        String SUPPLIER_ID = "supplier_id";

        @Column(type = Column.Type.TEXT)
        String TOTAL_COST = "total_cost";

        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";
    }

    @Table(PurchaseItemTable.TABLE_NAME)
    @PrimaryKey(columns = {PurchaseItemTable.ORDER_ID, PurchaseItemTable.ITEM_ID})
    public static interface PurchaseItemTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "purchase_order_item";

        String TABLE_NAME = "purchase_order_item";

        @Column(type = Column.Type.INTEGER)
        String ORDER_ID = "order_id";

        @Column(type = Column.Type.INTEGER)
        String ITEM_ID = "item_id";

        @Column(type = Column.Type.TEXT)
        String QUANTITY = "quantity";
    }

    @Table(SupplierTablet.TABLE_NAME)
    public static interface SupplierTablet extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "supplier";

        String TABLE_NAME = "supplier";

        @PrimaryKey
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @Column(type = Column.Type.TEXT)
        String FULLNAME = "fullname";

        @Column(type = Column.Type.TEXT)
        String COMPANY_NAME = "company_name";

        @Column(type = Column.Type.TEXT)
        String ADDRESS = "address";

        @Column(type = Column.Type.TEXT)
        String PHONE = "phone";

        @Column(type = Column.Type.TEXT)
        String FAX = "fax";

        @Column(type = Column.Type.TEXT)
        String EMAIL = "email";
    }*/

    @Table(EmployeeTable.TABLE_NAME)
    @Indexes({
            @Index(name = "first_name", columns = EmployeeTable.FIRST_NAME),
            @Index(name = "email", columns = EmployeeTable.EMAIL)
    })
    public static interface EmployeeTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "employee";

        String TABLE_NAME = "employee";

        @PrimaryKey
        @Autoincrement
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @Unique
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String FIRST_NAME = "first_name";

        @Column(type = Column.Type.TEXT)
        String LAST_NAME = "last_name";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String LOGIN = "login";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String PASSWORD = "password";

        @Column(type = Column.Type.TEXT)
        String STREET = "street";

        @Column(type = Column.Type.TEXT)
        String COMPLEMENTARY = "complementary";

        @Column(type = Column.Type.TEXT)
        String CITY = "city";

        @Column(type = Column.Type.TEXT)
        String STATE = "state";

        @Column(type = Column.Type.TEXT)
        String COUNTRY = "country";

        @Column(type = Column.Type.TEXT)
        String ZIP = "zip";

        @Column(type = Column.Type.TEXT)
        String PHONE = "phone";

        @Column(type = Column.Type.TEXT)
        String EMAIL = "email";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String SEX = "sex";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String HIRE_DATE = "hire_date";

        @Column(type = Column.Type.INTEGER)
        String FIRE_DATE = "fire_date";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";

        @Column(type = Column.Type.TEXT)
        String HOURLY_RATE = "hourly_rate";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String SHOP_ID = "shop_id";

        @NotNull
        @Column(type = Column.Type.INTEGER, defVal = "0")
        String TIPS_ELIGIBLE = "tips_eligible";

        @NotNull
        @Column(type = Column.Type.INTEGER, defVal = "1")
        String ELIGIBLE_FOR_COMMISSION = "eligible_for_commission";

        @Column(type = Column.Type.TEXT)
        String COMMISSION = "commission";

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String IS_MERCHANT = "is_merchant";
    }

    @Table(EmployeePermissionTable.TABLE_NAME)
    @Index(name = "user", columns = EmployeePermissionTable.USER_GUID)
    @PrimaryKey(columns = {EmployeePermissionTable.USER_GUID, EmployeePermissionTable.PERMISSION_ID})
    public static interface EmployeePermissionTable {

        @URI
        String URI_CONTENT = "employee_permission";

        String TABLE_NAME = "employee_permission";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String USER_GUID = "user_guid";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String PERMISSION_ID = "permission_id";

        @NotNull
        @Column(type = Column.Type.INTEGER, defVal = "0")
        String ENABLED = "enabled";

        @Column(type = Column.Type.INTEGER)
        String UPDATE_TIME = DEFAULT_UPDATE_TIME;

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String UPDATE_IS_DRAFT = DEFAULT_IS_DRAFT;
    }

    static {
        applyForeignKeys(EmployeePermissionTable.TABLE_NAME,
                foreignKey(EmployeePermissionTable.USER_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID)
        );
    }

    @Table(ShiftTable.TABLE_NAME)
    @Indexes({
            @Index(name = "start_time", columns = ShiftTable.START_TIME),
            @Index(name = "open_manager", columns = ShiftTable.OPEN_MANAGER_ID),
            @Index(name = "close_manager", columns = ShiftTable.CLOSE_MANAGER_ID),
            @Index(name = "register", columns = ShiftTable.REGISTER_ID)
    })
    public static interface ShiftTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "shift";

        String TABLE_NAME = "shift";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String START_TIME = "start_time";

        @Column(type = Column.Type.INTEGER)
        String END_TIME = "end_time";

        @NotNull
        @Column(type = Type.TEXT)
        String OPEN_MANAGER_ID = "open_manager_id";

        @Column(type = Type.TEXT)
        String CLOSE_MANAGER_ID = "close_manager_id";

        @NotNull
        @Column(type = Type.INTEGER)
        String REGISTER_ID = "register_id";

        @Column(type = Type.TEXT)
        String OPEN_AMOUNT = "open_amount";

        @Column(type = Type.TEXT)
        String CLOSE_AMOUNT = "close_amount";
    }

    static {
        applyForeignKeys(ShiftTable.TABLE_NAME,
                foreignKey(ShiftTable.OPEN_MANAGER_ID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(ShiftTable.CLOSE_MANAGER_ID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(ShiftTable.REGISTER_ID, RegisterTable.TABLE_NAME, RegisterTable.ID)
        );
    }

    @Table(CashDrawerMovementTable.TABLE_NAME)
    @Indexes({
            @Index(name = "shift", columns = CashDrawerMovementTable.SHIFT_GUID),
            @Index(name = "manager", columns = CashDrawerMovementTable.MANAGER_GUID)
    })
    public static interface CashDrawerMovementTable extends IBemaSyncTable {

        @URI(altNotify = ShiftTable.URI_CONTENT)
        String URI_CONTENT = "cashdrawer_tr";

        String TABLE_NAME = "cashdrawer_tr";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String SHIFT_GUID = "shift_guid";

        @NotNull
        @Column(type = Type.TEXT)
        String MANAGER_GUID = "manager_guid";

        @NotNull
        @Column(type = Type.INTEGER)
        String TYPE = "type";

        @NotNull
        @Column(type = Type.TEXT)
        String AMOUNT = "amount";

        @Column(type = Column.Type.INTEGER)
        String MOVEMENT_TIME = "MOVEMENT_TIME";

        @Column(type = Type.TEXT)
        String COMMENT = "comment";
    }

    static {
        applyForeignKeys(CashDrawerMovementTable.TABLE_NAME,
                foreignKey(CashDrawerMovementTable.SHIFT_GUID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(CashDrawerMovementTable.MANAGER_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID));
    }

    @Table(SqlCommandTable.TABLE_NAME)
    public static interface SqlCommandTable {

        @URI
        String URI_CONTENT = "sql_command";

        String TABLE_NAME = "sql_command";

        @PrimaryKey
        @Autoincrement
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String CREATE_TIME = "create_time";

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String IS_SENT = "is_sent";

        @Column(type = Column.Type.TEXT)
        String SQL_COMMAND = "sql_command";

        @Column(type = Type.INTEGER, defVal = "2")
        String API_VERSION = "api_v";

        @Column(type = Column.Type.INTEGER)
        String UPDATE_TIME = DEFAULT_UPDATE_TIME;
    }

    @Table(PaymentTransactionTable.TABLE_NAME)
    @Indexes({
            @Index(name = "order_guid", columns = PaymentTransactionTable.ORDER_GUID),
            @Index(name = "parent", columns = PaymentTransactionTable.PARENT_GUID),
            @Index(name = "status", columns = PaymentTransactionTable.STATUS),
            @Index(name = "type", columns = PaymentTransactionTable.TYPE),
            @Index(name = "operator", columns = PaymentTransactionTable.OPERATOR_GUID),
            @Index(name = "shift", columns = PaymentTransactionTable.SHIFT_GUID)
    })
    public static interface PaymentTransactionTable extends IBemaSyncTable {

        @URI(altNotify = {ShiftTable.URI_CONTENT, PaymentTransactionView.URI_CONTENT, SaleOrderTipsQuery.URI_CONTENT})
        String URI_CONTENT = "payment_transaction";

        String TABLE_NAME = "payment_transaction";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ORDER_GUID = "order_guid";

        @Column(type = Column.Type.TEXT)
        String PARENT_GUID = "parent_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String AMOUNT = "amount";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String TYPE = "unitsLabel";

        @NotNull
        @Column(type = Type.TEXT)
        String OPERATOR_GUID = "operator_id";

        @Column(type = Column.Type.INTEGER)
        String GATEWAY = "gtw_id";

        @Column(type = Column.Type.TEXT)
        String GATEWAY_PAYMENT_ID = "gtwp_id";

        @Column(type = Column.Type.TEXT)
        String GATEWAY_PREAUTH_PAYMENT_ID = "gtw_preauth_payment_id";

        @Column(type = Column.Type.TEXT)
        String GATEWAY_CLOSED_PERAUTH_GUID = "gateway_closed_perauth_guid";

        @Column(type = Column.Type.TEXT)
        String DECLINE_REASON = "d_reason";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Type.TEXT)
        String SHIFT_GUID = "shift_guid";

        @Column(type = Type.TEXT)
        String CARD_NAME = "card_name";

        @Column(type = Type.TEXT)
        String CHANGE_AMOUNT = "change_amount";

        @Column(type = Column.Type.INTEGER)
        String IS_PREAUTH = "is_preauth";

        @Column(type = Column.Type.TEXT)
        String BALANCE = "balance";

        @Column(type = Column.Type.TEXT)
        String CASH_BACK = "cash_back";
    }

    static {
        applyForeignKeys(PaymentTransactionTable.TABLE_NAME,
                foreignKey(PaymentTransactionTable.ORDER_GUID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID),
                foreignKey(PaymentTransactionTable.PARENT_GUID, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID),
                foreignKey(PaymentTransactionTable.OPERATOR_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(PaymentTransactionTable.SHIFT_GUID, ShiftTable.TABLE_NAME, ShiftTable.GUID)
        );
    }

    @Table(EmployeeTimesheetTable.TABLE_NAME)
    @Indexes({
            @Index(name = "employee", columns = EmployeeTimesheetTable.EMPLOYEE_GUID),
            @Index(name = "clock_in", columns = EmployeeTimesheetTable.CLOCK_IN)
    })
    public static interface EmployeeTimesheetTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "employee_timesheet";

        String TABLE_NAME = "employee_timesheet";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String EMPLOYEE_GUID = "employee_guid";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String CLOCK_IN = "clock_in";

        @Column(type = Column.Type.INTEGER)
        String CLOCK_OUT = "clock_out";
    }

    static {
        applyForeignKeys(EmployeeTimesheetTable.TABLE_NAME,
                foreignKey(EmployeeTimesheetTable.EMPLOYEE_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID)
        );
    }

    @Table(TaxGroupTable.TABLE_NAME)
    public static interface TaxGroupTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "tax_group";

        String TABLE_NAME = "tax_group";

        @PrimaryKey
        @Autoincrement
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @Unique
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String TITLE = "title";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String TAX = "tax";
    }

    @Table(RegisterTable.TABLE_NAME)
    public static interface RegisterTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "register";

        String TABLE_NAME = "register";

        @PrimaryKey
        @Column(type = Column.Type.INTEGER)
        String ID = "_id";

        @NotNull
        @Column(type = Type.TEXT)
        String REGISTER_SERIAL = "register_serial";

        @Column(type = Column.Type.TEXT)
        String TITLE = "title";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";

        @Column(type = Column.Type.INTEGER)
        String PREPAID_TID = "prepaid_tid";

        @Column(type = Column.Type.INTEGER)
        String BLACKSTONE_PAYMENT_CID = "blackstone_payment_cid";
    }

    @Table(BillPaymentDescriptionTable.TABLE_NAME)
    public static interface BillPaymentDescriptionTable extends IBemaSyncTable {
        @URI
        String URI_CONTENT = "bp_description";

        String TABLE_NAME = "bp_description";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String DESCRIPTION = "description";

        @Column(type = Type.INTEGER)
        String TYPE = "type";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String IS_VOIDED = "is_voided";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String IS_FAILED = "is_failed";

        @Column(type = Type.INTEGER)
        String ORDER_ID = "order_id";
    }

    @Table(CustomerTable.TABLE_NAME)
    @Indexes({
            @Index(name = "last_name", columns = CustomerTable.LAST_NAME),
            @Index(name = "email", columns = CustomerTable.EMAIL)
    })
    public static interface CustomerTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "customer";

        String TABLE_NAME = "customer";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String FISRT_NAME = "first_name";

        @Column(type = Column.Type.TEXT)
        String LAST_NAME = "last_name";

        @Column(type = Column.Type.TEXT)
        String STREET = "street";

        @Column(type = Column.Type.TEXT)
        String COMPLEMENTARY = "complementary";

        @Column(type = Column.Type.TEXT)
        String CITY = "city";

        @Column(type = Column.Type.TEXT)
        String STATE = "state";

        @Column(type = Column.Type.TEXT)
        String COUNTRY = "country";

        @Column(type = Column.Type.TEXT)
        String ZIP = "zip";

        @Column(type = Column.Type.TEXT)
        String EMAIL = "email";

        @Column(type = Column.Type.TEXT)
        String PHONE = "phone";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String SEX = "sex";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Column.Type.INTEGER, defVal = "0")
        String CONSENT_PROMOTIONS = "consent_promotions";
    }

    @Table(PrinterAliasTable.TABLE_NAME)
    public static interface PrinterAliasTable extends IBemaSyncTable {
        @URI
        String URI_CONTENT = "printer_alias_table";

        String TABLE_NAME = "printer_alias_table";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String ALIAS = "alias";
    }

    @Table(PaxTable.TABLE_NAME)
    public static interface PaxTable {

        @URI
        String URI_CONTENT = "pax_table";

        String TABLE_NAME = "pax_table";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String IP = "ip";

        @NotNull
        @Column(type = Type.INTEGER)
        String PORT = "port";

        @Column(type = Type.TEXT)
        String MAC = "mac";

        @Column(type = Type.TEXT)
        String SUBNET = "subnet";

        @Column(type = Type.TEXT)
        String GATEWAY = "gateway";

        @Column(type = Type.TEXT)
        String DHCP = "dhcp";

    }

    @Table(PrinterTable.TABLE_NAME)
    public static interface PrinterTable {

        @URI(altNotify = {PrinterView.URI_CONTENT})
        String URI_CONTENT = "printer_table";

        String TABLE_NAME = "printer_able";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String IP = "ip";

        @NotNull
        @Column(type = Type.INTEGER)
        String PORT = "port";

        @Column(type = Type.TEXT)
        String ALIAS_GUID = "alias_guid";

        @Column(type = Type.TEXT)
        String MAC = "mac";

        @Column(type = Type.TEXT)
        String SUBNET = "subnet";

        @Column(type = Type.TEXT)
        String GATEWAY = "gateway";

        @Column(type = Type.TEXT)
        String DHCP = "dhcp";

    }

    @Table(CreditReceiptTable.TABLE_NAME)
    @Indexes({
            @Index(name = "register", columns = CreditReceiptTable.REGISTER_ID),
            @Index(name = "cashier", columns = CreditReceiptTable.CASHIER_GUID),
            @Index(name = "shift", columns = CreditReceiptTable.SHIFT_ID)
    })
    public static interface CreditReceiptTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "credit_receipt_table";

        String TABLE_NAME = "credit_receipt_table";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String CASHIER_GUID = "cashier_guid";

        @NotNull
        @Column(type = Type.INTEGER)
        String REGISTER_ID = "register_id";

        @NotNull
        @Column(type = Type.TEXT)
        String SHIFT_ID = "shift_id";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Type.TEXT)
        String AMOUNT = "amount";

        @NotNull
        @Column(type = Type.INTEGER)
        String PRINT_NUMBER = "print_number";

        @NotNull
        @Column(type = Type.INTEGER)
        String EXPIRE_TIME = "expire_time";
    }

    static {
        applyForeignKeys(CreditReceiptTable.TABLE_NAME,
                foreignKey(CreditReceiptTable.CASHIER_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(CreditReceiptTable.REGISTER_ID, RegisterTable.TABLE_NAME, RegisterTable.ID),
                foreignKey(CreditReceiptTable.SHIFT_ID, ShiftTable.TABLE_NAME, ShiftTable.GUID)
        );
    }

    @Table(EmployeeTipsTable.TABLE_NAME)
    @Indexes({
            @Index(name = "shift", columns = EmployeeTipsTable.SHIFT_ID),
            @Index(name = "cashier", columns = EmployeeTipsTable.EMPLOYEE_ID),
            @Index(name = "order", columns = EmployeeTipsTable.ORDER_ID),
            @Index(name = "payment", columns = EmployeeTipsTable.PAYMENT_TRANSACTION_ID),
            @Index(name = "parent", columns = EmployeeTipsTable.PARENT_GUID)
    })
    public static interface EmployeeTipsTable extends IBemaSyncTable {

        @URI(altNotify = {TipsView.URI_CONTENT})
        String URI_CONTENT = "employee_tips";

        String TABLE_NAME = "employee_tips";


        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @Column(type = Type.TEXT)
        String PARENT_GUID = "parent_guid";

        @Column(type = Type.TEXT)
        String EMPLOYEE_ID = "employee_id";

        @NotNull
        @Column(type = Type.TEXT)
        String SHIFT_ID = "shift_id";

        @Column(type = Type.TEXT)
        String ORDER_ID = "order_id";

        @Column(type = Type.TEXT)
        String PAYMENT_TRANSACTION_ID = "payment_transaction_id";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Type.TEXT)
        String AMOUNT = "amount";

        @Column(type = Type.TEXT)
        String COMMENT = "comment";

        @NotNull
        @Column(type = Type.INTEGER)
        String PAYMENT_TYPE = "payment_type";
    }

    static {
        applyForeignKeys(EmployeeTipsTable.TABLE_NAME,
                foreignKey(EmployeeTipsTable.PARENT_GUID, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID),
                foreignKey(EmployeeTipsTable.EMPLOYEE_ID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(EmployeeTipsTable.SHIFT_ID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(EmployeeTipsTable.ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID),
                foreignKey(EmployeeTipsTable.PAYMENT_TRANSACTION_ID, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID)
        );
    }

    @Table(ActivationCarrierTable.TABLE_NAME)
    public static interface ActivationCarrierTable {

        @URI
        String URI_CONTENT = "activation_carrier";

        String TABLE_NAME = "activation_carrier";

        @PrimaryKey
        @Column(type = Type.INTEGER)
        String ID = "ID";

        @NotNull
        @Column(type = Type.TEXT)
        String NAME = "NAME";

        @NotNull
        @Column(type = Type.TEXT)
        String URL = "URL";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String IS_ACTIVE = "IS_ACTIVE";
    }

    @Table(EmployeeCommissionsTable.TABLE_NAME)
    @Indexes({
            @Index(name = "shift", columns = EmployeeCommissionsTable.SHIFT_ID),
            @Index(name = "cashier", columns = EmployeeCommissionsTable.EMPLOYEE_ID),
            @Index(name = "order", columns = EmployeeCommissionsTable.ORDER_ID)
    })
    public static interface EmployeeCommissionsTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "employee_commissions";

        String TABLE_NAME = "employee_commissions";


        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String EMPLOYEE_ID = "employee_id";

        @NotNull
        @Column(type = Type.TEXT)
        String SHIFT_ID = "shift_id";

        @NotNull
        @Column(type = Type.TEXT)
        String ORDER_ID = "order_id";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Type.TEXT)
        String AMOUNT = "amount";

    }

    static {
        applyForeignKeys(EmployeeCommissionsTable.TABLE_NAME,
                foreignKey(EmployeeCommissionsTable.EMPLOYEE_ID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(EmployeeCommissionsTable.SHIFT_ID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(EmployeeCommissionsTable.ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID)
        );
    }

    /**
     * views *
     */

    @SimpleView(CustomerView.VIEW_NAME)
    public static interface CustomerView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "customer_view";

        String VIEW_NAME = "customer_view";

        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

        @Join(type = Join.Type.LEFT, joinTable = CustomerTable.TABLE_NAME, joinColumn = CustomerTable.GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.CUSTOMER_GUID)
        String TABLE_CUSTOMER = "customer_table";
    }

    @SimpleView(PrinterView.VIEW_NAME)
    public static interface PrinterView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "printer_view";

        String VIEW_NAME = "printer_view";

        @From(PrinterTable.TABLE_NAME)
        String TABLE_PRINTER = "printer_table";

        @Columns(PrinterAliasTable.ALIAS)
        @Join(type = Join.Type.LEFT, joinTable = PrinterAliasTable.TABLE_NAME, joinColumn = PrinterAliasTable.GUID, onTableAlias = TABLE_PRINTER, onColumn = PrinterTable.ALIAS_GUID)
        String TABLE_ALIAS = "alias_table";
    }

    @SimpleView(SaleOrderItemsView.VIEW_NAME)
    public static interface SaleOrderItemsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_items_view";

        String VIEW_NAME = "so_items_view";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.TAXABLE, SaleOrderTable.ORDER_TYPE, SaleOrderTable.TRANSACTION_FEE})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = BillPaymentDescriptionTable.TABLE_NAME, joinColumn = BillPaymentDescriptionTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_BILL_PAYMENT_DESCRIPTION = "bill_payment_description_table";

        @Join(type = Join.Type.LEFT, joinTable = SaleAddonTable.TABLE_NAME, joinColumn = SaleAddonTable.ITEM_GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.SALE_ITEM_GUID)
        String TABLE_SALE_ORDER_ITEM_ADDON = "sale_addon_table";

        @Columns(ModifierTable.TITLE)
        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.MODIFIER_GUID, onTableAlias = TABLE_SALE_ORDER_ITEM_ADDON, onColumn = SaleAddonTable.ADDON_GUID)
        String TABLE_MODIFIER = "modifier_table";
    }

    @SimpleView(SaleOrderItemsViewFast.VIEW_NAME)
    public static interface SaleOrderItemsViewFast {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_items_fast_view";

        String VIEW_NAME = "so_items_fast_view";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.TAXABLE, SaleOrderTable.UPDATE_IS_DRAFT})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        /*@Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";*/
    }

    @RawQuery(SaleOrderItemsViewFastSynced.VIEW_NAME)
    public static interface SaleOrderItemsViewFastSynced {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_items_fast_view_synced";

        String VIEW_NAME = "so_items_fast_view_synced";

        String SALE_ITEM_GUID = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.SALE_ITEM_GUID;
        String ORDER_GUID = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.ORDER_GUID;
        String ITEM_GUID = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.ITEM_GUID;
        String QUANTITY = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.QUANTITY;
        String PRICE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.PRICE;
        String PRICE_TYPE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.PRICE_TYPE;
        String DISCOUNTABLE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.DISCOUNTABLE;
        String DISCOUNT = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.DISCOUNT;
        String DISCOUNT_TYPE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.DISCOUNT_TYPE;
        String TAXABLE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.TAXABLE;
        String TAX = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.TAX;
        String SEQUENCE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.SEQUENCE;
        String PARENT_GUID = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.PARENT_GUID;
        String FINAL_GROSS_PRICE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.FINAL_GROSS_PRICE;
        String FINAL_TAX = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.FINAL_TAX;
        String FINAL_DISCOUNT = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.FINAL_DISCOUNT;
        String HAS_NOTES = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.HAS_NOTES;
        String IS_DELETED = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.IS_DELETED;

        String SALE_ORDER_DISCOUNT = SaleOrderItemsViewFast.TABLE_SALE_ORDER + "_" + SaleOrderTable.DISCOUNT;
        String SALE_ORDER_DISCOUNT_TYPE = SaleOrderItemsViewFast.TABLE_SALE_ORDER + "_" + SaleOrderTable.DISCOUNT_TYPE;
        String SALE_ORDER_TAXABLE = SaleOrderItemsViewFast.TABLE_SALE_ORDER + "_" + SaleOrderTable.TAXABLE;

        String innerSql = "select DISTINCT sale_item_table_" + SaleItemTable.ORDER_GUID
                + " from " + SaleOrderItemsViewFast.VIEW_NAME
                + " where"
                + " sale_order_table_" + SaleOrderTable.UPDATE_IS_DRAFT + " = 1 "
                + " or sale_item_table_" + SaleItemTable.UPDATE_IS_DRAFT + " = 1";

        @SqlQuery
        String SQL = "select"
                + " i." + ORDER_GUID + ","
                + " i." + SALE_ITEM_GUID + ","
                + " i." + ITEM_GUID + ","
                + " i." + QUANTITY + ","
                + " i." + PRICE + ","
                + " i." + DISCOUNTABLE + ","
                + " i." + DISCOUNT + ","
                + " i." + DISCOUNT_TYPE + ","
                + " i." + TAXABLE + ","
                + " i." + TAX + ","
                + " i." + FINAL_GROSS_PRICE + ","
                + " i." + FINAL_DISCOUNT + ","
                + " i." + FINAL_TAX + ","
                + " i." + SALE_ORDER_TAXABLE + ","
                + " i." + SALE_ORDER_DISCOUNT + ","
                + " i." + SALE_ORDER_DISCOUNT_TYPE
                + " from " + SaleOrderItemsViewFast.VIEW_NAME + " as i"
                + " inner join (" + innerSql + ") as t1 ON"
                + " i.sale_item_table_" + SaleItemTable.ORDER_GUID + " = t1.sale_item_table_" + SaleItemTable.ORDER_GUID
                + " order by " + " i." + ORDER_GUID;
    }

    @SimpleView(ReportsTopItemsView.VIEW_NAME)
    public static interface ReportsTopItemsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "reports_top_items_view";

        String VIEW_NAME = "reports_top_items_view";

        @Columns({SaleItemTable.QUANTITY, SaleItemTable.TMP_REFUND_QUANTITY})
        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.CREATE_TIME, SaleOrderTable.SHIFT_GUID, SaleOrderTable.REGISTER_ID, SaleOrderTable.STATUS})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.GUID, ItemTable.DESCRIPTION})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

    }

    @SimpleView(ItemExtView.VIEW_NAME)
    public static interface ItemExtView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "items_ext_view";

        String VIEW_NAME = "items_ext_view";

        String MODIFIERS_COUNT = "mc";

        String ADDONS_COUNT = "ac";

        String OPTIONAL_COUNT = "oc";

        @From(ItemTable.TABLE_NAME)
        String TABLE_ITEM = "item_table";

        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @IgnoreColumns
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

        @Columns({ModifierTable.TYPE, ModifierTable.MODIFIER_GUID, ModifierTable.EXTRA_COST})
        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.ITEM_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_MODIFIER = "modifier_table";

        @Columns(TaxGroupTable.TAX)
        @Join(type = Join.Type.LEFT, joinTable = TaxGroupTable.TABLE_NAME, joinColumn = TaxGroupTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.TAX_GROUP_GUID)
        String TABLE_TAX_GROUP = "tax_group_table";

    }

    @SimpleView(ShiftView.VIEW_NAME)
    public static interface ShiftView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "shift_view";

        String VIEW_NAME = "shift_view";


        @From(ShiftTable.TABLE_NAME)
        String TABLE_SHIFT = "shift_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME})
        @Join(joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_SHIFT, onColumn = ShiftTable.OPEN_MANAGER_ID)
        String TABLE_OPEN_MANAGER = "open_manager_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME})
        @Join(type = Join.Type.LEFT, joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_SHIFT, onColumn = ShiftTable.CLOSE_MANAGER_ID)
        String TABLE_CLOSE_MANAGER = "close_manager_table";

        @Columns({RegisterTable.TITLE})
        @Join(joinTable = RegisterTable.TABLE_NAME, joinColumn = RegisterTable.ID, onTableAlias = TABLE_SHIFT, onColumn = ShiftTable.REGISTER_ID)
        String TABLE_REGISTER = "register_table";
    }

    @RawQuery(TotalSalesQuery.QUERY_NAME)
    public static interface TotalSalesQuery {

        String QUERY_NAME = "total_sales_query";

        @URI
        String CONTENT_PATH = "total_sales_query";

        String TR = "transactions";
        String TIPS = "tips";

        @SqlQuery
        String QUERY = "select " + TR + "." + PaymentTransactionTable.AMOUNT + ", " + TR + "." + PaymentTransactionTable.GATEWAY + ", " + TIPS + "." + EmployeeTipsTable.AMOUNT
                + " from " + PaymentTransactionTable.TABLE_NAME + " as " + TR
                + " left join " + EmployeeTipsTable.TABLE_NAME + " as " + TIPS
                + " on " + TR + "." + PaymentTransactionTable.GUID + " = " + TIPS + "." + EmployeeTipsTable.PAYMENT_TRANSACTION_ID
                + " where " + TR + "." + PaymentTransactionTable.SHIFT_GUID + " = ? and " + TR + "." + PaymentTransactionTable.IS_DELETED + " = 0 and " + TIPS + "." + EmployeeTipsTable.PARENT_GUID + " IS NULL"
                + " and " + PaymentTransactionTable.STATUS + " != ?"
                + " union all "
                + " select " + CashDrawerMovementTable.AMOUNT + ", null, null from " + CashDrawerMovementTable.TABLE_NAME
                + " where " + CashDrawerMovementTable.SHIFT_GUID + " = ? and " + CashDrawerMovementTable.IS_DELETED + " = 0";
    }

    @RawQuery(SearchItemWithModifierView.QUERY_NAME)
    public static interface SearchItemWithModifierView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "search_item_with_modifier_view";

        String QUERY_NAME = "search_item_with_modifier_view";

        //String ID = ItemTable.ID;
        String GUID = ItemExtView.TABLE_ITEM + "_" + ItemTable.GUID;
        String CATEGORY_ID = ItemExtView.TABLE_ITEM + "_" + ItemTable.CATEGORY_ID;
        String DESCRIPTION = ItemExtView.TABLE_ITEM + "_" + ItemTable.DESCRIPTION;
        String CODE = ItemExtView.TABLE_ITEM + "_" + ItemTable.CODE;
        String EAN_CODE = ItemExtView.TABLE_ITEM + "_" + ItemTable.EAN_CODE;
        String PRODUCT_CODE = ItemExtView.TABLE_ITEM + "_" + ItemTable.PRODUCT_CODE;
        String PRICE_TYPE = ItemExtView.TABLE_ITEM + "_" + ItemTable.PRICE_TYPE;
        String SALE_PRICE = ItemExtView.TABLE_ITEM + "_" + ItemTable.SALE_PRICE;
        String QUANTITY = ItemExtView.TABLE_ITEM + "_" + ItemTable.TMP_AVAILABLE_QTY;
        String UNITS_LABEL = ItemExtView.TABLE_ITEM + "_" + ItemTable.UNITS_LABEL;
        String STOCK_TRACKING = ItemExtView.TABLE_ITEM + "_" + ItemTable.STOCK_TRACKING;
        String ACTIVE_STATUS = ItemExtView.TABLE_ITEM + "_" + ItemTable.ACTIVE_STATUS;
        String DISCOUNTABLE = ItemExtView.TABLE_ITEM + "_" + ItemTable.DISCOUNTABLE;
        String DISCOUNT = ItemExtView.TABLE_ITEM + "_" + ItemTable.DISCOUNT;
        String DISCOUNT_TYPE = ItemExtView.TABLE_ITEM + "_" + ItemTable.DISCOUNT_TYPE;
        String TAXABLE = ItemExtView.TABLE_ITEM + "_" + ItemTable.TAXABLE;
        String TAX_GROUP_GUID = ItemExtView.TABLE_ITEM + "_" + ItemTable.TAX_GROUP_GUID;
        String ORDER_NUM = ItemExtView.TABLE_ITEM + "_" + ItemTable.ORDER_NUM;
        String DEFAULT_MODIFIER_GUID = ItemExtView.TABLE_ITEM + "_" + ItemTable.DEFAULT_MODIFIER_GUID;
        String PRINTER_ALIAS_GUID = ItemExtView.TABLE_ITEM + "_" + ItemTable.PRINTER_ALIAS_GUID;
        String BUTTON_VIEW = ItemExtView.TABLE_ITEM + "_" + ItemTable.BUTTON_VIEW;
        String HASNOTES = ItemExtView.TABLE_ITEM + "_" + ItemTable.HAS_NOTES;
        String SERIALIZABLE = ItemExtView.TABLE_ITEM + "_" + ItemTable.SERIALIZABLE;
        String CODE_TYPE = ItemExtView.TABLE_ITEM + "_" + ItemTable.CODE_TYPE;
        String ELIGIBLE_FOR_COMMISSION = ItemExtView.TABLE_ITEM + "_" + ItemTable.ELIGIBLE_FOR_COMMISSION;
        String COMMISSION = ItemExtView.TABLE_ITEM + "_" + ItemTable.COMMISSION;

        String DEPARTMENT_ID = ItemExtView.TABLE_CATEGORY + "_" + CategoryTable.DEPARTMENT_GUID;
        String CATEGORY_TITLE = ItemExtView.TABLE_CATEGORY + "_" + CategoryTable.TITLE;
        String MODIFIER_TYPE = ItemExtView.TABLE_MODIFIER + "_" + ModifierTable.TYPE;
        //String MODIFIER_IS_DELETED = ItemExtView.TABLE_MODIFIER + "_" + ModifierTable.IS_DELETED;

        String MODIFIERS_COUNT = "sum(case when " + MODIFIER_TYPE + " = 0 then 1 else 0 end) as " + ItemExtView.MODIFIERS_COUNT;//+ " and " + MODIFIER_IS_DELETED + " = 0
        String ADDONS_COUNT = "sum(case when " + MODIFIER_TYPE + " = 1 then 1 else 0 end) as " + ItemExtView.ADDONS_COUNT;//" + " and " + MODIFIER_IS_DELETED + " = 0
        String OPTIONAL_COUNT = "sum(case when " + MODIFIER_TYPE + " = 2 then 1 else 0 end) as " + ItemExtView.OPTIONAL_COUNT;//" + " and " + MODIFIER_IS_DELETED + " = 0

        String TAX = ItemExtView.TABLE_TAX_GROUP + "_" + TaxGroupTable.TAX;

        @SqlQuery
        String QUERY = "select " +
                GUID + "," +
                DEPARTMENT_ID + "," +
                CATEGORY_ID + "," +
                DESCRIPTION + "," +
                CODE + "," +
                EAN_CODE + "," +
                PRODUCT_CODE + "," +
                PRICE_TYPE + "," +
                SALE_PRICE + "," +
                QUANTITY + "," +
                UNITS_LABEL + "," +
                STOCK_TRACKING + "," +
                ACTIVE_STATUS + "," +
                DISCOUNTABLE + "," +
                DISCOUNT + "," +
                DISCOUNT_TYPE + "," +
                TAXABLE + "," +
                TAX_GROUP_GUID + "," +
                MODIFIERS_COUNT + "," +
                ADDONS_COUNT + ", " +
                OPTIONAL_COUNT + "," +
                CATEGORY_TITLE + "," +
                TAX + "," +
                ORDER_NUM + "," +
                //MODIFIER_IS_DELETED + "," +
                DEFAULT_MODIFIER_GUID + "," +
                PRINTER_ALIAS_GUID + "," +
                BUTTON_VIEW + "," +
                HASNOTES + "," +
                SERIALIZABLE + "," +
                CODE_TYPE + "," +
                ELIGIBLE_FOR_COMMISSION + "," +
                COMMISSION +
                " from " + ItemExtView.VIEW_NAME +
                " where " + ACTIVE_STATUS + " = 1 and " + DESCRIPTION + " like ? " + //and " + MODIFIER_IS_DELETED + " = 0
                " group by " + GUID +
                " having " + ItemExtView.MODIFIERS_COUNT + " > 0 or " + ItemExtView.ADDONS_COUNT + " > 0 or " + ItemExtView.OPTIONAL_COUNT + " > 0 " +
                " order by " + CATEGORY_TITLE + ", " + ORDER_NUM;
    }

    @SimpleView(SaleItemExView.VIEW_NAME)
    public static interface SaleItemExView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_with_item_view";

        String VIEW_NAME = "so_with_item_view";

        @From(SaleItemTable.TABLE_NAME)
        @Columns({SaleItemTable.SALE_ITEM_GUID, SaleItemTable.ORDER_GUID, SaleItemTable.ITEM_GUID, SaleItemTable.QUANTITY, SaleItemTable.NOTES})
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({ItemTable.UPDATE_QTY_FLAG, ItemTable.STOCK_TRACKING, ItemTable.DESCRIPTION, ItemTable.PRINTER_ALIAS_GUID})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";
    }

    @SimpleView(SaleItemExDelView.VIEW_NAME)
    public static interface SaleItemExDelView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_with_deleted_item_view";

        String VIEW_NAME = "so_with_deleted_item_view";

        @From(SaleItemTable.TABLE_NAME)
        @Columns({SaleItemTable.SALE_ITEM_GUID, SaleItemTable.ORDER_GUID, SaleItemTable.ITEM_GUID, SaleItemTable.QUANTITY, SaleItemTable.KITCHEN_PRINTED_QTY, SaleItemTable.NOTES})
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.UPDATE_QTY_FLAG, ItemTable.STOCK_TRACKING, ItemTable.DESCRIPTION, ItemTable.PRINTER_ALIAS_GUID})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";
    }

    @SimpleView(SaleOrderView.VIEW_NAME)
    public static interface SaleOrderView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_view";

        String VIEW_NAME = "so_view";

        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME})
        @Join(type = Join.Type.LEFT, joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.OPERATOR_GUID)
        String TABLE_OPERATOR = "operator_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({CustomerTable.GUID, CustomerTable.FISRT_NAME, CustomerTable.LAST_NAME, CustomerTable.PHONE, CustomerTable.EMAIL})
        @Join(type = Join.Type.LEFT, joinTable = CustomerTable.TABLE_NAME, joinColumn = CustomerTable.GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.CUSTOMER_GUID)
        String TABLE_CUSTOMER = "customer_table";

        @Columns(RegisterTable.TITLE)
        @Join(joinTable = RegisterTable.TABLE_NAME, joinColumn = RegisterTable.ID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.REGISTER_ID)
        String TABLE_REGISTER = "register_table";

        @Columns({EmployeeTipsTable.AMOUNT})
        @Join(type = Join.Type.LEFT, joinTable = EmployeeTipsTable.TABLE_NAME, joinColumn = EmployeeTipsTable.ORDER_ID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_TIPS = "tips_table";

    }

    @RawQuery(SaleOrderTipsQuery.QUERY_NAME)
    public static interface SaleOrderTipsQuery {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sale_order_tips_query";

        String QUERY_NAME = "sale_order_tips_query";

        //dirty hack
        final static int PAYMENT_TRANSACTION_STATUS_SUCCESS = 0;
        final static int PAYMENT_TRANSACTION_STATUS_PREAUTHORIZED = 4;
        final static int SALE_ORDER_STATUS_COMPLETED = 1;
        final static String CASH_GATEWAY = "5";
        final static String CREDIT_GATEWAYS = "0, 4, 9";
        final static String DEBIT_GATEWAY = "3";
        final static String EBT_GATEWAYS = "1, 2";
        final static String OTHER_GATEWAYS = "6, 7, 8";


        String HAS_PREAUTH_TRANSACTIONS = "has_preauth_transactions";
        String HAS_OPENED_TRANSACTIONS = "has_opened_transactions";

        String CASH_TRANSACTION_CNT = "cash_transaction_cnt";
        String CREDIT_TRANSACTION_CNT = "credit_transaction_cnt";
        String DEBIT_TRANSACTION_CNT = "debit_transaction_cnt";
        String EBT_TRANSACTION_CNT = "ebt_transaction_cnt";
        String OTHER_TRANSACTION_CNT = "other_transaction_cnt";

        String ORDER_GUID = SaleOrderView.TABLE_SALE_ORDER + "_" + SaleOrderTable.GUID;
        String ORDER_STATUS = SaleOrderView.TABLE_SALE_ORDER + "_" + SaleOrderTable.STATUS;

        @SqlQuery
        String QUERY = "select * from ("
                + "select " + SaleOrderView.VIEW_NAME + ".*, "

                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.IS_PREAUTH + " = 1 then 1 else 0 end) as " + HAS_PREAUTH_TRANSACTIONS + ", "
                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.STATUS + " = " + PAYMENT_TRANSACTION_STATUS_PREAUTHORIZED + " then 1 else 0 end) > 0 as " + HAS_OPENED_TRANSACTIONS + ", "

                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.GATEWAY + " = " + CASH_GATEWAY + " then 1 else 0 end) as " + CASH_TRANSACTION_CNT + ", "
                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.GATEWAY + " in (" + CREDIT_GATEWAYS + ") then 1 else 0 end) as " + CREDIT_TRANSACTION_CNT + ", "
                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.GATEWAY + " = " + DEBIT_GATEWAY + " then 1 else 0 end) as " + DEBIT_TRANSACTION_CNT + ", "
                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.GATEWAY + " in (" + EBT_GATEWAYS + ") then 1 else 0 end) as " + EBT_TRANSACTION_CNT + ", "
                + "sum(case when " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.GATEWAY + " in (" + OTHER_GATEWAYS + ") then 1 else 0 end) as " + OTHER_TRANSACTION_CNT

                + " from " + SaleOrderView.VIEW_NAME

                + " left join " + PaymentTransactionTable.TABLE_NAME
                + " on " + ORDER_GUID + " = " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.ORDER_GUID
                + " and (" + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.STATUS + " = " + PAYMENT_TRANSACTION_STATUS_PREAUTHORIZED
                + " or " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.STATUS + " = " + PAYMENT_TRANSACTION_STATUS_SUCCESS + ")"
//                + " and " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.IS_PREAUTH + " = 1"
                + " and " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.IS_DELETED + " = 0"

                + " where " + ORDER_STATUS + " = " + SALE_ORDER_STATUS_COMPLETED

                + " group by " + ORDER_GUID
                + ") as t";
    }

    @SimpleView(PaymentTransactionView.VIEW_NAME)
    public static interface PaymentTransactionView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "payment_transaction_view";

        String VIEW_NAME = "payment_transaction_view";

        @From(PaymentTransactionTable.TABLE_NAME)
        String TABLE_PAYMENT_TRANSACTION = "payment_transaction_table";

        @Columns({SaleOrderTable.REGISTER_ID, SaleOrderTable.GUID, SaleOrderTable.PARENT_ID, SaleOrderTable.CREATE_TIME})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_PAYMENT_TRANSACTION, onColumn = PaymentTransactionTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @Join(type = Join.Type.LEFT, joinTable = EmployeeTipsTable.TABLE_NAME, joinColumn = EmployeeTipsTable.PAYMENT_TRANSACTION_ID, onTableAlias = TABLE_PAYMENT_TRANSACTION, onColumn = PaymentTransactionTable.GUID)
        String TABLE_EMPLOYEE_TIPS = "employee_tips_table";

//        @Join(type = Join.Type.LEFT, joinTable = CreditReceiptTable.TABLE_NAME, joinColumn = CreditReceiptTable.GUID, onTableAlias = TABLE_PAYMENT_TRANSACTION, onColumn = PaymentTransactionTable.GATEWAY_PAYMENT_ID)
//        String TABLE_CREDIT_RECEIPT = "credit_receipt_table";
    }

    @SimpleView(ItemMovementView.VIEW_NAME)
    public static interface ItemMovementView {
        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_im_view";

        String VIEW_NAME = "so_im_view";

        @From(ItemMovementTable.TABLE_NAME)
        String TABLE_MOVEMENT = "movement_table";

        @Columns({ItemTable.UPDATE_QTY_FLAG, ItemTable.UPDATE_IS_DRAFT})
        @RawJoin(joinTable = ItemTable.TABLE_NAME, onCondition = TABLE_MOVEMENT + "." + ItemMovementTable.ITEM_GUID + " = item_table." + ItemTable.GUID + " and item_table." + ItemTable.UPDATE_QTY_FLAG + " is NOT NULL and item_table." + ItemTable.UPDATE_QTY_FLAG + " = " + TABLE_MOVEMENT + "." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG)
        String TABLE_ITEM = "item_table";
    }

    @SimpleView(ExportItemView.VIEW_NAME)
    public static interface ExportItemView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "export_items_view";

        String VIEW_NAME = "export_items_view";

        //String MODIFIERS_COUNT = "mc";

        //String ADDONS_COUNT = "ac";

        //String OPTIONAL_COUNT = "oc";

        @From(ItemTable.TABLE_NAME)
        String TABLE_ITEM = "item_table";

        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @Columns({DepartmentTable.TITLE})
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

        @IgnoreColumns
        @Join(type = Join.Type.LEFT, joinTable = SaleItemTable.TABLE_NAME, joinColumn = SaleItemTable.ITEM_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns(SaleOrderTable.CREATE_TIME)
        @Join(type = Join.Type.LEFT, joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

    }

    @SimpleView(SaleReportItemsView.VIEW_NAME)
    public static interface SaleReportItemsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_sale_reports_items_view";

        String VIEW_NAME = "so_sale_reports_items_view";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.SHIFT_GUID, SaleOrderTable.STATUS, SaleOrderTable.TAXABLE, SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.CREATE_TIME, SaleOrderTable.REGISTER_ID, SaleOrderTable.ORDER_TYPE, SaleOrderTable.TRANSACTION_FEE})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.DESCRIPTION, ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.COST, ItemTable.CATEGORY_ID})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        /*@Join(type = Join.Type.LEFT, joinTable = SaleAddonTable.TABLE_NAME, joinColumn = SaleAddonTable.ITEM_GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.SALE_ITEM_GUID)
        String TABLE_SALE_ORDER_ITEM_ADDON = "sale_addon_table";*/

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(type = Join.Type.LEFT, joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({DepartmentTable.TITLE})
        @Join(type = Join.Type.LEFT, joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = BillPaymentDescriptionTable.TABLE_NAME, joinColumn = BillPaymentDescriptionTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_BILL_PAYMENT_DESCRIPTION = "bill_payment_description_table";
    }

    @SimpleView(XReportView.VIEW_NAME)
    public static interface XReportView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "xreport_view";

        String VIEW_NAME = "xreport_view";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.SHIFT_GUID, SaleOrderTable.STATUS, SaleOrderTable.TAXABLE, SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.CREATE_TIME, SaleOrderTable.REGISTER_ID, SaleOrderTable.ORDER_TYPE, SaleOrderTable.TRANSACTION_FEE})
        @Join(type = Join.Type.LEFT, joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.GUID, ItemTable.DESCRIPTION, ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.COST, ItemTable.CATEGORY_ID})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        /*@Join(type = Join.Type.LEFT, joinTable = SaleAddonTable.TABLE_NAME, joinColumn = SaleAddonTable.ITEM_GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.SALE_ITEM_GUID)
        String TABLE_SALE_ORDER_ITEM_ADDON = "sale_addon_table";*/

/*        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(type = Join.Type.LEFT, joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @Columns({DepartmentTable.TITLE})
        @Join(type = Join.Type.LEFT, joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";*/
    }

    @SimpleView(EmployeeTimesheetView.VIEW_NAME)
    public static interface EmployeeTimesheetView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "employee_timesheet_view";

        String VIEW_NAME = "employee_timesheet_view";

        @From(EmployeeTimesheetTable.TABLE_NAME)
        String TABLE_TIME = "time_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({EmployeeTable.GUID, EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME, EmployeeTable.HOURLY_RATE})
        @Join(joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_TIME, onColumn = EmployeeTimesheetTable.EMPLOYEE_GUID)
        String TABLE_EMPLOYEE = "employee_table";
    }

    @SimpleView(EmployeeComissionView.VIEW_NAME)
    public static interface EmployeeComissionView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "employee_comission_view";

        String VIEW_NAME = "employee_comission_view";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @From(EmployeeCommissionsTable.TABLE_NAME)
        String TABLE_NAME = "comission_table";
    }

    @SimpleView(ItemManualMovementView.VIEW_NAME)
    public static interface ItemManualMovementView {
        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "inventory_log_view";

        String VIEW_NAME = "inventory_log_view";

        @From(ItemMovementTable.TABLE_NAME)
        String TABLE_MOVEMENT = "item_movement_table";

        @Columns({ItemTable.DESCRIPTION})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_MOVEMENT, onColumn = ItemMovementTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";
    }

    @SimpleView(ExportTopItemsView.VIEW_NAME)
    public static interface ExportTopItemsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "export_top_items_view";

        String VIEW_NAME = "export_top_items_view";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.CREATE_TIME, SaleOrderTable.SHIFT_GUID, SaleOrderTable.REGISTER_ID, SaleOrderTable.STATUS, SaleOrderTable.ORDER_TYPE})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

    }

    @SimpleView(ExportSoldItemsView.VIEW_NAME)
    public static interface ExportSoldItemsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "export_sold_items";

        String VIEW_NAME = "export_sold_items";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.SHIFT_GUID, SaleOrderTable.STATUS, SaleOrderTable.TAXABLE, SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.CREATE_TIME, SaleOrderTable.REGISTER_ID, SaleOrderTable.CUSTOMER_GUID, SaleOrderTable.ORDER_TYPE})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.DESCRIPTION, ItemTable.CATEGORY_ID, ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.GUID, ItemTable.COST})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @Columns({DepartmentTable.TITLE})
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";
    }

    @SimpleView(KitchenPrintView.VIEW_NAME)
    public static interface KitchenPrintView {
        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "kitchen_print";

        String VIEW_NAME = "kitchen_print";

        @From(SaleItemTable.TABLE_NAME)
        @Columns({SaleItemTable.ORDER_GUID, SaleItemTable.ITEM_GUID, SaleItemTable.QUANTITY})
        String TABLE_SALE_ITEM = "sale_item_table";

        @Columns({ItemTable.DESCRIPTION, ItemTable.PRINTER_ALIAS_GUID})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        @Join(joinTable = PrinterAliasTable.TABLE_NAME, joinColumn = PrinterAliasTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.PRINTER_ALIAS_GUID)
        String TABLE_PRINTER_ALIAS = "printer_alias_table";

        @Join(joinTable = PrinterTable.TABLE_NAME, joinColumn = PrinterTable.ALIAS_GUID, onTableAlias = TABLE_PRINTER_ALIAS, onColumn = PrinterAliasTable.GUID)
        String TABLE_PRINTER = "printer_table";

        @Columns({SaleOrderTable.OPERATOR_GUID, SaleOrderTable.PRINT_SEQ_NUM, SaleOrderTable.REGISTER_ID})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_ORDER = "order_table";

        @Columns({EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME})
        @Join(joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_ORDER, onColumn = SaleOrderTable.OPERATOR_GUID)
        String TABLE_EMPLOYEE = "employee_table";
    }

    public static interface MaxUpdateTableTimeQuery {

        String URI_CONTENT = "raw_max_time_table_query";

        String QUERY = "select " + DEFAULT_UPDATE_TIME + ", %2$s from %1$s where " + DEFAULT_UPDATE_TIME + " is not null order by " + DEFAULT_UPDATE_TIME + " DESC, %2$s DESC limit 1";
    }

    public static interface MaxUpdateTableTimeParentRelationsQuery {

        String URI_CONTENT = "raw_max_time_table_query_parent";

        String QUERY = "select " + DEFAULT_UPDATE_TIME + ", %2$s from %1$s where " + DEFAULT_UPDATE_TIME + " is not null and %3$s is %4$s order by " + DEFAULT_UPDATE_TIME + " DESC, %2$s DESC limit 1";
    }

    /*public static interface MaxUpdateTableParentTimeQuery {

        String URI_CONTENT = "raw_max_time_table_parent_query";

        String QUERY = "select max(" + DEFAULT_UPDATE_TIME + ") from %s where " + DEFAULT_UPDATE_TIME + " is not null and %s is null";
    }

    public static interface MaxUpdateTableChildTimeQuery {

        String URI_CONTENT = "raw_max_time_table_child_query";

        String QUERY = "select max(" + DEFAULT_UPDATE_TIME + ") from %s where " + DEFAULT_UPDATE_TIME + " is not null and %s is not null";
    }*/

    @SimpleView(CreditReceiptView.VIEW_NAME)
    public static interface CreditReceiptView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "credit_receipt_view";

        String VIEW_NAME = "credit_receipt_view";

        @From(CreditReceiptTable.TABLE_NAME)
        String TABLE_CREDIT_RECEIPT = "credit_receipt_table";

        @Columns(RegisterTable.TITLE)
        @Join(joinTable = RegisterTable.TABLE_NAME, joinColumn = RegisterTable.ID, onTableAlias = TABLE_CREDIT_RECEIPT, onColumn = CreditReceiptTable.REGISTER_ID)
        String TABLE_REGISTER = "register_table";
    }

    @SimpleView(CreditReceiptExView.VIEW_NAME)
    public static interface CreditReceiptExView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "credit_receipt_ex_view";

        String VIEW_NAME = "credit_receipt_ex_view";

        @From(CreditReceiptTable.TABLE_NAME)
        String TABLE_CREDIT_RECEIPT = "credit_receipt_table";

        @Columns(RegisterTable.TITLE)
        @Join(joinTable = RegisterTable.TABLE_NAME, joinColumn = RegisterTable.ID, onTableAlias = TABLE_CREDIT_RECEIPT, onColumn = CreditReceiptTable.REGISTER_ID)
        String TABLE_REGISTER = "register_table";

        @Columns({EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME})
        @Join(joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_CREDIT_RECEIPT, onColumn = CreditReceiptTable.CASHIER_GUID)
        String TABLE_CASHIER = "cashier_table";

        String TABLE_PAYMENT_TML = "payment_table";

        @Columns({PaymentTransactionTable.CREATE_TIME, PaymentTransactionTable.AMOUNT})
        @RawJoin(
                type = Join.Type.LEFT,
                joinTable = PaymentTransactionTable.TABLE_NAME,
                onCondition = TABLE_PAYMENT_TML + "." + PaymentTransactionTable.GATEWAY_PAYMENT_ID + " = " + TABLE_CREDIT_RECEIPT + "." + CreditReceiptTable.GUID
                        + " and " + TABLE_PAYMENT_TML + "." + PaymentTransactionTable.TYPE + " = 0"
                        + " and " + TABLE_PAYMENT_TML + "." + PaymentTransactionTable.IS_DELETED + " = 0")
        String TABLE_PAYMENT = TABLE_PAYMENT_TML;
    }

    @SimpleView(SaleAddonView.VIEW_NAME)
    public static interface SaleAddonView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sale_addon_view";

        String VIEW_NAME = "sale_addon_view";

        @From(SaleAddonTable.TABLE_NAME)
        String TABLE_SALE_ADDON = "sale_addon_table";

        @Columns(ModifierTable.TITLE)
        @Join(joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.MODIFIER_GUID, onTableAlias = TABLE_SALE_ADDON, onColumn = SaleAddonTable.ADDON_GUID)
        String TABLE_MODIFIER = "modifier_table";
    }

    @SimpleView(InventoryStatusReportView.VIEW_NAME)
    public static interface InventoryStatusReportView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "inventory_status_report_view";

        String VIEW_NAME = "inventory_status_report_view";

        @From(ItemTable.TABLE_NAME)
        @Columns({ItemTable.GUID, ItemTable.DESCRIPTION, ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.COST, ItemTable.TMP_AVAILABLE_QTY, ItemTable.ACTIVE_STATUS})
        String TABLE_ITEM = "item_table";

        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @Columns(DepartmentTable.TITLE)
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

    }

    @SimpleView(PrepaidOrderView.VIEW_NAME)
    public static interface PrepaidOrderView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "prepaid_order_view";

        String VIEW_NAME = "prepaid_order_view";

        @Columns({SaleOrderTable.GUID, SaleOrderTable.ORDER_TYPE})
        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

        @Columns({SaleItemTable.SALE_ITEM_GUID})
        @Join(type = Join.Type.LEFT, joinTable = SaleItemTable.TABLE_NAME, joinColumn = SaleItemTable.ORDER_GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Join(type = Join.Type.LEFT, joinTable = BillPaymentDescriptionTable.TABLE_NAME, joinColumn = BillPaymentDescriptionTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_BILL_PAYMENT_DESCRIPTION = "bill_payment_description_table";
    }

    @SimpleView(TipsView.VIEW_NAME)
    public static interface TipsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "tips_view";

        String VIEW_NAME = "tips_view";

        @Columns({SaleOrderTable.GUID, SaleOrderTable.PARENT_ID})
        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

        @Join(joinTable = EmployeeTipsTable.TABLE_NAME, joinColumn = EmployeeTipsTable.ORDER_ID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_TIPS = "tips_table";

    }

    @SimpleView(TipsReportView.VIEW_NAME)
    public static interface TipsReportView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "tips_report_view";

        String VIEW_NAME = "tips_report_view";

        @From(EmployeeTipsTable.TABLE_NAME)
        String TABLE_TIPS = "tips_table";

        @Columns({ShiftTable.START_TIME, ShiftTable.END_TIME})
        @Join(joinTable = ShiftTable.TABLE_NAME, joinColumn = ShiftTable.GUID, onTableAlias = TABLE_TIPS, onColumn = EmployeeTipsTable.SHIFT_ID)
        String TABLE_SHIFTS = "shift_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME})
        @Join(type = Join.Type.LEFT, joinTable = EmployeeTable.TABLE_NAME, joinColumn = EmployeeTable.GUID, onTableAlias = TABLE_TIPS, onColumn = EmployeeTipsTable.EMPLOYEE_ID)
        String TABLE_EMPLOYEES = "employee_table";
    }

    @SimpleView(SaleItemCommissionsView.VIEW_NAME)
    public static interface SaleItemCommissionsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sale_items_commissions_view";

        String VIEW_NAME = "sale_items_commissions_view";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.GUID, ItemTable.ELIGIBLE_FOR_COMMISSION, ItemTable.COMMISSION})
        @From(ItemTable.TABLE_NAME)
        String TABLE_ITEM = "item_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({CategoryTable.ELIGIBLE_FOR_COMMISSION, CategoryTable.COMMISSION})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";
    }

    @SimpleView(CategoryView.VIEW_NAME)
    public static interface CategoryView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "category_view";

        String VIEW_NAME = "category_view";

        String ITEM_COUNT = "item_count";

        @From(CategoryTable.TABLE_NAME)
        String TABLE_CATEGORY = "category_table";

        @IgnoreColumns
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

        @Columns({ItemTable.GUID, ItemTable.STOCK_TRACKING, ItemTable.TMP_AVAILABLE_QTY, ItemTable.MINIMUM_QTY})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.CATEGORY_ID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.GUID)
        String TABLE_ITEM = "item_table";

    }

    @SimpleView(CategorySimpleView.VIEW_NAME)
    public static interface CategorySimpleView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "category_simple_view";

        String VIEW_NAME = "category_simple_view";

        @From(CategoryTable.TABLE_NAME)
        String TABLE_CATEGORY = "category_table";

        @IgnoreColumns
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

    }

    @RawQuery(RecalcItemMovementTableView.VIEW_NAME)
    public static interface RecalcItemMovementTableView {

        String VIEW_NAME = "recalc_sync_item_movement";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "recalc_sync_item_movement";

        String innerSql = "select distinct " + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " from " + ItemMovementTable.TABLE_NAME
                + " where " + ItemMovementTable.IS_DELETED + " = 0 and " + ItemMovementTable.UPDATE_IS_DRAFT + " = 1";

        @SqlQuery
        String SQL = "select m." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + ", m." + ItemMovementTable.QTY
                + " from " + ItemMovementTable.TABLE_NAME + " as m inner join (" + innerSql + ") as t1"
                + " ON m." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " = t1." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG
                + " where m." + ItemMovementTable.IS_DELETED + " = 0";

    }

    @RawQuery(RecalcItemMovementForItemTableView.VIEW_NAME)
    public static interface RecalcItemMovementForItemTableView {

        String VIEW_NAME = "recalc_sync_item_movement_4item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "recalc_sync_item_movement_4item";


        String innerSql = "select " + ItemTable.UPDATE_QTY_FLAG + " from " + ItemTable.TABLE_NAME
                + " where " + ItemTable.IS_DELETED + " = 0 and " + ItemTable.UPDATE_IS_DRAFT + " = 1";

        @SqlQuery
        String SQL = "select m." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + ", m." + ItemMovementTable.QTY
                + " from " + ItemMovementTable.TABLE_NAME + " as m inner join (" + innerSql + ") as t1"
                + " ON m." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " = t1." + ItemTable.UPDATE_QTY_FLAG
                + " where m." + ItemMovementTable.IS_DELETED + " = 0";

    }

    @RawQuery(RecalcSaleItemTableView.VIEW_NAME)
    public static interface RecalcSaleItemTableView {

        String VIEW_NAME = "recalc_sync_sale_item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "recalc_sync_sale_item";

        /*//select ALL related items for synced items
        @Columns({SaleItemTable.PARENT_GUID, SaleItemTable.QUANTITY})
        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ITEM = "sale_item_table";

        //select sale items with DRAFT = 1
        @IgnoreColumns
        @RawJoin(joinTable = SaleItemTable.TABLE_NAME,
                onCondition = TABLE_SALE_ITEM + "." + SaleItemTable.PARENT_GUID + " = sync_sale_item_table." + SaleItemTable.PARENT_GUID
                        + " and sync_sale_item_table." + SaleItemTable.UPDATE_IS_DRAFT + " = 1 and sync_sale_item_table." + SaleItemTable.PARENT_GUID + " is not NULL"
        )
        String TABLE_SYNC_ITEM = "sync_sale_item_table";*/

        String innerSql = "select distinct " + SaleItemTable.PARENT_GUID + " from " + SaleItemTable.TABLE_NAME + " where "
                + SaleItemTable.IS_DELETED + " = 0"
                + " and " + SaleItemTable.UPDATE_IS_DRAFT + " = 1 "
                + " and " + SaleItemTable.PARENT_GUID + " is not NULL";

        @SqlQuery
        String SQL = "select si." + SaleItemTable.PARENT_GUID + ", si." + SaleItemTable.QUANTITY
                + " from " + SaleItemTable.TABLE_NAME + " as si"
                + " inner join (" + innerSql + ") as t1 ON si." + SaleItemTable.PARENT_GUID + " = t1." + SaleItemTable.PARENT_GUID
                + " where si." + SaleItemTable.IS_DELETED + " = 0"
                + " order by " + "si." + SaleItemTable.PARENT_GUID;
    }



    /*@SimpleView(InventoryView.VIEW_NAME)
    public static interface InventoryView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "inventory_view";

        String VIEW_NAME = "inventory_view";

        @ExcludeStaticWhere(CategoryTable.IS_DELETED)
        @Columns(DepartmentTable.UPDATE_TIME)
        @From(DepartmentTable.TABLE_NAME)
        String TABLE_DEPARTMENT = "department_table";

        @ExcludeStaticWhere(CategoryTable.IS_DELETED)
        @Columns(CategoryTable.UPDATE_TIME)
        @Join(type = Join.Type.LEFT, joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.DEPARTMENT_GUID, onTableAlias = TABLE_DEPARTMENT, onColumn = DepartmentTable.GUID)
        String TABLE_CATEGORY = "category_table";

        @ExcludeStaticWhere(CategoryTable.IS_DELETED)
        @Columns(ItemTable.UPDATE_TIME)
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.CATEGORY_ID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.GUID)
        String TABLE_ITEM = "item_table";

        @ExcludeStaticWhere(CategoryTable.IS_DELETED)
        @Columns(ModifierTable.UPDATE_TIME)
        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.ITEM_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_MODIFIER = "modifier_table";

        @ExcludeStaticWhere(CategoryTable.IS_DELETED)
        @Columns(UnitTable.UPDATE_TIME)
        @Join(type = Join.Type.LEFT, joinTable = UnitTable.TABLE_NAME, joinColumn = UnitTable.ITEM_ID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_UNIT = "unit_table";
    }

    @RawQuery(InventoryMaxUpdateTableTimeQuery.QUERY_NAME)
    public static interface InventoryMaxUpdateTableTimeQuery {

        String QUERY_NAME = "raw_inventory_max_time";

        @URI
        String URI_CONTENT = "raw_inventory_max_time";

        String DepartmentTable_UPDATE_TIME = ShopStore.InventoryView.TABLE_DEPARTMENT + "_" + ShopStore.DepartmentTable.UPDATE_TIME;

        String CategoryTableUPDATE_TIME = ShopStore.InventoryView.TABLE_CATEGORY + "_" + ShopStore.CategoryTable.UPDATE_TIME;

        String ItemTableUPDATE_TIME = ShopStore.InventoryView.TABLE_ITEM + "_" + ShopStore.ItemTable.UPDATE_TIME;

        String ModifierTableUPDATE_TIME = ShopStore.InventoryView.TABLE_MODIFIER + "_" + ShopStore.ModifierTable.UPDATE_TIME;

        String UnitTableUPDATE_TIME = ShopStore.InventoryView.TABLE_UNIT + "_" + ShopStore.UnitTable.UPDATE_TIME;

        @SqlQuery
        String QUERY = "select max(" + DEFAULT_UPDATE_TIME + ") from "
                + "("
                + " select max(" + DepartmentTable_UPDATE_TIME + ","
                + " ifnull(" + CategoryTableUPDATE_TIME + "," + "0), "
                + " ifnull(" +  ItemTableUPDATE_TIME  + "," + "0), "
                + " ifnull(" +  ModifierTableUPDATE_TIME  + "," + "0), "
                + " ifnull(" +  UnitTableUPDATE_TIME  + "," + "0) ) as " + DEFAULT_UPDATE_TIME +" from " + InventoryView.VIEW_NAME
                + ") as t1 ";
    }

    @RawQuery(EmployeeMaxUpdateTableTimeQuery.QUERY_NAME)
    public static interface EmployeeMaxUpdateTableTimeQuery {

        String QUERY_NAME = "raw_employee_max_time";

        @URI
        String URI_CONTENT = "raw_employee_max_time";

        @SqlQuery
        String QUERY = "select max(" + DEFAULT_UPDATE_TIME + ") from "
                + " ("
                + " select max(" + EmployeeTable.TABLE_NAME + "." + DEFAULT_UPDATE_TIME + ", ifnull(" + EmployeePermissionTable.TABLE_NAME + "." + DEFAULT_UPDATE_TIME + ", 0) )"
                + " as " + DEFAULT_UPDATE_TIME
                + " from " + EmployeeTable.TABLE_NAME
                + " left join " + EmployeePermissionTable.TABLE_NAME
                + " on " + EmployeeTable.TABLE_NAME + "." + EmployeeTable.GUID + " = " + EmployeePermissionTable.TABLE_NAME + "." + EmployeePermissionTable.USER_GUID
                +        " and " + EmployeePermissionTable.TABLE_NAME + "." + DEFAULT_UPDATE_TIME + " is not null"
                + " where " + EmployeeTable.TABLE_NAME + "." + DEFAULT_UPDATE_TIME + " is not null "
                + " ) as t1";
    }*/

}