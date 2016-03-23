package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.ModifierExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ModifierTable;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ModifierTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.ModifierView;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * Created by vkompaniets on 6/26/2015.
 */
public class CollectModifiersCommand extends PublicGroundyTask {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ItemTable.URI_CONTENT);
    private static final Uri MODIFIER_URI = ShopProvider.contentUri(ModifierView.URI_CONTENT);
    private static final Uri SALE_MODIFIER_URI = ShopProvider.contentUri(SaleAddonTable.URI_CONTENT);

    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";
    private static final String ARG_SALE_ITEM_GUID = "ARG_SALE_ITEM_GUID";
    private static final String ARG_ITEM_MODEL = "ARG_ITEM_MODEL";
    private static final String ARG_PRICE = "ARG_PRICE";
    private static final String ARG_QUANTITY = "ARG_QUANTITY";
    private static final String ARG_UNIT = "ARG_UNIT";
    private static final String ARG_AUTO_APPLY_CHECK = "ARG_AUTO_APPLY_CHECK";

    private static final String PARAM_MODIFIERS = "PARAM_MODIFIERS";
    private static final String PARAM_ITEM_MODEL = "PARAM_ITEM_MODEL";
    private static final String PARAM_PRICE = "PARAM_PRICE";
    private static final String PARAM_QUANTITY = "PARAM_QUANTITY";
    private static final String PARAM_UNIT = "PARAM_UNIT";
    private static final String PARAM_HAS_AUTO_APPLY = "PARAM_HAS_AUTO_APPLY";

    @Override
    protected TaskResult doInBackground() {
        String itemGuid = getStringArg(ARG_ITEM_GUID);
        String saleItemGuid = getStringArg(ARG_SALE_ITEM_GUID);

        ItemExModel model = (ItemExModel) getArgs().getSerializable(ARG_ITEM_MODEL);
        BigDecimal price = (BigDecimal) getArgs().getSerializable(ARG_PRICE);
        BigDecimal quantity = (BigDecimal) getArgs().getSerializable(ARG_PRICE);
        Unit unit = (Unit) getArgs().getSerializable(ARG_UNIT);
        boolean isAutoApplycheck = getArgs().getBoolean(ARG_AUTO_APPLY_CHECK);

        Cursor c = ProviderAction.query(MODIFIER_URI)
                .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                .orderBy(ModifierTable.TITLE)
                .perform(getContext());

        if (c.getCount() == 0) {
            return succeeded().add(PARAM_ITEM_MODEL, model).add(PARAM_PRICE, price).add(PARAM_QUANTITY, quantity).add(PARAM_UNIT, unit);
        }

        ArrayList<ModifierExModel> modifierModels = new ArrayList<>();
        ModifierExFunction function = new ModifierExFunction();
        while (c.moveToNext()) {
            modifierModels.add(function.apply(c));
        }

        ArrayList<SelectedModifierExModel> wrappedModifiers = new ArrayList<>(modifierModels.size());
        ArrayList<SelectedModifierExModel> wrappedAutoApplyModifiers = new ArrayList<>(modifierModels.size());
        boolean hasAutoApply = false;
        if (TextUtils.isEmpty(saleItemGuid)) {
            c = ProviderAction.query(ITEM_URI)
                    .projection(ItemTable.DEFAULT_MODIFIER_GUID)
                    .where(ItemTable.GUID + " = ?", itemGuid)
                    .perform(getContext());

            ItemModel item = null;
            if (c.moveToFirst()) {
                item = new ItemModel(itemGuid);
                item.defaultModifierGuid = c.getString(0);
            }


                for (ModifierExModel modifier : modifierModels) {
                    if (modifier.autoApply) {
                        wrappedAutoApplyModifiers.add(new SelectedModifierExModel(modifier, modifier.isDefaultWithinGroupOrItem(item)));
                        hasAutoApply = true;
                    }
                }

                for (ModifierExModel modifier : modifierModels) {
                    wrappedModifiers.add(new SelectedModifierExModel(modifier, modifier.isDefaultWithinGroupOrItem(item)));
                }

        } else {
            c = ProviderAction.query(SALE_MODIFIER_URI)
                    .projection(SaleAddonTable.ADDON_GUID)
                    .where(SaleAddonTable.ITEM_GUID + " = ?", saleItemGuid)
                    .perform(getContext());

            HashSet<String> selectedGuids = new HashSet<>();
            while (c.moveToNext()) {
                selectedGuids.add(c.getString(0));
            }

            for (ModifierExModel modifier : modifierModels) {
                wrappedModifiers.add(new SelectedModifierExModel(modifier, selectedGuids.contains(modifier.getGuid())));
            }
        }
        c.close();
        if (hasAutoApply)
            return succeeded().add(PARAM_MODIFIERS, wrappedAutoApplyModifiers).add(PARAM_ITEM_MODEL, model).add(PARAM_PRICE, price).add(PARAM_QUANTITY, quantity).add(PARAM_UNIT, unit).add(PARAM_HAS_AUTO_APPLY, hasAutoApply);
        else
            return succeeded().add(PARAM_MODIFIERS, wrappedModifiers).add(PARAM_ITEM_MODEL, model).add(PARAM_PRICE, price).add(PARAM_QUANTITY, quantity).add(PARAM_UNIT, unit).add(PARAM_HAS_AUTO_APPLY, hasAutoApply);

    }

    public static class SelectedModifierExModel extends ModifierExModel {
        public boolean isSelected;

        public SelectedModifierExModel(ModifierExModel model, boolean isSelected) {
            super(model.getGuid(), model.itemGuid, model.type, model.title, model.cost, model.childItemGuid, model.childItemQty, model.modifierGroupGuid, model.getGroup(), model.getItem(), model.autoApply);
            this.isSelected = isSelected;
        }
    }

    public static void start(Context context, String itemGuid, String saleItemGuid, BigDecimal price, ItemExModel model, BigDecimal quantity, Unit unit, boolean isAutoApplyCheck, BaseCollectModifiersCallback callback) {
        create(CollectModifiersCommand.class).arg(ARG_ITEM_GUID, itemGuid).arg(ARG_PRICE, price).arg(ARG_ITEM_MODEL, model).arg(ARG_QUANTITY, quantity).arg(ARG_UNIT, unit).arg(ARG_AUTO_APPLY_CHECK, isAutoApplyCheck).arg(ARG_SALE_ITEM_GUID, saleItemGuid).callback(callback).queueUsing(context);
    }

    public static void start(Context context, String itemGuid, String saleItemGuid, BaseCollectModifiersCallback callback) {
        create(CollectModifiersCommand.class).arg(ARG_ITEM_GUID, itemGuid).arg(ARG_SALE_ITEM_GUID, saleItemGuid).callback(callback).queueUsing(context);
    }

    public static abstract class BaseCollectModifiersCallback {

        @OnSuccess(CollectModifiersCommand.class)
        public void onSuccess(@Param(PARAM_MODIFIERS) ArrayList<SelectedModifierExModel> modifiers, @Param(PARAM_ITEM_MODEL) ItemExModel model, @Param(PARAM_PRICE) BigDecimal price, @Param(PARAM_QUANTITY) BigDecimal quantity, @Param(PARAM_UNIT) Unit unit, @Param(PARAM_HAS_AUTO_APPLY) boolean hasAutoApply) {
            onCollected(modifiers, model, price, quantity, unit,hasAutoApply);
        }

        public abstract void onCollected(ArrayList<SelectedModifierExModel> modifiers, ItemExModel model, BigDecimal price, BigDecimal quantity, Unit unit, boolean hasAutoApply);
    }
}
