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
                                 String eanCode,
                                 String productCode,
                                 PriceType priceType,
                                 BigDecimal price,
                                 BigDecimal price1,
                                 BigDecimal price2,
                                 BigDecimal price3,
                                 BigDecimal price4,
                                 BigDecimal price5,
                                 BigDecimal qty,
                                 String unitsLabelId,
                                 String shortcut,
                                 boolean isStockTracking,
                                 boolean limitQty,
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
                                 String departmentGuid,
                                 String categoryName,
                                 BigDecimal tax,
                                 BigDecimal tax2,
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
                                 boolean isEbtEligible,
                                 int ageVerification) {
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
                qty,
                unitsLabelId,
                shortcut,
                isStockTracking,
                limitQty,
                isActiveStatus,
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
                departmentGuid,
                tax,
                tax2,
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
                loyaltyPoints,
                excludeFromLoyaltyPlan,
                isEbtEligible,
                ageVerification);
        this.categoryName = categoryName;
    }

}
