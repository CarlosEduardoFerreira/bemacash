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
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.store.ShopSchemaEx.Trigger.Action;
import com.kaching123.tcr.store.ShopSchemaEx.Trigger.Time;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.store.ShopSchemaEx.ForeignKey.foreignKey;
import static com.kaching123.tcr.store.ShopSchemaEx.Trigger.trigger;
import static com.kaching123.tcr.store.ShopSchemaEx.applyForeignKeys;
import static com.kaching123.tcr.store.ShopSchemaEx.applyTmpFields;
import static com.kaching123.tcr.store.ShopSchemaEx.applyTriggers;

@Schema(className = "ShopSchema", dbName = "shop.db", dbVersion = 309)
@Provider(name = "ShopProvider", authority = BuildConfig.PROVIDER_AUTHORITY, schemaClass = "ShopSchema", openHelperClass = "ShopOpenHelper")
public abstract class ShopStore {

    /*force static initialization of class*/
    public static void init() {
    }

    public static final String DEFAULT_UPDATE_TIME_LOCAL = "update_time_local";
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


    public interface IBemaSyncUpdateTimeLocalTable extends IBemaSyncTable {

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }


    @Table(CountryTable.TABLE_NAME)
    public interface CountryTable {

        @URI
        String URI_CONTENT = "country";

        String TABLE_NAME = "country";

        @PrimaryKey
        @NotNull
        @Column(type = Type.INTEGER)
        String SID = "sid";

        @Unique
        @NotNull
        @Column(type = Type.TEXT)
        String ID = "_id";

        @NotNull
        @Column(type = Type.TEXT)
        String NAME = "name";

        @Column(type = Type.INTEGER, defVal = "0")
        String UPDATE_IS_DRAFT = DEFAULT_IS_DRAFT;

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(StateTable.TABLE_NAME)
    @Index(name = "country", columns = StateTable.COUNTRY_ID)
    public interface StateTable {

        @URI
        String URI_CONTENT = "state";

        String TABLE_NAME = "state";

        @PrimaryKey
        @NotNull
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Column(type = Type.INTEGER)
        @NotNull
        String CODE = "code";

        @Column(type = Type.TEXT)
        @NotNull
        String NAME = "name";

        @Column(type = Type.TEXT)
        @NotNull
        String ABBREVIATION = "abbreviation";

        @Column(type = Type.INTEGER)
        String FISCAL_TECH = "FISCAL_TECH";

        @Column(type = Type.TEXT)
        String MAX_SALES_AMOUNT = "MAX_SALES_AMOUNT";

        @NotNull
        @Column(type = Type.TEXT)
        String COUNTRY_ID = "country_id";

        @Column(type = Type.INTEGER, defVal = "0")
        String UPDATE_IS_DRAFT = DEFAULT_IS_DRAFT;

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(StateTable.TABLE_NAME,
                foreignKey(StateTable.COUNTRY_ID, CountryTable.TABLE_NAME, CountryTable.ID)
        );
    }


    @Table(ApkUpdate.TABLE_NAME)
    public static interface ApkUpdate extends IBemaSyncTable {
        @URI
        String URI_CONTENT = "apk_update";
        String TABLE_NAME = "apk_update";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @Column(type = Type.TEXT)
        String URL = "url";

        @Column(type = Type.TEXT)
        String VERSION = "version";

        @Column(type = Type.INTEGER)
        String SCHEDULE_TIME = "scheduleTime";

        @Column(type = Type.TEXT)
        String PRIORITY = "priority";

        @Column(type = Type.INTEGER, defVal = "0")
        String APROVE = "aprove";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.TEXT)
        String FEEAMOUNT = "feeAmount";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(ComposerTable.TABLE_NAME)
    @Indexes({
            @Index(name = "host_item", columns = ComposerTable.ITEM_HOST_ID),
            @Index(name = "child_item", columns = ComposerTable.ITEM_CHILD_ID),
    })
    public interface ComposerTable extends IBemaSyncTable {

        @URI(altNotify = {ComposerView.URI_CONTENT})
        String URI_CONTENT = "composer";

        String TABLE_NAME = "composer";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String ID = "_id";

        @NotNull
        @Column(type = Type.TEXT)
        String ITEM_HOST_ID = "item_host_id";

        @NotNull
        @Column(type = Type.TEXT)
        String ITEM_CHILD_ID = "item_child_id";

        @NotNull
        @Column(type = Type.TEXT)
        String QUANTITY = "qty";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String STORE_TRACKING_ENABLED = "store_tracking_enabled";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String FREE_OF_CHARGE_COMPOSER = "free_of_charge_composer";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(ComposerTable.TABLE_NAME,
                foreignKey(ComposerTable.ITEM_HOST_ID, ItemTable.TABLE_NAME, ItemTable.GUID),
                foreignKey(ComposerTable.ITEM_CHILD_ID, ItemTable.TABLE_NAME, ItemTable.GUID)
        );
    }

    @Table(UnitTable.TABLE_NAME)
    @Indexes({
            @Index(name = "item", columns = UnitTable.ITEM_ID),
            @Index(name = "sale_order", columns = UnitTable.SALE_ORDER_ID),
            @Index(name = "sale_item", columns = UnitTable.SALE_ITEM_ID),
            @Index(name = "child_sale_item", columns = UnitTable.CHILD_ORDER_ID)
    })
    public static interface UnitTable extends IBemaSyncTable {

        @URI(altNotify = {UnitsView.URI_CONTENT})
        String URI_CONTENT = "unit";

        String TABLE_NAME = "unit";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String ID = "_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_ID = "item_id";

        @Column(type = Type.TEXT)
        String SALE_ITEM_ID = "sale_item_id";

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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(UnitTable.TABLE_NAME,
                foreignKey(UnitTable.ITEM_ID, ItemTable.TABLE_NAME, ItemTable.GUID),
                foreignKey(UnitTable.SALE_ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, true),
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

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
    public interface ItemTable extends IBemaSyncTable {

        @URI(altNotify = {ItemExtView.URI_CONTENT, CategoryView.URI_CONTENT, ModifierView.URI_CONTENT})
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

        @Column(type = Column.Type.TEXT)
        String PRICE_1 = "price_1";

        @Column(type = Column.Type.TEXT)
        String PRICE_2 = "price_2";

        @Column(type = Column.Type.TEXT)
        String PRICE_3 = "price_3";

        @Column(type = Column.Type.TEXT)
        String PRICE_4 = "price_4";

        @Column(type = Column.Type.TEXT)
        String PRICE_5 = "price_5";

        @Column(type = Type.TEXT)
        String UNIT_LABEL_ID = "unit_label_id";

        @Column(type = Column.Type.INTEGER)
        String STOCK_TRACKING = "stock_tracking";

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String LIMIT_QTY = "limit_qty";

        @Column(type = Column.Type.INTEGER)
        String ACTIVE_STATUS = "active_status";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNTABLE = "discountable";

        @Column(type = Type.INTEGER, defVal = "1")
        String SALABLE = "salable";

        @Column(type = Column.Type.TEXT)
        String DISCOUNT = "discount";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNT_TYPE = "discount_type";

        @Column(type = Column.Type.INTEGER)
        String TAXABLE = "taxable";

        @Column(type = Column.Type.INTEGER)
        String EBT_ELIGIBLE = "ebt_eligible";

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
        String TAX_GROUP_GUID2 = "tax_group_guid2";

        @Column(type = Type.TEXT)
        String TMP_AVAILABLE_QTY = "tmp_available_qty";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String ORDER_NUM = "order_num";

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

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String ITEM_REF_TYPE = "item_ref_type";

        @Column(type = Type.TEXT)
        String REFERENCE_ITEM_ID = "reference_item_id";

        @Column(type = Type.TEXT)
        String LOYALTY_POINTS = "loyalty_points";

        @Column(type = Type.INTEGER)
        String AGE_VERIFICATION = "age_verification";

        @Column(type = Type.INTEGER)
        String EXCLUDE_FROM_LOYALTY_PLAN = "exclude_from_loyalty_plan";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(ItemTable.TABLE_NAME,
                foreignKey(ItemTable.CATEGORY_ID, CategoryTable.TABLE_NAME, CategoryTable.GUID),
                foreignKey(ItemTable.TAX_GROUP_GUID, TaxGroupTable.TABLE_NAME, TaxGroupTable.GUID),
                foreignKey(ItemTable.PRINTER_ALIAS_GUID, PrinterAliasTable.TABLE_NAME, PrinterAliasTable.GUID)
        );

        applyTmpFields(ItemTable.TABLE_NAME, ItemTable.TMP_AVAILABLE_QTY);
    }

    @RawQuery(ItemTableAllColumns.QUERY_NAME)
    public static interface ItemTableAllColumns {

        String QUERY_NAME = "item_table_all_columns";

        @URI
        String CONTENT_PATH = "item_table_all_columns";

        @SqlQuery
        String QUERY = "select * from " + ItemTable.TABLE_NAME;
    }

    @Table(ItemMovementTable.TABLE_NAME)
    @PrimaryKey(columns = {ItemMovementTable.GUID, ItemMovementTable.ITEM_GUID})
    @Indexes({
            @Index(name = "item", columns = ItemMovementTable.ITEM_GUID),
            @Index(name = "mvn_flag", columns = ItemMovementTable.ITEM_UPDATE_QTY_FLAG),
            @Index(name = "create_time", columns = ItemMovementTable.CREATE_TIME)
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

        @Column(type = Type.TEXT)
        String OPERATOR_GUID = "operator_guid";

        @Column(type = Type.INTEGER)
        String MANUAL = "manual";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @Column(type = Type.TEXT)
        String TMP_AVAILABLE_QTY = "tmp_available_qty";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(ItemMovementTable.TABLE_NAME,
                foreignKey(ItemMovementTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));

        applyTmpFields(ItemMovementTable.TABLE_NAME, ItemMovementTable.TMP_AVAILABLE_QTY);
    }

    @Table(ModifierTable.TABLE_NAME)
    @Index(name = "item", columns = ModifierTable.ITEM_GUID)
    public interface ModifierTable extends IBemaSyncTable {

        @URI(altNotify = {ModifierView.URI_CONTENT, ModifierGroupView.URI_CONTENT})
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

        @Column(type = Type.TEXT)
        String ITEM_SUB_GUID = "item_sub_guid";

        @Column(type = Type.TEXT)
        String ITEM_SUB_QTY = "item_sub_qty";

        @Column(type = Type.TEXT)
        String ITEM_GROUP_GUID = "item_group_guid";

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String AUTO_APPLY ="auto_apply";

        @Column(type = Column.Type.INTEGER)
        String ORDER_NUM ="order_num";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(ModifierTable.TABLE_NAME,
                foreignKey(ModifierTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID),
                foreignKey(ModifierTable.ITEM_SUB_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));
    }



    @Table(ItemKDSTable.TABLE_NAME)
    public interface ItemKDSTable extends IBemaSyncTable {

        @URI  //(altNotify = {KDSView.URI_CONTENT})
        String URI_CONTENT = "items_kds";

        String TABLE_NAME = "items_kds";

        @PrimaryKey
        @Autoincrement
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String KDS_ALIAS_GUID = "kds_alias_guid";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String ITEM_GUID = "item_guid";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(ItemKDSTable.TABLE_NAME,
                foreignKey(ItemKDSTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID),
                foreignKey(ItemKDSTable.KDS_ALIAS_GUID, KDSAliasTable.TABLE_NAME, KDSAliasTable.GUID));
    }

    @Table(DefinedOnHoldTable.TABLE_NAME)
    public static interface DefinedOnHoldTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "defined_on_hold";

        String TABLE_NAME = "defined_on_hold";

        @PrimaryKey
        @NotNull
        @Column(type = Column.Type.TEXT)
        String ID = "id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String NAME = "name";

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
    public interface SaleOrderTable extends IBemaSyncUpdateTimeLocalTable {

        @URI(altNotify = {SaleOrderItemsView.URI_CONTENT, SaleOrderTipsQuery.URI_CONTENT, UnitsView.URI_CONTENT, SaleOrderView.URI_CONTENT})
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

        @Column(type = Type.INTEGER)
        String CHECKOUT_STATE = "checkout_state";

        @Column(type = Column.Type.TEXT)
        String DISCOUNT = "discount";

        @Column(type = Column.Type.INTEGER)
        String DISCOUNT_TYPE = "discount_type";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";

        @Column(type = Column.Type.TEXT)
        String DEFINED_ON_HOLD_ID = "defined_on_hold_id";

        @Column(type = Column.Type.TEXT)
        String HOLD_NAME = "hold_name";

        @Column(type = Column.Type.TEXT)
        String HOLD_TEL = "hold_tel";

        @Column(type = Type.INTEGER)
        String HOLD_STATUS = "hold_status";

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

        @Column(type = Type.INTEGER)
        String KDS_SEND_STATUS = "kds_print_status";

        @Column(type = Column.Type.TEXT)
        String TRANSACTION_FEE = "transaction_fee";

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String ON_REGISTER = "on_register";

        @Column(type = Column.Type.INTEGER, defVal = "0")
        String AGE_VERIFIED = "age_verified";

    }

    public static final String TRIGGER_NAME_UNLINK_OLD_REFUND_UNITS = "trigger_unlink_old_refund_units";
    public static final String TRIGGER_NAME_FIX_SALE_ORDER_UNITS = "trigger_fix_sale_order_units";

    static {
        applyForeignKeys(SaleOrderTable.TABLE_NAME,
                foreignKey(SaleOrderTable.OPERATOR_GUID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(SaleOrderTable.SHIFT_GUID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(SaleOrderTable.CUSTOMER_GUID, CustomerTable.TABLE_NAME, CustomerTable.GUID),
                foreignKey(SaleOrderTable.REGISTER_ID, RegisterTable.TABLE_NAME, RegisterTable.ID),
                foreignKey(SaleOrderTable.PARENT_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, true),
                foreignKey(SaleOrderTable.DEFINED_ON_HOLD_ID, DefinedOnHoldTable.TABLE_NAME, DefinedOnHoldTable.ID)
        );

        applyTmpFields(SaleOrderTable.TABLE_NAME,
                SaleOrderTable.TML_TOTAL_PRICE,
                SaleOrderTable.TML_TOTAL_DISCOUNT,
                SaleOrderTable.TML_TOTAL_TAX);

        applyTriggers(
                trigger(TRIGGER_NAME_UNLINK_OLD_REFUND_UNITS, Time.BEFORE, Action.DELETE, SaleOrderTable.TABLE_NAME,
                        " OLD." + SaleOrderTable.PARENT_ID + " IS NOT NULL ",
                        " UPDATE " + UnitTable.TABLE_NAME + " SET " + UnitTable.CHILD_ORDER_ID + " =  NULL " + " WHERE " + UnitTable.CHILD_ORDER_ID + " = OLD." + SaleOrderTable.GUID),
                trigger(TRIGGER_NAME_FIX_SALE_ORDER_UNITS, Time.BEFORE, Action.DELETE, SaleOrderTable.TABLE_NAME,
                        " OLD." + SaleOrderTable.PARENT_ID + " IS NULL " + " AND OLD." + SaleOrderTable.STATUS + " = " + _enum(OrderStatus.COMPLETED),
                        " UPDATE " + UnitTable.TABLE_NAME + " SET " + UnitTable.SALE_ORDER_ID + " =  NULL "
                                + " WHERE " + UnitTable.SALE_ORDER_ID + " = OLD." + SaleOrderTable.GUID
                                + " AND " + UnitTable.STATUS + " != " + _enum(Status.SOLD)));
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
        String EBT_ELIGIBLE = "ebt_eligible";

        @Column(type = Column.Type.TEXT)
        String TMP_EBT_PAYED = "tmp_ebt_payed";

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

        @Column(type = Type.TEXT)
        String TAX2 = "tax2";

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

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String IS_PREPAID_ITEM = "is_prepaid_item";

        @NotNull
        @Column(type = Type.INTEGER, defVal = "0")
        String IS_GIFT_CARD = "is_gift_card";

        @Column(type = Column.Type.TEXT)
        String LOYALTY_POINTS = "loyalty_points";

        @Column(type = Type.INTEGER)
        String POINTS_FOR_DOLLAR_AMOUNT = "points_for_dollar_amount";

        @Column(type = Column.Type.TEXT)
        String DISCOUNT_BUNDLE_ID = "discount_bundle_id";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(SaleItemTable.TABLE_NAME,
                foreignKey(SaleItemTable.ORDER_GUID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, true),
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

        @Column(type = Type.TEXT)
        String CHILD_ITEM_ID = "child_item_guid";

        @Column(type = Type.TEXT)
        String CHILD_ITEM_QTY = "child_item_qty";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(SaleAddonTable.TABLE_NAME,
                foreignKey(SaleAddonTable.ADDON_GUID, ModifierTable.TABLE_NAME, ModifierTable.MODIFIER_GUID),
                foreignKey(SaleAddonTable.ITEM_GUID, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID, true),
                foreignKey(SaleAddonTable.CHILD_ITEM_ID, ItemTable.TABLE_NAME, ItemTable.GUID)
        );
    }

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

        @Column(type = Column.Type.INTEGER, defVal = "1")
        String IS_SYNC = "is_sync";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(SqlCommandHostTable.TABLE_NAME)
    public interface SqlCommandHostTable {

        @URI
        String URI_CONTENT = "sql_command_host";

        String TABLE_NAME = "sql_command_host";
        int MAX_ROWS = 10000;

        @Autoincrement
        @PrimaryKey
        @Column(type = Type.INTEGER)
        String ORDER_COMMAND = "order_command";

        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String SQL_COMMAND = "sql_command";

        @NotNull
        @Column(type = Type.INTEGER)
        String CREATE_TIME = "create_time";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

        String DELETE_WHERE = GUID + " in (SELECT " + GUID + " FROM " + TABLE_NAME + " order by " +
                CREATE_TIME + " LIMIT ?)";
    }

    @Table(SqlCommandClientTable.TABLE_NAME)
    public interface SqlCommandClientTable {

        @URI
        String URI_CONTENT = "sql_command_client";

        String TABLE_NAME = "sql_command_client";

        @NotNull
        @Column(type = Type.INTEGER)
        String COMMAND_ID = "command_id";

        @NotNull
        @Column(type = Type.TEXT)
        String CLIENT_SERIAL = "client_serial";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

        String DELETE_WHERE = COMMAND_ID + " in (SELECT " + SqlCommandHostTable.ORDER_COMMAND + " FROM " +
                SqlCommandHostTable.TABLE_NAME + " order by " + SqlCommandHostTable.CREATE_TIME + " LIMIT ?)";
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


        @Column(type = Column.Type.TEXT)
        String LAST_FOUR = "last_four";

        @Column(type = Column.Type.TEXT)
        String ENTRY_METHOD = "entry_method";

        @Column(type = Column.Type.TEXT)
        String APPLICATION_IDENTIFIER = "application_identifier";

        @Column(type = Column.Type.TEXT)
        String APPLICATION_CRYPTOGRAM_TYPE = "application_cryptogram_type";

        @Column(type = Column.Type.TEXT)
        String AUTHORIZATION_NUMBER = "authorization_number";

        @Column(type = Column.Type.TEXT)
        String SIGNATURE_BYTES = "signature_bytes";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(PaymentTransactionTable.TABLE_NAME,
                foreignKey(PaymentTransactionTable.ORDER_GUID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, true),
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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
        @Column(type = Type.TEXT)
        String TITLE = "title";

        @NotNull
        @Column(type = Type.TEXT)
        String TAX = "tax";

        @Column(type = Type.INTEGER)
        String IS_DEFAULT = "is_default";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.TEXT)
        String DESCRIPTION = "description";

        @Column(type = Column.Type.TEXT)
        String TITLE = "title";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String STATUS = "status";

        @Column(type = Column.Type.INTEGER)
        String PREPAID_TID = "prepaid_tid";

        @Column(type = Column.Type.INTEGER)
        String BLACKSTONE_PAYMENT_CID = "blackstone_payment_cid";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(BillPaymentDescriptionTable.TABLE_NAME)
    @Index(name = "order", columns = BillPaymentDescriptionTable.ORDER_ID)
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
        String PREPAID_ORDER_ID = "order_id";

        @Column(type = Type.TEXT)
        String ORDER_ID = "sale_order_id";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

    }


    @Table(MunicipalityTable.TABLE_NAME)
    @Indexes({
            @Index(name = "country", columns = MunicipalityTable.COUNTRY_ID),
            @Index(name = "state", columns = MunicipalityTable.STATE_ID)
    })
    public interface MunicipalityTable extends IBemaSyncTable {

        @URI
        String URI_CONTENT = "municipality";

        String TABLE_NAME = "municipality";

        @PrimaryKey
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @NotNull
        @Column(type = Type.TEXT)
        String STATE_ID = "state_id";

        @NotNull
        @Column(type = Type.TEXT)
        String COUNTRY_ID = "country_id";

        @NotNull
        @Column(type = Type.INTEGER)
        String NAME = "name";

        @Column(type = Type.TEXT)
        String CODE = "code";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

    }

    static {
        applyForeignKeys(MunicipalityTable.TABLE_NAME,
                foreignKey(MunicipalityTable.COUNTRY_ID, CountryTable.TABLE_NAME, CountryTable.ID),
                foreignKey(MunicipalityTable.STATE_ID, StateTable.TABLE_NAME, StateTable.ID)
        );
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
        String CUSTOMER_IDENTIFICATION = "customer_identification";

        @Column(type = Column.Type.TEXT)
        String PHONE = "phone";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String SEX = "sex";

        @Column(type = Column.Type.INTEGER)
        String BIRTHDAY = "birthday";

        @Column(type = Column.Type.INTEGER)
        String BIRTHDAY_REWARD_APPLY_DATE = "birthday_reward_apply_date";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String CREATE_TIME = "create_time";

        @NotNull
        @Column(type = Column.Type.INTEGER, defVal = "0")
        String CONSENT_PROMOTIONS = "consent_promotions";

        @Column(type = Column.Type.TEXT)
        String NOTES = "notes";

        @Column(type = Column.Type.TEXT)
        String LOYALTY_PLAN_ID = "loyalty_plan_id";

        @Column(type = Column.Type.TEXT)
        String LOYALTY_BARCODE = "loyalty_barcode";

        @Column(type = Column.Type.TEXT)
        String TMP_LOYALTY_POINTS = "tmp_loyalty_points";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(CustomerTable.TABLE_NAME,
                foreignKey(CustomerTable.LOYALTY_PLAN_ID, LoyaltyPlanTable.TABLE_NAME, LoyaltyPlanTable.GUID)
        );
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(KDSAliasTable.TABLE_NAME)
    public static interface KDSAliasTable extends IBemaSyncTable {
        @URI
        String URI_CONTENT = "kds_alias_table";

        String TABLE_NAME = "kds_alias_table";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String ALIAS = "alias";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.TEXT)
        String SERIAL = "serial";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

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
        String PRINTER_TYPE = "printer_type";

        @Column(type = Type.TEXT)
        String MAC = "mac";

        @Column(type = Type.TEXT)
        String SUBNET = "subnet";

        @Column(type = Type.TEXT)
        String GATEWAY = "gateway";

        @Column(type = Type.TEXT)
        String DHCP = "dhcp";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(KDSTable.TABLE_NAME)
    public static interface KDSTable {

        @URI(altNotify = {KDSView.URI_CONTENT})
        String URI_CONTENT = "kds_table";

        String TABLE_NAME = "kds_table";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.INTEGER)
        String STATION_ID = "station_id";

        @Column(type = Type.TEXT)
        String ALIAS_GUID = "alias_guid";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(EmployeeTipsTable.TABLE_NAME,
                foreignKey(EmployeeTipsTable.PARENT_GUID, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID),
                foreignKey(EmployeeTipsTable.EMPLOYEE_ID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(EmployeeTipsTable.SHIFT_ID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(EmployeeTipsTable.ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, true),
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
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

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

    }

    static {
        applyForeignKeys(EmployeeCommissionsTable.TABLE_NAME,
                foreignKey(EmployeeCommissionsTable.EMPLOYEE_ID, EmployeeTable.TABLE_NAME, EmployeeTable.GUID),
                foreignKey(EmployeeCommissionsTable.SHIFT_ID, ShiftTable.TABLE_NAME, ShiftTable.GUID),
                foreignKey(EmployeeCommissionsTable.ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, true)
        );
    }

    @Table(UpdateTimeTable.TABLE_NAME)
    public static interface UpdateTimeTable {

        @URI
        String URI_CONTENT = "update_time";

        String TABLE_NAME = "update_time";

        @PrimaryKey
        @Column(type = Column.Type.INTEGER)
        String TABLE_ID = "table_id";

        @NotNull
        @Column(type = Column.Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Column.Type.INTEGER)
        String UPDATE_TIME = "update_time";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(VariantItemTable.TABLE_NAME)
    public interface VariantItemTable extends IBemaSyncTable {

        String TABLE_NAME = "variant_item";

        @URI(altNotify = {VariantsView.URI_CONTENT, VariantSubItemsCountView.URI_CONTENT, ItemExtView.URI_CONTENT})
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @NotNull
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Column(type = Type.TEXT)
        @NotNull
        @Unique
        String GUID = "guid";

        @Column(type = Type.TEXT)
        @NotNull
        String ITEM_GUID = "item_guid";

        @NotNull
        @Column(type = Type.INTEGER)
        String SHOP_ID = "shop_id";

        @Column(type = Type.TEXT)
        @NotNull
        String NAME = "name";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }


    @Table(VariantSubItemTable.TABLE_NAME)
    public interface VariantSubItemTable extends IBemaSyncTable {

        @URI(altNotify = {VariantsView.URI_CONTENT, VariantSubItemsCountView.URI_CONTENT, ItemExtView.URI_CONTENT})
        String URI_CONTENT = "variant_sub_item";

        String TABLE_NAME = "variant_sub_item";

        @PrimaryKey
        @NotNull
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Column(type = Type.TEXT)
        @NotNull
        @Unique
        String GUID = "guid";

        @Column(type = Type.TEXT)
        @NotNull
        String VARIANT_ITEM_GUID = "variant_item_guid";

        @Column(type = Type.TEXT)
        @NotNull
        String ITEM_GUID = "item_guid";

        @Column(type = Type.TEXT)
        @NotNull
        String NAME = "name";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

    }

    @Table(ItemMatrixTable.TABLE_NAME)
    public interface ItemMatrixTable extends IBemaSyncTable {

        String TABLE_NAME = "item_matrix";

        @URI(altNotify = {ItemMatrixByChildView.URI_CONTENT, ItemMatrixByParentView.URI_CONTENT, ItemExtView.URI_CONTENT})
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @NotNull
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Column(type = Type.TEXT)
        @NotNull
        @Unique
        String GUID = "guid";

        @Column(type = Type.TEXT)
        @NotNull
        String PARENT_GUID = "parent_guid";

        @Column(type = Type.TEXT)
        @NotNull
        String NAME = "name";

        @Column(type = Type.TEXT)
        String CHILD_GUID = "child_guid";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;

    }

    static {
        applyForeignKeys(ItemMatrixTable.TABLE_NAME,
                foreignKey(ItemMatrixTable.PARENT_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));
    }

    @Table(LoyaltyIncentiveTable.TABLE_NAME)
    public interface LoyaltyIncentiveTable extends IBemaSyncTable {

        String TABLE_NAME = "loyalty_incentive";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String NAME = "name";

        @NotNull
        @Column(type = Type.INTEGER)
        //points, birthday
        String TYPE = "type";

        @NotNull
        @Column(type = Type.INTEGER)
        //discount, gift_card, item
        String REWARD_TYPE = "reward_type";

        @Column(type = Type.INTEGER)
        String BIRTHDAY_OFFSET = "birthday_offset";

        @Column(type = Type.TEXT)
        String POINT_THRESHOLD = "point_threshold";

        @Column(type = Type.TEXT)
        String REWARD_VALUE = "reward_value";

        @Column(type = Type.INTEGER)
        //percent, value
        String REWARD_VALUE_TYPE = "reward_value_type";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Indexes({
            @Index(name = "incentive", columns = LoyaltyIncentiveItemTable.INCENTIVE_GUID),
            @Index(name = "item", columns = LoyaltyIncentiveItemTable.ITEM_GUID)
    })
    @Table(LoyaltyIncentiveItemTable.TABLE_NAME)
    public interface LoyaltyIncentiveItemTable extends IBemaSyncTable {

        String TABLE_NAME = "loyalty_incentive_item";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String INCENTIVE_GUID = "incentive_guid";

        @NotNull
        @Column(type = Type.TEXT)
        String ITEM_GUID = "item_guid";

        @Column(type = Type.TEXT)
        String PRICE = "price";

        @Column(type = Type.TEXT)
        String QTY = "qty";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(LoyaltyIncentiveItemTable.TABLE_NAME,
                foreignKey(LoyaltyIncentiveItemTable.INCENTIVE_GUID, LoyaltyIncentiveTable.TABLE_NAME, LoyaltyIncentiveTable.GUID),
                foreignKey(LoyaltyIncentiveItemTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));
    }

    @Table(LoyaltyPlanTable.TABLE_NAME)
    public interface LoyaltyPlanTable extends IBemaSyncTable {

        String TABLE_NAME = "loyalty_plan";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String NAME = "name";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Indexes({
            @Index(name = "incentive", columns = LoyaltyIncentivePlanTable.INCENTIVE_GUID),
            @Index(name = "plan", columns = LoyaltyIncentivePlanTable.PLAN_GUID)
    })
    @Table(LoyaltyIncentivePlanTable.TABLE_NAME)
    public interface LoyaltyIncentivePlanTable extends IBemaSyncTable {

        String TABLE_NAME = "loyalty_incentive_plan";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String INCENTIVE_GUID = "incentive_guid";

        @NotNull
        @Column(type = Type.TEXT)
        String PLAN_GUID = "plan_guid";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(LoyaltyIncentivePlanTable.TABLE_NAME,
                foreignKey(LoyaltyIncentivePlanTable.INCENTIVE_GUID, LoyaltyIncentiveTable.TABLE_NAME, LoyaltyIncentiveTable.GUID),
                foreignKey(LoyaltyIncentivePlanTable.PLAN_GUID, LoyaltyPlanTable.TABLE_NAME, LoyaltyPlanTable.GUID));
    }


    @Indexes({
            @Index(name = "customer", columns = LoyaltyPointsMovementTable.CUSTOMER_ID)
    })
    @Table(LoyaltyPointsMovementTable.TABLE_NAME)
    public interface LoyaltyPointsMovementTable extends IBemaSyncTable {

        String TABLE_NAME = "loyalty_points_movement";

        @URI(altNotify = {CustomerTable.URI_CONTENT, SaleOrderView.URI_CONTENT})
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String CUSTOMER_ID = "customer_id";

        @NotNull
        @Column(type = Type.TEXT)
        String LOYALTY_POINTS = "loyalty_points";

        @NotNull
        @Column(type = Type.INTEGER)
        String SHOP_ID = "shop_id";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }
    static {
        applyForeignKeys(LoyaltyPointsMovementTable.TABLE_NAME,
                foreignKey(LoyaltyPointsMovementTable.CUSTOMER_ID, CustomerTable.TABLE_NAME, CustomerTable.GUID));
    }

    @Table(SaleIncentiveTable.TABLE_NAME)
    public interface SaleIncentiveTable extends IBemaSyncTable {

        String TABLE_NAME = "sale_incentive";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String INCENTIVE_ID = "incentive_id";

        @NotNull
        @Column(type = Type.TEXT)
        String CUSTOMER_ID = "customer_id";

        @NotNull
        @Column(type = Type.TEXT)
        String ORDER_ID = "order_id";

        @NotNull
        @Column(type = Type.INTEGER)
        //points, birthday
        String TYPE = "type";

        @NotNull
        @Column(type = Type.INTEGER)
        //discount, gift_card, item
        String REWARD_TYPE = "reward_type";

        @Column(type = Type.TEXT)
        String REWARD_VALUE = "reward_value";

        @Column(type = Type.INTEGER)
        //percent, value
        String REWARD_VALUE_TYPE = "reward_value_type";

        @Column(type = Type.TEXT)
        String SALE_ITEM_ID = "sale_item_id";

        @Column(type = Type.TEXT)
        String POINT_THRESHOLD = "point_threshold";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }
    static {
        applyForeignKeys(SaleIncentiveTable.TABLE_NAME,
                foreignKey(SaleIncentiveTable.INCENTIVE_ID, LoyaltyIncentiveTable.TABLE_NAME, LoyaltyIncentiveTable.GUID),
                foreignKey(SaleIncentiveTable.CUSTOMER_ID, CustomerTable.TABLE_NAME, CustomerTable.GUID),
                foreignKey(SaleIncentiveTable.ORDER_ID, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID),
                foreignKey(SaleIncentiveTable.SALE_ITEM_ID, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID));
    }

    @Table(TBPTable.TABLE_NAME)
    public interface TBPTable  extends IBemaSyncTable{

        String TABLE_NAME = "tbp";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String ID = "id";

        @NotNull
        @Column(type = Type.TEXT)
        String DESCRIPTION = "description";

        @NotNull
        @Column(type = Type.INTEGER)
        String PRICE_LEVEL = "price_level";

        @NotNull
        @Column(type = Type.INTEGER)
        String IS_ACTIVE = "is_active";

        @Column(type = Type.TEXT)
        String MON_START = "mon_start";

        @Column(type = Type.TEXT)
        String MON_END = "mon_end";

        @Column(type = Type.TEXT)
        String TUE_START = "tue_start";

        @Column(type = Type.TEXT)
        String TUE_END = "tue_end";

        @Column(type = Type.TEXT)
        String WED_START = "wed_start";

        @Column(type = Type.TEXT)
        String WED_END = "wed_end";

        @Column(type = Type.TEXT)
        String THU_START = "thu_start";

        @Column(type = Type.TEXT)
        String THU_END = "thu_end";

        @Column(type = Type.TEXT)
        String FRI_START = "fri_start";

        @Column(type = Type.TEXT)
        String FRI_END = "fri_end";

        @Column(type = Type.TEXT)
        String SAT_START = "sat_start";

        @Column(type = Type.TEXT)
        String SAT_END = "sat_end";

        @Column(type = Type.TEXT)
        String SUN_START = "sun_start";

        @Column(type = Type.TEXT)
        String SUN_END = "sun_end";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @Table(TBPxRegisterTable.TABLE_NAME)
    public interface TBPxRegisterTable extends IBemaSyncTable{

        String TABLE_NAME = "tbp_x_register";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @NotNull
        @Column(type = Type.TEXT)
        String TBP_ID = "tbp_id";

        @NotNull
        @Column(type = Type.INTEGER)
        String REGISTER_ID = "register_id";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }
    static {
        applyForeignKeys(TBPxRegisterTable.TABLE_NAME,
                foreignKey(TBPxRegisterTable.TBP_ID, TBPTable.TABLE_NAME, TBPTable.ID),
                foreignKey(TBPxRegisterTable.REGISTER_ID, RegisterTable.TABLE_NAME, RegisterTable.ID));
    }

    @Indexes({
            @Index(name = "item", columns = MultipleDiscountTable.ITEM_ID),
            @Index(name = "bundle", columns = MultipleDiscountTable.BUNDLE_ID)
    })
    @Table(MultipleDiscountTable.TABLE_NAME)
    public interface MultipleDiscountTable extends IBemaSyncTable {

        String TABLE_NAME = "multiple_discount";

        @URI
        String URI_CONTENT = TABLE_NAME;

        @PrimaryKey
        @Column(type = Type.TEXT)
        String ID = "id";

        @NotNull
        @Column(type = Type.TEXT)
        String BUNDLE_ID = "bundle_id";

        @NotNull
        @Column(type = Type.TEXT)
        String ITEM_ID = "item_id";

        @NotNull
        @Column(type = Type.TEXT)
        String QTY = "qty";

        @NotNull
        @Column(type = Type.TEXT)
        String DISCOUNT = "discount";

        @Column(type = Type.INTEGER)
        String IS_ACTIVE = "is_active";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }
    static {
        applyForeignKeys(MultipleDiscountTable.TABLE_NAME,
                foreignKey(MultipleDiscountTable.ITEM_ID, ItemTable.TABLE_NAME, ItemTable.GUID));
    }

    /**
     * views *
     */

    @SimpleView(UnitsView.VIEW_NAME)
    public static interface UnitsView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "units_view";

        String VIEW_NAME = "units_view";

        @From(UnitTable.TABLE_NAME)
        String TABLE_UNIT = "unit_table";

        @Columns(SaleOrderTable.CREATE_TIME)
        @Join(type = Join.Type.LEFT, joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_UNIT, onColumn = UnitTable.SALE_ORDER_ID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    public static interface OldSaleOrdersQuery {

        String CONTENT_PATH = "old_sale_orders_query";

        String SALES = "sales";
        String TIPS = "tips";
        String REFUNDS = "refunds";

        String QUERY = "select * from ("
                + "select " + SALES + "." + SaleOrderTable.GUID
                + " from " + SaleOrderTable.TABLE_NAME + " as " + SALES
                + " left join " + EmployeeTipsTable.TABLE_NAME + " as " + TIPS
                + " on " + SALES + "." + SaleOrderTable.GUID + " = " + TIPS + "." + EmployeeTipsTable.ORDER_ID + " and " + TIPS + "." + EmployeeTipsTable.PARENT_GUID + " is null"
                + " left join " + SaleOrderTable.TABLE_NAME + " as " + REFUNDS
                + " on " + SALES + "." + SaleOrderTable.GUID + " = " + REFUNDS + "." + SaleOrderTable.PARENT_ID
                + " left join " + UnitTable.TABLE_NAME
                + " on " + SALES + "." + SaleOrderTable.STATUS + " = " + OrderStatus.ACTIVE.ordinal()
                + " and " + UnitTable.TABLE_NAME + "." + UnitTable.SALE_ORDER_ID + " = " + SALES + "." + SaleOrderTable.GUID
                + " where " + SALES + "." + SaleOrderTable.PARENT_ID + " is null and " + SALES + "." + SaleOrderTable.CREATE_TIME + " < %1$s "
                + " and (" + TIPS + "." + EmployeeTipsTable.CREATE_TIME + " IS NULL OR " + TIPS + "." + EmployeeTipsTable.CREATE_TIME + " < %2$s)"
                + " and " + UnitTable.TABLE_NAME + "." + UnitTable.ID + " is null"
                + " group by " + SALES + "." + SaleOrderTable.GUID
                + " having ( max(" + REFUNDS + "." + SaleOrderTable.CREATE_TIME + ") is null OR max(" + REFUNDS + "." + SaleOrderTable.CREATE_TIME + ") < %3$s)"
                + ") limit %4$s";

    }

    public static interface OldMovementGroupsQuery {

        String CONTENT_PATH = "old_movement_groups_query";

        String ITEM_GUID = "t_item_guid";
        String UPDATE_QTY_FLAG = "t_update_qty_flag";
        String CREATE_TIME = "t_create_time";

        String QUERY = "select * from ("
                + "select " + ItemMovementTable.TABLE_NAME + "." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG
                + " from " + ItemMovementTable.TABLE_NAME
                + " left join " + ItemTable.TABLE_NAME
                + " on " + ItemTable.TABLE_NAME + "." + ItemTable.GUID + " = " + ItemMovementTable.TABLE_NAME + "." + ItemMovementTable.ITEM_GUID
                + " and " + ItemTable.TABLE_NAME + "." + ItemTable.UPDATE_QTY_FLAG + " = " + ItemMovementTable.TABLE_NAME + "." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG
                + " where " + ItemTable.TABLE_NAME + "." + ItemTable.GUID + " is null "
                + " group by " + ItemMovementTable.TABLE_NAME + "." + ItemMovementTable.ITEM_UPDATE_QTY_FLAG
                + " having max(" + ItemMovementTable.TABLE_NAME + "." + ItemMovementTable.CREATE_TIME + ") < %1$s"
                + ") limit %2$s";

    }

    public static interface OldActiveUnitOrdersQuery {

        String CONTENT_PATH = "old_active_unit_orders_query";

        String QUERY = "SELECT " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.GUID
                + " FROM " + SaleOrderTable.TABLE_NAME
                + " join " + UnitTable.TABLE_NAME
                + " on " + UnitTable.TABLE_NAME + "." + UnitTable.SALE_ORDER_ID + " = " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.GUID
                + " join " + SaleItemTable.TABLE_NAME + " on " + SaleItemTable.TABLE_NAME + "." + SaleItemTable.ORDER_GUID + " = " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.GUID
                + " and " + SaleItemTable.TABLE_NAME + "." + SaleItemTable.ITEM_GUID + " = " + UnitTable.TABLE_NAME + "." + UnitTable.ITEM_ID
                + " where " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.STATUS + " = " + OrderStatus.ACTIVE.ordinal()
                + " and " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.UPDATE_IS_DRAFT + " = 0"
                + " and " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.UPDATE_TIME + " < %s"
                + " group by " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.GUID
                + " having max(" + SaleItemTable.TABLE_NAME + "." + SaleItemTable.UPDATE_TIME + ") not null";

    }

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

    @SimpleView(VariantsView.VIEW_NAME)
    public interface VariantsView {

        @URI
        String URI_CONTENT = "variant_view";

        String VIEW_NAME = "variant_view";

        String VARIANT_SUB_ITEMS_COUNT = "variant_sub_items_count";

        @From(VariantItemTable.TABLE_NAME)
        String TABLE_VARIANT_ITEM = "variant_item_table";

        @Join(type = Join.Type.LEFT, joinTable = VariantSubItemTable.TABLE_NAME, joinColumn = VariantSubItemTable.VARIANT_ITEM_GUID, onTableAlias = TABLE_VARIANT_ITEM, onColumn = VariantItemTable.GUID)
        String TABLE_VARIANT_SUB_ITEM = "variant_sub_item";
    }

    @SimpleView(ItemMatrixByChildView.VIEW_NAME)
    public interface ItemMatrixByChildView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "item_matrix_view";

        String VIEW_NAME = "item_matrix_view";


        @From(ItemMatrixTable.TABLE_NAME)
        String TABLE_ITEM_MATRIX = "item_matrix_table";

        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_ITEM_MATRIX, onColumn = ItemMatrixTable.CHILD_GUID)
        String TABLE_ITEM = "item_table";

        String ITEM_MATRIX_PARENT_GUID = TABLE_ITEM_MATRIX + "_" + ItemMatrixTable.PARENT_GUID;
        String ITEM_MATRIX_NAME = TABLE_ITEM_MATRIX + "_" + ItemMatrixTable.NAME;
        String ITEM_DESCRIPTION = TABLE_ITEM + "_" + ItemTable.DESCRIPTION;
        String CHILD_ITEM_GUID = TABLE_ITEM_MATRIX + "_" + ItemMatrixTable.CHILD_GUID;

    }

    @SimpleView(ItemMatrixByParentView.VIEW_NAME)
    public interface ItemMatrixByParentView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "item_matrix_view2";

        String VIEW_NAME = "item_matrix_view2";


        @From(ItemMatrixTable.TABLE_NAME)
        String TABLE_ITEM_MATRIX = "item_matrix_table";

        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_ITEM_MATRIX, onColumn = ItemMatrixTable.PARENT_GUID)
        String TABLE_ITEM = "item_table";

        String ITEM_DESCRIPTION = TABLE_ITEM + "_" + ItemTable.DESCRIPTION;
        String CHILD_ITEM_GUID = TABLE_ITEM_MATRIX + "_" + ItemMatrixTable.CHILD_GUID;

    }

    /*will consist direct parent info and info through a matrix*/
    @SimpleView(ItemWithParentInfoView.VIEW_NAME)
    public interface ItemWithParentInfoView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "item_with_parent_guid_view";

        String VIEW_NAME = "item_with_parent_guid_view";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.GUID, ItemTable.REFERENCE_ITEM_ID})
        @From(ItemTable.TABLE_NAME)
        String TABLE_ITEM = "item_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemMatrixTable.PARENT_GUID})
        @Join(type = Join.Type.LEFT, joinTable = ItemMatrixTable.TABLE_NAME, joinColumn = ItemMatrixTable.CHILD_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_ITEM_MATRIX = "item_matrix_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.DESCRIPTION})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.REFERENCE_ITEM_ID)
        String TABLE_DIRECT_PARENT_ITEM_DESCRIPTION = "direct_parent_item_description_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.DESCRIPTION})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_ITEM_MATRIX, onColumn = ItemMatrixTable.PARENT_GUID)
        String TABLE_PARENT_ITEM_THROUGH_MATRIX_DESCRIPTION = "parent_item_through_matrix_description_table";
    }

    @RawQuery(VariantSubItemsCountView.VIEW_NAME)
    public interface VariantSubItemsCountView {

        @URI
        String URI_CONTENT = "variants_sub_items_count";

        String VIEW_NAME = "variants_sub_items_count";
        String VARIANT_ITEM_PARENT_GUID = VariantsView.TABLE_VARIANT_ITEM + "_" + VariantItemTable.ITEM_GUID;
        String VARIANT_ITEM_SHOP_ID = VariantsView.TABLE_VARIANT_ITEM + "_" + VariantItemTable.SHOP_ID;
        String VARIANT_ITEM_GUID = VariantsView.TABLE_VARIANT_ITEM + "_" + VariantItemTable.GUID;
        String VARIANT_ITEM_NAME = VariantsView.TABLE_VARIANT_ITEM + "_" + VariantItemTable.NAME;
        String VARIANT_SUB_ITEM_GUID = VariantsView.TABLE_VARIANT_SUB_ITEM + "_" + VariantSubItemTable.GUID;


        String SUB_ITEMS_COUNT = "sum(case when " + VARIANT_SUB_ITEM_GUID + " IS NOT NULL then 1 else 0 end) as " + VariantsView.VARIANT_SUB_ITEMS_COUNT;
        @SqlQuery
        String QUERY = "select _id," + VARIANT_ITEM_GUID + "," +
                VARIANT_ITEM_NAME + "," + SUB_ITEMS_COUNT + "," + VARIANT_ITEM_PARENT_GUID + " from " + VariantsView.VIEW_NAME + " where " + VARIANT_ITEM_PARENT_GUID + "=?" + " AND " + VARIANT_ITEM_SHOP_ID + "=?" + " group by " + VARIANT_ITEM_GUID;

    }

    @RawQuery(RecalcComposersQuery.VIEW_NAME)
    public interface RecalcComposersQuery {

        String VIEW_NAME = "recalc_compose_item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "recalc_compose_item";

        String ID = "ID_ALIAS";
        String CHILD_ID = "CHILD_ID";
        String HOST_ID = "HOST_ID";

        String QTY_CHILD = "QTY_CHILD";
        String QTY_PARENT = "QTY_PARENT";

        String QTY_COMPOSER = "QTY_COMPOSER";

        String PRECIOUS_RESULT = "PRECIOUS_RESULT";
        String ACTUAL_RESULT = "ACTUAL_RESULT";

        String T1 = "T1";
        String T2 = "T2";
        String T3 = "T3";
        String T4 = "T4";

        String GROUP_BY = " GROUP BY ";
        String ORDER_BY = " ORDER BY ";

        String ON = " ON ";
        String AS = " AS ";
        String AND = " AND ";

        String ASC = " DESC ";
        String FROM = " FROM ";
        String SELECT = " SELECT ";
        String WHERE = " WHERE ";
        String JOIN = " JOIN ";

        String coma = ",";

        @SqlQuery
        String QUERY = SELECT + ID + coma + PRECIOUS_RESULT + FROM + "("
                + SELECT + ID + coma + "(" + QTY_CHILD + "/" + QTY_COMPOSER + ")" + AS + PRECIOUS_RESULT + coma + QTY_PARENT + AS + ACTUAL_RESULT
                + FROM + "("
                + SELECT + ComposerTable.ITEM_HOST_ID + AS + ID + coma + ComposerTable.ITEM_CHILD_ID + coma + ComposerTable.QUANTITY + AS + QTY_COMPOSER + coma + ComposerTable.FREE_OF_CHARGE_COMPOSER + coma + IBemaSyncTable.IS_DELETED
                + FROM + ComposerTable.TABLE_NAME + WHERE + ComposerTable.FREE_OF_CHARGE_COMPOSER + " = 1 " + AND + IBemaSyncTable.IS_DELETED + " = 0)" + T1
                + JOIN + "(" + SELECT + ItemTable.GUID + AS + CHILD_ID + coma + ItemTable.TMP_AVAILABLE_QTY + AS + QTY_CHILD + coma + IBemaSyncTable.IS_DELETED + FROM + ItemTable.TABLE_NAME + ") " + T3
                + ON + "(" + T3 + "." + IBemaSyncTable.IS_DELETED + " = 0 AND " + CHILD_ID + " = " + T1 + "." + ComposerTable.ITEM_CHILD_ID + ")"
                + JOIN + "(" + SELECT + ItemTable.GUID + AS + HOST_ID + coma + ItemTable.TMP_AVAILABLE_QTY + AS + QTY_PARENT + FROM + ItemTable.TABLE_NAME + ") " + T2
                + ON + "(" + ID + " = " + HOST_ID + ")"
                + ORDER_BY + PRECIOUS_RESULT + ASC + ") " + T4 + WHERE + "(" + ACTUAL_RESULT + " <> " + PRECIOUS_RESULT + ")" + GROUP_BY + ID;

        //// ****************************************            READABLE   V2        ****************************************** //////
//        SELECT GUID, PRECIOUS_RESULT, ACTUAL_RESULT FROM (
//                SELECT GUID, (QTY_CHILD / QTY ) AS PRECIOUS_RESULT, QTY_HOST AS ACTUAL_RESULT FROM (
//        SELECT ITEM_HOST_ID as GUID, ITEM_CHILD_ID, COMPOSER.QUANTITY as QTY, FREE_OF_CHARGE_COMPOSER FROM COMPOSER WHERE FREE_OF_CHARGE_COMPOSER = 1) T1
//        JOIN ( SELECT ID as CHILD_ID, SALE_PRICE as QTY_CHILD FROM ITEM) T3
//        ON (CHILD_ID = T1.ITEM_CHILD_ID)
//        JOIN ( SELECT ID as HOST_ID, SALE_PRICE as QTY_HOST FROM ITEM) T5
//        ON (HOST_ID = GUID)
//        ORDER BY PRECIOUS_RESULT ASC
//        ) T4 WHERE (ACTUAL_RESULT <> PRECIOUS_RESULT) GROUP BY GUID;
        //// ****************************************            READABLE           ****************************************** //////
    }

    @RawQuery(RecalcCostQuery.VIEW_NAME)
    public interface RecalcCostQuery {

        String VIEW_NAME = "recalc_cost_item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "recalc_cost_item";

        String ID = "ID_ALIAS";
        String CHILD_ID = "CHILD_ID";
        String HOST_ID = "HOST_ID";

        String COST_CHILD = "COST_CHILD";
        String COST_HOST = "COST_HOST";

        String QTY_COMPOSER = "QTY_COMPOSER";

        String PRECIOUS_RESULT = "PRECIOUS_RESULT";
        String ACTUAL_RESULT = "ACTUAL_RESULT";

        String T1 = "T1";
        String T2 = "T2";
        String T3 = "T3";

        String ORDER_BY = " ORDER BY ";

        String ON = " ON ";
        String AS = " AS ";
        String WHERE = " WHERE ";

        String ASC = " ASC ";
        String FROM = " FROM ";
        String SELECT = " SELECT ";
        String JOIN = " JOIN ";

        String coma = ",";

        @SqlQuery
        String QUERY = SELECT + ID + coma + "(" + COST_CHILD + "*" + QTY_COMPOSER + ")" + AS + PRECIOUS_RESULT + coma + COST_HOST + AS + ACTUAL_RESULT
                + FROM + "("
                + SELECT + ComposerTable.ITEM_HOST_ID + AS + ID + coma + ComposerTable.ITEM_CHILD_ID + coma + ComposerTable.QUANTITY + AS + QTY_COMPOSER + coma + IBemaSyncTable.IS_DELETED
                + FROM + ComposerTable.TABLE_NAME + WHERE + IBemaSyncTable.IS_DELETED + " = 0)" + T1
                + JOIN + "(" + SELECT + ItemTable.GUID + AS + CHILD_ID + coma + ItemTable.COST + AS + COST_CHILD + coma + IBemaSyncTable.IS_DELETED + FROM + ItemTable.TABLE_NAME + ") " + T3
                + ON + "(" + T3 + "." + IBemaSyncTable.IS_DELETED + " = 0 AND " + CHILD_ID + " = " + T1 + "." + ComposerTable.ITEM_CHILD_ID + ")"
                + JOIN + "(" + SELECT + ItemTable.GUID + AS + HOST_ID + coma + ItemTable.COST + AS + COST_HOST + FROM + ItemTable.TABLE_NAME + ") " + T2
                + ON + "(" + ID + " = " + HOST_ID + ")"
                + ORDER_BY + ID + ASC;

        //// ****************************************            READABLE          ****************************************** //////
//        SELECT GUID, (COST_CHILD * QTY ) AS PRECIOUS_RESULT, COST_HOST AS ACTUAL_RESULT FROM (
//                SELECT ITEM_HOST_ID as GUID, ITEM_CHILD_ID, COMPOSER.QUANTITY as QTY, FREE_OF_CHARGE_COMPOSER FROM COMPOSER WHERE FREE_OF_CHARGE_COMPOSER = 1) T1
//        JOIN ( SELECT ID as CHILD_ID, COST as COST_CHILD FROM ITEM) T3
//        ON (CHILD_ID = T1.ITEM_CHILD_ID)
//        JOIN ( SELECT ID as HOST_ID, COST as COST_HOST FROM ITEM) T5
//        ON (HOST_ID = GUID)
//        ORDER BY GUID ASC;
        //// ****************************************            READABLE           ****************************************** //////
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

    @SimpleView(KDSView.VIEW_NAME)
    public static interface KDSView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "kds_view";

        String VIEW_NAME = "kds_view";

        @From(KDSTable.TABLE_NAME)
        String TABLE_KDS = "kds_table";

        @Columns(KDSAliasTable.ALIAS)
        @Join(type = Join.Type.LEFT, joinTable = KDSAliasTable.TABLE_NAME, joinColumn = KDSAliasTable.GUID, onTableAlias = TABLE_KDS, onColumn = KDSTable.ALIAS_GUID)
        String TABLE_ALIAS = "alias_table";
    }

    @SimpleView(SaleOrderItemsView.VIEW_NAME)
    public interface SaleOrderItemsView {

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

        @Columns({TaxGroupTable.GUID, TaxGroupTable.TITLE, TaxGroupTable.TAX})
        @Join(type = Join.Type.LEFT, joinTable = TaxGroupTable.TABLE_NAME, joinColumn = TaxGroupTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.TAX_GROUP_GUID)
        String TABLE_TAX_GROUP = "tax_group_table";

        @Columns({TaxGroupTable.GUID, TaxGroupTable.TITLE, TaxGroupTable.TAX})
        @Join(type = Join.Type.LEFT, joinTable = TaxGroupTable.TABLE_NAME, joinColumn = TaxGroupTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.TAX_GROUP_GUID2)
        String TABLE_TAX_GROUP2 = "tax_group_table2";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = BillPaymentDescriptionTable.TABLE_NAME, joinColumn = BillPaymentDescriptionTable.ORDER_ID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.SALE_ITEM_GUID)
        String TABLE_BILL_PAYMENT_DESCRIPTION = "bill_payment_description_table";

        @Join(type = Join.Type.LEFT, joinTable = SaleAddonTable.TABLE_NAME, joinColumn = SaleAddonTable.ITEM_GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.SALE_ITEM_GUID)
        String TABLE_SALE_ORDER_ITEM_ADDON = "sale_addon_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.DESCRIPTION, ItemTable.SALE_PRICE})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM_ADDON, onColumn = SaleAddonTable.CHILD_ITEM_ID)
        String TABLE_SALE_ADDON_SUB_ITEM = "sale_addon_sub_item_table";

        @Columns({ModifierTable.TITLE, ModifierTable.ORDER_NUM})
        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.MODIFIER_GUID, onTableAlias = TABLE_SALE_ORDER_ITEM_ADDON, onColumn = SaleAddonTable.ADDON_GUID)
        String TABLE_MODIFIER = "modifier_table";

        @Columns({ModifierGroupTable.TITLE, ModifierGroupTable.ORDER_NUM})
        @Join(type = Join.Type.LEFT, joinTable = ModifierGroupTable.TABLE_NAME, joinColumn = ModifierGroupTable.GUID, onTableAlias = TABLE_MODIFIER, onColumn = ModifierTable.ITEM_GROUP_GUID)
        String TABLE_MODIFIER_GROUP = "modifier_group_table";

        //        String raw = "unit_label_id";
        @Columns(UnitLabelTable.SHORTCUT)
        @Join(type = Join.Type.LEFT, joinTable = UnitLabelTable.TABLE_NAME, joinColumn = UnitLabelTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.UNIT_LABEL_ID)
        String TABLE_UNIT_LABEL = "unit_label_table";
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
        String EBT_ELIGIBLE = SaleOrderItemsViewFast.TABLE_SALE_ORDER_ITEM + "_" + SaleItemTable.EBT_ELIGIBLE;

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
                + " i." + EBT_ELIGIBLE + ","
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

        String MODIFIERS_COUNT = "modifiers_count";
        String ADDONS_COUNT = "addons_count";
        String OPTIONAL_COUNT = "options_count";

        @From(ItemTable.TABLE_NAME)
        String TABLE_ITEM = "item_table";

        @Columns({CategoryTable.TITLE, CategoryTable.DEPARTMENT_GUID})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @IgnoreColumns
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";

        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.ITEM_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_MODIFIER = "modifier_table";

        @Columns({ItemTable.SALE_PRICE})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_MODIFIER, onColumn = ModifierTable.ITEM_SUB_GUID)
        String TABLE_MODIFIER_SUB_ITEM = "modifier_sub_item_table";


        @Columns(TaxGroupTable.TAX)
        @Join(type = Join.Type.LEFT, joinTable = TaxGroupTable.TABLE_NAME, joinColumn = TaxGroupTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.TAX_GROUP_GUID)
        String TABLE_TAX_GROUP = "tax_group_table";

        @Columns(TaxGroupTable.TAX)
        @Join(type = Join.Type.LEFT, joinTable = TaxGroupTable.TABLE_NAME, joinColumn = TaxGroupTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.TAX_GROUP_GUID2)
        String TABLE_TAX_GROUP2 = "tax_group_table2";

        @Columns({UnitLabelTable.SHORTCUT})
        @Join(type = Join.Type.LEFT,
                joinTable = UnitLabelTable.TABLE_NAME,
                joinColumn = UnitLabelTable.GUID,
                onTableAlias = TABLE_ITEM,
                onColumn = ItemTable.UNIT_LABEL_ID)
        String TABLE_UNIT_LABEL = "unit_label_table";

        @Columns({ItemMatrixTable.PARENT_GUID})
        @Join(type = Join.Type.LEFT,
                joinTable = ItemMatrixTable.TABLE_NAME,
                joinColumn = ItemMatrixTable.CHILD_GUID,
                onTableAlias = TABLE_ITEM,
                onColumn = ItemTable.GUID)
        String TABLE_ITEM_MATRIX = "item_matrix_table";

        @Columns({ComposerTable.ID, ComposerTable.FREE_OF_CHARGE_COMPOSER})
        @Join(type = Join.Type.LEFT, joinTable = ComposerTable.TABLE_NAME, joinColumn = ComposerTable.ITEM_HOST_ID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_CHILD_COMPOSER = "child_composer_table";

        @Columns(ComposerTable.ID)
        @Join(type = Join.Type.LEFT, joinTable = ComposerTable.TABLE_NAME, joinColumn = ComposerTable.ITEM_CHILD_ID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_HOST_COMPOSER = "host_composer_table";

        @Columns({UnitTable.ID, UnitTable.STATUS})
        @Join(type = Join.Type.LEFT, joinTable = UnitTable.TABLE_NAME, joinColumn = UnitTable.ITEM_ID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_UNIT = "unit_table";
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
    public interface SearchItemWithModifierView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "search_item_with_modifier_view";

        String QUERY_NAME = "search_item_with_modifier_view";

        String GUID = ItemExtView.TABLE_ITEM + "_" + ItemTable.GUID;
        String CATEGORY_ID = ItemExtView.TABLE_ITEM + "_" + ItemTable.CATEGORY_ID;
        String DESCRIPTION = ItemExtView.TABLE_ITEM + "_" + ItemTable.DESCRIPTION;
        String CODE = ItemExtView.TABLE_ITEM + "_" + ItemTable.CODE;
        String EAN_CODE = ItemExtView.TABLE_ITEM + "_" + ItemTable.EAN_CODE;
        String PRODUCT_CODE = ItemExtView.TABLE_ITEM + "_" + ItemTable.PRODUCT_CODE;
        String PRICE_TYPE = ItemExtView.TABLE_ITEM + "_" + ItemTable.PRICE_TYPE;
        String SALE_PRICE = ItemExtView.TABLE_ITEM + "_" + ItemTable.SALE_PRICE;
        String QUANTITY = ItemExtView.TABLE_ITEM + "_" + ItemTable.TMP_AVAILABLE_QTY;
        String UNITS_LABEL = ItemExtView.TABLE_UNIT_LABEL + "_" + UnitLabelTable.SHORTCUT;
        String STOCK_TRACKING = ItemExtView.TABLE_ITEM + "_" + ItemTable.STOCK_TRACKING;
        String ACTIVE_STATUS = ItemExtView.TABLE_ITEM + "_" + ItemTable.ACTIVE_STATUS;
        String DISCOUNTABLE = ItemExtView.TABLE_ITEM + "_" + ItemTable.DISCOUNTABLE;
        String SALABLE = ItemExtView.TABLE_ITEM + "_" + ItemTable.SALABLE;
        String DISCOUNT = ItemExtView.TABLE_ITEM + "_" + ItemTable.DISCOUNT;
        String DISCOUNT_TYPE = ItemExtView.TABLE_ITEM + "_" + ItemTable.DISCOUNT_TYPE;
        String TAXABLE = ItemExtView.TABLE_ITEM + "_" + ItemTable.TAXABLE;
        String TAX_GROUP_GUID = ItemExtView.TABLE_ITEM + "_" + ItemTable.TAX_GROUP_GUID;
        String ORDER_NUM = ItemExtView.TABLE_ITEM + "_" + ItemTable.ORDER_NUM;
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

        String MODIFIERS_COUNT = "sum(case when " + MODIFIER_TYPE + " = 0 then 1 else 0 end) as " + ItemExtView.MODIFIERS_COUNT;
        String ADDONS_COUNT = "sum(case when " + MODIFIER_TYPE + " = 1 then 1 else 0 end) as " + ItemExtView.ADDONS_COUNT;
        String OPTIONAL_COUNT = "sum(case when " + MODIFIER_TYPE + " = 2 then 1 else 0 end) as " + ItemExtView.OPTIONAL_COUNT;

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
                PRINTER_ALIAS_GUID + "," +
                BUTTON_VIEW + "," +
                HASNOTES + "," +
                SERIALIZABLE + "," +
                CODE_TYPE + "," +
                ELIGIBLE_FOR_COMMISSION + "," +
                COMMISSION +
                " from " + ItemExtView.VIEW_NAME +
                " where " + ACTIVE_STATUS + " = 1 and " + DESCRIPTION + " like ? " +
                " group by " + GUID +
                " having " + ItemExtView.MODIFIERS_COUNT + " > 0 or " + ItemExtView.ADDONS_COUNT + " > 0 or " + ItemExtView.OPTIONAL_COUNT + " > 0 " +
                " order by " + CATEGORY_TITLE + ", " + ORDER_NUM;
    }

    @RawQuery(ModifiersCountView.QUERY_NAME)
    public interface ModifiersCountView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "modifiers_count_view";

        String QUERY_NAME = "modifiers_count_view";

        String GROUP_BY = " GROUP BY ";
        String ORDER_BY = " ORDER BY ";
        String HAVING = " HAVING ";
        String ON = " ON ";
        String AS = " AS ";
        String AND = " AND ";
        String FROM = " FROM ";
        String SELECT = " SELECT ";
        String WHERE = " WHERE ";
        String JOIN = " JOIN ";
        String coma = ", ";

        String ITEM_GUID = "ITEM_GUID2";
        String ITEM_DESCRIPTION = "ITEM_DESCRIPTION2";
        String CATEGORY_GUID = "CATEGORY_GUID2";
        String CATEGORY_TITLE = "CATEGORY_TITLE2";
        String MODIFIERS_COUNT = " sum(case when " + "m." + ModifierTable.TYPE + " = 0 then 1 else 0 end) as " + ItemExtView.MODIFIERS_COUNT;
        String ADDONS_COUNT = " sum(case when " + "m." + ModifierTable.TYPE + " = 1 then 1 else 0 end) as " + ItemExtView.ADDONS_COUNT;
        String OPTIONALS_COUNT = " sum(case when " + "m." + ModifierTable.TYPE + " = 2 then 1 else 0 end) as " + ItemExtView.OPTIONAL_COUNT;

        @SqlQuery
        String QUERY =
                SELECT
                        + "i." + ItemTable.GUID + AS + ITEM_GUID + coma + "i." + ItemTable.DESCRIPTION + AS + ITEM_DESCRIPTION + coma
                        + "c." + CategoryTable.GUID + AS + CATEGORY_GUID + coma + "c." + CategoryTable.TITLE + AS + CATEGORY_TITLE + coma
                        + MODIFIERS_COUNT + coma + ADDONS_COUNT + coma + OPTIONALS_COUNT
                        + FROM + ItemTable.TABLE_NAME + " as i "
                        + JOIN + CategoryTable.TABLE_NAME + " as c "
                        + ON + "c." + CategoryTable.GUID + " = " + "i." + ItemTable.CATEGORY_ID
                        + JOIN + ModifierTable.TABLE_NAME + " as m "
                        + ON + ITEM_GUID + " = " + "m." + ModifierTable.ITEM_GUID + AND + "m." + ModifierTable.IS_DELETED + " = 0"
                        + WHERE + "i." + ItemTable.IS_DELETED + " = 0" + AND + "i." + ItemTable.ACTIVE_STATUS + " = 1 and " + ITEM_DESCRIPTION + " like ? and " + ITEM_GUID + " != ? "
                        + GROUP_BY + ITEM_GUID
                        + HAVING + ItemExtView.MODIFIERS_COUNT + " > 0 or " + ItemExtView.ADDONS_COUNT + " > 0 or " + ItemExtView.OPTIONAL_COUNT + " > 0 "
                        + ORDER_BY + CATEGORY_GUID;
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
        @Columns({ItemTable.GUID,ItemTable.UPDATE_QTY_FLAG, ItemTable.STOCK_TRACKING, ItemTable.DESCRIPTION, ItemTable.PRINTER_ALIAS_GUID})
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
        @Join(type = Join.Type.LEFT, joinTable = CustomerTable.TABLE_NAME, joinColumn = CustomerTable.GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.CUSTOMER_GUID)
        String TABLE_CUSTOMER = "customer_table";

        @Columns(RegisterTable.TITLE)
        @Join(joinTable = RegisterTable.TABLE_NAME, joinColumn = RegisterTable.ID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.REGISTER_ID)
        String TABLE_REGISTER = "register_table";

        @Columns({EmployeeTipsTable.AMOUNT, EmployeeTipsTable.CREATE_TIME})
        @Join(type = Join.Type.LEFT, joinTable = EmployeeTipsTable.TABLE_NAME, joinColumn = EmployeeTipsTable.ORDER_ID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_TIPS = "tips_table";

    }

    @SimpleView(SaleOrderWithDelView.VIEW_NAME)
    public static interface SaleOrderWithDelView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_with_del_view";

        String VIEW_NAME = "so_with_del_view";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

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

        String REFUNDS = "refunds";
        String MAX_REFUND_CREATE_TIME = "max_refund_create_time";

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

                + " from ("
                + " select " + SaleOrderView.VIEW_NAME + ".*, max(" + REFUNDS + "." + SaleOrderTable.CREATE_TIME + ") as " + MAX_REFUND_CREATE_TIME
                + " from " + SaleOrderView.VIEW_NAME
                + " left join " + SaleOrderTable.TABLE_NAME + " as " + REFUNDS
                + " on " + REFUNDS + "." + SaleOrderTable.PARENT_ID + " = " + ORDER_GUID
                + " group by " + ORDER_GUID
                + ") as " + SaleOrderView.VIEW_NAME

                + " left join " + PaymentTransactionTable.TABLE_NAME
                + " on " + ORDER_GUID + " = " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.ORDER_GUID
                + " and (" + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.STATUS + " = " + PAYMENT_TRANSACTION_STATUS_PREAUTHORIZED
                + " or " + PaymentTransactionTable.TABLE_NAME + "." + PaymentTransactionTable.STATUS + " = " + PAYMENT_TRANSACTION_STATUS_SUCCESS + ")"
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

        @Columns({UnitLabelTable.SHORTCUT})
        @Join(type = Join.Type.LEFT,
                joinTable = UnitLabelTable.TABLE_NAME,
                joinColumn = UnitLabelTable.GUID,
                onTableAlias = TABLE_ITEM,
                onColumn = ItemTable.UNIT_LABEL_ID)
        String TABLE_UNIT_LABEL = "unit_label_table";

        @Columns(ItemMatrixTable.PARENT_GUID)
        @Join(type = Join.Type.LEFT, joinTable = ItemMatrixTable.TABLE_NAME, joinColumn = ItemMatrixTable.CHILD_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_ITEM_MATRIX = "item_matrix_table";
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
        @Join(type = Join.Type.LEFT, joinTable = BillPaymentDescriptionTable.TABLE_NAME, joinColumn = BillPaymentDescriptionTable.ORDER_ID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.SALE_ITEM_GUID)
        String TABLE_BILL_PAYMENT_DESCRIPTION = "bill_payment_description_table";
    }

    @SimpleView(XReportView.VIEW_NAME)
    public static interface XReportView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "xreport_view";

        String VIEW_NAME = "xreport_view";

        @From(SaleItemTable.TABLE_NAME)
        String TABLE_SALE_ORDER_ITEM = "sale_item_table";

        @Columns({SaleOrderTable.SHIFT_GUID, SaleOrderTable.STATUS, SaleOrderTable.TAXABLE,
                SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.CREATE_TIME,
                SaleOrderTable.REGISTER_ID, SaleOrderTable.ORDER_TYPE, SaleOrderTable.TRANSACTION_FEE,
                SaleOrderTable.KITCHEN_PRINT_STATUS})
        @Join(type = Join.Type.LEFT, joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.GUID, ItemTable.DESCRIPTION, ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.COST, ItemTable.CATEGORY_ID,
                ItemTable.PRINTER_ALIAS_GUID})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

    }

    @SimpleView(ZReportView.VIEW_NAME)
    public static interface ZReportView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "zreport_view";

        String VIEW_NAME = "zreport_view";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({SaleOrderTable.SHIFT_GUID, SaleOrderTable.STATUS, SaleOrderTable.TAXABLE,
                SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.CREATE_TIME,
                SaleOrderTable.REGISTER_ID, SaleOrderTable.ORDER_TYPE, SaleOrderTable.TRANSACTION_FEE,
                SaleOrderTable.KITCHEN_PRINT_STATUS})
        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({SaleItemTable.ITEM_GUID, SaleItemTable.ORDER_GUID, SaleItemTable.KITCHEN_PRINTED_QTY, SaleItemTable.QUANTITY, SaleItemTable.IS_DELETED})
        @Join(type = Join.Type.LEFT, joinTable = SaleItemTable.TABLE_NAME, joinColumn = SaleItemTable.ORDER_GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_SALE_ORDER_ITEM = "sale_order_item_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.GUID, ItemTable.DESCRIPTION, ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.COST, ItemTable.CATEGORY_ID,
                ItemTable.PRINTER_ALIAS_GUID})
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

    }


    @SimpleView(SaleItemDeptView.VIEW_NAME)
    public static interface SaleItemDeptView {
        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sale_item_dept_view";

        String VIEW_NAME = "sale_item_dept_view";

        @Columns({SaleOrderTable.GUID, SaleOrderTable.SHIFT_GUID, SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE})
        @From(SaleOrderTable.TABLE_NAME)
        String TABLE_SALE_ORDER = "sale_order_table";

        @Columns({PaymentTransactionTable.CREATE_TIME, PaymentTransactionTable.STATUS})
        @Join(joinTable = PaymentTransactionTable.TABLE_NAME, joinColumn = PaymentTransactionTable.ORDER_GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_PAYMNET_TRANSACTION = "payment_transaction_table";

        @Columns({SaleItemTable.FINAL_DISCOUNT, SaleItemTable.FINAL_GROSS_PRICE, SaleItemTable.FINAL_TAX, SaleItemTable.ITEM_GUID, SaleItemTable.ORDER_GUID, SaleItemTable.PRICE, SaleItemTable.TAX, SaleItemTable.QUANTITY, SaleItemTable.DISCOUNT, SaleItemTable.DISCOUNT_TYPE, SaleItemTable.DISCOUNTABLE, SaleItemTable.TAXABLE})
        @Join(joinTable = SaleItemTable.TABLE_NAME, joinColumn = SaleItemTable.ORDER_GUID, onTableAlias = TABLE_SALE_ORDER, onColumn = SaleOrderTable.GUID)
        String TABLE_SALE_ITEM = "sale_item_table";

        @Columns({ModifierTable.EXTRA_COST})
        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.ITEM_GUID, onTableAlias = TABLE_SALE_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_MODIFIER = "modifier_table";

        @Columns({ItemTable.CATEGORY_ID, ItemTable.GUID})
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ITEM, onColumn = SaleItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        @Columns({CategoryTable.GUID, CategoryTable.TITLE})
        @Join(joinTable = CategoryTable.TABLE_NAME, joinColumn = CategoryTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.CATEGORY_ID)
        String TABLE_CATEGORY = "category_table";

        @Columns({DepartmentTable.GUID, DepartmentTable.TITLE})
        @Join(joinTable = DepartmentTable.TABLE_NAME, joinColumn = DepartmentTable.GUID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.DEPARTMENT_GUID)
        String TABLE_DEPARTMENT = "department_table";
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

        @Columns({SaleOrderTable.CREATE_TIME, SaleOrderTable.SHIFT_GUID, SaleOrderTable.REGISTER_ID,
                SaleOrderTable.STATUS, SaleOrderTable.ORDER_TYPE})
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

        @Columns({SaleOrderTable.SHIFT_GUID, SaleOrderTable.STATUS, SaleOrderTable.TAXABLE, SaleOrderTable.DISCOUNT, SaleOrderTable.DISCOUNT_TYPE, SaleOrderTable.CREATE_TIME,
                SaleOrderTable.REGISTER_ID, SaleOrderTable.CUSTOMER_GUID, SaleOrderTable.ORDER_TYPE})
        @Join(joinTable = SaleOrderTable.TABLE_NAME, joinColumn = SaleOrderTable.GUID, onTableAlias = TABLE_SALE_ORDER_ITEM, onColumn = SaleItemTable.ORDER_GUID)
        String TABLE_SALE_ORDER = "sale_order_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemTable.DESCRIPTION, ItemTable.CATEGORY_ID, ItemTable.EAN_CODE,
                ItemTable.PRODUCT_CODE, ItemTable.GUID, ItemTable.COST})
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

        @Columns({ModifierTable.TITLE, ModifierTable.ITEM_GROUP_GUID, ModifierTable.ORDER_NUM})
        @Join(joinTable = ModifierTable.TABLE_NAME, joinColumn = ModifierTable.MODIFIER_GUID,
                onTableAlias = TABLE_SALE_ADDON, onColumn = SaleAddonTable.ADDON_GUID)
        String TABLE_MODIFIER = "modifier_table";

        @Columns(ModifierGroupTable.ORDER_NUM)
        @Join(type = Join.Type.LEFT, joinTable = ModifierGroupTable.TABLE_NAME, joinColumn = ModifierGroupTable.GUID, onTableAlias = TABLE_MODIFIER, onColumn = ModifierTable.ITEM_GROUP_GUID)
        String TABLE_MODIFIER_GROUP = "modifier_group_table";

        @Columns(ItemTable.DESCRIPTION)
        @Join(type = Join.Type.LEFT, joinTable =  ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_SALE_ADDON, onColumn = SaleAddonTable.CHILD_ITEM_ID)
        String TABLE_ITEM = "item_table";
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

    @SimpleView(ComposerView.VIEW_NAME)
    public interface ComposerView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_composer_view";

        String VIEW_NAME = "so_composer_view";

        @From(ComposerTable.TABLE_NAME)
        String TABLE_COMPOSER_ITEM = "composer_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_COMPOSER_ITEM, onColumn = ComposerTable.ITEM_HOST_ID)
        String TABLE_HOST_ITEM = "item_host_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_COMPOSER_ITEM, onColumn = ComposerTable.ITEM_CHILD_ID)
        String TABLE_CHILD_ITEM = "item_child_table";

        @Columns(UnitLabelTable.SHORTCUT)
        @Join(type = Join.Type.LEFT, joinTable = UnitLabelTable.TABLE_NAME, joinColumn = UnitLabelTable.GUID, onTableAlias = TABLE_HOST_ITEM, onColumn = ItemTable.UNIT_LABEL_ID)
        String TABLE_HOST_UNIT_LABEL = "host_unit_label_table";

        @Columns(UnitLabelTable.SHORTCUT)
        @Join(type = Join.Type.LEFT, joinTable = UnitLabelTable.TABLE_NAME, joinColumn = UnitLabelTable.GUID, onTableAlias = TABLE_CHILD_ITEM, onColumn = ItemTable.UNIT_LABEL_ID)
        String TABLE_CHILD_UNIT_LABEL = "child_unit_label_table";
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

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.CATEGORY_ID, onTableAlias = TABLE_CATEGORY, onColumn = CategoryTable.GUID)
        String TABLE_ITEM = "item_table";

        @Columns(ComposerTable.ID)
        @Join(type = Join.Type.LEFT, joinTable = ComposerTable.TABLE_NAME, joinColumn = ComposerTable.ITEM_HOST_ID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_CHILD_COMPOSER = "child_composer_table";

        @Columns(ComposerTable.ID)
        @Join(type = Join.Type.LEFT, joinTable = ComposerTable.TABLE_NAME, joinColumn = ComposerTable.ITEM_CHILD_ID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_HOST_COMPOSER = "host_composer_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Columns({ItemMatrixTable.PARENT_GUID})
        @Join(type = Join.Type.LEFT, joinTable = ItemMatrixTable.TABLE_NAME, joinColumn = ItemMatrixTable.CHILD_GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.GUID)
        String TABLE_ITEM_MATRIX = "item_matrix_table";

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

    @RawQuery(ProductCodeView.VIEW_NAME)
    public interface ProductCodeView {

        String VIEW_NAME = "product_code_view";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "product_code_view";

        @SqlQuery
        String SQL = "select " + ItemTable.PRODUCT_CODE
                + " from " + ItemTable.TABLE_NAME
                + " where UPPER(" + ItemTable.PRODUCT_CODE + ") = LOWER(" + ItemTable.PRODUCT_CODE + ")"
                + " and LENGTH(" + ItemTable.PRODUCT_CODE + ") = " + 5
                + " order by " + ItemTable.PRODUCT_CODE;
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

    @Table(ModifierGroupTable.TABLE_NAME)
    public interface ModifierGroupTable extends IBemaSyncTable {

        @URI(altNotify = {ModifierGroupView.URI_CONTENT, ModifierView.URI_CONTENT})
        String URI_CONTENT = "items_modifier_group";

        String TABLE_NAME = "items_modifier_group";

        @PrimaryKey
        @Autoincrement
        @Column(type = Type.INTEGER)
        String ID = "_id";

        @Unique
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String TITLE = "title";

        @NotNull
        @Column(type = Type.TEXT)
        String ITEM_GUID = "item_guid";

        @Column(type = Type.INTEGER)
        String ORDER_NUM = "order_num";

        @Column(type = Type.INTEGER, defVal = "0")
        String CONDITION = "condition";

        @Column(type = Type.INTEGER, defVal = "0")
        String CONDITION_VALUE = "condition_value";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    static {
        applyForeignKeys(ModifierGroupTable.TABLE_NAME, foreignKey(ModifierGroupTable.ITEM_GUID, ItemTable.TABLE_NAME, ItemTable.GUID));
    }

    @SimpleView(ModifierView.VIEW_NAME)
    public interface ModifierView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "so_modifier_view";

        String VIEW_NAME = "so_modifier_view";

        @From(ModifierTable.TABLE_NAME)
        String TABLE_MODIFIER_ITEM = "modifier_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_MODIFIER_ITEM, onColumn = ModifierTable.ITEM_SUB_GUID)
        String TABLE_ITEM = "item_table";

        @ExcludeStaticWhere(IBemaSyncTable.IS_DELETED)
        @Join(type = Join.Type.LEFT, joinTable = ModifierGroupTable.TABLE_NAME, joinColumn = ModifierGroupTable.GUID, onTableAlias = TABLE_MODIFIER_ITEM, onColumn = ModifierTable.ITEM_GROUP_GUID)
        String TABLE_GROUP_ITEM = "item_group_table";

        @Columns(UnitLabelTable.SHORTCUT)
        @Join(type = Join.Type.LEFT, joinTable = UnitLabelTable.TABLE_NAME, joinColumn = UnitLabelTable.GUID, onTableAlias = TABLE_ITEM, onColumn = ItemTable.UNIT_LABEL_ID)
        String TABLE_UNIT_LABEL = "unit_label_table";
    }

    @Table(UnitLabelTable.TABLE_NAME)
    public interface UnitLabelTable extends IBemaSyncTable {
        @URI
        String URI_CONTENT = "unit_label_table";

        String TABLE_NAME = "unit_label_table";

        @PrimaryKey
        @NotNull
        @Column(type = Type.TEXT)
        String GUID = "guid";

        @NotNull
        @Column(type = Type.TEXT)
        String DESCRIPTION = "description";

        @NotNull
        @Column(type = Type.TEXT)
        String SHORTCUT = "shortcut";

        @Column(type = Type.INTEGER)
        String UPDATE_TIME_LOCAL = DEFAULT_UPDATE_TIME_LOCAL;
    }

    @SimpleView(ModifierGroupView.VIEW_NAME)
    public interface ModifierGroupView {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "modifier_group_view";

        String VIEW_NAME = "modifier_group_view";

        String ITEM_COUNT = "item_count";

        @From(ModifierGroupTable.TABLE_NAME)
        String TABGROUP = "group_table";

        @Join(type = Join.Type.LEFT, joinTable = ModifierTable.TABLE_NAME,
                joinColumn = ModifierTable.ITEM_GROUP_GUID, onTableAlias = TABGROUP,
                onColumn = ModifierGroupTable.GUID)
        String TABLE_ITEM = "item_table";
    }

    @SimpleView(LoyaltyView.VIEW_NAME)
    public interface LoyaltyView {

        String VIEW_NAME = "loyalty_incentive_view";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = VIEW_NAME;

        @From(LoyaltyIncentivePlanTable.TABLE_NAME)
        String TABLE_INCENTIVE_PLAN = "loyalty_incentive_plan_table";

        @Join(joinTable = LoyaltyPlanTable.TABLE_NAME, joinColumn = LoyaltyPlanTable.GUID, onTableAlias = TABLE_INCENTIVE_PLAN, onColumn = LoyaltyIncentivePlanTable.PLAN_GUID)
        String TABLE_PLAN = "loyalty_plan_table";

        @Join(joinTable = LoyaltyIncentiveTable.TABLE_NAME, joinColumn = LoyaltyIncentiveTable.GUID, onTableAlias = TABLE_INCENTIVE_PLAN, onColumn = LoyaltyIncentivePlanTable.INCENTIVE_GUID)
        String TABLE_INCENTIVE = "loyalty_incentive_table";

        @Join(type = Join.Type.LEFT, joinTable = LoyaltyIncentiveItemTable.TABLE_NAME, joinColumn = LoyaltyIncentiveItemTable.INCENTIVE_GUID, onTableAlias = TABLE_INCENTIVE, onColumn = LoyaltyIncentiveTable.GUID)
        String TABLE_INCENTIVE_ITEM = "loyalty_incentive_item_table";

        @Join(type = Join.Type.LEFT, joinTable = ItemTable.TABLE_NAME, joinColumn = ItemTable.GUID, onTableAlias = TABLE_INCENTIVE_ITEM, onColumn = LoyaltyIncentiveItemTable.ITEM_GUID)
        String TABLE_ITEM = "item_table";

        /*@Join(joinTable =  CustomerTable.TABLE_NAME, joinColumn = CustomerTable.LOYALTY_PLAN_ID, onTableAlias = TABLE_PLAN, onColumn = LoyaltyPlanTable.GUID)
        String TABLE_CUSTOMER = "customer_table";*/
    }

    @SimpleView(TBPRegisterView.VIEW_NAME)
    public interface TBPRegisterView {
        String VIEW_NAME = "register_tbp_view";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = VIEW_NAME;

        @From(TBPxRegisterTable.TABLE_NAME)
        String TABLE_TBP_X_REGISTER = "tbp_x_register_table";

        @Join(joinTable = TBPTable.TABLE_NAME, joinColumn = TBPTable.ID, onTableAlias = TABLE_TBP_X_REGISTER, onColumn = TBPxRegisterTable.TBP_ID)
        String TABLE_TBP = "tbp_table";
    }

    @RawQuery(RecalcQtyQuery.VIEW_NAME)
    public interface RecalcQtyQuery {

        String VIEW_NAME = "recalc_qty_item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "recalc_qty_item";

        String T1 = "T1";

        String GROUP_BY = " GROUP BY ";

        String ON = " ON ";

        String AND = " AND ";
        String FROM = " FROM ";
        String SELECT = " SELECT ";
        String JOIN = " JOIN ";

        String coma = ",";

        @SqlQuery
        String QUERY = SELECT + ItemMovementTable.ITEM_GUID + coma + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + coma + "SUM ( " + ItemMovementTable.QTY + " ) " +
                FROM + ItemMovementTable.TABLE_NAME
                + JOIN + "(" + SELECT + ItemTable.UPDATE_QTY_FLAG + FROM + ItemTable.TABLE_NAME + ") " + T1
                + ON + ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " = " + T1 + "." + ItemTable.UPDATE_QTY_FLAG + AND + ItemMovementTable.ITEM_GUID + " = ? "
                + GROUP_BY + ItemMovementTable.ITEM_UPDATE_QTY_FLAG;

    }

    @RawQuery(SqlCommandHostQuery.VIEW_NAME)
    public interface SqlCommandHostQuery {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sql_command_host_query";

        String VIEW_NAME = "sql_command_host_query";

        @SqlQuery
        String SQL = "SELECT " + SqlCommandHostTable.ORDER_COMMAND + "," + SqlCommandHostTable.SQL_COMMAND +
                " FROM " + SqlCommandHostTable.TABLE_NAME +
                " LEFT JOIN " + SqlCommandClientTable.TABLE_NAME +
                " ON " + SqlCommandHostTable.TABLE_NAME + "." + SqlCommandHostTable.ORDER_COMMAND + " = " +
                SqlCommandClientTable.TABLE_NAME + "." + SqlCommandClientTable.COMMAND_ID +
                " AND " + SqlCommandClientTable.TABLE_NAME + "." + SqlCommandClientTable.CLIENT_SERIAL + " = ?" +
                " WHERE " + SqlCommandClientTable.TABLE_NAME + "." + SqlCommandClientTable.COMMAND_ID + " is null" +
                " ORDER BY " + SqlCommandHostTable.ORDER_COMMAND + " LIMIT ?";
    }

    @RawQuery(SaleOrderItemsMappingQuery.VIEW_NAME)
    public interface SaleOrderItemsMappingQuery {

        String VIEW_NAME = "mapping_order_item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "mapping_order_item";

        String ITEM_GUID = "ITEM_GUID_TAG";
        String QUANTITY = "QUANTITY_TAG";
        String ITEM_QUANTITY = "ITEM_QUANTITY";
        String SOURCE = "SOURCE";
        String MOD_QTY = "MOD_QTY";
        String MOD_ID = "MOD_ID";
        String COMPOSER_QUANTITY = "COMPOSER_QUANTITY";
        String FLAG = "FLAG";
        String STOCK_TRACKING = "STOCK_TRACKING";
        String QUANTITY_RESULT = "QUANTITY_RESULT";

        String ID = "ID_ALIAS";

        String T1 = "T1";
        String T2 = "T2";
        String T3 = "T3";
        String TT1 = "TT1";
        String TT2 = "TT2";
        String TT3 = "TT3";
        String TT4 = "TT4";
        String TTT1 = "TTT1";
        String TTT2 = "TTT2";

        String AS = " AS ";
        String IS = " IS ";
        String ON = " ON ";
        String NOT = " NOT ";
        String NULL = " NULL ";
        String ALL = " ALL ";
        String UNION = " UNION ";
        String WHERE = " WHERE ";
        String AND = " AND ";
        String SUM = " SUM ";
        String FROM = " FROM ";
        String SELECT = " SELECT ";
        String JOIN = " JOIN ";
        String GROUP_BY = " GROUP BY ";

        String coma = ", ";
        String dot = ".";
        String space = " ";
        String equals = " = ";
        String multiply = " * ";

        @SqlQuery
        String QUERY =
                SELECT + ITEM_GUID + coma + " -1 * " + SUM + "(" + QUANTITY + ")" + AS + QUANTITY_RESULT + coma + FLAG + coma + SOURCE + coma + STOCK_TRACKING + FROM + "("

                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM_MODIFIER_ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + T1 + dot + SaleItemTable.SALE_ITEM_GUID + coma + SaleItemTable.ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma + SaleItemTable.QUANTITY + multiply + MOD_QTY + AS + QUANTITY
                        + coma + MOD_ID + AS + ITEM_GUID + FROM + SaleItemTable.TABLE_NAME + space + T1
                        + JOIN + "(" + SELECT + SaleAddonTable.ITEM_GUID + coma + SaleAddonTable.CHILD_ITEM_ID + AS + MOD_ID + coma + SaleAddonTable.CHILD_ITEM_QTY + AS + MOD_QTY + FROM + SaleAddonTable.TABLE_NAME + ")" + T2
                        + ON + T2 + dot + SaleAddonTable.ITEM_GUID + equals + T1 + dot + SaleItemTable.SALE_ITEM_GUID + AND + MOD_ID + IS + NOT + NULL
                        + WHERE + T1 + dot + SaleItemTable.ORDER_GUID + " = ?)" + TT1 + UNION + ALL


                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + SaleItemTable.ITEM_GUID + AS + ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma + SaleItemTable.QUANTITY + AS + QUANTITY + FROM + SaleItemTable.TABLE_NAME + space + T1
                        + WHERE + T1 + dot + SaleItemTable.ORDER_GUID + " = ?)" + TT2 + UNION + ALL


                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM_COMPOSER_ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + ComposerTable.ITEM_CHILD_ID + AS + ITEM_GUID + coma + ComposerTable.STORE_TRACKING_ENABLED + coma + ComposerTable.QUANTITY + multiply + ITEM_QUANTITY + AS + QUANTITY + coma
                        + ComposerTable.ITEM_HOST_ID + coma + IBemaSyncTable.IS_DELETED + FROM + ComposerTable.TABLE_NAME + space + T1
                        + JOIN + "(" + SELECT + SaleItemTable.ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma + SaleItemTable.QUANTITY + AS + ITEM_QUANTITY + FROM + SaleItemTable.TABLE_NAME + ")" + T2
                        + ON + T2 + dot + SaleItemTable.ITEM_GUID + equals + T1 + dot + ComposerTable.ITEM_HOST_ID
                        + WHERE + T1 + dot + ComposerTable.STORE_TRACKING_ENABLED + " = 1" + AND + T1 + dot + IBemaSyncTable.IS_DELETED + " = 0" + AND + T2 + dot + SaleItemTable.ORDER_GUID + " = ?)" + TT3 + UNION + ALL


                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM_MODIFIER_COMPOSER_ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + ITEM_GUID + coma + T1 + dot + SaleItemTable.SALE_ITEM_GUID + coma + SaleItemTable.ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma
                        + SaleItemTable.QUANTITY + multiply + MOD_QTY + multiply + COMPOSER_QUANTITY + AS + QUANTITY + FROM + SaleItemTable.TABLE_NAME + space + T1
                        + JOIN + "(" + SELECT + SaleAddonTable.ITEM_GUID + coma + SaleAddonTable.CHILD_ITEM_ID + AS + MOD_ID + coma + SaleAddonTable.CHILD_ITEM_QTY + AS + MOD_QTY + FROM + SaleAddonTable.TABLE_NAME + ")" + T2
                        + ON + T2 + dot + SaleAddonTable.ITEM_GUID + equals + T1 + dot + SaleItemTable.SALE_ITEM_GUID + AND + MOD_ID + IS + NOT + NULL
                        + JOIN + "(" + SELECT + ComposerTable.STORE_TRACKING_ENABLED
                        + coma + ComposerTable.ITEM_CHILD_ID + AS + ITEM_GUID + coma + IBemaSyncTable.IS_DELETED + coma + ComposerTable.QUANTITY + AS + COMPOSER_QUANTITY + coma
                        + ComposerTable.ITEM_HOST_ID + FROM + ComposerTable.TABLE_NAME + ")" + T3
                        + ON + T3 + dot + IBemaSyncTable.IS_DELETED + " = 0" + AND + T3 + dot + ComposerTable.STORE_TRACKING_ENABLED + " = 1" + AND + T2 + dot + MOD_ID + equals + T3 + dot + ComposerTable.ITEM_HOST_ID
                        + WHERE + T1 + dot + SaleItemTable.ORDER_GUID + " = ?)" + TT4
                        + ")" + TTT1


                        + JOIN + "(" + SELECT + ItemTable.GUID + coma + ItemTable.UPDATE_QTY_FLAG + AS + FLAG + coma + ItemTable.STOCK_TRACKING + AS + STOCK_TRACKING + FROM + ItemTable.TABLE_NAME + ")" + TTT2
                        + ON + TTT2 + dot + ItemTable.GUID + equals + ITEM_GUID
                        + GROUP_BY + ITEM_GUID;

        //// ****************************************            READABLE    V3   not valid anymore   ****************************************** //////

//        SELECT ITEM_GUID, sum(QUANTITY), FLAG, SOURCE FROM (
//                SELECT ITEM_GUID, QUANTITY, 'ITEM_MODIFIER_ITEM' AS SOURCE FROM (
//                SELECT T1.SALE_ITEM_ID, ITEM_ID, ORDER_ID, QUANTITY * MOD_QTY AS QUANTITY, MOD_ID AS ITEM_GUID FROM SALE_ORDER_ITEM T1
//                        JOIN (SELECT SALE_ITEM_ID, SALE_CHILD_ITEM_QTY AS MOD_QTY, SALE_CHILD_ITEM_ID AS MOD_ID FROM SALE_ORDER_ITEM_ADDON) T2
//        ON T2.SALE_ITEM_ID = T1.SALE_ITEM_ID AND MOD_ID IS NOT NULL
//        WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//                ) TT0
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM' AS SOURCE FROM (
//                SELECT ITEM_ID AS ITEM_GUID, ORDER_ID, QUANTITY AS QUANTITY FROM SALE_ORDER_ITEM T1
//                WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//        ) TT1
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_COMPOSER_ITEM' AS SOURCE FROM (
//                SELECT ITEM_CHILD_ID AS ITEM_GUID, STORE_TRACKING_ENABLED, QUANTITY * ITEM_QUANTITY AS QUANTITY, ITEM_HOST_ID FROM COMPOSER T1
//                JOIN (SELECT ITEM_ID, ORDER_ID, QUANTITY AS ITEM_QUANTITY FROM SALE_ORDER_ITEM) T2
//        ON T2.ITEM_ID = T1.ITEM_HOST_ID
//        WHERE T2.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773' AND T1.STORE_TRACKING_ENABLED = 1
//                ) TT2
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_MODIFIER_COMPOSER_ITEM' AS SOURCE FROM (
//                SELECT T1.SALE_ITEM_ID, ITEM_ID, ORDER_ID, QUANTITY * MOD_QTY * COMPOSER_QUANTITY AS QUANTITY, MOD_ID AS ITEM_GUID FROM SALE_ORDER_ITEM T1
//                JOIN (SELECT SALE_ITEM_ID, SALE_CHILD_ITEM_QTY AS MOD_QTY, SALE_CHILD_ITEM_ID AS MOD_ID FROM SALE_ORDER_ITEM_ADDON) T2
//        ON T2.SALE_ITEM_ID = T1.SALE_ITEM_ID AND MOD_ID IS NOT NULL
//        JOIN (SELECT ITEM_CHILD_ID AS ITEM_GUID, QUANTITY AS COMPOSER_QUANTITY,STORE_TRACKING_ENABLED, ITEM_HOST_ID FROM COMPOSER) T3
//        ON T2.MOD_ID = T3.ITEM_HOST_ID AND T3.STORE_TRACKING_ENABLED = 0
//        WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//                ) TT3 ) TTT1
//                        JOIN (SELECT ID, UPDATE_QTY_FLAG AS FLAG FROM ITEM) TTT2
//        ON TTT2.ID = ITEM_GUID
//        GROUP BY ITEM_GUID

        //// ****************************************            READABLE    V2      ****************************************** //////

//        SELECT ITEM_GUID, sum(QUANTITY), SOURCE FROM (
//                SELECT ITEM_GUID, QUANTITY, 'ITEM_MODIFIER_ITEM' AS SOURCE FROM (
//                SELECT T1.SALE_ITEM_ID, ITEM_ID, ORDER_ID, QUANTITY * MOD_QTY AS QUANTITY, MOD_ID AS ITEM_GUID FROM SALE_ORDER_ITEM T1
//                        JOIN (SELECT SALE_ITEM_ID, SALE_CHILD_ITEM_QTY AS MOD_QTY, SALE_CHILD_ITEM_ID AS MOD_ID FROM SALE_ORDER_ITEM_ADDON) T2
//        ON T2.SALE_ITEM_ID = T1.SALE_ITEM_ID AND MOD_ID IS NOT NULL
//        WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//                ) TT0
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM' AS SOURCE FROM (
//                SELECT ITEM_ID AS ITEM_GUID, ORDER_ID, QUANTITY AS QUANTITY FROM SALE_ORDER_ITEM T1
//                WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//        ) TT1
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_COMPOSER_ITEM' AS SOURCE FROM (
//                SELECT ITEM_CHILD_ID AS ITEM_GUID, STORE_TRACKING_ENABLED, QUANTITY * ITEM_QUANTITY AS QUANTITY, ITEM_HOST_ID FROM COMPOSER T1
//                JOIN (SELECT ITEM_ID, ORDER_ID, QUANTITY AS ITEM_QUANTITY FROM SALE_ORDER_ITEM) T2
//        ON T2.ITEM_ID = T1.ITEM_HOST_ID
//        WHERE T2.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773' AND T1.STORE_TRACKING_ENABLED = 1 AND T1.IS_DELETED
//                ) TT2
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_MODIFIER_COMPOSER_ITEM' AS SOURCE FROM (
//                SELECT T1.SALE_ITEM_ID, ITEM_ID, ORDER_ID, QUANTITY * MOD_QTY * COMPOSER_QUANTITY AS QUANTITY, MOD_ID AS ITEM_GUID FROM SALE_ORDER_ITEM T1
//                JOIN (SELECT SALE_ITEM_ID, SALE_CHILD_ITEM_QTY AS MOD_QTY, SALE_CHILD_ITEM_ID AS MOD_ID FROM SALE_ORDER_ITEM_ADDON) T2
//        ON T2.SALE_ITEM_ID = T1.SALE_ITEM_ID AND MOD_ID IS NOT NULL
//        JOIN (SELECT ITEM_CHILD_ID AS ITEM_GUID, QUANTITY AS COMPOSER_QUANTITY,STORE_TRACKING_ENABLED, ITEM_HOST_ID FROM COMPOSER) T3
//        ON T2.MOD_ID = T3.ITEM_HOST_ID AND T3.STORE_TRACKING_ENABLED = 0
//        WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//                ) TT3 ) TTT1 GROUP BY ITEM_GUID

        //// ****************************************            READABLE    V1      ****************************************** //////
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_MODIFIER_ITEM' AS SOURCE FROM (
//                SELECT T1.SALE_ITEM_ID, ITEM_ID, ORDER_ID, QUANTITY * MOD_QTY AS QUANTITY, MOD_ID AS ITEM_GUID FROM SALE_ORDER_ITEM T1
//                JOIN (SELECT SALE_ITEM_ID, SALE_CHILD_ITEM_QTY AS MOD_QTY, SALE_CHILD_ITEM_ID AS MOD_ID FROM SALE_ORDER_ITEM_ADDON) T2
//        ON T2.SALE_ITEM_ID = T1.SALE_ITEM_ID AND MOD_ID IS NOT NULL
//        WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//        ) TT0
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM' AS SOURCE FROM (
//                SELECT ITEM_ID AS ITEM_GUID, ORDER_ID, QUANTITY AS QUANTITY FROM SALE_ORDER_ITEM T1
//                WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//        ) TT1
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_COMPOSER_ITEM' AS SOURCE FROM (
//                SELECT ITEM_CHILD_ID AS ITEM_GUID, QUANTITY * ITEM_QUANTITY AS QUANTITY, ITEM_HOST_ID FROM COMPOSER T1
//                JOIN (SELECT ITEM_ID, ORDER_ID, QUANTITY AS ITEM_QUANTITY FROM SALE_ORDER_ITEM) T2
//        ON T2.ITEM_ID = T1.ITEM_HOST_ID
//        WHERE T2.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//        ) TT2
//        UNION ALL
//        SELECT ITEM_GUID, QUANTITY, 'ITEM_MODIFIER_COMPOSER_ITEM' AS SOURCE FROM (
//                SELECT T1.SALE_ITEM_ID, ITEM_ID, ORDER_ID, QUANTITY * MOD_QTY * COMPOSER_QUANTITY AS QUANTITY, MOD_ID AS ITEM_GUID FROM SALE_ORDER_ITEM T1
//                JOIN (SELECT SALE_ITEM_ID, SALE_CHILD_ITEM_QTY AS MOD_QTY, SALE_CHILD_ITEM_ID AS MOD_ID FROM SALE_ORDER_ITEM_ADDON) T2
//        ON T2.SALE_ITEM_ID = T1.SALE_ITEM_ID AND MOD_ID IS NOT NULL
//        JOIN (SELECT ITEM_CHILD_ID AS ITEM_GUID, QUANTITY AS COMPOSER_QUANTITY, ITEM_HOST_ID FROM COMPOSER) T3
//        ON T2.MOD_ID = T3.ITEM_HOST_ID
//        WHERE T1.ORDER_ID = '6153c2b5-1dd5-48ef-aba5-4c1852e47773'
//        ) TT3
        //// ****************************************            READABLE           ****************************************** //////
    }

    @RawQuery(ReturnOrderItemsMappingQuery.VIEW_NAME)
    public interface ReturnOrderItemsMappingQuery {

        String VIEW_NAME = "mapping_return_item";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "mapping_return_item";

        String ITEM_GUID = "ITEM_GUID_TAG";
        String QUANTITY = "QUANTITY_TAG";
        String ITEM_QUANTITY = "ITEM_QUANTITY";
        String SOURCE = "SOURCE";
        String MOD_QTY = "MOD_QTY";
        String MOD_ID = "MOD_ID";
        String COMPOSER_QUANTITY = "COMPOSER_QUANTITY";
        String FLAG = "FLAG";
        String STOCK_TRACKING = "STOCK_TRACKING";
        String QUANTITY_RESULT = "QUANTITY_RESULT";

        String ID = "ID_ALIAS";

        String T1 = "T1";
        String T2 = "T2";
        String T3 = "T3";
        String TT1 = "TT1";
        String TT2 = "TT2";
        String TT3 = "TT3";
        String TT4 = "TT4";
        String TTT1 = "TTT1";
        String TTT2 = "TTT2";

        String AS = " AS ";
        String IS = " IS ";
        String ON = " ON ";
        String NOT = " NOT ";
        String NULL = " NULL ";
        String ALL = " ALL ";
        String UNION = " UNION ";
        String WHERE = " WHERE ";
        String AND = " AND ";
        String SUM = " SUM ";
        String FROM = " FROM ";
        String SELECT = " SELECT ";
        String JOIN = " JOIN ";
        String GROUP_BY = " GROUP BY ";

        String coma = ", ";
        String dot = ".";
        String space = " ";
        String equals = " = ";
        String multiply = " * ";

        @SqlQuery
        String QUERY =
                SELECT + ITEM_GUID + coma + " -1 * " + SUM + "(" + QUANTITY + ")" + AS + QUANTITY_RESULT + coma + FLAG + coma + SOURCE + coma + STOCK_TRACKING
                        + FROM + "("

                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM_MODIFIER_ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + T1 + dot + SaleItemTable.SALE_ITEM_GUID + coma + SaleItemTable.ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma + SaleItemTable.QUANTITY + multiply + MOD_QTY + AS + QUANTITY
                        + coma + MOD_ID + AS + ITEM_GUID + FROM + SaleItemTable.TABLE_NAME + space + T1
                        + JOIN + "(" + SELECT + SaleAddonTable.ITEM_GUID + coma + SaleAddonTable.CHILD_ITEM_ID + AS + MOD_ID + coma + SaleAddonTable.CHILD_ITEM_QTY + AS + MOD_QTY + FROM + SaleAddonTable.TABLE_NAME + ")" + T2
                        + ON + T2 + dot + SaleAddonTable.ITEM_GUID + equals + T1 + dot + SaleItemTable.SALE_ITEM_GUID + AND + MOD_ID + IS + NOT + NULL
                        + WHERE + T1 + dot + SaleItemTable.ORDER_GUID + " = ? AND " + T1 + dot + SaleItemTable.SALE_ITEM_GUID + " = ?)" + TT1

                        + UNION + ALL


                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + SaleItemTable.ITEM_GUID + AS + ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma + SaleItemTable.QUANTITY + AS + QUANTITY + FROM + SaleItemTable.TABLE_NAME + space + T1
                        + WHERE + T1 + dot + SaleItemTable.ORDER_GUID + " = ? AND " + T1 + dot + SaleItemTable.SALE_ITEM_GUID + " = ?)" + TT2

                        + UNION + ALL


                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM_COMPOSER_ITEM\'" + AS + SOURCE +
                        FROM + "(" + SELECT + ComposerTable.ITEM_CHILD_ID + AS + ITEM_GUID + coma + ComposerTable.STORE_TRACKING_ENABLED + coma + ComposerTable.QUANTITY + multiply + ITEM_QUANTITY + AS + QUANTITY + coma
                        + ComposerTable.ITEM_HOST_ID + coma + IBemaSyncTable.IS_DELETED + coma + SaleItemTable.SALE_ITEM_GUID +
                        FROM + ComposerTable.TABLE_NAME + space + T1
                        + JOIN + "(" + SELECT + SaleItemTable.ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma + SaleItemTable.QUANTITY + AS + ITEM_QUANTITY + coma
                        + SaleItemTable.SALE_ITEM_GUID + FROM + SaleItemTable.TABLE_NAME + ")" + T2
                        + ON + T2 + dot + SaleItemTable.ITEM_GUID + equals + T1 + dot + ComposerTable.ITEM_HOST_ID
                        + WHERE + T1 + dot + ComposerTable.STORE_TRACKING_ENABLED + " = 1" + AND + T1 + dot + IBemaSyncTable.IS_DELETED + " = 0" + AND + T2 + dot
                        + SaleItemTable.ORDER_GUID + " = ? AND " + T2 + dot + SaleItemTable.SALE_ITEM_GUID + " = ?)" + TT3 + UNION + ALL


                        + SELECT + ITEM_GUID + coma + QUANTITY + coma + "\'ITEM_MODIFIER_COMPOSER_ITEM\'" + AS + SOURCE + FROM + "("
                        + SELECT + ITEM_GUID + coma + T1 + dot + SaleItemTable.SALE_ITEM_GUID + coma + SaleItemTable.ITEM_GUID + coma + SaleItemTable.ORDER_GUID + coma
                        + SaleItemTable.QUANTITY + multiply + MOD_QTY + multiply + COMPOSER_QUANTITY + AS + QUANTITY + FROM + SaleItemTable.TABLE_NAME + space + T1
                        + JOIN + "(" + SELECT + SaleAddonTable.ITEM_GUID + coma + SaleAddonTable.CHILD_ITEM_ID + AS + MOD_ID + coma + SaleAddonTable.CHILD_ITEM_QTY + AS + MOD_QTY + FROM + SaleAddonTable.TABLE_NAME + ")" + T2
                        + ON + T2 + dot + SaleAddonTable.ITEM_GUID + equals + T1 + dot + SaleItemTable.SALE_ITEM_GUID + AND + MOD_ID + IS + NOT + NULL
                        + JOIN + "(" + SELECT + ComposerTable.STORE_TRACKING_ENABLED
                        + coma + ComposerTable.ITEM_CHILD_ID + AS + ITEM_GUID + coma + IBemaSyncTable.IS_DELETED + coma + ComposerTable.QUANTITY + AS + COMPOSER_QUANTITY + coma
                        + ComposerTable.ITEM_HOST_ID + FROM + ComposerTable.TABLE_NAME + ")" + T3
                        + ON + T3 + dot + IBemaSyncTable.IS_DELETED + " = 0" + AND + T3 + dot + ComposerTable.STORE_TRACKING_ENABLED + " = 1" + AND + T2 + dot + MOD_ID + equals + T3 + dot + ComposerTable.ITEM_HOST_ID
                        + WHERE + T1 + dot + SaleItemTable.ORDER_GUID + " = ? AND " + T1 + dot + SaleItemTable.SALE_ITEM_GUID + " = ?)" + TT4
                        + ")" + TTT1


                        + JOIN + "(" + SELECT + ItemTable.GUID + coma + ItemTable.UPDATE_QTY_FLAG + AS + FLAG + coma + ItemTable.STOCK_TRACKING + AS + STOCK_TRACKING + FROM + ItemTable.TABLE_NAME + ")" + TTT2
                        + ON + TTT2 + dot + ItemTable.GUID + equals + ITEM_GUID
                        + GROUP_BY + ITEM_GUID;
    }

    @RawQuery(ItemRawQuery.QUERY_NAME)
    public interface ItemRawQuery {

        String QUERY_NAME = "item_raw";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "item_raw";

        @SqlQuery
        String QUERY = "SELECT * FROM " + ItemTable.TABLE_NAME;
    }

    @RawQuery(SaleOrderDailyRawQuery.QUERY_NAME)
    public interface SaleOrderDailyRawQuery {

        String QUERY_NAME = "sale_order_daily_raw";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sale_order_daily_raw";

        @SqlQuery
        String SQL = "SELECT * FROM " + SaleOrderTable.TABLE_NAME +
                " WHERE " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.CREATE_TIME + " > ?" +
                " AND " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.CREATE_TIME + " < ?";

    }

    @RawQuery(SaleOrderDailyRegisterRawQuery.QUERY_NAME)
    public interface SaleOrderDailyRegisterRawQuery {

        String QUERY_NAME = "sale_order_daily_register_raw";

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sale_order_daily_register_raw";

        @SqlQuery
        String SQL = "SELECT * FROM " + SaleOrderTable.TABLE_NAME +
                " WHERE " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.CREATE_TIME + " > ?" +
                " AND " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.CREATE_TIME + " < ?" +
                " AND " + SaleOrderTable.TABLE_NAME + "." + SaleOrderTable.REGISTER_ID + " = ?";

    }


    @RawQuery(SqlCommandHostCountQuery.VIEW_NAME)
    public interface SqlCommandHostCountQuery {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sql_command_host_count_query";

        String VIEW_NAME = "sql_command_host_count_query";

        @SqlQuery
        String SQL = "SELECT COUNT(*)" +
                " FROM " + SqlCommandHostTable.TABLE_NAME +
                " LEFT JOIN " + SqlCommandClientTable.TABLE_NAME +
                " ON " + SqlCommandHostTable.TABLE_NAME + "." + SqlCommandHostTable.ORDER_COMMAND + " = " +
                SqlCommandClientTable.TABLE_NAME + "." + SqlCommandClientTable.COMMAND_ID +
                " AND " + SqlCommandClientTable.TABLE_NAME + "." + SqlCommandClientTable.CLIENT_SERIAL + " = ?" +
                " WHERE " + SqlCommandClientTable.TABLE_NAME + "." + SqlCommandClientTable.COMMAND_ID + " is null";
    }


    @RawQuery(SqlCommandLocalSent.URI_CONTENT)
    public interface SqlCommandLocalSent {

        @URI(type = URI.Type.DIR, onlyQuery = true)
        String URI_CONTENT = "sql_command_host_sent_query";

        @SqlQuery
        String SQL = "SELECT " + SqlCommandClientTable.COMMAND_ID +
                " FROM " + SqlCommandClientTable.TABLE_NAME +
                " GROUP BY " + SqlCommandClientTable.COMMAND_ID +
                " HAVING COUNT(DISTINCT " + SqlCommandClientTable.CLIENT_SERIAL + ") >= " +
                "(SELECT COUNT(" + RegisterTable.ID + ") FROM " + RegisterTable.TABLE_NAME +
                " WHERE " + RegisterTable.REGISTER_SERIAL+ " <> ? " +
                " AND (" + RegisterTable.STATUS + " = 0 OR " + RegisterTable.STATUS + " = 1)" +
                " AND " + RegisterTable.IS_DELETED + " = 0)";
    }


}
