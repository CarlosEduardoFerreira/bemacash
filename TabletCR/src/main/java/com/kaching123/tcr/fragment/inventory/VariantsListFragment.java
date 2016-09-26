package com.kaching123.tcr.fragment.inventory;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.adapter.VariantsExpandableAdapter;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

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

import static com.kaching123.tcr.util.CursorUtil._selectionArgs;


/**
 * Created by aakimov on 28/04/15.
 */
@EFragment(R.layout.variants_list_fragment)
@OptionsMenu(R.menu.variants_list_fragment)

public class VariantsListFragment extends Fragment {

    private final static String FTAG = VariantsListFragment.class.getName();
    private static final Uri URI_VARIANT_SUB_ITEMS_COUNT = ShopProvider.contentUri(ShopStore.VariantSubItemsCountView.URI_CONTENT);
    private static final Uri URI_VARIANT_SUB_ITEM = ShopProvider.contentUri(ShopStore.VariantSubItemTable.URI_CONTENT);
    private static final Uri URI_VARIANT_MATRIX = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);

    private final static int VARIANT_ITEMS_LOADER_TAG = 0x00000010;
    private final static int VARIANT_SUB_ITEM_LOADER_TAG = 0x00000020;
    private final static String VARIANT_SUB_ITEM_ID = "VARIANT_SUB_ITEM_ID";

    @FragmentArg
    ItemExModel model;

    @OptionsMenuItem
    MenuItem actionAddItem;

    @ViewById(R.id.variants_list)
    protected ExpandableListView expandableListView;

    @ViewById(android.R.id.empty)
    protected TextView emptyText;

    protected VariantsExpandableAdapter variantsExpandableAdapter;

    private WeakReference<ActionMode> actionModeRef = null;

    @AfterViews
    protected void init() {

        expandableListView.setAdapter(variantsExpandableAdapter = new VariantsExpandableAdapter(getActivity(), null));
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (getActionMode() != null) {
                    multiChoiceModeListener.onItemCheckedStateChanged(getActionMode(), childPosition, ExpandableListView.getPackedPositionForChild(groupPosition, childPosition), variantsExpandableAdapter.setChildChecked((int) id));
                    if (variantsExpandableAdapter.hasNoInSelection()) {
                        stopActionMode();
                    }
                    return true;
                }
                return false;
            }
        });
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (variantsExpandableAdapter.hasNoInSelection()) {
                    setActionMode(getActivity().startActionMode(multiChoiceModeListener));
                }
                boolean handled = false;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    long packedPos = ((ExpandableListView) parent).getExpandableListPosition(position);
                    int childPos = ExpandableListView.getPackedPositionChild(packedPos);
                    int groupPos = ExpandableListView.getPackedPositionGroup(packedPos);
                    multiChoiceModeListener.onItemCheckedStateChanged(getActionMode(), childPos, id, variantsExpandableAdapter.setChildChecked((int) variantsExpandableAdapter.getChildId(groupPos, childPos)));
                    handled = true;
                } else if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    long packedPos = ((ExpandableListView) parent).getExpandableListPosition(position);
                    int groupPos = ExpandableListView.getPackedPositionGroup(packedPos);
                    multiChoiceModeListener.onItemCheckedStateChanged(getActionMode(), groupPos, id, variantsExpandableAdapter.setGroupChecked((int) variantsExpandableAdapter.getGroupId(groupPos)));
                    handled = true;
                }
                if (variantsExpandableAdapter.hasNoInSelection()) {
                    stopActionMode();
                }
                return handled;
            }
        });
        getLoaderManager().restartLoader(VARIANT_ITEMS_LOADER_TAG, Bundle.EMPTY, loaderCallbacks);
    }

    @OptionsItem
    protected void actionAddItemSelected() {
        VariantItemDialogFragment.showToAdd(getActivity(), model);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (variantsExpandableAdapter != null && variantsExpandableAdapter.getCursor() != null) {
            actionAddItem = menu.findItem(R.id.action_add_item);
            checkIfHasMatrix();
        }
    }

    @Background
    protected void checkIfHasMatrix() {
        Cursor c = ProviderAction.query(URI_VARIANT_MATRIX).where(ShopStore.ItemMatrixTable.PARENT_GUID + "=?", model.guid).perform(getActivity());
        allowToEdit(!c.moveToFirst());
        c.close();
    }

    @UiThread
    protected void allowToEdit(boolean visible) {
        if (visible) {
            actionAddItem.setVisible(variantsExpandableAdapter.getCursor().getCount() < 3);
        } else {
            expandableListView.setOnItemLongClickListener(null);
            actionAddItem.setVisible(false);
        }
    }

    protected ActionMode getActionMode() {
        return null != actionModeRef ? actionModeRef.get() : null;
    }

    protected void setActionMode(final ActionMode mode) {
        actionModeRef = new WeakReference<ActionMode>(mode);
    }

    protected void stopActionMode() {
        if (actionModeRef != null && actionModeRef.get() != null) {
            actionModeRef.get().finish();
        }
        variantsExpandableAdapter.clearSelection();
    }

    protected void editVariantSubItem(final VariantSubItemModel itemModel) {
        new Handler().post(
                new Runnable() {
                    @Override
                    public void run() {
                        VariantSubItemDialogFragment.showToEdit(getActivity(), itemModel);
                    }
                });
    }

    private final AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            variantsExpandableAdapter.configureSelection(id, checked);
            mode.getMenu().findItem(R.id.action_edit).setVisible(variantsExpandableAdapter.hasOnlyChildInSelection() || variantsExpandableAdapter.hasOnlyGroupInSelection());
            mode.getMenu().findItem(R.id.action_add_sub_item).setVisible(variantsExpandableAdapter.hasOnlyGroupInSelection());
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.variants_actions, menu);
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
                case R.id.action_add_sub_item:
                    VariantSubItemDialogFragment.showToAdd(getActivity(), model.guid, variantsExpandableAdapter.getVariantItemModel().guid, 1);
                    mode.finish();
                    return true;
                case R.id.action_delete:
                    variantsExpandableAdapter.deleteSelected();
                    mode.finish();
                    return true;
                case R.id.action_edit:
                    if (variantsExpandableAdapter.hasOnlyGroupInSelection()) {
                        VariantItemDialogFragment.showToEdit(getActivity(), variantsExpandableAdapter.getVariantItemModel());
                    } else if (variantsExpandableAdapter.hasOnlyChildInSelection()) {
                        Bundle bundle = new Bundle(1);
                        bundle.putInt(VARIANT_SUB_ITEM_ID, variantsExpandableAdapter.getSelectedChildId());
                        getLoaderManager().restartLoader(VARIANT_SUB_ITEM_LOADER_TAG, bundle, loaderCallbacks);
                    }
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            variantsExpandableAdapter.clearSelection();
            setActionMode(null);
        }
    };

    private final LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
            switch (loaderId) {
                case VARIANT_ITEMS_LOADER_TAG:
                    return new CursorLoader(getActivity(),
                            URI_VARIANT_SUB_ITEMS_COUNT,
                            null,
                            null,
                            _selectionArgs(model.guid, TcrApplication.get().getShopId()),
                            null);
                case VARIANT_SUB_ITEM_LOADER_TAG:
                    return CursorLoaderBuilder.forUri(URI_VARIANT_SUB_ITEM).where(ShopStore.VariantSubItemTable.ID + "=?", String.valueOf(args.getInt(VARIANT_SUB_ITEM_ID)))
                            .build(getActivity());
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loaderId));
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            switch (loader.getId()) {
                case VARIANT_ITEMS_LOADER_TAG:
                    variantsExpandableAdapter.setGroupCursor(cursor);
                    variantsExpandableAdapter.clearSelection();
                    getActivity().invalidateOptionsMenu();
                    emptyText.setVisibility(cursor.getCount() > 0 ? View.GONE : View.VISIBLE);
                    break;
                case VARIANT_SUB_ITEM_LOADER_TAG:
                    cursor.moveToFirst();
                    editVariantSubItem(new VariantSubItemModel(cursor));
                    cursor.close();
                    getLoaderManager().destroyLoader(VARIANT_SUB_ITEM_LOADER_TAG);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loader.getId()));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            switch (loader.getId()) {
                case VARIANT_ITEMS_LOADER_TAG:
                    variantsExpandableAdapter.setGroupCursor(null);
                    break;
                case VARIANT_SUB_ITEM_LOADER_TAG:
                    getLoaderManager().destroyLoader(VARIANT_SUB_ITEM_LOADER_TAG);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loader.getId()));
            }
        }
    };

    public static void replace(FragmentManager manager, ItemExModel model) {
        Fragment f = VariantsListFragment_.builder().model(model).build();
        manager.beginTransaction().replace(R.id.container, f, FTAG).commit();
    }
}
