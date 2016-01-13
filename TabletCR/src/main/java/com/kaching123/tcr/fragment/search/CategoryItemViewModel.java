package com.kaching123.tcr.fragment.search;

import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.Unit;

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
                                 int modifiersCount,
                                 int addonsCount,
                                 int optionalCount,
                                 String departmentGuid,
                                 String categoryName,
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
                                 ItemRefType itemRefType) {
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
                modifiersCount,
                addonsCount,
                optionalCount,
                departmentGuid,
                tax,
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
                itemRefType);
        this.categoryName = categoryName;
    }

}
