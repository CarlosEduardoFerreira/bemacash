package com.kaching123.tcr.model;

/*
 * Created by gdubina on 19/11/13.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ItemExModel extends ItemModel {

    private static final Uri URI_VARIANT_ITEMS = ShopProvider.contentUri(VariantItemTable.URI_CONTENT);

    private static final Uri URI_VARIANT_SUB_ITEMS = ShopProvider.contentUri(VariantSubItemTable.URI_CONTENT);

    public ArrayList<Unit> tmpUnit = new ArrayList<Unit>();

    public String tmpBarcode;

    public final int modifiersCount;
    public final int addonsCount;
    public final int optionalCount;

    public String departmentGuid;
    public BigDecimal tax;

    public String matrixGuid;

    public String shortCut;

    public boolean isAComposisiton;
    public boolean isAComposer;

    public ItemExModel() {
        super();
        this.modifiersCount = 0;
        this.addonsCount = 0;
        this.optionalCount = 0;
    }

    public ItemExModel(String guid,
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
                       String shortCut,
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
                       int modifiersCount,
                       int addonsCount,
                       int optionalCount,
                       String departmentGuid,
                       BigDecimal tax,
                       String defaultModifierGuid,
                       int orderNum,
                       String printerAliasGuid,
                       int btnView,
                       boolean hasNotes,
                       boolean serializable,
                       Unit.CodeType codeType,
                       boolean commissionEligible,
                       BigDecimal commission,
                       String referenceItemGuid,
                       ItemRefType itemRefType, boolean isPrepaidItem) {
        super(guid,
                categoryId,
                description,
                code,
                eanCode,
                productCode,
                priceType,
                price,
                availableQty,
                unitsLabel,
                unitsLabelId,
                isStockTracking,
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
                commission,
                referenceItemGuid,
                itemRefType,
                isPrepaidItem);
        this.modifiersCount = modifiersCount;
        this.addonsCount = addonsCount;
        this.optionalCount = optionalCount;
        this.departmentGuid = departmentGuid;
        this.tax = tax;
        if (TextUtils.isEmpty(unitsLabelId)) {
            this.shortCut = unitsLabel;
        } else {
            this.shortCut = shortCut;
        }
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
}