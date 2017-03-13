package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.converter.IntegerFunction;
import com.kaching123.tcr.model.converter.ItemFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.UnitUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._max;
import static com.kaching123.tcr.model.ContentValuesUtil._putDiscount;
import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;
import static com.kaching123.tcr.model.ContentValuesUtil._putItemRefType;

public class ItemModel extends BaseItemModel implements Serializable, IValueModel {

    private static final Uri URI_ITEM = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_RAW_QUERY = ShopProvider.contentUri(ShopStore.ItemRawQuery.URI_CONTENT);

    private static final long serialVersionUID = 1L;

    public String guid;
    public String categoryId;
    public String description;
    public String code;
    public String eanCode;
    public String productCode;
    public PriceType priceType = PriceType.FIXED;
    public CodeType codeType;
    public BigDecimal price;
    public BigDecimal price1;
    public BigDecimal price2;
    public BigDecimal price3;
    public BigDecimal price4;
    public BigDecimal price5;
    public BigDecimal availableQty;
    public String unitsLabelId;
    public boolean isStockTracking;
    public boolean limitQty;
    public boolean isActiveStatus;
    public boolean isDiscountable;
    public BigDecimal discount;
    public DiscountType discountType  = DiscountType.PERCENT;
    public boolean isTaxable;
    public BigDecimal cost;
    public BigDecimal minimumQty;
    public BigDecimal recommendedQty;
    public String updateQtyFlag;
    public String taxGroupGuid;
    public String taxGroupGuid2;
    @Deprecated
    public boolean isPcsUnit;
    public int orderNum;
    public String printerAliasGuid;
    public int btnView;
    public boolean hasNotes;
    public boolean serializable;
    public boolean commissionEligible = true;
    public BigDecimal commission;
    public boolean isSalable;
    public ItemRefType refType;
    public String referenceItemGuid;
    public boolean ignoreMovementupdate;
    public BigDecimal loyaltyPoints;
    public boolean excludeFromLoyaltyPlan;
    public boolean isEbtEligible;

    private List<String> mIgnoreFields;

    public ItemModel() {
        this.guid = UUID.randomUUID().toString();
    }

    public ItemModel(String guid) {
        this.guid = guid;
    }

    public ItemModel(String guid,
                     String categoryId,
                     String description,
                     String code,
                     String eanCode,
                     String productCode,
                     PriceType priceType,
                     BigDecimal price,
                     BigDecimal price1,
                     BigDecimal price2,
                     BigDecimal price3,
                     BigDecimal price4,
                     BigDecimal price5,
                     BigDecimal availableQty,
                     String unitsLabelId,
                     boolean isStockTracking,
                     boolean limitQty,
                     boolean isActiveStatus,
                     boolean isDiscountable,
                     boolean isSalable,
                     BigDecimal discount,
                     DiscountType discountType,
                     boolean isTaxable,
                     BigDecimal cost,
                     BigDecimal minimumQty,
                     BigDecimal recommendedQty,
                     String updateQtyFlag,
                     String taxGroupGuid,
                     String taxGroupGuid2,
                     int orderNum,
                     String printerAliasGuid,
                     int btnView,
                     boolean hasNotes,
                     boolean serializable,
                     CodeType codeType,
                     boolean commissionEligible,
                     BigDecimal commission,
                     String referenceItemGuid,
                     ItemRefType refType,
                     BigDecimal loyaltyPoints,
                     boolean excludeFromLoyaltyPlan,
                     boolean isEbtEligible,
                     List<String> ignoreFields) {
        super();
        this.guid = guid;
        this.categoryId = categoryId;
        this.description = description;
        this.code = code;
        this.eanCode = eanCode;
        this.productCode = productCode;
        this.priceType = priceType;
        this.price = price;
        this.price1 = price1;
        this.price2 = price2;
        this.price3 = price3;
        this.price4 = price4;
        this.price5 = price5;
        this.availableQty = availableQty;
        this.unitsLabelId = unitsLabelId;
        this.isStockTracking = isStockTracking;
        this.limitQty = limitQty;
        this.isActiveStatus = isActiveStatus;
        this.isDiscountable = isDiscountable;
        this.isSalable = isSalable;
        this.discount = discount;
        this.discountType = discountType;
        this.isTaxable = isTaxable;
        this.cost = cost;
        this.isPcsUnit = UnitUtil.isPcs(priceType);
        this.minimumQty = minimumQty;
        this.recommendedQty = recommendedQty;
        this.updateQtyFlag = updateQtyFlag;
        this.taxGroupGuid = taxGroupGuid;
        this.taxGroupGuid2 = taxGroupGuid2;
        this.orderNum = orderNum;
        this.printerAliasGuid = printerAliasGuid;
        this.btnView = btnView;
        this.hasNotes = hasNotes;
        this.serializable = serializable;
        this.codeType = codeType;
        this.commissionEligible = commissionEligible;
        this.commission = commission;
        this.referenceItemGuid = referenceItemGuid;
        this.refType = refType;
        this.loyaltyPoints = loyaltyPoints;
        this.excludeFromLoyaltyPlan = excludeFromLoyaltyPlan;
        this.isEbtEligible = isEbtEligible;

        this.mIgnoreFields = ignoreFields;
    }

    public ItemModel(ItemModel itemModel) {
        super();
        this.guid = itemModel.guid;
        this.categoryId = itemModel.categoryId;
        this.description = itemModel.description;
        this.code = itemModel.code;
        this.eanCode = itemModel.eanCode;
        this.productCode = itemModel.productCode;
        this.priceType = itemModel.priceType;
        this.price = itemModel.price;
        this.price1 = itemModel.price1;
        this.price2 = itemModel.price2;
        this.price3 = itemModel.price3;
        this.price4 = itemModel.price4;
        this.price5 = itemModel.price5;
        this.availableQty = itemModel.availableQty;
        this.unitsLabelId = itemModel.unitsLabelId;
        this.isStockTracking = itemModel.isStockTracking;
        this.limitQty = itemModel.limitQty;
        this.isActiveStatus = itemModel.isActiveStatus;
        this.isDiscountable = itemModel.isDiscountable;
        this.isSalable = itemModel.isSalable;
        this.discount = itemModel.discount;
        this.discountType = itemModel.discountType;
        this.isTaxable = itemModel.isTaxable;
        this.cost = itemModel.cost;
        this.isPcsUnit = itemModel.isPcsUnit;
        this.minimumQty = itemModel.minimumQty;
        this.recommendedQty = itemModel.recommendedQty;
        this.updateQtyFlag = itemModel.updateQtyFlag;
        this.taxGroupGuid = itemModel.taxGroupGuid;
        this.taxGroupGuid2 = itemModel.taxGroupGuid2;
        this.orderNum = itemModel.orderNum;
        this.printerAliasGuid = itemModel.printerAliasGuid;
        this.btnView = itemModel.btnView;
        this.hasNotes = itemModel.hasNotes;
        this.serializable = itemModel.serializable;
        this.codeType = itemModel.codeType;
        this.commissionEligible = itemModel.commissionEligible;
        this.commission = itemModel.commission;
        this.refType = itemModel.refType;
        this.referenceItemGuid = itemModel.referenceItemGuid;
        this.loyaltyPoints = itemModel.loyaltyPoints;
        this.excludeFromLoyaltyPlan = itemModel.excludeFromLoyaltyPlan;
        this.isEbtEligible = itemModel.isEbtEligible;
    }


    public ItemModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(ItemTable.GUID)),
                c.getString(c.getColumnIndex(ItemTable.CATEGORY_ID)),
                c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)),
                c.getString(c.getColumnIndex(ItemTable.CODE)),
                c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                _enum(PriceType.class, c.getString(c.getColumnIndex(ItemTable.PRICE_TYPE)), PriceType.OPEN),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.SALE_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.SALE_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.SALE_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.SALE_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.SALE_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.SALE_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY))),
                c.getString(c.getColumnIndex(ItemTable.UNIT_LABEL_ID)),
                c.getInt(c.getColumnIndex(ItemTable.STOCK_TRACKING)) == 1,
                c.getInt(c.getColumnIndex(ItemTable.LIMIT_QTY)) == 1,
                c.getInt(c.getColumnIndex(ItemTable.ACTIVE_STATUS)) == 1,
                c.getInt(c.getColumnIndex(ItemTable.DISCOUNTABLE)) == 1,
                c.getInt(c.getColumnIndex(ItemTable.SALABLE)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.DISCOUNT))),
                _enum(DiscountType.class, c.getString(c.getColumnIndex(ItemTable.DISCOUNT_TYPE)), DiscountType.PERCENT),
                c.getInt(c.getColumnIndex(ItemTable.TAXABLE)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.COST))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.MINIMUM_QTY))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.RECOMMENDED_QTY))),
                c.getString(c.getColumnIndex(ItemTable.UPDATE_QTY_FLAG)),
                c.getString(c.getColumnIndex(ItemTable.TAX_GROUP_GUID)),
                c.getString(c.getColumnIndex(ItemTable.TAX_GROUP_GUID2)),
                c.getInt(c.getColumnIndex(ItemTable.ORDER_NUM)),
                c.getString(c.getColumnIndex(ItemTable.PRINTER_ALIAS_GUID)),
                c.getInt(c.getColumnIndex(ItemTable.BUTTON_VIEW)),
                c.getInt(c.getColumnIndex(ItemTable.HAS_NOTES)) == 1,
                c.getInt(c.getColumnIndex(ItemTable.SERIALIZABLE)) == 1,
                _enum(CodeType.class, c.getString(c.getColumnIndex(ItemTable.CODE_TYPE)), null),
                c.getInt(c.getColumnIndex(ItemTable.ELIGIBLE_FOR_COMMISSION)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.COMMISSION))),
                c.getString(c.getColumnIndex(ItemTable.REFERENCE_ITEM_ID)),
                ItemRefType.valueOf(c.getString(c.getColumnIndex(ItemTable.ITEM_REF_TYPE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemTable.LOYALTY_POINTS))),
                c.getInt(c.getColumnIndex(ItemTable.EXCLUDE_FROM_LOYALTY_PLAN)) == 1,
                c.getInt(c.getColumnIndex(ItemTable.EBT_ELIGIBLE)) == 1,
                null
        );
    }


    public static int getMaxOrderNum(Context context, String categoryId){
        Integer i = ProviderAction.query(ShopProvider.contentUri(ItemTable.URI_CONTENT))
                .projection(_max(ItemTable.ORDER_NUM))
                .where(ItemTable.CATEGORY_ID + " = ?", categoryId)
                .perform(context)
                .toFluentIterable(new IntegerFunction())
                .first().or(0);

        return i;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public boolean isLimitQtySelected() {
        return isStockTracking && limitQty;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.GUID)) values.put(ItemTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.CATEGORY_ID)) values.put(ItemTable.CATEGORY_ID, categoryId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.DESCRIPTION)) values.put(ItemTable.DESCRIPTION, description);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.CODE)) values.put(ItemTable.CODE, code);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.EAN_CODE)) values.put(ItemTable.EAN_CODE, eanCode);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRODUCT_CODE)) values.put(ItemTable.PRODUCT_CODE, productCode);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRICE_TYPE)) values.put(ItemTable.PRICE_TYPE, priceType.ordinal());
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.SALE_PRICE)) values.put(ItemTable.SALE_PRICE, _decimal(price));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRICE_1)) values.put(ItemTable.PRICE_1, _decimal(price1));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRICE_2)) values.put(ItemTable.PRICE_2, _decimal(price2));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRICE_3)) values.put(ItemTable.PRICE_3, _decimal(price3));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRICE_4)) values.put(ItemTable.PRICE_4, _decimal(price4));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRICE_5)) values.put(ItemTable.PRICE_5, _decimal(price5));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.UNIT_LABEL_ID)) values.put(ItemTable.UNIT_LABEL_ID, unitsLabelId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.STOCK_TRACKING)) values.put(ItemTable.STOCK_TRACKING, isStockTracking);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.LIMIT_QTY)) values.put(ItemTable.LIMIT_QTY, limitQty);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.ACTIVE_STATUS)) values.put(ItemTable.ACTIVE_STATUS, isActiveStatus);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.DISCOUNTABLE)) values.put(ItemTable.DISCOUNTABLE, isDiscountable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.SALABLE)) values.put(ItemTable.SALABLE, isSalable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.DISCOUNT)) values.put(ItemTable.DISCOUNT, _decimal(discount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.DISCOUNT_TYPE)) _putDiscount(values, ItemTable.DISCOUNT_TYPE, discountType);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.TAXABLE)) values.put(ItemTable.TAXABLE, isTaxable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.COST)) values.put(ItemTable.COST, _decimal(cost));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.MINIMUM_QTY)) values.put(ItemTable.MINIMUM_QTY, _decimalQty(minimumQty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.RECOMMENDED_QTY)) values.put(ItemTable.RECOMMENDED_QTY, _decimalQty(recommendedQty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.UPDATE_QTY_FLAG)) values.put(ItemTable.UPDATE_QTY_FLAG, updateQtyFlag);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.TAX_GROUP_GUID)) values.put(ItemTable.TAX_GROUP_GUID, taxGroupGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.TAX_GROUP_GUID2)) values.put(ItemTable.TAX_GROUP_GUID2, taxGroupGuid2);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.ORDER_NUM)) values.put(ItemTable.ORDER_NUM, orderNum);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.PRINTER_ALIAS_GUID)) values.put(ItemTable.PRINTER_ALIAS_GUID, printerAliasGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.BUTTON_VIEW)) values.put(ItemTable.BUTTON_VIEW, btnView);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.HAS_NOTES)) values.put(ItemTable.HAS_NOTES, hasNotes);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.SERIALIZABLE)) values.put(ItemTable.SERIALIZABLE, serializable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.CODE_TYPE)) _putEnum(values, ItemTable.CODE_TYPE, codeType);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.ELIGIBLE_FOR_COMMISSION)) values.put(ItemTable.ELIGIBLE_FOR_COMMISSION, commissionEligible);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.COMMISSION)) values.put(ItemTable.COMMISSION, _decimal(commission));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.ITEM_REF_TYPE)) _putItemRefType(values, ItemTable.ITEM_REF_TYPE, refType);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.REFERENCE_ITEM_ID)) values.put(ItemTable.REFERENCE_ITEM_ID, referenceItemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.LOYALTY_POINTS)) values.put(ItemTable.LOYALTY_POINTS, _decimal(loyaltyPoints));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.EXCLUDE_FROM_LOYALTY_PLAN)) values.put(ItemTable.EXCLUDE_FROM_LOYALTY_PLAN, excludeFromLoyaltyPlan);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemTable.EBT_ELIGIBLE)) values.put(ItemTable.EBT_ELIGIBLE, isEbtEligible);
        return values;
    }

    @Override
    public String getIdColumn() {
        return ItemTable.GUID;
    }

    @Override
    public String toString() {
        return description;
    }

    public ContentValues toQtyValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        values.put(ItemTable.TMP_AVAILABLE_QTY, _decimalQty(availableQty));
        values.put(ItemTable.COST, _decimal(cost));
        return values;
    }

    public BigDecimal getPrice(int level){
        switch (level){
            case 1 : return price1;
            case 2 : return price2;
            case 3 : return price3;
            case 4 : return price4;
            case 5 : return price5;
            default: return price;
        }
    }

    public void setPrice(BigDecimal price, int level){
        switch (level){
            case 1 : price1 = price; break;
            case 2 : price2 = price; break;
            case 3 : price3 = price; break;
            case 4 : price4 = price; break;
            case 5 : price5 = price; break;
            default: this.price = price;
        }
    }

    public boolean isPcsUnit(){
        return UnitUtil.isPcs(this.priceType) || codeType != null;
    }

    public boolean isReferenceItem() {
        return this.refType == ItemRefType.Reference;
    }

    public static ItemModel getById(final Context context, final String itemGuid, boolean ignoreIsDeleted) {
        final Cursor cursor = ProviderAction.query(ignoreIsDeleted ? ITEM_RAW_QUERY : URI_ITEM)
                .where(ShopStore.ItemTable.GUID + " = ?", itemGuid)
                .perform(context);
        ItemModel item = null;
        if (cursor != null && cursor.moveToFirst()) {
            item = new ItemFunction().apply(cursor);
            cursor.close();
        }
        return item;
    }

}
