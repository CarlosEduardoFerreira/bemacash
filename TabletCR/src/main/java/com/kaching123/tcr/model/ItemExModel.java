package com.kaching123.tcr.model;

/*
 * Created by gdubina on 19/11/13.
 */

import java.math.BigDecimal;
import java.util.ArrayList;

public class ItemExModel extends ItemModel {

    public ArrayList<Unit> tmpUnit = new ArrayList<Unit>();

    public String tmpBarcode;

    public final int modifiersCount;
    public final int addonsCount;
    public final int optionalCount;

    public String departmentGuid;
    public BigDecimal tax;

    public String shortCut;

    public ItemExModel(){
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
                       BigDecimal commission) {
        super(guid, categoryId, description, code, eanCode, productCode, priceType, price, availableQty, unitsLabel,
                isStockTracking, isActiveStatus,
                isDiscountable,
                isSalable,
                discount, discountType, isTaxable, cost, minimumQty, recommendedQty, updateQtyFlag, taxGroupGuid, defaultModifierGuid,
                orderNum, printerAliasGuid, btnView, hasNotes, serializable, codeType, commissionEligible, commission);
        this.modifiersCount = modifiersCount;
        this.addonsCount = addonsCount;
        this.optionalCount = optionalCount;
        this.departmentGuid = departmentGuid;
        this.tax = tax;
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
}