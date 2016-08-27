package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.InventoryHelper;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.inventory.AddItemCommand.AddItemResult;
import com.kaching123.tcr.function.NextProductCodeQuery;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.ItemFunction;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CursorUtil;
import com.kaching123.tcr.util.UnitUtil;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnStart;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by gdubina on 15/01/14.
 */
public class ImportInventoryCommand extends PublicGroundyTask {

    public enum ImportType {ALL, QTY, PRICE, DELETE}

    //    private static final Uri URI_ITEM = ShopProvider.contentUri(ItemTable.URI_CONTENT);
    private static final Uri URI_ITEM_WITH_IS_DELETED = ShopProvider.contentUri(ShopStore.ItemTableAllColumns.QUERY_NAME);
    private static final Uri URI_CATEGORY = ShopProvider.contentUri(CategoryTable.URI_CONTENT);
    private static final Uri URI_DEPARTMENT = ShopProvider.contentUri(DepartmentTable.URI_CONTENT);

    private static final String ARG_FILENAME = "ARG_FILENAME";
    private static final String ARG_TYPE = "ARG_TYPE";

    private static final String RESULT_COUNT = "RESULT_COUNT";

    private static final String ERROR_INVALID_DATA = "INVALID_DATA";
    private static final String ERROR_EXTRA_DESCRIPTION = "ERROR_EXTRA_DESCRIPTION";
    private static final String ERROR_EXTRA_PRODUCT_CODE = "ERROR_EXTRA_PRODUCT_CODE";

    private static final String ERROR_MAX_ITEMS_COUNT = "ERROR_MAX_ITEMS_COUNT";

    private static final String MSG_EXTRA_DESCRIPTION = "MSG_EXTRA_DESCRIPTION";
    private static final String MSG_EXTRA_PRODUCT_GUID = "MSG_EXTRA_PRODUCT_GUID";
    private static final String MSG_IS_DELETED_ITEMS = "MSG_IS_DELETED_ITEMS";

    @Override
    protected TaskResult doInBackground() {
        String fileName = getStringArg(ARG_FILENAME);
        ImportType type = (ImportType) getArgs().getSerializable(ARG_TYPE);


        ICsvListReader listReader = null;
        try {
            listReader = new CsvListReader(new FileReader(fileName), CsvPreference.STANDARD_PREFERENCE);
            listReader.getHeader(true); // skip the header (can't be used with CsvListReader)

            final CellProcessor[] processors = getProcessors(ImportType.ALL);
            int count = 0;

            switch (type) {
                case ALL:
                    count = importAllRecords(listReader, processors, getAppCommandContext());
                    break;
                case PRICE:
                    count = updatePriceData(listReader, processors);
                    break;
                case QTY:
                    count = updateQtyData(listReader, processors);
                    break;
                case DELETE:
                    count = deleteRecords(listReader, processors);
                    break;
            }
            return succeeded().add(RESULT_COUNT, count);
        } catch (IOException e) {
            Logger.e("[IMPORT] Import exception", e);
        } finally {
            if (listReader != null) {
                try {
                    listReader.close();
                } catch (IOException e) {
                    Logger.e("[IMPORT] close file exception", e);
                }
            }
        }
        return failed();
    }

    private int importAllRecords(final ICsvListReader listReader, final CellProcessor[] processors, IAppCommandContext appCommandContext) throws IOException {
        HashMap<String, String> departments = readDepartments();
        HashMap<String, HashMap<String, String>> categoriesByDepartments = readCategoriesByDepartments();
        AddDepartmentCommand addDepartmentCommand = new AddDepartmentCommand();
        AddCategoryCommand addCategoryCommand = new AddCategoryCommand();
        AddItemCommand addItemCommand = new AddItemCommand();
        EditItemCommand editItemCommand = new EditItemCommand();


        List<Object> fields;
        int count = 0;

        while ((fields = listReader.read(processors)) != null) {
            ItemModel item = readItem(departments,
                    categoriesByDepartments,
                    addDepartmentCommand,
                    addCategoryCommand,
                    fields);
            if (item == null) {
                Logger.d("[IMPORT] Can't read row #%d. Item is null. Continue", count);
                continue;
            }

            if (!isEanValid(item)) {
                Logger.d("[IMPORT] fireWrongEan %s", item);
                fireInvalidData(item);
                continue;
            }
            ItemModel oldModel;
            if ((oldModel = findGuid(item)) != null) {
                copyOldToNew(oldModel, item);
                if (oldModel.isDeleted()) {
                    collectItemWithIsDeletedFlag(item);

                    //update deleted items could be done from callback in InventoryActivity of from here
                    //if we need to run update here uncomment and delete call from InventoryActivity
                    /*
                    UpdateDeletedItemCommand updateDeletedItemCommand = new UpdateDeletedItemCommand();
                    int result = updateDeletedItemCommand.sync(getContext(), item.getGuid(), getAppCommandContext());
                    if (result == 0) {
                        fireInvalidData(item.description, null);
                    }
                        else {
                        count += result;
                    }*/
                    continue;
                } else if (!editItemCommand.sync(getContext(), item, appCommandContext)) {
                    Logger.d("[IMPORT] Can't update item %s", item);
                    fireInvalidData(item);
                    continue;

                }
            } else if (findDuplicateProductCode(item) != null) {
                Logger.d("[IMPORT] fireDuplicateProductCode %s, %s", item, item.guid);
                fireInvalidData(item);
                continue;
            } else {
                item.guid = UUID.randomUUID().toString();// generate new GUID to prevent duplicates
                AddItemResult addItemResult = addItemCommand.sync(getContext(), item, appCommandContext);
                if (!addItemResult.isSuccess) {
                    Logger.d("[IMPORT] Can't create item %s", item);
                    if (addItemResult.isMaxItemsCountError) {
                        Logger.d("[IMPORT] Max items count reached!");
                        fireMaxItemsCountError();
                        break;
                    }
                    fireInvalidData(item);
                    continue;
                }
            }
            count++;
            Logger.d("[IMPORT] Add item %s", item);
            if(InventoryHelper.isLimited() && count >= InventoryHelper.getLimit()) {
                fireMaxItemsCountError();
                break;
            }
        }
        return count;
    }

    private void copyOldToNew(ItemModel oldModel, ItemModel item) {
        item.updateQtyFlag = oldModel.updateQtyFlag;
        item.isActiveStatus = oldModel.isActiveStatus;
        item.taxGroupGuid = oldModel.taxGroupGuid;
        item.orderNum = oldModel.orderNum;
        item.printerAliasGuid = oldModel.printerAliasGuid;
    }

    private void fireInvalidData(ItemModel item) {
        fireInvalidData(item.description, item.productCode/*eanCode*/);
    }

    private void fireInvalidData(String description, String productCode) {
        Bundle args = new Bundle(2);
        args.putString(ERROR_EXTRA_DESCRIPTION, description);
        args.putString(ERROR_EXTRA_PRODUCT_CODE, productCode);
        callback(ERROR_INVALID_DATA, args);
    }

    private void collectItemWithIsDeletedFlag(ItemModel item) {
        Bundle args = new Bundle(2);
        args.putString(MSG_EXTRA_DESCRIPTION, item.description);
        args.putString(MSG_EXTRA_PRODUCT_GUID, item.getGuid());
        callback(MSG_IS_DELETED_ITEMS, args);
    }

    private void fireMaxItemsCountError() {
        callback(ERROR_MAX_ITEMS_COUNT);
    }

    private boolean isEanValid(ItemModel model) {
        if (TextUtils.isEmpty(model.productCode/*eanCode*/)) {
            return true;
        }
        if (model.productCode/*eanCode*/.length() < TcrApplication.BARCODE_MIN_LEN || model.productCode/*eanCode*/.length() > TcrApplication.BARCODE_MAX_LEN) {
            return false;
        }

        if (!TextUtils.isDigitsOnly(model.productCode/*eanCode*/)) {
            return false;
        }
        return true;
    }

    private ItemModel findDuplicateEan(ItemModel model) {
        if (TextUtils.isEmpty(model.productCode/*eanCode*/)) {
            return null;
        }
        Cursor c = ProviderAction
                .query(URI_ITEM_WITH_IS_DELETED)
                        //.where(ItemTable.EAN_CODE + " = ?", model.eanCode)
                .where(ItemTable.PRODUCT_CODE + " = ?", model.productCode)
                .perform(getContext());
        ItemModel existModel = null;
        if (c.moveToFirst()) {
            existModel = new ItemFunction().apply(c);
        }
        c.close();
        return existModel;
    }

    private ItemModel findDuplicateProductCode(ItemModel model) {
        if (TextUtils.isEmpty(model.productCode/*eanCode*/)) {
            return null;
        }
        Cursor c = ProviderAction
                .query(URI_ITEM_WITH_IS_DELETED)
                .where(ItemTable.PRODUCT_CODE + " = ?", model.productCode)
                .perform(getContext());
        ItemModel existModel = null;
        if (c.moveToFirst()) {
            existModel = new ItemFunction().apply(c);
        }
        c.close();
        return existModel;
    }


    private ItemModel findGuid(ItemModel model) {
        if (TextUtils.isEmpty(model.guid)) {
            return null;
        }
        Cursor c = ProviderAction
                .query(URI_ITEM_WITH_IS_DELETED)
                .where(ItemTable.GUID + " = ?", model.guid)
                .perform(getContext());
        ItemModel existModel = null;
        if (c.moveToFirst()) {
            existModel = new ItemFunction().apply(c);
            if (existModel != null) {
                existModel.setIsDeleted(c.getInt(c.getColumnIndex(ItemTable.IS_DELETED)) == 1);
            }
        }
        c.close();
        return existModel;
    }

    private int updatePriceData(final ICsvListReader listReader, final CellProcessor[] processors) throws IOException {
        UpdateItemPriceCommand updatePriceCommand = new UpdateItemPriceCommand();

        List<Object> fields;
        int count = 0;
        while ((fields = listReader.read(processors)) != null) {
            String guid = (String) fields.get(FIELD_GUID);
            String description = (String) fields.get(FIELD_DESCRIPTION);
            if (TextUtils.isEmpty(guid)) {
                Logger.d("[IMPORT] empty updatePriceData skipped");
                fireInvalidData(description, null);
                continue;
            }
            BigDecimal value = (BigDecimal) fields.get(FIELD_PRICE);
            Logger.d("[IMPORT] updatePriceData %s = %s", guid, value);
            int result = updatePriceCommand.sync(getContext(), guid, value, getAppCommandContext());
            if (result == 0)
                fireInvalidData(description, null);
            else
                count += result;
        }
        return count;
    }

    /*
    *     private int importAllRecords(final ICsvListReader listReader, final CellProcessor[] processors, IAppCommandContext appCommandContext) throws IOException {
            HashMap<String, String> departments = readDepartments();
            HashMap<String, HashMap<String, String>> categoriesByDepartments = readCategoriesByDepartments();
            AddDepartmentCommand addDepartmentCommand = new AddDepartmentCommand();
            AddCategoryCommand addCategoryCommand = new AddCategoryCommand();
            AddItemCommand addItemCommand = new AddItemCommand();
            EditItemCommand editItemCommand = new EditItemCommand();

            List<Object> fields;
            int count = 0;

            while ((fields = listReader.read(processors)) != null) {
    */
    private int updateQtyData(final ICsvListReader listReader, final CellProcessor[] processors) throws IOException {
        UpdateItemQtyCommand updateItemQtyCommand = new UpdateItemQtyCommand();

        List<Object> fields;
        int count = 0;
        while ((fields = listReader.read(processors)) != null) {
            String guid = (String) fields.get(FIELD_GUID);
            String description = (String) fields.get(FIELD_DESCRIPTION);
            if (TextUtils.isEmpty(guid)) {
                Logger.d("[IMPORT] empty updateQtyData skipped");
                fireInvalidData(description, null);
                continue;
            }
            BigDecimal value = (BigDecimal) fields.get(FIELD_QTY);
            if (value == null)
                value = BigDecimal.ZERO;
            Logger.d("[IMPORT] updateQtyData %s = %s", guid, value);
            Integer result = updateItemQtyCommand.sync(getContext(), guid, value, getAppCommandContext());
            if (result == null)
                fireInvalidData(description, null);
            else
                count += result;
        }
        return count;
    }

    private ItemModel getItemIfNonStockable(String guid) {
        ItemModel item = null;

        Cursor c = ProviderAction.query(URI_ITEM_WITH_IS_DELETED)
                .projection(ItemTable.EAN_CODE, ItemTable.PRODUCT_CODE, ItemTable.DESCRIPTION)
                .where(ItemTable.GUID + " = ?", guid)
                .where("(" + ItemTable.STOCK_TRACKING + " IS NULL OR " + ItemTable.STOCK_TRACKING + " = 0)")
                .perform(getContext());

        if (c.moveToFirst()) {
            item = new ItemModel();
            item.eanCode = c.getString(0);
            item.productCode = c.getString(1);
            item.description = c.getString(2);
        }

        c.close();
        return item;
    }

    private ItemModel readItem(HashMap<String, String> departments,
                               HashMap<String, HashMap<String, String>> categoriesByDepartments,
                               AddDepartmentCommand addDepartmentCommand,
                               AddCategoryCommand addCategoryCommand,
                               List<Object> fields) {
        String description = (String) fields.get(FIELD_DESCRIPTION);
        String eanCode = (String) fields.get(FIELD_UPC);
        String productCode = (String) fields.get(FIELD_PRODUCT_CODE);
        BigDecimal price = fields.get(FIELD_PRICE) == null ? new BigDecimal(0) : (BigDecimal) fields.get(FIELD_PRICE);
        //TODO: validate if not const
        PriceType priceType = price == null || BigDecimal.ZERO.compareTo(price) == 0 ? PriceType.OPEN : PriceType.FIXED;
        String departmentName = (String) fields.get(FIELD_DEPARTMENT);
        String categoryName = (String) fields.get(FIELD_CATEGORY);

        if (!validateItemFields(description, price, priceType, departmentName, categoryName)) {
            fireInvalidData(description, productCode);
            return null;
        }

        if (!validatePrice(price, priceType)) {
            fireInvalidData(description, productCode);
            return null;
        }

        //read department
        String departmentGuid = departments.get(departmentName);
        if (departmentGuid == null) {
            departmentGuid = addDepartmentCommand.sync(getContext(), departmentName, getAppCommandContext());
            if (departmentGuid == null) {
                Logger.d("[IMPORT] Can't create department %s", departmentName);
                fireInvalidData(description, productCode);
                return null;
            }
            departments.put(departmentName, departmentGuid);
        }
        //read category
        HashMap<String, String> categories = categoriesByDepartments.get(departmentGuid);
        if (categories == null) {
            categoriesByDepartments.put(departmentGuid, categories = new HashMap<String, String>());
        }
        String categoryGuid = categories.get(categoryName);
        if (categoryGuid == null) {
            categoryGuid = addCategoryCommand.sync(getContext(), departmentGuid, categoryName, getAppCommandContext());
            if (categoryGuid == null) {
                Logger.d("[IMPORT] Can't create category %s", categoryName);
                fireInvalidData(description, productCode);
                return null;
            }
            categories.put(categoryName, categoryGuid);
        }

        // Read Unit Label
        String oldUnitLabel = (String) fields.get(FIELD_UNITS_LABEL);
        String unitLabelId = null;
        UnitLabelModel unitLabelModel;
        if (!TextUtils.isEmpty(oldUnitLabel)) { // if not empty
            if (!UnitUtil.isContainInvalidChar(oldUnitLabel) || oldUnitLabel.length() > UnitUtil.MAX_LENGTH) { // if valid imported UL
                unitLabelModel = UnitLabelModel.getByShortcut(getContext(), oldUnitLabel);

                if (unitLabelModel != null) { // if found
                    unitLabelId = unitLabelModel.guid;
                    oldUnitLabel = null;
                } else if (TextUtils.equals(oldUnitLabel, TcrApplication.get().getShopInfo().defUnitLabelShortcut)) {
                    oldUnitLabel = null;
                } else { // if not found
                    fireInvalidData(description, productCode);
                    Logger.d("[IMPORT] Can't found unit label: %s", oldUnitLabel);
                }
            } else { // if not valid imported UL
                fireInvalidData(description, productCode);
                Logger.d("[IMPORT] Unit label contains invalid chars: %s", oldUnitLabel);
            }
        } else { // if imported UL was empty
            oldUnitLabel = TcrApplication.get().getShopInfo().defUnitLabelShortcut;
        }

        ItemRefType itemRefType = ItemRefType.valueOf(((Boolean) fields.get(FIELD_IS_REFERENCE) ? 1 : 0));
        //create model
        String guid = (String) fields.get(FIELD_GUID);
        if (guid == null) {
            guid = UUID.randomUUID().toString();
        }

        if (itemRefType == ItemRefType.Simple) {
            if (TextUtils.isEmpty(productCode)) {
                productCode = NextProductCodeQuery.getCode(getContext());
            } else {
                if (productCode.length() < 5 && isInteger(productCode)) {
                    productCode = NextProductCodeQuery.format(Integer.valueOf(productCode));
                }
            }
        }

        String referenceItemGuid = null;
        if (itemRefType != ItemRefType.Reference) {
            String parentThroughItemMatrixGuid = parentThroughItemMatrix(guid);
            Object directParentGuid = fields.get(FIELD_REFERENCE_ITEM);
            referenceItemGuid = parentThroughItemMatrixGuid != null
                    || directParentGuid == null ? null : (String) fields.get(FIELD_REFERENCE_ITEM);
            if (directParentGuid != null && parentThroughItemMatrixGuid != null && !directParentGuid.equals(parentThroughItemMatrixGuid)) {
                deleteItemFromItemMatrixChild(guid);
                referenceItemGuid = (String) directParentGuid;
            }
        }

        BigDecimal quantity = (BigDecimal) fields.get(FIELD_QTY);
        if (quantity == null)
            quantity = BigDecimal.ZERO;
        ItemModel item = new ItemModel(
                guid,
                categoryGuid,
                description,
                null,
                eanCode,
                productCode,
                priceType,
                price,
                null,
                null,
                null,
                null,
                null,
                quantity,
                unitLabelId,
                (Boolean) fields.get(FIELD_STOCK_TRACKING),
                true,
                (Boolean) fields.get(FIELD_DISCOUNTABLE),
                (Boolean) fields.get(FIELD_SALABLE),
                BigDecimal.ZERO,
                DiscountType.PERCENT,
                (Boolean) fields.get(FIELD_TAXABLE),
                (BigDecimal) fields.get(FIELD_COST),
                (BigDecimal) fields.get(FIELD_ORDER_TRIGGER),
                (BigDecimal) fields.get(FIELD_RECOMMENDED),
                null,
                null,
                null,
                0,
                null,
                0,
                false,
                false,
                null,
                true,
                null, referenceItemGuid, itemRefType, null, false);
        return item;
    }

    private boolean isInteger(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (Character.digit(s.charAt(i), 10) < 0) return false;
        }
        return true;
    }

    private String parentThroughItemMatrix(String guid) {
        Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT)).where(ShopStore.ItemMatrixTable.CHILD_GUID + "=?", guid).perform(getContext());
        String parentGuid = null;
        if (c.moveToFirst()) {
            parentGuid = c.getString(c.getColumnIndex(ShopStore.ItemMatrixTable.PARENT_GUID));
        }
        c.close();
        return parentGuid;
    }

    private void deleteItemFromItemMatrixChild(String guid) {
        Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT)).where(ShopStore.ItemMatrixTable.CHILD_GUID + "=?", guid).perform(getContext());
        if (c.moveToFirst()) {
            ItemMatrixModel itemMatrixModel = CursorUtil._wrap(c, new ItemMatrixFunction());
            itemMatrixModel.childItemGuid = null;
            new EditVariantMatrixItemCommand().sync(getContext(), itemMatrixModel, getAppCommandContext());

        }
        c.close();
    }

    private boolean validateItemFields(String description, BigDecimal price, PriceType priceType, String departmentName, String categoryName) {
        return !TextUtils.isEmpty(description) && price != null && !TextUtils.isEmpty(departmentName) && !TextUtils.isEmpty(categoryName);
    }

    private boolean validatePrice(BigDecimal price, PriceType priceType) {
        return priceType != PriceType.FIXED || (BigDecimal.ZERO.compareTo(price) != 0);
    }

    private HashMap<String, String> readDepartments() {
        Cursor c = ProviderAction.query(URI_DEPARTMENT)
                .projection(DepartmentTable.TITLE, DepartmentTable.GUID)
                .perform(getContext());

        HashMap<String, String> result = new HashMap<String, String>();
        while (c.moveToNext()) {
            result.put(c.getString(0), c.getString(1));
        }
        c.close();
        return result;
    }

    private HashMap<String, HashMap<String, String>> readCategoriesByDepartments() {
        Cursor c = ProviderAction.query(URI_CATEGORY)
                .projection(CategoryTable.DEPARTMENT_GUID, CategoryTable.TITLE, CategoryTable.GUID)
                .perform(getContext());

        HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
        while (c.moveToNext()) {
            String department = c.getString(0);
            HashMap<String, String> categories = result.get(department);
            if (categories == null) {
                result.put(department, categories = new HashMap<String, String>());
            }
            categories.put(c.getString(1), c.getString(2));
        }
        c.close();
        return result;
    }

    private static final int BATCH_SIZE = 500;

    private int deleteRecords(ICsvListReader listReader, CellProcessor[] processors) throws IOException {
        int count = 0;
        List<Object> fields;

        ArrayList<String> items = new ArrayList<String>(BATCH_SIZE);
        Context context = getContext();
        while ((fields = listReader.read(processors)) != null) {
            String itemGuid = (String) fields.get(0);
            if (TextUtils.isEmpty(itemGuid))
                continue;
            items.add(itemGuid);
            if (items.size() == BATCH_SIZE) {
                if (new BatchDeleteItemsCommand().sync(context, items, getAppCommandContext())) {
                    count += items.size();
                    items.clear();
                }
            }
        }

        //for tail
        if (items.size() != 0) {
            if (new BatchDeleteItemsCommand().sync(context, items, getAppCommandContext())) {
                count += items.size();
                items.clear();
            }
        }
        return count;
    }

    public static void start(Context context, ImportType type, String fileName, BaseImportCommandCallback callback) {
        create(ImportInventoryCommand.class).arg(ARG_TYPE, type).arg(ARG_FILENAME, fileName).callback(callback).queueUsing(context);
    }

    private static final int FIELD_GUID = 0;
    private static final int FIELD_DESCRIPTION = 1;
    private static final int FIELD_DEPARTMENT = 2;
    private static final int FIELD_CATEGORY = 3;
    private static final int FIELD_UNITS_LABEL = 4;
    private static final int FIELD_UPC = 5;
    private static final int FIELD_PRODUCT_CODE = 6;
    private static final int FIELD_PRICE = 7;
    private static final int FIELD_REFERENCE_ITEM = 8;
    private static final int FIELD_DISCOUNTABLE = 9;
    private static final int FIELD_SALABLE = 10;
    private static final int FIELD_TAXABLE = 11;
    private static final int FIELD_STOCK_TRACKING = 12;
    private static final int FIELD_COST = 13;
    private static final int FIELD_QTY = 14;
    private static final int FIELD_ORDER_TRIGGER = 15;
    private static final int FIELD_RECOMMENDED = 16;
    private static final int FIELD_IS_REFERENCE = 17;
    //private static final int FIELD_LAST_SOLD_DATE = 18;

    private static CellProcessor[] getProcessors(ImportType type) {
        if (type == ImportType.DELETE) {
            return new CellProcessor[]{
                    new Optional(), //Item Id
                    new Optional() // description
            };
        }
        if (type == ImportType.ALL) {
            return new CellProcessor[]{
                    new Optional(), //store code
                    new Optional(), // description
                    new Optional(), // department
                    new Optional(), // category
                    new Optional(), // units label
                    new Optional(), // upc code
                    new Optional(), // product code
                    new Optional(new ParseBigDecimal()),//price
                    new Optional(), // reference item
                    new Optional(new ParseBool()),//discountable
                    new Optional(new ParseBool()),//salable
                    new Optional(new ParseBool()),//taxable
                    new Optional(new ParseBool()),//stock track
                    new Optional(new ParseBigDecimal()),//cost
                    new Optional(new ParseBigDecimal()),//qty on hand
                    new Optional(new ParseBigDecimal()),//order trigger
                    new Optional(new ParseBigDecimal()),//recommended order
                    new Optional(new ParseBool()),//is reference
                    new Optional()//last sold Data
            };
        } else {
            return new CellProcessor[]{
                    new Optional(),//guid
                    new Optional(),//description
                    new Optional(new ParseBigDecimal()),//decimal
            };
        }
    }

    public static abstract class BaseImportCommandCallback {

        @OnStart(ImportInventoryCommand.class)
        public void onStart() {
            handleStart();
        }

        @OnSuccess(ImportInventoryCommand.class)
        public void onSuccess(@Param(RESULT_COUNT) int count) {
            handleSuccess(count);
        }

        @OnFailure(ImportInventoryCommand.class)
        public void onFailure() {
            handleFailure();
        }

        @OnCallback(value = ImportInventoryCommand.class, name = ERROR_INVALID_DATA)
        public void onErrorInvalidData(@Param(ERROR_EXTRA_DESCRIPTION) String description, @Param(ERROR_EXTRA_PRODUCT_CODE) String productCode) {
            handleInvalidData(new WrongImportInfo(description, productCode));
        }

        @OnCallback(value = ImportInventoryCommand.class, name = ERROR_MAX_ITEMS_COUNT)
        public void onErrorMaxItemsCount() {
            handleMaxItemsCountError();
        }

        @OnCallback(value = ImportInventoryCommand.class, name = MSG_IS_DELETED_ITEMS)
        public void onIsDeletedItemFound(@Param(MSG_EXTRA_DESCRIPTION) String description, @Param(MSG_EXTRA_PRODUCT_GUID) String productGuid) {
            handleIsDeletedItemFound(new WrongImportInfo(description, productGuid));
        }

        protected abstract void handleStart();

        protected abstract void handleInvalidData(WrongImportInfo info);

        protected abstract void handleSuccess(int count);

        protected abstract void handleFailure();

        protected abstract void handleMaxItemsCountError();

        protected abstract void handleIsDeletedItemFound(final WrongImportInfo info);
    }

    public static class WrongImportInfo {
        public final String description;
        public final String productCode;

        private WrongImportInfo(String description, String productCode) {
            this.description = description;
            this.productCode = productCode;
        }
    }

    private static class UpdateData {
        String guid;
        BigDecimal value;

        private UpdateData(String guid, BigDecimal value) {
            this.guid = guid;
            this.value = value;
        }
    }
}
