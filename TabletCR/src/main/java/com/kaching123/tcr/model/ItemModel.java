package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.UnitUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._putDiscount;
import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;
import static com.kaching123.tcr.model.ContentValuesUtil._putItemRefType;

public class ItemModel extends BaseItemModel implements Serializable, IValueModel {

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
    public BigDecimal availableQty;
    public String unitsLabel;
    public String unitsLabelId;
    public boolean isStockTracking;
    public boolean isActiveStatus;
    public boolean isDiscountable;
    public BigDecimal discount;
    public DiscountType discountType;
    public boolean isTaxable;
    public BigDecimal cost;
    public BigDecimal minimumQty;
    public BigDecimal recommendedQty;
    public String updateQtyFlag;
    public String taxGroupGuid;
    public String taxGroupGuid2;
    public boolean isPcsUnit;
    public String defaultModifierGuid;
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
                     BigDecimal availableQty,
                     String unitsLabel,
                     String unitsLabelId,
                     boolean isStockTracking,
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
                     String defaultModifierGuid,
                     int orderNum,
                     String printerAliasGuid,
                     int btnView,
                     boolean hasNotes,
                     boolean serializable,
                     CodeType codeType,
                     boolean commissionEligible,
                     BigDecimal commission,
                     String referenceItemGuid,
                     ItemRefType refType) {
        super();
        this.guid = guid;
        this.categoryId = categoryId;
        this.description = description;
        this.code = code;
        this.eanCode = eanCode;
        this.productCode = productCode;
        this.priceType = priceType;
        this.price = price;
        this.availableQty = availableQty;
        this.unitsLabel = unitsLabel;
        this.unitsLabelId = unitsLabelId;
        this.isStockTracking = isStockTracking;
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
        this.defaultModifierGuid = defaultModifierGuid;
        this.printerAliasGuid = printerAliasGuid;
        this.btnView = btnView;
        this.hasNotes = hasNotes;
        this.serializable = serializable;
        this.codeType = codeType;
        this.commissionEligible = commissionEligible;
        this.commission = commission;
        this.referenceItemGuid = referenceItemGuid;
        this.refType = refType;
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
        this.availableQty = itemModel.availableQty;
        this.unitsLabel = itemModel.unitsLabel;
        this.unitsLabelId = itemModel.unitsLabelId;
        this.isStockTracking = itemModel.isStockTracking;
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
        this.defaultModifierGuid = itemModel.defaultModifierGuid;
        this.printerAliasGuid = itemModel.printerAliasGuid;
        this.btnView = itemModel.btnView;
        this.hasNotes = itemModel.hasNotes;
        this.serializable = itemModel.serializable;
        this.codeType = itemModel.codeType;
        this.commissionEligible = itemModel.commissionEligible;
        this.commission = itemModel.commission;
        this.refType = itemModel.refType;
        this.referenceItemGuid = itemModel.referenceItemGuid;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();

        values.put(ItemTable.GUID, guid);
        values.put(ItemTable.CATEGORY_ID, categoryId);

        values.put(ItemTable.DESCRIPTION, description);
        values.put(ItemTable.CODE, code);
        values.put(ItemTable.EAN_CODE, eanCode);
        values.put(ItemTable.PRODUCT_CODE, productCode);

        values.put(ItemTable.PRICE_TYPE, priceType.ordinal());
        values.put(ItemTable.SALE_PRICE, _decimal(price));
        values.put(ItemTable.UNITS_LABEL, unitsLabel);
        values.put(ItemTable.UNIT_LABEL_ID, unitsLabelId);

        values.put(ItemTable.STOCK_TRACKING, isStockTracking);
        values.put(ItemTable.ACTIVE_STATUS, isActiveStatus);

        values.put(ItemTable.DISCOUNTABLE, isDiscountable);
        values.put(ItemTable.SALABLE, isSalable);
        values.put(ItemTable.DISCOUNT, _decimal(discount));
        _putDiscount(values, ItemTable.DISCOUNT_TYPE, discountType);

        values.put(ItemTable.TAXABLE, isTaxable);
        values.put(ItemTable.COST, _decimal(cost));
        values.put(ItemTable.MINIMUM_QTY, _decimalQty(minimumQty));
        values.put(ItemTable.RECOMMENDED_QTY, _decimalQty(recommendedQty));
        values.put(ItemTable.UPDATE_QTY_FLAG, updateQtyFlag);
        values.put(ItemTable.TAX_GROUP_GUID, taxGroupGuid);
        values.put(ItemTable.TAX_GROUP_GUID2, taxGroupGuid2);

        values.put(ItemTable.DEFAULT_MODIFIER_GUID, defaultModifierGuid);

        values.put(ItemTable.ORDER_NUM, orderNum);

        values.put(ItemTable.PRINTER_ALIAS_GUID, printerAliasGuid);

        values.put(ItemTable.BUTTON_VIEW, btnView);

        values.put(ItemTable.HAS_NOTES, hasNotes);
        values.put(ItemTable.SERIALIZABLE, serializable);
        _putEnum(values, ItemTable.CODE_TYPE, codeType);

        values.put(ItemTable.ELIGIBLE_FOR_COMMISSION, commissionEligible);
        values.put(ItemTable.COMMISSION, _decimal(commission));

        _putItemRefType(values, ItemTable.ITEM_REF_TYPE, refType);
        values.put(ItemTable.REFERENCE_ITEM_ID, referenceItemGuid);

        return values;
    }

    @Override
    public String toString() {
        return description;
    }

    public ContentValues toQtyValues() {
        ContentValues values = new ContentValues();
        values.put(ItemTable.TMP_AVAILABLE_QTY, _decimalQty(availableQty));
        values.put(ItemTable.COST, _decimal(cost));
        return values;
    }

}
