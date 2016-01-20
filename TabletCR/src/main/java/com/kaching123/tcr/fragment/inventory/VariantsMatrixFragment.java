package com.kaching123.tcr.fragment.inventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.InventoryHelper;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddItemCommand;
import com.kaching123.tcr.commands.store.inventory.AddVariantMatrixItemsCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteVariantMatrixItemsCommand;
import com.kaching123.tcr.commands.store.inventory.EditVariantMatrixItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditVariantMatrixItemsCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.function.NextProductCodeQuery;
import com.kaching123.tcr.function.VariantSubItemWrapFunction;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;
import com.kaching123.tcr.util.CursorUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by aakimov on 28/04/15.
 */
@EFragment(R.layout.variants_matrix_fragment)
@OptionsMenu(R.menu.variants_matrix_fragment)
public class VariantsMatrixFragment extends Fragment {

    private final static String FTAG = VariantsMatrixFragment.class.getName();
    private static final Uri URI_VARIANT_MATRIX = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);
    private static final Uri URI_ITEM_MATRIX_VIEW = ShopProvider.contentUri(ShopStore.ItemMatrixByChildView.URI_CONTENT);
    private static final Uri URI_VARIANT_ITEMS = ShopProvider.contentUri(VariantItemTable.URI_CONTENT);
    private static final Uri URI_VARIANT_SUB_ITEMS = ShopProvider.contentUri(VariantSubItemTable.URI_CONTENT);
    private final static int LOADER_TAG = 0x00000010;

    @FragmentArg
    ItemExModel model;

    @OptionsMenuItem
    MenuItem actionPopulate;

    @ViewById(android.R.id.empty)
    protected TextView emptyText;

    @ViewById(R.id.variants_matrix)
    protected ListView variantsMatrixListView;

    protected VariantMatrixAdapter adapter;

    @AfterViews
    protected void init() {
        variantsMatrixListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        variantsMatrixListView.setMultiChoiceModeListener(multiChoiceModeListener);
        variantsMatrixListView.setAdapter(adapter = new VariantMatrixAdapter(getActivity()));
        getLoaderManager().restartLoader(LOADER_TAG, Bundle.EMPTY, loaderCallbacks);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        buildVariantMatrix();
    }

    protected void checkMatrixSizeAndPopulation() {
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.PARENT_GUID + "=?", model.guid)
                .perform(getActivity());
        if (!c.moveToFirst() || !hasEmptyChildId(c)) {
            displayPopulateAction(false);
        } else {
            displayPopulateAction(true);
        }
        c.close();
    }

    protected boolean hasChildId(Cursor c) {
        boolean isPopulated = false;
        if (c.moveToFirst()) {
            int childGuidIdx = c.getColumnIndex(ShopStore.ItemMatrixTable.CHILD_GUID);
            do {
                if (!c.isNull(childGuidIdx)) {
                    isPopulated = true;
                }
            } while (c.moveToNext());
        }
        return isPopulated;
    }

    protected boolean hasEmptyChildId(Cursor c) {
        boolean hasEmpty = false;
        if (c.moveToFirst()) {
            int childGuidIdx = c.getColumnIndex(ShopStore.ItemMatrixTable.CHILD_GUID);
            do {
                if (c.isNull(childGuidIdx)) {
                    hasEmpty = true;
                    break;
                }
            } while (c.moveToNext());
        }
        return hasEmpty;
    }

    @UiThread
    protected void displayPopulateAction(boolean visible) {
        if (actionPopulate != null) {
            actionPopulate.setVisible(visible);
        }
    }

    @OptionsItem
    protected void actionPopulateSelected() {
        if (!limitIsExceeded()) {
            populate();
        } else {
            AlertDialogFragment.showAlert(getActivity(), R.string.warning_dialog_title,
                    getString(R.string.warning_inventory_matrix_limit));
        }
    }

    private boolean limitIsExceeded() {
        if (InventoryHelper.isLimited()) {
            int currentCount = (int) InventoryHelper.getLimitedItemsCount(getContext());
            Logger.d("[Inventory] current non-ref items count: %d", currentCount);
            int maxMatrixCount = model.getMaxMatrixCount(getContext());
            if (maxMatrixCount + currentCount > InventoryHelper.getLimit()) {
                return true;
            }
        }
        return false;
    }

    @Background
    protected void buildVariantMatrix() {

        Cursor c = ProviderAction.query(URI_VARIANT_ITEMS)
                .where(ShopStore.VariantItemTable.ITEM_GUID + "=?", model.guid)
                .perform(getActivity());
        Cursor matrixCursor = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.PARENT_GUID + "=?", model.guid)
                .perform(getActivity());
        boolean isPopulated = hasChildId(matrixCursor);
        if (c.getCount() > 0 && matrixCursor.getCount() == 0 && !isPopulated) {
            List<List<VariantSubItemModel>> variants = new ArrayList<>(c.getCount());
            if (c.moveToFirst()) {
                do {
                    Cursor subCursor = ProviderAction.query(URI_VARIANT_SUB_ITEMS)
                            .where(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID + "=?",
                                    c.getString(c.getColumnIndex(ShopStore.VariantItemTable.GUID)))
                            .perform(getActivity());
                    List<VariantSubItemModel> subItems = CursorUtil
                            ._wrap(subCursor, new VariantSubItemWrapFunction());
                    if (subItems != null && subItems.size() > 0) {
                        variants.add(subItems);
                    }
                } while (c.moveToNext());
            }
            List<String> variantMatrix = new ArrayList<>();
            if (variants.size() > 0) {
                build(variants, 0, null, variantMatrix);
            }

            ArrayList<ItemMatrixModel> models = new ArrayList<>(variantMatrix.size());
            for (int i = 0; i < variantMatrix.size(); i++) {
                String itemMatrixName = variantMatrix.get(i);
                models.add(new ItemMatrixModel(UUID.randomUUID().toString(), itemMatrixName,
                        model.guid, null));
            }
            callAddItemMatrixCommand(models);
        }
        c.close();
        matrixCursor.close();
        checkMatrixSizeAndPopulation();

    }

    @UiThread
    protected void updateMatrixCommand(ArrayList<ItemMatrixModel> models) {
        EditVariantMatrixItemsCommand.start(getActivity(), models);
    }

    @Background
    protected void populate() {
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.PARENT_GUID + "= ? AND "
                        + ShopStore.ItemMatrixTable.CHILD_GUID + " IS NULL", model.guid)
                .perform(getActivity());
        if (c.moveToFirst()) {
            int matrixItemNameIdx = c.getColumnIndex(ShopStore.ItemMatrixTable.NAME);
            ArrayList<ItemMatrixModel> models = new ArrayList<>(c.getCount());
            String[] productCodes = NextProductCodeQuery.getCodes(getActivity(), c.getCount());
            int pcPos = 0;
            do {
                ItemMatrixModel itemMatrixModel = new ItemMatrixModel(c);
                ItemModel itemModel = new ItemModel(model);
                itemModel.guid = UUID.randomUUID().toString();
                itemModel.description = c.getString(matrixItemNameIdx);
                itemModel.refType = ItemRefType.Simple;
                itemModel.isSalable = true;
                itemModel.productCode = productCodes[pcPos];
                itemModel.eanCode = null;
                itemModel.isStockTracking = true;

                callAddItemCommand(itemModel);
                itemMatrixModel.childItemGuid = itemModel.guid;
                models.add(itemMatrixModel);
                pcPos++;
            } while (c.moveToNext());
            updateMatrixCommand(models);
        }
        c.close();
        getActivity().getContentResolver().notifyChange(URI_VARIANT_MATRIX, null);
        displayPopulateAction(false);
    }

    @UiThread
    protected void callAddItemCommand(ItemModel model) {
        AddItemCommand.start(getActivity(), model);
    }

    @UiThread
    protected void updateItemMatrixCommand(ItemMatrixModel model) {
        EditVariantMatrixItemCommand.start(getActivity(), model);
    }

    private WeakReference<ActionMode> actionModeRef = null;

    protected ActionMode getActionMode() {
        return null != actionModeRef ? actionModeRef.get() : null;
    }

    protected void setActionMode(final ActionMode mode) {
        actionModeRef = new WeakReference<>(mode);
    }

    @UiThread
    protected void configureActionItems(boolean isPopulated) {
        if (getActionMode() != null) {
            actionModeRef.get().getMenu().findItem(R.id.action_edit)
                    .setVisible(variantsMatrixListView.getCheckedItemIds().length == 1);
            actionModeRef.get().getMenu().findItem(R.id.action_clear).setVisible(isPopulated);
            actionModeRef.get().getMenu().findItem(R.id.action_delete).setVisible(!isPopulated);
        }
    }

    @Background
    protected void configureActionMode() {
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.PARENT_GUID + "=?", model.guid)
                .perform(getActivity());
        configureActionItems(hasChildId(c));
        c.close();
    }

    private final AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            configureActionMode();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.variant_matrix_actions, menu);
            setActionMode(mode);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteMatrixItems(toLongArray(variantsMatrixListView.getCheckedItemIds()));
                    mode.finish();
                    return true;
                case R.id.action_edit:
                    updateMatrixItem(variantsMatrixListView.getCheckedItemIds()[0]);
                    mode.finish();
                    return true;
                case R.id.action_clear:
                    ClearMatrixDialogFragment.show(getActivity(), VariantsMatrixFragment.this);
                    return true;
                case R.id.action_select_all:
                    for (int i = 0; i < variantsMatrixListView.getCount(); i++) {
                        variantsMatrixListView.setItemChecked(i, true);
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            setActionMode(null);
        }
    };

    private Long[] toLongArray(long[] longArray) {
        Long[] result = new Long[longArray.length];
        for (int i = 0; i < longArray.length; i++) {
            result[i] = longArray[i];
        }
        return result;
    }

    @Background
    protected void deleteMatrixItems(Long[] ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append('?');
        }
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.ID + " IN (" + sb.toString() + ")", ids)
                .perform(getActivity());

        if (c.moveToFirst()) {
            ArrayList<ItemMatrixModel> models = new ArrayList<>(c.getCount());
            do {
                models.add(new ItemMatrixModel(c));
            } while (c.moveToNext());
            callDeleteItemMatrixCommand(models);
        }
        c.close();
    }

    @Background
    protected void clearChildrenInMatrixItem() {
        Long[] ids = toLongArray(variantsMatrixListView.getCheckedItemIds());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append('?');
        }
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.ID + " IN (" + sb.toString() + ")", ids)
                .perform(getActivity());
        if (c.moveToFirst()) {
            ArrayList<ItemMatrixModel> models = new ArrayList<ItemMatrixModel>(c.getCount());
            do {
                ItemMatrixModel m = new ItemMatrixModel(c);
                m.childItemGuid = null;
                models.add(m);
            } while (c.moveToNext());
            callUpdateItemMatrixCommand(models);
        }
        c.close();
    }

    @Background
    protected void updateMatrixItem(long id) {
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX)
                .where(ShopStore.ItemMatrixTable.ID + "=?", id)
                .perform(getActivity());
        if (c.moveToFirst()) {
            changeChild(new ItemMatrixModel(c));
        }
        c.close();
    }

    @UiThread
    protected void changeChild(ItemMatrixModel model) {
        ChooseChildItemDialogFragment.show(getActivity(), model);
    }

    @UiThread
    protected void callAddItemMatrixCommand(ArrayList<ItemMatrixModel> models) {
        AddVariantMatrixItemsCommand.start(getActivity(), models);
    }

    @UiThread
    protected void callDeleteItemMatrixCommand(ArrayList<ItemMatrixModel> models) {
        DeleteVariantMatrixItemsCommand.start(getActivity(), models);
    }

    @UiThread
    protected void callUpdateItemMatrixCommand(ArrayList<ItemMatrixModel> models) {
        EditVariantMatrixItemsCommand.start(getActivity(), models);
        getActionMode().finish();

    }

    private final LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
            switch (loaderId) {
                case LOADER_TAG:
                    return CursorLoaderBuilder.forUri(URI_ITEM_MATRIX_VIEW)
                            .where(ShopStore.ItemMatrixByChildView.ITEM_MATRIX_PARENT_GUID + " = ?"
                                    , model.guid)
                            .build(getActivity());

                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loaderId));
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_TAG:
                    adapter.swapCursor(cursor);
                    emptyText.setVisibility(cursor.getCount() > 0 ? View.GONE : View.VISIBLE);
                    checkMatrixSizeAndPopulation();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loader.getId()));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            switch (loader.getId()) {
                case LOADER_TAG:
                    adapter.swapCursor(null);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loader.getId()));
            }
        }
    };

    public static void replace(FragmentManager manager, ItemExModel model) {
        Fragment f = VariantsMatrixFragment_.builder().model(model).build();
        manager.beginTransaction().replace(R.id.container, f, FTAG).commit();
    }

    protected void build(List<List<VariantSubItemModel>> list, int index, String s,
                         List<String> variantMatrix) {
        List<VariantSubItemModel> subs = list.get(index);
        String prevString = s;

        for (int k = 0; k < subs.size(); k++) {
            if (s == null) {
                s = model.description + " " + subs.get(k).name;
            } else {
                s = s + " " + subs.get(k).name;
            }
            if (index < list.size() - 1) {
                build(list, index + 1, s, variantMatrix);
            } else {
                variantMatrix.add(s);
            }
            s = prevString;
        }
    }


    private static class VariantMatrixAdapter extends ResourceCursorAdapter {
        private int varMatrixItemNameIdx, varitemChildDescriptionIdx;

        public VariantMatrixAdapter(Context context) {
            super(context, R.layout.variant_matrix_item, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            if (cursor != null) {
                varMatrixItemNameIdx = cursor.getColumnIndex(ShopStore.ItemMatrixByChildView.ITEM_MATRIX_NAME);
                varitemChildDescriptionIdx = cursor.getColumnIndex(ShopStore.ItemMatrixByChildView.ITEM_DESCRIPTION);
            }
            View view = super.newView(context, cursor, parent);
            view.setTag(new VariantMatrixHolder(view));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            VariantMatrixHolder variantMatrixHolder = (VariantMatrixHolder) view.getTag();
            variantMatrixHolder.name.setText(cursor.getString(varMatrixItemNameIdx));
            if (cursor.isNull(varitemChildDescriptionIdx)) {
                variantMatrixHolder.childDescription.setVisibility(View.INVISIBLE);
            } else {
                variantMatrixHolder.childDescription.setVisibility(View.VISIBLE);
                variantMatrixHolder.childDescription.setText(cursor.getString(varitemChildDescriptionIdx));
            }
        }

        private static class VariantMatrixHolder {
            TextView name;
            TextView childDescription;

            VariantMatrixHolder(View v) {
                name = (TextView) v.findViewById(android.R.id.text1);
                childDescription = (TextView) v.findViewById(android.R.id.text2);
            }
        }
    }
}
