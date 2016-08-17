package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.device.KDSCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderKdsPrintStatusCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderKitchenPrintStatusCommand;
import com.kaching123.tcr.model.KDSModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.KDSModifier;
import com.kaching123.tcr.model.converter.KdsOrder;
import com.kaching123.tcr.model.converter.KdsOrderItem;
import com.kaching123.tcr.model.converter.KdsTransaction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._kdsSendStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;

/**
 * Created by long.jiao on 07.07.16.
 */
public class PrintOrderToKdsCommand extends PublicGroundyTask {

    public enum KDSSendStatus {
        PRINT, PRINTED, UPDATED
    }
    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String EXTRA_ERROR_KDS = "EXTRA_ERROR_KDS";
    private static final String EXTRA_KDS = "EXTRA_KDS";
    private static final String EXTRA_ALIAS_TITLE = "EXTRA_ALIAS_TITLE";
    private static final String ARG_KDS_STATUS = "ARG_KDS_STATUS";

    private static final Uri URI_KDS = ShopProvider.getContentUri(ShopStore.KDSTable.URI_CONTENT);
    private static final Uri URI_ALIAS = ShopProvider.getContentUri(ShopStore.KDSAliasTable.URI_CONTENT);
    private static final Uri URI_ITEM_KDS_ALIAS = ShopProvider.getContentUri(ShopStore.ItemKDSTable.URI_CONTENT);

    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ShopStore.SaleItemExDelView.URI_CONTENT);
    private static final Uri URI_MODIFIERS = ShopProvider.getContentUri(ShopStore.SaleAddonView.URI_CONTENT);
    private static final Uri URI_MODIFIERS_GROUP = ShopProvider.getContentUri(ShopStore.ModifierGroupTable.URI_CONTENT);
    private static final Uri URI_REGISTER = ShopProvider.getContentUri(ShopStore.RegisterTable.URI_CONTENT);

    private KdsTransaction KdsTransaction;

    @Override
    protected TaskResult doInBackground() {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        boolean isVoidOrder = getBooleanArg(ARG_KDS_STATUS);
        List<ItemInfo> items = loadItems(orderGuid);

        LinkedHashMap<String, List<ItemInfo>> itemsMap = new LinkedHashMap<>();
        for (ItemInfo i : items) {
            if(i.kdsAliasGuid != 0){
                List<String> kdsGuidList = getKdsListOfItem(i.itemGuid);
                for(String s: kdsGuidList){
                    if(itemsMap.containsKey(s))
                        itemsMap.get(s).add(i);
                    else{
                        itemsMap.put(s, new ArrayList<ItemInfo>());
                        itemsMap.get(s).add(i);
                    }
                }
            }
        }

        Map<String, List<KDSModel>> kdsMap = loadKds(itemsMap.keySet());
        Map<String, String> guid2Title = loadAliasTitles(itemsMap.keySet());
        for (Map.Entry<String, List<ItemInfo>> e : itemsMap.entrySet()) {
            String aliasGuid = e.getKey();
            List<KDSModel> kdsModels = kdsMap.get(aliasGuid);
            if(kdsModels == null || kdsModels.isEmpty()){
                return failed().add(EXTRA_ERROR_KDS, KDSCommand.KDSError.NOT_CONFIGURED).add(EXTRA_KDS, aliasGuid).add(EXTRA_ALIAS_TITLE, guid2Title.get(aliasGuid));
            }
            StringBuilder sb = new StringBuilder();
            for (KDSModel kdsModel : kdsModels) {
                sb.append(kdsModel.stationId).append(',');
            }
            sb.deleteCharAt(sb.length()-1);// delete last ','
            TaskResult result = new SendToKDSCommand().sync(getContext(), sb.toString(), orderGuid, e.getValue(), isVoidOrder , getAppCommandContext());
            if (isFailed(result)) {
                return result.add(EXTRA_KDS, aliasGuid).add(EXTRA_ALIAS_TITLE, guid2Title.get(aliasGuid));
            }
        }

        if (!new UpdateSaleOrderKdsPrintStatusCommand().syncStandalone(getContext(), orderGuid, KDSSendStatus.PRINTED, getAppCommandContext()))
            return failed();

        return succeeded();
    }

    private List<String> getKdsListOfItem(String guid) {
        ArrayList<String> kdsGuids = new ArrayList<>();
        Cursor c = ProviderAction.query(URI_ITEM_KDS_ALIAS)
                            .projection(ShopStore.ItemKDSTable.KDS_ALIAS_GUID)
                            .where(ShopStore.ItemKDSTable.ITEM_GUID + " = ?", guid)
                            .perform(getContext());
        if(c.moveToFirst()){
            do{
                kdsGuids.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return kdsGuids;
    }

//    private boolean isOrderUpdated(String orderGuid) {
//        Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(ShopStore.SaleOrderTable.URI_CONTENT, 1))
//                .projection(ShopStore.SaleOrderTable.KDS_SEND_STATUS)
//                .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid)
//                .perform(getContext());
//
//        boolean result = false;
//        if (c.moveToFirst()) {
//            result = _kdsSendStatus(c, 0) == KDSSendStatus.UPDATED;
//        }
//        c.close();
//        return result;
//    }

    private class SendToKDSCommand extends KDSCommand {
        private String orderGuid;
        private List<ItemInfo> items;
        private boolean isVoid;

        @Override
        protected TaskResult execute() throws IOException {
            if(TextUtils.isEmpty(getApp().getShopPref().kdsRouterIp().getOr("")))
                return failed().add(EXTRA_ERROR_KDS, KDSError.IP_NOT_FOUND);
            KdsTransaction kdsTransaction = covertItemsToKdsOrder(orderGuid, items);
            Serializer serializer = new Persister();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try{
                serializer.write(kdsTransaction, baos);
            } catch (Exception e) {
                e.printStackTrace();
                return failed().add(EXTRA_ERROR_KDS, KDSError.OFFLINE);
            }
            String str = new String( baos.toByteArray(), "UTF-8");
            str = removeTags(str);
            writeToFile(str);
            byte[] command = buildXmlCommand(str);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(command);
            return succeeded();
        }

        private void writeToFile(String data) {
            try {
                OutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/example.xml"), false); // true will be same as Context.MODE_APPEND
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(data);
                outputStreamWriter.close();
                outputStream.flush();
                outputStream.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }

        private KdsTransaction covertItemsToKdsOrder(String orderGuid, List<ItemInfo> items) {
            KdsTransaction transaction = new KdsTransaction();
            Cursor c = ProviderAction.query(URI_ORDER)
                            .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid)
                            .perform(getContext());
            if(!c.moveToFirst()) return transaction;
            SaleOrderModel saleOrderModel = new SaleOrderModel(c);
            c = ProviderAction.query(URI_REGISTER)
                            .projection(ShopStore.RegisterTable.TITLE)
                            .where(ShopStore.RegisterTable.ID + " = ?", getApp().getRegisterId())
                            .perform(getContext());
            if(!c.moveToFirst()) return transaction;
            String registerTitle = c.getString(0);
            c.close();
//            if(isVoid){
//                KdsOrder order = new KdsOrder(registerTitle+"-"+saleOrderModel.printSeqNum);
//                transaction.setOrder(order);
//                return transaction;
//            }
            KdsOrder order = new KdsOrder(registerTitle+"-"+saleOrderModel.printSeqNum,
                                            saleOrderModel.registerId,
                                            isVoid ? 2:saleOrderModel.kdsSendStatus == KDSSendStatus.PRINT ? 1:6,
                                            2, // put as in processing for now
                                            "", // normal order
                                            getApp().getOperatorFullName(),
                                            registerTitle);
            ArrayList<KdsOrderItem> itemList = new ArrayList<>();
            order.setItems(itemList);
            for (ItemInfo item : items){
                KdsOrderItem kdsItem = new KdsOrderItem(item.guid, 1, item.description, item.qty.setScale(2, BigDecimal.ROUND_CEILING).toString(), kdsModels);
                if(!item.addons.isEmpty()){
                    ArrayList<KDSModifier> modifiers = new ArrayList<>();
                    for(int i = 0; i < item.addons.size(); i++){
                        modifiers.add(new KDSModifier(i+"",1,item.addons.get(i)));
                    }
                    kdsItem.setModifiers(modifiers);
                }
                itemList.add(kdsItem);
            }
            transaction.setOrder(order);
            return transaction;
        }

        public TaskResult sync(Context context, String kdsModels,String orderGuid, List<ItemInfo> items, boolean isVoid, IAppCommandContext appCommandContext) {
            this.orderGuid = orderGuid;
            this.items = items;
            this.isVoid = isVoid;

            return sync(context, kdsModels, appCommandContext);
        }
    }

    private Map<String, List<KDSModel>> loadKds(Collection<String> aliases) {
        if (aliases == null || aliases.isEmpty())
            return new HashMap<>();
        Cursor c = ProviderAction.query(URI_KDS)
                .whereIn(ShopStore.KDSTable.ALIAS_GUID, aliases)
                .perform(getContext());
        HashMap<String, List<KDSModel>> result = new HashMap<>();
        while (c.moveToNext()) {
            String aliasGuid = c.getString(c.getColumnIndex(ShopStore.KDSTable.ALIAS_GUID));
            List<KDSModel> printers = result.get(aliasGuid);
            if (printers == null) {
                result.put(aliasGuid, printers = new ArrayList<>());
            }
            printers.add(new KDSModel(c));
        }
        c.close();
        return result;
    }

    private Map<String, String> loadAliasTitles(Collection<String> aliases) {
        HashMap<String, String> guid2Title = new HashMap<String, String>();
        Cursor c = ProviderAction
                .query(URI_ALIAS)
                .projection(ShopStore.KDSAliasTable.GUID, ShopStore.KDSAliasTable.ALIAS)
                .whereIn(ShopStore.KDSAliasTable.GUID, aliases)
                .perform(getContext());

        while (c.moveToNext()) {
            guid2Title.put(c.getString(0), c.getString(1));
        }
        c.close();

        return guid2Title;
    }

    private List<ItemInfo> loadItems(String orderGuid) {
        /** load items **/
        Query query = ProviderAction
                .query(URI_ITEMS)
                .projection(
                        ShopSchema2.SaleItemExDelView2.SaleItemTable.SALE_ITEM_GUID,
                        ShopSchema2.SaleItemExDelView2.ItemTable.DESCRIPTION,
                        ShopSchema2.SaleItemExDelView2.SaleItemTable.QUANTITY,
                        ShopSchema2.SaleItemExDelView2.ItemTable.GUID,
                        ShopSchema2.SaleItemExDelView2.ItemTable.KDS_ALIAS_GUID,
                        ShopSchema2.SaleItemExDelView2.SaleItemTable.NOTES
                )
                .where(ShopSchema2.SaleItemExDelView2.SaleItemTable.ORDER_GUID + " = ?", orderGuid);

//        if (!printAllItems) {
//            query.where(ShopSchema2.SaleItemExDelView2.SaleItemTable.QUANTITY + " != " + ShopSchema2.SaleItemExDelView2.SaleItemTable.KITCHEN_PRINTED_QTY);
//        }


        List<ItemInfo> items = query
                .perform(getContext())
                .toFluentIterable(new Function<Cursor, ItemInfo>() {
                    @Override
                    public ItemInfo apply(Cursor c) {
                        return new ItemInfo(
                                c.getString(0),
                                c.getString(1),
                                _decimalQty(c, 2),
                                c.getString(3),
                                c.getInt(4),
                                c.getString(5));
                    }
                }).toImmutableList();

        /** load modifiers **/
        Cursor c = null;
        for (ItemInfo item : items) {
            c = ProviderAction
                    .query(URI_MODIFIERS)
                    .projection(ShopSchema2.SaleAddonView2.SaleAddonTable.TYPE, ShopSchema2.SaleAddonView2.ModifierTable.TITLE, ShopSchema2.SaleAddonView2.ModifierTable.ITEM_GROUP_GUID)
                    .where(ShopSchema2.SaleAddonView2.SaleAddonTable.ITEM_GUID + " = ?", item.guid)
                    .perform(getContext());
            ArrayList<SimpleModifier> modifiers = new ArrayList<>();
            while (c.moveToNext()) {
                ModifierType type = _modifierType(c, 0);
                String title = c.getString(1);
                String groupGuid = c.getString(2);
                if (type == ModifierType.MODIFIER) {
                    if(TextUtils.isEmpty(groupGuid)){
                        modifiers.add(new SimpleModifier(title, null));
                        continue;
                    }
                    Cursor cursor = ProviderAction.query((URI_MODIFIERS_GROUP))
                            .projection(ShopStore.ModifierGroupTable.TITLE)
                            .where(ShopStore.ModifierGroupTable.GUID + " = ?", groupGuid)
                            .perform(getContext());
                    cursor.moveToFirst();
                    modifiers.add(new SimpleModifier(title, cursor.getString(0)));
                    cursor.close();
                } else if (type == ModifierType.ADDON) {
                    item.addons.add(title);
                } else if (type == ModifierType.OPTIONAL) {
                    item.options.add(title);
                }
            }
            Collections.sort(modifiers);
            for(SimpleModifier mod: modifiers)
                item.modifier.add(mod.title);
        }
        if (c != null)
            c.close();

        return items;
    }

    class SimpleModifier implements Comparable<SimpleModifier>{
        String title;
        String groupTitle;

        public SimpleModifier(String title, String groupTitle){
            this.title = title;
            this.groupTitle = groupTitle;
        }

        @Override
        public int compareTo(SimpleModifier another) {
            return this.groupTitle.compareTo(another.groupTitle);
        }
    }

    public class ItemInfo {
        public String guid;
        public String description;
        public BigDecimal qty;
        public String itemGuid;
        public int kdsAliasGuid;
        public String notes;

        public ArrayList<String> modifier = new ArrayList<>();
        public ArrayList<String> addons = new ArrayList<String>();
        public ArrayList<String> options = new ArrayList<String>();

        private ItemInfo(String guid, String description, BigDecimal qty, String itemGuid, int kdsAliasGuid, String notes) {
            this.guid = guid;
            this.description = description;
            this.qty = qty;
            this.itemGuid = itemGuid;
            this.kdsAliasGuid = kdsAliasGuid;
            this.notes = notes;
        }
    }

    public static void start(Context context, String orderGuid, boolean isVoidOrder, BasePrintOrderToKdsCallback callback) {
        create(PrintOrderToKdsCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_KDS_STATUS, isVoidOrder)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BasePrintOrderToKdsCallback {

        @OnSuccess(PrintOrderToKdsCommand.class)
        public void onPrintSuccess() {
            onDigitalPrintSuccess();
        }

        @OnFailure(PrintOrderToKdsCommand.class)
        public void onPrintError(@Param(EXTRA_ERROR_KDS)
                                 KDSCommand.KDSError kdsError) {
            if(kdsError != null){
                if(kdsError == KDSCommand.KDSError.IP_NOT_FOUND){
                    onRouterNotConfigured();
                    return;
                }
                if(kdsError == KDSCommand.KDSError.NOT_CONFIGURED){
                    onKdsNotConfigured();
                    return;
                }
            }
            onDigitalPrintError(kdsError);
        }

        protected abstract void onDigitalPrintSuccess();

        protected abstract void onDigitalPrintError(KDSCommand.KDSError kdsError);

        protected abstract void onKdsNotConfigured();

        protected abstract void onRouterNotConfigured();
    }
}
