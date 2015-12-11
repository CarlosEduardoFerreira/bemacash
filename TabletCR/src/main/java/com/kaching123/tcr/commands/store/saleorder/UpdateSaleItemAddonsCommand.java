package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemAddonJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CursorUtil._wrapOrNull;

/**
 * Created by gdubina on 19/11/13.
 */
public class UpdateSaleItemAddonsCommand extends AsyncCommand {

    private static final Uri URI_ADDONS = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    private static final Uri URI_SALE_ADDONS_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(SaleAddonTable.URI_CONTENT);
    private static final Uri URI_SALE_ADDONS = ShopProvider.getContentUri(SaleAddonTable.URI_CONTENT);

    private static final String ARG_MODIFIER_GUID = "ARG_MODIFIER_GUID";
    private static final String ARG_ADDONS_GUIDS = "ARG_ADDONS_GUIDS";
    private static final String ARG_OPTIONALS_GUIDS = "ARG_OPTIONALS_GUIDS";

    private static final String ARG_SALE_ITEM_GUID = "ARG_SALE_ITEM_GUID";
    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";

    private static final String ARG_SKIP_NOTIFY = "ARG_SKIP_NOTIFY";

    private static final String PARAM_SALE_ITEM_GUID = "PARAM_SALE_ITEM_GUID";

    private SaleOrderItemAddonModel modifier;
    private List<SaleOrderItemAddonModel> addons;
    private List<SaleOrderItemAddonModel> optionals;
    private String saleItemGuid;
    private String itemGuid;
    private boolean skipNotify;

    private ImmutableSet<String> needToDelete;

    @Override
    protected TaskResult doCommand() {
        saleItemGuid = getStringArg(ARG_SALE_ITEM_GUID);
        itemGuid = getStringArg(ARG_ITEM_GUID);

        String modifierGuid = getStringArg(ARG_MODIFIER_GUID);
        List<String> addonsGuid = getArgs().getStringArrayList(ARG_ADDONS_GUIDS);
        List<String> optionalsGuid = getArgs().getStringArrayList(ARG_OPTIONALS_GUIDS);

        skipNotify = getBooleanArg(ARG_SKIP_NOTIFY);

        final Set<String> oldAddons = loadOldAddons();
        final ImmutableSet.Builder<String> newAddonsBuilder = ImmutableSet.builder();

        if (modifierGuid != null) {
            newAddonsBuilder.add(modifierGuid);
            if (oldAddons.contains(modifierGuid)) {
                modifierGuid = null;
            }
        }

        Predicate filter = new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return !oldAddons.contains(s);
            }
        };

        if (addonsGuid != null && !addonsGuid.isEmpty()) {
            newAddonsBuilder.addAll(addonsGuid);
            addonsGuid = FluentIterable.from(addonsGuid).filter(filter).toImmutableList();
        }

        if (optionalsGuid != null && !optionalsGuid.isEmpty()) {
            newAddonsBuilder.addAll(optionalsGuid);
            optionalsGuid = FluentIterable.from(optionalsGuid).filter(filter).toImmutableList();
        }

        needToDelete = Sets.difference(oldAddons, newAddonsBuilder.build()).immutableCopy();

        initVariables(modifierGuid, addonsGuid, optionalsGuid);

        return succeeded().add(PARAM_SALE_ITEM_GUID, saleItemGuid);
    }

    private Set<String> loadOldAddons() {
        FluentIterable<String> it = ProviderAction
                .query(URI_SALE_ADDONS_NO_NOTIFY)
                .where(SaleAddonTable.ITEM_GUID + " = ?", saleItemGuid)
                .perform(getContext()).toFluentIterable(new Function<Cursor, String>() {
                    @Override
                    public String apply(Cursor c) {
                        return c.getString(c.getColumnIndex(SaleAddonTable.ADDON_GUID));
                    }
                }
                );
        return it.toImmutableSet();
    }

    private void initVariables(String modifierGuid, List<String> addonsGuid, List<String> optionalsGuid) {
        modifier = loadModifier(modifierGuid);
        addons = loadAddons(addonsGuid, ModifierType.ADDON);
        optionals = loadAddons(optionalsGuid, ModifierType.OPTIONAL);
    }

    private SaleOrderItemAddonModel loadModifier(String modifierGuid) {
        if (TextUtils.isEmpty(modifierGuid))
            return null;
        return _wrapOrNull(ProviderAction
                .query(URI_ADDONS)
                .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                .where(ModifierTable.TYPE + " = ?", ModifierType.MODIFIER.ordinal())
                .where(ModifierTable.MODIFIER_GUID + " = ?", modifierGuid)
                .perform(getContext()),
                new Function<Cursor, SaleOrderItemAddonModel>() {
                    @Override
                    public SaleOrderItemAddonModel apply(Cursor c) {
                        Logger.d("Modifier: %s = %s", ModifierType.MODIFIER.toString(), c.getString(c.getColumnIndex(ModifierTable.MODIFIER_GUID)));
                        return new SaleOrderItemAddonModel(
                                UUID.randomUUID().toString(),
                                c.getString(c.getColumnIndex(ModifierTable.MODIFIER_GUID)),
                                saleItemGuid,
                                _decimal(c, c.getColumnIndex(ModifierTable.EXTRA_COST)),
                                ModifierType.MODIFIER);
                    }
                }
        );
    }

    private List<SaleOrderItemAddonModel> loadAddons(List<String> addonsGuid, final ModifierType type) {
        if (addonsGuid == null || addonsGuid.isEmpty())
            return null;
        assert type != null;

        FluentIterable<SaleOrderItemAddonModel> it = ProviderAction
                .query(URI_ADDONS)
                .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                .where(ModifierTable.TYPE + " = ?", type.ordinal())
                .whereIn(ModifierTable.MODIFIER_GUID, addonsGuid)
                .perform(getContext()).toFluentIterable(new Function<Cursor, SaleOrderItemAddonModel>() {
                    @Override
                    public SaleOrderItemAddonModel apply(Cursor c) {
                        Logger.d("Addon: %s = %s", type.name(), c.getString(c.getColumnIndex(ModifierTable.MODIFIER_GUID)));
                        return new SaleOrderItemAddonModel(
                                UUID.randomUUID().toString(),
                                c.getString(c.getColumnIndex(ModifierTable.MODIFIER_GUID)),
                                saleItemGuid,
                                _decimal(c, c.getColumnIndex(ModifierTable.EXTRA_COST)),
                                type);
                    }
                }
                );
        return it.toImmutableList();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        for (String addonGuid : needToDelete) {
            operations.add(ContentProviderOperation.newUpdate(URI_SALE_ADDONS_NO_NOTIFY)
                    .withValues(ShopStore.DELETE_VALUES)
                    .withSelection(SaleAddonTable.ADDON_GUID + " = ?", new String[]{addonGuid})
                    .build());
        }

        if (modifier != null) {
            operations.add(ContentProviderOperation.newInsert(URI_SALE_ADDONS_NO_NOTIFY)
                    .withValues(modifier.toValues())
                    .build());
        }
        if (addons != null) {
            for (SaleOrderItemAddonModel addon : addons) {
                operations.add(ContentProviderOperation.newInsert(URI_SALE_ADDONS_NO_NOTIFY)
                        .withValues(addon.toValues())
                        .build());
            }
        }
        if (optionals != null) {
            for (SaleOrderItemAddonModel optional : optionals) {
                operations.add(ContentProviderOperation.newInsert(URI_SALE_ADDONS_NO_NOTIFY)
                        .withValues(optional.toValues())
                        .build());
            }
        }

        if (operations.isEmpty())
            return null;


        if (!skipNotify) {
            ContentProviderOperation lastOperation = operations.get(operations.size() - 1);
            operations.remove(operations.size() - 1);

            String[] selectionArgs = lastOperation.resolveSelectionArgsBackReferences(null, 0);
            boolean isUpdate = selectionArgs != null;

            if (isUpdate) {
                operations.add(ContentProviderOperation.newUpdate(URI_SALE_ADDONS)
                        .withValues(ShopStore.DELETE_VALUES)
                        .withSelection(SaleAddonTable.ADDON_GUID + " = ?", selectionArgs)
                        .build());
            } else {
                operations.add(ContentProviderOperation.newInsert(URI_SALE_ADDONS)
                        .withValues(lastOperation.resolveValueBackReferences(null, 0))
                        .build());
            }
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(SaleOrderItemAddonModel.class);
        SaleOrderItemAddonJdbcConverter converter = (SaleOrderItemAddonJdbcConverter) JdbcFactory.getConverter(SaleAddonTable.TABLE_NAME);
        assert converter != null;

        for (String addonGuid : needToDelete) {
            batch.add(converter.deleteItemAddonsSQL(addonGuid, saleItemGuid, getAppCommandContext()));
        }
        if (modifier != null) {
            batch.add(converter.insertSQL(modifier, getAppCommandContext()));
        }
        if (addons != null) {
            for (SaleOrderItemAddonModel a : addons) {
                batch.add(converter.insertSQL(a, getAppCommandContext()));
            }
        }
        if (optionals != null) {
            for (SaleOrderItemAddonModel a : optionals) {
                batch.add(converter.insertSQL(a, getAppCommandContext()));
            }
        }

        SaleOrderItemModel model = new SaleOrderItemModel(saleItemGuid);
        SaleOrderItemJdbcConverter saleItemModel = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(model);
        batch.add(saleItemModel.updateFinalPrices(model, getAppCommandContext()));
        return batch;
    }

    public static void start(Context context,
                             String saleItemGuid,
                             String itemGuid,
                             String modifierGiud,
                             ArrayList<String> addonsGuid,
                             ArrayList<String> optionalGuid,
                             BaseUpdateSaleItemAddonsCallback callback) {
        create(UpdateSaleItemAddonsCommand.class)
                .args(args(saleItemGuid,
                        itemGuid,
                        modifierGiud,
                        addonsGuid,
                        optionalGuid))
                .callback(callback)
                .queueUsing(context);
    }

    public SyncResult sync(Context context, String saleItemGuid, String itemGuid, String modifierGiud, ArrayList<String> addonsGuid, ArrayList<String> optionalGuid, IAppCommandContext appCommandContext) {
        return syncDependent(context, args(saleItemGuid, itemGuid, modifierGiud, addonsGuid, optionalGuid), appCommandContext);
    }

    public SyncResult sync(Context context, String saleItemGuid, String itemGuid, String modifierGiud, ArrayList<String> addonsGuid, ArrayList<String> optionalGuid, boolean skipNotify, IAppCommandContext appCommandContext) {
        return syncDependent(context, args(saleItemGuid, itemGuid, modifierGiud, addonsGuid, optionalGuid, skipNotify), appCommandContext);
    }

    private static Bundle args(String saleItemGuid, String itemGuid, String modifierGiud, ArrayList<String> addonsGuid, ArrayList<String> optionalGuid) {
        return args(saleItemGuid, itemGuid, modifierGiud, addonsGuid, optionalGuid, false);
    }

    public static Bundle args(String saleItemGuid,
                              String itemGuid,
                              String modifierGiud,
                              ArrayList<String> addonsGuid,
                              ArrayList<String> optionalGuid,
                              boolean skipNotify) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SALE_ITEM_GUID, saleItemGuid);
        bundle.putString(ARG_ITEM_GUID, itemGuid);
        bundle.putString(ARG_MODIFIER_GUID, modifierGiud);
        bundle.putStringArrayList(ARG_ADDONS_GUIDS, addonsGuid);
        bundle.putStringArrayList(ARG_OPTIONALS_GUIDS, optionalGuid);
        bundle.putBoolean(ARG_SKIP_NOTIFY, skipNotify);
        return bundle;
    }

    public static abstract class BaseUpdateSaleItemAddonsCallback {

        @OnSuccess(UpdateSaleItemAddonsCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String saleItemGuid) {
            onSuccess(saleItemGuid);
        }

        protected abstract void onSuccess(String saleItemGuid);

    }

}
