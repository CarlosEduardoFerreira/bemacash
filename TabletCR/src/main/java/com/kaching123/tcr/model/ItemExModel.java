package com.kaching123.tcr.model;

/*
 * Created by gdubina on 19/11/13.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemExModel extends ItemModel {

    private static final Uri URI_VARIANT_ITEMS = ShopProvider.contentUri(VariantItemTable.URI_CONTENT);

    private static final Uri URI_VARIANT_SUB_ITEMS = ShopProvider.contentUri(VariantSubItemTable.URI_CONTENT);

    public ArrayList<Unit> tmpUnit = new ArrayList<>();

    public String tmpBarcode;
    public int currentPriceLevel;
    public int modifiersCount;
    public int addonsCount;
    public int optionalCount;
    public int unitCount;
    public int availableUnitCount;
    public int composersCount;
    public int restrictComposersCount;

    public String departmentGuid;
    public BigDecimal tax;
    public BigDecimal tax2;

    public String matrixGuid;

    public String shortCut;

    public boolean isAComposisiton;
    public boolean isAComposer;
    public boolean isIncentive;

    public ItemExModel() {
        super();
    }

    public ItemExModel(BigDecimal itemPrice){
        super(UUID.randomUUID().toString(),
                null,
                "Gift Card",
                null,
                null,
                "Gift Card",
                PriceType.FIXED,
                itemPrice,
                null,
                null,
                null,
                null,
                null,
                BigDecimal.ONE,
                null,
                false,
                false,
                true, // temp to true
                false,
                false,
                BigDecimal.ZERO,
                DiscountType.PERCENT,
                false,
                null,
                null,
                null,
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                0,
                null,
                0,
                false,
                false,
                null,
                true,
                null,
                null,
                ItemRefType.Simple,
                null,
                false,
                false);
        this.modifiersCount = 0;
        this.addonsCount = 0;
        this.optionalCount = 0;
        this.departmentGuid = null;

        this.tax = null;
        this.tax2 = null;
    }

    public ItemExModel(PrepaidSendResult result) {
        super(UUID.randomUUID().toString(),
                null,
                result.itemName,
                null,
                null,
                result.transactionId,
                PriceType.FIXED,
                result.itemPrice,
                null,
                null,
                null,
                null,
                null,
                result.itemQty,
                null,
                false,
                false,
                true,
                false,
                false,
                BigDecimal.ZERO,
                DiscountType.PERCENT,
                result.itemTaxable,
                null,
                null,
                null,
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                0,
                null,
                0,
                false,
                false,
                null,
                true, null, null, ItemRefType.Simple, null, false,
                false);
        this.departmentGuid = null;
        if (result.taxAmount != null && result.itemTaxable)
            this.tax = result.taxAmount;
        else
            this.tax = null;
        this.tax2 = null;
    }

    public ItemExModel(String guid,
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
                       String shortCut,
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
                       int modifiersCount,
                       int addonsCount,
                       int optionalCount,
                       int unitCount,
                       int availableUnitCount,
                       int composersCount,
                       int restrictComposersCount,
                       String departmentGuid,
                       BigDecimal tax,
                       BigDecimal tax2,
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
                       ItemRefType itemRefType,
                       BigDecimal loyaltyPoints,
                       boolean excludeFromLoyaltyPlan,
                       boolean isEbtEligible) {
        super(guid,
                categoryId,
                description,
                code,
                eanCode,
                productCode,
                priceType,
                price,
                price1,
                price2,
                price3,
                price4,
                price5,
                availableQty,
                unitsLabelId,
                isStockTracking,
                limitQty,
                isActiveStatus,
                isDiscountable,
                isSalable,
                discount,
                discountType,
                isTaxable,
                cost,
                minimumQty,
                recommendedQty,
                updateQtyFlag,
                taxGroupGuid,
                taxGroupGuid2,
                defaultModifierGuid,
                orderNum,
                printerAliasGuid,
                btnView,
                hasNotes,
                serializable,
                codeType,
                commissionEligible,
                commission, referenceItemGuid, itemRefType, loyaltyPoints, excludeFromLoyaltyPlan,
                isEbtEligible);
        this.modifiersCount = modifiersCount;
        this.addonsCount = addonsCount;
        this.optionalCount = optionalCount;
        this.unitCount = unitCount;
        this.composersCount = composersCount;
        this.restrictComposersCount = restrictComposersCount;
        this.departmentGuid = departmentGuid;
        this.tax = tax;
        this.tax2 = tax2;
        this.shortCut = shortCut;
    }

    public ItemExModel setMatrixGuid(String matrixGuid) {
        this.matrixGuid = matrixGuid;
        return this;
    }

    public ItemExModel setIsAComposisiton(boolean isAComposisiton) {
        this.isAComposisiton = isAComposisiton;
        return this;
    }

    public ItemExModel setIsAComposer(boolean isAComposer) {
        this.isAComposer = isAComposer;
        return this;
    }

    public boolean hasModificators() {
        return modifiersCount > 0 || addonsCount > 0 || optionalCount > 0;
    }

    public String unitGuid() {
        return tmpUnit.size() == 0 ? null : tmpUnit.get(0).guid;
    }

    public boolean isSerializable() {
        return serializable && codeType != null;
    }

    public void setCurrentPriceLevel(List<Integer> currentPriceLevels){
        if (currentPriceLevels == null){
            currentPriceLevel = 0;
            return;
        }

        for (Integer level : currentPriceLevels){
            BigDecimal price = getPrice(level);
            if (price != null){
                currentPriceLevel = level;
                return;
            }
        }
        currentPriceLevel = 0;
    }

    public BigDecimal getCurrentPrice(){
        return getPrice(currentPriceLevel);
    }

    public int getMaxMatrixCount(Context context) {
        Cursor variantCursor = ProviderAction.query(URI_VARIANT_ITEMS)
                .where(VariantItemTable.ITEM_GUID + "= ? ", guid)
                .perform(context);
        ArrayList<Integer> variants = new ArrayList<>();
        if (variantCursor != null && variantCursor.moveToFirst()) {
            do {
                String currentVariantGuid = variantCursor.getString(
                        variantCursor.getColumnIndex(VariantItemTable.GUID));
                Cursor subVariantCursor = ProviderAction.query(URI_VARIANT_SUB_ITEMS)
                        .where(VariantSubItemTable.VARIANT_ITEM_GUID + "= ? ", currentVariantGuid)
                        .perform(context);
                if (subVariantCursor != null) {
                    int currentSubVarCount = subVariantCursor.getCount();
                    if (currentSubVarCount != 0) {
                        variants.add(currentSubVarCount);
                    }
                    subVariantCursor.close();
                }
            } while (variantCursor.moveToNext());
            variantCursor.close();
        }
        int variantsCount = 0;
        for (int i = 0; i < variants.size(); i++) {
            if (i == 0) {
                variantsCount = variants.get(i);
            } else {
                variantsCount *= variants.get(i);
            }
        }
        return variantsCount;
    }

    public static ItemExModel loadSync(Context context, String itemGuid){
        Cursor c = ProviderAction.query(ItemExFunction.VIEW_URI)
                .projection(ItemExFunction.PROJECTION)
                .where(ItemTable.GUID + " = ?", itemGuid)
                .perform(context);

        ItemExModel result = null;
        if (c.moveToFirst()){
            result = new ItemExFunction().apply(c);
        }
        c.close();

        return result;
    }
}
