package com.kaching123.tcr.model.converter;

import android.database.Cursor;
import android.net.Uri;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ChildComposerTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.HostComposerTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemMatrixTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ModifierTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.TaxGroupTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.UnitLabelTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.UnitTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemExtView;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._caseCount;
import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._count;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._itemRefType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;
import static com.kaching123.tcr.model.ContentValuesUtil._sum;

/**
 * Created by gdubina on 22/11/13.
 */
public class ItemExFunction extends ListConverterFunction<ItemExModel> {

    public static Uri VIEW_URI = ShopProvider.contentUriGroupBy(ShopStore.ItemExtView.URI_CONTENT, ItemTable.GUID);

    public static String[] PROJECTION = new String[]{
            ItemTable.GUID,
            ItemTable.CATEGORY_ID,
            ItemTable.DESCRIPTION,
            ItemTable.CODE,
            ItemTable.EAN_CODE,
            ItemTable.PRODUCT_CODE,
            ItemTable.PRICE_TYPE,
            ItemTable.SALE_PRICE,
            ItemTable.TMP_AVAILABLE_QTY,
            ItemTable.UNITS_LABEL,
            ItemTable.UNIT_LABEL_ID,
            UnitLabelTable.SHORTCUT,
            ItemTable.STOCK_TRACKING,
            ItemTable.ACTIVE_STATUS,
            ItemTable.DISCOUNTABLE,
            ItemTable.SALABLE,
            ItemTable.DISCOUNT,
            ItemTable.DISCOUNT_TYPE,
            ItemTable.TAXABLE,
            ItemTable.COST,
            ItemTable.MINIMUM_QTY,
            ItemTable.RECOMMENDED_QTY,
            ItemTable.UPDATE_QTY_FLAG,
            ItemTable.TAX_GROUP_GUID,
            ItemTable.TAX_GROUP_GUID2,
            ItemTable.IS_DELETED,
            ItemTable.DEFAULT_MODIFIER_GUID,
            ItemTable.LOYALTY_POINTS,
            ItemTable.EXCLUDE_FROM_LOYALTY_PLAN,
            CategoryTable.DEPARTMENT_GUID,
            _caseCount(ModifierTable.TYPE, String.valueOf(ModifierType.MODIFIER.ordinal()), ItemExtView.MODIFIERS_COUNT),
            _caseCount(ModifierTable.TYPE, String.valueOf(ModifierType.ADDON.ordinal()), ItemExtView.ADDONS_COUNT),
            _caseCount(ModifierTable.TYPE, String.valueOf(ModifierType.OPTIONAL.ordinal()), ItemExtView.OPTIONAL_COUNT),
            _count(UnitTable.ID, ItemExtView.UNITS_COUNT),
            _caseCount(UnitTable.STATUS,
                    new String[]{String.valueOf(Status.NEW.ordinal()), String.valueOf(Status.USED.ordinal()), String.valueOf(Status.BROKEN.ordinal())},
                    ItemExtView.AVAILABLE_UNITS_COUNT),
            _count(ChildComposerTable.ID, ItemExtView.COMPOSERS_COUNT),
            _sum(ChildComposerTable.FREE_OF_CHARGE_COMPOSER, ItemExtView.RESTRICT_COMPOSERS_COUNT),
            TaxGroupTable.TAX,
            ShopSchema2.ItemExtView2.TaxGroupTable2.TAX,
            ItemTable.ORDER_NUM,
            ItemTable.PRINTER_ALIAS_GUID,
            ItemTable.BUTTON_VIEW,
            ItemTable.HAS_NOTES,
            ItemTable.SERIALIZABLE,
            ItemTable.CODE_TYPE,
            ItemTable.ELIGIBLE_FOR_COMMISSION,
            ItemTable.COMMISSION,
            ChildComposerTable.ID,
            HostComposerTable.ID,
            ItemTable.REFERENCE_ITEM_ID,
            ItemTable.ITEM_REF_TYPE,
            ItemMatrixTable.PARENT_GUID
    };

    @Override
    public ItemExModel apply(Cursor c) {
        super.apply(c);
        String shortCut;
        try {
            shortCut = c.getString(indexHolder.get(UnitLabelTable.SHORTCUT));
        } catch (IllegalArgumentException noItem) {
            shortCut = null;
        }
        String matrixGuid = c.getString(indexHolder.get(ItemMatrixTable.PARENT_GUID));
        return new ItemExModel(
                c.getString(indexHolder.get(ItemTable.GUID)),
                c.getString(indexHolder.get(ItemTable.CATEGORY_ID)),
                c.getString(indexHolder.get(ItemTable.DESCRIPTION)),
                c.getString(indexHolder.get(ItemTable.CODE)),
                c.getString(indexHolder.get(ItemTable.EAN_CODE)),
                c.getString(indexHolder.get(ItemTable.PRODUCT_CODE)),
                _priceType(c, indexHolder.get(ItemTable.PRICE_TYPE)),
                _decimal(c.getString(indexHolder.get(ItemTable.SALE_PRICE))),
                _decimalQty(c.getString(indexHolder.get(ItemTable.TMP_AVAILABLE_QTY))),
                c.getString(indexHolder.get(ItemTable.UNITS_LABEL)),
                c.getString(c.getColumnIndex(ItemTable.UNIT_LABEL_ID)),
                shortCut,
                c.getInt(indexHolder.get(ItemTable.STOCK_TRACKING)) == 1,
                c.getInt(indexHolder.get(ItemTable.ACTIVE_STATUS)) == 1,
                c.getInt(indexHolder.get(ItemTable.DISCOUNTABLE)) == 1,
                c.getInt(indexHolder.get(ItemTable.SALABLE)) == 1,
                _decimal(c.getString(indexHolder.get(ItemTable.DISCOUNT))),
                _discountType(c, indexHolder.get(ItemTable.DISCOUNT_TYPE)),
                c.getInt(indexHolder.get(ItemTable.TAXABLE)) == 1,
                _decimal(c.getString(indexHolder.get(ItemTable.COST))),
                _decimalQty(c.getString(indexHolder.get(ItemTable.MINIMUM_QTY))),
                _decimalQty(c.getString(indexHolder.get(ItemTable.RECOMMENDED_QTY))),
                c.getString(indexHolder.get(ItemTable.UPDATE_QTY_FLAG)),
                c.getString(indexHolder.get(ItemTable.TAX_GROUP_GUID)),
                c.getString(indexHolder.get(ItemTable.TAX_GROUP_GUID2)),
                c.getInt(indexHolder.get(ItemExtView.MODIFIERS_COUNT)),
                c.getInt(indexHolder.get(ItemExtView.ADDONS_COUNT)),
                c.getInt(indexHolder.get(ItemExtView.OPTIONAL_COUNT)),
                c.getInt(indexHolder.get(ItemExtView.UNITS_COUNT)),
                c.getInt(indexHolder.get(ItemExtView.AVAILABLE_UNITS_COUNT)),
                c.getInt(indexHolder.get(ItemExtView.COMPOSERS_COUNT)),
                c.getInt(indexHolder.get(ItemExtView.RESTRICT_COMPOSERS_COUNT)),
                c.getString(indexHolder.get(CategoryTable.DEPARTMENT_GUID)),
                _decimal(c.getString(indexHolder.get(TaxGroupTable.TAX))),
                _decimal(c.getString(indexHolder.get(ShopSchema2.ItemExtView2.TaxGroupTable2.TAX))),
                c.getString(c.getColumnIndex(ItemTable.DEFAULT_MODIFIER_GUID)),
                c.getInt(indexHolder.get(ItemTable.ORDER_NUM)),
                c.getString(indexHolder.get(ItemTable.PRINTER_ALIAS_GUID)),
                c.getInt(indexHolder.get(ItemTable.BUTTON_VIEW)),
                c.getInt(indexHolder.get(ItemTable.HAS_NOTES)) == 1,
                c.getInt(indexHolder.get(ItemTable.SERIALIZABLE)) == 1,
                _codeType(c, indexHolder.get(ItemTable.CODE_TYPE)),
                _bool(c, indexHolder.get(ItemTable.ELIGIBLE_FOR_COMMISSION)),
                _decimal(c, indexHolder.get(ItemTable.COMMISSION)), c.getString(indexHolder.get(ItemTable.REFERENCE_ITEM_ID)), _itemRefType(c, indexHolder.get(ItemTable.ITEM_REF_TYPE)), _decimal(c, indexHolder.get(ItemTable.LOYALTY_POINTS)), _bool(c, indexHolder.get(ItemTable.EXCLUDE_FROM_LOYALTY_PLAN)))
                .setIsAComposer(c.getString(indexHolder.get(ShopSchema2.ItemExtView2.HostComposerTable.ID)) != null)
                .setIsAComposisiton(c.getString(indexHolder.get(ShopSchema2.ItemExtView2.ChildComposerTable.ID)) != null)
                .setMatrixGuid(matrixGuid);
    }

    public static class Wrap implements Function<Cursor, Optional<ItemExModel>> {
        @Override
        public Optional<ItemExModel> apply(Cursor c) {
            if (!c.moveToFirst()) {
                return Optional.absent();
            }
            ItemExModel item = new ItemExFunction().apply(c);
            return Optional.of(item);
        }
    }
}