package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by mboychenko on 05.09.2016.
 */
public class ItemsNegativeStockTrackingCommand extends AsyncCommand {

    private static final Uri URI_ORDER_ITEMS = ShopProvider.contentUri(ShopStore.SaleOrderItemsView.URI_CONTENT);
    private static final Uri URI_ADDONS = ShopProvider.contentUri(ShopStore.ModifierTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    private static final String ORDER_BY = ShopSchema2.SaleOrderItemsView2.SaleItemTable.SEQUENCE + ", " + ShopSchema2.SaleOrderItemsView2.SaleAddonTable.TYPE + ", " + ShopSchema2.SaleOrderItemsView2.ModifierTable.TITLE;

    public static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    public static final String ARG_ITEM_ADDONS = "ARG_ITEM_ADDONS";
    public static final String ARG_ITEM_MODS = "ARG_ITEM_MODS";
    public static final String ARG_ITEM_REMOVE_ADDONS = "ARG_ITEM_REMOVE_ADDONS";
    public static final String ARG_ITEM_NOOPT = "ARG_ITEM_NOOPT";
    public static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";
    public static final String ARG_ITEM_NEW_QTY = "ARG_ITEM_NEW_QTY";
    public static final String ARG_ITEM_OLD_QTY = "ARG_ITEM_OLD_QTY";

    public static final String ARG_ITEM_TYPE = "ARG_ITEM_TYPE";
    public static final String COMMAND_RESULT_KEY = "COMMAND_RESULT_KEY";

    private List<Pair<String, BigDecimal>> modifiers;
    private List<Pair<String, BigDecimal>> optionals;
    private HashMap<String, BigDecimal> uniqMods;
    private HashMap<String, BigDecimal> currentOrderItemQty;
    private HashMap<String, BigDecimal> tmpOrderItemQty;
    private ArrayList<TrackedItemInfo> trackableMods;
    private String itemGuid;

    @Override
    protected TaskResult doCommand() {
        Logger.d("ItemsNegativeStockTrackingCommand doCommand");
        currentOrderItemQty = getApp().getOrderItemsQty();
        tmpOrderItemQty = new HashMap<>();
        modifiers = new ArrayList<>();
        uniqMods = new HashMap<>();
        trackableMods = new ArrayList<>();

        ItemType itemType = (ItemType) getArgs().get(ARG_ITEM_TYPE);
        if (itemType != null)
            switch (itemType) {
                case HOLD_ON:
                    return succeeded().add(COMMAND_RESULT_KEY, holdOn());
                case MODIFIER:
                    return succeeded().add(COMMAND_RESULT_KEY, addModifier());
                case CHANGE_QTY:
                    return succeeded().add(COMMAND_RESULT_KEY, changeQty());
                case COMPOSITION:
                    return succeeded().add(COMMAND_RESULT_KEY, composition());
                case REMOVE:
                    removeItems();
                    break;
            }

        return succeeded().add(COMMAND_RESULT_KEY, true);
    }

    private void removeItems() {
        ArrayList<SaleOrderItemViewModel.AddonInfo> addonsForRemove = (ArrayList<SaleOrderItemViewModel.AddonInfo>) getArgs().get(ARG_ITEM_REMOVE_ADDONS);
        BigDecimal oldQty = (BigDecimal) getArgs().get(ARG_ITEM_OLD_QTY);
        oldQty = oldQty == null ? BigDecimal.ZERO : oldQty;
        String itemGuid = getArgs().getString(ARG_ITEM_GUID);

        HashMap<String, BigDecimal> composers = ComposerModel.getChildsGuidQtyByHostId(getContext(), itemGuid);                                        //kill composed items
        if (composers != null && !composers.isEmpty()) {
            for (Map.Entry<String, BigDecimal> composer : composers.entrySet()) {
                BigDecimal subValue = currentOrderItemQty.get(composer.getKey()).subtract(oldQty.multiply(composer.getValue()));
                if (subValue.compareTo(BigDecimal.ZERO) == 0) {
                    currentOrderItemQty.remove(composer.getKey());
                } else {
                    currentOrderItemQty.put(composer.getKey(), subValue);
                }
            }
        }

        if (addonsForRemove != null && !addonsForRemove.isEmpty() && oldQty != null) {                                                          //kill modifiers
            for (SaleOrderItemViewModel.AddonInfo addon : addonsForRemove) {
                ItemModel item = ItemModel.getById(getContext(), addon.addon.childItemGuid, true);
                if (item.isLimitQtySelected()) {
                    BigDecimal subValue = currentOrderItemQty.get(addon.addon.childItemGuid).subtract(oldQty.multiply(addon.addon.childItemQty));
                    if (subValue.compareTo(BigDecimal.ZERO) == 0) {
                        currentOrderItemQty.remove(addon.addon.childItemGuid);
                    } else {
                        currentOrderItemQty.put(addon.addon.childItemGuid, subValue);
                    }
                }
            }
        } else {                                                                                                                                 //kill serializable modifiers
            getTrackedUniqMods();
            ItemModel parentItem = ItemModel.getById(getContext(), itemGuid, true);
            if (itemGuid != null) {
                BigDecimal subValue = currentOrderItemQty.get(parentItem.getGuid()).subtract(BigDecimal.ONE);
                if (subValue.compareTo(BigDecimal.ZERO) == 0) {
                    currentOrderItemQty.remove(parentItem.getGuid());
                } else {
                    currentOrderItemQty.put(parentItem.getGuid(), subValue);
                }
            }

            for (Map.Entry<String, BigDecimal> pair : uniqMods.entrySet()) {
                BigDecimal subValue = currentOrderItemQty.get(pair.getKey()).subtract(pair.getValue());
                if (subValue.compareTo(BigDecimal.ZERO) == 0) {
                    currentOrderItemQty.remove(pair.getKey());
                } else {
                    currentOrderItemQty.put(pair.getKey(), subValue);
                }
            }
        }
    }

    private boolean changeQty() {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        String itemGuid = getStringArg(ARG_ITEM_GUID);
        BigDecimal newQty = (BigDecimal) getArgs().get(ARG_ITEM_NEW_QTY);
        BigDecimal oldQty = (BigDecimal) getArgs().get(ARG_ITEM_OLD_QTY);
        SaleOrderModel saleOrderModel = SaleOrderModel.getById(getContext(), orderGuid);
        ArrayList<SaleOrderItemViewModel.AddonInfo> addonsForChange = (ArrayList<SaleOrderItemViewModel.AddonInfo>) getArgs().get(ARG_ITEM_REMOVE_ADDONS);

        tmpOrderItemQty.putAll(currentOrderItemQty);

        if (itemGuid != null && newQty != null && oldQty != null) {
            ItemModel model = ItemModel.getById(getContext(), itemGuid, true);
            if (model != null) {
                if (model.isLimitQtySelected()) {

                    if (saleOrderModel != null && saleOrderModel.orderStatus == OrderStatus.HOLDON) {
                        BigDecimal historyAddedQty = BigDecimal.ZERO;
                        Cursor c = ProviderAction.query(ITEM_MOVEMENT_URI)
                                .projection(ShopStore.ItemMovementTable.QTY)
                                .where(ShopStore.ItemMovementTable.ORDER_GUID + " = ?", orderGuid)
                                .where(ShopStore.ItemMovementTable.ITEM_GUID + " = ?", model.guid)
                                .perform(getContext());

                        if (c != null && c.moveToFirst()) {
                            do {
                                historyAddedQty = historyAddedQty.add(new BigDecimal(c.getDouble(c.getColumnIndex(ShopStore.ItemMovementTable.QTY))).abs());
                            }while (c.moveToNext());
                            c.close();
                        }
                        model.availableQty =  model.availableQty.add(historyAddedQty);
                    }

                    BigDecimal newValue = newQty;
                    if (currentOrderItemQty.containsKey(model.guid)) {
                        newValue = (currentOrderItemQty.get(model.guid).subtract(oldQty)).add(newQty);
                    }

                    if (model.availableQty.subtract(newValue).compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                    }
                    tmpOrderItemQty.put(model.getGuid(), newValue);
                }

                if (!compositionChangeQty(model.getGuid(), oldQty, newQty))
                    return false;
            }

            if (addonsForChange != null && !addonsForChange.isEmpty()) {
                for (SaleOrderItemViewModel.AddonInfo addon : addonsForChange) {
                    if (addon.addon.childItemGuid == null)
                        continue;
                    if (uniqMods.containsKey(addon.addon.childItemGuid)) {
                        uniqMods.put(addon.addon.childItemGuid, uniqMods.get(addon.addon.childItemGuid).add(addon.addon.childItemQty));
                    } else {
                        uniqMods.put(addon.addon.childItemGuid, addon.addon.childItemQty);
                    }
                }

                for (Map.Entry<String, BigDecimal> uniqItems : uniqMods.entrySet()) {
                    ItemModel item = ItemModel.getById(getContext(), uniqItems.getKey(), true);
                    if (item != null) {
                        if (item.isLimitQtySelected()) {
                            trackableMods.add(new TrackedItemInfo(item.getGuid(), uniqItems.getValue(), item.availableQty));
                        }
                        if (!compositionChangeQty(item.getGuid(), oldQty, newQty))
                            return false;
                    }
                }

                for (TrackedItemInfo trackedItems : trackableMods) {
                    HashMap<String, BigDecimal> tmp = new HashMap<>(1);

                    if (!tmpOrderItemQty.isEmpty()) {
                        if (tmpOrderItemQty.containsKey(trackedItems.getItemGuid())) {
                            tmp.put(trackedItems.getItemGuid(), (tmpOrderItemQty.get(trackedItems.getItemGuid()).subtract(trackedItems.getQty4Sale().multiply(oldQty))).add(trackedItems.getQty4Sale().multiply(newQty)));
                        } else {
                            tmp.put(trackedItems.getItemGuid(), trackedItems.getQty4Sale());
                        }

                        if ((trackedItems.getAvailableQty().subtract(tmp.get(trackedItems.getItemGuid()))).compareTo(BigDecimal.ZERO) < 0) {
                            return false;
                        }

                        tmpOrderItemQty.put(trackedItems.getItemGuid(), tmp.get(trackedItems.getItemGuid()));
                    } else {
                        BigDecimal freshQty = (trackedItems.getQty4Sale()).multiply(newQty);
                        if (trackedItems.getAvailableQty().subtract(freshQty).compareTo(BigDecimal.ZERO) < 0) {
                            return false;
                        }
                        tmpOrderItemQty.put(trackedItems.getItemGuid(), freshQty);
                    }
                }
            }
        }
        currentOrderItemQty.putAll(tmpOrderItemQty);
        return true;
    }

    private boolean addModifier() {
        tmpOrderItemQty.putAll(currentOrderItemQty);
        itemGuid = getStringArg(ARG_ITEM_GUID);
        ItemModel itemModel = ItemModel.getById(getContext(), itemGuid, true);

        if (itemModel != null && itemModel.isLimitQtySelected()) {
            if (!tmpOrderItemQty.isEmpty()) {
                BigDecimal count = tmpOrderItemQty.containsKey(itemModel.getGuid()) ? tmpOrderItemQty.get(itemModel.getGuid()) : BigDecimal.ZERO;
                if (itemModel.availableQty.subtract(count.add(BigDecimal.ONE)).compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
                tmpOrderItemQty.put(itemModel.getGuid(), count.add(BigDecimal.ONE));
            } else {
                if (itemModel.availableQty.subtract(BigDecimal.ONE).compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
                tmpOrderItemQty.put(itemModel.getGuid(), BigDecimal.ONE);
            }
        }

        getTrackedUniqMods();

        for (Map.Entry<String, BigDecimal> pair : uniqMods.entrySet()) {
            ItemModel item = ItemModel.getById(getContext(), pair.getKey(), true);
            if (item != null) {
                if (item.isLimitQtySelected()) {
                    trackableMods.add(new TrackedItemInfo(item.getGuid(), pair.getValue(), item.availableQty));
                }
                if (!compositionProcessing(item.getGuid()))
                    return false;
            }
        }

        for (TrackedItemInfo item : trackableMods) {
            if (!tmpOrderItemQty.isEmpty()) {
                BigDecimal count = tmpOrderItemQty.containsKey(item.getItemGuid()) ? tmpOrderItemQty.get(item.getItemGuid()) : BigDecimal.ZERO;
                if (item.getAvailableQty().subtract(item.getQty4Sale().add(count)).compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
                tmpOrderItemQty.put(item.getItemGuid(), count.add(item.getQty4Sale()));
            } else {
                if (item.getAvailableQty().subtract(item.getQty4Sale()).compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
                tmpOrderItemQty.put(item.getItemGuid(), item.getQty4Sale());
            }
        }
        currentOrderItemQty.putAll(tmpOrderItemQty);
        return true;
    }

    private boolean holdOn() {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        if (orderGuid != null) {
            List<SaleOrderItemViewModel> items = _wrap(ProviderAction.query(URI_ORDER_ITEMS)
                            .where(ShopSchema2.SaleOrderItemsView2.SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                            .orderBy(ORDER_BY)
                            .perform(getContext()),
                    new SaleOrderItemViewModelWrapFunction(getContext()));

            tmpOrderItemQty.putAll(currentOrderItemQty);

            for (SaleOrderItemViewModel itemViewModel : items) {
                ItemModel itemModel = ItemModel.getById(getContext(), itemViewModel.itemModel.itemGuid, true);

                if (itemModel != null && itemModel.isLimitQtySelected()) {
                    if (!tmpOrderItemQty.isEmpty()) {
                        BigDecimal count = tmpOrderItemQty.containsKey(itemModel.getGuid()) ? tmpOrderItemQty.get(itemModel.getGuid()) : BigDecimal.ZERO;
                        if (itemModel.availableQty.subtract(itemViewModel.getQty()).compareTo(BigDecimal.ZERO) < 0) {
                            return false;
                        }
                        tmpOrderItemQty.put(itemModel.getGuid(), count.add(itemViewModel.getQty()));
                    } else {
                        if (itemModel.availableQty.subtract(itemViewModel.getQty()).compareTo(BigDecimal.ZERO) < 0) {
                            return false;
                        }
                        tmpOrderItemQty.put(itemModel.getGuid(), itemViewModel.getQty());
                    }
                }

                if (itemViewModel.hasModifiers()) {
                    BigDecimal qty = itemViewModel.getQty();
                    for (SaleOrderItemViewModel.AddonInfo addon : itemViewModel.modifiers) {
                        if (addon.addon.childItemGuid == null)
                            continue;
                        if (uniqMods.containsKey(addon.addon.childItemGuid)) {
                            uniqMods.put(addon.addon.childItemGuid, uniqMods.get(addon.addon.childItemGuid).add(addon.addon.childItemQty));
                        } else {
                            uniqMods.put(addon.addon.childItemGuid, addon.addon.childItemQty);
                        }
                    }

                    for (Map.Entry<String, BigDecimal> uniqItems : uniqMods.entrySet()) {
                        ItemModel item = ItemModel.getById(getContext(), uniqItems.getKey(), true);
                        if (item != null && item.isLimitQtySelected()) {
                            trackableMods.add(new TrackedItemInfo(item.getGuid(), uniqItems.getValue(), item.availableQty));
                        }
                    }

                    for (TrackedItemInfo trackedItems : trackableMods) {
                        BigDecimal freshQty = (trackedItems.getQty4Sale()).multiply(qty);

                        if (!tmpOrderItemQty.isEmpty()) {
                            if (tmpOrderItemQty.containsKey(trackedItems.getItemGuid())) {
                                tmpOrderItemQty.put(trackedItems.getItemGuid(), tmpOrderItemQty.get(trackedItems.getItemGuid()).add(trackedItems.getQty4Sale().multiply(qty)));
                            } else {
                                tmpOrderItemQty.put(trackedItems.getItemGuid(), trackedItems.getQty4Sale().multiply(qty));
                            }

                            if ((trackedItems.getAvailableQty().subtract(tmpOrderItemQty.get(trackedItems.getItemGuid()))).compareTo(BigDecimal.ZERO) < 0) {
                                return false;
                            }

                        } else {
                            if (trackedItems.getAvailableQty().subtract(freshQty).compareTo(BigDecimal.ZERO) < 0) {
                                return false;
                            }
                            tmpOrderItemQty.put(trackedItems.getItemGuid(), freshQty);
                        }
                    }
                }
                if (itemModel != null && !compositionProcessing(itemModel.getGuid()))
                    return false;
            }
        }
        currentOrderItemQty.putAll(tmpOrderItemQty);
        return true;
    }

    private boolean composition() {
        tmpOrderItemQty.putAll(currentOrderItemQty);
        String itemGuid = getStringArg(ARG_ORDER_GUID);                                             //item guid, not order
        boolean result = compositionProcessing(itemGuid);
        if (result)
            currentOrderItemQty.putAll(tmpOrderItemQty);
        return result;
    }

    private boolean compositionProcessing(String itemGuid) {
        ArrayList<TrackedItemInfo> localTrackableMods = new ArrayList<>();

        HashMap<String, BigDecimal> composers = ComposerModel.getChildsGuidQtyByHostId(getContext(), itemGuid);

        if (composers != null && !composers.isEmpty()) {
            for (Map.Entry<String, BigDecimal> childItem : composers.entrySet()) {
                ItemModel child = ItemModel.getById(getContext(), childItem.getKey(), true);
                if (child != null && child.isLimitQtySelected()) {
                    localTrackableMods.add(new TrackedItemInfo(childItem.getKey(), childItem.getValue(), child.availableQty));
                }
            }

            for (TrackedItemInfo trackedItems : localTrackableMods) {
                BigDecimal freshQty = trackedItems.getQty4Sale();

                if (!tmpOrderItemQty.isEmpty()) {
                    if (tmpOrderItemQty.containsKey(trackedItems.getItemGuid())) {
                        tmpOrderItemQty.put(trackedItems.getItemGuid(), tmpOrderItemQty.get(trackedItems.getItemGuid()).add(trackedItems.getQty4Sale()));
                    } else {
                        tmpOrderItemQty.put(trackedItems.getItemGuid(), trackedItems.getQty4Sale());
                    }
                    if ((trackedItems.getAvailableQty().subtract(tmpOrderItemQty.get(trackedItems.getItemGuid()))).compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                    }
                } else {
                    if (trackedItems.getAvailableQty().subtract(freshQty).compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                    }
                    tmpOrderItemQty.put(trackedItems.getItemGuid(), freshQty);
                }
            }
        }
        return true;
    }

    private boolean compositionChangeQty(String itemGuid, BigDecimal oldParentQty, BigDecimal newParentQty) {
        ArrayList<TrackedItemInfo> localTrackableMods = new ArrayList<>();

        HashMap<String, BigDecimal> composers = ComposerModel.getChildsGuidQtyByHostId(getContext(), itemGuid);

        if (composers != null && !composers.isEmpty()) {
            for (Map.Entry<String, BigDecimal> childItem : composers.entrySet()) {
                ItemModel child = ItemModel.getById(getContext(), childItem.getKey(), true);
                if (child != null && child.isLimitQtySelected()) {
                    localTrackableMods.add(new TrackedItemInfo(childItem.getKey(), childItem.getValue(), child.availableQty));
                }
            }

            for (TrackedItemInfo trackedItems : localTrackableMods) {
                BigDecimal freshQty = (trackedItems.getQty4Sale()).multiply(newParentQty);

                if (!tmpOrderItemQty.isEmpty()) {
                    if (tmpOrderItemQty.containsKey(trackedItems.getItemGuid())) {
                        tmpOrderItemQty.put(trackedItems.getItemGuid(), (tmpOrderItemQty.get(trackedItems.getItemGuid()).subtract(trackedItems.getQty4Sale().multiply(oldParentQty))).add(freshQty));
                    } else {
                        tmpOrderItemQty.put(trackedItems.getItemGuid(), trackedItems.getQty4Sale());
                    }
                    if ((trackedItems.getAvailableQty().subtract(tmpOrderItemQty.get(trackedItems.getItemGuid()))).compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                    }
                } else {
                    if (trackedItems.getAvailableQty().subtract(freshQty).compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                    }
                    tmpOrderItemQty.put(trackedItems.getItemGuid(), freshQty);
                }
            }
        }
        return true;
    }

    private void getTrackedUniqMods() {
        List<Pair<String, BigDecimal>> tmp;

        itemGuid = getStringArg(ARG_ITEM_GUID);
        List<String> modifierGuids = getArgs().getStringArrayList(ARG_ITEM_MODS);
        List<String> addonsGuids = getArgs().getStringArrayList(ARG_ITEM_ADDONS);
        List<String> optionalsGuid = getArgs().getStringArrayList(ARG_ITEM_NOOPT);

        tmp = loadAddons(modifierGuids, ModifierType.MODIFIER);
        if (tmp != null)
            modifiers.addAll(tmp);

        tmp = loadAddons(addonsGuids, ModifierType.ADDON);
        if (tmp != null)
            modifiers.addAll(tmp);

        optionals = loadAddons(optionalsGuid, ModifierType.OPTIONAL);

        if (modifiers != null)
            for (Pair<String, BigDecimal> pair : modifiers) {
                if (pair.first == null)
                    continue;
                if (uniqMods.containsKey(pair.first)) {
                    uniqMods.put(pair.first, uniqMods.get(pair.first).add(pair.second));
                } else {
                    uniqMods.put(pair.first, pair.second);
                }
            }

        if (optionals != null)
            for (Pair<String, BigDecimal> negativePair : optionals) {
                if (negativePair.first == null)
                    continue;
                if (uniqMods.containsKey(negativePair.first)) {
                    uniqMods.put(negativePair.first, uniqMods.get(negativePair.first).add(negativePair.second));
                }
            }
    }

    private List<Pair<String, BigDecimal>> loadAddons(List<String> addonsGuid, final ModifierType type) {
        if (addonsGuid == null || addonsGuid.isEmpty())
            return null;
        assert type != null;

        FluentIterable<Pair<String, BigDecimal>> it = ProviderAction
                .query(URI_ADDONS)
                .where(ShopStore.ModifierTable.ITEM_GUID + " = ?", itemGuid)
                .where(ShopStore.ModifierTable.TYPE + " = ?", type.ordinal())
                .whereIn(ShopStore.ModifierTable.MODIFIER_GUID, addonsGuid)
                .perform(getContext()).toFluentIterable(new Function<Cursor, Pair<String, BigDecimal>>() {
                                                            @Override
                                                            public Pair<String, BigDecimal> apply(Cursor c) {
                                                                return new Pair<>(c.getString(c.getColumnIndex(ShopStore.ModifierTable.ITEM_SUB_GUID)),
                                                                        _decimalQty(c, c.getColumnIndex(ShopStore.ModifierTable.ITEM_SUB_QTY)));
                                                            }
                                                        }
                );
        return it.toList();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    public static void start(Context context, String orderGuid, ItemType itemType, NegativeStockTrackingCallback callback) {
        create(ItemsNegativeStockTrackingCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ITEM_TYPE, itemType)
                .callback(callback).queueUsing(context);
    }

    public static void start(Context context, String itemGuid, BigDecimal oldQty, ArrayList<SaleOrderItemViewModel.AddonInfo> itemAddons, ItemType itemType) {
        create(ItemsNegativeStockTrackingCommand.class)
                .arg(ARG_ITEM_GUID, itemGuid)
                .arg(ARG_ITEM_OLD_QTY, oldQty)
                .arg(ARG_ITEM_REMOVE_ADDONS, itemAddons)
                .arg(ARG_ITEM_TYPE, itemType).queueUsing(context);
    }

    public static void start(Context context, String orderGuid, String itemGuid, BigDecimal oldQty, BigDecimal newQty, ArrayList<SaleOrderItemViewModel.AddonInfo> itemAddons, ItemType itemType, NegativeStockTrackingCallback callback) {
        create(ItemsNegativeStockTrackingCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ITEM_GUID, itemGuid)
                .arg(ARG_ITEM_REMOVE_ADDONS, itemAddons)
                .arg(ARG_ITEM_TYPE, itemType)
                .arg(ARG_ITEM_NEW_QTY, newQty)
                .arg(ARG_ITEM_OLD_QTY, oldQty)
                .callback(callback).queueUsing(context);
    }

    public static void start(Context context, ItemType itemType, String itemGuid, ArrayList<String> modifierGuid, ArrayList<String> addonsGuid,
                             ArrayList<String> optionalsGuid, NegativeStockTrackingCallback callback) {
        create(ItemsNegativeStockTrackingCommand.class)
                .arg(ARG_ITEM_TYPE, itemType)
                .arg(ARG_ITEM_MODS, modifierGuid)
                .arg(ARG_ITEM_ADDONS, addonsGuid)
                .arg(ARG_ITEM_NOOPT, optionalsGuid)
                .arg(ARG_ITEM_GUID, itemGuid)
                .callback(callback).queueUsing(context);
    }

    public enum ItemType {
        HOLD_ON,
        MODIFIER,
        REMOVE,
        COMPOSITION,
        CHANGE_QTY
    }

    public static abstract class NegativeStockTrackingCallback {

        @OnSuccess(ItemsNegativeStockTrackingCommand.class)
        public final void onSuccess(@Param(COMMAND_RESULT_KEY) boolean result) {
            handleSuccess(result);
        }

        protected abstract void handleSuccess(boolean model);

    }

    private class TrackedItemInfo {
        private String itemGuid;
        private BigDecimal qty4Sale;
        private BigDecimal availableQty;

        public TrackedItemInfo(String itemGuid, BigDecimal qty4Sale, BigDecimal availableQty) {
            this.itemGuid = itemGuid;
            this.qty4Sale = qty4Sale;
            this.availableQty = availableQty;
        }

        public String getItemGuid() {
            return itemGuid;
        }

        public BigDecimal getQty4Sale() {
            return qty4Sale;
        }

        public BigDecimal getAvailableQty() {
            return availableQty;
        }
    }
}
