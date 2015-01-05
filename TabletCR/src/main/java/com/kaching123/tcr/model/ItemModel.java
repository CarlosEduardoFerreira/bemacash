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

public class ItemModel implements Serializable, IValueModel {

    /**
     *
     */
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
    public boolean isPcsUnit;
    public String defaultModifierGuid;
    public int orderNum;
    public String printerAliasGuid;
    public int btnView;
    public boolean hasNotes;
    public boolean serializable;
    public boolean commissionEligible = true;
    public BigDecimal commission;

    public ItemModel(){
        this.guid = UUID.randomUUID().toString();
    }

    public ItemModel(String guid) {
        this.guid = guid;
    }

    public ItemModel(String guid,
                     String categoryId,
                     String description,
                     String code, String eanCode, String productCode,
                     PriceType priceType, BigDecimal price, BigDecimal availableQty, String unitsLabel,

                     boolean isStockTracking, boolean isActiveStatus,
                     boolean isDiscountable, BigDecimal discount, DiscountType discountType,
                     boolean isTaxable,
                     BigDecimal cost,
                     BigDecimal minimumQty,
                     BigDecimal recommendedQty,
                     String updateQtyFlag,
                     String taxGroupGuid,
                     String defaultModifierGuid,
                     int orderNum,
                     String printerAliasGuid,
                     int btnView,
                     boolean hasNotes,
                     boolean serializable,
                     CodeType codeType,
                     boolean commissionEligible,
                     BigDecimal commission) {
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
        this.isStockTracking = isStockTracking;
        this.isActiveStatus = isActiveStatus;
        this.isDiscountable = isDiscountable;
        this.discount = discount;
        this.discountType = discountType;
        this.isTaxable = isTaxable;
        this.cost = cost;
        this.isPcsUnit = UnitUtil.isPcs(priceType);
        this.minimumQty = minimumQty;
        this.recommendedQty = recommendedQty;
        this.updateQtyFlag = updateQtyFlag;
        this.taxGroupGuid = taxGroupGuid;
        this.orderNum = orderNum;
        this.defaultModifierGuid = defaultModifierGuid;
        this.printerAliasGuid = printerAliasGuid;
        this.btnView = btnView;
        this.hasNotes = hasNotes;
        this.serializable = serializable;
        this.codeType = codeType;
        this.commissionEligible = commissionEligible;
        this.commission = commission;
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

        values.put(ItemTable.STOCK_TRACKING, isStockTracking);
        values.put(ItemTable.ACTIVE_STATUS, isActiveStatus);

        values.put(ItemTable.DISCOUNTABLE, isDiscountable);
        values.put(ItemTable.DISCOUNT, _decimal(discount));
        _putDiscount(values, ItemTable.DISCOUNT_TYPE, discountType);

        values.put(ItemTable.TAXABLE, isTaxable);
        values.put(ItemTable.COST, _decimal(cost));
        values.put(ItemTable.MINIMUM_QTY, _decimalQty(minimumQty));
        values.put(ItemTable.RECOMMENDED_QTY, _decimalQty(recommendedQty));
        values.put(ItemTable.UPDATE_QTY_FLAG, updateQtyFlag);
        values.put(ItemTable.TAX_GROUP_GUID, taxGroupGuid);

        values.put(ItemTable.DEFAULT_MODIFIER_GUID, defaultModifierGuid);

        values.put(ItemTable.ORDER_NUM, orderNum);

        values.put(ItemTable.PRINTER_ALIAS_GUID, printerAliasGuid);

        values.put(ItemTable.BUTTON_VIEW, btnView);

        values.put(ItemTable.HAS_NOTES, hasNotes);
        values.put(ItemTable.SERIALIZABLE, serializable);
        _putEnum(values, ItemTable.CODE_TYPE, codeType);

        values.put(ItemTable.ELIGIBLE_FOR_COMMISSION, commissionEligible);
        values.put(ItemTable.COMMISSION, _decimal(commission));

		return values;
	}
	
	@Override
	public String toString() {
		return description;
	}

}