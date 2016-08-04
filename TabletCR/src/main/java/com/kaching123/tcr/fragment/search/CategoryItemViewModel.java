package com.kaching123.tcr.fragment.search;

import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.Unit.CodeType;

import java.math.BigDecimal;

/**
 * Created by gdubina on 12/11/13.
 */
public class CategoryItemViewModel extends ItemExModel {

    public final String categoryName;

    public CategoryItemViewModel(String guid,
                                 String categoryId,
                                 String description,
                                 String code,
                                 String eanCode, String productCode,
                                 PriceType priceType,
                                 BigDecimal price,
                                 BigDecimal qty,
                                 String unitsLabel,
                                 String unitsLabelId,
                                 boolean isStockTracking,
                                 boolean isActiveStatus,
                                 boolean isDiscountable,
                                 boolean isSalable,
                                 BigDecimal discount,
                                 DiscountType discountType,
                                 boolean isTaxable,
                                 String taxGroupGuid,
                                 String taxGroupGuid2,
                                 int modifiersCount,
                                 int addonsCount,
                                 int optionalCount,
                                 int unitCount,
                                 String departmentGuid,
                                 String categoryName,
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
                                 boolean excludeFromLoyaltyPlan) {
        super(guid,
                categoryId,
                description,
                code,
                eanCode,
                productCode,
                priceType,
                price,
                qty,
                unitsLabel,
                unitsLabelId,
                null,
                isStockTracking, isActiveStatus,
                isDiscountable,
                isSalable,
                discount,
                discountType,
                isTaxable,
                null,
                null,
                null,
                null,
                taxGroupGuid,
                taxGroupGuid2,
                modifiersCount,
                addonsCount,
                optionalCount,
                unitCount,
                departmentGuid,
                tax,
                tax2,
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
                loyaltyPoints, excludeFromLoyaltyPlan);
        this.categoryName = categoryName;
    }

}
