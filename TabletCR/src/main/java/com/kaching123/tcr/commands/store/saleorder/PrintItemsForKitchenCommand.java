package com.kaching123.tcr.commands.store.saleorder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.activity.HistoryActivity;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.print.printer.PosKitchenPrinter;
import com.kaching123.tcr.print.processor.PrintItemsForKitchenProcessor;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleAddonView2;
import com.kaching123.tcr.store.ShopSchema2.SaleAddonView2.ModifierGroupTable;
import com.kaching123.tcr.store.ShopSchema2.SaleAddonView2.ModifierTable;
import com.kaching123.tcr.store.ShopSchema2.SaleAddonView2.SaleAddonTable;
import com.kaching123.tcr.store.ShopSchema2.SaleItemExDelView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleItemExDelView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonView;
import com.kaching123.tcr.store.ShopStore.SaleItemExDelView;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._kitchenPrintStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;

/**
 * Created by gdubina on 17/02/14.
 */
public class PrintItemsForKitchenCommand extends PublicGroundyTask {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemExDelView.URI_CONTENT);
    private static final Uri URI_MODIFIERS = ShopProvider.getContentUri(SaleAddonView.URI_CONTENT);
    private static final Uri URI_MODIFIERS_GROUP = ShopProvider.getContentUri(ShopStore.ModifierGroupTable.URI_CONTENT);

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterTable.URI_CONTENT);
    private static final Uri URI_ALIAS = ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT);

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_FROM_PRINTER = "ARG_FROM_PRINTER";
    private static final String ARG_SKIP_PRINTER = "ARG_SKIP_PRINTER";
    private static final String ARG_SKIP_PAPER_WARNING = "ARG_SKIP_PAPER_WARNING";
    private static final String ARG_PRINT_ALL_ITEMS = "ARG_PRINT_ALL_ITEMS";
    private static final String ARG_SEARCH_BY_MAC = "ARG_SEARCH_BY_MAC";
    private static final String ARG_ORDER_TITLE = "ARG_ORDER_TITLE";
    private static final String EXTRA_PRINTER = "EXTRA_PRINTER";
    private static final String EXTRA_ALIAS_TITLE = "EXTRA_ALIAS_TITLE";
    private static final String ARG_VOID_ORDER = "ARG_VOID_ORDER";

    private String orderGuid;
    private String orderTitle;
    private String fromPrinter;
    private String saleItemId;
    private boolean skip;
    private boolean skipPaperWarning;
    private boolean searchByMac;
    private boolean isUpdated;
    private boolean printAllItems;
    private boolean isVoidOrder;
    private boolean isSyncCall;

    public static boolean itComesFromPay = true;

    @Override
    protected TaskResult doInBackground() {
        if(!isSyncCall) {
            orderGuid = getStringArg(ARG_ORDER_GUID);
            fromPrinter = getStringArg(ARG_FROM_PRINTER);
            skip = getBooleanArg(ARG_SKIP_PRINTER);
            skipPaperWarning = getBooleanArg(ARG_SKIP_PAPER_WARNING);
            printAllItems = getBooleanArg(ARG_PRINT_ALL_ITEMS);
            searchByMac = getBooleanArg(ARG_SEARCH_BY_MAC);
            orderTitle = getStringArg(ARG_ORDER_TITLE);
            isUpdated = isOrderUpdated();
            isVoidOrder = getBooleanArg(ARG_VOID_ORDER);
        }

        List<ItemInfo> items = loadItems();
        ArrayList<String> guids = new ArrayList<String>(items.size());
        for (ItemInfo item : items) {
            guids.add(item.guid);
        }

        LinkedHashMap<String, List<ItemInfo>> itemsMap = new LinkedHashMap<String, List<ItemInfo>>();
        for (ItemInfo i : items) {
            List<ItemInfo> ii = itemsMap.get(i.printAliasGuid);
            if (ii == null) {
                itemsMap.put(i.printAliasGuid, ii = new ArrayList<ItemInfo>());
            }
            ii.add(i);
        }

        boolean find = fromPrinter == null;

        Map<String, List<PrinterInfo>> printers = loadPrinters(itemsMap.keySet());
        Map<String, String> guid2Title = loadAliasTitles(itemsMap.keySet());
        for (Entry<String, List<ItemInfo>> e : itemsMap.entrySet()) {
            String aliasGuid = e.getKey();
            if (!find && fromPrinter != null && !fromPrinter.equals(aliasGuid)) {
                continue;
            }
            find = true;
            List<PrinterInfo> pp = printers.get(aliasGuid);
            if (pp == null || pp.isEmpty()) {
                if (skip && fromPrinter.equals(aliasGuid)) {
                    continue;
                }
                boolean qtyUpdated = new UpdateSaleItemKitchenQty().syncStandalone(getContext(), e.getValue(), getAppCommandContext());
                return failed().add(PrintForKitchenCommand.EXTRA_ERROR_PRINTER, PrinterError.NOT_CONFIGURED).add(EXTRA_PRINTER,
                        aliasGuid).add(EXTRA_ALIAS_TITLE, guid2Title.get(aliasGuid));
            }
            for (PrinterInfo p : pp) {
                TaskResult result = new PrintForKitchenCommand().sync(getContext(), p, e.getValue(), aliasGuid,
                        skipPaperWarning, searchByMac, isUpdated, getAppCommandContext());
                if (isFailed(result)) {
                    if (skip && fromPrinter.equals(aliasGuid)) {
                        continue;
                    }
                    return result.add(EXTRA_PRINTER, aliasGuid).add(EXTRA_ALIAS_TITLE, guid2Title.get(aliasGuid));
                } else {
                    boolean qtyUpdated = new UpdateSaleItemKitchenQty().syncStandalone(getContext(), e.getValue(), getAppCommandContext());
                    if (!qtyUpdated) {
                        return failed().add(EXTRA_PRINTER, aliasGuid).add(EXTRA_ALIAS_TITLE, guid2Title.get(aliasGuid));
                    }
                }
            }
        }

        if (!new UpdateSaleOrderKitchenPrintStatusCommand().syncStandalone(getContext(), orderGuid, KitchenPrintStatus.PRINTED, getAppCommandContext()))
            return failed();

        return succeeded();
    }

    private boolean isOrderUpdated() {
        Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(SaleOrderTable.URI_CONTENT, 1))
                .projection(SaleOrderTable.KITCHEN_PRINT_STATUS)
                .where(SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(getContext());

        boolean result = false;
        if (c.moveToFirst()) {
            result = _kitchenPrintStatus(c, 0) == KitchenPrintStatus.UPDATED;
        }
        c.close();
        return result;
    }

    private Map<String, List<PrinterInfo>> loadPrinters(Collection<String> aliases) {
        if (aliases == null || aliases.isEmpty())
            return new HashMap<String, List<PrinterInfo>>();
        Cursor c = ProviderAction.query(URI_PRINTER)
                .projection(
                        PrinterTable.ALIAS_GUID,
                        PrinterTable.IP,
                        PrinterTable.PORT,
                        PrinterTable.MAC,
                        PrinterTable.SUBNET,
                        PrinterTable.GATEWAY,
                        PrinterTable.DHCP,
                        PrinterTable.PRINTER_TYPE
                )
                .whereIn(PrinterTable.ALIAS_GUID, aliases)
                .perform(getContext());
        HashMap<String, List<PrinterInfo>> result = new HashMap<String, List<PrinterInfo>>();
        while (c.moveToNext()) {
            String aliasGuid = c.getString(0);
            List<PrinterInfo> printers = result.get(aliasGuid);
            if (printers == null) {
                result.put(aliasGuid, printers = new ArrayList<PrinterInfo>());
            }
            printers.add(new PrinterInfo(
                    c.getString(1),
                    c.getInt(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getInt(6) == 1,
                    c.getString(7)));
        }
        c.close();
        return result;
    }

    private List<ItemInfo> loadItems() {
        /** load items **/
        Query query = ProviderAction
                .query(URI_ITEMS)
                .projection(
                        SaleItemTable.SALE_ITEM_GUID,
                        ItemTable.DESCRIPTION,
                        SaleItemTable.QUANTITY,
                        SaleItemTable.KITCHEN_PRINTED_QTY,
                        SaleItemTable.NOTES,
                        ItemTable.PRINTER_ALIAS_GUID
                )
                .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .where(ItemTable.PRINTER_ALIAS_GUID + " IS NOT NULL");

        if (!printAllItems) {
            query.where(SaleItemTable.QUANTITY + " != " + SaleItemTable.KITCHEN_PRINTED_QTY);
        }

        if (isVoidOrder) {
            query.where(SaleItemTable.KITCHEN_PRINTED_QTY + " <> ?", "0.000");
        }

        if (!TextUtils.isEmpty(saleItemId)) {
            query.where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemId);
        }

        List<ItemInfo> items = query
                .perform(getContext())
                .toFluentIterable(new Function<Cursor, ItemInfo>() {
                    @Override
                    public ItemInfo apply(Cursor c) {
                        return new ItemInfo(
                                c.getString(0),
                                c.getString(1),
                                _decimalQty(c, 2, BigDecimal.ZERO),
                                _decimalQty(c, 3, BigDecimal.ZERO),
                                c.getString(4),
                                c.getString(5));
                    }
                }).toList();

        /** load modifiers **/
        Cursor c = null;
        for (ItemInfo item : items) {
            c = ProviderAction
                    .query(URI_MODIFIERS)
                    .where(SaleAddonTable.ITEM_GUID + " = ?", item.guid)
                    .perform(getContext());
            ArrayList<SimpleModifier> modifiers = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                ModifierType type = _modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE));
                String title = c.getString(c.getColumnIndex(SaleAddonView2.ItemTable.DESCRIPTION));
                if (title == null)
                    title = c.getString(c.getColumnIndex(ModifierTable.TITLE));
                int groupOrderNum = c.getInt(c.getColumnIndex(ModifierGroupTable.ORDER_NUM));
                int orderNum = c.getInt(c.getColumnIndex(ModifierTable.ORDER_NUM));

                modifiers.add(new SimpleModifier(type, title, groupOrderNum, orderNum));
            }
            Collections.sort(modifiers);
            item.modifiers = modifiers;

        }
        if (c != null)
            c.close();

        return items;
    }

    public class SimpleModifier implements Comparable<SimpleModifier>{
        public ModifierType type;
        public String title;
        public int groupOrderNum;
        public int orderNum;

        public SimpleModifier(ModifierType type, String title, int groupOrderNum, int orderNum){
            this.type = type;
            this.title = title;
            this.groupOrderNum = groupOrderNum;
            this.orderNum = orderNum;
        }

        @Override
        public int compareTo(@NonNull SimpleModifier another) {
            int diff;

            diff = type.ordinal() - another.type.ordinal();
            if (diff != 0)
                return diff;

            diff = groupOrderNum - another.groupOrderNum;
            if (diff != 0)
                return diff;

            diff = orderNum - another.orderNum;
            if (diff != 0)
                return diff;

            return  title.compareTo(another.title);
        }
    }

    private Map<String, String> loadAliasTitles(Collection<String> aliases) {
        HashMap<String, String> guid2Title = new HashMap<String, String>();
        Cursor c = ProviderAction
                .query(URI_ALIAS)
                .projection(PrinterAliasTable.GUID, PrinterAliasTable.ALIAS)
                .whereIn(PrinterAliasTable.GUID, aliases)
                .perform(getContext());

        while (c.moveToNext()) {
            guid2Title.put(c.getString(0), c.getString(1));
        }
        c.close();

        return guid2Title;
    }


    private class PrintForKitchenCommand extends PrinterCommand {

        private List<ItemInfo> items;
        private PrinterInfo printer;
        private String aliasGuid;
        private boolean skipPaperWarning;
        private boolean searchByMac;
        private boolean isUpdated;

        protected PrinterInfo getPrinter() {
            return printer;
        }

        @Override
        protected TaskResult execute(PosPrinter printer) throws IOException {
            final PosKitchenPrinter kitchenPrinter = new PosKitchenPrinter();
            kitchenPrinter.setVoidOrder(isVoidOrder);
            PrintItemsForKitchenProcessor processor = new PrintItemsForKitchenProcessor(items, this.printer, aliasGuid, orderGuid,
                    isUpdated, orderTitle, printAllItems, isVoidOrder, this.getAppCommandContext());

            processor.print(getContext(), getApp(), kitchenPrinter);

            kitchenPrinter.emptyLine(5);
            try {
                /*
                 *   Added if condition to print only if "Receipt Settings" configuration is seted "Print Kitchen Receipt for On Hold Orders" = enabled
                 */
                if(getApp().getShopInfo().printOnholdOrders || itComesFromPay) {
                    kitchenPrinter.print(printer);
                }
            } catch (IOException e) {
                Logger.e("PrintForKitchenCommand execute error: ", e);
                return failed().add(EXTRA_ERROR_PRINTER, PrinterError.DISCONNECTED);
            }

            return succeeded();
        }

        @Override
        protected TaskResult validatePrinterStateExt(PrinterStatusEx status) {
            if (!(skipPaperWarning || searchByMac) && status.offlineStatus.paperIsNearEnd) {
                Logger.e("PrintForKitchenCommand validate statues execute: paperIsNearEnd!");
                return failed().add(EXTRA_ERROR_PRINTER, PrinterError.PAPER_IS_NEAR_END);
            }

            if (status.offlineStatus.noPaper) {
                Logger.e("PrintForKitchenCommand validate statues execute: noPaper!");
                return failed().add(EXTRA_ERROR_PRINTER, PrinterError.NO_PAPER);
            }

            if (!status.offlineStatus.coverIsClosed) {
                Logger.e("PrintForKitchenCommand validate statues execute: noPaper!");
                return failed().add(EXTRA_ERROR_PRINTER, PrinterError.COVER_IS_OPENED);
            }

            if (status.printerHead.headIsOverhead) {
                Logger.e("PrintForKitchenCommand validate statues execute: headIsOverhead!");
                return failed().add(EXTRA_ERROR_PRINTER, PrinterError.HEAD_OVERHEATED);
            }

            return null;
        }


        public TaskResult sync(Context context, PrinterInfo printer, List<ItemInfo> items, String aliasGuid, boolean skipPaperWarning,
                               boolean searchByMac, boolean isUpdated, IAppCommandContext appCommandContext) {
            this.items = items;
            this.printer = printer;
            this.aliasGuid = aliasGuid;
            this.skipPaperWarning = skipPaperWarning;
            this.searchByMac = searchByMac;
            this.isUpdated = isUpdated;

            return sync(context, null, appCommandContext);
        }
    }

    public class ItemInfo {
        public String guid;
        public String description;
        public BigDecimal qty;
        public BigDecimal printedQty;
        public String printAliasGuid;
        public String notes;

        public ArrayList<SimpleModifier> modifiers;

        public ArrayList<String> modifier = new ArrayList<>();
        public ArrayList<String> addons = new ArrayList<String>();
        public ArrayList<String> options = new ArrayList<String>();

        private ItemInfo(String guid, String description, BigDecimal qty, BigDecimal printedQty, String notes, String printAliasGuid) {
            this.guid = guid;
            this.description = description;
            this.qty = qty;
            this.printedQty = printedQty;
            this.notes = notes;
            this.printAliasGuid = printAliasGuid;
        }
    }

    public static abstract class BaseKitchenPrintCallback {

        @OnSuccess(PrintItemsForKitchenCommand.class)
        public final void onSuccess() {
            onPrintSuccess();
        }

        @OnFailure(PrintItemsForKitchenCommand.class)
        public final void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError printerError,
                                    @Param(EXTRA_PRINTER) String fromPrinter,
                                    @Param(EXTRA_ALIAS_TITLE) String aliasTitle) {

            if (printerError != null && printerError == PrinterError.DISCONNECTED) {
                onPrinterDisconnected(fromPrinter, aliasTitle);
                return;
            }
            if (printerError != null && printerError == PrinterError.IP_NOT_FOUND) {
                onPrinterIPnotFound(fromPrinter, aliasTitle);
                return;
            }
            if (printerError != null && printerError == PrinterError.NOT_CONFIGURED) {
                onPrinterNotConfigured(fromPrinter, aliasTitle);
                return;
            }
            if (printerError != null && printerError == PrinterError.PAPER_IS_NEAR_END) {
                onPrinterPaperNearTheEnd(fromPrinter, aliasTitle);
                return;
            }
            onPrintError(printerError, fromPrinter, aliasTitle);
        }

        protected abstract void onPrintSuccess();

        protected abstract void onPrintError(PrinterError error, String fromPrinter, String aliasTitle);

        protected abstract void onPrinterNotConfigured(String fromPrinter, String aliasTitle);

        protected abstract void onPrinterDisconnected(String fromPrinter, String aliasTitle);

        protected abstract void onPrinterIPnotFound(String fromPrinter, String aliasTitle);

        protected abstract void onPrinterPaperNearTheEnd(String fromPrinter, String aliasTitle);
    }


    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, String fromPrinter,
                             boolean skip, BaseKitchenPrintCallback callback, boolean printAllItems, String argOrderTitle) {
        create(PrintItemsForKitchenCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_FROM_PRINTER, fromPrinter)
                .arg(ARG_SKIP_PRINTER, skip)
                .arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning)
                .arg(ARG_PRINT_ALL_ITEMS, printAllItems)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_ORDER_TITLE, argOrderTitle)
                .callback(callback)
                .queueUsing(context);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, String fromPrinter,
                             boolean skip, boolean printAllItems, String argOrderTitle, boolean isVoidOrder) {
        create(PrintItemsForKitchenCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_FROM_PRINTER, fromPrinter)
                .arg(ARG_SKIP_PRINTER, skip)
                .arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning)
                .arg(ARG_PRINT_ALL_ITEMS, printAllItems)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_ORDER_TITLE, argOrderTitle)
                .arg(ARG_VOID_ORDER, isVoidOrder)
                .queueUsing(context);
    }

    public void sync(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, String fromPrinter,
                     boolean skip, boolean printAllItems, String argOrderTitle, boolean isVoidOrder, String saleItemId, IAppCommandContext appCommandContext){
        this.saleItemId = saleItemId;
        sync(context, skipPaperWarning, searchByMac, orderGuid, fromPrinter, skip, printAllItems, argOrderTitle, isVoidOrder, appCommandContext);
    }

    public void sync(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, String fromPrinter,
                     boolean skip, boolean printAllItems, String argOrderTitle, boolean isVoidOrder, IAppCommandContext appCommandContext){
        this.isSyncCall = true;
        this.skipPaperWarning = skipPaperWarning;
        this.searchByMac = searchByMac;
        this.orderGuid = orderGuid;
        this.fromPrinter = fromPrinter;
        this.skip = skip;
        this.printAllItems = printAllItems;
        this.orderTitle = argOrderTitle;
        this.isVoidOrder= isVoidOrder;
        sync(context, null, appCommandContext);
    }

    public enum KitchenPrintStatus {
        PRINT, PRINTED, UPDATED
    }
}
